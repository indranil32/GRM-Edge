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

import com.att.ocnp.mgmt.grm_edge_service.cache.ServiceRegistry;
import com.att.ocnp.mgmt.grm_edge_service.topology.data.DataAccessManager;
import com.att.ocnp.mgmt.grm_edge_service.util.GRMEdgeConstants;
import com.att.ocnp.mgmt.grm_edge_service.util.GRMEdgeUtil;
import com.att.scld.grm.types.v1.ServiceEndPoint;
import com.att.scld.grm.v1.AddServiceEndPointRequest;
import com.att.scld.grm.v1.DeleteServiceEndPointRequest;

public class EdgeMapStoreDbWriter extends EdgeMapStoreBaseWriter  {
	private static final Logger logger = LoggerFactory.getLogger(EdgeMapStoreDbWriter.class);
	private int retryAttempts = Integer.parseInt(System.getProperty("PLATFORM_RUNTIME_EDGECORE_DB_RETRY","0"));
	private static String SUCCESS = "Successful";
	private DataAccessManager dataAccessManager = null;
	
	public EdgeMapStoreDbWriter(String mapName) {
	 	dataAccessManager = DataAccessManager.getInstance();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void send(Object key, Object value){
		MDC.put(GRMEdgeConstants.Tracking, UUID.randomUUID().toString());
		ArrayList<ServiceEndPoint> oldSeps = (ArrayList<ServiceEndPoint>) ServiceRegistry.getInstance().getCache(GRMEdgeConstants.OLD_ENDPOINT_CASS_CACHE).get(key);

		if(oldSeps == null){
			oldSeps = new ArrayList<>();
		}
		ArrayList<ServiceEndPoint> newSeps = (ArrayList<ServiceEndPoint>) value;
		
		String env = GRMEdgeUtil.extractEnvironment(key.toString());
		String serviceName = GRMEdgeUtil.extractServiceName(key.toString(), env);
		boolean goodResponse = true;
		
		int statusCode = -1;
		int attempts = 0;
		
		if(StringUtils.isEmpty(env) || StringUtils.isEmpty(serviceName)){
			//TODO SERIOUS ERROR
			logger.error("CRITICAL ERROR. Please contact Platform Runtime support. Not able to retrieve env or servicename from the object key: {}", key.toString());
			return;
		}
		
		while(statusCode != 200 && attempts <= retryAttempts){
			try{				
				logger.trace("Checking to see if DB add endpoint required");
				for (ServiceEndPoint sep: newSeps){
					if(GRMEdgeUtil.sepInCurrentCluster(sep)){
						boolean notFound = true;
						for(ServiceEndPoint sep2:oldSeps){
							if(GRMEdgeUtil.checkEqualServiceEndPointPK(sep, sep2)){
								notFound = false;
								if(!GRMEdgeUtil.checkEqualServiceEndPoints(sep, sep2)){
									//need to add it
									AddServiceEndPointRequest add = new AddServiceEndPointRequest();
									add.setServiceEndPoint(sep);
									add.setEnv(env);
									add.setCheckNcreateParents(true);
					
									String addResp = addEndPoint(add);
									if(addResp == null){
										goodResponse = false;
									} else{
										logger.trace("Add endpoint response from db: {}" , addResp.toString());	
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
			
							String addResp = addEndPoint(add);
							if(addResp == null){
								goodResponse = false;
							} else{
								logger.trace("Add endpoint response from db: {}" , addResp.toString());	
							}
						}
					}
				}

				
				DeleteServiceEndPointRequest delete = new DeleteServiceEndPointRequest();
				logger.trace("Checking to see if DB delete endpoint required");
				for(ServiceEndPoint sep: oldSeps){
					if(GRMEdgeUtil.sepInCurrentCluster(sep)){
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
							logger.trace("Adding endpoint to delete");
						}
					}
				}
				if(!delete.getServiceEndPoint().isEmpty()){
					String deleteResp = deleteSEP(delete);		
					
					if(deleteResp == null){
						goodResponse = false;
					} else{
						logger.trace("Delete endpoint response from db: {}" , deleteResp.toString());
					}

				}

				if(goodResponse){
					logger.trace("Completed DB Update");
					break;
				}
				else{
					statusCode = -1;
					attempts++;
				}
			}	
			catch(Exception e){
				String errorMessage = "Error occured during attempt number: "+attempts+" to call DB";
				logger.error(errorMessage,e);
				statusCode = -1;
				attempts++;
			}
		}
		if (attempts >  retryAttempts){
			//TODO needs to raise an alarm
			logger.error("CRITICAL ERROR.  Please contact Platform Runtime support. Not able to update DB with grmedge");
		}
		else{
			ServiceRegistry.getInstance().getCache(GRMEdgeConstants.OLD_ENDPOINT_CASS_CACHE).remove(key);
			ServiceRegistry.getInstance().getCache(GRMEdgeConstants.OLD_ENDPOINT_CASS_CACHE).put(key, value);
		}
		MDC.remove(GRMEdgeConstants.Tracking);
	}

	@Override
	public void sendAll(Map<Object, Object> map) {
		for(Object j: map.keySet()){
			send(j, map.get(j));
		}
	}
	
	private String addEndPoint(AddServiceEndPointRequest add) {
		try {
			dataAccessManager.addOrUpdateServiceEndPoint(add);	
			return SUCCESS;
		} catch (Exception e) {
			logger.error("Error while adding service endpoint:" + add.getServiceEndPoint().getName(), e);
		}
		return null;
	}

	private String deleteSEP(DeleteServiceEndPointRequest delete) {
		try {
			dataAccessManager.deleteServiceEndPoint(delete);;	
			return SUCCESS;
		} catch (Exception e) {
			if ((delete.getServiceEndPoint() != null) && (!delete.getServiceEndPoint().isEmpty()))
				logger.error("Error while deleting service endpoint:" + delete.getServiceEndPoint().get(0).getName(), e);
			else 
				logger.error("Error while deleting service endpoint",e);
		}
		return null;
	}

	@Override
	public void delete(Object key) {
		send(key, new ArrayList<ServiceEndPoint>());
	}

	@Override
	public void deleteAll(Collection<Object> keys) {
		for(Object j: keys){
			delete(j);
		}
		
	}	
}
