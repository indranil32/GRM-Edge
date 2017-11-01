/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.ocnp.mgmt.grm_edge_service.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.att.ocnp.mgmt.grm_edge_service.util.GRMEdgeConstants;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

public class RouteInfoCache {
	
	private static final Logger logger = LoggerFactory.getLogger(RouteInfoCache.class.getName());

	private static final String routexmlConfigMapEndingString = System.getProperty("PLATFORM_RUNTIME_ROUTEINFO_XML_ENDING_STRING", ".dmeroutexml");
	
	private static final ScheduledExecutorService scheduledRefreshService = Executors.newScheduledThreadPool(1);
	
	private static EdgeCacheManager cacheManager = null;
	
	private static List<String> apiServerUrls = null;
	
	private static String goodURL = null;
	
	private static RouteInfoCache INSTANCE = null;
	
	private static ScheduledFuture scheduledFuture = null;
	
	private EdgeCache CACHE = null;
	
	private RouteInfoCache(List<String> urls) {
		
		logger.trace("Initializing RouteInfoCache");
		
		cacheManager = EdgeCacheManager.getInstance();
		
		CACHE = cacheManager.getCache(GRMEdgeConstants.ROUTEINFO_XML_CACHE);
		
		apiServerUrls = urls;
		
		scheduledFuture = scheduledRefreshService.scheduleAtFixedRate(new Runnable() {
	        public void run() {
	        	try {
	        		logger.trace("Starting refresh of routeinfo xml cache.");
					refreshCacheFromConfigMap();
				} catch (Exception e) {
					logger.error("Cannot load routeinfo xml to internal cache during periodic refresh", e);
				}
	        }
		}
		, Integer.parseInt(System.getProperty("PLATFORM_RUNTIME_ROUTEINFO_XML_PERIODIC_REFRESH_INITIAL_DELAY", "5"))
		, Integer.parseInt(System.getProperty("PLATFORM_RUNTIME_ROUTEINFO_XML_PERIODIC_REFRESH_DELAY_BETWEEN_REFRESH", "5"))
		, TimeUnit.MINUTES);
		
		logger.trace("Initialized RouteInfoCache");
	}
	
	public static RouteInfoCache getInstance(List<String> urls) {
		
		logger.trace("Calling getInstance in RouteInfoCache");
		if (INSTANCE == null) {
			synchronized (RouteInfoCache.class) {
				INSTANCE = new RouteInfoCache(urls);
			}
		}
		return INSTANCE;
	}
	
	public static RouteInfoCache getInstance() {
		
		logger.trace("Calling getInstance in RouteInfoCache");
		
		return INSTANCE;
	}
	
	public static ScheduledExecutorService getRefreshService() {
		
		logger.trace("Calling getRefreshService in RouteInfoCache");
		
		return scheduledRefreshService;
	}
	
	public static EdgeCache getCache() throws Exception {
		
		logger.trace("Calling getCache in RouteInfoCache");
		
		if (INSTANCE == null) {
			throw new Exception("RouteInfoCache is not initialized");
		}
		
		return INSTANCE.CACHE;
	}
	
	public static void loadCache(Properties props) throws Exception {
		
		logger.trace("Loading routeinfo xml cache");
		
		long start = System.currentTimeMillis();
		
		if (INSTANCE == null) {
			throw new Exception("RouteInfoCache is not initialized");
		}
		
		for (Object key : INSTANCE.CACHE.getKeySet()) {
			INSTANCE.CACHE.remove(key);
		}
		
		for (Object key : props.keySet()) {
			if (((String)key).contains(routexmlConfigMapEndingString) && (((String)key).length() > routexmlConfigMapEndingString.length())) {
				String entryKey = ((String)key).substring(0, ((String)key).lastIndexOf(routexmlConfigMapEndingString));
				logger.trace("Adding entry with key: {}; Value: {}",entryKey,props.getProperty((String)key));
				INSTANCE.CACHE.put(entryKey, props.getProperty((String)key));
			} else {
				throw new Exception("Invalid routeinfo xml key format: " + (String)key);
			}
		}
		
		logger.trace("Loaded routeinfo xml cache in {} ms",System.currentTimeMillis() - start);
	}
	
	public static void refreshCacheFromConfigMap() throws Exception {
		
		Boolean successRefresh = false;
		
		Config config;
		
		/*
		 * Using the previous working URL if any
		 */
		if (goodURL != null) {
			try {
				logger.trace("Using {} to load config map for routeinfo xml.",goodURL);
				config = new ConfigBuilder().withMasterUrl(goodURL).withOauthToken(System.getProperty("GRM_EDGE_WATCHER_TOKEN", "3ftLdxGOFyNOWzDBv4k6gIWMLMtbnbN9")).withTrustCerts(true).build();
				successRefresh = callAPIServerForRouteInfoConfigMap(config);
			} catch (Exception e) {
				logger.error("Exception in loading routeinfo xml from config map", e);
				successRefresh = false;
			}
		}
		
		/*
		 * if no previous working URL, or the refresh is not successful, switch to the list of API Server URLs
		 */
		if (!successRefresh) {
			List<String> urlsToUse = new ArrayList<>();
			for (String aURL : apiServerUrls) {
				urlsToUse.add(aURL);
			}
			
			// Shuffle the URLs
			if (urlsToUse.size() > 1) {
				long seed = System.nanoTime();
				Collections.shuffle(urlsToUse, new Random(seed));
			}
			
			for (String aURL : urlsToUse) {
				if (!successRefresh) {
					logger.trace("Using {} to load config map for routeinfo xml.",aURL);
					config = new ConfigBuilder().withMasterUrl(aURL).withOauthToken(System.getProperty("GRM_EDGE_WATCHER_TOKEN", "3ftLdxGOFyNOWzDBv4k6gIWMLMtbnbN9")).withTrustCerts(true).build();
					successRefresh = callAPIServerForRouteInfoConfigMap(config);
					if (successRefresh) {
						// Record the working URLs
						goodURL = aURL;
					}
				} else {
					break;
				}
			}
		}
		
		if (!successRefresh) {
			logger.error("Cannot load routeinfo xml from config map.");
		} else {
			logger.trace("Successfully reload routeinfo xml cache from config map.");
		}
	}
	
	public static Boolean callAPIServerForRouteInfoConfigMap(Config config) {
		
		Properties props = new Properties();
		
		/*
		 * Try to load from config map. Once the pod started, updates to config map will not affect what the pod has, so we need to search for the config map from APi Server
		 */
		try(KubernetesClient client = new DefaultKubernetesClient(config)){
			
			if (client.configMaps().inNamespace("com-att-ocnp-mgmt").withName("dmerouteinfo").get() == null) {
				logger.warn("Cannot find config map for GRM-Edge's RouteInfo XML");
			} else {
				Map<String,String> dmeRouteInfoMap = client.configMaps().inNamespace("com-att-ocnp-mgmt").withName("dmerouteinfo").get().getData();
				
				for (String key : dmeRouteInfoMap.keySet()) {
					props.setProperty(key, dmeRouteInfoMap.get(key));
				}
			}
		} catch (Exception e) {
			logger.error("Cannot load routeinfo xml from config map", e);
			return false;
		}
		
		/*
		 * Lastly, we put the routeinfo xml from the config map into the cache.
		 */
		try {
			loadCache(props);
		} catch (Exception e) {
			logger.error("Cannot load routeinfo xml to internal cache", e);
			return false;
		}
		
		return true;
	}
}