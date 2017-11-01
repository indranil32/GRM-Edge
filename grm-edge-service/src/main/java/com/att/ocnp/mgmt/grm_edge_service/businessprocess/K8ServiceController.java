/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.ocnp.mgmt.grm_edge_service.businessprocess;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.att.ocnp.mgmt.grm_edge_service.cache.ServiceRegistry;
import com.att.ocnp.mgmt.grm_edge_service.types.K8Service;
import com.att.ocnp.mgmt.grm_edge_service.types.KubePod;
import com.att.ocnp.mgmt.grm_edge_service.util.GRMEdgeConstants;
import com.att.ocnp.mgmt.grm_edge_service.util.GRMEdgeErrorGenerator;
import com.att.ocnp.mgmt.grm_edge_service.util.GRMEdgeUtil;
import com.att.ocnp.mgmt.grm_edge_service.util.ObjectValidator;
import com.att.scld.grm.types.v1.ServiceEndPoint;

public class K8ServiceController {


	private static final Logger logger = LoggerFactory.getLogger(K8ServiceController.class.getName());
	private static final Boolean dashToDotEnabled = Boolean.valueOf(System.getProperty("GRM_EDGE_ENABLE_DASH_TO_DOT_CONVERSION", "true"));

	/*
	 * 
	 * 
	 * Add section
	 *
	 *
	 */
	public static void addPod(KubePod pod) {
		logger.trace("Trying to add Pod: {}", pod.toString());
		String key = getCacheKeyPod(pod);
		logger.trace("Using Cache Key: {}" , key);
		//we need to see if its already in the cache
		if(ServiceRegistry.getInstance().getCache(GRMEdgeConstants.POD_CACHE).get(key) == null){
			ServiceRegistry.getInstance().getCache(GRMEdgeConstants.POD_CACHE).put(key,pod);	
		}
		else{
			logger.trace("Modifying pod since it already exists");
			KubePod cachePod = (KubePod) ServiceRegistry.getInstance().getCache(GRMEdgeConstants.POD_CACHE).get(key);
			if(cachePod != null)
				deletePods(cachePod);
			ServiceRegistry.getInstance().getCache(GRMEdgeConstants.POD_CACHE).put(key,pod);	
		}
		logger.trace("Pod added to Cache. Now checking for matching services.");
		for(K8Service service: GRMEdgeUtil.getMatchingServiceTags(pod)){
			logger.trace("Found matching service! Creating a new SEP to add to cache");
			ServiceEndPoint sep = GRMEdgeUtil.createSEP(pod, service);
			if(sep == null){
				logger.warn("SEP was not able to be completed. Please see logs for more detail.");
			}
			else{
				//didnt exist, lets make a new endpoint to add
				addEndPoint(sep);	
			}	
		}
	}
	
	public static void forceAddPod(KubePod pod) {
		logger.trace("Trying to add Pod: {}", pod.toString());
		String key = getCacheKeyPod(pod);
		logger.trace("Force Adding Pod Using Cache Key: {}" , key);
		
		ServiceRegistry.getInstance().getCache(GRMEdgeConstants.POD_CACHE).put(key,pod);
		
	}

	public static String addServices(K8Service k8Service){
		logger.debug("Trying to add k8Service: {}", k8Service.toString());
		addService(k8Service);
		String result = "Added service to k8 service cache.";
		List<KubePod> matchingPods = GRMEdgeUtil.getMatchingPodTags(k8Service);
		if(matchingPods == null || matchingPods.isEmpty()){
			logger.debug("No Matching Pods were found");
		}
		else{
			logger.debug("Matching pods were found. Creating a SEP for each matching pod.");
			int successCount = 0;
			for(KubePod pod: matchingPods){
				ServiceEndPoint sep = GRMEdgeUtil.createSEP(pod,k8Service);
				if(sep != null && addEndPoint(sep)){
					successCount++;
				}
			}
			if(successCount > -1){
				result = "Added service to k8 service cache. Found pods with same tag, created "+successCount+" ServiceEndPoint(s)";
			}
			logger.debug(result);
		}
		return result;
	}



