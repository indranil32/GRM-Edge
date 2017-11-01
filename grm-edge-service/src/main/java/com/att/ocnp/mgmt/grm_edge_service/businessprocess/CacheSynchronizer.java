/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.ocnp.mgmt.grm_edge_service.businessprocess;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.att.ocnp.mgmt.grm_edge_service.cache.ServiceRegistry;
import com.att.ocnp.mgmt.grm_edge_service.util.GRMEdgeConstants;
import com.att.ocnp.mgmt.grm_edge_service.util.GRMEdgeUtil;
import com.att.scld.grm.types.v1.ServiceEndPoint;
import com.att.scld.grm.v1.AddServiceEndPointRequest;
import com.att.scld.grm.v1.DeleteServiceEndPointRequest;

public class CacheSynchronizer implements Runnable{

	private static final Logger logger = LoggerFactory.getLogger(CacheSynchronizer.class);
	
	public CacheSynchronizer(){
		Thread.currentThread().setName("CacheSynchronizer");
	}
	
	/*
	 *
	 * The CacheSynchronizer uses the following scenarios as its rules.
	 * 
	 *     
	 *
	 *     
Scenario	 EdgeContains	OldSepContains	GRMContains		Possible Reasons													Action
-We dont do scenario #1
	1			0				0				0				empty key													delete key from edge cache
	2			0				0				1			Added in next layer												add to edge
	3			0				1				0	delete from edge and deleted from grm with no write behind yet			do nothing
	4			0				1				1	delete from edge with no write behind yet								covered by action below
	5			0				1				1	delete from edge with no write behind yet added to grm					compare timestamps. If edge is older, add GRMSEP to Edge Cache
																															If edge is younger, do nothing write behind will cover. If same, do nothing, write behind will cover
	6			1				0				0	added to current layer but no write behind yet							do nothing
	7			1				0				1	added to current layer but no write behind yet. 						compare timestamps. If edge is older, delete current sep from edge and add GRMSEP to Edge Cache
													Endpoint also added to GRM from another source	 						If edge is younger, do nothing write behind will cover. If same, do nothing, write behind will cover
	8			1				1				0	grm delete endpoint														delete from edge
	9			1				1				1	atable endpoint (same in all)											do nothing
	10			1				1				1	modified in edge no write behind has occurred							compare timestamps to edge cache and grm. 
																															If edge is older, add grm to edge.
																															If edge is younger, do nothing.
																															If the same then do nothing."
	11			1				1				1	modified in edge no write behind has occurred modified in grm			see 10
	12			1				1				1	modified in grm but not edge											see 10

	 *     
	 *     
	 */
	
	
	
	
	//scenario 1 doesnt require its own method
	
	//scenario 2
	private boolean scenario2(List<ServiceEndPoint> edgeSEPList, ServiceEndPoint grmSEP, List<ServiceEndPoint> oldSEPList){
		for(ServiceEndPoint sep: edgeSEPList){
			if(GRMEdgeUtil.checkEqualServiceEndPointPK(grmSEP, sep)){
				return false;
			}
		}
		for(ServiceEndPoint sep:oldSEPList){
			if(GRMEdgeUtil.checkEqualServiceEndPointPK(grmSEP, sep)){
				return false;
			}
		}
		return true;
	}
	
	//scenario 5
	private ServiceEndPoint scenario5(List<ServiceEndPoint> edgeSEPList, ServiceEndPoint grmSEP, List<ServiceEndPoint> oldSEPList){
		for(ServiceEndPoint sep: edgeSEPList){
			if(GRMEdgeUtil.checkEqualServiceEndPointPK(grmSEP, sep)){
				return null;
			}
		}
		for(ServiceEndPoint sep: oldSEPList){
			if(GRMEdgeUtil.checkEqualServiceEndPointPK(grmSEP, sep)){
				return sep;
			}
		}
		return null;
	}
	
	//scenario7
	private ServiceEndPoint scenario7(ServiceEndPoint edgeSEP, List<ServiceEndPoint> grmSEPList, List<ServiceEndPoint> oldSEPList){
		for(ServiceEndPoint sep: oldSEPList){
			if(GRMEdgeUtil.checkEqualServiceEndPointPK(edgeSEP, sep)){
				return null;
			}
		}
		for(ServiceEndPoint sep: grmSEPList){
			if(GRMEdgeUtil.checkEqualServiceEndPointPK(sep, edgeSEP)){
				return sep;
			}
		}
		return null;
	}
	
