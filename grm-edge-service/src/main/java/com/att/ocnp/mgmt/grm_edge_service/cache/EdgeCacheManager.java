/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.ocnp.mgmt.grm_edge_service.cache;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.att.ocnp.mgmt.grm_edge_service.util.GRMEdgeConstants;
import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

public class EdgeCacheManager {
	private static EdgeCacheManager edgeCacheManager = null;
	private static final Logger logger = LoggerFactory.getLogger(EdgeCacheManager.class.getName());
	private static HazelcastInstance hz = null;
	public static final String CACHE_CONFIG_FILE_PATH_WITH_NAME = "CACHE_CONFIG_FILE_PATH_WITH_NAME";
	public static final String HZ_CACHE_CONFIG_FILE_NAME="/hazelcast-config.xml";
	public static final String LOCK_TIMEOUT_MS_STR = "LOCK_TIMEOUT_MS";
	public static final String EDGE_CACHE_INSTANCE = "EDGE_CACHE_INSTANCE";
	private Map<String, EdgeCache> caches = new HashMap<String, EdgeCache>();

	private EdgeCacheManager() {
		try {
			getHzRunningInstance();
		} catch (Exception e) {
			logger.error("Cannot Initialize EdgeCacheManager",e);
		}
	}
	
	public static EdgeCacheManager getInstance() {
		if (edgeCacheManager == null)
			edgeCacheManager = new EdgeCacheManager();
		return edgeCacheManager;
	}

	/**
	 * loading the configuration from file
	 * @return Config object holding all the configuration details
	 */
	private static Config loadHzConfig()
	{
		Config local_config = null;
		try{
			InputStream hzConfigXMLInputStream = EdgeCacheManager.class.getResourceAsStream(HZ_CACHE_CONFIG_FILE_NAME );
			if(hzConfigXMLInputStream==null){
				throw new CacheException(CacheException.ErrorCatalog.CACHE_021, GRMEdgeConstants.HZ_CACHE_CONFIG_FILE_NAME);
			}
			local_config = new XmlConfigBuilder( hzConfigXMLInputStream ).build();
			local_config.setInstanceName(EDGE_CACHE_INSTANCE);
			
			logger.trace( "Searching for existing grmedge instances for hazelcast");
			NetworkConfig networkCfg = local_config.getNetworkConfig();
			JoinConfig joinCfg = networkCfg.getJoin();
			joinCfg.getTcpIpConfig().setRequiredMember(null).setEnabled(true);
			InetAddress[] addresses = null;
			try {
				try{
					addresses = InetAddress.getAllByName("kubernetes.default");	
				}
				catch(Exception e){
					addresses = null;
				}
				if(addresses == null){
					addresses = InetAddress.getAllByName("kubernetes.default.svc");
				}
				
				List<String> urls = new ArrayList<String>();
				for (InetAddress address : addresses) {
					urls.add("https://" + ipToString(address.getAddress()) + ":443/");
				}
				Iterator<String> iter = urls.iterator();
				io.fabric8.kubernetes.client.Config config = new ConfigBuilder().withMasterUrl(iter.next()).withWatchReconnectInterval(0).withTrustCerts(true).withOauthToken(System.getProperty("GRM_EDGE_WATCHER_TOKEN", "3ftLdxGOFyNOWzDBv4k6gIWMLMtbnbN9")).withWatchReconnectLimit(-1).build();
				
				KubernetesClient getPodIPClient = new DefaultKubernetesClient(config);
				List<Pod> podList = getPodIPClient.pods().inAnyNamespace().list().getItems();
				getPodIPClient.close();
				
				File file = new File("/etc/hostname");
				String hostname = null;
				try (FileInputStream in = new FileInputStream(file); BufferedReader br = new BufferedReader(new InputStreamReader(in))){
					String line = null;
					while ((line = br.readLine()) != null) {
						hostname = line;
					}
				} catch (Exception e) {
					logger.error("Cannot load hostname from /etc/hostname. ", e);
				}
				
				for (Pod aPod : podList) {
					if (aPod.getMetadata().getName().contains("grm-edge")) {
						if(aPod.getStatus().getHostIP() == null || aPod.getStatus().getHostIP().contains(hostname)){
							//do nothing, this is the current instance of grm-edge
						}else{
							joinCfg.getTcpIpConfig().addMember(aPod.getStatus().getPodIP() + ":31999");	
						}
						
					}
				}
			} catch (Exception e) {
				logger.error("EdgeCacheManager.startHzContainer - Error trying to establish connection across grmedge instances", e);
			}			
			
			// We want the write delay to be configurable so the following code will read from system property and override
			String writeDelay = System.getProperty("CPFRUN_GRMEDGE_WRITE_BEHIND_DELAY",GRMEdgeConstants.CPFRUN_GRMEDGE_DEFAULT_WRITE_DELAY);
			String writeCoalescing = System.getProperty("CPFRUN_GRMEDGE_WRITE_COALESCING",String.valueOf(GRMEdgeConstants.PLATFORM_RUNTIME_GRMEDGE_DEFAULT_WRITE_COALESCING));
			if(writeDelay != null){
				for(MapConfig mc: local_config.getMapConfigs().values()){
						int writeDelayInt = 0;
						try{
							writeDelayInt = Integer.parseInt(writeDelay);
							mc.getMapStoreConfig().setWriteDelaySeconds(writeDelayInt);
							try{
								logger.trace("Trying to set CPFRUN_GRMEDGE_WRITE_COALESCING="+writeCoalescing);
								Boolean booleanCoalescing = Boolean.valueOf(writeCoalescing);
								mc.getMapStoreConfig().setWriteCoalescing(booleanCoalescing);
							}
							catch(Exception e){
								logger.warn("Unable to convert CPFRUN_GRMEDGE_WRITE_COALESCING="+writeCoalescing+" to Boolean. Using config value: " + false,e);
								mc.getMapStoreConfig().setWriteCoalescing(false);
							}
						}
						catch(Exception e){
							logger.warn("Unable to convert CPFRUN_GRMEDGE_WRITE_BEHIND_DELAY="+writeDelay+" to integer, using default config value",e);
						}
					}	
			}
			
			
		} catch(Exception e){
			throw new CacheException(CacheException.ErrorCatalog.CACHE_010, e, GRMEdgeConstants.HZ_CACHE_CONFIG_FILE_NAME);
		}
		return local_config;
	}
	
