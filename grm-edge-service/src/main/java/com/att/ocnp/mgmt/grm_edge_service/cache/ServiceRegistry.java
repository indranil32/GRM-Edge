/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.ocnp.mgmt.grm_edge_service.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.att.ocnp.mgmt.grm_edge_service.util.Configuration;
/**
 * A singleton instance of Registry houses the Request and Response cache and the stale endpoint cache for a jvm instance.
 * Initialization involves cache init and the registration of a shutdown hook as well.
 */

public class ServiceRegistry {
	private static ServiceRegistry instance = null;
	private static EdgeCache DME2Cache = null;
	private static Configuration config = null;
	private static final Logger logger = LoggerFactory.getLogger(ServiceRegistry.class);
	private static EdgeCacheManager cacheManager = null;

	private ServiceRegistry() {
		if (this.config == null)
			this.config = Configuration.getBootStrapConfig();
		init();		
	}

	public static ServiceRegistry getInstance() {
			if (instance == null) {
				synchronized (ServiceRegistry.class) {
					instance = new ServiceRegistry();
					instance.addShutDownHook();
				}
			}

		return instance;
	}

	private void init() {
		cacheManager = EdgeCacheManager.getInstance();
		// create the cache to hold the request response
		if (DME2Cache == null)
			DME2Cache = cacheManager.getCache("GRMSERVICE_SERVICEENDPOINT_CACHE");
		logger.debug("Cache manager and caches initialized.");
	}
	
    public void addShutDownHook() {
        try {
            Thread shutdownHook = new Thread("edge-shutdown-hook") {
                @Override
                public void run() {
                    stopProcessing();
                }
            };
            Runtime.getRuntime().addShutdownHook(shutdownHook);
            logger.trace("Code=Server.StartUp; Shutdown hook added");
        } catch (Exception e) {
            logger.error("Code=Server.StartUp; Shutdown hook exception: ", e);
        }
    }
    
    public synchronized void stopProcessing() {
        try {
        	EdgeCacheManager.shutdown();
        } catch (Exception e) {
        	logger.error("Error shutting down hazelcast.",e);
        }
    }
    
    public EdgeCache getCache(String cacheName) {
    	return cacheManager.getCache(cacheName);
    }

}