	private boolean scenario8(ServiceEndPoint edgeSEP, List<ServiceEndPoint> grmSEPList, List<ServiceEndPoint> oldSEPList){
		for(ServiceEndPoint sep: grmSEPList){
			if(GRMEdgeUtil.checkEqualServiceEndPointPK(sep, edgeSEP)){
				return false;
			}
		}
		for(ServiceEndPoint sep: oldSEPList){
			if(GRMEdgeUtil.checkEqualServiceEndPointPK(sep, edgeSEP)){
				return true;
			}
		}
		return false;
	}
	
	private ServiceEndPoint scenario10(ServiceEndPoint edgeSEP, List<ServiceEndPoint> grmSEPList, List<ServiceEndPoint> oldSEPList){
		boolean found = false;
		ServiceEndPoint sepReturn = null;
		for(ServiceEndPoint sep: grmSEPList){
			if(GRMEdgeUtil.checkEqualServiceEndPointPK(sep, edgeSEP)){
				found = true;
				sepReturn = sep;
				
			}
		}
		if(!found){
			return null;
		}
		found = false;
		for(ServiceEndPoint sep: oldSEPList){
			if(GRMEdgeUtil.checkEqualServiceEndPointPK(sep, edgeSEP)){
				found = true;
			}
		}
		if(!found){
			return null;
		}
		return sepReturn;
	}
		
