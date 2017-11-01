/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.ocnp.mgmt.grm_edge_service.service.rs;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/*
 * Interface for management service. This class will manage the application
 * 
 */

@Api(value = "/management")
@Produces({MediaType.TEXT_PLAIN})
public interface ManagementService {

	/**
	 * reload Cache from API server GET request
	 * 
	 *
	 * @return Response object
	 */
	@GET
	@Path("/refreshCacheWithAPIServer")
	@Produces(MediaType.TEXT_PLAIN)
	@ApiOperation(value = "Refresh the Cache with Data from the API Server",
	notes = "Run this method if you are missing endpoints that should be there from the API server. This indicates an issue has occured with GRMEdge and should be reported. Running this method "
			+ "should resolve your issue at least temporarily.",
	response = String.class
			)
	public Response refreshCacheWithAPIServer(
			@Context HttpHeaders headers);	
}
