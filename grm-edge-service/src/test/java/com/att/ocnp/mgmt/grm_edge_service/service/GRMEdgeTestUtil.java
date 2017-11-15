/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.ocnp.mgmt.grm_edge_service.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.att.ocnp.mgmt.grm_edge_service.util.CustomXGCalConverter;
import com.att.scld.grm.types.v1.FindLevel;
import com.att.scld.grm.types.v1.NameValuePair;
import com.att.scld.grm.types.v1.ServiceEndPoint;
import com.att.scld.grm.types.v1.VersionDefinition;
import com.att.scld.grm.v1.AddServiceEndPointRequest;
import com.att.scld.grm.v1.DeleteServiceEndPointRequest;
import com.att.scld.grm.v1.FindRunningServiceEndPointRequest;
import com.att.scld.grm.v1.FindServiceEndPointRequest;
import com.att.scld.grm.v1.FindServiceEndPointResponse;
import com.att.scld.grm.v1.GetRouteInfoResponse;
import com.att.scld.grm.v1.UpdateServiceEndPointRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GRMEdgeTestUtil {

	public static final String HTTP = "http";
	public static final String HTTPS="https";

	private static String getAuthString(){
		String name = System.getProperty("KUBE_API_USER","root");
		String pass = System.getProperty("KUBE_API_PWD","root");

		String authString = name + ":" + pass;
		byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
		String authStringEnc = new String(authEncBytes);
		return authStringEnc;
	}

	public static FindServiceEndPointResponse getEndPointsFromGRM(String name, String env) throws InterruptedException{
		//sleep for write behind
		Thread.sleep(10000);

		String urlAndPath = GRMEdgeTestConstants.GRM_REST_PATH_PORT_CONTEXT;
		String inputJSONBody = getFindEndPointJSON(name, env);
		return convertToFindRunningResponse(sendRestRequest(urlAndPath,inputJSONBody,"POST","Basic bTcxMzY0Om03MTM2NA=="));
	}

	public static void getEndPointsfromGRMWithAutomatedSleep(String name, String env, long maxSleep, int sizeExpected) throws InterruptedException{

		String urlAndPath = GRMEdgeTestConstants.GRM_REST_PATH_PORT_CONTEXT;
		String inputJSONBody = getFindEndPointJSON(name, env);
		long curTime = 0;
		boolean passed = false;
		int count = 0;
		while(curTime < maxSleep){
			FindServiceEndPointResponse eps = convertToFindRunningResponse(sendRestRequest(urlAndPath, inputJSONBody,"POST","Basic bTcxMzY0Om03MTM2NA=="));
			count = eps.getServiceEndPointList().size();
			if(count == sizeExpected){
				passed = true;
				break;
			}
			System.out.println("Waiting for GRM to reflect changes. Sleeping for 5 seconds. Total Sleep Time: " + curTime +". Max Sleep Time: " + maxSleep);
			Thread.sleep(5000);
			curTime += 5000;
		}
		assertTrue("Did not find the correct number of endpoints in GRM. Expected to find " + sizeExpected+". But Found: " + count, passed);
	}

	public static String getGRMEdgeHostIP(){
		String podName = sendRestRequest(GRMEdgeTestConstants.kubernetesBaseURL+GRMEdgeTestConstants.namespace+"/pods","","GET","Basic " + getAuthString());
		int grmedge = podName.indexOf("grm-edge-");
		podName = podName.substring(grmedge);
		podName = podName.substring(0,podName.indexOf("\""));
		//now describe pod so that we can get url
		String urlInfo = sendRestRequest(GRMEdgeTestConstants.kubernetesBaseURL+GRMEdgeTestConstants.namespace+"/pods/"+podName,"","GET","Basic " + getAuthString());
		String hostipstring = "hostIP\":\"";
		int hostip = urlInfo.indexOf(hostipstring);
		urlInfo = urlInfo.substring(hostip);
		urlInfo = urlInfo.substring(hostipstring.length());
		urlInfo = urlInfo.substring(0,urlInfo.indexOf("\""));
		return urlInfo;
	}

	public static FindServiceEndPointResponse getEndPointsFromKubernetes(String name, String env) throws InterruptedException{

		Thread.sleep(10000); //sleep for writebehind


		//find url from kubernetes api
		//first get pod name
		String podName = sendRestRequest(GRMEdgeTestConstants.kubernetesBaseURL+GRMEdgeTestConstants.namespace+"/pods","","GET","Basic " + getAuthString());
		int grmedge = podName.indexOf("grm-edge-");
		podName = podName.substring(grmedge);
		podName = podName.substring(0,podName.indexOf("\""));
		//now describe pod so that we can get url
		String urlInfo = sendRestRequest(GRMEdgeTestConstants.kubernetesBaseURL+GRMEdgeTestConstants.namespace+"/pods/"+podName,"","GET","Basic " + getAuthString());
		String hostipstring = "hostIP\":\"";
		int hostip = urlInfo.indexOf(hostipstring);
		urlInfo = urlInfo.substring(hostip);
		urlInfo = urlInfo.substring(hostipstring.length());
		urlInfo = urlInfo.substring(0,urlInfo.indexOf("\""));
		String inputJSONBody = getEndPointJSON(name, env);

		String urlAndPath = "https://"+urlInfo+":"+System.getProperty("GRM_EDGE_NODEPORT","31998")+"/GRMLWPService/v1/serviceEndPoint/findAllEps";
		return convertToFindRunningResponse(sendRestRequest(urlAndPath, inputJSONBody,"POST","Basic " + getAuthString()));
	}

	public static String deleteService(String name, String namespace) throws InterruptedException{
		String urlAndPath = GRMEdgeTestConstants.kubernetesBaseURL+namespace+"/services/"+name;
		return sendRestRequest(urlAndPath, "","DELETE","Basic " + getAuthString());
	}

	public static String deleteRc(String name, String namespace) throws InterruptedException{
		String urlAndPath = GRMEdgeTestConstants.kubernetesBaseURL+namespace+"/replicationcontrollers/"+name;
		//scale down rc
		scaleRc(name, "0",namespace);
		//delete rc
		return sendRestRequest(urlAndPath, "","DELETE","Basic " + getAuthString());
	}
	
	public static String scaleDeployment(String name, String replicas, String namespace) throws InterruptedException{
		namespace=namespace.replace(".", "-");
		String urlAndPath = GRMEdgeTestConstants.kubernetesBetaAPIURL+"namespaces/"+namespace+"/deployments/"+name;
		System.out.println("Scaling Deployment: " +name + " with replicas="+replicas);
		//scale rc
		//get current rc json
		String s = sendRestRequest(urlAndPath,"","GET","Basic " + getAuthString());
		String str = "spec\":{\"replicas\":";
		int ind = s.indexOf(str);
		String output = s.substring(0,ind+str.length()) + replicas + s.substring(ind+str.length()+1);
		//send in scale request
		String resp = sendRestRequest(urlAndPath,output,"PUT","Basic " + getAuthString());
		System.out.println("Scaling complete with replicas=" + replicas);
		if(name.contains("grm-edge")){
			if(replicas.equalsIgnoreCase("0"))
				Thread.sleep(5000);
			else
				Thread.sleep(GRMEdgeTestConstants.SLEEP*4);
		}
		
		return resp;
	}


	public static String updateService(String serviceName, String appName, String namespace){
		String urlAndPath = GRMEdgeTestConstants.kubernetesBaseURL+namespace+"/services/"+serviceName;
		String inputJSON = "{\"spec\":{\"ports\":[{\"name\":\"https\",\"port\":80}]}}";
		//		String inputJSON = loadFile("src/test/resources/kubeService");
		//		inputJSON = inputJSON.replace("<APP_NAME>",appName);
		//		inputJSON = inputJSON.replace("<SERVICE_NAME>", serviceName);
		//		inputJSON = inputJSON.replace("<NAMESPACE>",namespace);
		//		inputJSON = inputJSON.replace("<PORT_NAME>", "https");		
		return sendRestRequest(urlAndPath,inputJSON, "PATCH", "Basic " + getAuthString());
	}

	public static String scaleRc(String name, String replicas, String namespace) throws InterruptedException{
		String urlAndPath = GRMEdgeTestConstants.kubernetesBaseURL+namespace+"/replicationcontrollers/"+name;
		System.out.println("Scaling RC: " +name + " with replicas="+replicas);
		//scale rc
		//get current rc json
		String s = sendRestRequest(urlAndPath,"","GET","Basic " + getAuthString());
		String str = "spec\":{\"replicas\":";
		int ind = s.indexOf(str);
		String output = s.substring(0,ind+str.length()) + replicas + s.substring(ind+str.length()+1);
		//send in scale request
		String resp = sendRestRequest(urlAndPath,output,"PUT","Basic " + getAuthString());
		System.out.println("Scaling complete with replicas=" + replicas);
		if(name.contains("grm-edge")){
			Thread.sleep(GRMEdgeTestConstants.SLEEP*2);
		}
		return resp;
	}

	public static String addRC(String name, String appName, String replicaCount, String namespace){
		String urlAndPath = GRMEdgeTestConstants.kubernetesBaseURL+namespace+"/replicationcontrollers";
		String inputJSON = loadFile("src/test/resources/kubeTestRc");
		inputJSON = inputJSON.replace("<APP_NAME>",appName);
		inputJSON = inputJSON.replace("<RC_NAME>", name);
		inputJSON = inputJSON.replace("<REPLICA_COUNT>", replicaCount);
		inputJSON = inputJSON.replace("<IMAGE_NAME>", GRMEdgeTestConstants.IMAGE_NAME);
		inputJSON = inputJSON.replace("<NAMESPACE>",namespace);
		inputJSON = inputJSON.replace("<ROUTE_OFFER>", "TEST");
		return sendRestRequest(urlAndPath,inputJSON,"POST","Basic " + getAuthString());
	}

	public static String addService(String name, String appName, String namespace){
		String urlAndPath = GRMEdgeTestConstants.kubernetesBaseURL+namespace+"/services";
		String inputJSON = loadFile("src/test/resources/kubeService");
		inputJSON = inputJSON.replace("<APP_NAME>",appName);
		inputJSON = inputJSON.replace("<SERVICE_NAME>", name);
		inputJSON = inputJSON.replace("<NAMESPACE>",namespace);
		inputJSON = inputJSON.replace("<PORT_NAME>", "http");
		inputJSON = inputJSON.replace("<ROUTE_OFFER>", "TEST");
		return sendRestRequest(urlAndPath,inputJSON,"POST","Basic " + getAuthString());
	}
	
    /**
     * Adds a service with annotations. The annotations are specified in
     * kubeServiceWithAnnotations resource.
     * 
     * @param name Service name
     * @param appName Service application name
     * @param namespace Service namespace
     * 
     * @return Output of rest request to add the service
     */
    public static String addServiceWithAnnotations(String name, String appName, String namespace)
    {
        String urlAndPath = GRMEdgeTestConstants.kubernetesBaseURL + namespace + "/services";
        String inputJSON = loadFile("src/test/resources/kubeServiceWithAnnotations");
        inputJSON = inputJSON.replace("<APP_NAME>", appName);
        inputJSON = inputJSON.replace("<SERVICE_NAME>", name);
        inputJSON = inputJSON.replace("<NAMESPACE>", namespace);
        inputJSON = inputJSON.replace("<PORT_NAME>", "http");
        inputJSON = inputJSON.replace("<ROUTE_OFFER>", "TEST");
        return sendRestRequest(urlAndPath, inputJSON, "POST", "Basic " + getAuthString());
    }
    
	public static String addRCLabel(String name, String appName, String replicaCount,
			String namespace, String label) {
		String urlAndPath = GRMEdgeTestConstants.kubernetesBaseURL+namespace+"/replicationcontrollers";
		String inputJSON = loadFile("src/test/resources/kubeTestRcLabel");
		inputJSON = inputJSON.replace("<APP_NAME>",appName);
		inputJSON = inputJSON.replace("<RC_NAME>", name);
		inputJSON = inputJSON.replace("<REPLICA_COUNT>", replicaCount);
		inputJSON = inputJSON.replace("<IMAGE_NAME>", GRMEdgeTestConstants.IMAGE_NAME);
		inputJSON = inputJSON.replace("<NAMESPACE>",namespace);
		inputJSON = inputJSON.replace("<ROUTE_OFFER>", "TEST");
		inputJSON = inputJSON.replace("<LABEL>", label);
		return sendRestRequest(urlAndPath,inputJSON,"POST","Basic " + getAuthString());
	}

	public static String addServiceLabel(String name, String appName, String namespace, String label){
			String urlAndPath = GRMEdgeTestConstants.kubernetesBaseURL+namespace+"/services";
		String inputJSON = loadFile("src/test/resources/kubeServiceLabel");
		inputJSON = inputJSON.replace("<APP_NAME>",appName);
		inputJSON = inputJSON.replace("<SERVICE_NAME>", name);
		inputJSON = inputJSON.replace("<NAMESPACE>",namespace);
		inputJSON = inputJSON.replace("<PORT_NAME>", "http");
		inputJSON = inputJSON.replace("<ROUTE_OFFER>", "TEST");
		inputJSON = inputJSON.replace("<LABEL>", label);
		return sendRestRequest(urlAndPath,inputJSON,"POST","Basic " + getAuthString());
	}

	private static FindServiceEndPointResponse convertToFindRunningResponse(String response) {
//		System.out.println("Pre GSON: " + response);
		Gson gsonForFindRunning = new GsonBuilder().registerTypeAdapter(XMLGregorianCalendar.class, new CustomXGCalConverter.Serializer()) 
				.registerTypeAdapter(XMLGregorianCalendar.class, new CustomXGCalConverter.Deserializer()).create(); 
//		System.out.println("Post GSON: " + gsonForFindRunning.fromJson(response.toString().replace("ServiceEndPointList", "serviceEndPointList"), FindServiceEndPointResponse.class));
		return gsonForFindRunning.fromJson(response.toString().replace("ServiceEndPointList", "serviceEndPointList"), FindServiceEndPointResponse.class);		
	}
	
	private static GetRouteInfoResponse convertToGetRouteInfoResponse(String response) {
		Gson gsonForGetRouteInfo = new GsonBuilder().registerTypeAdapter(XMLGregorianCalendar.class, new CustomXGCalConverter.Serializer()) 
				.registerTypeAdapter(XMLGregorianCalendar.class, new CustomXGCalConverter.Deserializer()).create(); 
		return gsonForGetRouteInfo.fromJson(response.toString().replace("ServiceEndPointList", "serviceEndPointList"), GetRouteInfoResponse.class);		
	}

	private static String sendRestRequest(String urlAndPath, String inputJSONBody, String method, String auth){
		System.out.println("\nurlAndPath: " + urlAndPath);
		System.out.println("method: " + method);
		System.out.println("Input json: " + inputJSONBody);
		StringBuilder response = null;
		HttpsURLConnection connection = null;

		try{
			//		    //Create connection
			//			HttpsURLConnection.setDefaultHostnameVerifier(
			//				    new javax.net.ssl.HostnameVerifier(){
			//
			//				            @Override
			//				        public boolean verify(String hostname,
			//				                javax.net.ssl.SSLSession sslSession) {
			//				            	System.out.println(" ************  Host name verified ************ ");
			//				            	return true;
			//				        }
			//				    });
			URL url = new URL(urlAndPath);
			connection = (HttpsURLConnection)url.openConnection();
			connection.setConnectTimeout(10000);
			connection.setReadTimeout(20000);
			connection.setRequestMethod(method);
			if(method.equalsIgnoreCase("PATCH")){  	
				connection.setRequestProperty("Content-Type", "application/strategic-merge-patch+json");
			}
			else{
				connection.setRequestProperty("Content-Type", 
						"application/json");	
			}
			if (auth != null){
				connection.setRequestProperty("Authorization", auth);
			}
			connection.setUseCaches(false);
			connection.setDoOutput(true);

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
			System.out.println("Response code: " +connection.getResponseCode());
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
			connection.connect();
		}
		catch(Exception e){
			e.printStackTrace();
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
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return null;
		}
		finally{
			if(connection != null) {
				connection.disconnect(); 
			}
		}
		System.out.println("Response: " +response+"\n");
		return response == null ? "" : response.toString();
	}

	public static String generateValidPodJSON(String name){
		String s = loadFile("src/test/resources/podJson");
		s = s.replace("<REPLACE_NAME>", name);
		s = s.replace("<REPLACE_ENV>", GRMEdgeTestConstants.ENV);
		s = s.replace("<NAMESPACE>",GRMEdgeTestConstants.namespace);
		return s;
	}

	public static String generateValidServiceJSON(String name){
		String s = loadFile("src/test/resources/serviceJson");
		s = s.replace("<REPLACE_NAME>", name);
		s = s.replace("<REPLACE_ENV>", GRMEdgeTestConstants.ENV);
		s = s.replace("<NAMESPACE>",GRMEdgeTestConstants.namespace);
		s = s.replace("<PORT_NAME>", HTTP);
		return s;
	}

	public static String generateValidHttpsModifyServiceJSON(String name){
		String s = loadFile("src/test/resources/serviceJson");
		s = s.replace("<REPLACE_NAME>", name);
		s = s.replace("<REPLACE_ENV>", GRMEdgeTestConstants.ENV);
		s = s.replace("<NAMESPACE>",GRMEdgeTestConstants.namespace);
		s = s.replace("<PORT_NAME>", HTTPS);
		return s;
	}

	public static String generateValidPodJSON1(String name){
		String s = loadFile("src/test/resources/podJson1");
		s = s.replace("<REPLACE_NAME>", name);
		s = s.replace("<REPLACE_ENV>", GRMEdgeTestConstants.ENV);
		s = s.replace("<NAMESPACE>",GRMEdgeTestConstants.namespace);
		System.out.println(s);
		return s;
	}

	public static String generateValidPodJSON2(String name){
		String s = loadFile("src/test/resources/podJson2");
		s = s.replace("<REPLACE_NAME>", name);
		s = s.replace("<REPLACE_ENV>", GRMEdgeTestConstants.ENV);
		s = s.replace("<NAMESPACE>",GRMEdgeTestConstants.namespace);
		System.out.println(s);
		return s;
	}

	public static String generateValidPodJSON3(String name){
		String s = loadFile("src/test/resources/podJson3");
		s = s.replace("<REPLACE_NAME>", name);
		s = s.replace("<REPLACE_ENV>", GRMEdgeTestConstants.ENV);
		s = s.replace("<NAMESPACE>",GRMEdgeTestConstants.namespace);
		System.out.println(s);
		return s;
	}

	public static String getEndPointJSON(String serviceName){
		return "{\"service\":\""+serviceName+"\",\"env\":\"DEV\"}";
	}

	public static String getFindRunningJSON(String serviceName){
		return "{\"serviceEndPoint\":{\"name\":\""+serviceName+"\",\"version\":{\"major\":1,\"minor\":0,\"patch\":\"0\"}},\"env\":\"DEV\"}";
	}

	public static String getFindRunningJSONDemo(){
		return "{\"serviceEndPoint\":{\"name\":\"com.att.testAPI.SomeApp.App123.ajscservice-1-svc\",\"version\":{\"major\":1,\"minor\":0,\"patch\":\"0\"}},\"env\":\"LAB\"}";
	}

	public static String getEndPointJSON(String serviceName, String env){
		return "{\"service\":\""+serviceName+"\",\"env\":\""+env+"\"}";
	}

	public static String getFindRunningJSON(String serviceName, String env, String version){
		return "{\"serviceEndPoint\":{\"name\":\""+serviceName+"\","+version+"},\"env\":\""+env+"\"}";
	}
	
	public static String getRouteInfoJSON(String serviceName, String majorVersion, String minorVersion, String patchVersion, String env){
		return "{\"serviceVersionDefinition\": {\"name\": \""+serviceName+"\",\"version\": {\"major\": " + majorVersion + ",\"minor\": " + minorVersion + ",\"patch\": \"" + patchVersion + "\"}},\"env\": \"" + env + "\"}";
	}

	public static String getFindEndPointJSON(String name, String env){

		FindServiceEndPointRequest req = new FindServiceEndPointRequest();
		VersionDefinition vd = new VersionDefinition();
		vd.setMajor(1);
		vd.setMinor(0);
		vd.setPatch("0");
		req.setName(name);
		req.setVersion(vd);
		req.setFindLevel(FindLevel.ALL);
		req.setEnv(env);

		Gson gson = new Gson();
		String json = gson.toJson(req);
		return json;
	}
	
	public static String getFindEndPointJSON(String name, String env, int major, int minor, String patch){

		FindServiceEndPointRequest req = new FindServiceEndPointRequest();
		VersionDefinition vd = new VersionDefinition();
		vd.setMajor(major);
		vd.setMinor(minor);
		vd.setPatch(patch);
		req.setName(name);
		req.setVersion(vd);
		req.setFindLevel(FindLevel.ALL);
		req.setEnv(env);

		Gson gson = new Gson();
		String json = gson.toJson(req);
		return json;
	}

	public static String getFindRunningEndPointJSON(String name, String env){

		FindRunningServiceEndPointRequest req = new FindRunningServiceEndPointRequest();
		ServiceEndPoint sep = new ServiceEndPoint();
		sep.setName(name);
		VersionDefinition vd = new VersionDefinition();
		vd.setMajor(1);
		vd.setMinor(0);
		vd.setPatch("0");
		sep.setVersion(vd);
		req.setServiceEndPoint(sep);
		req.setFindLevel(FindLevel.ALL);
		req.setEnv(env);

		Gson gson = new Gson();
		String json = gson.toJson(req);
		return json;
	}

	public static String generateInvalidPodJSON(String name){
		return "{";
	}

	public static String generateInvalidServiceJSON(String name){
		return "{";
	}	

	private static String loadFile(String name){
		String content = "";
		try {
			content = new Scanner(new File(name)).useDelimiter("\\Z").next();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return content;
	}

	static class SSLCertificateValidation {

		public static void disable() {
			try {
				SSLContext sslc = SSLContext.getInstance("TLS");
				TrustManager[] trustManagerArray = { new NullX509TrustManager() };
				sslc.init(null, trustManagerArray, null);
				HttpsURLConnection.setDefaultSSLSocketFactory(sslc.getSocketFactory());
				HttpsURLConnection.setDefaultHostnameVerifier(new NullHostnameVerifier());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		private static class NullX509TrustManager implements X509TrustManager {
			@Override
			public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				System.out.println();
			}

			@Override
			public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				System.out.println();
			}

			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return new X509Certificate[0];
			}
		}

		private static class NullHostnameVerifier implements HostnameVerifier {
			@Override
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		}

	}

	public static String addInvalidService(String name, String appName, String namespace){
		String urlAndPath = GRMEdgeTestConstants.kubernetesBaseURL+namespace+"/services";
		String inputJSON = loadFile("src/test/resources/kubeServiceNoVersion");
		inputJSON = inputJSON.replace("<APP_NAME>",appName);
		inputJSON = inputJSON.replace("<SERVICE_NAME>", name);
		inputJSON = inputJSON.replace("<NAMESPACE>",namespace);
		return sendRestRequest(urlAndPath,inputJSON,"POST","Basic " + getAuthString());
	}

	public static String addInvalidRC(String name, String appName, String replicaCount, String namespace){
		String urlAndPath = GRMEdgeTestConstants.kubernetesBaseURL+namespace+"/replicationcontrollers";
		String inputJSON = loadFile("src/test/resources/kubeTestRcNoVersion");
		inputJSON = inputJSON.replace("<APP_NAME>",appName);
		inputJSON = inputJSON.replace("<RC_NAME>", name);
		inputJSON = inputJSON.replace("<REPLICA_COUNT>", replicaCount);
		inputJSON = inputJSON.replace("<IMAGE_NAME>", GRMEdgeTestConstants.IMAGE_NAME);
		inputJSON = inputJSON.replace("<NAMESPACE>",namespace);
		return sendRestRequest(urlAndPath,inputJSON,"POST","Basic " + getAuthString());
	}

	public static String createNamespace(String testnamespace) {
		// TODO Auto-generated method stub
		String urlAndPath = GRMEdgeTestConstants.kubernetesBaseURL;
		String inputJSON = loadFile("src/test/resources/namespaceCreate");
		inputJSON = inputJSON.replace("<NAMESPACE>",testnamespace);
		return sendRestRequest(urlAndPath, inputJSON, "POST", "Basic " + getAuthString());
	}

	public static String deleteNamespace(String testnamespace) {
		// TODO Auto-generated method stub
		String urlAndPath = GRMEdgeTestConstants.kubernetesBaseURL+ testnamespace;
		String inputJSON = "";
		return sendRestRequest(urlAndPath, inputJSON, "DELETE", "Basic " + getAuthString());
	}

	public static FindServiceEndPointResponse getEndPointsfromKubernetesFindRunning(String name, String namespace, String env, String version) throws InterruptedException{

		Thread.sleep(1000); // blanket sleep for 1k to allow write behind to happen
		
		//find url from kubernetes api
		//first get pod name
		String podName = sendRestRequest(GRMEdgeTestConstants.kubernetesBaseURL+namespace+"/pods","","GET","Basic " + getAuthString());
		int grmedge = podName.indexOf("grm-edge-");
		podName = podName.substring(grmedge);
		podName = podName.substring(0,podName.indexOf("\""));
		//now describe pod so that we can get url
		String urlInfo = sendRestRequest(GRMEdgeTestConstants.kubernetesBaseURL+namespace+"/pods/"+podName,"","GET","Basic " + getAuthString());
		String hostipstring = "hostIP\":\"";
		int hostip = urlInfo.indexOf(hostipstring);
		urlInfo = urlInfo.substring(hostip);
		urlInfo = urlInfo.substring(hostipstring.length());
		urlInfo = urlInfo.substring(0,urlInfo.indexOf("\""));
		String inputJSONBody = getFindRunningJSON(name, env,version);

		String urlAndPath = "https://"+urlInfo+":"+System.getProperty("GRM_EDGE_NODEPORT","31998")+"/GRMLWPService/v1/serviceEndPoint/findRunning";
		return convertToFindRunningResponse(sendRestRequest(urlAndPath, inputJSONBody,"POST","Basic " + getAuthString()));
	}
	
	// Find RouteInfo XML from GRM-Edge
	public static GetRouteInfoResponse getRouteInfofromKubernetes(String name, String namespace, String majorVersion, String minorVersion, String patchVersion, String env) throws InterruptedException{

		//find url from kubernetes api
		//first get pod name
		String podName = sendRestRequest(GRMEdgeTestConstants.kubernetesBaseURL+namespace+"/pods","","GET","Basic " + getAuthString());
		int grmedge = podName.indexOf("grm-edge-");
		podName = podName.substring(grmedge);
		podName = podName.substring(0,podName.indexOf("\""));
		//now describe pod so that we can get url
		String urlInfo = sendRestRequest(GRMEdgeTestConstants.kubernetesBaseURL+namespace+"/pods/"+podName,"","GET","Basic " + getAuthString());
		String hostipstring = "hostIP\":\"";
		int hostip = urlInfo.indexOf(hostipstring);
		urlInfo = urlInfo.substring(hostip);
		urlInfo = urlInfo.substring(hostipstring.length());
		urlInfo = urlInfo.substring(0,urlInfo.indexOf("\""));
		String inputJSONBody = getRouteInfoJSON(name, majorVersion, minorVersion, patchVersion, env);

		String urlAndPath = "https://"+urlInfo+":"+System.getProperty("GRM_EDGE_NODEPORT","31998")+"/GRMLWPService/v1/routeInfo/get";
		return convertToGetRouteInfoResponse(sendRestRequest(urlAndPath, inputJSONBody,"POST","Basic " + getAuthString()));
	}

	/*
	 * Just going to add asserts here 
	 */
	public static void getEndPointsfromKubernetesWithAutomatedSleep(String name, String env, long maxSleep, int sizeExpected) throws InterruptedException{

		//find url from kubernetes api
		//first get pod name
		String podName = sendRestRequest(GRMEdgeTestConstants.kubernetesBaseURL+GRMEdgeTestConstants.namespace+"/pods","","GET","Basic " + getAuthString());
		int grmedge = podName.indexOf("grm-edge-");
		podName = podName.substring(grmedge);
		podName = podName.substring(0,podName.indexOf("\""));
		//now describe pod so that we can get url
		String urlInfo = sendRestRequest(GRMEdgeTestConstants.kubernetesBaseURL+GRMEdgeTestConstants.namespace+"/pods/"+podName,"","GET","Basic " + getAuthString());
		String hostipstring = "hostIP\":\"";
		int hostip = urlInfo.indexOf(hostipstring);
		urlInfo = urlInfo.substring(hostip);
		urlInfo = urlInfo.substring(hostipstring.length());
		urlInfo = urlInfo.substring(0,urlInfo.indexOf("\""));
		String inputJSONBody = getEndPointJSON(name, env);

		String urlAndPath = "https://"+urlInfo+":"+System.getProperty("GRM_EDGE_NODEPORT","31998")+"/GRMLWPService/v1/serviceEndPoint/findAllEps";
		long curTime = 0;
		boolean passed = false;
		int count = 0;
		while(curTime < maxSleep){
			FindServiceEndPointResponse eps = convertToFindRunningResponse(sendRestRequest(urlAndPath, inputJSONBody,"POST","Basic " + getAuthString()));
			count = eps.getServiceEndPointList().size();
			if(count == sizeExpected){
				passed = true;
				break;
			}
			System.out.println("Waiting for kubernetes to reflect changes. Sleeping for 5 seconds. Total Sleep Time: " + curTime +". Max Sleep Time: " + maxSleep);
			Thread.sleep(5000);
			curTime += 5000;
		}
		assertTrue("Did not find the correct number of endpoints in kubernetes. Expected to find " + sizeExpected+". But Found: " + count, passed);
	}

	public static void allowPatch() {
		try {
			Field methodsField = HttpURLConnection.class.getDeclaredField("methods");
			methodsField.setAccessible(true);
			// get the methods field modifiers
			Field modifiersField = Field.class.getDeclaredField("modifiers");
			// bypass the "private" modifier 
			modifiersField.setAccessible(true);

			// remove the "final" modifier
			modifiersField.setInt(methodsField, methodsField.getModifiers() & ~Modifier.FINAL);

			/* valid HTTP methods */
			String[] methods = {
					"GET", "POST", "HEAD", "OPTIONS", "PUT", "DELETE", "TRACE", "PATCH"
			};
			// set the new methods - including patch
			methodsField.set(null, methods);

		} catch (SecurityException | IllegalArgumentException | IllegalAccessException | NoSuchFieldException e) {
			e.printStackTrace();
		}

	}

	public static void getEndPointsfromKubernetesFindRunningWithAutomatedSleepVersion(String name, String env, long maxSleep, int sizeExpected, int major, int minor, String patch) throws InterruptedException{

		long curTime = 0;
		boolean passed = false;
		int count = 0;
		String version = "";
		if (minor == -1){
			version = "\"version\":{\"major\":"+major+"}";
		}
		else if(patch == null){
			version="\"version\":{\"major\":"+major+",\"minor\":"+minor+"}";
		}
		else{
			version="\"version\":{\"major\":"+major+",\"minor\":"+minor+",\"patch\":\""+patch+"\"}";
		}
		while(curTime < maxSleep){
			
			count = getEndPointsfromKubernetesFindRunning(name,GRMEdgeTestConstants.namespace,env,version).getServiceEndPointList().size();
			if(count == sizeExpected){
				passed = true;
				break;
			}
			System.out.println("Waiting for kubernetes to reflect changes. Sleeping for 5 seconds. Total Sleep Time: " + curTime +". Max Sleep Time: " + maxSleep);
			Thread.sleep(5000);
			curTime += 5000;
		}
	}
	
	public static void getEndPointsfromKubernetesFindRunningWithAutomatedSleep(String name, String env, long maxSleep, int sizeExpected) throws InterruptedException{

		long curTime = 0;
		boolean passed = false;
		int count = 0;

		while(curTime < maxSleep){
			count = getEndPointsfromKubernetesFindRunning(name,GRMEdgeTestConstants.namespace,env).getServiceEndPointList().size();
			if(count == sizeExpected){
				passed = true;
				break;
			}
			System.out.println("Waiting for kubernetes to reflect changes. Sleeping for 5 seconds. Total Sleep Time: " + curTime +". Max Sleep Time: " + maxSleep);
			Thread.sleep(5000);
			curTime += 5000;
		}
		assertTrue("Did not find the correct number of endpoints in Kubernetes with Find Running. Expected to find " + sizeExpected+". But Found: " + count, passed);
	}

	public static String getCurrentConfig() throws JSONException {
		String urlAndPath = GRMEdgeTestConstants.kubernetesBaseURL+GRMEdgeTestConstants.namespace+"/configmaps/"+GRMEdgeTestConstants.EDGE_CONFIG_MAP_NAME;

		JSONObject root = new JSONObject(sendRestRequest(urlAndPath,"","GET","Basic " + getAuthString()));
		JSONObject data = root.getJSONObject("data");
		Gson gson = new Gson();
		String grmedgeprops = gson.toJson(data.getString("grmedgeprops.properties"));
		return grmedgeprops;
	}

	public static void resetConfig(String patchUpdate) throws JSONException{
		String urlAndPath = GRMEdgeTestConstants.kubernetesBaseURL+GRMEdgeTestConstants.namespace+"/configmaps/"+GRMEdgeTestConstants.EDGE_CONFIG_MAP_NAME;
		String inputJSON = "{\"data\":{\"grmedgeprops.properties\":"+patchUpdate+"}}";
		Gson gson = new Gson();
		String json = gson.toJson(inputJSON);
		sendRestRequest(urlAndPath,inputJSON,"PATCH","Basic " + getAuthString());
	}

	public static void setConfigValue(String base, String string) throws JSONException {
		String inputJSON = base;
		String[] split = string.split("=");
		String name = split[0];
		String value = split[1];
		base=base.replaceAll("\\\\u003d", "=");
		int index = base.indexOf(name)+name.length()+1;
		inputJSON = base.substring(index);
		int index2 = base.substring(index).indexOf("\\n")+index;
		String oldValue = base.substring(index,index2);
		inputJSON = base.replace(name+"="+oldValue, name+"="+value);
		resetConfig(inputJSON);
	}

	public static String updateService2(String serviceName, String appName, String namespace){
		String urlAndPath = GRMEdgeTestConstants.kubernetesBaseURL+namespace+"/services/"+serviceName;
		//		String inputJSON = "{\"spec\":{\"ports\":[{\"name\":\"https\",\"port\":80}]}}";
		String inputJSON = loadFile("src/test/resources/kubeService");
		inputJSON = inputJSON.replace("<APP_NAME>",appName);
		inputJSON = inputJSON.replace("<SERVICE_NAME>", serviceName);
		inputJSON = inputJSON.replace("<NAMESPACE>",namespace);
		inputJSON = inputJSON.replace("<PORT_NAME>", "https");		
		inputJSON = inputJSON.replace("<ROUTE_OFFER>", "TEST");
		return sendRestRequest(urlAndPath,inputJSON, "PATCH", "Basic " + getAuthString());
	}

	public static String updateService3(String serviceName, String appName, String namespace){
		String urlAndPath = GRMEdgeTestConstants.kubernetesBaseURL+namespace+"/services/"+serviceName;
		//		String inputJSON = "{\"spec\":{\"ports\":[{\"name\":\"https\",\"port\":80}]}}";
		String inputJSON = loadFile("src/test/resources/kubeService2");
		inputJSON = inputJSON.replace("<APP_NAME>",appName);
		inputJSON = inputJSON.replace("<SERVICE_NAME>", serviceName);
		inputJSON = inputJSON.replace("<NAMESPACE>",namespace);
		inputJSON = inputJSON.replace("<PORT_NAME>", "https");		
		inputJSON = inputJSON.replace("<ROUTE_OFFER>", "DEFAULT");		
		inputJSON = inputJSON.replace("<LISTENPORT>", "31990");				
		return sendRestRequest(urlAndPath,inputJSON, "PATCH", "Basic " + getAuthString());
	}

	public static String updateService4(String serviceName, String appName, String namespace){
		String urlAndPath = GRMEdgeTestConstants.kubernetesBaseURL+namespace+"/services/"+serviceName;
		//		String inputJSON = "{\"spec\":{\"ports\":[{\"name\":\"https\",\"port\":80}]}}";
		String inputJSON = loadFile("src/test/resources/kubeService2");
		inputJSON = inputJSON.replace("<APP_NAME>",appName);
		inputJSON = inputJSON.replace("<SERVICE_NAME>", serviceName);
		inputJSON = inputJSON.replace("<NAMESPACE>",namespace);
		inputJSON = inputJSON.replace("<PORT_NAME>", "https");		
		inputJSON = inputJSON.replace("<ROUTE_OFFER>", "DEFAULT");		
		inputJSON = inputJSON.replace("<LISTENPORT>", "31992");				
		return sendRestRequest(urlAndPath,inputJSON, "PATCH", "Basic " + getAuthString());
	}

	public static void deleteSEPFromGRM(String serviceName, String env) {
		String urlAndPath = GRMEdgeTestConstants.GRM_REST_PATH_PORT_CONTEXT_DELETE;
		List<ServiceEndPoint> fromGRM = (List<ServiceEndPoint>) convertToFindRunningResponse(sendRestRequest(GRMEdgeTestConstants.GRM_REST_PATH_PORT_CONTEXT,getFindEndPointJSON(serviceName, "LAB"),"POST","Basic bTcxMzY0Om03MTM2NA==")).getServiceEndPointList();
		DeleteServiceEndPointRequest delete = new DeleteServiceEndPointRequest();
		for(ServiceEndPoint sep: fromGRM){
			delete.getServiceEndPoint().add(sep);
		}
		delete.setEnv("LAB");
		Gson gson = new Gson();
		String inputJSONBody = gson.toJson(delete);
		if (delete.getServiceEndPoint().size() > 0)
			sendRestRequest(urlAndPath,inputJSONBody,"POST","Basic bTcxMzY0Om03MTM2NA==");
		
		fromGRM = (List<ServiceEndPoint>) convertToFindRunningResponse(sendRestRequest(GRMEdgeTestConstants.GRM_REST_PATH_PORT_CONTEXT,getFindEndPointJSON(serviceName, "DEV"),"POST","Basic bTcxMzY0Om03MTM2NA==")).getServiceEndPointList();
		delete = new DeleteServiceEndPointRequest();
		for(ServiceEndPoint sep: fromGRM){
			delete.getServiceEndPoint().add(sep);
		}
		delete.setEnv("DEV");
		gson = new Gson();
		inputJSONBody = gson.toJson(delete);
		if (delete.getServiceEndPoint().size() > 0)
			sendRestRequest(urlAndPath,inputJSONBody,"POST","Basic bTcxMzY0Om03MTM2NA==");
		
		//now from other grm instance
		fromGRM = (List<ServiceEndPoint>) convertToFindRunningResponse(sendRestRequest(GRMEdgeTestConstants.GRM_REST_SECONDARY_PATH_PORT_CONTEXT,getFindEndPointJSON(serviceName, "LAB"),"POST","Basic bTcxMzY0Om03MTM2NA==")).getServiceEndPointList();
		delete = new DeleteServiceEndPointRequest();
		for(ServiceEndPoint sep: fromGRM){
			delete.getServiceEndPoint().add(sep);
		}
		delete.setEnv("LAB");
		gson = new Gson();
		inputJSONBody = gson.toJson(delete);
		if (delete.getServiceEndPoint().size() > 0)
			sendRestRequest(GRMEdgeTestConstants.GRM_REST_SECONDARY_PATH_PORT_CONTEXT_DELETE,inputJSONBody,"POST", "Basic bTcxMzY0Om03MTM2NA==");
		
		fromGRM = (List<ServiceEndPoint>) convertToFindRunningResponse(sendRestRequest(GRMEdgeTestConstants.GRM_REST_SECONDARY_PATH_PORT_CONTEXT,getFindEndPointJSON(serviceName, "DEV"),"POST","Basic bTcxMzY0Om03MTM2NA==")).getServiceEndPointList();
		delete = new DeleteServiceEndPointRequest();
		for(ServiceEndPoint sep: fromGRM){
			delete.getServiceEndPoint().add(sep);
		}
		delete.setEnv("DEV");
		gson = new Gson();
		inputJSONBody = gson.toJson(delete);
		if (delete.getServiceEndPoint().size() > 0)
			sendRestRequest(GRMEdgeTestConstants.GRM_REST_SECONDARY_PATH_PORT_CONTEXT_DELETE,inputJSONBody,"POST", "Basic bTcxMzY0Om03MTM2NA==");
		
		deleteSEPFromGRMWithVersion(serviceName, env, 1,0,"1");
		deleteSEPFromGRMWithVersion(serviceName,env,10,1,"1");
		deleteSEPFromGRMWithVersion(serviceName,env,100,1,"5");
	}
	public static void deleteSEPFromGRMWithVersion(String serviceName, String env, int major, int minor, String patch) {
		
		String urlAndPath = GRMEdgeTestConstants.GRM_REST_PATH_PORT_CONTEXT_DELETE;
		List<ServiceEndPoint> fromGRM = (List<ServiceEndPoint>) convertToFindRunningResponse(sendRestRequest(GRMEdgeTestConstants.GRM_REST_PATH_PORT_CONTEXT,getFindEndPointJSON(serviceName, "LAB",major,minor,patch),"POST","Basic bTcxMzY0Om03MTM2NA==")).getServiceEndPointList();
		DeleteServiceEndPointRequest delete = new DeleteServiceEndPointRequest();
		for(ServiceEndPoint sep: fromGRM){
			delete.getServiceEndPoint().add(sep);
		}
		delete.setEnv("LAB");
		Gson gson = new Gson();
		String inputJSONBody = gson.toJson(delete);
		if (delete.getServiceEndPoint().size() > 0)
			sendRestRequest(urlAndPath,inputJSONBody,"POST","Basic bTcxMzY0Om03MTM2NA==");
		
		fromGRM = (List<ServiceEndPoint>) convertToFindRunningResponse(sendRestRequest(GRMEdgeTestConstants.GRM_REST_PATH_PORT_CONTEXT,getFindEndPointJSON(serviceName, "DEV",major,minor,patch),"POST","Basic bTcxMzY0Om03MTM2NA==")).getServiceEndPointList();
		delete = new DeleteServiceEndPointRequest();
		for(ServiceEndPoint sep: fromGRM){
			delete.getServiceEndPoint().add(sep);
		}
		delete.setEnv("DEV");
		gson = new Gson();
		inputJSONBody = gson.toJson(delete);
		if (delete.getServiceEndPoint().size() > 0)
			sendRestRequest(urlAndPath,inputJSONBody,"POST","Basic bTcxMzY0Om03MTM2NA==");
		
		//now from other grm instance
		fromGRM = (List<ServiceEndPoint>) convertToFindRunningResponse(sendRestRequest(GRMEdgeTestConstants.GRM_REST_SECONDARY_PATH_PORT_CONTEXT,getFindEndPointJSON(serviceName, "LAB",major,minor,patch),"POST","Basic bTcxMzY0Om03MTM2NA==")).getServiceEndPointList();
		delete = new DeleteServiceEndPointRequest();
		for(ServiceEndPoint sep: fromGRM){
			delete.getServiceEndPoint().add(sep);
		}
		delete.setEnv("LAB");
		gson = new Gson();
		inputJSONBody = gson.toJson(delete);
		if (delete.getServiceEndPoint().size() > 0)
			sendRestRequest(GRMEdgeTestConstants.GRM_REST_SECONDARY_PATH_PORT_CONTEXT_DELETE,inputJSONBody,"POST", "Basic bTcxMzY0Om03MTM2NA==");
		
		fromGRM = (List<ServiceEndPoint>) convertToFindRunningResponse(sendRestRequest(GRMEdgeTestConstants.GRM_REST_SECONDARY_PATH_PORT_CONTEXT,getFindEndPointJSON(serviceName, "DEV",major,minor,patch),"POST","Basic bTcxMzY0Om03MTM2NA==")).getServiceEndPointList();
		delete = new DeleteServiceEndPointRequest();
		for(ServiceEndPoint sep: fromGRM){
			delete.getServiceEndPoint().add(sep);
		}
		delete.setEnv("DEV");
		gson = new Gson();
		inputJSONBody = gson.toJson(delete);
		if (delete.getServiceEndPoint().size() > 0)
			sendRestRequest(GRMEdgeTestConstants.GRM_REST_SECONDARY_PATH_PORT_CONTEXT_DELETE,inputJSONBody,"POST", "Basic bTcxMzY0Om03MTM2NA==");
	}
	
	public static void deleteSEPFromGRMSpecificWithCheck(String serviceName, String env) {
		
		String urlAndPath = GRMEdgeTestConstants.GRM_REST_PATH_PORT_CONTEXT_DELETE;
		
		List<ServiceEndPoint> fromGRM = (List<ServiceEndPoint>) convertToFindRunningResponse(sendRestRequest(GRMEdgeTestConstants.GRM_REST_PATH_PORT_CONTEXT,getFindEndPointJSON(serviceName, env),"POST","Basic bTcxMzY0Om03MTM2NA==")).getServiceEndPointList();
		DeleteServiceEndPointRequest delete = new DeleteServiceEndPointRequest();
		for(ServiceEndPoint sep: fromGRM){
			delete.getServiceEndPoint().add(sep);
		}
		delete.setEnv(env);
		Gson gson = new Gson();
		String inputJSONBody = gson.toJson(delete);
		if (delete.getServiceEndPoint().size() > 0)
			sendRestRequest(urlAndPath,inputJSONBody,"POST","Basic bTcxMzY0Om03MTM2NA==");
		
		fromGRM = (List<ServiceEndPoint>) convertToFindRunningResponse(sendRestRequest(GRMEdgeTestConstants.GRM_REST_PATH_PORT_CONTEXT,getFindEndPointJSON(serviceName, env),"POST","Basic bTcxMzY0Om03MTM2NA==")).getServiceEndPointList();
		assertTrue(fromGRM.size() == 0);
	}
	
	public static String addRCVersion(String name, String appName, String replicaCount,
			String namespace, String version) {
		String urlAndPath = GRMEdgeTestConstants.kubernetesBaseURL+namespace+"/replicationcontrollers";
		String inputJSON = loadFile("src/test/resources/kubeTestRcVersion");
		inputJSON = inputJSON.replace("<APP_NAME>",appName);
		inputJSON = inputJSON.replace("<RC_NAME>", name);
		inputJSON = inputJSON.replace("<REPLICA_COUNT>", replicaCount);
		inputJSON = inputJSON.replace("<IMAGE_NAME>", GRMEdgeTestConstants.IMAGE_NAME);
		inputJSON = inputJSON.replace("<NAMESPACE>",namespace);
		inputJSON = inputJSON.replace("<VERSION>",version);
		return sendRestRequest(urlAndPath,inputJSON,"POST","Basic " + getAuthString());
	}
	
	public static String addServiceVersion(String name, String appName, String namespace, String version){
		String urlAndPath = GRMEdgeTestConstants.kubernetesBaseURL+namespace+"/services";
		String inputJSON = loadFile("src/test/resources/kubeServiceVersion");
		inputJSON = inputJSON.replace("<APP_NAME>",appName);
		inputJSON = inputJSON.replace("<SERVICE_NAME>", name);
		inputJSON = inputJSON.replace("<NAMESPACE>",namespace);
		inputJSON = inputJSON.replace("<PORT_NAME>", "http");
		inputJSON = inputJSON.replace("<VERSION>",version);
		return sendRestRequest(urlAndPath,inputJSON,"POST","Basic " + getAuthString());
	}
	

	public static FindServiceEndPointResponse getEndPointsfromKubernetesFindRunningVersion(
			String name, String namespace, String env) {

			//find url from kubernetes api
			//first get pod name
			String podName = sendRestRequest(GRMEdgeTestConstants.kubernetesBaseURL+namespace+"/pods","","GET","Basic " + getAuthString());
			int grmedge = podName.indexOf("grm-edge-");
			podName = podName.substring(grmedge);
			podName = podName.substring(0,podName.indexOf("\""));
			//now describe pod so that we can get url
			String urlInfo = sendRestRequest(GRMEdgeTestConstants.kubernetesBaseURL+namespace+"/pods/"+podName,"","GET","Basic " + getAuthString());
			String hostipstring = "hostIP\":\"";
			int hostip = urlInfo.indexOf(hostipstring);
			urlInfo = urlInfo.substring(hostip);
			urlInfo = urlInfo.substring(hostipstring.length());
			urlInfo = urlInfo.substring(0,urlInfo.indexOf("\""));
			String inputJSONBody = getFindRunningJSONVersion(name, env);

			String urlAndPath = "https://"+urlInfo+":"+System.getProperty("GRM_EDGE_NODEPORT","31998")+"/GRMLWPService/v1/serviceEndPoint/findRunning";
			return convertToFindRunningResponse(sendRestRequest(urlAndPath, inputJSONBody,"POST","Basic " + getAuthString()));
	}

	private static String getFindRunningJSONVersion(String name, String env) {
		return "{\"serviceEndPoint\":{\"name\":\""+name+"\",\"version\":{\"major\":100,\"minor\":1,\"patch\":\"5\"}},\"env\":\""+env+"\"}";
	}

	public static FindServiceEndPointResponse getEndPointsFromGRMVersion(String name,
			String env) throws InterruptedException {
		//sleep for write behind
		Thread.sleep(5000);

		String urlAndPath = GRMEdgeTestConstants.GRM_REST_PATH_PORT_CONTEXT;
		String inputJSONBody = getFindEndPointJSONVersion(name, env);
		return convertToFindRunningResponse(sendRestRequest(urlAndPath,inputJSONBody,"POST","Basic bTcxMzY0Om03MTM2NA=="));
	}

	private static String getFindEndPointJSONVersion(String name, String env) {
		FindServiceEndPointRequest req = new FindServiceEndPointRequest();
		VersionDefinition vd = new VersionDefinition();
		vd.setMajor(100);
		vd.setMinor(1);
		vd.setPatch("5");
		req.setName(name);
		req.setVersion(vd);
		req.setFindLevel(FindLevel.ALL);
		req.setEnv(env);

		Gson gson = new Gson();
		String json = gson.toJson(req);
		return json;
	}

	public static FindServiceEndPointResponse getEndPointsfromKubernetesFindRunning(
			String name, String namespace, String env) throws InterruptedException {
		return getEndPointsfromKubernetesFindRunning(name, namespace, env, "\"version\":{\"major\":1,\"minor\":0,\"patch\":\"0\"}");
	}
	
	public static void addVersionFindRunningTestEndPoint(String serviceName, int major, int minor, String patch) {
		AddServiceEndPointRequest add = new AddServiceEndPointRequest();
		ServiceEndPoint sep = new ServiceEndPoint();
		sep.setHostAddress("localhost");
		sep.setListenPort("8080");
		VersionDefinition vd = new VersionDefinition();
		vd.setMajor(major);
		vd.setMinor(minor);
		vd.setPatch(patch);
		sep.setVersion(vd);
		sep.setContextPath("/");
		sep.setLatitude("1");
		sep.setLongitude("1");
		sep.setRouteOffer("TEST");
		sep.setName(serviceName);
		add.setEnv("LAB");
		add.setServiceEndPoint(sep);
		add.setCheckNcreateParents(true);
		Gson gson = new Gson();
		String json = gson.toJson(add);
		
		String urlAndPath = GRMEdgeTestConstants.GRM_REST_PATH_PORT_CONTEXT_ADD;
		String inputJSONBody = json;
		sendRestRequest(urlAndPath,inputJSONBody,"POST","Basic bTcxMzY0Om03MTM2NA==");
	}
		
	public static void addSyncTestEndPoint(int i) {
		AddServiceEndPointRequest add = new AddServiceEndPointRequest();
		ServiceEndPoint sep = new ServiceEndPoint();
		sep.setHostAddress("localhost");
		sep.setListenPort("808"+String.valueOf(i));
		VersionDefinition vd = new VersionDefinition();
		vd.setMajor(1);
		vd.setMinor(0);
		vd.setPatch("0");
		sep.setVersion(vd);
		sep.setRouteOffer("TEST");
		sep.setProtocol("http");
		sep.setContextPath("/");
		sep.setName(GRMEdgeTestConstants.SYNC_TEST_CASE_NAME);
		add.setEnv("LAB");
		sep.setLatitude("1");
		sep.setLongitude("1");
		add.setServiceEndPoint(sep);
		add.setCheckNcreateParents(true);
		Gson gson = new Gson();
		String json = gson.toJson(add);
		
		String urlAndPath = GRMEdgeTestConstants.GRM_REST_PATH_PORT_CONTEXT_ADD;
		String inputJSONBody = json;
		sendRestRequest(urlAndPath,inputJSONBody,"POST","Basic bTcxMzY0Om03MTM2NA==");
	}

	public static void deleteVersionFindRunningTestEndPoint(String serviceName, int major, int minor, String patch) {
		DeleteServiceEndPointRequest delete = new DeleteServiceEndPointRequest();
		ServiceEndPoint sep = new ServiceEndPoint();
		sep.setHostAddress("localhost");
		sep.setListenPort("8080");
		VersionDefinition vd = new VersionDefinition();
		vd.setMajor(major);
		vd.setMinor(minor);
		vd.setPatch(patch);
		sep.setVersion(vd);
		sep.setContextPath("/");
		sep.setLatitude("1");
		sep.setLongitude("1");
		sep.setRouteOffer("TEST");
		sep.setName(serviceName);
		delete.setEnv("LAB");
		delete.getServiceEndPoint().add(sep);
		Gson gson = new Gson();
		String json = gson.toJson(delete);
		
		String urlAndPath = GRMEdgeTestConstants.GRM_REST_PATH_PORT_CONTEXT_DELETE;
		String inputJSONBody = json;
		sendRestRequest(urlAndPath,inputJSONBody,"POST","Basic bTcxMzY0Om03MTM2NA==");
	}
	public static void deleteSyncTestEndPoint(int i) {
		DeleteServiceEndPointRequest delete = new DeleteServiceEndPointRequest();
		ServiceEndPoint sep = new ServiceEndPoint();
		sep.setHostAddress("localhost");
		sep.setListenPort("808"+String.valueOf(i));
		VersionDefinition vd = new VersionDefinition();
		vd.setMajor(1);
		vd.setMinor(0);
		vd.setPatch("0");
		sep.setVersion(vd);
		sep.setContextPath("/");
		sep.setLatitude("1");
		sep.setLongitude("1");
		sep.setRouteOffer("TEST");
		sep.setName(GRMEdgeTestConstants.SYNC_TEST_CASE_NAME);
		delete.setEnv("LAB");
		delete.getServiceEndPoint().add(sep);
		Gson gson = new Gson();
		String json = gson.toJson(delete);
		
		String urlAndPath = GRMEdgeTestConstants.GRM_REST_PATH_PORT_CONTEXT_DELETE;
		String inputJSONBody = json;
		sendRestRequest(urlAndPath,inputJSONBody,"POST","Basic bTcxMzY0Om03MTM2NA==");
	}

	public static void modifySyncTestEndPoint(int i) {
		UpdateServiceEndPointRequest update = new UpdateServiceEndPointRequest();
		ServiceEndPoint sep = new ServiceEndPoint();
		sep.setHostAddress("localhost");
		sep.setListenPort("808"+String.valueOf(i));
		VersionDefinition vd = new VersionDefinition();
		vd.setMajor(1);
		vd.setMinor(0);
		vd.setPatch("0");
		sep.setVersion(vd);
		sep.setRouteOffer("TEST");
		sep.setProtocol("http"); //TODO --sr
		sep.setName(GRMEdgeTestConstants.SYNC_TEST_CASE_NAME);
		update.setEnv("LAB");
		update.setServiceEndPoint(sep);
		Gson gson = new Gson();
		String json = gson.toJson(update);
		
		String urlAndPath = GRMEdgeTestConstants.GRM_REST_PATH_PORT_CONTEXT_UPDATE;
		String inputJSONBody = json;
		sendRestRequest(urlAndPath,inputJSONBody,"POST","Basic bTcxMzY0Om03MTM2NA==");
		
	}

	public static String getSynchronizeTime(String jsonOriginalConfig) {
		String base = jsonOriginalConfig.replaceAll("\\\\u003d", "=");
		jsonOriginalConfig=jsonOriginalConfig.replaceAll("\\\\u003d", "=");
		int index = jsonOriginalConfig.indexOf("PLATFORM_RUNTIME_GRMEDGE_DEFAULT_CACHESYNCHRONIZER_SLEEP")+"PLATFORM_RUNTIME_GRMEDGE_DEFAULT_CACHESYNCHRONIZER_SLEEP".length()+1;
		jsonOriginalConfig = jsonOriginalConfig.substring(index);
		int index2 = jsonOriginalConfig.indexOf("\\n")+index;
		String oldValue = base.substring(index,index2);
		return oldValue;
	}

	public static String addRCVersion(String name, String appName, String replicaCount,
			String namespace) {
		String urlAndPath = GRMEdgeTestConstants.kubernetesBaseURL+namespace+"/replicationcontrollers";
		String inputJSON = loadFile("src/test/resources/kubeTestRcVersion");
		inputJSON = inputJSON.replace("<APP_NAME>",appName);
		inputJSON = inputJSON.replace("<RC_NAME>", name);
		inputJSON = inputJSON.replace("<REPLICA_COUNT>", replicaCount);
		inputJSON = inputJSON.replace("<IMAGE_NAME>", GRMEdgeTestConstants.IMAGE_NAME);
		inputJSON = inputJSON.replace("<NAMESPACE>",namespace);
		inputJSON = inputJSON.replace("<VERSION>","100.1.5");
		inputJSON = inputJSON.replace("<ROUTE_OFFER>", "TEST");
		return sendRestRequest(urlAndPath,inputJSON,"POST","Basic " + getAuthString());
	}
	
	public static String addServiceVersion(String name, String appName, String namespace){
		String urlAndPath = GRMEdgeTestConstants.kubernetesBaseURL+namespace+"/services";
		String inputJSON = loadFile("src/test/resources/kubeServiceVersion");
		inputJSON = inputJSON.replace("<APP_NAME>",appName);
		inputJSON = inputJSON.replace("<SERVICE_NAME>", name);
		inputJSON = inputJSON.replace("<NAMESPACE>",namespace);
		inputJSON = inputJSON.replace("<PORT_NAME>", "http");
		inputJSON = inputJSON.replace("<VERSION>","100.1.5");
		return sendRestRequest(urlAndPath,inputJSON,"POST","Basic " + getAuthString());
	}

	public static void modifySyncTestEndPointK8InGRM(String hostAddress, String port, String protocol) {
		UpdateServiceEndPointRequest update = new UpdateServiceEndPointRequest();
		ServiceEndPoint sep = new ServiceEndPoint();
		sep.setHostAddress(hostAddress);
		sep.setListenPort(port);
		VersionDefinition vd = new VersionDefinition();
		vd.setMajor(1);
		vd.setMinor(0);
		vd.setPatch("0");
		sep.setVersion(vd);
		sep.setRouteOffer("TEST");
		sep.setProtocol(protocol);
		sep.setLatitude("1");
		sep.setLongitude("1");
		sep.setName(GRMEdgeTestConstants.SYNC_TEST_CASE_NAME);
		update.setEnv("LAB");
		update.setServiceEndPoint(sep);
		Gson gson = new Gson();
		String json = gson.toJson(update);
		
		String urlAndPath = GRMEdgeTestConstants.GRM_REST_PATH_PORT_CONTEXT_UPDATE;
		String inputJSONBody = json;
		sendRestRequest(urlAndPath,inputJSONBody,"POST","Basic bTcxMzY0Om03MTM2NA==");
	}

	public static String addRCRouteOffer(String name, String appName, String replicaCount,
			String namespace, String routeOffer) {
		String urlAndPath = GRMEdgeTestConstants.kubernetesBaseURL+namespace+"/replicationcontrollers";
		String inputJSON = loadFile("src/test/resources/kubeTestRc");
		inputJSON = inputJSON.replace("<APP_NAME>",appName);
		inputJSON = inputJSON.replace("<RC_NAME>", name);
		inputJSON = inputJSON.replace("<REPLICA_COUNT>", replicaCount);
		inputJSON = inputJSON.replace("<IMAGE_NAME>", GRMEdgeTestConstants.IMAGE_NAME);
		inputJSON = inputJSON.replace("<NAMESPACE>",namespace);
		inputJSON = inputJSON.replace("<ROUTE_OFFER>", routeOffer);
		return sendRestRequest(urlAndPath,inputJSON,"POST","Basic " + getAuthString());
	}

	public static String addServiceRouteOffer(String name, String appName, String namespace, String routeOffer){
			String urlAndPath = GRMEdgeTestConstants.kubernetesBaseURL+namespace+"/services";
		String inputJSON = loadFile("src/test/resources/kubeService");
		inputJSON = inputJSON.replace("<APP_NAME>",appName);
		inputJSON = inputJSON.replace("<SERVICE_NAME>", name);
		inputJSON = inputJSON.replace("<NAMESPACE>",namespace);
		inputJSON = inputJSON.replace("<PORT_NAME>", "http");
		inputJSON = inputJSON.replace("<ROUTE_OFFER>", routeOffer);
		return sendRestRequest(urlAndPath,inputJSON,"POST","Basic " + getAuthString());
	}
    
    /**
     * Returns a list of all the GRM Edge pod names.
     * 
     * @return List of all the GRM Edge pod names.
     * @throws JSONException 
     */
    public static List<String> getGRMEdgePodNames()
    {
        List<String> nameList = new ArrayList<String>();
        
        // first get pod name via kubernetes
        String pods = sendRestRequest(GRMEdgeTestConstants.kubernetesBaseURL + //url
                                      GRMEdgeTestConstants.namespace+"/pods", //namespace and path
                                      "", //json
                                      "GET", //method
                                      "Basic " + getAuthString()); //auth-string
        
        try{
            JSONObject root = new JSONObject(pods);
            JSONArray items = root.getJSONArray("items");
            for(int i = 0; i < items.length(); i++){
            	JSONObject pod = items.getJSONObject(i);
            	JSONObject metadata = pod.getJSONObject("metadata");
            	String name = metadata.getString("name");
            	System.out.println(name);
            	if(name.contains("grm-edge-")){
            		if(!name.contains("health")){
            			nameList.add(name);
            		}
            	}
            }	
        }
        catch(Exception e){
        	assertTrue("Received error while getting pod names for grm edge",false);
        }
        
        return nameList;
    }
    
    /**
     * Returns the output of kubectl log call which is basically the contents
     * of the log for the given pod.
     * 
     * @param podName The name of the pod we want the log from
     * @return Log file content for given pod
     */
    public static String getLogContentForGivenPod(String podName)
    {
        // the API call to get log looks like this GET http://localhost:8080/api/v1/namespaces/com-att-ocnp-mgmt/pods/<pod name>/log
        return sendRestRequest(GRMEdgeTestConstants.kubernetesBaseURL + //url
                               GRMEdgeTestConstants.namespace + "/pods/" + podName + "/log?container=grm-edge", //namespace and path
                               "", //json
                               "GET", //method
                               "Basic " + getAuthString()); //auth-string
    }
    
    /**
     * Returns the number current of Hazelcast members using the cache based on
     * the given string which represents the output of the kubectl log command.
     * 
     * The log file should contain something similar to the following:
     * 
     * Members [1] {
     *          Member [10.233.82.3]:31999 - c940dec3-ee11-4c6a-92ba-3faa728ff6a2 this
     * }
     * 
     * The number inside the brackets is the current number of Hazelcast members
     * using the cache.
     * 
     * @param logContent String representing the content of the log
     * @return Number of Hazelcast members using cache
     */
    public static int getNumberOfHazelcastMembersFromLog(String logContent)
    {
        // find location last occurrence of Members specification
        int indexOfHazelcastMembers = logContent.lastIndexOf("Members [");
        if (indexOfHazelcastMembers == -1)
        {
            System.err.println("Could not find hazelcast members in log.");
            return -1;
        }
        
        // this substring has enough slack to get a complete number of instances even if very large
        String hazelcastInstancesExcerpt = logContent.substring(indexOfHazelcastMembers, indexOfHazelcastMembers + 13);
        
        
        int indexOfBeginBracket = hazelcastInstancesExcerpt.indexOf("[");
        int indexOfEndBracket = hazelcastInstancesExcerpt.indexOf("]");
        
        // get string containing number of instances
        String numberOfHazelcastMembersStr = hazelcastInstancesExcerpt.substring(indexOfBeginBracket+1, indexOfEndBracket);
        
        // try to convert string to int 
        int numberOfHazelcastMembers;
        try
        {
            numberOfHazelcastMembers = Integer.parseInt(numberOfHazelcastMembersStr);
        }
        catch(NumberFormatException e)
        {
            System.err.println("Error parsing string to integer.\n" + e.getStackTrace());
            numberOfHazelcastMembers = 0;
        }
                
        return numberOfHazelcastMembers;
    }
    
	public static void updateServiceVersion(String serviceName, String appName,
			String namespace, String version) {
		String urlAndPath = GRMEdgeTestConstants.kubernetesBaseURL+namespace+"/services/"+serviceName;
		String inputJSON = "{\"spec\":{\"selector\":{\"version\":\""+version+"\"}}}";
		//		String inputJSON = loadFile("src/test/resources/kubeService");
		//		inputJSON = inputJSON.replace("<APP_NAME>",appName);
		//		inputJSON = inputJSON.replace("<SERVICE_NAME>", serviceName);
		//		inputJSON = inputJSON.replace("<NAMESPACE>",namespace);
		//		inputJSON = inputJSON.replace("<PORT_NAME>", "https");		
		sendRestRequest(urlAndPath,inputJSON, "PATCH", "Basic " + getAuthString());
	}
	
	
//	 * Get the endpoints from Kubernetes service cache
	
	public static FindServiceEndPointResponse getEndPointsFromKubernetesServiceCache(String name, String env) throws InterruptedException{
		//path to grmedge running in k8 env

		//find url from kubernetes api
		//first get pod name
		String podName = sendRestRequest(GRMEdgeTestConstants.kubernetesBaseURL+GRMEdgeTestConstants.namespace+"/pods","","GET","Basic " + getAuthString());
		int grmedge = podName.indexOf("grm-edge-");
		podName = podName.substring(grmedge);
		podName = podName.substring(0,podName.indexOf("\""));
		//now describe pod so that we can get url
		String urlInfo = sendRestRequest(GRMEdgeTestConstants.kubernetesBaseURL+GRMEdgeTestConstants.namespace+"/pods/"+podName,"","GET","Basic " + getAuthString());
		String hostipstring = "hostIP\":\"";
		int hostip = urlInfo.indexOf(hostipstring);
		urlInfo = urlInfo.substring(hostip);
		urlInfo = urlInfo.substring(hostipstring.length());
		urlInfo = urlInfo.substring(0,urlInfo.indexOf("\""));
		//String inputJSONBody = getEndPointJSON(name, env);

		String urlAndPath = "https://"+urlInfo+":"+System.getProperty("GRM_EDGE_NODEPORT","31998")+"/GRMLWPService/v1/serviceEndPoint/getFromCache?serviceName="+name+"&env="+env;
			FindServiceEndPointResponse eps = convertToFindRunningResponse(sendRestRequest(urlAndPath, "","GET","Basic " + getAuthString()));
			return eps;
		
	}


	public static ServiceEndPoint getEndpointByName(List<ServiceEndPoint> endPointList, String serviceName) {

		ServiceEndPoint matchedEndPoint = null;

		for (ServiceEndPoint endPoint : endPointList) {
			if (endPoint.getName().equals(serviceName)) {
				matchedEndPoint = endPoint;
				break;

			}

		}
		return matchedEndPoint;

	}
	
	public static String getEnvName(ServiceEndPoint endPoint) {

		String envName = "";

		List<NameValuePair> nameValuePairs = endPoint.getProperties();
		for (NameValuePair valuePair : nameValuePairs) {
			if (valuePair.getName().equals("Environment")) {

				envName = valuePair.getValue();
				break;

			}

		}
		return envName;

	}

	public static int getNumberOfNodes() throws JSONException {
		String response = sendRestRequest("https://"+System.getProperty("KUBE_API_HOST")+":"+ System.getProperty("KUBE_API_PORT","443")+ "/api/v1/nodes","","GET","Basic " + getAuthString());
		JSONObject root = new JSONObject(response);
		JSONArray nodeArr = root.getJSONArray("items");
		return nodeArr.length();
	}

	public static void createDummyServices(int numberOfUnique) {
		for(int i = 0; i < numberOfUnique; i++)
			addService(GRMEdgeTestConstants.DUMMY_TESTCASE_NAME+i, GRMEdgeTestConstants.DUMMY_TESTCASE_NAME+i, GRMEdgeTestConstants.dummynamespace);
	}

	public static void createDummyPods(int numberOfUnique, int numberOfNodes) {
		for(int i = 0; i < numberOfUnique; i++)
			addRC(GRMEdgeTestConstants.DUMMY_TESTCASE_NAME+i, GRMEdgeTestConstants.DUMMY_TESTCASE_NAME+i, String.valueOf(numberOfNodes), GRMEdgeTestConstants.dummynamespace);
	}
	
	public static void deleteDummyServices(int numberOfUnique) throws InterruptedException {
		for(int i = 0; i < numberOfUnique; i++)
			deleteService(GRMEdgeTestConstants.DUMMY_TESTCASE_NAME+i, GRMEdgeTestConstants.dummynamespace);
	}

	public static void deleteDummyPods(int numberOfUnique, int numberOfNodes) throws InterruptedException {
		for(int i = 0; i < numberOfUnique; i++)
			deleteRc(GRMEdgeTestConstants.DUMMY_TESTCASE_NAME+i, GRMEdgeTestConstants.dummynamespace);
	}
	
	//	 * Refresh Service Cache 
	
	public static String refreshServiceCache(String username,String pass) throws InterruptedException{

		//find url from kubernetes api
		//first get pod name
		String podName = sendRestRequest(GRMEdgeTestConstants.kubernetesBaseURL+GRMEdgeTestConstants.namespace+"/pods","","GET","Basic " + getAuthString());
		int grmedge = podName.indexOf("grm-edge-");
		podName = podName.substring(grmedge);
		podName = podName.substring(0,podName.indexOf("\""));
		//now describe pod so that we can get url
		String urlInfo = sendRestRequest(GRMEdgeTestConstants.kubernetesBaseURL+GRMEdgeTestConstants.namespace+"/pods/"+podName,"","GET","Basic " + getAuthString());
		String hostipstring = "hostIP\":\"";
		int hostip = urlInfo.indexOf(hostipstring);
		urlInfo = urlInfo.substring(hostip);
		urlInfo = urlInfo.substring(hostipstring.length());
		urlInfo = urlInfo.substring(0,urlInfo.indexOf("\""));
		//String inputJSONBody = getEndPointJSON(name, env);

		String urlAndPath = "https://"+urlInfo+":"+System.getProperty("GRM_EDGE_NODEPORT","31998")+"/GRMLWPService/v1/management/refreshCacheWithAPIServer";
			String response= sendRestRequest(urlAndPath, "","GET","Basic " + getAuthStringForUser(username, pass));
			return response.trim();
		
	}
	
	private static String getAuthStringForUser(String name,String pass){
		
		String authString = name + ":" + pass;
		byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
		String authStringEnc = new String(authEncBytes);
		return authStringEnc;
	}

	public static String getEdgeHealth() {
		//path to grmedge running in k8 env
		//Find NODEPORT from kubernetes api
		
		//find url from kubernetes api
		//first get pod name
		String podName = sendRestRequest(GRMEdgeTestConstants.kubernetesBaseURL+GRMEdgeTestConstants.namespace+"/pods","","GET","Basic " + getAuthString());
		int grmedge = podName.indexOf("grm-edge-");
		podName = podName.substring(grmedge);
		podName = podName.substring(0,podName.indexOf("\""));
		//now describe pod so that we can get url
		String urlInfo = sendRestRequest(GRMEdgeTestConstants.kubernetesBaseURL+GRMEdgeTestConstants.namespace+"/pods/"+podName,"","GET","Basic " + getAuthString());
		String hostipstring = "hostIP\":\"";
		int hostip = urlInfo.indexOf(hostipstring);
		urlInfo = urlInfo.substring(hostip);
		urlInfo = urlInfo.substring(hostipstring.length());
		urlInfo = urlInfo.substring(0,urlInfo.indexOf("\""));
		//String inputJSONBody = getEndPointJSON(name, env);

		String urlAndPath = "https://"+urlInfo+":"+System.getProperty("GRM_EDGE_NODEPORT","31998")+"/GRMLWPService/v1/health";
		return sendRestRequest(urlAndPath, "", "GET", "Basic " + getAuthString());
	}

	public static void updateSyncEndpointInGRMToHttps() throws InterruptedException {
		List<ServiceEndPoint> seps = GRMEdgeTestUtil.getEndPointsFromGRM(GRMEdgeTestConstants.SYNC_TEST_CASE_NAME,"LAB").getServiceEndPointList();
		assertEquals(1,seps.size());
		ServiceEndPoint sep = seps.get(0);
		sep.setProtocol("https");
		UpdateServiceEndPointRequest update = new UpdateServiceEndPointRequest();
		update.setEnv("LAB");
		update.setServiceEndPoint(sep);
		String urlAndPath = GRMEdgeTestConstants.GRM_REST_PATH_PORT_CONTEXT_UPDATE;
		Gson gson = new Gson();
		String json = gson.toJson(update);
		sendRestRequest(urlAndPath,json,"POST","Basic bTcxMzY0Om03MTM2NA==");
	}

	public static void addSyncTestEndPointWithSameClusterInfo(String clusterName) {
		AddServiceEndPointRequest add = new AddServiceEndPointRequest();
		ServiceEndPoint sep = new ServiceEndPoint();
		sep.setHostAddress("localhost");
		sep.setListenPort("8085");
		VersionDefinition vd = new VersionDefinition();
		vd.setMajor(1);
		vd.setMinor(0);
		vd.setPatch("0");
		sep.setVersion(vd);
		sep.setRouteOffer("TEST");
		sep.setProtocol("http");
		sep.setContextPath("/");
		sep.setName(GRMEdgeTestConstants.SYNC_TEST_CASE_NAME);
		add.setEnv("LAB");
		sep.setLatitude("1");
		sep.setLongitude("1");
		
		NameValuePair nvp = new NameValuePair();
		nvp.setName("Environment");
		nvp.setValue("LAB");
		sep.getProperties().add(nvp);
		nvp = new NameValuePair();
		nvp.setName("cpfrun_cluster_name");
		nvp.setValue(clusterName);
		sep.getProperties().add(nvp);
		
		add.setServiceEndPoint(sep);
		add.setCheckNcreateParents(true);
		Gson gson = new Gson();
		String json = gson.toJson(add);
		String urlAndPath = GRMEdgeTestConstants.GRM_REST_PATH_PORT_CONTEXT_ADD;
		String inputJSONBody = json;
		sendRestRequest(urlAndPath,inputJSONBody,"POST","Basic bTcxMzY0Om03MTM2NA==");
	}
}
