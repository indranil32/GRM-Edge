/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.ocnp.mgmt.grm_edge_service.APIServerWatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.att.ocnp.mgmt.grm_edge_service.businessprocess.DMEIngressObjectController;
import com.att.ocnp.mgmt.grm_edge_service.util.GRMEdgeConstants;
import com.att.ocnp.mgmt.grm_edge_service.util.GRMEdgeErrorGenerator;
import com.att.scld.grm.types.v1.DMEIngressMappingObject;
import com.google.common.base.Splitter;

import io.fabric8.kubernetes.api.model.extensions.HTTPIngressPath;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.kubernetes.api.model.extensions.IngressRule;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;

public class KubernetesIngressObjectWatcher implements Runnable {
	
	private static String apiUrl = "";

	public KubernetesIngressObjectWatcher(String url) {
		apiUrl = url;
	}

	/*
	 * This class handles watching Ingress Object events on the API server.
	 * I expect most operations to occur under ADDED and DELETED ACTION.
	 */
	@Override
	public void run() {
		
		final Logger logger = LoggerFactory.getLogger(KubernetesIngressObjectWatcher.class.getName());

		String master = apiUrl;

		Config config = new ConfigBuilder().withMasterUrl(master).withWatchReconnectInterval(0).withTrustCerts(true).withOauthToken(System.getProperty("GRM_EDGE_WATCHER_TOKEN", "3ftLdxGOFyNOWzDBv4k6gIWMLMtbnbN9")).withWatchReconnectLimit(-1).build();
		
		try (final KubernetesClient client = new DefaultKubernetesClient(config)) {
			Watch watch = null;
			try {
				/*
				 * Watches for Ingress Object events on the API server
				 */
				watch = client.extensions().ingresses().inAnyNamespace().watch(new Watcher<Ingress>() {
					
					@Override
					public void eventReceived(Action action, Ingress ingress) {
						MDC.put(GRMEdgeConstants.Tracking, UUID.randomUUID().toString());
						try {
							DMEIngressObjectController controller = new DMEIngressObjectController();
							if (ingress.getMetadata().getName().equalsIgnoreCase(System.getProperty("PLATFORM_RUNTIME_DME_INGRESS_NAME", "dmeingressobject"))) {
								if (action.equals(Action.ADDED)) {
									logger.info("Received Ingress Add Event");
									logger.trace("Ingress Information: " + ingress.toString());
									List<DMEIngressMappingObject> listOfObjectsToRegister = new ArrayList<>();
									for (IngressRule rule : ingress.getSpec().getRules()) {
										for (HTTPIngressPath path : rule.getHttp().getPaths()) {
											List<String> elementsInPath = Splitter.on(":").splitToList(path.getPath());
											if (elementsInPath.size() != 3 || elementsInPath.get(0).isEmpty() || elementsInPath.get(1).isEmpty() || elementsInPath.get(2).isEmpty()) {
												logger.error(GRMEdgeErrorGenerator.generateErrorMessage(GRMEdgeConstants.WATCHER_EXCEPTION, "Ingress Object Format Incorrect."));
											} else {
												DMEIngressMappingObject aObjectToRegister = new DMEIngressMappingObject();
												aObjectToRegister.setIngressContextPath(elementsInPath.get(0));
												aObjectToRegister.setServiceName(path.getBackend().getServiceName());
												aObjectToRegister.setVersion(elementsInPath.get(1));
												aObjectToRegister.setBaseContext(elementsInPath.get(2));
												listOfObjectsToRegister.add(aObjectToRegister);
											}
										}
									}
									if (!listOfObjectsToRegister.isEmpty()) {
										controller.addIngresses(listOfObjectsToRegister);
									}
								} else if (action.equals(Action.DELETED)) {
									logger.info("Received Ingress Deleted Event");
									logger.trace("Ingress Information: " + ingress.toString());
									List<DMEIngressMappingObject> listOfObjectsToDelete = new ArrayList<DMEIngressMappingObject>();
									for (IngressRule rule : ingress.getSpec().getRules()) {
										for (HTTPIngressPath path : rule.getHttp().getPaths()) {
											List<String> elementsInPath = Splitter.on(":").splitToList(path.getPath());
											if (elementsInPath.size() != 3 || elementsInPath.get(0).isEmpty() || elementsInPath.get(1).isEmpty() || elementsInPath.get(2).isEmpty()) {
												logger.error(GRMEdgeErrorGenerator.generateErrorMessage(GRMEdgeConstants.WATCHER_EXCEPTION, "Ingress Object Format Incorrect."));
											} else {
												DMEIngressMappingObject aObjectToDelete = new DMEIngressMappingObject();
												aObjectToDelete.setIngressContextPath(elementsInPath.get(0));
												aObjectToDelete.setServiceName(path.getBackend().getServiceName());
												aObjectToDelete.setVersion(elementsInPath.get(1));
												aObjectToDelete.setBaseContext(elementsInPath.get(2));
												listOfObjectsToDelete.add(aObjectToDelete);
											}
										}
									}
									if (!listOfObjectsToDelete.isEmpty()) {
										controller.deleteIngresses(listOfObjectsToDelete);
									}
								} else {
									logger.info("Received Unrecognized Ingress Event: " + action.toString());
									logger.trace("Ingress Information: " + ingress.toString());
								}
							}
						} catch (Exception e) {
							logger.error(GRMEdgeErrorGenerator.generateErrorMessage(GRMEdgeConstants.WATCHER_EXCEPTION, e.getMessage()));
							e.printStackTrace();
						}
					}
					
					@Override
					public void onClose(KubernetesClientException e) {
						if (e != null) {
							logger.error(GRMEdgeErrorGenerator.generateErrorMessage(GRMEdgeConstants.WATCHER_EXCEPTION, e.getMessage()));
							e.printStackTrace();
						}
						KubernetesAPIWatcherControl.getInstance().setIngressWatcherStatus(apiUrl,false);
					}
					
				});
			} catch (Exception e) {
				logger.error(GRMEdgeErrorGenerator.generateErrorMessage(GRMEdgeConstants.WATCHER_EXCEPTION, e.getMessage()));
				if(watch != null)
					watch.close();
				KubernetesAPIWatcherControl.getInstance().setIngressWatcherStatus(apiUrl,false);
				e.printStackTrace();
			}
		} catch (Exception e) {
			logger.error(GRMEdgeErrorGenerator.generateErrorMessage(GRMEdgeConstants.WATCHER_EXCEPTION, e.getMessage()));
			e.printStackTrace();
			KubernetesAPIWatcherControl.getInstance().setIngressWatcherStatus(apiUrl,false);
		}
	}
}