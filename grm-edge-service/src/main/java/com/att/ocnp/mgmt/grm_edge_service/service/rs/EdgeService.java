/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.ocnp.mgmt.grm_edge_service.service.rs;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.att.scld.grm.v1.FindRunningServiceEndPointResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/*
 * Interface for rest service. Prototypes of Add and Delete were added but were not used/supported. They are only used for testing. please don't use them.
 */
@Api(value = "/serviceEndPoint")
@Produces({MediaType.APPLICATION_JSON})
public interface EdgeService {
	
	/**
	 * Get Service Endpoints with a GET request
	 * 
	 * Should be used for debugging purposes
	 * 
	 * @param serviceName, leave blank for wildcard
	 * @param env, leave blank for wildcard
	 * @return list of service endpoints in form of FindRunningServiceEndPointResponse
	 */
	@GET
	@Path("/getFromCache")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get Endpoints From Cache",
	notes = "Returns a list of endpoints from the cache. This should be used for debugging purposes.",
	response = FindRunningServiceEndPointResponse.class
			)
	public FindRunningServiceEndPointResponse getFromCache(
			@DefaultValue("*") @QueryParam("serviceName") String serviceName,
			@DefaultValue("*") @QueryParam("env") String env);

	/**
	 * Get Service Endpoints by providing a JSON format in a POST request
	 * 
	 * Example:
	 * {
	 *     	"service":"Test",
	 *		"env":"DEV"
	 * }
	 *
	 *	@param JSON of service, env the endpoints reside in
	 *	@return list of service endpoints
	 */
	@POST
	@Path("/findAllEps")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Find All Endpoints",
	notes = "Returns a list of endpoints registered to the service requested",
	response = FindRunningServiceEndPointResponse.class
			)
	public FindRunningServiceEndPointResponse findAllEps(final String requestBodyJSON);

	/**
	 * Get Service Endpoints by providing a JSON format in a POST request
	 *
	 *	@param JSON of findRunningRequest
	 *	@return list of service endpoints
	 */
	@POST
	@Path("/findRunning")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get Find Running Endpoints",
	notes = "Returns a list of endpoints registered to the service requested",
	response = FindRunningServiceEndPointResponse.class
			)
	public FindRunningServiceEndPointResponse findRunning(final String requestBodyJSON);

	/**
	 * Allows you to add Pods and services to the cache using POST. Currently only Pods has been implemented.
	 * Provide JSON in the body of your request. 
	 *  
	 * This method is not supported. Please do not use it. It is for testcases only
	 * 
	 * @deprecated
	 * @param JSON supplying attributes required for a pod or service
	 * @return Success or Fail
	 */
	@POST
	@Path("/add")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces("text/plain")
	@ApiOperation(value = "Add",
	notes = "Add a new service or pod",
	response = String.class
			)
	public String add(final String requestBodyJSON);

	/**
	 * Allows you delete pods and services from the cache. Currently only deleting pods will work.
	 * 
	 * This method is not supported. Please do not use it. It is for testcases only
	 *
	 * @deprecated 
	 * @param JSON supplying attributes required for a pod or service
	 * @return Success or Fail
	 */
	@DELETE
	@Path("/delete")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces("text/plain")
	@ApiOperation(value = "Delete",
	notes = "Delete a service or pod from cache",
	response = String.class
			)
	public String delete(final String requestBodyJSON);
	
	@GET
	@Path("/test")
	@Produces("text/plain")
	public String dummy();
}