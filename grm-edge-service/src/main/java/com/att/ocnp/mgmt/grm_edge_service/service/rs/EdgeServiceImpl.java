/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.ocnp.mgmt.grm_edge_service.service.rs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Path;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Controller;

import com.att.ocnp.mgmt.grm_edge_service.EdgeException;
import com.att.ocnp.mgmt.grm_edge_service.businessprocess.EdgeGRMHelper;
import com.att.ocnp.mgmt.grm_edge_service.businessprocess.K8ServiceController;
import com.att.ocnp.mgmt.grm_edge_service.cache.ServiceRegistry;
import com.att.ocnp.mgmt.grm_edge_service.topology.data.DataAccessManager;
import com.att.ocnp.mgmt.grm_edge_service.types.K8Service;
import com.att.ocnp.mgmt.grm_edge_service.types.KubePod;
import com.att.ocnp.mgmt.grm_edge_service.util.GRMEdgeConstants;
import com.att.ocnp.mgmt.grm_edge_service.util.GRMEdgeErrorGenerator;
import com.att.ocnp.mgmt.grm_edge_service.util.GRMEdgeUtil;
import com.att.scld.grm.types.v1.FindLevel;
import com.att.scld.grm.types.v1.OperationalInfo;
import com.att.scld.grm.types.v1.ServiceEndPoint;
import com.att.scld.grm.types.v1.StatusInfo;
import com.att.scld.grm.types.v1.VersionDefinition;
import com.att.scld.grm.v1.FindRunningServiceEndPointRequest;
import com.att.scld.grm.v1.FindRunningServiceEndPointResponse;

/*
 * Implementation for rest service. Prototypes of Add and Delete were added but are not supported. 
 * They are used for testing purposes only.
 */
@Controller
@Path("/serviceEndPoint")
public class EdgeServiceImpl implements EdgeService {
	private static final Logger logger = LoggerFactory.getLogger(EdgeServiceImpl.class.getName());

	@Override
	public FindRunningServiceEndPointResponse getFromCache(String serviceName, String env) {
		logger.debug("Running Query with serviceName={} and env={}",serviceName,env);
		
		FindRunningServiceEndPointResponse response = new FindRunningServiceEndPointResponse();
		
		if (serviceName.isEmpty()){
			serviceName = "*";
		}
		
		if(!env.isEmpty() && !env.equalsIgnoreCase("*") && !serviceName.contains("*")){
			logger.debug("No wildcard for env or serviceName found, looking at cache using key: {}" + env+serviceName);
			List<ServiceEndPoint> eps = (List<ServiceEndPoint>) ServiceRegistry.getInstance().getCache(GRMEdgeConstants.ENDPOINT_CACHE).get(env.toUpperCase()+serviceName);
			if(eps == null){
				eps = new ArrayList<>();
			}
			response.getServiceEndPointList().addAll(eps);
			return response;
		}
		
		//if we made it to here, we have a wildcard
		Set<Object> sepKeys = ServiceRegistry.getInstance().getCache(GRMEdgeConstants.ENDPOINT_CACHE).getKeySet();
		if(sepKeys.isEmpty()){
			logger.warn("List of Keys in Endpoint Cache is empty");
			return response;
		}
		
		for(Object o: sepKeys){
			String sepEnv = GRMEdgeUtil.extractEnvironment(o.toString());
			if(env.isEmpty() || env.equalsIgnoreCase("*") || (sepEnv!= null && sepEnv.equalsIgnoreCase(env))){
				List<ServiceEndPoint> seps = (List<ServiceEndPoint>) ServiceRegistry.getInstance().getCache(GRMEdgeConstants.ENDPOINT_CACHE).get(o);
				for(ServiceEndPoint sep: seps){
					String sepName = serviceName;
					if(sepName.contains("*")){
						sepName = sepName.substring(0, sepName.length()-1);
						if (sep.getName().startsWith(sepName)){
							response.getServiceEndPointList().add(sep);
						}
					}
					else{
						if(sep.getName().equals(sepName)){
							response.getServiceEndPointList().add(sep);
						}
					}
					
				}
			}
		}
		return response;
	}
	
