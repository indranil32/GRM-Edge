/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.ocnp.mgmt.grm_edge_service.APIServerWatcher;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.att.ocnp.mgmt.grm_edge_service.businessprocess.K8ServiceController;
import com.att.ocnp.mgmt.grm_edge_service.types.KubePod;
import com.att.ocnp.mgmt.grm_edge_service.util.GRMEdgeConstants;
import com.att.ocnp.mgmt.grm_edge_service.util.GRMEdgeErrorGenerator;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodCondition;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;

public class KubernetesPodAPIWatcher implements Runnable {

	private String apiUrl = "";
	private CountDownLatch latch;
	
	public KubernetesPodAPIWatcher(String url) {
		apiUrl = url;
	}

	final Logger logger = LoggerFactory.getLogger(KubernetesPodAPIWatcher.class.getName());
	
	/*
	 * This class handles watching pod events on the API server.
	 * I expect most operations to occur under MODIFIED ACTION. This is because create wont have enough information and modified will have deleted the pod before delete action occurs
	 */
	@Override
	public void run() {
		Thread.currentThread().setName("KubernetesPodAPIWatcher");
		boolean loop = true;
		while(loop){
			latch = new CountDownLatch(1);
			watcher();
			try {
				latch.await();
			} catch (InterruptedException e) {
				logger.error(e.getMessage());
			}
			String sleepForWatcherRetry = System.getProperty("CPFRUN_GRM_EDGE_WATCHER_RECONNECT_SLEEP","5000");
			logger.error("Found unhealthy APIWatcher on url: {}. Restarting in {} ms" , apiUrl, sleepForWatcherRetry);
			try{
				Thread.sleep(Integer.valueOf(sleepForWatcherRetry));
			}
			catch(Exception e){
				logger.error("Error while trying to sleep");
			}
			
		}
	}
	
	private void watcher() {
		Config config = new ConfigBuilder().withMasterUrl(apiUrl).withWatchReconnectInterval(0).withTrustCerts(true).withOauthToken(System.getProperty("GRM_EDGE_WATCHER_TOKEN", "3ftLdxGOFyNOWzDBv4k6gIWMLMtbnbN9")).withWatchReconnectLimit(-1).build();
		try (final KubernetesClient client = new DefaultKubernetesClient(config)) {
			Watch watch = null;
			try {
				/*
				 * Watches for podEvents on the API server
				 */
				logger.info("Starting up APIWatcher on url: {}", apiUrl);
				watch = client.pods().inAnyNamespace().watch(generateWatcher());
			} catch (Exception e) {
				if(watch != null){
					watch.close();
				}
				latch.countDown();
				KubernetesAPIWatcherControl.getInstance().setPodWatcherStatus(apiUrl,false);
				logger.error(GRMEdgeErrorGenerator.generateErrorMessage(GRMEdgeConstants.WATCHER_EXCEPTION, e.getMessage()),e);
			}
		} catch (Exception e) {
			latch.countDown();
			KubernetesAPIWatcherControl.getInstance().setPodWatcherStatus(apiUrl,false);
			logger.error(GRMEdgeErrorGenerator.generateErrorMessage(GRMEdgeConstants.WATCHER_EXCEPTION + " Unable to create kubernetesclient", e.getMessage()),e);
		}
		
	}

	private KubePod generateKubePod(Pod pod){
		Map<String, String> annotation = pod.getMetadata().getAnnotations() == null ? new HashMap<>() : pod.getMetadata().getAnnotations();
		Map<String, String> labels = pod.getMetadata().getLabels() == null ? new HashMap<>() : pod.getMetadata().getLabels();
		String namespace = pod.getMetadata().getNamespace() == null ? "default" : pod.getMetadata().getNamespace();
		return new KubePod(pod.getMetadata().getName(), namespace, labels, annotation, pod.getStatus().getHostIP());
	}
	