	public static void addService(K8Service k8Service) {
		try{
			String key = getCacheKeyK8(k8Service);
			logger.trace("Using Cache Key: {}" , key);
			ServiceRegistry.getInstance().getCache(GRMEdgeConstants.SERVICE_CACHE).put(key,k8Service);
			logger.trace("Added k8Service to registry {}",k8Service.toString());
		}
		catch (Exception e){
			logger.error(GRMEdgeErrorGenerator.generateErrorMessage(GRMEdgeConstants.K8SERVICE_CACHE_ADD_ERROR,e.getMessage()),e);
		}
		return;

	}

	public static boolean addEndPoint(ServiceEndPoint sep) {
		try{
			if(sep != null && ObjectValidator.validate(sep)){

				logger.trace("Service EndPoint Add request: {}" , sep.toString());
				String key = getCacheKeySEP(sep);
				logger.trace("Using cache key: {}" , key);
				ServiceRegistry.getInstance().getCache(GRMEdgeConstants.ENDPOINT_CACHE).addEndPoint(key, sep);
				logger.info("Added SEP to cache with serviceName: {}" , sep.getName());	
			}
			else{
				logger.debug("Not adding invalid SEP");
				return false;
			}
		}
		catch (Exception e){
			logger.error(GRMEdgeErrorGenerator.generateErrorMessage(GRMEdgeConstants.ENDPOINT_CACHE_ADD_ERROR,e.getMessage()),e);
			return false;
		}
		return true;
	}
	
	public static boolean addEndPointsOverride(List<ServiceEndPoint> seps) {
		try{
			if(seps != null && !seps.isEmpty()){
				
				List<ServiceEndPoint> sepsAfterValidation = new ArrayList<>();
				
				for (ServiceEndPoint aSEP : seps) {
					if(ObjectValidator.validate(aSEP)){
						logger.trace("Service EndPoint Bulk Add Request: {}" , aSEP.toString());
						sepsAfterValidation.add(aSEP);
					}
				}
				
				String key = getCacheKeySEP(seps.get(0));
				logger.trace("Using cache key: {}" , key);
				ServiceRegistry.getInstance().getCache(GRMEdgeConstants.ENDPOINT_CACHE).put(key, sepsAfterValidation);
			} else{
				logger.debug("Not adding invalid SEP list");
				return false;
			}
		}
		catch (Exception e){
			logger.error(GRMEdgeErrorGenerator.generateErrorMessage(GRMEdgeConstants.ENDPOINT_CACHE_ADD_ERROR,e.getMessage()),e);
			return false;
		}
		return true;
	}

	/*
	 * 
	 * 
	 * Cache Key Section
	 * 
	 * 
	 */
	public static String getCacheKeyK8(K8Service k8Service){
		return k8Service.getMetadata().get("uid");
	}

	public static String getCacheKeyPod(KubePod pod){
		return pod.getName()+pod.getHostIP();
	}

	public static String getCacheKeySEP(ServiceEndPoint sep){
		return GRMEdgeUtil.getEnv(sep)+sep.getName();
	}
	
	public static String getCacheKeySEP(KubePod pod){
		String env;
		env = GRMEdgeUtil.getEnvNameSpaceWithDashes(pod.getNamespace());
		String k8NamespaceNameWithoutEnv = GRMEdgeUtil.removeEnvFromNamespace(pod.getNamespace(), env);;
		if(System.getProperty("GRM_EDGE_DISABLE_ENV_PARSING","false").equalsIgnoreCase("true")){
			k8NamespaceNameWithoutEnv = pod.getNamespace();
			env = GRMEdgeUtil.getEnvDefault();
		}
		if (dashToDotEnabled) {
			k8NamespaceNameWithoutEnv = k8NamespaceNameWithoutEnv.replace('-','.');
		}
		return getCacheKeySEP(k8NamespaceNameWithoutEnv+"."+pod.getLabels().get("app"),env); 
	}

