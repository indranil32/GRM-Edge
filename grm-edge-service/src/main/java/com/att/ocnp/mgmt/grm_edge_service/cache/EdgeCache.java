/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.ocnp.mgmt.grm_edge_service.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.att.ocnp.mgmt.grm_edge_service.util.GRMEdgeUtil;
import com.att.ocnp.mgmt.grm_edge_service.util.ObjectValidator;
import com.att.scld.grm.types.v1.ServiceEndPoint;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.hazelcast.core.IMap;

/**
 * TODO add comments here
 */
public class EdgeCache {
	private static final Logger logger = LoggerFactory.getLogger(EdgeCache.class.getName());
	private IMap<Object, Object> cacheMap = null;
	public static final String LOCK_TIMEOUT_MS_STR = "LOCK_TIMEOUT_MS";
	private static long LOCKOUT_TIMEOUT_MS = 100L;
	public static EdgeCache hzCache = null;
	private String cacheName = null;

	/**
	 * to be used for user cache
	 * @param cacheName - unique name of the cache 
	 * @throws CacheException
	 */
	public EdgeCache(String cacheName, IMap<Object, Object> cacheMap){
		this.cacheName = cacheName;
		this.cacheMap = cacheMap;
	}

	private IMap<Object, Object> getCacheMap()
	{
		return this.cacheMap;
	}
	
	public void put(final Object k, final Object element)
	{
		logger.info("start - cache: [{}]", getCacheName());
		try{
			getCacheMap().put(k, element);
		}catch(Exception e){
			logger.error(  "cache:[{}] put operation encountered exception:[{}] ", e.getMessage(), getCacheName());
		}
		logger.info("completed - cache: [{}],[{}],[{}]", getCacheName(),k,element);
		
	}
	
	public void put(final Object k, final Object element, int ttlInSecs)
	{
		logger.info("start - cache: [{}]", getCacheName());
		try{
			getCacheMap().put(k, element, ttlInSecs, TimeUnit.SECONDS);
		}catch(Exception e){
			logger.error(  "cache:[{}] put operation encountered exception:[{}] ", e.getMessage(), getCacheName());
		}
		logger.info("completed - cache: [{}]", getCacheName());
	}
	
	public Object get(final Object key)
	{
		logger.trace("start - cache: [{}] ,[{}]", getCacheName(), key);
		Object element = null;
		try {
			element = getCacheMap().get(key);
		} catch(HazelcastInstanceNotActiveException hze) {
			logger.error( "hazelcast is probably down!!!",hze);
		}
		
		logger.trace("completed - cache: [{}] ,[{}], [{}]", getCacheName(), key, element);
		return element;
	}

	
	public void remove(final Object key)
	{
		logger.info("start - cache: [{}] ,[{}]", getCacheName(), key);
		try {
			getCacheMap().remove(key);
		} catch(HazelcastInstanceNotActiveException hze) {
			logger.error( "hazelcast is probably down!!!",hze);
		}
		logger.info("completed- cache: [{}] ,[{}]", getCacheName(), key);
	}	
	
	/**
	 * Helper Method to remove a ServiceEndPoint from a list of ServiceEndPoints. 
	 * <p>
	 * In the event that no ServiceEndPoint exists, a log is printed and ServiceEndPoint removal is skipped.
	 * </p>
	 * @param List<ServiceEndPoint> that has an object to be removed
	 * @param value ServiceEndPoint object to remove from the cache.
	 */
	private List<ServiceEndPoint> removeEP(List<ServiceEndPoint> list, ServiceEndPoint epToRemove){
		Iterator iter = (Iterator) list.iterator();
		boolean found = false;
		while(iter.hasNext()){
			ServiceEndPoint iterSep = (ServiceEndPoint) iter.next();
			if(GRMEdgeUtil.checkEqualServiceEndPointPK(iterSep,(ServiceEndPoint)epToRemove)){
				iter.remove();
				found = true;
				break;
			}
		}
		if(!found){
			logger.info("Did not find EP {} to delete in cache. Skipping.",epToRemove.toString());
		}
		return list;
	}