	@Override
	public FindRunningServiceEndPointResponse findAllEps(String requestBodyJson){
		FindRunningServiceEndPointResponse response = new FindRunningServiceEndPointResponse();
		try {
			JSONObject root = new JSONObject(requestBodyJson);
			String name = root.getString("service");
			String env = root.getString("env");
			if (System.getProperty("GRM_EDGE_CONVERT_DASH_TO_DOT_ON_FIND_RUNNING","False").equalsIgnoreCase("True")){
				name = GRMEdgeUtil.convertSEPNamespaceToDots(name);
			}
			
			String key = K8ServiceController.getCacheKeySEP(name, env);
			logger.trace("Using cache key: {}" , key);
			List<ServiceEndPoint> seps = (List<ServiceEndPoint>) ServiceRegistry.getInstance().getCache(GRMEdgeConstants.ENDPOINT_CACHE).get(key);
			if(seps != null){
				response.getServiceEndPointList().addAll(seps);
			} 
		} catch (JSONException e) {
			logger.error(GRMEdgeErrorGenerator.generateErrorMessage(GRMEdgeConstants.JSON_PARSE_ERROR,e.getMessage()));
		}
		return response;
	}

	@Override
	public FindRunningServiceEndPointResponse findRunning(String requestBodyJSON) {
		logger.trace("call with parameter {}",  requestBodyJSON);
		FindRunningServiceEndPointResponse response = new FindRunningServiceEndPointResponse();
		try {
			//convert json to FindRunningServiceEndPointRequest
			FindRunningServiceEndPointRequest req = GRMEdgeUtil.convertToFindRunningRequest(requestBodyJSON);
			logger.trace("FindRunningServiceEndPointRequest {}" , req);

			req.setFindLevel(FindLevel.ALL);
			JSONObject root = new JSONObject(requestBodyJSON);
			JSONObject sepjson = root.getJSONObject("serviceEndPoint");
			JSONObject version = sepjson.getJSONObject("version");
			int major = version.getInt("major");
			int minor = -1;
			String patch = null;
			try{
				minor = version.getInt("minor");
				patch = version.getString("patch");
			}
			catch(JSONException e){
				
			}
			
			String name = req.getServiceEndPoint().getName();
			if (System.getProperty("GRM_EDGE_CONVERT_DASH_TO_DOT_ON_FIND_RUNNING","False").equalsIgnoreCase("True")){
				name = GRMEdgeUtil.convertSEPNamespaceToDots(name);
			}
			
			String key = K8ServiceController.getCacheKeySEP(name, req.getEnv());
			List<ServiceEndPoint> returnSeps = new ArrayList<>();

			if (req.getServiceEndPoint().getName().equalsIgnoreCase("*")) {
				//TODO look up how to throw rest exception
				//invalid servicename
				throw new HttpMessageNotReadableException(GRMEdgeErrorGenerator.generateErrorMessage(GRMEdgeConstants.JSON_PARSE_ERROR,"Error parsing serviceEndPoint name. It contains only a * which is not supported"));
			}
			
			if (req.getServiceEndPoint().getName().endsWith("*")) {
				Iterator<Object> iter = ServiceRegistry.getInstance().getCache(GRMEdgeConstants.ENDPOINT_CACHE).getKeySet().iterator();
				while (iter.hasNext()) {
					String cacheKey = iter.next().toString();
					if (cacheKey.startsWith(req.getEnv().toUpperCase()) && cacheKey.contains(name.substring(0, name.length() - 1))) {
						logger.trace("match found for cacheKey : {}", cacheKey);
						List<ServiceEndPoint> seps = (List<ServiceEndPoint>) ServiceRegistry.getInstance().getCache(GRMEdgeConstants.ENDPOINT_CACHE).get(cacheKey);
						if(seps == null){
							seps = new ArrayList<>();
						}
						//apply a version filter

						for(ServiceEndPoint sep: seps){
							if ((sep.getVersion().getMajor() == major)&&(minor < 0 || sep.getVersion().getMinor() == minor) && (StringUtils.isEmpty(patch) || sep.getVersion().getPatch().equals(patch))){
								returnSeps.add(sep);
							}
						}
					}
				}

			} else {
				logger.trace("Using cache key: {}" , key);
				boolean isNull = false;
				List<ServiceEndPoint> seps = (List<ServiceEndPoint>) ServiceRegistry.getInstance().getCache(GRMEdgeConstants.ENDPOINT_CACHE).get(key);
				if(seps == null){
					seps = new ArrayList<>();
					if ("True".equalsIgnoreCase(System.getProperty("CPFRUN_GRMEDGE_DB_WRITE_BEHIND_ENABLE","False"))){
						logger.debug("Loading data from CassandraDB");
						//make sure right type - should be db
						List<com.att.ocnp.mgmt.grm_edge_service.topology.domain.ServiceEndPoint> dbList = (List<com.att.ocnp.mgmt.grm_edge_service.topology.domain.ServiceEndPoint>) loadFromDB(key);
						List<ServiceEndPoint> newdbList = new ArrayList<>();
						if (dbList != null && !dbList.isEmpty()) {			
							Iterator<com.att.ocnp.mgmt.grm_edge_service.topology.domain.ServiceEndPoint> iter = dbList.iterator();
							while (iter.hasNext()) {
								com.att.ocnp.mgmt.grm_edge_service.topology.domain.ServiceEndPoint sep = iter.next();
								newdbList.add(convertDomainSEP(sep));
							}
						}
						if(!newdbList.isEmpty()){
							seps = newdbList;
						}
					}
					if((seps.isEmpty()) && "True".equalsIgnoreCase(System.getProperty("CPFRUN_GRMEDGE_GRM_WRITE_BEHIND_ENABLE","False"))){
						logger.debug("Loading data from GRM Layer");
						try {
							seps = (List<ServiceEndPoint>) loadFromGRM(key);
						} catch (Exception e) {
							logger.error("Received Error while loading data from GRM",e);
						}
					}
					if(seps == null){
						seps = new ArrayList<>();
					}
					ServiceRegistry.getInstance().getCache(GRMEdgeConstants.ENDPOINT_CACHE).addEndPoints(key, seps);
				}
				//apply a version filter
				for(ServiceEndPoint sep: seps){
					if ((sep.getVersion().getMajor() == major)&&(minor < 0 || sep.getVersion().getMinor() == minor) && (StringUtils.isEmpty(patch) || sep.getVersion().getPatch().equals(patch))){
						returnSeps.add(sep);
					}
				}
			}
			response.getServiceEndPointList().addAll(returnSeps);
		} catch (JSONException e) {
			logger.error(GRMEdgeErrorGenerator.generateErrorMessage(GRMEdgeConstants.JSON_PARSE_ERROR,e.getMessage()));
			//TODO look up how to throw rest exception
			throw new HttpMessageNotReadableException(GRMEdgeErrorGenerator.generateErrorMessage(GRMEdgeConstants.JSON_PARSE_ERROR,e.getMessage())); 
		}
//		logger.debug("Result: {}" , response.toString());
		return response;
	}

