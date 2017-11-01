/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.ocnp.mgmt.grm_edge_service;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.att.ajsc.common.messaging.DateTimeParamConverterProvider;
import com.att.ajsc.common.messaging.LogRequestFilter;
import com.att.ajsc.common.messaging.ObjectMapperContextResolverNonNull;
import com.att.ajsc.common.messaging.TransactionIdResponseFilter;
import com.att.ocnp.mgmt.grm_edge_service.service.rs.EdgeServiceImpl;
import com.att.ocnp.mgmt.grm_edge_service.service.rs.LifecycleController;
import com.att.ocnp.mgmt.grm_edge_service.service.rs.ManagementServiceImpl;
import com.att.ocnp.mgmt.grm_edge_service.service.rs.RouteInfoServiceImpl;

@Component
@ApplicationPath("/")
public class JerseyConfiguration extends ResourceConfig {
	private static final Logger log = LoggerFactory.getLogger(JerseyConfiguration.class.getName());
	
	/*
	 * The rest interfaces have to be listed here for them to start up
	 */
	@Autowired
    public JerseyConfiguration(LogRequestFilter lrf) {
    	register(new ObjectMapperContextResolverNonNull());
        register(EdgeServiceImpl.class);
        register(RouteInfoServiceImpl.class);
        register(ManagementServiceImpl.class);
        register(MDCFilter.class);
        register(LifecycleController.class);
        property(ServletProperties.FILTER_FORWARD_ON_404, true);
        register(TransactionIdResponseFilter.class, 6001);
        register(DateTimeParamConverterProvider.class);
        register(lrf, 6002);
      //  register(new EdgeLoggingFilter(log, true));
    }
}