	@Override
	public void run() {
		boolean go = true;
		while(go){

			try{
				Thread.sleep(Integer.parseInt(System.getProperty("PLATFORM_RUNTIME_GRMEDGE_DEFAULT_CACHESYNCHRONIZER_SLEEP",String.valueOf(GRMEdgeConstants.PLATFORM_RUNTIME_GRMEDGE_DEFAULT_CACHESYNCHRONIZER_SLEEP))));
				if(System.getProperty("CPFRUN_PRINT_CACHE_SIZES_ON_SYNCHRONIZE","False").equalsIgnoreCase("True")){
					logger.info("Cache Sizes: " + GRMEdgeConstants.POD_CACHE+"[{}], "+GRMEdgeConstants.SERVICE_CACHE+"[{}], " + GRMEdgeConstants.ENDPOINT_CACHE+"[{}], " + GRMEdgeConstants.OLD_ENDPOINT_CACHE +"[{}], "
							+GRMEdgeConstants.OLD_ENDPOINT_CASS_CACHE+"[{}],  "+GRMEdgeConstants.ROUTEINFO_XML_CACHE+"[{}]",ServiceRegistry.getInstance().getCache(GRMEdgeConstants.POD_CACHE).size()
							,ServiceRegistry.getInstance().getCache(GRMEdgeConstants.SERVICE_CACHE).size(),ServiceRegistry.getInstance().getCache(GRMEdgeConstants.ENDPOINT_CACHE).size()
							,ServiceRegistry.getInstance().getCache(GRMEdgeConstants.OLD_ENDPOINT_CACHE).size(),ServiceRegistry.getInstance().getCache(GRMEdgeConstants.OLD_ENDPOINT_CASS_CACHE).size()
							,ServiceRegistry.getInstance().getCache(GRMEdgeConstants.ROUTEINFO_XML_CACHE).size());
				}
				if(System.getProperty("CPFRUN_GRMEDGE_GRM_WRITE_BEHIND_ENABLE","False").equalsIgnoreCase("True")){

					MDC.put(GRMEdgeConstants.Tracking, UUID.randomUUID().toString());
					
					//get the list of service keys from the endpoint cache. 
					Iterator<Object> cacheKeysToCheck = ServiceRegistry.getInstance().getCache(GRMEdgeConstants.ENDPOINT_CACHE).getKeySet().iterator();
					
					while(cacheKeysToCheck.hasNext()){
						String keyToCheck = (String) cacheKeysToCheck.next();
						logger.debug("Synchronzer is checking key: {}" , keyToCheck);
						if(keyToCheck != null){
							List<AddServiceEndPointRequest> addOrUpdateGRMList = new ArrayList<>();
							List<DeleteServiceEndPointRequest> deleteServiceEndPointGRMList = new ArrayList<>();
							List<ServiceEndPoint> sepsFromGRMKey = EdgeGRMHelper.getEndpointsWithKey(keyToCheck);
							List<ServiceEndPoint> sepsFromOldCacheKey = (List<ServiceEndPoint>) ServiceRegistry.getInstance().getCache(GRMEdgeConstants.OLD_ENDPOINT_CACHE).get(keyToCheck);
							boolean obtainedLock = false;
							try{
								obtainedLock = ServiceRegistry.getInstance().getCache(GRMEdgeConstants.ENDPOINT_CACHE).tryLock(keyToCheck);
							}
							catch(InterruptedException e){
								logger.error("Error obtained while obtaining lock for key: {}",keyToCheck);
							}
							if(!obtainedLock){
								logger.warn("Did not obtain lock for key: {} Will try again next synchronize.",keyToCheck);
								continue;
							}
							List<ServiceEndPoint> sepsFromCacheKey = (List<ServiceEndPoint>) ServiceRegistry.getInstance().getCache(GRMEdgeConstants.ENDPOINT_CACHE).get(keyToCheck);
							
							List<ServiceEndPoint> sepsToAddToCache = new ArrayList<>();
							
							if(sepsFromCacheKey == null || sepsFromGRMKey == null){
								//something seems to have gone wrong. We will try again on next sync up. Cache should always have a list if it gets to this point (even if its empty list) and GRM should always return at least empty list
								logger.warn("Skipping synchronize for sep key: " + keyToCheck+" . Got an unexpected null value in either cache("+sepsFromCacheKey+") or next layer of GRM("+sepsFromGRMKey+")");
								if(obtainedLock)
									ServiceRegistry.getInstance().getCache(GRMEdgeConstants.ENDPOINT_CACHE).unlock(keyToCheck);
								continue;
							}
							
							if(sepsFromOldCacheKey == null){
								sepsFromOldCacheKey = new ArrayList<>();
							}
							
							boolean updatedSEPCache = false;
							
							logger.debug("Number of Endpoints in Edge: {}" , sepsFromCacheKey.size());
							logger.debug("Number of Endpoints in OldSep: {}" , sepsFromOldCacheKey.size());
							logger.debug("Number of Endpoints in GRM: {}" , sepsFromGRMKey.size());
							
							Iterator cacheIter = sepsFromCacheKey.iterator();
							while(cacheIter.hasNext()){
								ServiceEndPoint sepCache = (ServiceEndPoint) cacheIter.next();
								if(!GRMEdgeUtil.sepInCurrentCluster(sepCache)){

									ServiceEndPoint sepGRM = scenario7(sepCache, sepsFromGRMKey, sepsFromOldCacheKey);
									if(sepGRM != null){
										int comparison = compareTimestampsWithNullCheck(sepCache, sepGRM);
										logger.trace("Scenario7 found.");
										if(comparison < 0){
											ServiceEndPoint sepToAdd = checkAndAddOperationalInfo(sepGRM);
											updateServiceEndPoint(sepCache, sepToAdd);							
											logger.trace("Pulling a more recent SEP.\nModified SEP: {}",sepCache.toString());
											updatedSEPCache = true;
											continue;	
										}
									}
									
									if(scenario8(sepCache, sepsFromGRMKey, sepsFromOldCacheKey)){
										logger.trace("Scenario8 found");
										cacheIter.remove();
										logger.trace("Removing SEP from Edge: {}" , sepCache.toString());
										updatedSEPCache = true;
										continue;
									}
									
									sepGRM = scenario10(sepCache, sepsFromGRMKey, sepsFromOldCacheKey);
								    if(sepGRM != null){
										logger.trace("Scenario10 found");
										int comparison = compareTimestampsWithNullCheck(sepCache, sepGRM);
										if(comparison < 0){
											ServiceEndPoint sepToAdd = checkAndAddOperationalInfo(sepGRM);
											updateServiceEndPoint(sepCache, sepToAdd);							
											logger.trace("Pulling a more recent SEP.\nModified SEP: {}",sepCache.toString());
											updatedSEPCache = true;
											continue;	
										}
									}		
								}
								else{
									if(scenario8(sepCache,sepsFromGRMKey, sepsFromOldCacheKey)){
										logger.trace("Scenario8 found with endpoint in current cluster. We need to add this endpoint back to GRM.");
										AddServiceEndPointRequest add = new AddServiceEndPointRequest();
										add.setServiceEndPoint(sepCache);
										add.setEnv(GRMEdgeUtil.getEnv(sepCache));
										add.setCheckNcreateParents(true);
//										AddServiceEndPointResponse response = EdgeGRMHelper.addGRMEndPoint(add);
										addOrUpdateGRMList.add(add);
									}
									ServiceEndPoint sepGRM = scenario10(sepCache, sepsFromGRMKey, sepsFromOldCacheKey);
									if(sepGRM != null && !GRMEdgeUtil.checkEqualServiceEndPoints(sepCache, sepGRM)){
										//update GRM with local copy.
										AddServiceEndPointRequest add = new AddServiceEndPointRequest();
										add.setServiceEndPoint(sepCache);
										add.setEnv(GRMEdgeUtil.getEnv(sepCache));
										add.setCheckNcreateParents(true);
//										AddServiceEndPointResponse response = EdgeGRMHelper.addGRMEndPoint(add);
										addOrUpdateGRMList.add(add);
									}
								}
							}
							
							logger.trace("Checking GRM Seps");
							Iterator grmIter = sepsFromGRMKey.iterator();
							//Now we need to compare next layer GRM to cache to see if endpoint in grm that is not in cache
							while(grmIter.hasNext()){
								ServiceEndPoint sepGRM = (ServiceEndPoint) grmIter.next();
								if(!GRMEdgeUtil.sepInCurrentCluster(sepGRM)){
									
									if (scenario2(sepsFromCacheKey, sepGRM, sepsFromOldCacheKey)){
										logger.trace("Scenario2 found");
										ServiceEndPoint sepToAdd = checkAndAddOperationalInfo(sepGRM);
										updatedSEPCache = true;
										sepsToAddToCache.add(sepToAdd);
										logger.trace("GRM has a SEP for empty key in Edge. Adding it to edge. {}" , sepGRM);
										continue;
									}
									
									
									ServiceEndPoint edgeOLDSEP = scenario5(sepsFromCacheKey,sepGRM, sepsFromOldCacheKey);
									if(edgeOLDSEP != null){
										logger.trace("Scenario5 found");
										int comparison = compareTimestampsWithNullCheck(edgeOLDSEP, sepGRM);
										if (comparison < 0){
											ServiceEndPoint sepToAdd = checkAndAddOperationalInfo(sepGRM);
											sepsToAddToCache.add(sepToAdd);
											logger.trace("GRM has more recent SEP. Adding it to cache\nAdding SEP: {}",sepGRM.toString());
											updatedSEPCache = true;	
										}
										continue;
									}
									
								}
								else{
									if (scenario2(sepsFromCacheKey, sepGRM, sepsFromOldCacheKey)){
										DeleteServiceEndPointRequest deleteReq = new DeleteServiceEndPointRequest();
										deleteReq.getServiceEndPoint().add(sepGRM);
										deleteReq.setEnv(GRMEdgeUtil.getEnv(sepGRM));
										deleteServiceEndPointGRMList.add(deleteReq);
									}
									ServiceEndPoint edgeOLDSEP = scenario5(sepsFromCacheKey,sepGRM, sepsFromOldCacheKey);
									if(edgeOLDSEP != null){
										if(GRMEdgeUtil.checkEqualServiceEndPoints(edgeOLDSEP, sepGRM)){
											DeleteServiceEndPointRequest deleteReq = new DeleteServiceEndPointRequest();
											deleteReq.getServiceEndPoint().add(sepGRM);
											deleteReq.setEnv(GRMEdgeUtil.getEnv(sepGRM));
											deleteServiceEndPointGRMList.add(deleteReq);
										}
									}
								}
							}
							if(updatedSEPCache){
								logger.trace("Performing Synchronizer update now");
								sepsFromCacheKey.addAll(sepsToAddToCache);
								ServiceRegistry.getInstance().getCache(GRMEdgeConstants.ENDPOINT_CACHE).put(keyToCheck,sepsFromCacheKey);
							}
							if(obtainedLock)
								ServiceRegistry.getInstance().getCache(GRMEdgeConstants.ENDPOINT_CACHE).unlock(keyToCheck);
							for(AddServiceEndPointRequest add: addOrUpdateGRMList){
								Object o = EdgeGRMHelper.addGRMEndPoint(add);
								if( o == null){
									logger.error("Unable to update GRM with cache data for sep: [{}] . Trying again next time.", add.getServiceEndPoint().toString());
								}
							}
							for(DeleteServiceEndPointRequest delete: deleteServiceEndPointGRMList){
								Object o = EdgeGRMHelper.grmDeleteSEP(delete);
								if( o == null){
									logger.error("Unable to update GRM with cache data for sep: [{}] . Trying again next time.", delete.getServiceEndPoint().toString());
								}
							}
						}
					}
				
				}
				else{
					logger.debug("CacheSynchronizer did not run. CPFRUN_GRMEDGE_GRM_WRITE_BEHIND_ENABLE needs to be set to true. It is set to: " + System.getProperty("CPFRUN_GRMEDGE_GRM_WRITE_BEHIND_ENABLE","False"));
				}
			}		
			catch(InterruptedException e){
				try {
					logger.warn("Unable to sleep CacheSynchronizer with value for PLATFORM_RUNTIME_GRMEDGE_DEFAULT_CACHESYNCHRONIZER_SLEEP. Trying again with default value.",e);
					Thread.sleep(GRMEdgeConstants.PLATFORM_RUNTIME_GRMEDGE_DEFAULT_CACHESYNCHRONIZER_SLEEP);
				} catch (InterruptedException e1) {
					logger.error("Unable to sleep CacheSynchronizer", e1);
				}
			}
			catch(Exception e){
				logger.error("Exception occured during synchronization!",e);
			}
			MDC.remove(GRMEdgeConstants.Tracking);
		}
	}
	