	public static String getCacheKeySEP(K8Service service){
		String env;
		env = GRMEdgeUtil.getEnvNameSpaceWithDashes(service.getMetadata().get("namespace"));
		String k8NamespaceNameWithoutEnv = GRMEdgeUtil.removeEnvFromNamespace(service.getMetadata().get("namespace"), env);;
		if(System.getProperty("GRM_EDGE_DISABLE_ENV_PARSING","false").equalsIgnoreCase("true")){
			k8NamespaceNameWithoutEnv = service.getMetadata().get("namespace");
			env = GRMEdgeUtil.getEnvDefault();
		}
		if (dashToDotEnabled) {
			k8NamespaceNameWithoutEnv = k8NamespaceNameWithoutEnv.replace('-','.');
		}
		return getCacheKeySEP(k8NamespaceNameWithoutEnv+"."+service.getLabelSelector().get("app"),env);
	}

	public static String getCacheKeySEP(String service, String env) {
		return env+service;
	}

	/*
	 * 
	 * 
	 * Delete Section
	 * 
	 * 
	 */
	public static void deleteServices(K8Service k8Service){
		logger.trace("Deleting k8Service details: {}" , k8Service.toString());
		//Delete Service from Service Cache
		String key = getCacheKeyK8(k8Service);
		logger.trace("Using Cache Key: {}" , key);
		ServiceRegistry.getInstance().getCache(GRMEdgeConstants.SERVICE_CACHE).remove(key);
		logger.trace("Removed k8Service from cache");
		//Delete from service endpoint cache all entries of this service
		logger.trace("Checking and deleting matching ServiceEndPoints");
		String sepKey = getCacheKeySEP(k8Service);
		logger.trace("Using CacheMap Key: {}" , sepKey);

		List<ServiceEndPoint> seps = (List<ServiceEndPoint>) ServiceRegistry.getInstance().getCache(GRMEdgeConstants.ENDPOINT_CACHE).get(sepKey);
		if(seps != null){

			List<KubePod> pods = GRMEdgeUtil.getMatchingPodTags(k8Service);
			List<ServiceEndPoint> epsToRemove = new ArrayList<>();
			if (pods != null){
				for (KubePod pod : pods){
					ServiceEndPoint tempSEP = GRMEdgeUtil.createSEP(pod, k8Service);
					Iterator sepIter = seps.iterator();
					while(sepIter.hasNext()){
						ServiceEndPoint sep = (ServiceEndPoint) sepIter.next();
						if (GRMEdgeUtil.checkEqualServiceEndPointPK(tempSEP, sep)){
							logger.trace("Found matching SEP to delete!");
							epsToRemove.add(tempSEP);
						}
					}
				}
			}

			ServiceRegistry.getInstance().getCache(GRMEdgeConstants.ENDPOINT_CACHE).removeEndPoints(sepKey, epsToRemove);
		}
		logger.debug("Deleted Service");
	}

