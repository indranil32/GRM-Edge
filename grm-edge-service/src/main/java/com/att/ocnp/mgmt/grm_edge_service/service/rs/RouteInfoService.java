/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.ocnp.mgmt.grm_edge_service.service.rs;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.att.scld.grm.v1.GetRouteInfoResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "/routeInfo")
@Produces({MediaType.APPLICATION_JSON})
public interface RouteInfoService {
	
	/**
	* Get RouteInfo XML by providing a JSON format in a POST request
	* 
	* Example:
	* {
	* 		"serviceVersionDefinition": {
	* 		"name": "com.att.aft.TestService",
	* 		"version": {
	* 			"major": 1,
	* 			"minor": 0,
	* 			"patch": "0"
	* 		}
	* 	},
	* 	"env": "LAB"
	* }
	* 
	* @param JSON of service version definition, and the env
	* @return RouteInfo XML defined for the service
	*/
	@POST
	@Path("/get")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get RouteInfo for a service",
	notes = "Returns the routeinfo for a service requested by DME",
	response = GetRouteInfoResponse.class)
	public GetRouteInfoResponse getRouteInfo(final String requestBodyJSON);
	
	/**
	* Get all RouteInfo XML in a GET request
	* 
	* @param No input needed
	* @return All RouteInfo XML defined
	*/
	@GET
	@Path("/getAll")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@ApiOperation(value = "Get all RouteInfo",
	notes = "Returns all the routeinfo in cache",
	response = String.class)
	public String getAllRouteInfo();

}