	private void updateServiceEndPoint(ServiceEndPoint sepToModify, ServiceEndPoint sepDataToUse) {
		
		if(sepToModify.getVersion().getMajor() != sepDataToUse.getVersion().getMajor() || sepToModify.getVersion().getMinor() != sepDataToUse.getVersion().getMinor() || !sepToModify.getVersion().getPatch().equalsIgnoreCase(sepDataToUse.getVersion().getPatch())){
			sepToModify.setVersion(sepDataToUse.getVersion());
		}
		if(!sepToModify.getListenPort().equalsIgnoreCase(sepDataToUse.getListenPort())){
			sepToModify.setListenPort(sepDataToUse.getListenPort());
		}
		//ideally lat and long will not change
		if(!sepToModify.getLatitude().equalsIgnoreCase(sepDataToUse.getLatitude())){
			sepToModify.setLatitude(sepDataToUse.getLatitude());
		}
		if(!sepToModify.getLongitude().equalsIgnoreCase(sepDataToUse.getLongitude())){
			sepToModify.setLongitude(sepDataToUse.getLongitude());
		}
		if(!sepToModify.getProtocol().equalsIgnoreCase(sepDataToUse.getProtocol())){
			sepToModify.setProtocol(sepDataToUse.getProtocol());
		}
		
		if(!sepToModify.getRouteOffer().equalsIgnoreCase(sepDataToUse.getRouteOffer())){
			sepToModify.setRouteOffer(sepDataToUse.getRouteOffer());
		}
		
		sepToModify.setOperationalInfo(sepDataToUse.getOperationalInfo());
		
	}