	private Object loadFromGRM(String key) throws Exception {
		List<ServiceEndPoint> seps = EdgeGRMHelper.getEndpointsWithKey(key);
		if(seps == null || seps.isEmpty()){
			return null;
		}
		return seps;
	}

	private Object loadFromDB(String key) {
		String env = GRMEdgeUtil.extractEnvironment(key);
		String serviceName = GRMEdgeUtil.extractServiceName(key, env);
		try{
			return DataAccessManager.getInstance().getServiceEndPointByEnvAndName(env, serviceName);
		}
		catch(EdgeException e){
			logger.error("EdgeException occured while loading from DB: ", e);
			return null;
		}
	}

	public String add(String requestBodyJson){
		String result = "";
		JSONObject root;
		try {
			root = new JSONObject(requestBodyJson);
			JSONObject jarray = root.getJSONObject("object");
			String kind = jarray.getString("kind");
			if(kind.equalsIgnoreCase("Pod")){
				logger.trace("Attempting to add pod");
				//convert to a pod!!!
				KubePod pod = GRMEdgeUtil.convertJSONObjectToKubePod(jarray);
				if (pod == null){
					result = GRMEdgeErrorGenerator.generateErrorMessage(GRMEdgeConstants.POD_NOT_CREATED);
				}
				else
					K8ServiceController.addPod(pod);
			}
			else if(kind.equalsIgnoreCase("Service")){
				logger.trace("Attempting to add service");
				K8Service k8Service = GRMEdgeUtil.convertJSONObjectToK8Service(jarray);
				if (k8Service == null){
					result = GRMEdgeErrorGenerator.generateErrorMessage(GRMEdgeConstants.K8SERVICES_NOT_CREATED);
				}
				else {
					K8Service existService = (K8Service) ServiceRegistry.getInstance().getCache(GRMEdgeConstants.SERVICE_CACHE).get(K8ServiceController.getCacheKeyK8(k8Service));
					if (existService != null) {
						K8ServiceController.modifyService(k8Service);
						result = "Found pods with same tag, updated ServiceEndPoint(s)";
					} else {
						logger.debug("K8Service protocol being added:" + k8Service.getProtocol());
						result = K8ServiceController.addServices(k8Service);		
					}
				}
			}
			else{
				result = GRMEdgeErrorGenerator.generateErrorMessage(GRMEdgeConstants.JSON_MISSING_KIND, "Pod,Service");
				logger.error(result);
			}
		} catch (JSONException e) {
			result = GRMEdgeErrorGenerator.generateErrorMessage(GRMEdgeConstants.JSON_PARSE_ERROR,e.getMessage());
			logger.error(result,e);
		}
		logger.debug("Result: " + result);

		return result;
	}
	
