/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.ocnp.mgmt.grm_edge_service.types;

import java.io.Serializable;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Object to hold pods from kubernetes. 
 */
public class KubePod implements Serializable {
	
	private static final long serialVersionUID = 1;

	private String name;
	private Map<String, String> labels;
	private Map<String, String> annotations;
	private String hostIP;
	private String namespace;
	
	private static final Logger logger = LoggerFactory.getLogger(KubePod.class.getName());
	
	public KubePod(String name, String namespace, Map<String, String> labels, Map<String,String> annotations, String hostIP){
		this.name = name;
		this.namespace=namespace;
		this.labels = labels;
		this.annotations = annotations;
		this.hostIP = hostIP;
	}
	
	@Override
	public String toString(){
		return "name: '" + this.name + "', namespace: '"+this.namespace+"' labels: '" + this.labels + "', annotations: '" + this.annotations + "', hostIP: '" + this.hostIP;
	}
	
	public String getName(){
		return name;
	}
	
	public String getNamespace(){
		return namespace;
	}
	
	public Map<String, String> getLabels(){
		return labels;
	}
	
	public Map<String, String> getAnnotations(){
		return annotations;
	}
	
	public String getHostIP(){
		return hostIP;
	}
}