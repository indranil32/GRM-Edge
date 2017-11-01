/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.ocnp.mgmt.grm_edge_service.types;

import java.io.Serializable;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Object to hold services from kubernetes.
 */
public class K8Service implements Serializable{

	private static final Logger logger = LoggerFactory.getLogger(K8Service.class.getName());
	
	private static final long serialVersionUID = 1;

	private Map<String, String> metadata;
	private String nodePort;
	private String protocol;
	private Map<String, String> labelSelector;
	
	public K8Service(Map<String, String> metadata, String nodePort, String protocol, Map<String, String> labelSelector){
		this.metadata = metadata;
		this.nodePort = nodePort;
		this.protocol = protocol;
		this.labelSelector = labelSelector;
	}
	
	public void setNodePort(String nodePort){
		this.nodePort = nodePort;
	}
	
	public void setLabelSelector(Map<String, String> labelSelector){
		this.labelSelector = labelSelector;
	}
	
	public void setMetadata(Map<String, String> metadata){
		this.metadata = metadata;
	}
	
	public void setProtocol(String protocol){
		this.protocol = protocol;
	}
	
	@Override
	public String toString() { 
	    return "metadata: '" + this.metadata + "', nodePort: '" + this.nodePort + "', protocol: '" + this.protocol + "', labelSelector: '" + this.labelSelector;
	} 
	
	public Map<String, String> getMetadata(){
		return metadata;
	}
	
	public String getNodePort(){
		return nodePort;
	}
	
	public String getProtocol(){
		return protocol;
	}
	
	public Map<String, String> getLabelSelector(){
		return labelSelector;
	}
}