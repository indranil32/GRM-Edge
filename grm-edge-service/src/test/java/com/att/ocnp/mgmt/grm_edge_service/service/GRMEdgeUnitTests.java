/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.ocnp.mgmt.grm_edge_service.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.att.ocnp.mgmt.grm_edge_service.util.ObjectValidator;
import com.att.scld.grm.types.v1.NameValuePair;
import com.att.scld.grm.types.v1.ServiceEndPoint;

public class GRMEdgeUnitTests {

	@Test
	public void testSEPObjectValidator(){
		String listenPort = "8080";
		String hostAddress = "localhost";
		String name = "test";
		String routeOffer = "TEST";
		List<NameValuePair> withoutEnv = new ArrayList<>();
		List<NameValuePair> withEmptyEnv = new ArrayList<>();
		List<NameValuePair> withEnv = new ArrayList<>();
		NameValuePair nvp = new NameValuePair();
		nvp.setName("Environment");
		withEmptyEnv.add(nvp);
		nvp = new NameValuePair();
		nvp.setName("Environment");
		nvp.setValue("TEST");
		withEnv.add(nvp);
		
		ServiceEndPoint sep = null;
		//null check
		assertFalse(ObjectValidator.validate(sep));
		//host address check
		assertFalse(ObjectValidator.validate(generateSEP(null,listenPort, routeOffer,name, withEnv)));
		assertFalse(ObjectValidator.validate(generateSEP("",listenPort, routeOffer,name, withEnv)));
		//port check
		assertFalse(ObjectValidator.validate(generateSEP(hostAddress,null, routeOffer,name, withEnv)));
		assertFalse(ObjectValidator.validate(generateSEP(hostAddress,"", routeOffer,name, withEnv)));
		//routeOffer Check
		assertFalse(ObjectValidator.validate(generateSEP(hostAddress,listenPort, null,name, withEnv)));
		assertFalse(ObjectValidator.validate(generateSEP(hostAddress,listenPort, "",name, withEnv)));
		//name check
		assertFalse(ObjectValidator.validate(generateSEP(hostAddress,listenPort, routeOffer,null, withEnv)));
		assertFalse(ObjectValidator.validate(generateSEP(hostAddress,listenPort, routeOffer,"", withEnv)));
		//properties
		assertFalse(ObjectValidator.validate(generateSEP(hostAddress,listenPort, routeOffer,name, null)));
		assertFalse(ObjectValidator.validate(generateSEP(hostAddress,listenPort, routeOffer,name, withoutEnv)));
		assertFalse(ObjectValidator.validate(generateSEP(hostAddress,listenPort, routeOffer,name, withEmptyEnv)));
		//valid check
		assertTrue(ObjectValidator.validate(generateSEP(hostAddress,listenPort, routeOffer,name, withEnv)));
	}

	private ServiceEndPoint generateSEP(String hostAddress, String listenPort, String routeOffer, String name, List<NameValuePair> properties){
		ServiceEndPoint sep = new ServiceEndPoint();
		sep.setHostAddress((String) hostAddress);
		sep.setListenPort((String) listenPort);
		sep.setRouteOffer(routeOffer);
		sep.setName(name);
		if(properties != null){
			for(NameValuePair nvp: properties){
				sep.getProperties().add(nvp);
			}
		}
		return sep;
	}
}
