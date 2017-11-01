/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.ocnp.mgmt.grm_edge_service.businessprocess;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.att.ocnp.mgmt.grm_edge_service.APIServerWatcher.KubernetesAPIWatcherControl;
import com.att.ocnp.mgmt.grm_edge_service.APIServerWatcher.KubernetesIngressObjectWatcher;
import com.att.ocnp.mgmt.grm_edge_service.APIServerWatcher.KubernetesPodAPIWatcher;
import com.att.ocnp.mgmt.grm_edge_service.APIServerWatcher.KubernetesServiceAPIWatcher;
import com.att.ocnp.mgmt.grm_edge_service.types.ManagedThread;
import com.att.ocnp.mgmt.grm_edge_service.util.GRMEdgeConstants;

public class APIWatcherAndThreadSupervisor implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(EdgeGRMHelper.class);
	
	private ExecutorService executor;
	private List<ManagedThread> watchedThreads;
		
	/**
	 * Starts up the APIWatchers and CacheSynchronizer. CacheSynchronizer gets added to a list of managed threads we watch while the APIWatchers
	 * are controlled by the KubernetesAPIWatcherControl class variables. The reason we can't manage the thread's directly is because they get spun up in a separate thread by kubernetes
	 * and our threads will end immediately.
	 * 
	 * @param urls
	 */
	public APIWatcherAndThreadSupervisor(List<String> urls){
		Thread.currentThread().setName("ThreadSupervisor");
		
		watchedThreads = new ArrayList<>();
		long startTime = System.currentTimeMillis();
	
		if(System.getProperty("CPFRUN_GRMEDGE_ENABLE_API_WATCHERS","true").equalsIgnoreCase("true")){
			logger.info("Starting Kubernetes Watcher for Pods");
			for(String aUrl: urls){
				Thread r = new Thread(new KubernetesPodAPIWatcher(aUrl));
				r.start();
			}
			logger.info("Kubernetes Watcher for Pods Started. Elapsed ms={}" , System.currentTimeMillis() - startTime);
			
			logger.info("Starting Kubernetes Watcher for Services");
			startTime = System.currentTimeMillis();
			for(String aUrl: urls){
				Thread r = new Thread(new KubernetesServiceAPIWatcher(aUrl));
				r.start();
			}

			logger.info("Kubernetes Watcher for Services Started. Elapsed ms={}" , System.currentTimeMillis() - startTime);
		}

		this.executor = Executors.newFixedThreadPool(1);
		logger.info("Starting CacheSynchronizer");
		startTime = System.currentTimeMillis();
		Runnable r = new CacheSynchronizer();
		watchedThreads.add(new ManagedThread(r,executor.submit(r),null));
		executor.execute(new CacheSynchronizer());
		logger.info("CacheSynchronizer started. Elapsed ms={}" , (System.currentTimeMillis() - startTime));		
	}
	
	/**
	 * Tries to maintain and restart any threads it is managing. At this point in time, it is just CacheSyncronizer. It has the ability to pass in String paramaters to the constructor of the thread.
	 * 
	 * Additionally, it will monitor the KubernetesAPIWatchers based on the KubernetesAPIWatcherControl variables.
	 * If the variable is set to false, it will start up a new thread.
	 */
	void watchThreads(){
		for(ManagedThread thread: watchedThreads){
			if(thread.getFuture().isDone()){
				logger.error("Found an unhealthy Edge Thread: {}",thread.getRunnable().getClass().getName());
				try{
					Class<?> clazz = Class.forName(thread.getRunnable().getClass().getName());
					Constructor<?> ctor;
					Runnable r;
					if(thread.getParams() != null){
						ctor = clazz.getConstructor(String.class);
						r = (Runnable) ctor.newInstance(new Object[] { thread.getParams() });
					}
					else{
						ctor = clazz.getConstructor();
						r = (Runnable) ctor.newInstance(new Object[] {});
					}
					thread.setRunnable(r);
					thread.setFuture(executor.submit(r));
					logger.info("Restarted Edge Thread {}",thread.getRunnable().getClass().getName());
				}
				catch(Exception e){
					logger.error("Error while trying to recreate unhealthy Edge Thread",e);
				}
			}
		}
		
		for(String url: KubernetesAPIWatcherControl.getInstance().getAllIngressStatus().keySet()){
			if(!KubernetesAPIWatcherControl.getInstance().getIngressStatusWatcherStatus(url)){
				logger.error("Found an unhealthy KubernetesIngressObjectWatcher Thread");
				//service watcher needs to be restarted
				logger.info("Starting Kubernetes Watcher for Ingress Objects");
				KubernetesAPIWatcherControl.getInstance().setIngressWatcherStatus(url,true);
				long startTime = System.currentTimeMillis();
				Runnable r = new KubernetesIngressObjectWatcher(url);
				executor.submit(r);
				logger.info("Kubernetes Watcher for Ingress Objects Started. Elapsed ms={}" , System.currentTimeMillis() - startTime);
			}
		}	
	}

	@Override
	public void run() {
		boolean runForever = true;
		while(runForever){
			try{

				try {
					Thread.sleep(Integer.parseInt(System.getProperty("PLATFORM_RUNTIME_KUBERNETESSUPERVISOR_THREAD_SLEEP",GRMEdgeConstants.CPFRUN_GRMEDGE_DEFAULT_KUBERNETESSUPERVISOR_THREAD_SLEEP)));
				} catch (InterruptedException e) {
					logger.error("Unable to sleep for some reason",e);
				}catch(NumberFormatException e){
					try{
						logger.warn("Unable to sleep the requested amount. Trying to sleep 5000 instead");
						Thread.sleep(5000);
					}
					catch(InterruptedException ex){
					logger.error("Unable to sleep for some reason",ex);
					}
				}
				watchThreads();	
			}
			catch(Throwable e){
				logger.error("Exception found: " ,e);
			}
		}
	}
}
