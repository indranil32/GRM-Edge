/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.ocnp.mgmt.grm_edge_service.APIServerWatcher;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import org.jboss.logging.MDC;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.att.ocnp.mgmt.grm_edge_service.businessprocess.K8ServiceController;
import com.att.ocnp.mgmt.grm_edge_service.types.K8Service;
import com.att.ocnp.mgmt.grm_edge_service.util.GRMEdgeConstants;
import com.att.ocnp.mgmt.grm_edge_service.util.GRMEdgeErrorGenerator;
import com.google.gson.Gson;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;

public class KubernetesServiceAPIWatcher implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(KubernetesServiceAPIWatcher.class.getName());

	private String apiUrl = "";
	
	private CountDownLatch latch;
	

	public KubernetesServiceAPIWatcher(String url){
		apiUrl = url;
	}

	/*
	 * This class adds service events to a serviceMap. The reason why we don't handle service events immediately is because the watchers start and stop constantly. 
	 */
	@Override
	public void run() {
		Thread.currentThread().setName("KubernetesServiceAPIWatcher");
		boolean loop = true;
		while(loop){
			latch = new CountDownLatch(1);
			watcher();
			try {
				latch.await();
			} catch (InterruptedException e) {
				logger.error(e.getMessage());
			}
			String sleepForWatcherRetry = System.getProperty("CPFRUN_GRM_EDGE_WATCHER_RECONNECT_SLEEP","5000");
			logger.error("Found unhealthy APIWatcher on url: {}. Restarting in {} ms" , apiUrl, sleepForWatcherRetry);
			try{
				Thread.sleep(Integer.valueOf(sleepForWatcherRetry));
			}
			catch(Exception e){
				logger.error("Error while trying to sleep");
			}
		}
	}
	
	private void watcher() {
		Config config = new ConfigBuilder().withMasterUrl(apiUrl).withOauthToken(System.getProperty("GRM_EDGE_WATCHER_TOKEN", "3ftLdxGOFyNOWzDBv4k6gIWMLMtbnbN9")).withTrustCerts(true).withWatchReconnectInterval(0).withWatchReconnectLimit(-1).build();
		try (final KubernetesClient client = new DefaultKubernetesClient(config)) {
			Watch watch = null;
			try {
				logger.info("Starting up APIWatcher on url: {}", apiUrl);
				watch = client.services().inAnyNamespace().watch(generateWatcher());

			} catch (Exception e) {
				if (watch != null){
					watch.close();
				}
				latch.countDown();
				logger.error(GRMEdgeErrorGenerator.generateErrorMessage(GRMEdgeConstants.WATCHER_EXCEPTION, e.getMessage()),e);
				KubernetesAPIWatcherControl.getInstance().setServiceWatcherStatus(apiUrl,false);
			}
		} catch (Exception e) {
			latch.countDown();
			logger.error(GRMEdgeErrorGenerator.generateErrorMessage(GRMEdgeConstants.WATCHER_EXCEPTION + " Unable to create kubernetesclient", e.getMessage()),e);
			KubernetesAPIWatcherControl.getInstance().setServiceWatcherStatus(apiUrl,false);
		}
		
	}

	private K8Service createK8Service(Service service) throws JSONException{
		Gson gson = new Gson();
		Map<String, String> metadata = new HashMap<>();
		JSONObject root = new JSONObject(gson.toJson(service));
		JSONObject metadataObj = root.getJSONObject("metadata");
		
		for(int i = 0; i < metadataObj.length(); i++){
			Iterator key = metadataObj.keys();
			while (key.hasNext()) {
				String n = (String) key.next();
				metadata.put(n, metadataObj.getString(n));
			}
		}
		
		String protocol = (service.getSpec().getPorts().get(0).getName() == null || service.getSpec().getPorts().get(0).getName().isEmpty() ? System.getProperty("CPFRUN_GRMEDGE_DEFAULT_PROTOCOL",GRMEdgeConstants.CPFRUN_GRMEDGE_DEFAULT_PROTOCOL) : service.getSpec().getPorts().get(0).getName());
		Map<String, String> selector = (service.getSpec() == null || service.getSpec().getSelector() == null) ? new HashMap<String, String>() : service.getSpec().getSelector();
		String port = service.getSpec() == null || service.getSpec().getPorts() == null || service.getSpec().getPorts().get(0) == null || service.getSpec().getPorts().get(0).getNodePort() == null ? "" : service.getSpec().getPorts().get(0).getNodePort().toString();
		return new K8Service(metadata, port, protocol, selector);
	}
	
	private Watcher<Service> generateWatcher(){
		KubernetesAPIWatcherControl.getInstance().setServiceWatcherStatus(apiUrl,true);
		return new Watcher<Service>() {			
			
			@Override
			public void eventReceived(Action action, Service service) {
				
				MDC.put(GRMEdgeConstants.Tracking, UUID.randomUUID().toString());
				try {
					K8Service k8Service;
					if (action.equals(Action.ADDED)) {
						logger.debug("Add Event Received");
						k8Service = createK8Service(service);
						K8ServiceController.addServices(k8Service);
					} else if (action.equals(Action.DELETED)) {
						logger.debug("Delete Event Received");
						k8Service = createK8Service(service);
						K8ServiceController.deleteServices(k8Service);
					} else if (action.equals(Action.MODIFIED)){
						logger.debug("Modify Event Received");
						k8Service = createK8Service(service);
						K8ServiceController.modifyService(k8Service);
					}
					else{
						logger.debug("Received an event from kubernetes with a unexpected Action {}", action);
						return;
					}
					logger.debug("Performed Action {} on service Event JSON: {}" ,action, k8Service.toString());
				} catch (Exception e) {
					logger.error(GRMEdgeErrorGenerator.generateErrorMessage(GRMEdgeConstants.WATCHER_EXCEPTION, e.getMessage()),e);
				}
			}

			@Override
			public void onClose(KubernetesClientException e) {
				if (e != null) {
					logger.error(GRMEdgeErrorGenerator.generateErrorMessage(GRMEdgeConstants.WATCHER_EXCEPTION, e.getMessage()),e);
				}
				else{
					logger.error(GRMEdgeErrorGenerator.generateErrorMessage(GRMEdgeConstants.WATCHER_EXCEPTION, "GRMEdge KubernetesPodAPIWatcher received valid shutdown but we are restarting it instead."));
				}
				//marks this watcher as dead
				KubernetesAPIWatcherControl.getInstance().setServiceWatcherStatus(apiUrl,false);
				latch.countDown();
			}
		};
	}
	
}