	private ServiceEndPoint checkAndAddOperationalInfo(ServiceEndPoint sepGRM) {
		ServiceEndPoint returnSEP = sepGRM;
		logger.debug("SEP TO ADD OPERATION INFO TO: " + returnSEP.getOperationalInfo().toString());
		logger.debug("GRM SEP TO USE AS INFO: " + sepGRM.getOperationalInfo().toString());
		returnSEP.setOperationalInfo(sepGRM.getOperationalInfo());
		logger.debug("SEP WITH GRM OPERATION: " + returnSEP.getOperationalInfo().toString());
		if(returnSEP.getOperationalInfo().getUpdatedTimestamp() == null){
			returnSEP.getOperationalInfo().setUpdatedTimestamp(returnSEP.getOperationalInfo().getCreatedTimestamp());
			returnSEP.getOperationalInfo().setUpdatedBy(returnSEP.getOperationalInfo().getUpdatedBy());
		}
		logger.debug("SEP WITH GRM OPERATION AFTER UPDATING UPDATE: " + returnSEP.getOperationalInfo().toString());
		return returnSEP;
	}

	//return -1 if left is older
	//return 0 if equal
	//return 1 if left is younger
	private int compareTimestampsWithNullCheck(ServiceEndPoint sep1, ServiceEndPoint sep2){
		if(sep1.getOperationalInfo() != null && sep2.getOperationalInfo() != null){
			if(sep1.getOperationalInfo().getUpdatedTimestamp() == null && sep2.getOperationalInfo().getUpdatedTimestamp() == null){
				return 0;
			}
			else if( sep1.getOperationalInfo().getUpdatedTimestamp() != null && sep2.getOperationalInfo().getUpdatedTimestamp() == null){
				return 1;
			}
			else if (sep1.getOperationalInfo().getUpdatedTimestamp() == null && sep2.getOperationalInfo().getUpdatedTimestamp() != null){
				return -1;
			}
			else{
				int comparison = sep1.getOperationalInfo().getUpdatedTimestamp().compare(sep2.getOperationalInfo().getUpdatedTimestamp());
				if(comparison < 0){
					return -1;
				}
				if(comparison > 0){
					return 1;
				}
				else{
					return 0;
				}	
			}
		}
		return 0;
	}
}