/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.ocnp.mgmt.grm_edge_service.APIServerWatcher;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class KubernetesAPIWatcherControl {

	private static KubernetesAPIWatcherControl instance = null;

	private ConcurrentHashMap<String, Boolean> podStatus;
	private ConcurrentHashMap<String, Boolean> serviceStatus;
	private ConcurrentHashMap<String, Boolean> ingressStatus;
	
	/**
	 * Constructor for KubernetesAPIWatcherControl.
	 * Inits watcher hashmaps to new HashMap Objects.
	 */
	private KubernetesAPIWatcherControl(){
		podStatus = new ConcurrentHashMap<>();
		serviceStatus = new ConcurrentHashMap<>();
		ingressStatus = new ConcurrentHashMap<>();
	}
	
	/**
	 * Gets all the pod watcher status by passing the hashmap to the caller.
	 * 
	 * @return
	 */
	public Map<String, Boolean> getAllPodStatus(){
		return podStatus;
	}

	/**
	 * Gets all the pod watcher status by passing the hashmap to the caller.
	 * 
	 * @return
	 */
	public Map<String, Boolean> getAllIngressStatus(){
		return ingressStatus;
	}
	
	/**
	 * Gets all the service watcher status by passing the hashmap to the caller.
	 * 
	 * @return
	 */
	public Map<String, Boolean> getAllServiceStatus(){
		return serviceStatus;
	}

	
	/**
	 * Synchronized method that sets the pod watcher on the ip address to the boolean specified. If the IP Address does not exist in our map, method will add it.
	 * @param alive
	 */
	public synchronized void setPodWatcherStatus(String ipaddress, boolean alive){
		podStatus.put(ipaddress, alive);
	}
	
	/**
	 * Synchronized method that sets the service watcher on the ip address to the boolean specified. If the IP Address does not exist in our map, method will add it.
	 * @param alive
	 */
	public synchronized void setServiceWatcherStatus(String ipaddress, boolean alive){
		serviceStatus.put(ipaddress, alive);	
	}
	
	/**
	 * Synchronized method that sets the ingress watcher on the ip address to the boolean specified. If the IP Address does not exist in our map, method will add it.
	 * @param alive
	 */
	public synchronized void setIngressWatcherStatus(String ipaddress, boolean alive){
		ingressStatus.put(ipaddress, alive);	
	}

	/**
	 * Retrieves the podWatcherUp status on the ip address. Returns false if value is not in hashmap
	 * @return
	 */
	public boolean getPodWatcherStatus(String ipaddress){
		return podStatus.get(ipaddress) == null ? false : podStatus.get(ipaddress);
	}
	
	/**
	 * Retrieves the serviceWatcherUp status on the ip address. Returns false if value is not in hashmap
	 * 
	 * @return
	 */
	public boolean getServiceWatcherStatus(String ipaddress){
		return serviceStatus.get(ipaddress) == null ? false : serviceStatus.get(ipaddress);
	}
	
	/**
	 * Retrieves the IngressStatus status on the ip address. Returns false if value is not in hashmap
	 * 
	 * @return
	 */
	public boolean getIngressStatusWatcherStatus(String ipaddress){
		return ingressStatus.get(ipaddress) == null ? false : ingressStatus.get(ipaddress);
	}
	
	/**
	 * Singleton for KubernetesAPIWatcherControl. This will create a new instance if it does not exist.
	 * Upon creation, it will set the pod and service watcher status's to false. They need to be set by the Watchers themselves.
	 * 
	 * @return KubernetesAPIWatcherControl
	 */
	 
	public static KubernetesAPIWatcherControl getInstance(){
		if(instance == null){
			instance = new KubernetesAPIWatcherControl();
		}
		return instance;
	}
	
}
