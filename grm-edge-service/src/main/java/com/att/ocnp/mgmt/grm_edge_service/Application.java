/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.ocnp.mgmt.grm_edge_service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;

import com.att.ocnp.mgmt.grm_edge_service.businessprocess.K8ServiceController;
import com.att.ocnp.mgmt.grm_edge_service.businessprocess.APIWatcherAndThreadSupervisor;
import com.att.ocnp.mgmt.grm_edge_service.cache.RouteInfoCache;
import com.att.ocnp.mgmt.grm_edge_service.cache.ServiceRegistry;
import com.att.ocnp.mgmt.grm_edge_service.types.K8Service;
import com.att.ocnp.mgmt.grm_edge_service.types.KubePod;
import com.att.ocnp.mgmt.grm_edge_service.util.GRMEdgeConstants;
import com.att.ocnp.mgmt.grm_edge_service.util.GRMEdgeUtil;

@SpringBootApplication
@ComponentScan(basePackages = "com")
@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class })
public class Application extends SpringBootServletInitializer {
	private static final Logger edgelogger = LoggerFactory.getLogger(Application.class.getName());

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(Application.class);
	}

	public static void main(String[] args) throws Exception {
		
		try {
			initConfig();
			initToken();
			initServiceRequirements();
		} catch (Exception e) {
			edgelogger.error("Exception in initializing", e);
		}
		
		SpringApplication.run(Application.class, args);
	}
	
	private static void initServiceRequirements(){
		/*
		 * get lat and long and store it
		 */
		if (System.getProperty("AFT_LATITUDE") != null && System.getProperty("AFT_LONGITUDE") != null) {
			GRMEdgeUtil.setLatAndLong(System.getProperty("AFT_LATITUDE"), System.getProperty("AFT_LONGITUDE"));
		} else {
			edgelogger.error("Unable to get AFT_LATITUDE or AFT_LONGITUDE. They are required. Exiting GRM Edge.");
			System.exit(1);
		}

		if (System.getProperty("CPFRUN_GRMEDGE_DEFAULT_ROUTEOFFER") != null){
			GRMEdgeUtil.setRouteOfferDefault(System.getProperty("CPFRUN_GRMEDGE_DEFAULT_ROUTEOFFER"));
		}
		else{
			GRMEdgeUtil.setRouteOfferDefault("DEFAULT");
		}
		
		if (System.getProperty("CPFRUN_GRMEDGE_DEFAULT_ENV") != null){
			GRMEdgeUtil.setEnvDefault(System.getProperty("CPFRUN_GRMEDGE_DEFAULT_ENV"));
		}
		else{
			String defaultEnv = "LAB";
			edgelogger.error("Unable to parse CPFRUN_GRMEDGE_DEFAULT_ENV. Setting ENV to default: {}" , defaultEnv);
			GRMEdgeUtil.setEnvDefault(defaultEnv);
		}
		
		String clustername = System.getProperty("ocnp_cluster_name");
		if (clustername!= null && !clustername.equalsIgnoreCase("null") && !clustername.equalsIgnoreCase("")){
			GRMEdgeUtil.setClusterName(clustername);
		}
		else{
			edgelogger.warn("Cluster name is not set. Setting to CLUSTER_NAME_NOT_SET.");
			GRMEdgeUtil.setClusterName("CLUSTER_NAME_NOT_SET");
		}
		
		
		try {
			List<String> urls = GRMEdgeUtil.getAPIServerUrls();
						
			try{
				initCache(urls);
			}
			catch(Exception e){
				edgelogger.error("Exception in initializing", e);
				System.exit(1);
			}
			
			edgelogger.trace("Starting up TheadSupervisor");
			Thread supervisor = new Thread(new APIWatcherAndThreadSupervisor(urls));
			supervisor.start();
			edgelogger.trace("ThreadSupervisor started up");
			
			try{
				initRouteInfoOperations(urls);
			} catch(Exception e){
				edgelogger.error("Exception in initializing", e);
				System.exit(1);
			}
		} catch (Exception e) {
			edgelogger.error("Exception in initializing", e);
			System.exit(1);
		}		
	}

	private static void initCache(List<String> urls) throws Exception{
		edgelogger.info("===Initializing Cache===");
		boolean emptyEndPointCache = ServiceRegistry.getInstance().getCache(GRMEdgeConstants.ENDPOINT_CACHE).isEmpty();
		boolean emptyPodCache = ServiceRegistry.getInstance().getCache(GRMEdgeConstants.POD_CACHE).isEmpty();
		boolean emptyServiceCache = ServiceRegistry.getInstance().getCache(GRMEdgeConstants.SERVICE_CACHE).isEmpty();

		/*
		 * If the cache data is empty OR CPFRUN_GRMEDGE_LOAD_K8DATA_ON_STARTUP is set to true. Load all data from the api server
		 * 
		 */
		if((System.getProperty("CPFRUN_GRMEDGE_LOAD_K8DATA_ON_STARTUP","false").equalsIgnoreCase("true")) || (emptyEndPointCache && emptyPodCache && emptyServiceCache)){

			// If we use the old way, i.e. clearing the cache and then add
			if (System.getProperty("CPFRUN_GRMEDGE_CLEAR_K8DATA_ON_STARTUP","false").equalsIgnoreCase("true")) {
				edgelogger.debug("Found empty cache or CPFRUN_GRMEDGE_LOAD_K8DATA_ON_STARTUP=true && CPFRUN_GRMEDGE_CLEAR_K8DATA_ON_STARTUP=true. Clearing Caches and Loading data from Kubernetes");

				//clear the 3 caches we use
				for(Object o: ServiceRegistry.getInstance().getCache(GRMEdgeConstants.ENDPOINT_CACHE).getKeySet()){
					ServiceRegistry.getInstance().getCache(GRMEdgeConstants.ENDPOINT_CACHE).remove(o);
				}
				for(Object o: ServiceRegistry.getInstance().getCache(GRMEdgeConstants.POD_CACHE).getKeySet()){
					ServiceRegistry.getInstance().getCache(GRMEdgeConstants.POD_CACHE).remove(o);
				}
				for(Object o: ServiceRegistry.getInstance().getCache(GRMEdgeConstants.SERVICE_CACHE).getKeySet()){
					ServiceRegistry.getInstance().getCache(GRMEdgeConstants.SERVICE_CACHE).remove(o);
				}

				emptyEndPointCache = ServiceRegistry.getInstance().getCache(GRMEdgeConstants.ENDPOINT_CACHE).isEmpty();
				emptyPodCache = ServiceRegistry.getInstance().getCache(GRMEdgeConstants.POD_CACHE).isEmpty();
				emptyServiceCache = ServiceRegistry.getInstance().getCache(GRMEdgeConstants.SERVICE_CACHE).isEmpty();

				edgelogger.debug(GRMEdgeConstants.ENDPOINT_CACHE + " clear: " + emptyEndPointCache+"\n"
						+ GRMEdgeConstants.POD_CACHE + " clear: " + emptyPodCache+"\n"
						+ GRMEdgeConstants.SERVICE_CACHE + " clear: " + emptyServiceCache+"\n");

				List<String> namespaceNames = new ArrayList<>();
				for(String url: urls){
					String fullpath = url + "api/v1/namespaces";
					String auth = "Bearer " + System.getProperty("GRM_EDGE_WATCHER_TOKEN");
					try{

						String json = GRMEdgeUtil.sendRequest(fullpath,"","GET",auth);
						JSONObject root = new JSONObject(json);
						JSONArray namespaces = root.getJSONArray("items");
						for(int i = 0; i < namespaces.length();i++){
							JSONObject namespaceObj = namespaces.getJSONObject(i);
							JSONObject metadata = namespaceObj.getJSONObject("metadata");
							namespaceNames.add(metadata.getString("name"));
						}

						//for each namespace get all services and pods
						for(String namespace: namespaceNames){

							//populate this by calling GET http://localhost:8080/api/v1/namespaces/<namespace>/pods
							fullpath = url + "api/v1/namespaces/"+namespace+"/pods";
							json = GRMEdgeUtil.sendRequest(fullpath,"","GET",auth);
							root = new JSONObject(json);
							try{
								JSONArray pods = root.getJSONArray("items");
								for(int i = 0; i < pods.length();i++){
									try{
										JSONObject podObj = pods.getJSONObject(i);
										KubePod pod = GRMEdgeUtil.convertJSONObjectToKubePod(podObj);
										if (pod == null){
											throw new Exception();
										}
										K8ServiceController.addPod(pod);
									}
									catch(Exception e){
										edgelogger.error("Pod did not have required attributes: " + pods.getJSONObject(i).toString());
									}
								}	
							}
							catch(JSONException e){
								edgelogger.error("No pods found for namespace: {}" ,namespace);
							}
							catch(Exception e){
								edgelogger.error("Received error while trying to load pods from API server.",e);
							}

							//populate this by calling GET http://localhost:8080/api/v1/namespaces/<namespace>/services
							fullpath = url + "/api/v1/namespaces/"+namespace+"/services";
							json = GRMEdgeUtil.sendRequest(fullpath,"","GET",auth);
							root = new JSONObject(json);
							try{
								JSONArray services = root.getJSONArray("items");
								for(int i = 0; i < services.length();i++){
									try{
										JSONObject serviceObj = services.getJSONObject(i);
										K8Service service = GRMEdgeUtil.convertJSONObjectToK8Service(serviceObj);
										if(service == null){
											throw new Exception();
										}
										K8ServiceController.addServices(service);	
									}
									catch(Exception e){
										edgelogger.error("Service did not have required attributes: " + services.getJSONObject(i).toString());
									}
								}	
							}
							catch(JSONException e){
								edgelogger.error("No services found for namespace: {}" , namespace);
							}
							catch(Exception e){
								edgelogger.error("Received error while trying to load services from API server.",e);
							}
						}		
					}
					catch(Exception e){
						edgelogger.error("Error occured while reading Kubernetes API for cache data",e);
					}
				}

				// The new way is to use replace rather than add (OCNP-687)
			} else {

				edgelogger.debug("Found empty cache or CPFRUN_GRMEDGE_LOAD_K8DATA_ON_STARTUP=true. Loading data from Kubernetes and doing replace");
				GRMEdgeUtil.replaceCache(urls);
			}
		}

		edgelogger.trace("===Done Initializing Cache===");
	}

	/*
	 * Loads the data from grmedge.properties configmap. Sets them as system properties for use.
	 */
	private static void initConfig() throws Exception {

		Properties props = new Properties();
		File file = new File("/grmedge-config/grmedgeprops.properties");
		
		try(FileInputStream in = new FileInputStream(file)) {	
			props.load(in);
		} catch (Exception e) {
			edgelogger.error("Cannot load properties from /grmedge-config/grmedgeprops.properties, grmedge will now exit.", e);
			System.exit(1);
		}

		try {
			for (Object key : props.keySet()) {
				System.setProperty((String)key, props.getProperty((String)key));
			}
		} catch (Exception e) {
			edgelogger.error("Cannot set properties to System, grmedge will now exit.", e);
			System.exit(1);
		}

		props = new Properties();		
		int count = 0;
		boolean loaded = false;
		while(count < 3){
			file = new File("/ocnp-cluster-info/properties");
			
			try(FileInputStream in = new FileInputStream(file)) {	
				props.load(in);
			} catch (Exception e) {
				edgelogger.error("Cannot load properties from /ocnp-cluster-info/properties, grmedge will try again.", e);
				Thread.sleep(2500);
			}

			try {
				for (Object key : props.keySet()) {
					loaded = true;
					System.setProperty((String)key, props.getProperty((String)key));
					edgelogger.trace("Property {} found with value {}",(String)key, props.getProperty((String)key));
				}
			} catch (Exception e) {
				edgelogger.error("Cannot set properties to System, grmedge will now exit.", e);
				System.exit(1);
			}
			if(loaded){
				break;
			}
			count++;
		}
		if(!loaded){
			edgelogger.error("Cannot load properties from /ocnp-cluster-info/properties, grmedge will not exit.");
			System.exit(1);
		}
	}
	
	/*
	 * Loads the token for use in calls to the api server. sets it as a system property for use later
	 */
	private static void initToken() throws Exception {
		File file = new File("/var/run/secrets/kubernetes.io/serviceaccount/token");
		
		String token = null;
		try(FileInputStream in = new FileInputStream(file);	BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
			String line = null;
			while ((line = br.readLine()) != null) {
				if(StringUtils.isEmpty(line)){
					throw new Exception("Line is empty. Token cannot be set to an empty line");
				}
				token = line;
			}
		} catch (Exception e) {
			edgelogger.error("Cannot load properties from /grmedge-config/grmedgeprops.properties, grmedge will now exit.", e);
			System.exit(1);
		}
		
		edgelogger.trace("Setting GRM_EDGE_WATCHER_TOKEN to: {}",token );
		
		System.setProperty("GRM_EDGE_WATCHER_TOKEN", token);
	}
	
	/*
	 * Loads the data from dmerouteinfo.properties configmap. This caches the routeinfo xml for GRM-Edge.
	 */
	private static void initRouteInfoOperations(List<String> urls) throws Exception {
		
		RouteInfoCache cacheInstance = null;
		
		/*
		 * First, initialize the cache.
		 */
		try {
			cacheInstance = RouteInfoCache.getInstance(urls);
		} catch (Exception e) {
			edgelogger.error("Cannot initialize the routeinfo xml cache", e);
			return;
		}
		
		
		/*
		 * Secondly, check if the cache is initialized.
		 */
		if (cacheInstance == null) {
			edgelogger.error("Routeinfo xml cache not initialized");
			return;
		}
		
		
		/*
		 * Lastly, we put the routeinfo xml from the config map into the cache.
		 */
		try {
			if (cacheInstance.getCache().getKeySet() == null) {
				cacheInstance.refreshCacheFromConfigMap();
			}
		} catch (Exception e) {
			edgelogger.error("Cannot load routeinfo xml to internal cache", e);
		}
	}	
}