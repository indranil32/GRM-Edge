/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.ocnp.mgmt.grm_edge_service.businessprocess;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.att.ocnp.mgmt.grm_edge_service.EdgeException;
import com.att.ocnp.mgmt.grm_edge_service.util.CustomXGCalConverter;
import com.att.ocnp.mgmt.grm_edge_service.util.GRMEdgeConstants;
import com.att.scld.grm.types.v1.FindLevel;
import com.att.scld.grm.types.v1.ServiceEndPoint;
import com.att.scld.grm.v1.AddServiceEndPointRequest;
import com.att.scld.grm.v1.AddServiceEndPointResponse;
import com.att.scld.grm.v1.DeleteServiceEndPointRequest;
import com.att.scld.grm.v1.DeleteServiceEndPointResponse;
import com.att.scld.grm.v1.DeregisterDMEIngressRequest;
import com.att.scld.grm.v1.DeregisterDMEIngressResponse;
import com.att.scld.grm.v1.FindRunningServiceEndPointRequest;
import com.att.scld.grm.v1.FindRunningServiceEndPointResponse;
import com.att.scld.grm.v1.GetDMEIngressRequest;
import com.att.scld.grm.v1.GetDMEIngressResponse;
import com.att.scld.grm.v1.RegisterDMEIngressRequest;
import com.att.scld.grm.v1.RegisterDMEIngressResponse;
import com.att.scld.grm.v1.UpdateServiceEndPointRequest;
import com.att.scld.grm.v1.UpdateServiceEndPointResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class EdgeGRMHelper {

	private static final Logger logger = LoggerFactory.getLogger(EdgeGRMHelper.class);
	
	private static String grmFindRunning = "GRMLWPService/v1/serviceEndPoint/findRunning";
	private static String grmAddSEP = "GRMLWPService/v1/serviceEndPoint/add";
	private static String grmDeleteSEP = "GRMLWPService/v1/serviceEndPoint/delete";
	private static String grmUpdateSEP = "GRMLWPService/v1/serviceEndPoint/update";
	private static String grmRegisterDMEIngress = "GRMLWPService/v1/dmeIngress/register";
	private static String grmGetDMEIngress = "GRMLWPService/v1/dmeIngress/get";
	private static String grmDeregisterDMEIngress = "GRMLWPService/v1/dmeIngress/deregister";
	
	/*
	 * Needed so that it trusts the certificate from GRM while making connections
	 */
	static {
		 HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier()
	        {
	            public boolean verify(String hostname, SSLSession session)
	            {
	               return true;
	            }
	        });
	}
	
	public static List<ServiceEndPoint> getEndpointsWithKey(String keyToCheck) throws Exception {
		
		logger.trace( "Calling GRM Rest Service for endpoints");
		FindRunningServiceEndPointRequest req = new FindRunningServiceEndPointRequest();
		
		String env = extractEnvironment(keyToCheck.toString());
		String serviceName = extractServiceName(keyToCheck.toString(), env);
		
		if(StringUtils.isEmpty(env) || StringUtils.isEmpty(serviceName)){
			throw new EdgeException(GRMEdgeConstants.CRIT_ERROR_UNABLE_TO_PARSE_ENV_SERVICE_NAME,GRMEdgeConstants.CRIT_ERROR_UNABLE_TO_PARSE_ENV_SERVICE_NAME_MSG,keyToCheck.toString());

		}
		
		req.setEnv(env);
		ServiceEndPoint sep = new ServiceEndPoint();
		sep.setName(serviceName);
		req.setServiceEndPoint(sep);
		req.setFindLevel(FindLevel.ALL);
		FindRunningServiceEndPointResponse grmResponse = getGRMEndpointList(req);
		if(grmResponse != null){
			logger.debug( "Size of endpoints returned from GRM Rest: " + grmResponse.getServiceEndPointList().size());
			return grmResponse.getServiceEndPointList();
		}
		return null;	
	}
	
	/*
	 * 
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

	/*
	 * the cache should have ENV+serviceName as its key. This is trying to parse out the serviceName from the cache key
	 */
	public static String extractServiceName(String string, String env) {
		if (env == null || string == null || string.length()==env.length()){
			return null;
		}
		return string.substring(env.length());
	}

	private static String invokeGRM(Object request, String grmContextForMethod) {
		
		URL url = null;
		int respCode = 0;
		Gson gson = new Gson();
		Map<String,String> endpointAccessRecordMap = new HashMap<String,String>();
		
		List<URL> grmEndpointsToAttempt = new ArrayList<>();
		
		for (String aHost : System.getProperty("null","null").split(",")) {
			try {
				logger.trace("GRM Host: " + aHost);
				grmEndpointsToAttempt.add(new URL("https://"+ aHost +":"+System.getProperty("null","null") +"/"+ grmContextForMethod));
			} catch (Exception e) {
				logger.error("Error retrieving GRM URLs", e);
				return null;
			}
		}
		
		if (grmEndpointsToAttempt.isEmpty()) {
			logger.error("Error invoking GRM. No CPFRUN_GRMEDGE_GRM_HOST specified.");
			return null;
		}
		
		long seed = System.nanoTime();
		Collections.shuffle(grmEndpointsToAttempt, new Random(seed));
		
		Iterator<URL> iter = grmEndpointsToAttempt.iterator();
		
		while (iter.hasNext()) {
			HttpsURLConnection connection = null;

			try{ 
				//Create connection 
				url = iter.next();

				connection = (HttpsURLConnection)url.openConnection(); 
				connection.setRequestMethod("POST"); 
				connection.setRequestProperty("Content-Type", "application/json");
				//TODO fix authorization 
				connection.setRequestProperty("Authorization", "null");
				connection.setUseCaches(false); 
				connection.setDoOutput(true); 

				// 2. Java object to JSON, and assign to a String 
				String jsonInString = gson.toJson(request); 
				
				logger.trace("Calling GRM URL: " + url.toString());

				//Send request 
				DataOutputStream wr = new DataOutputStream(connection.getOutputStream()); 
				wr.writeBytes(jsonInString); 
				wr.close(); 
				
				respCode = connection.getResponseCode();

				if (respCode != 200) {
					String faultResponse = null;

					if (connection.getInputStream() != null) {
						faultResponse = parseResponse(connection.getInputStream());
					} else if (connection.getErrorStream() != null) {
						faultResponse = parseResponse(connection.getErrorStream());
					} else {
						faultResponse = connection.getResponseMessage();
					}

					try {
						endpointAccessRecordMap.put(url.toString(), "\"" + respCode + "\" :" + faultResponse);
					} catch (Exception e) {}
					
					throw new Exception("GRM Call Failed; StatusCode=" + respCode + "; GRMFaultResponse=" + faultResponse);

				} else {

					//Get Response 
					InputStream is = connection.getInputStream(); 
					BufferedReader rd = new BufferedReader(new InputStreamReader(is)); 
					StringBuilder response = new StringBuilder(); // or StringBuffer if not Java 5+ 
					String line; 
					while((line = rd.readLine()) != null) { 
						response.append(line); 
						response.append('\r'); 
					}
					rd.close();
					
					return response.toString();
				}
				
			} catch(SocketTimeoutException e){
				
				logger.debug("Error in Calling GRM URL: " + url.toString(), e.getMessage());
				
				try {
					endpointAccessRecordMap.put(url.toString(), "\"" + respCode + "\" :" + e.getMessage());
				} catch (Exception ex) {}
				
				iter.remove();
				
			} catch(ConnectException e){
				
				logger.debug("Error in Calling GRM URL: " + url.toString(), e.getMessage());
				
				try {
					endpointAccessRecordMap.put(url.toString(), "\"" + respCode + "\" :" + e.getMessage());
				} catch (Exception ex) {}
				
				iter.remove();
				
			} catch(Throwable e){
				
				logger.debug("Error in Calling GRM URL: " + url.toString(), e.getMessage());
				
				try {
					endpointAccessRecordMap.put(url.toString(), "\"" + respCode + "\" :" + e.getMessage());
				} catch (Exception ex) {}
				
				iter.remove();
				
			} finally{ 
				if(connection != null) { 
					connection.disconnect(); 
				} 
			}
		}
		
		StringBuffer buf = new StringBuffer();
		buf.append(" [GRM Endpoint Access Trace: " );
		
		for (String key : endpointAccessRecordMap.keySet()) {
			buf.append("[");
			buf.append(key);
			buf.append("=");
			buf.append(endpointAccessRecordMap.get(key));
			buf.append("]");
		}
		buf.append("], A http status code of 0 might indicate the error occurred before the request is sent or during sending.");
		
		logger.error("Error invoking GRM ", buf.toString());
		return null;
	}
	
	public static FindRunningServiceEndPointResponse getGRMEndpointList(FindRunningServiceEndPointRequest req){ 
		
		logger.trace("Invoking FindRunningServiceEndPoint");

		FindRunningServiceEndPointResponse resp = null;
		
		String response = invokeGRM(req, grmFindRunning);
		
		if (response != null) {
			Gson gsonForFindRunning = new GsonBuilder().registerTypeAdapter(XMLGregorianCalendar.class, new CustomXGCalConverter.Serializer()) 
					.registerTypeAdapter(XMLGregorianCalendar.class, new CustomXGCalConverter.Deserializer()).create(); 
			resp = gsonForFindRunning.fromJson(response.toString().replace("ServiceEndPointList", "serviceEndPointList"), FindRunningServiceEndPointResponse.class);
		}

		return resp;
	}
	
	public static UpdateServiceEndPointResponse updateGRMEndPoint(UpdateServiceEndPointRequest update) {
		
		logger.trace("Invoking UpdateServiceEndPoint");
		
		UpdateServiceEndPointResponse resp = null;
		
		String response = invokeGRM(update, grmUpdateSEP);
		
		if (response != null) {
			Gson gson = new Gson();
			resp = gson.fromJson(response.toString() , UpdateServiceEndPointResponse.class);
		}

		return resp;
	}

	public static DeleteServiceEndPointResponse grmDeleteSEP(DeleteServiceEndPointRequest delete) {
		
		logger.trace("Invoking DeleteServiceEndPoint");
		
		DeleteServiceEndPointResponse resp = null;
		
		String response = invokeGRM(delete, grmDeleteSEP);
		
		if (response != null) {
			Gson gson = new Gson();
			resp = gson.fromJson(response.toString() , DeleteServiceEndPointResponse.class);
		}

		return resp;
	}
	
	public static AddServiceEndPointResponse addGRMEndPoint(AddServiceEndPointRequest add) {
		
		logger.trace("Invoking AddServiceEndPoint");

		AddServiceEndPointResponse resp = null;
		
		String response = invokeGRM(add, grmAddSEP);
		
		if (response != null) {
			Gson gson = new Gson();
			resp = gson.fromJson(response.toString() , AddServiceEndPointResponse.class);
		}

		return resp;
	}
	
	public static RegisterDMEIngressResponse registerDMEIngress(RegisterDMEIngressRequest req) {
		
		logger.trace("Invoking RegisterDMEIngress");
		
		RegisterDMEIngressResponse resp = new RegisterDMEIngressResponse();
		
		String response = invokeGRM(req, grmRegisterDMEIngress);
		
		if (response != null) {
			Gson gson = new Gson();
			resp = gson.fromJson(response.toString() , RegisterDMEIngressResponse.class);
		}
		
		return resp;
	}
	
	public static GetDMEIngressResponse getDMEIngress(GetDMEIngressRequest req) {
		
		logger.trace("Invoking GetDMEIngress");
		
		GetDMEIngressResponse resp = new GetDMEIngressResponse();
		
		String response = invokeGRM(req, grmGetDMEIngress);
		
		if (response != null) {
			Gson gson = new Gson();
			resp = gson.fromJson(response.toString() , GetDMEIngressResponse.class);
		}
		
		return resp;
	}
	
	public static DeregisterDMEIngressResponse deregisterDMEIngress(DeregisterDMEIngressRequest req) {
		
		logger.trace("Invoking DeregisterDMEIngress");
		
		DeregisterDMEIngressResponse resp = new DeregisterDMEIngressResponse();
		
		String response = invokeGRM(req, grmDeregisterDMEIngress);
		
		if (response != null) {
			Gson gson = new Gson();
			resp = gson.fromJson(response.toString() , DeregisterDMEIngressResponse.class);
		}
		
		return resp;
	}
	
	private static String parseResponse(InputStream istream) throws IOException {
		byte[] buffer = new byte[1024];
		int iter_length = 0; // number of characters read for each iter
		int total_length = 0; // total length read from stream
		StringBuffer strBuffer = new StringBuffer();
		while (iter_length != -1) {
			iter_length = istream.read(buffer, 0, 1024);

			if (iter_length >= 0) {
				String tmpStr = new String(buffer, 0, iter_length);
				total_length = total_length + iter_length;
				strBuffer.append(tmpStr);
			}
		}
		return strBuffer.toString();
	}
}