	public String delete(String requestBodyJson){
		String result = "Completed Successfully";
		JSONObject root;
		try {
			root = new JSONObject(requestBodyJson);
			JSONObject jarray = root.getJSONObject("object");
			String kind = jarray.getString("kind");
			if(kind.equalsIgnoreCase("Pod")){
				logger.trace("Attempting to delete pod");
				//convert to a pod!!!
				KubePod pod = GRMEdgeUtil.convertJSONObjectToKubePod(jarray);
				if (pod == null){
					result = "ERROR: Could not delete pod, unable to parse the request JSON";
				}
				else
					K8ServiceController.deletePods(pod);
			}
			else if(kind.equalsIgnoreCase("Service")){
				logger.trace("Attempting to delete service");
				K8Service k8Service = GRMEdgeUtil.convertJSONObjectToK8Service(jarray);

				if (k8Service == null){
					result = "ERROR: Could not delete service, unable to parse the request JSON";
				}
				else
					K8ServiceController.deleteServices(k8Service);
			}
			else{
				result = GRMEdgeErrorGenerator.generateErrorMessage(GRMEdgeConstants.JSON_MISSING_KIND, "Pod,Service");
				logger.error(result);
			}
		} catch (JSONException e) {
			result = GRMEdgeErrorGenerator.generateErrorMessage(GRMEdgeConstants.JSON_PARSE_ERROR,e.getMessage());
			logger.error(result,e);
		}
		logger.debug("Result: " + result);
		return result;
	}
	private com.att.scld.grm.types.v1.ServiceEndPoint convertDomainSEP(com.att.ocnp.mgmt.grm_edge_service.topology.domain.ServiceEndPoint sep) {
		com.att.scld.grm.types.v1.ServiceEndPoint retSep = new com.att.scld.grm.types.v1.ServiceEndPoint();
		retSep.setName(sep.getName());
		retSep.setClientSupportedVersions(sep.getClientSupportedVersions());
		retSep.setContainerVersionDefinitionName(sep.getContainerVersionName());
		retSep.setContextPath(sep.getContextPath());
		retSep.setDME2JDBCDatabaseName(sep.getDme2JDBCDatabaseName());
		retSep.setDME2JDBCHealthCheckDriver(sep.getDme2JDBCHealthCheckDriver());
		retSep.setDME2JDBCHealthCheckPassword(sep.getDme2JDBCHealthCheckPassword());
		retSep.setDME2JDBCHealthCheckUser(sep.getDme2JDBCHealthCheckUser());
		retSep.setDME2Version(sep.getDme2Version());
		retSep.setExpirationTime(sep.getExpirationTime());
		retSep.setHostAddress(sep.getHostAddress());
		retSep.setLatitude(sep.getLatitude());
		retSep.setLongitude(sep.getLongitude());
		retSep.setListenPort(sep.getListenPort());
		retSep.setProtocol(sep.getProtocol());
		retSep.setRegistrationTime(sep.getRegistrationTime());
		retSep.setRouteOffer(sep.getRouteOffer());
		StatusInfo si = new StatusInfo();
		si.setStatus(sep.getStatus());
		retSep.setStatusInfo(si);
		VersionDefinition vd = new VersionDefinition();
		vd.setMajor(sep.getMajorVersion());
		vd.setMinor(sep.getMinorVersion());
		vd.setPatch(sep.getPatchVersion());
		retSep.setVersion(vd);
		OperationalInfo oi = new OperationalInfo();
		oi.setUpdatedBy(sep.getUpdatedBy());
		oi.setUpdatedTimestamp(sep.getUpdatedTimestamp());
		oi.setCreatedBy(sep.getCreatedBy());
		oi.setCreatedTimestamp(sep.getCreatedTimestamp());
		retSep.setOperationalInfo(oi);
		return retSep;
	}

	
}
