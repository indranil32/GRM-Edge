/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.ocnp.mgmt.grm_edge_service.mapstore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.att.ocnp.mgmt.grm_edge_service.businessprocess.EdgeGRMHelper;
import com.att.ocnp.mgmt.grm_edge_service.cache.ServiceRegistry;
import com.att.ocnp.mgmt.grm_edge_service.util.GRMEdgeConstants;
import com.att.ocnp.mgmt.grm_edge_service.util.GRMEdgeUtil;
import com.att.scld.grm.types.v1.ServiceEndPoint;
import com.att.scld.grm.v1.AddServiceEndPointRequest;
import com.att.scld.grm.v1.AddServiceEndPointResponse;
import com.att.scld.grm.v1.DeleteServiceEndPointRequest;
import com.att.scld.grm.v1.DeleteServiceEndPointResponse;
import com.att.scld.grm.v1.UpdateServiceEndPointRequest;
import com.att.scld.grm.v1.UpdateServiceEndPointResponse;

/*
 * A class that handles write behinds to GRM. 
 * It is called in hazelcasts write behind by the EdgeMapStoreWriteController. It only works for the service endpoint cache (since that is what we are sending to grm.
 * We can assume that this is the master copy of the data and we are simply updating grm with the results.
 */
public class EdgeMapStoreGRMWriter {

	private static final Logger logger = LoggerFactory.getLogger(EdgeMapStoreGRMWriter.class);
	
	private int retryAttempts = Integer.parseInt(System.getProperty("PLATFORM_RUNTIME_EDGECORE_GRM_RETRY","2"));
	
	public void send(Object key, Object value){
		MDC.put(GRMEdgeConstants.Tracking, UUID.randomUUID().toString());
		ArrayList<ServiceEndPoint> oldSeps = (ArrayList<ServiceEndPoint>) ServiceRegistry.getInstance().getCache(GRMEdgeConstants.OLD_ENDPOINT_CACHE).get(key);
		if(oldSeps == null){
			oldSeps = new ArrayList<>();
		}
		ArrayList<ServiceEndPoint> newSeps = (ArrayList<ServiceEndPoint>) value;
		
		String env = EdgeGRMHelper.extractEnvironment(key.toString());
		String serviceName = EdgeGRMHelper.extractServiceName(key.toString(), env);
		boolean goodResponse = true;
		
		int statusCode = -1;
		int attempts = 0;
		
		if(StringUtils.isEmpty(env) || StringUtils.isEmpty(serviceName)){
			//TODO SERIOUS ERROR
			logger.error("CRITICAL ERROR. Please contact Platform Runtime support. Not able to retrieve env or servicename from the object key: {}", key.toString());
			return;
		}

		
		while(statusCode != 200 && attempts < retryAttempts){
			try{				
				logger.trace("Checking to see if GRM add endpoint required");
				for (ServiceEndPoint sep: newSeps){
					if(GRMEdgeUtil.sepInCurrentCluster(sep)){
						boolean notFound = true;
						for(ServiceEndPoint sep2:oldSeps){
							if(GRMEdgeUtil.checkEqualServiceEndPointPK(sep, sep2)){
								notFound = false;
								if(!GRMEdgeUtil.checkEqualServiceEndPoints(sep, sep2)){
									//Need to update SEPs if details other than pk values changed
									UpdateServiceEndPointRequest update = new UpdateServiceEndPointRequest();
									update.setEnv(env);
									update.setServiceEndPoint(sep);
									UpdateServiceEndPointResponse updateResp = EdgeGRMHelper.updateGRMEndPoint(update);
									if(updateResp == null){
										goodResponse = false;
										logger.warn("Received a null response for Update endpoint response! Check GRM Rest for errors. Request sent: {}" , update.toString());
									}
									else{
										logger.trace("Update endpoint response from GRM: {}", updateResp.toString());
									}
								}
								break;
							}
						}
						
						if(notFound){
							//need to add it
							AddServiceEndPointRequest add = new AddServiceEndPointRequest();
							add.setServiceEndPoint(sep);
							add.setEnv(env);
							add.setCheckNcreateParents(true);
			
							AddServiceEndPointResponse addResp = EdgeGRMHelper.addGRMEndPoint(add);
							if(addResp == null){
								goodResponse = false;
								logger.warn("Recieved a null response for Add endpoint response! Check GRM REST for errors.  Request sent: {}" , add.toString());
							}
							else{
								logger.trace("Add endpoint response from grm: {}" , addResp.toString());	
							}
						}
					}	
				}
				
				DeleteServiceEndPointRequest delete = new DeleteServiceEndPointRequest();
				logger.trace("Checking to see if GRM delete endpoint required");
				for(ServiceEndPoint sep: oldSeps){
					boolean notFound = true;
					for(ServiceEndPoint sep2: newSeps){
						if(GRMEdgeUtil.checkEqualServiceEndPointPK(sep, sep2)){
							notFound = false;
							break;
						}
					}
					if(notFound){
						//need to delete the sep
						delete.getServiceEndPoint().add(sep);
						delete.setEnv(env);
						//do I need to add anything else
						logger.trace("Adding endpoint to delete");
					}
				}
				if(!delete.getServiceEndPoint().isEmpty()){
					DeleteServiceEndPointResponse deleteResp = EdgeGRMHelper.grmDeleteSEP(delete);		
				
					if(deleteResp == null){
						goodResponse = false;
						logger.warn("Recieved a null response for delete endpoint response! Check GRM REST for errors. Request sent: {}" , delete.toString());
					}
					else{
						logger.trace("Delete endpoint response from grm: {}" , deleteResp.toString());
					}
				}
				
				if(goodResponse){
					logger.info("Completed GRM Update");
					break;
				}
				else{
					statusCode = -1;
					attempts++;
				}
			}	
			catch(Exception e){
				String errorMessage = "Error occured during attempt number: "+attempts+" to call GRM";
				logger.error(errorMessage,e);
				statusCode = -1;
				attempts++;
			}
		}
		if (attempts >  retryAttempts){
			//TODO needs to raise an alarm
			logger.error("CRITICAL ERROR.Please contact Platform Runtime support. Not able to update GRM with grmedge");
		}
		else{
			ServiceRegistry.getInstance().getCache(GRMEdgeConstants.OLD_ENDPOINT_CACHE).remove(key);
			ServiceRegistry.getInstance().getCache(GRMEdgeConstants.OLD_ENDPOINT_CACHE).put(key, value);
		}
		MDC.remove(GRMEdgeConstants.Tracking);
	}

	public void sendAll(Map<Object, Object> map) {
		for(Object j: map.keySet()){
			send(j, map.get(j));
		}
	}
	
	public void delete(Object key) {
		send(key, new ArrayList<ServiceEndPoint>());
	}

	public void deleteAll(Collection<Object> keys) {
		for(Object j: keys){
			delete(j);
		}
		
	}
}