	public static void deletePods(KubePod pod){
		logger.trace("Deleting pod details: {}" , pod.toString());
		//Delete pod from pod cache
		String key = getCacheKeyPod(pod);
		logger.trace("Using Cache Key: {}" , key);
		if(ServiceRegistry.getInstance().getCache(GRMEdgeConstants.POD_CACHE).get(key) == null){
			logger.trace("Pod has already been deleted. Not Continuing with this transaction");
			return;
		}
		KubePod podToDelete = (KubePod)ServiceRegistry.getInstance().getCache(GRMEdgeConstants.POD_CACHE).get(key);
		ServiceRegistry.getInstance().getCache(GRMEdgeConstants.POD_CACHE).remove(key);
		logger.debug("Deleted Pod from Cache");

		logger.trace("Checking to see if there is still one or more of the same pods running on same node");
		boolean anotherPodStillRunning = false;
		Iterator poditer = ServiceRegistry.getInstance().getCache(GRMEdgeConstants.POD_CACHE).values().iterator();
		while(poditer.hasNext()){
			KubePod curPod = (KubePod) poditer.next();
			if(podToDelete !=null && podToDelete.getHostIP() != null && curPod.getHostIP()!= null && podToDelete.getHostIP().equals(curPod.getHostIP())){
				if(GRMEdgeUtil.podMatchingOnSameHost(curPod, podToDelete, ServiceRegistry.getInstance().getCache(GRMEdgeConstants.SERVICE_CACHE).values().iterator())){
					logger.trace("Found matching pod still running on node: " + curPod.toString());
					anotherPodStillRunning = true;
					break;
				}
			}
		}
		
		//no matching pods are running, delete any SEP matching!
		if(!anotherPodStillRunning){
			Iterator iter = ServiceRegistry.getInstance().getCache(GRMEdgeConstants.SERVICE_CACHE).values().iterator();
			String sepKey = getCacheKeySEP(podToDelete);
			logger.trace("Using Cache Key: {} to look for SEPS matching the POD", sepKey);
			String podhost = pod.getHostIP();
			if (podhost != null && System.getProperty("GRM_EDGE_REGISTER_ENDPOINTS_WITH_FULL_HOSTNAME", "false").equalsIgnoreCase("true")) {
				try {
					InetAddress addr = InetAddress.getByName(pod.getHostIP());
					podhost = addr.getHostName();
				} catch (Exception e) {
					podhost = pod.getHostIP();
				}	
			}
			List<ServiceEndPoint> curSep = (List<ServiceEndPoint>) ServiceRegistry.getInstance().getCache(GRMEdgeConstants.ENDPOINT_CACHE).get(sepKey);
			if(curSep != null){
				Iterator<ServiceEndPoint> sepIter = curSep.iterator();
				ServiceEndPoint epToRemove = null;
				while(sepIter.hasNext()){
					ServiceEndPoint aSep = sepIter.next();
					if (podhost != null && aSep.getHostAddress().equals(podhost) && podToDelete.getLabels() != null)
					{
						String routeOffer = podToDelete.getLabels().get("routeoffer");
						if(routeOffer == null){
							routeOffer = GRMEdgeUtil.getRouteOfferDefault();
						}
						if(GRMEdgeUtil.getVersionString(aSep.getVersion()).equals(podToDelete.getLabels().get("version")) && routeOffer.equals(aSep.getRouteOffer())){
							logger.trace("Found matching SEP to delete");
							logger.trace("Matching SEP details: {}" , aSep.toString());
							epToRemove = aSep;
							break;
						}
					}
				}
				if(epToRemove != null){
					ServiceRegistry.getInstance().getCache(GRMEdgeConstants.ENDPOINT_CACHE).removeEndPoint(sepKey,epToRemove);		
				}
			}	
		}
	}

	//we are assuming we only get 1 modify event per service that is modified. This is unlike the pod which gets multiple modified events throughout its lifespan.
	public static void modifyService(K8Service serviceToModify) {

		K8Service existService = (K8Service) ServiceRegistry.getInstance().getCache(GRMEdgeConstants.SERVICE_CACHE).get(getCacheKeyK8(serviceToModify));
		if (existService == null){
			logger.trace("Service does not exist in cache. Not modifying service but adding it instead");
			addServices(serviceToModify);
			return;
		}
		K8Service originalService = new K8Service(existService.getMetadata(), existService.getNodePort(), existService.getProtocol(), existService.getLabelSelector());

		boolean changed = false;
		//if protocols dont match override.
		if (existService.getProtocol() != null && serviceToModify.getProtocol() != null && !existService.getProtocol().equalsIgnoreCase(serviceToModify.getProtocol())){
			existService.setProtocol(serviceToModify.getProtocol());
			changed = true;
		}

		//if metadata doesnt match override.
		if (existService.getMetadata() != null && serviceToModify.getMetadata() != null && !existService.getMetadata().equals(serviceToModify.getMetadata())){
			existService.setMetadata(serviceToModify.getMetadata());
			changed = true;
		}

		//if NodePort doesnt match override.
		if (existService.getNodePort() != null && serviceToModify.getNodePort() != null && !existService.getNodePort().equalsIgnoreCase(serviceToModify.getNodePort())){
			existService.setNodePort(serviceToModify.getNodePort());
			changed = true;
		}

		//if LabelSelector doesnt match override.
		if (existService.getLabelSelector() != null && serviceToModify.getLabelSelector() != null && !existService.getLabelSelector().equals(serviceToModify.getLabelSelector())){
			existService.setLabelSelector(serviceToModify.getLabelSelector());
			changed = true;
		}
		if(!changed){
			logger.trace("No change was detected for modify service");
			return;
		}
		
		logger.trace("Deleting old SEPs for modified service");
		deleteServices(originalService);
		logger.trace("Adding any new SEPs for modified service");
		addServices(existService);
	}
}
