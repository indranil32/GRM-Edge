/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.ocnp.mgmt.grm_edge_service;

import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health.Builder;
import org.springframework.stereotype.Component;

import com.att.ocnp.mgmt.grm_edge_service.APIServerWatcher.KubernetesAPIWatcherControl;

@Component
public class GRMEdgeWatcherHealth extends AbstractHealthIndicator {
	
	@Override
	protected void doHealthCheck(Builder builder) throws Exception {
		boolean allWatchersAlive = true;
		
		for(String url: KubernetesAPIWatcherControl.getInstance().getAllPodStatus().keySet()){
			boolean podStatus = KubernetesAPIWatcherControl.getInstance().getPodWatcherStatus(url);
			if(!podStatus){
				allWatchersAlive = false;
			}
			builder.withDetail("PodWatcher("+url+")", podStatus ? "UP" : "DOWN");
		}
		for(String url: KubernetesAPIWatcherControl.getInstance().getAllServiceStatus().keySet()){
			boolean serviceStatus = KubernetesAPIWatcherControl.getInstance().getServiceWatcherStatus(url);
			if(!serviceStatus){
				allWatchersAlive = false;
			}
			builder.withDetail("ServiceWatcher("+url+")", serviceStatus ? "UP" : "DOWN");
		}
		
		if(allWatchersAlive){
			builder.up();
		}
		else{
			builder.down();
		}
	}
}
