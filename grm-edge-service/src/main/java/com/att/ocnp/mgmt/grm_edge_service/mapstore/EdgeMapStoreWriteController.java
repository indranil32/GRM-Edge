/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.ocnp.mgmt.grm_edge_service.mapstore;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import org.jboss.logging.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.att.ocnp.mgmt.grm_edge_service.util.GRMEdgeConstants;
import com.hazelcast.core.MapLoader;
import com.hazelcast.core.MapStore;

public class EdgeMapStoreWriteController implements MapLoader<Object, Object>, MapStore<Object, Object>{
	
	private static final Logger logger = LoggerFactory.getLogger(EdgeMapStoreWriteController.class);
	
	private String mapName = "default";
	private EdgeMapStoreGRMWriter grmWriter;
	private boolean grmCall = false;
	private EdgeMapStoreBaseWriter dbWriter;
	private boolean dbCall = false;
	
	public EdgeMapStoreWriteController(String mapName){
		this.mapName = mapName;
	    logger.trace("mapName : {}", mapName);
		
		//determine if we need to use grm call. It only works with GRMSERVICE_SERVICEENDPOINT_CACHE
	    
		if (mapName.equals(GRMEdgeConstants.ENDPOINT_CACHE) && "True".equalsIgnoreCase(System.getProperty("CPFRUN_GRMEDGE_GRM_WRITE_BEHIND_ENABLE","False"))){
			logger.trace("GRM Write Behind enabled for mapname: {}", mapName);
			grmCall = true;
			grmWriter = new EdgeMapStoreGRMWriter();
		}  
		
		if (mapName.equals(GRMEdgeConstants.ENDPOINT_CACHE) && "True".equalsIgnoreCase(System.getProperty("CPFRUN_GRMEDGE_DB_WRITE_BEHIND_ENABLE","False"))){
			logger.trace("DB Write Behind enabled for mapname : {}", mapName);
			dbCall = true;
			dbWriter = new EdgeMapStoreDbWriter(mapName); //TODO fix constructor;
		}
	}
	
	@Override
	public void store(Object key, Object value) {
		MDC.put(GRMEdgeConstants.Tracking, UUID.randomUUID().toString());
		logger.trace("Store called for: {}", mapName);
		if (grmCall){
			grmWriter.send(key, value);
		}
		if (dbCall){
			dbWriter.send(key, value);
		}
		MDC.remove(GRMEdgeConstants.Tracking);
	}

	@Override
	public void storeAll(Map<Object, Object> map) {
		logger.trace("StoreAll called for: {}", mapName);
		if (grmCall){
			grmWriter.sendAll(map);
		}
		if (dbCall){
			dbWriter.sendAll(map);
		}

	}

	@Override
	public void delete(Object key) {
		MDC.put(GRMEdgeConstants.Tracking, UUID.randomUUID().toString());
		logger.trace("Delete called for: {}", mapName);
		if(grmCall){
			grmWriter.delete(key);
		}

		if(dbCall){
			dbWriter.delete(key);
		}
		MDC.remove(GRMEdgeConstants.Tracking);
	}

	@Override
	public void deleteAll(Collection<Object> keys) {
		logger.trace("Delete All called for: {}", mapName);
		if(grmCall){
			grmWriter.deleteAll(keys);
		}
		if(dbCall){
			dbWriter.deleteAll(keys);
		}
	}

	@Override
	public Object load(Object key) {
		//we moved load logic to findRunning call
		logger.trace("Load called for: {}", mapName);
		return null;
	}

	@Override
	public Map<Object, Object> loadAll(Collection<Object> keys) {
		logger.trace("Load All called for: {}",mapName);
		return null;
	}

	@Override
	public Iterable<Object> loadAllKeys() {
		logger.trace("Load All Keys called for: {}", mapName);
		return null;
	}
	
}