	/**
	 * This method provides safe entry for removing multiple endpoints from the ENDPOINT CACHE <b>only</b>. It obtains a lock, removes each endpoint if it exists.
	 * Because it has a lock, it should be thread safe for multiple grm edge instances watching the API Server.
	 * 
	 * <p>
	 * In the event that no Endpoint exists, a log is printed and endpoint removal is skipped.
	 * <br>
	 * In the event that the cache no longer has entries after the removal, the key is removed.
	 * </p>
	 * @param key of the cache
	 * @param value List<ServiceEndPoint> object to remove from the cache.
	 */
	public void removeEndPoints(final String key, final List<ServiceEndPoint> epsToRemove) {
		logger.info("start - cache: [{}] ,[{}]", getCacheName(), key);
		try {
			boolean b = getCacheMap().tryLock(key,LOCKOUT_TIMEOUT_MS , TimeUnit.MILLISECONDS);
			if(b){
				logger.info("Acquired lock for key {}",key);
				List<ServiceEndPoint> seps = (List<ServiceEndPoint>) get(key);
				if(seps != null){
					for(ServiceEndPoint epToRemove: epsToRemove){
						seps = removeEP(seps, epToRemove);
					}
					if(seps.isEmpty()){
						logger.info("Empty cache for key {}. Removing this key from cache.",key);
						getCacheMap().remove(key);
					}
					else{
						getCacheMap().put(key, seps);
						logger.info("Removed endpoints from cache {}", key);
					}
				}
				else{
					logger.info("Unable to remove endpoints, the key {} contains no value",key);
				}
			}
			else{
				logger.warn("Unable to aquire lock for key: {}",key);
			}
		} catch(HazelcastInstanceNotActiveException hze) {
			logger.error( "hazelcast is probably down!!!",hze);
		} catch (Exception e) {
			logger.error( "Error while trying to removeEndPoint",e);
		} 
		
		logger.info("Releasing lock on key {} if obtained", key);
		try{
			getCacheMap().unlock(key);
		}
		catch(IllegalMonitorStateException e){
			logger.warn("Thread never obtained lock for key:{} so unable to unlock it",key);
		}
		
		logger.info("completed- cache: [{}] ,[{}]", getCacheName(), key);
		
	}
	
	/**
	 * This method provides safe entry for removing a single endpoint from the ENDPOINT CACHE <b>only</b>. It obtains a lock, removes an endpoint if it exist.
	 * Because it has a lock, it should be thread safe for multiple grm edge instances watching the API Server.
	 * 
	 * <p>
	 * In the event that no Endpoint exists, a log is printed and endpoint removal is skipped.
	 * <br>
	 * In the event that the cache no longer has entries after the removal, the key is removed.
	 * </p>
	 * @param key of the cache
	 * @param value ServiceEndPoint object to remove from the cache.
	 */
	public void removeEndPoint(final Object key, final Object value)
	{
		logger.info("start - cache: [{}] ,[{}]", getCacheName(), key);
		try {
			boolean b = getCacheMap().tryLock(key,LOCKOUT_TIMEOUT_MS , TimeUnit.MILLISECONDS);
			if(b){
				logger.info("Acquired lock for key {}",key);
				List<ServiceEndPoint> seps = (List<ServiceEndPoint>) get(key);
				if(seps != null){
					seps = removeEP(seps, (ServiceEndPoint) value);
					if(seps.isEmpty()){
						logger.info("Empty cache for key {}. Removing this key from cache.",key);
						getCacheMap().remove(key);
					}
					else{
						getCacheMap().put(key, seps);
						logger.info("Removed endpoints from cache {}", key);
					}
				}
				else{
					logger.info("Unable to remove endpoints, the key {} contains no value",key);
				}
			}
			else{
				logger.error("Unable to aquire lock for key: {}",key);
			}
		} catch(HazelcastInstanceNotActiveException hze) {
			logger.error( "hazelcast is probably down!!!",hze);
		} catch (Exception e) {
			logger.error( "Error while trying to removeEndPoint",e);
		}
		
		logger.info("Releasing lock on key {} if obtained", key);
		try{
			getCacheMap().unlock(key);
		}
		catch(IllegalMonitorStateException e){
			logger.warn("Thread never obtained lock for key:{} so unable to unlock it",key);
		}		
		logger.info("completed- cache: [{}] ,[{}]", getCacheName(), key);
	}

	/**
	 * This method provides safe entry for adding multiple endpoints with the same key to the ENDPOINT CACHE <b>only</b>. It obtains a lock, adds each endpoint if it does not exist.
	 * Because it has a lock, it should be thread safe for multiple grm edge instances watching the API Server.
	 * 
	 * <p>
	 * It should handle the case where the cache for this key doesn't exist by creating a new list to add to.
	 * </p>
	 * @param key of the cache
	 * @param value List<ServiceEndPoint> of new SEP objects to add to a cache.
	 */
	public void addEndPoints(final Object key, final Object value)
	{
		logger.info("start - cache: [{}] ,[{}]", getCacheName(), key);
		try {
			boolean b = getCacheMap().tryLock(key,LOCKOUT_TIMEOUT_MS , TimeUnit.MILLISECONDS);
			if(b){
				logger.info("Acquired lock for key {}",key);
				List<ServiceEndPoint> seps = (List<ServiceEndPoint>) get(key);
				boolean found = false;
				if(seps == null){
					seps = new ArrayList<>();
				}
				for(ServiceEndPoint sep: (List<ServiceEndPoint>) value){
					if(ObjectValidator.validate(sep)){
						Iterator iter = (Iterator) seps.iterator();
						while(iter.hasNext()){
							ServiceEndPoint iterSep = (ServiceEndPoint) iter.next();
							if(GRMEdgeUtil.checkEqualServiceEndPointPK(iterSep,sep)){
								found = true;
								logger.info("Cache already contains endpoint {}, not adding again",iterSep.toString());
								break;
							}
						}
						if (!found){
							seps.add(sep);
							logger.info("Added SEP to cache with serviceName: {}" , sep.getName());	
						}	
					}
				}
				getCacheMap().put(key, seps);
			}
			else{
				logger.warn("Unable to aquire lock for key: {}",key);
			}
		} catch(HazelcastInstanceNotActiveException hze) {
			logger.error( "hazelcast is probably down!!!",hze);
		} catch (Exception e) {
			logger.error( "Error while trying to removeEndPoint",e);
		}
		
		logger.info("Releasing lock on key {} if obtained", key);
		try{
			getCacheMap().unlock(key);
		}
		catch(IllegalMonitorStateException e){
			logger.warn("Thread never obtained lock for key:{} so unable to unlock it",key);
		}
		
		logger.info("completed- cache: [{}] ,[{}]", getCacheName(), key);
	}
	
