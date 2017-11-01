/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.ocnp.mgmt.grm_edge_service.service.rs;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.StringTokenizer;

import javax.ws.rs.Path;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import com.att.ocnp.mgmt.grm_edge_service.cache.ServiceRegistry;
import com.att.ocnp.mgmt.grm_edge_service.util.GRMEdgeConstants;
import com.att.ocnp.mgmt.grm_edge_service.util.GRMEdgeUtil;

@Controller
@Path(value = "/management")
public class ManagementServiceImpl implements ManagementService {

	private static final Logger logger = LoggerFactory.getLogger(ManagementServiceImpl.class.getName());
	
	static{
		logger.debug("ManagementServiceImpl started up.");
	}
	
	@Override
	public Response refreshCacheWithAPIServer(HttpHeaders headers) {
		
		if(System.getProperty("CPFRUN_GRMEDGE_GRM_WRITE_BEHIND_ENABLE","False").equalsIgnoreCase("True") || System.getProperty("CPFRUN_GRMEDGE_DB_WRITE_BEHIND_ENABLE","False").equalsIgnoreCase("True")
				|| !ServiceRegistry.getInstance().getCache(GRMEdgeConstants.OLD_ENDPOINT_CACHE).isEmpty() || !ServiceRegistry.getInstance().getCache(GRMEdgeConstants.OLD_ENDPOINT_CASS_CACHE).isEmpty()){
			//This means that write behind is enabled or was enabled at start of the application. We Do not want to run this because it could be deleting endpoints taht exist in other clusters, obtained from synchronization
			return Response.status(Status.INTERNAL_SERVER_ERROR).type(MediaType.TEXT_PLAIN).entity("Not Allowing This Call. Write behind is or was enabled for GRM Edge.").build();
		}
		
		String authorization = null;
		 //Fetch authorization header
        try{
        	authorization = headers.getRequestHeader("Authorization").get(0);
        }
        catch(Exception e){
        	
        }
          
        //If no authorization information present; block access
        if(authorization == null || authorization.isEmpty())
        {
           return Response.status(Status.UNAUTHORIZED).type(MediaType.TEXT_PLAIN).entity("No Authorization Header was supplied!").build();
        }
          
        //Get encoded username and password
        final String encodedUserPassword = authorization.replaceFirst("Basic" + " ", "");           
        //Decode username and password
        String usernameAndPassword = new String(Base64.getDecoder().decode(encodedUserPassword.getBytes()));
        String username = null;
        String password = null;
        try{
        //Split username and password tokens
        final StringTokenizer tokenizer = new StringTokenizer(usernameAndPassword, ":");
         username = tokenizer.nextToken();
         password = tokenizer.nextToken();
        }
        catch(Exception e){
        	return Response.status(Status.UNAUTHORIZED).type("text/plain").entity("Invalid Username and Password supplied").build();
        }
        if(username.equals("admin") && password.equals("ocnpmgmt1")){
        	//valid credentials
        	List<String> urls = new ArrayList<>();
        	try{
        		urls = GRMEdgeUtil.getAPIServerUrls();
        	}
        	catch(Exception e){
        		return Response.status(Status.INTERNAL_SERVER_ERROR).type("text/plain").entity("Unable to parse API urls. Not Continuing" + e.getMessage()).build();
        	}
        	try{
        		GRMEdgeUtil.replaceCache(urls);	
        	}
        	catch(Exception e){
        		return Response.status(Status.INTERNAL_SERVER_ERROR).type("text/plain").entity("Error replacing Cache. This is not good. Please alert someone on the Platform Runtime team." + e.getMessage()).build();
        	}
        	
    		return Response.status(Status.OK).type(MediaType.TEXT_PLAIN).entity("Cache was refreshed successfully!").build();
        }

    	return Response.status(Status.UNAUTHORIZED).type("text/plain").entity("Invalid Username and Password supplied").build();
	}

}
