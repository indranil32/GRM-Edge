/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.ocnp.mgmt.grm_edge_service.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.att.scld.grm.types.v1.NameValuePair;
import com.att.scld.grm.types.v1.ServiceEndPoint;

public class ObjectValidator {

	  private static final Logger logger = LoggerFactory.getLogger(ObjectValidator.class);
	
	/**
	 * This validation checks for the following:
	 * <br>
	 * <ul>
	 * <li>attribute: hostAddress</li>
	 * <li>attribute: listenPort</li>
	 * <li>attribute: routeOffer</li>
	 * <li>attribute: name</li>
	 * <li>property: Environment</li> 
	 * </ul>
	 * 
	 * Returns false if any of these are null or empty. Will also return false if the input is null
	 * 
	 * @param sep
	 * @return boolean
	 */
	public static boolean validate(ServiceEndPoint sep){
		if(sep == null){	
			logger.warn("SEP Failed Validation. SEP is null");
			return false;
		}
		if(StringUtils.isEmpty(sep.getHostAddress())){
			logger.warn("SEP Failed Validation. Host Address is missing");
			return false;
		}
		if(StringUtils.isEmpty(sep.getListenPort())){
			logger.warn("SEP Failed Validation. Listen Port is missing");
			return false;
		}
		if(StringUtils.isEmpty(sep.getRouteOffer())){
			logger.warn("SEP Failed Validation. RouteOffer is missing");
			return false;
		}
		if(StringUtils.isEmpty(sep.getName())){
			logger.warn("SEP Failed Validation. ServiceName is missing");
			return false;
		}
		if(sep.getProperties() == null){
			logger.warn("SEP Failed Validation. Properties missing");
			return false;
		}
		else{
			boolean found = false;
			for(NameValuePair nvp: sep.getProperties()){
				if(nvp.getName().equals("Environment") && !StringUtils.isEmpty(nvp.getValue())){
					found = true;
					break;
				}
			}
			if(!found){
				logger.warn("SEP Failed Validation. Environment property is missing");
				return false;
			}
		}
		return true;
	}
	
	/*
	 * 
	 * We will add these eventually
	public static void validate(KubePod pod){
		
	}
	
	public static void validate(K8Service service){
		
	}
	*/
}