	/**
	 * This method provides safe entry for adding endpoints to the ENDPOINT CACHE <b>only</b>. It obtains a lock, adds an endpoint if it does not exist.
	 * Because it has a lock, it should be thread safe for multiple grm edge instances watching the API Server.
	 * 
	 * <p>
	 * It should handle the case where the cache for this key doesn't exist by creating a new list to add to.
	 * </p>
	 * @param key of the cache
	 * @param value ServiceEndPoint object to add to a cache.
	 */
	public void addEndPoint(final Object key, final Object value)
	{
		logger.info("start - cache: [{}] ,[{}]", getCacheName(), key);
		if(ObjectValidator.validate((ServiceEndPoint) value)){
			try {
				boolean b = getCacheMap().tryLock(key,LOCKOUT_TIMEOUT_MS , TimeUnit.MILLISECONDS);
				if(b){
					logger.info("Acquired lock for key {}",key);
					List<ServiceEndPoint> seps = (List<ServiceEndPoint>) get(key);
					boolean found = false;
					if(seps == null){
						seps = new ArrayList<>();
					}
					Iterator iter = (Iterator) seps.iterator();
					while(iter.hasNext()){
						ServiceEndPoint iterSep = (ServiceEndPoint) iter.next();
						if(GRMEdgeUtil.checkEqualServiceEndPointPK(iterSep,(ServiceEndPoint)value)){
							found = true;
							logger.info("Cache already contains endpoint {}, not adding again",iterSep.toString());
							break;
						}
					}
					if (!found){
						seps.add((ServiceEndPoint)value);
						getCacheMap().put(key, seps);
						logger.info("Added SEP to cache with serviceName: {}" , ((ServiceEndPoint)value).getName());	
					}
				}
				else{
					logger.warn("Unable to aquire lock for key: {}",key);
				}
			} catch(HazelcastInstanceNotActiveException hze) {
				logger.error( "hazelcast is probably down!!!",hze);
			} catch (Exception e) {
				logger.error( "Error while trying to removeEndPoint",e);
			}
			
			logger.info("Releasing lock on key {} if obtained", key);
			try{
				getCacheMap().unlock(key);
			}
			catch(IllegalMonitorStateException e){
				logger.warn("Thread never obtained lock for key:{} so unable to unlock it",key);
			}
			
			logger.info("completed- cache: [{}] ,[{}]", getCacheName(), key);	
		}
	}

	public Set<Object> getKeySet() 
	{
		try {
			return getCacheMap()!=null?getCacheMap().keySet():null;
		} catch(HazelcastInstanceNotActiveException hze){
			logger.error( "hazelcast is probably down!!!",hze);
		}
		return new HashSet<>();
	}
	
	public Collection<Object> values()
	{
		try {
			return getCacheMap()!=null?getCacheMap().values():null;
		} catch(HazelcastInstanceNotActiveException hze){
			logger.error( "hazelcast is probably down!!!",hze);
		}
		return new HashSet<>();
	}
	
	public String getCacheName() {
		return this.cacheName;
	}

	public boolean isEmpty() {
		try {
			return getCacheMap().size()<= 0 ? true : false; 
		} catch(HazelcastInstanceNotActiveException hze){
			logger.error( "hazelcast is probably down!!!",hze);
		}
		return true;
	}

	public int size(){
		return getCacheMap().size();
	}
	
	public boolean tryLock(String keyToLock) throws InterruptedException {
		logger.info("Lock called on key: {}",keyToLock);
		return cacheMap.tryLock(keyToLock,LOCKOUT_TIMEOUT_MS,TimeUnit.MILLISECONDS);
	}
	
	public void unlock(String keyToUnlock){
		logger.info("Unlock called on key: {}",keyToUnlock);
		cacheMap.unlock(keyToUnlock);
	}
}