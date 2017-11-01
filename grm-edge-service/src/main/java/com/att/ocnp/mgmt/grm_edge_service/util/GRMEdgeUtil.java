/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.ocnp.mgmt.grm_edge_service.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.att.ocnp.mgmt.grm_edge_service.businessprocess.K8ServiceController;
import com.att.ocnp.mgmt.grm_edge_service.cache.ServiceRegistry;
import com.att.ocnp.mgmt.grm_edge_service.types.K8Service;
import com.att.ocnp.mgmt.grm_edge_service.types.KubePod;
import com.att.scld.grm.types.v1.NameValuePair;
import com.att.scld.grm.types.v1.OperationalInfo;
import com.att.scld.grm.types.v1.ServiceEndPoint;
import com.att.scld.grm.types.v1.Status;
import com.att.scld.grm.types.v1.StatusInfo;
import com.att.scld.grm.types.v1.VersionDefinition;
import com.att.scld.grm.v1.FindRunningServiceEndPointRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GRMEdgeUtil {

	private static final Logger logger = LoggerFactory.getLogger(GRMEdgeUtil.class.getName());
	public static final String NAME_SEP = ":";
	public static final String SERVICE_NAME_REGEX_PATTERN = "(?<!\\\\)\\.";
	public static final String VERSION_SEP = ".";

	private static String edgeLat;
	private static String edgeLong;
	private static String routeofferDefault;
	private static String envDefault;
	private static final Boolean dashToDotEnabled = Boolean.valueOf(System.getProperty("GRM_EDGE_ENABLE_DASH_TO_DOT_CONVERSION", "true"));
	private static String edgeClusterName;
	
	public static void setClusterName(String clusterName){
		edgeClusterName = clusterName;
		logger.info("Cluster name set to: " + edgeClusterName);
	}
	
	public static String getClusterName(){
		return edgeClusterName;
	}
	
	//  Sets the default value, populated by grmedge-config configmap or set to 1
	public static void setLatAndLong(String edgeLat2,String edgeLong2){
		edgeLat = edgeLat2;
		edgeLong = edgeLong2;
	}

	// Sets the default value, populated by grmedge-config configmap
	public static void setRouteOfferDefault(String routeoffer){
		routeofferDefault = routeoffer;
	}

	// Sets the default value, populated by grmedge-config configmap
	public static void setEnvDefault(String env){
		envDefault = env;
	}
	
	// returns the default value, populated by grmedge-config configmap
	public static String getEnvDefault(){
		return envDefault;
	}
	
	// Sets the default value, populated by grmedge-config configmap
	public static String getRouteOfferDefault(){
		return routeofferDefault;
	}

	/**
	 * Method to return whether or not the Endpoint belongs to the current GRMEdge Cluster
	 * 
	 * Return false if SEP does not have cluster data
	 * Return true if SEP data matches current cluster 
	 * 
	 * @param sep
	 * @return boolean
	 */
	public static boolean sepInCurrentCluster(ServiceEndPoint sep) {
		String clusterName = getClusterNameFromSEP(sep);
		if(clusterName == null || clusterName.isEmpty()){
			return false;
		}
		if(edgeClusterName.equals(clusterName)){
			return true;
		}
		return false;
	}
	
	public static String getClusterNameFromSEP(ServiceEndPoint sep) {
		for(NameValuePair nvp: sep.getProperties()){
			if(nvp.getName().equalsIgnoreCase("cpfrun_cluster_name")){
				return nvp.getValue();
			}
		}
		return "";
	}
	
	/**
	 * 
	 * In a namespace like com-att-ocnp-mgmt, it will return com-att-ocnp-mgmt
	 * In a namespace like com-att-ocnp-mgmt-dev, it will return com-att-ocnp-mgmt
	 * 
	 * @param namespaceWithOrWithoutEnv
	 * @param env in any case
	 * @return the appropriate namespace value for a ServiceEndPoint
	 */
	public static String removeEnvFromNamespace(String namespaceWithOrWithoutEnv, String env){
		if(namespaceWithOrWithoutEnv.endsWith("-"+env.toLowerCase())){
			namespaceWithOrWithoutEnv = namespaceWithOrWithoutEnv.substring(0, namespaceWithOrWithoutEnv.lastIndexOf("-"+env.toLowerCase()));
		}
		return namespaceWithOrWithoutEnv;
	}
	/*
	 * Create a ServiceEndPoint from K8Service and KubePod
	 * They will match up based on their selector and label values. This is also how kubernetes matches
	 * 
	 */
	public static ServiceEndPoint createSEP(KubePod pod, K8Service k8service){
		logger.trace("Creating SEP from pod{{}} and k8service{{}}",pod.toString(),k8service.toString());

		ServiceEndPoint sep = new ServiceEndPoint();

		NameValuePair nvp = new NameValuePair();
		nvp.setName("Environment");
		
		if(System.getProperty("GRM_EDGE_DISABLE_ENV_PARSING","false").equalsIgnoreCase("true")){
			nvp.setValue(GRMEdgeUtil.envDefault);
		}
		else{
			nvp.setValue(GRMEdgeUtil.getEnvNameSpaceWithDashes(k8service.getMetadata().get("namespace")));	
		}
		sep.getProperties().add(nvp);
		
		String k8NamespaceNameWithoutEnv = removeEnvFromNamespace(k8service.getMetadata().get("namespace"), nvp.getValue());
		String k8Namespace = k8service.getMetadata().get("namespace");
		
		if (dashToDotEnabled) {
			k8NamespaceNameWithoutEnv = k8NamespaceNameWithoutEnv.replace('-','.');
			k8Namespace = k8Namespace.replace('-','.');
		}
		
		if(System.getProperty("GRM_EDGE_DISABLE_ENV_PARSING","false").equalsIgnoreCase("true")){
			sep.setName(k8Namespace+"."+k8service.getLabelSelector().get("app"));
		}
		else{
			sep.setName( k8NamespaceNameWithoutEnv + "." + k8service.getLabelSelector().get("app"));	
		}
		
		
		String version = k8service.getLabelSelector().get("version");
		VersionDefinition vd = GRMEdgeUtil.getVersionDefinitionFromString(version);
		if (vd != null){
			sep.setVersion(GRMEdgeUtil.getVersionDefinitionFromString(version));	
		}

		String host = pod.getHostIP();
		if (System.getProperty("GRM_EDGE_REGISTER_ENDPOINTS_WITH_FULL_HOSTNAME", "false").equalsIgnoreCase("true")) {
			try {
				InetAddress addr = InetAddress.getByName(pod.getHostIP());
				host = addr.getHostName();
			} catch (Exception e) {
				host = pod.getHostIP();
			}
			sep.setHostAddress(host);
		} else {
			sep.setHostAddress(host);
		}
		
		sep.setListenPort(k8service.getNodePort());
		sep.setContextPath("/");
		if (null != pod.getLabels().get("routeoffer")) {
			sep.setRouteOffer(pod.getLabels().get("routeoffer"));
		} else {
			sep.setRouteOffer(routeofferDefault);
		}

		NameValuePair nvp2 = new NameValuePair();
		nvp2.setName("Kubernetes Namespace");
		nvp2.setValue(k8service.getMetadata().get("namespace"));
		sep.getProperties().add(nvp2);
        
		nvp = new NameValuePair();
		nvp.setName("cpfrun_cluster_name");
		nvp.setValue(GRMEdgeUtil.getClusterName());
		sep.getProperties().add(nvp);
		
        //get this service's annotations and register
        //each annotation as a service end point property
        String annotations = k8service.getMetadata().get("annotations");
        if (annotations != null)
        {
            List<NameValuePair> properties = getPropertiesFromAnnotations(annotations);
            for (int i = 0; i < properties.size(); i++)
            {
                //don't allow these to overwrite properties already written
                if (!properties.get(i).getName().equalsIgnoreCase("Environment") &&
                    !properties.get(i).getName().equalsIgnoreCase("Kubernetes Namespace") && !properties.get(i).getName().equalsIgnoreCase("cpfrun_cluster_name"))
                {
                    sep.getProperties().add(properties.get(i));
                }
            }
        }
        
		sep.setLatitude(edgeLat);
		sep.setLongitude(edgeLong);

		GregorianCalendar c = new GregorianCalendar();
		c.setTimeZone(TimeZone.getTimeZone("GMT"));
		Date d = new Date();
		c.setTime(d);
		try {
			
			sep.setRegistrationTime(DatatypeFactory.newInstance().newXMLGregorianCalendar(c));
			GregorianCalendar c2 = new GregorianCalendar();
			Date myDate;
			Calendar cal = Calendar.getInstance();
			cal.setTimeZone(TimeZone.getTimeZone("GMT"));
			cal.set(Calendar.MONTH, 9);
			cal.set(Calendar.DATE, 9);
			cal.set(Calendar.YEAR, 9999);
			myDate = cal.getTime();
			c2.setTime(myDate);
			XMLGregorianCalendar date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c2);
			sep.setExpirationTime(date2);
			StatusInfo status = new StatusInfo();
			//We are only adding pods if they are running, so the SEP has to be running
			status.setStatus(Status.RUNNING);
			sep.setStatusInfo(status);
			sep.setValidatorStatusInfo(status);
			sep.setEventStatusInfo(status);
			OperationalInfo info = new OperationalInfo();
			info.setCreatedBy("edge");
			info.setCreatedTimestamp(DatatypeFactory.newInstance().newXMLGregorianCalendar(c));
			info.setUpdatedBy("edge");
			info.setUpdatedTimestamp(info.getCreatedTimestamp());
			sep.setOperationalInfo(info);
			sep.setProtocol(k8service.getProtocol());

		} catch (DatatypeConfigurationException e) {
			logger.error(GRMEdgeErrorGenerator.generateErrorMessage(GRMEdgeConstants.SEP_CREATE_ERROR,e.getMessage()));
			return null;
		}

		if(StringUtils.isEmpty(getEnv(sep))){
			logger.warn(GRMEdgeErrorGenerator.generateErrorMessage(GRMEdgeConstants.SEP_ENV_MISSING,sep.toString()));
			return null;
		}
		if(StringUtils.isEmpty(sep.getListenPort())){
			logger.warn(GRMEdgeErrorGenerator.generateErrorMessage(GRMEdgeConstants.SEP_PORT_MISSING,sep.toString()));
			return null;
		}
		if(StringUtils.isEmpty(sep.getHostAddress())){
			logger.warn(GRMEdgeErrorGenerator.generateErrorMessage(GRMEdgeConstants.SEP_HOST_MISSING,sep.toString()));
			return null;
		}
		if(sep.getVersion() == null){
			logger.warn(GRMEdgeErrorGenerator.generateErrorMessage(GRMEdgeConstants.SEP_VERSION_MISSING,sep.toString()));
			return null;
		}

		logger.trace("SEP Created info: {}",sep.toString());
		return sep;
	}

	/*
	 * This method is used when reading directly from API server on startup. Converts the JSON received into a K8Service object
	 */
	public static K8Service convertJSONObjectToK8Service(JSONObject servicesJson) {
		Map<String, String> metadata = new HashMap<>();
		String nodeport = "";
		String protocol = "";
		Map<String, String> labelSelector = new HashMap<>();
		
		JSONObject root;
		try {
			root = servicesJson;
			JSONObject metadataObj = root.getJSONObject("metadata");
			JSONObject specObj = root.getJSONObject("spec");
			JSONArray ports = specObj.getJSONArray("ports");
			JSONObject firstPortObj;
			try{
				firstPortObj = ports.getJSONObject(0);	
			}
			catch(JSONException e){
				firstPortObj = null;
			}
			JSONObject selectorObj;
			try{
				selectorObj = specObj.getJSONObject("selector");
			}
			catch(JSONException e){
				selectorObj = null;
			}

			//get metadata
			for(int i = 0; i < metadataObj.length(); i++){
				Iterator key = metadataObj.keys();
				while (key.hasNext()) {
					String n = (String) key.next();
					metadata.put(n, metadataObj.getString(n));
				}
			}

			try{
				if(firstPortObj != null){
					if(StringUtils.isNotEmpty(firstPortObj.getString("nodePort"))){
						nodeport = firstPortObj.getString("nodePort");	
					}
				}
			}
			catch(JSONException e){
				nodeport = null;
			}

			try{
				if(firstPortObj != null){
					if(firstPortObj.getString("name") != null){
						if(firstPortObj.getString("name").isEmpty()){
							protocol = "http";
						}
						else
							protocol = firstPortObj.getString("name");	
					}
				}
			}
			catch(JSONException e){
				protocol = "http";
			}

			if(selectorObj != null){
				//get labelselector
				for(int i = 0; i < selectorObj.length(); i++){
					Iterator key = selectorObj.keys();
					while (key.hasNext()) {
						String n = (String) key.next();
						labelSelector.put(n, selectorObj.getString(n));
					}
				}	
			}
		}
		catch(JSONException e){
			logger.error(GRMEdgeErrorGenerator.generateErrorMessage(GRMEdgeConstants.JSON_PARSE_ERROR,e.getMessage()));
		}



		String env;
		
		if(System.getProperty("GRM_EDGE_DISABLE_ENV_PARSING","false").equalsIgnoreCase("true")){
			env = envDefault;
		} else {
			env = GRMEdgeUtil.getEnvNameSpaceWithDashes(metadata.get("namespace"));
		}	
		
		//validation
		if(metadata.get("name") == null || labelSelector.get("version") == null || env == null || env.length() == 0){
			logger.warn(GRMEdgeErrorGenerator.generateErrorMessage(GRMEdgeConstants.K8SERVICE_MISSING_REQUIRED,"name,version,env,nodePort",metadata.get("name")+","+labelSelector.get("version")+","+env+","+nodeport));
			return null;
		}
		return new K8Service(metadata,nodeport,protocol,labelSelector);
	}

	/*
	 * This method is used when reading directly from API server on startup. Converts the JSON received into a KubePod object
	 */
	public static KubePod convertJSONObjectToKubePod(JSONObject podJson){
		String name = "";
		Map<String, String> labels = new HashMap<>();
		Map<String, String> annotations = new HashMap<>();
		String hostIP = "";
		String namespace = "default";

		JSONObject root;
		try {
			root = podJson;
			JSONObject metadataObj = root.getJSONObject("metadata");
			JSONObject labelsObj;
			try{
				labelsObj = metadataObj.getJSONObject("labels");	
			}
			catch(JSONException e){
				labelsObj = null;
			}
			JSONObject annotationObj;
			try{
				annotationObj = metadataObj.getJSONObject("annotations");
			}
			catch(JSONException e){
				annotationObj = null;
			}
			JSONObject status = root.getJSONObject("status");

			try{
				namespace = metadataObj.getString("namespace");
			}
			catch(JSONException e){
				namespace = "default";
			}
			
			//get name
			name = metadataObj.getString("name");

			// get labels
			if(labelsObj != null){
				for(int i = 0; i < labelsObj.length(); i++){
					Iterator key = labelsObj.keys();
					while (key.hasNext()) {
						String n = (String) key.next();
						labels.put(n, labelsObj.getString(n));
					}
				}	
			}

			// get annotations
			if(annotationObj != null){
				for(int i = 0; i < annotationObj.length(); i++){
					Iterator key = annotationObj.keys();
					while (key.hasNext()) {
						String n = (String) key.next();
						annotations.put(n, annotationObj.getString(n));
					}
				}	
			}

			//get hostIP
			hostIP = status.getString("hostIP").trim();

		} catch (JSONException e) {
			logger.error("JSONException occured while converting JSON to kubepod",e);
		}

		//validate to make sure we got valid stuff...
		if ( name == null || hostIP == null || name.length()  == 0 || hostIP.length() == 0 ){
			logger.warn(GRMEdgeErrorGenerator.generateErrorMessage(GRMEdgeConstants.POD_MISSING_REQUIRED,"name,hostIP",name+","+hostIP));

			return null;
		}

		return new KubePod(name, namespace, labels, annotations, hostIP);
	}	

	/**
	 * Converts a String version in format "1.0.0" to a VersionDefinitionObject
	 * <p>
	 * Returns null if one of the following is true:
	 * 		<ul>
	 * 			<li> version == null </li>
	 * 			<li> version.isEmpty() </li>
	 *      	<li> !version.contains(".") </li>
	 * 		</ul>
	 * </p>
	 * 
	 * @param version
	 * @return VersionDefinition 
	 */
	public static VersionDefinition getVersionDefinitionFromString(String version) {
		if(version == null || version.isEmpty()){
			return null;
		}
		try{
			VersionDefinition vd = new VersionDefinition();
	
			String[] versionArr = version.split("\\.");
			if(versionArr.length < 3){
				logger.error("Did not find Major or Minor version in value: {}" ,version);
				return null;
			}
			vd.setMajor(Integer.parseInt(versionArr[0]));
			vd.setMinor(Integer.parseInt(versionArr[1]));
			String patch = "";
			for(int i = 2; i < versionArr.length; i++){
				patch = patch+versionArr[i];
			}
			if(!patch.isEmpty()) 
				vd.setPatch(patch);
		    return vd;
		}   
		catch(NumberFormatException e){
			logger.error("NumberFormatException while parsing version: " + version,e);
			return null;
		}
	}

	public static String getEnv(ServiceEndPoint sep) {
		for(NameValuePair nvp: sep.getProperties()){
			if(nvp.getName().equalsIgnoreCase("Environment")){
				return nvp.getValue().toUpperCase();
			}
		}
		return null;
	}

	/*
	 * the cache should have ENV+serviceName as its key. This is trying to parse out the serviceName from the cache key
	 */
	public static String extractServiceName(String string, String env) {
		logger.trace("Extracting extractServiceName from service name:{}  and env :{} ",string, env);
		if (env == null || string.length()==env.length()){
			return null;
		}
		return string.substring(env.length());
	}

	/*
	 * The cache should have ENV+serviceName as its key. The reason for this is because ServiceEndPoint does not have an environment property. We need to have env.
	 * This is trying to parse out the env from the service name
	 */
	public static String extractEnvironment(String string) {
		
		logger.trace("Extracting environment from service name: {}" , string);
		
		if(string.length() < 3 ){
			return null;
		}
		
		String env = string.substring(0,3);
		if (env.equalsIgnoreCase("DEV") || env.equalsIgnoreCase("UAT") || env.equalsIgnoreCase("LAB")){
			return env;
		}
		
		if(string.length() < 4 ){
			return null;
		}
		
		env = string.substring(0,4);
		if (env.equalsIgnoreCase("PROD") || env.equalsIgnoreCase("TEST") || env.equalsIgnoreCase("PERF")){
			return env;
		}
		return null;
	}
	
	//expecting com-att-dev. will parse out the string after the last hyphen. If not able to convert it to a known env, it will use the default env value
	public static String getEnvNameSpaceWithDashes(String namespaceOnly) {
		String env;
		String temp = namespaceOnly;
		if(!temp.contains("-")){
			return envDefault;
		}
		try{
			env = temp.substring(temp.lastIndexOf('-')+1);
			if(env.length() < 3 ){
				return envDefault;
			}
			if (env.equalsIgnoreCase("DEV") || env.equalsIgnoreCase("UAT") || env.equalsIgnoreCase("LAB")){
				return env.toUpperCase();
			}

			if(env.length() < 4 ){
				return envDefault;
			}
			if (env.equalsIgnoreCase("PROD") || env.equalsIgnoreCase("TEST") || env.equalsIgnoreCase("PERF")){
				return env.toUpperCase();
			}
			return envDefault;
		}
		catch(Exception e){
			env = envDefault;
		}
		return env.toUpperCase();
	}

	/**
	 * Returns a List of all KubePods that have matching KubePod.getLabels() to the passed in K8Service selector labels
	 * 
	 * @param k8Service K8Service with an app name in the labelSelector.
	 * @return List<KubePod> with matching app name
	 */
	public static List<KubePod> getMatchingPodTags(K8Service k8Service) {
		Iterator iter = ServiceRegistry.getInstance().getCache(GRMEdgeConstants.POD_CACHE).values().iterator();
		List<KubePod> matchingPods = new ArrayList<>();

		while(iter.hasNext()){
			KubePod pod = (KubePod) iter.next();
			//does the pod and service have same app label
			if(hasMatchingTags(k8Service,pod) && hasMatchingNamespace(k8Service,pod)){
				//create SEP with this k8 service and pod data
				matchingPods.add(pod);
				logger.trace("Match pod details: {}", pod.toString());
			}
		}
		return matchingPods;
	}
	
	public static List<K8Service> getMatchingServiceTags(KubePod pod){
		Iterator iter = ServiceRegistry.getInstance().getCache(GRMEdgeConstants.SERVICE_CACHE).values().iterator();
		List<K8Service> matchingServices = new ArrayList<>();

		while(iter.hasNext()){
			K8Service service = (K8Service) iter.next();
			//does the pod and service have same app label
			if(hasMatchingTags(service,pod) && hasMatchingNamespace(service,pod)){
				//create SEP with this k8 service and pod data
				matchingServices.add(service);
				logger.trace("Match service details: {}" ,service.toString());
			}
		}
		return matchingServices;
	}
	
	public static boolean podMatchingOnSameHost(KubePod podFound, KubePod podToDelete, Iterator services) {
		while(services.hasNext()){
			K8Service aService = (K8Service) services.next();
			if (hasMatchingTags(aService, podFound) && hasMatchingTags(aService, podToDelete)
					&& hasMatchingNamespace(aService, podFound) && hasMatchingNamespace(aService, podToDelete)) {
				return true;
			}
		}
		return false;
	}
	
	//kubernetes matches service to pod based on a subset. if the pod contains ALL of the service selector values, then it matches. even if the pod has additional labels
	private static boolean validateLabelsSubset(Map<String, String> selectorLabels, Map<String, String> podLabels){
		String mandatoryLabels = "app,version";
		if (selectorLabels == null || podLabels == null){
			return false;
		}
		for(String s: mandatoryLabels.split(",")){
			if (selectorLabels.get(s) == null || podLabels.get(s) == null){
				return false;
			}
		}
		for(String s: selectorLabels.keySet()){
			if (podLabels.get(s) == null || !podLabels.get(s).equalsIgnoreCase(selectorLabels.get(s))){
				return false;
			}
		}
		return true;
	}
	
	private static boolean hasMatchingNamespace(K8Service service, KubePod pod){
		String serviceNs;
		String podNs;
		try{
			serviceNs = service.getMetadata().get("namespace");
		}
		catch(Exception e){
			serviceNs = "default";
		}
		podNs = pod.getNamespace();
		return StringUtils.equals(serviceNs, podNs);
	}
	
	public static boolean hasMatchingTags(K8Service service, KubePod pod){
		if (service.getLabelSelector() == null || pod.getLabels() == null ){
			return false;
		}
		return validateLabelsSubset(service.getLabelSelector(),pod.getLabels());
	}

	//Need to convert the findRrunningServiceEndPointRequest Json to the Object
	public static FindRunningServiceEndPointRequest convertToFindRunningRequest(String response) throws JSONException {
		//CustomXGCalConverter is needed for the automatic conversion
		Gson gsonForFindRunning = new GsonBuilder().registerTypeAdapter(XMLGregorianCalendar.class, new CustomXGCalConverter.Serializer()) 
				.registerTypeAdapter(XMLGregorianCalendar.class, new CustomXGCalConverter.Deserializer()).create(); 
		return gsonForFindRunning.fromJson(response.replace("ServiceEndPointList", "serviceEndPointList"), FindRunningServiceEndPointRequest.class);		
	}

	/**
	 * Helper method to send a generic request to a server. Can handle HTTPS. HTTPS ignores the server certificate.
	 * 
	 * @param urlAndPath I.E. https://localhost:421/testpath
	 * @param inputJSONBody For methods with a payload, this is a string input representing JSON payload
	 * @param method I.E. GET
	 * @param auth Sets the Authorization header. I.E. Basic encryptedtoken.
	 * @return String response
	 */
	public static String sendRequest(String urlAndPath, String inputJSONBody, String method, String auth){
		// Current required to trust the certificates...
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			public void checkClientTrusted(X509Certificate[] certs, String authType) {
			}

			public void checkServerTrusted(X509Certificate[] certs, String authType) {
			}
		} };

		SSLContext sc;
		try {
			sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
		} catch (Exception e){
			logger.error("Error occured when setting up ssl",e);
			return null;
		}

		StringBuilder response = null;
		//we send over https
		HttpsURLConnection connection = null;

		try{
			//Create connection
			URL url = new URL(urlAndPath);


			connection = (HttpsURLConnection)url.openConnection();
			connection.setRequestMethod(method);
			connection.setSSLSocketFactory(sc.getSocketFactory());
			connection.setRequestProperty("Content-Type", 
					"application/json");
			if (auth != null){
				connection.setRequestProperty("Authorization", auth);
			}
			connection.setUseCaches(false);
			connection.setDoOutput(true);

			//no payload, so connect
			if (method.equals("GET")){
				connection.connect();
			}
			else{					
				//Send request
				DataOutputStream wr = new DataOutputStream (
						connection.getOutputStream());
				wr.writeBytes(inputJSONBody);
				wr.close();

			}
			//Get Response
			InputStream is;
			if (connection.getResponseCode() / 100 == 2){
				is = connection.getInputStream();
			}
			else{
				is = connection.getErrorStream();
			}
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			response = new StringBuilder(); // or StringBuffer if not Java 5+ 
			String line;
			while((line = rd.readLine()) != null) {
				response.append(line);
				response.append('\r');
			}
			rd.close();
		}
		catch(Exception e){
			logger.error("Exception occured while sending request. Trying to get errorStream.",e);
			InputStream is = connection.getErrorStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			response = new StringBuilder(); // or StringBuffer if not Java 5+ 
			String line;
			try {
				while((line = rd.readLine()) != null) {
					response.append(line);
					response.append('\r');
				}
			} catch (IOException e1) {
				logger.error("Error IOException while getting the error stream",e1);
			}finally{
				try {
					rd.close();
				} catch (IOException e2) {
					//ignore
				}
				try {
					is.close();
				} catch (IOException e1) {
					//ignore
				}
			}
			return null;
		}
		finally{
			if(connection != null) {
				connection.disconnect(); 
			}
		}
		return response == null ? "" : response.toString();
	}

	/*
	 * This checks for identical ServiceEndPoints.
	 * 
	 * This will be important for update ServiceEndPointChecks. The PKs should match but the other data may not. This method will confirm if they are absolute identical
	 */
	public static boolean checkEqualServiceEndPoints(ServiceEndPoint sep1, ServiceEndPoint sep2){

			if(!StringUtils.equals(sep1.getContainerVersionDefinitionName(), sep2.getContainerVersionDefinitionName())){
				return false;
			}
			if(!StringUtils.equals(sep1.getContextPath(), sep2.getContextPath())){
				return false;
			}
			if(!StringUtils.equals(sep1.getDME2Version(), sep2.getDME2Version())){
				return false;
			}
			if(!StringUtils.equals(sep1.getHostAddress(), sep2.getHostAddress())){
				return false;
			}
			if(!StringUtils.equals(sep1.getLatitude(), sep2.getLatitude())){
				return false;
			}
			if(!StringUtils.equals(sep1.getListenPort(), sep2.getListenPort())){
				return false;
			}
			if(!StringUtils.equals(sep1.getLongitude(), sep2.getLongitude())){
				return false;
			}
			if(!StringUtils.equals(sep1.getName(), sep2.getName())){
				return false;
			}
			if(!StringUtils.equals(sep1.getProtocol(), sep2.getProtocol())){
				return false;
			}
			
			String version1 = String.valueOf(sep1.getVersion().getMajor()) + String.valueOf(sep1.getVersion().getMinor()) + sep1.getVersion().getPatch();
			String version2 = String.valueOf(sep2.getVersion().getMajor()) + String.valueOf(sep2.getVersion().getMinor()) + sep2.getVersion().getPatch();
			if(!StringUtils.equals(version1, version2)){
				return false;
			}
			
			if(!StringUtils.equals(sep1.getOperationalInfo().getCreatedBy(),sep2.getOperationalInfo().getCreatedBy())){
				return false;
			}
			if(!StringUtils.equals(sep1.getOperationalInfo().getUpdatedBy(),sep2.getOperationalInfo().getUpdatedBy())){
				return false;
			}
			if((sep1.getOperationalInfo() == null && sep2.getOperationalInfo() != null) || (sep1.getOperationalInfo() != null && sep2.getOperationalInfo() == null)){
				return false;
			}
			
			if(sep1.getOperationalInfo() != null && sep2.getOperationalInfo() != null){
				if(!compareXMLGregarianCalendarWithNullCheck(sep1.getOperationalInfo().getUpdatedTimestamp(), sep2.getOperationalInfo().getUpdatedTimestamp())){
					return false;
				}
				if(!compareXMLGregarianCalendarWithNullCheck(sep1.getOperationalInfo().getCreatedTimestamp(), sep2.getOperationalInfo().getCreatedTimestamp())){
					return false;
				}
			}
			
		return true;
	}

	/*
	 * This checks for identical ServiceEndPoints Private Keys.
	 * 
	 * The PK is how GRM finds SEPs unique
	 */
	public static boolean checkEqualServiceEndPointPK(ServiceEndPoint sep1, ServiceEndPoint sep2){
		/*
		 * This might have to include route offers 
		 */
		if(!StringUtils.equals(sep1.getHostAddress(), sep2.getHostAddress())){
			return false;
		}
		if(!StringUtils.equals(sep1.getListenPort(), sep2.getListenPort())){
			return false;
		}
		if(!StringUtils.equals(sep1.getName(), sep2.getName())){
			return false;
		}
		String version1 = String.valueOf(sep1.getVersion().getMajor()) + String.valueOf(sep1.getVersion().getMinor()) + sep1.getVersion().getPatch();
		String version2 = String.valueOf(sep2.getVersion().getMajor()) + String.valueOf(sep2.getVersion().getMinor()) + sep2.getVersion().getPatch();
		if(!StringUtils.equals(version1, version2)){
			return false;
		}
		if(!StringUtils.equals(sep1.getRouteOffer(),sep2.getRouteOffer())){
			return false;
		}
		return true;
	}

	private static boolean compareXMLGregarianCalendarWithNullCheck(XMLGregorianCalendar one, XMLGregorianCalendar two){

		if(one == null && two == null){
			return true;
		}
		if (one == null && two != null){
			return false;
		}
		if (one != null && two == null){
			return false;
		}	
		int comparison = one.compare(two);
		logger.trace("Comparing timestamp: {}\nTo Timestamp: {}", one.toString() , two.toString());
		if (comparison == 0){
			return true;
		}
		return false;
		
	}
	
	public static List<String> getProperties(List<NameValuePair> nameValuePairs) {
		if (nameValuePairs == null) {
			nameValuePairs = new ArrayList<>();
			NameValuePair nameValuePair = new NameValuePair();
			nameValuePair.setName("");
			nameValuePair.setValue("");
			nameValuePairs.add(nameValuePair);
		} else if (nameValuePairs.isEmpty()) {
			NameValuePair nameValuePair = new NameValuePair();
			nameValuePair.setName("");
			nameValuePair.setValue("");
			nameValuePairs.add(nameValuePair);
		}
		List<String> properties = new ArrayList<>();

		for (NameValuePair pair : nameValuePairs) {
			properties.add(pair.getName() + NAME_SEP + pair.getValue());
		}
		return properties;
	}

	public static String getSEPId(String sepName, String verStr, String hostName, String port, String env) {
		if (port != null) {
			return sepName + GRMEdgeConstants.NAME_SEP + verStr + GRMEdgeConstants.NAME_SEP + hostName + "|" + port + "-" + env;
		} else {
			return sepName + GRMEdgeConstants.NAME_SEP + verStr + GRMEdgeConstants.NAME_SEP + hostName;
		}
	}

	public static String getSEPName(String sepName, String verStr, String hostName, String port) {
		if (port != null) {
			return sepName + GRMEdgeConstants.NAME_SEP + verStr + GRMEdgeConstants.NAME_SEP + hostName + "|" + port;
		} else {
			return sepName + GRMEdgeConstants.NAME_SEP + verStr + GRMEdgeConstants.NAME_SEP + hostName;
		}
	}

	public static String getVersion(int inMajorVersion, int inMinorVerion, String inPatchVersion) {
		String version = inMajorVersion + VERSION_SEP + inMinorVerion;
		if (!StringUtils.isEmpty(inPatchVersion)) {
			version = version + VERSION_SEP + inPatchVersion;
		}
		return version;
	}

	public static String getVersionString(VersionDefinition versionDef) {
		if (versionDef == null) {
			return null;
		}
		return getVersion(versionDef.getMajor(), versionDef.getMinor(), versionDef.getPatch());
	}

	public static String getDomain(String inName) {
		//inName = com.att.csi.fooService
		// skip periods escaped by \\
		if(StringUtils.isEmpty(inName)){
			return null;
		}
		String[] values = inName.split(GRMEdgeConstants.SERVICE_NAME_REGEX_PATTERN);
		if (values.length > 1) {
			StringBuilder domainName = new StringBuilder();
			for(int i=0;i<values.length-1;i++) {
				domainName.append(values[i]+".");
			}
			if(domainName.length()>0) {
				String domainNameStr = domainName.toString();
				if(domainNameStr.endsWith(".")){
					int stIndex = domainNameStr.lastIndexOf('.');
					return domainNameStr.substring(0,stIndex);
				}
				return domainName.toString();
			}
			else{
				return null;
			}
		} else {
			return null; 	
		}
	}
	
    public static String getId(String name, String environment) {
        if ((name != null) && !name.isEmpty()) {
            return name + "-" + environment;
        } else {
            return "";
        }
    }

    public static String getSVDName(String name) {
        if ((name != null) && !name.isEmpty()) {
            return name;
        } else {
            return "";
        }
    }

	public static String convertSEPNamespaceToDots(String name) {
		int lastDot = StringUtils.lastIndexOf(name, ".");
		String returnName = name;
		if(lastDot != -1)
			returnName = returnName.substring(0,lastDot).replace("-", ".")+returnName.substring(lastDot);
		return returnName;
	}

	public static void replaceCache(List<String> urls) throws Exception {

		List<String> serviceKeysAdded = new ArrayList<>();
		List<String> podKeysAdded = new ArrayList<>();
		
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
								podKeysAdded.add(K8ServiceController.getCacheKeyPod(pod));
								K8ServiceController.forceAddPod(pod);
							}
							catch(Exception e){
								logger.debug("Pod did not have required attributes: " + pods.getJSONObject(i).toString());
							}
						}	
					}
					catch(JSONException e){
						logger.error("No pods found for namespace: {}" ,namespace);
					}
					catch(Exception e){
						logger.error("Received error while trying to load pods from API server.",e);
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
								serviceKeysAdded.add(K8ServiceController.getCacheKeyK8(service));
								K8ServiceController.addService(service);	
							}
							catch(Exception e){
								logger.error("Service did not have required attributes: " + services.getJSONObject(i).toString());
							}
						}	
					}
					catch(JSONException e){
						logger.error("No services found for namespace: {}" , namespace);
					}
					catch(Exception e){
						logger.error("Received error while trying to load services from API server.",e);
					}
				}		
			}
			catch(Exception e){
				logger.error("Error occured while reading Kubernetes API for cache data",e);
				throw e;
			}
		}

		// Remove old entries. We added entries in pod and service cache by replacing, now we look add the entries that's not added and delete them
		// Also, we only added entries but not create the service endpoints, we'll create them now
		try {
			Iterator<Object> podCacheKeySetAfterReload = ServiceRegistry.getInstance().getCache(GRMEdgeConstants.POD_CACHE).getKeySet().iterator();
			Iterator<Object> serviceCacheKeySetAfterReload = ServiceRegistry.getInstance().getCache(GRMEdgeConstants.SERVICE_CACHE).getKeySet().iterator();

			while (serviceCacheKeySetAfterReload.hasNext()) {
				Object serviceKey = serviceCacheKeySetAfterReload.next();
				if (!serviceKeysAdded.contains(serviceKey)) {
					ServiceRegistry.getInstance().getCache(GRMEdgeConstants.SERVICE_CACHE).remove(serviceKey);
				}
			}

			while (podCacheKeySetAfterReload.hasNext()) {
				Object podKey = podCacheKeySetAfterReload.next();
				if (!podKeysAdded.contains(podKey)) {
					ServiceRegistry.getInstance().getCache(GRMEdgeConstants.POD_CACHE).remove(podKey);
				}
			}

			// Add Service EndPoints.
			List<String> serviceEndPointKeysAdded = new ArrayList<>();
			Iterator<Object> currentServiceCacheKeys = ServiceRegistry.getInstance().getCache(GRMEdgeConstants.SERVICE_CACHE).getKeySet().iterator();
			while (currentServiceCacheKeys.hasNext()) {
				List<ServiceEndPoint> tempSepListForThisService = new ArrayList<>();
				Object aKey = currentServiceCacheKeys.next();
				Object aServiceInCache = ServiceRegistry.getInstance().getCache(GRMEdgeConstants.SERVICE_CACHE).get(aKey);
				if (aServiceInCache != null) {
					K8Service aService = (K8Service)aServiceInCache;
					List<KubePod> matchingPods = GRMEdgeUtil.getMatchingPodTags(aService);
					if(matchingPods == null || matchingPods.isEmpty()){
						logger.debug("No Matching Pods were found");
					} else{
						logger.debug("Matching pods were found. Creating a SEP for each matching pod.");
						for(KubePod pod: matchingPods){
							ServiceEndPoint aSEP = GRMEdgeUtil.createSEP(pod, aService);
							if(aSEP != null){
								if (tempSepListForThisService.size() > 1) {
									Boolean matchedEndpoint = false;
									for (ServiceEndPoint curSep : tempSepListForThisService) {
										matchedEndpoint = GRMEdgeUtil.checkEqualServiceEndPointPK(aSEP, curSep);
										if (matchedEndpoint) {
											break;
										}
									}
									if (!matchedEndpoint) {
										tempSepListForThisService.add(aSEP);
									}
								} else {
									tempSepListForThisService.add(aSEP);
								}
							}
						}
						if(tempSepListForThisService.size() > 0 ){
							serviceEndPointKeysAdded.add(K8ServiceController.getCacheKeySEP(tempSepListForThisService.get(0)));
							for(ServiceEndPoint sep: tempSepListForThisService){
								K8ServiceController.addEndPoint(sep);
							}
						}
					}
				}
			}

			// Now remove the entries in ServiceEndPoint cache that did not get refreshed
			Iterator<Object> serviceEndpointCacheKeySetAfterReload = ServiceRegistry.getInstance().getCache(GRMEdgeConstants.ENDPOINT_CACHE).getKeySet().iterator();
			while (serviceEndpointCacheKeySetAfterReload.hasNext()) {
				Object serviceEndPointKey = serviceEndpointCacheKeySetAfterReload.next();
				if (!serviceEndPointKeysAdded.contains(serviceEndPointKey)) {
					ServiceRegistry.getInstance().getCache(GRMEdgeConstants.ENDPOINT_CACHE).remove(serviceEndPointKey);
				}
			}

		} catch(Exception e){
			logger.error("Error in creating service endpoint cache", e);
			throw e;
		}

	}

	private static String ipToString(byte[] ip) {
		StringBuilder builder = new StringBuilder(23); // IP6 size
		for (int part = 0; part < ip.length; part++) {
			if (builder.length() > 0) {
				builder.append('.');
			}
			int code = ip[part];
			if (code < 0) {
				code += 256; // numbers above 127 is interpreted as negative as byte is signed in java
			}
			builder.append(code);
		}
		return builder.toString();
	}

	
	public static List<String> getAPIServerUrls() throws Exception {
		logger.trace("Resolving API Server Location");
		InetAddress[] addresses;
		int count = 0;
		boolean error = false;
		List<String> urls = new ArrayList<>();
		do{
			try{
				try{
					addresses = InetAddress.getAllByName("kubernetes.default");	
				}
				catch(Exception e){
					addresses = null;
				}
				if(addresses == null){
					addresses = InetAddress.getAllByName("kubernetes.default.svc");
				}
				for (InetAddress address : addresses) {
					urls.add("https://" + ipToString(address.getAddress()) + ":443/");
				}
				Iterator<String> iter = urls.iterator();
				while (iter.hasNext()) {
					String address = iter.next();
					logger.trace("API Server Address: {}" , address);
				}
			}	
			catch(Exception e){
				logger.warn("Failed to lookup IP addresses",e);
				error = true;
				if (count == 2){
					throw e;
				}
			}
			count++;
		}while(error && count < 3);
		
		return urls;
	}
	
    /**
     * Returns a list of NameValuePair objects from parsing Kubernetes
     * annotations.
     * 
     * @param annotations String representing kubernetes annotations.
     * Annotations in Kubernetes have the following format:
     * 
     * e.g. {"key1":"value1","key2":"value2"}
     * 
     * @return list of NameValuePair objects from parsing Kubernetes
     * annotations
     */
    private static List<NameValuePair> getPropertiesFromAnnotations(String annotations)
    {
        List<NameValuePair> returnValue = new ArrayList<NameValuePair>();
        
        //get rid of curly brackets and double quotes
        annotations = annotations.replace("{", "");
        annotations = annotations.replace("}", "");
        annotations = annotations.replace("\"", "");
        
        //get annotation tokens
        String[] annotationTokens = annotations.split(",");
        
        //for each annotation token get each key and value
        for (int i = 0; i < annotationTokens.length; i++)
        {
            String[] keyValueTokens = annotationTokens[i].split(":");
            
            if (keyValueTokens.length != 2)
            {
                logger.trace("Invalid key-value pair found");
                continue;
            }
            else
            {
                String key = keyValueTokens[0];
                String value = keyValueTokens[1];
                NameValuePair nameValuePair = new NameValuePair();
                nameValuePair.setName(key);
                nameValuePair.setValue(value);
                returnValue.add(nameValuePair);
                
                logger.trace("Added Name=" + key + " Value=" + value + " to property list");
            }
        }
        
        return returnValue;
    }
}