	private Watcher<Pod> generateWatcher(){
		KubernetesAPIWatcherControl.getInstance().setPodWatcherStatus(apiUrl,true);
		return new Watcher<Pod>() {
			@Override
			public void eventReceived(Action action, Pod pod) {
				
				MDC.put(GRMEdgeConstants.Tracking, UUID.randomUUID().toString());
				try {
					if (action.equals(Action.ADDED)) {
						logger.trace("Received Pod Add Event");
						/*
						 * This method probably will never run due to pods not being in the ready state when added, but just in case they are we want to capture it.
`						 */
						if (pod.getStatus().getPhase() != null && pod.getStatus().getPhase().equalsIgnoreCase(GRMEdgeConstants.KUBERNETES_RUNNING)) {								
							Iterator<PodCondition> iter = pod.getStatus().getConditions().iterator();
							while(iter.hasNext()){
								PodCondition condition = iter.next();
								if(condition.getType() != null && condition.getType().equalsIgnoreCase(GRMEdgeConstants.KUBERNETES_READY) && condition.getStatus() != null && condition.getStatus().equals(GRMEdgeConstants.KUBERNETES_TRUE)){
									//pod has to be in ready state for us to add it otherwise we don't care. this will help limit duplicate add pod calls.
									KubePod podToAdd = generateKubePod(pod);
									logger.info("Adding to grm-edge a new pod: {}" + podToAdd.toString());
									K8ServiceController.addPod(podToAdd);
									break;
								}										
							}
						}
					} else if (action.equals(Action.DELETED)) {
						/*
						 * Deletes a pod from the cache. The pod has probably already been deleted from modified events that happened prior but just in case it hasn't we still want this method
						 */
						KubePod podToDelete = generateKubePod(pod);
						logger.debug("Deleting pod: {}" + podToDelete.toString());
						K8ServiceController.deletePods(podToDelete);
					} else if (action.equals(Action.MODIFIED)){
						logger.debug("Received Modified Event, Pod Information: {}" , pod.toString());
						pod.setAdditionalProperty("apiURL", apiUrl);						
						if (pod.getStatus().getPhase() != null && pod.getStatus().getPhase().equalsIgnoreCase(GRMEdgeConstants.KUBERNETES_RUNNING) && pod.getMetadata() != null && (pod.getMetadata().getDeletionTimestamp() == null || pod.getMetadata().getDeletionTimestamp().isEmpty())) {
							Iterator<PodCondition> iter = pod.getStatus().getConditions().iterator();
							while(iter.hasNext()){
								PodCondition condition = iter.next();
								if(condition.getType() != null && condition.getType().equalsIgnoreCase(GRMEdgeConstants.KUBERNETES_READY) && condition.getStatus() != null && condition.getStatus().equals(GRMEdgeConstants.KUBERNETES_TRUE)){
									//pod has to be in ready state for us to add it otherwise we don't care. this will help limit duplicate add pod calls.
									KubePod podToAdd = generateKubePod(pod);
									logger.debug("Modified added to grm-edge a new pod: {}" , podToAdd.toString());
									K8ServiceController.addPod(podToAdd);	
									break;
								}										
							}
						} // we only want to delete it if it is scheduled for deletion
						else if(pod.getMetadata().getDeletionTimestamp() != null && !pod.getMetadata().getDeletionTimestamp().isEmpty()){
							KubePod podToDelete = generateKubePod(pod);
							logger.debug("Modified deleted from grm-edge a pod: {}" , podToDelete.toString());

							K8ServiceController.deletePods(podToDelete);
						}
					}
				} catch (Exception e) {
					logger.error(GRMEdgeErrorGenerator.generateErrorMessage(GRMEdgeConstants.WATCHER_EXCEPTION, e.getMessage()),e);
				}
			}

			@Override
			public void onClose(KubernetesClientException e) {
				if (e != null) {
					logger.error(GRMEdgeErrorGenerator.generateErrorMessage(GRMEdgeConstants.WATCHER_EXCEPTION, e.getMessage()),e);
				}
				else{
					logger.error(GRMEdgeErrorGenerator.generateErrorMessage(GRMEdgeConstants.WATCHER_EXCEPTION, "GRMEdge KubernetesPodAPIWatcher received valid shutdown but we are restarting it instead."));
				}
				//set Watcher flag to false
				KubernetesAPIWatcherControl.getInstance().setPodWatcherStatus(apiUrl,false);
				latch.countDown();
			}
		};
	}
}