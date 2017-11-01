/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.ocnp.mgmt.grm_edge_service.service.rs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.att.ocnp.mgmt.grm_edge_service.Application;
import com.att.ocnp.mgmt.grm_edge_service.businessprocess.K8ServiceController;
import com.att.ocnp.mgmt.grm_edge_service.cache.RouteInfoCache;
import com.att.ocnp.mgmt.grm_edge_service.cache.ServiceRegistry;
import com.att.ocnp.mgmt.grm_edge_service.types.KubePod;
import com.att.ocnp.mgmt.grm_edge_service.util.GRMEdgeConstants;

/*
 * LifecycleController is used to add a shutdownhook into grmedge. The built in k8 preStop hook does not work with https endpoints so that was unable to be used.
 * We need a shutdown hook so that a grmedge can de-register itself from grm and there is not an empty endpoint floating out there.
 */
@Path("/lifecycle/shutdownhook")
@Controller
public class LifecycleController {
	
	private static final Logger logger = LoggerFactory.getLogger(Application.class.getName());
	
	static{
		logger.debug("LifecycleController started up.");
	}
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@ResponseStatus(value = HttpStatus.OK)
	public void shutdownHook() throws IOException{
		logger.trace("Shutdown hook activated. Waiting for GRMEdge to be deleted from SEP Cache");
		
		File file = new File("/etc/hostname");
		String hostname = null;
		try(FileInputStream in = new FileInputStream(file);BufferedReader br = new BufferedReader(new InputStreamReader(in)) ) {
			String line = null;
			while ((line = br.readLine()) != null) {
				hostname = line;
			}
		} catch (Exception e) {
			logger.error("Cannot load hostname from /etc/hostname, grmedge will now exit ungracefully.", e);
		}
		
		if(hostname != null){			
			KubePod podToDelete = null;

			Iterator podIter = ServiceRegistry.getInstance().getCache(GRMEdgeConstants.POD_CACHE).values().iterator();
			while(podIter.hasNext()){
				KubePod pod = (KubePod) podIter.next();
				if(pod.getName() != null && pod.getName().contains("grm-edge-") && pod.getLabels()!=null&& pod.getLabels().get("app") != null && pod.getLabels().get("app").equalsIgnoreCase("grm-edge")){
					if(pod.getName().contains(hostname)){
						//delete the pod from cache that contains this grmedge instance
						podToDelete = pod;
					}
				}
			}
			
			logger.trace("Deleting grmedge pod");
			if (podToDelete != null){
				K8ServiceController.deletePods(podToDelete);	
			}
			else{
				logger.trace("Pod has already been deleted previously");
			}
			
			//delete pods should delete service if that was the last pod
			//sleep 3 seconds to hope changes are propagated to GRM
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				logger.error("Sleep InterruptedException, grmedge will now exit ungracefully.", e);

			}			
		}
		
		logger.trace("Disconnecting from Hazelcast Cluster");
		ServiceRegistry.getInstance().stopProcessing();
		
		logger.trace("Shutting Down Routeinfo XML Refresh Timer");
		if (RouteInfoCache.getInstance().getRefreshService() != null) {
			try {
				RouteInfoCache.getInstance().getRefreshService().shutdownNow();
			} catch (Exception e) {
				
			}
		}
		
		//need to sleep so the changes get sent to grm rest if enabled
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
		
		}

		logger.trace("ShutdownHook Completed!");
	} 
}
