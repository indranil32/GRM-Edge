/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.ocnp.mgmt.grm_edge_service.service.rs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import com.att.ocnp.mgmt.grm_edge_service.cache.RouteInfoCache;
import com.att.ocnp.mgmt.grm_edge_service.topology.data.DbManager;
import com.att.ocnp.mgmt.grm_edge_service.util.GRMEdgeConstants;
import com.att.ocnp.mgmt.grm_edge_service.util.GRMEdgeErrorGenerator;
import com.att.ocnp.mgmt.grm_edge_service.util.GRMRouteInfoUtil;
import com.att.scld.grm.v1.GetRouteInfoRequest;
import com.att.scld.grm.v1.GetRouteInfoResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Controller
@Path("/routeInfo")
public class RouteInfoServiceImpl implements RouteInfoService {
	
	private static final Logger logger = LoggerFactory.getLogger(RouteInfoServiceImpl.class.getName());
	private static final Gson gson = new Gson();
	private static final GRMRouteInfoUtil grmRouteInfoUtil = new GRMRouteInfoUtil();
	
	@Override
	public GetRouteInfoResponse getRouteInfo(String requestBodyJSON) {
		
		long start = System.currentTimeMillis();
		
		/****************************************************
		 * Record request before starting any operations	*
		 ****************************************************/
		logger.trace("start {}",  requestBodyJSON);
		String resultXML = null;
		String resultXMLFilteredByVersion = null;
		GetRouteInfoResponse response = new GetRouteInfoResponse();
		try {
			
			/****************************************************
			 * Convert json to GetRouteInfoRequest				*
			 ****************************************************/
			GetRouteInfoRequest req = gson.fromJson(requestBodyJSON, GetRouteInfoRequest.class);
			logger.trace("GetRouteInfoRequest {}" , req);
			
			String serviceName = req.getServiceVersionDefinition().getName();
			if (serviceName == null) {
				logger.error(GRMEdgeErrorGenerator.generateErrorMessage(GRMEdgeConstants.GETROUTEINFO_INVALID_SERVICE_NAME, serviceName));
			}
			
			/****************************************************
			 * Construct List of Names to Check	from Hierarchy	*
			 ****************************************************/
			List<String> serviceNamesToCheck = new ArrayList<>();
			serviceNamesToCheck.add("root.namespace");
			serviceNamesToCheck.addAll(grmRouteInfoUtil.getDomainHierarchy(serviceName));
			serviceNamesToCheck.add(serviceName);
			Collections.reverse(serviceNamesToCheck);
			
			for (String nameToCheck : serviceNamesToCheck) {
				if (resultXML != null) {
					break;
				} else {
					/****************************************************
					 * Step 1 - Check in Cache							*
					 ****************************************************/
					logger.trace("Checking in Cache");
					if (RouteInfoCache.getInstance().getCache().get(nameToCheck) != null) {
						resultXML = (String)RouteInfoCache.getInstance().getCache().get(nameToCheck);
						if (resultXML != null) {
							logger.debug("Found routeinfo xml in cache with service: " , nameToCheck);
							break;
						}
					}
					
					/****************************************************
					 * Step 2 - Check from ConfigMap again				*
					 * Only on the first try							*
					 ****************************************************/
					if (resultXML == null && nameToCheck.equalsIgnoreCase(serviceName)) {
						logger.trace("Refreshing cache and check again"); //TODO not refreshing the cache. need to fix this
						if (RouteInfoCache.getInstance().getCache().get(nameToCheck) != null) {
							resultXML = (String)RouteInfoCache.getInstance().getCache().get(nameToCheck);
							if (resultXML != null) {
								logger.trace("Found routeinfo xml after cache refresh with service: {}" , nameToCheck);
								break;
							}
						}
					}
					
					/****************************************************
					 * Step 3 - Check from Cassandra					*
					 ****************************************************/
					if (resultXML == null && Boolean.parseBoolean(System.getProperty("PLATFORM_RUNTIME_ROUTEINFO_XML_CHECK_FROM_CASSANDRA", "false"))) {/*
						logger.trace("Checking from Cassandra");
						try {
							resultXML = DbManager.getInstance().getEffectiveRouteXML(req.getEnv(), nameToCheck);
							if (resultXML != null) {
								logger.trace("Found routeinfo xml from Cassandra with service: {}" , nameToCheck);
								RouteInfoCache.getInstance().getCache().put(nameToCheck, resultXML);
								break;
							}
						} catch (Exception e) {
							logger.warn("Unable to retrieve info from Cassandra",e);
							resultXML = null;
						}
					*/}
					
					/****************************************************
					 * Step 4 - Check from GRM-Edge						*
					 ****************************************************/
					if (resultXML == null && Boolean.parseBoolean(System.getProperty("PLATFORM_RUNTIME_ROUTEINFO_XML_CHECK_FROM_GRM_EDGE", "false"))) {
						try {
							logger.trace("Checking from GRM-Edge");
							resultXML = GRMRouteInfoUtil.getRouteInfoFromGRMEdge(nameToCheck, req.getServiceVersionDefinition().getVersion(), req.getEnv());
							if (resultXML != null) {
								logger.trace("Found routeinfo xml with GRM-Edge with service: {}" , nameToCheck);
								RouteInfoCache.getInstance().getCache().put(nameToCheck, resultXML);
								break;
							}
						} catch (Exception e) {
							logger.warn("Unable to retrieve info from GRM-Edge",e);
							resultXML = null;
						}
					}
				}
			}
			
			if (resultXML != null) {
				resultXMLFilteredByVersion = grmRouteInfoUtil.filterRouteXMLbyVersionSelector(resultXML, req.getServiceVersionDefinition());
			} else {
				resultXMLFilteredByVersion = System.getProperty(
						"null",
						"null"
						);
			}
			
		} catch (Exception e) {
			logger.error(GRMEdgeErrorGenerator.generateErrorMessage(GRMEdgeConstants.GETROUTEINFO_PROCESS_ERROR, e.getMessage()));
		}
		
		logger.trace("Finding RouteInfo XML: {}" ,  resultXMLFilteredByVersion);
		response.setRouteInfoXml(resultXMLFilteredByVersion);
		
		logger.trace("Returning response Routeinfo XML in {} ms.",  (System.currentTimeMillis() - start) );
			
		return response;
	}

	@Override
	public String getAllRouteInfo() {
		Gson gsonPrettyPrinting = new GsonBuilder().setPrettyPrinting().create();
		
		try {
			return gsonPrettyPrinting.toJson(RouteInfoCache.getInstance().getCache());
		} catch (Exception e) {
			logger.error("Error in getAllRouteInfo.",e);
		}
		
		return "Error in retriving all routeinfo xmls";
	}
}