/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.ocnp.mgmt.grm_edge_service;

import java.io.IOException;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;

import org.slf4j.MDC;

import com.att.ocnp.mgmt.grm_edge_service.util.GRMEdgeConstants;

@Priority(Integer.MAX_VALUE)
public class MDCFilter implements ContainerRequestFilter {

	@Override
	public void filter(ContainerRequestContext arg0) throws IOException {
			MDC.put(GRMEdgeConstants.Tracking,arg0.getHeaderString("X-ATT-TransactionId"));
	}
	
}