	private static String ipToString(byte[] ip) {
		StringBuilder builder = new StringBuilder(23); // IP6 size
		for (int part = 0; part < ip.length; part++) {
			if (builder.length() > 0) {
				builder.append('.');
			}
			int code = ip[part];
			if (code < 0) {
				code += 256; // numbers above 127 is interpreted as negative as byte is signed in java
			}
			builder.append(code);
		}
		return builder.toString();
	}

	/**
	 * starting the Hazelcast container
	 * @return HazelcastInstance
	 */
	private static HazelcastInstance startHzContainer()
	{
		logger.trace( "creating new Hazelcast container");
		return Hazelcast.newHazelcastInstance(loadHzConfig());
	}
	
	/**
	 * get the running Hazelcast instance; create if not exists
	 * @return HazelcastInstance
	 */
	private static HazelcastInstance getHzRunningInstance()
	{
		if( hz == null)
		{
			logger.debug( "cache container is not running");
			hz = startHzContainer();
			logger.debug( "cache container started");
		}
		return hz;
	}

	public boolean isContainerRunning()
	{
		if(hz==null || !hz.getLifecycleService().isRunning())
		{
			logger.trace( "completed; hz[{}].getLifecycleService.isRunning: [{}]",hz, hz!=null?hz.getLifecycleService().isRunning():false); 
		}
		return hz!=null?hz.getLifecycleService().isRunning():false;
	}
	
	public EdgeCache getCache(String cacheName) {
		if (caches.get(cacheName) == null) {
			EdgeCache cache = new EdgeCache(cacheName, getHzRunningInstance().getMap(cacheName));
			logger.trace("Creating new cache with name {}", cacheName);
			caches.put(cacheName, cache);
		}
		return caches.get(cacheName);
	}

	public static void shutdown() 
	{
		if(hz!=null && hz.getLifecycleService().isRunning())
		{
			getHzRunningInstance().shutdown();
		}
		hz = null;
	}
}
