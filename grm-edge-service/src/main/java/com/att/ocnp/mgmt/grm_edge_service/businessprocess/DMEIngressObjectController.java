/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.ocnp.mgmt.grm_edge_service.businessprocess;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.att.ocnp.mgmt.grm_edge_service.util.GRMEdgeUtil;
import com.att.scld.grm.types.v1.DMEIngressMappingObject;
import com.att.scld.grm.v1.DeregisterDMEIngressRequest;
import com.att.scld.grm.v1.DeregisterDMEIngressResponse;
import com.att.scld.grm.v1.RegisterDMEIngressRequest;
import com.att.scld.grm.v1.RegisterDMEIngressResponse;
import com.google.common.base.Strings;

public class DMEIngressObjectController {
	
	private static final Logger logger = LoggerFactory.getLogger(DMEIngressObjectController.class.getName());
	
	private static final int retryAttempts = Integer.parseInt(System.getProperty("PLATFORM_RUNTIME_EDGECORE_GRM_RETRY","2"));
	
	public void addIngresses(List<DMEIngressMappingObject> ingresses) {
		
		if (Strings.isNullOrEmpty(GRMEdgeUtil.getEnvDefault())) {
			logger.error("CRITICAL ERROR. PLEASE NOTIFY SOMEONE ON PLATFORM RUNTIME IMMEDIATELY. Not able to retrieve default env for this GRM-Edge instance");
			return;
		}
		
		boolean goodResponse = true;
		
		int statusCode = -1;
		int attempts = 0;
		
		while(statusCode != 200 && attempts < retryAttempts){
			try {
				RegisterDMEIngressRequest req = new RegisterDMEIngressRequest();
				req.setEnv(GRMEdgeUtil.getEnvDefault());
				req.getDmeIngressMappingObjectList().addAll(ingresses);
				RegisterDMEIngressResponse resp = EdgeGRMHelper.registerDMEIngress(req);
				
				if(resp == null){
					goodResponse = false;
					logger.warn("Received a null response for RegisterDMEIngress response! Check GRM Rest for errors. Request sent: " + req.toString());
				}
				else{
					logger.info("RegisterDMEIngress response from GRM: " + resp.toString());
				}
				
				if(goodResponse){
					logger.info("Completed GRM RegisterDMEIngress");
					statusCode = 200;
					break;
				}
				else{
					statusCode = -1;
					attempts++;
				}
			} catch(Exception e){
				logger.error("Error occured during attempt number: "+ attempts +" to call GRM");
				e.printStackTrace();
				statusCode = -1;
				attempts++;
			}
		}
		
		if (attempts >  retryAttempts){
			logger.error("CRITICAL ERROR. PLEASE NOTIFY SOMEONE ON PLATFORM RUNTIME IMMEDIATELY. Not able to register DME Ingress with grmedge");
		}
	}
	
	public void deleteIngresses(List<DMEIngressMappingObject> ingresses) {
		
		if (Strings.isNullOrEmpty(GRMEdgeUtil.getEnvDefault())) {
			logger.error("CRITICAL ERROR. PLEASE NOTIFY SOMEONE ON PLATFORM RUNTIME IMMEDIATELY. Not able to retrieve default env for this GRM-Edge instance");
			return;
		}
		
		boolean goodResponse = true;
		
		int statusCode = -1;
		int attempts = 0;
		
		while(statusCode != 200 && attempts < retryAttempts){
			try {
				DeregisterDMEIngressRequest req = new DeregisterDMEIngressRequest();
				req.setEnv(GRMEdgeUtil.getEnvDefault());
				req.getDmeIngressMappingObjectList().addAll(ingresses);
				DeregisterDMEIngressResponse resp = EdgeGRMHelper.deregisterDMEIngress(req);
				
				if(resp == null){
					goodResponse = false;
					logger.warn("Received a null response for DeregisterDMEIngress response! Check GRM Rest for errors. Request sent: " + req.toString());
				}
				else{
					logger.info("DeregisterDMEIngress response from GRM: " + resp.toString());
				}
				
				if(goodResponse){
					logger.info("Completed GRM DeregisterDMEIngress");
					statusCode = 200;
					break;
				}
				else{
					statusCode = -1;
					attempts++;
				}
			} catch(Exception e){
				logger.error("Error occured during attempt number: "+ attempts +" to call GRM");
				e.printStackTrace();
				statusCode = -1;
				attempts++;
			}
		}
		
		if (attempts >  retryAttempts){
			logger.error("CRITICAL ERROR. PLEASE NOTIFY SOMEONE ON PLATFORM RUNTIME IMMEDIATELY. Not able to deregister DME Ingress with grmedge");
		}
	}
}