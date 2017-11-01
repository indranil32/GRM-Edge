/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.ocnp.mgmt.grm_edge_service.mapstore;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.MapLoader;
import com.hazelcast.core.MapStoreFactory;

public class EdgeMapStoreFactory implements MapStoreFactory<Object, Object>
{
	private static final Logger logger = LoggerFactory.getLogger(EdgeMapStoreFactory.class);
	
    @Override
    public MapLoader<Object, Object> newMapStore(String mapName, Properties properties) {
    	logger.trace("Creating mapstore with name: {}",mapName);
        return new EdgeMapStoreWriteController(mapName);
    }
}

