package com.att.ocnp.mgmt.grm_edge_service;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import com.att.ocnp.mgmt.grm_edge_service.APIServerWatcher.KubernetesAPIWatcherControl;

@Component
public class GRMEdgeWatcherHealthIndicator implements HealthIndicator {
	
	@Override
	public Health health() {
		Health.Builder builder = new Health.Builder();
		int count = 0;
		boolean allWatchersAlive = true;
		
		for(String url: KubernetesAPIWatcherControl.getInstance().getAllPodStatus().keySet()){
			boolean podStatus = KubernetesAPIWatcherControl.getInstance().getPodWatcherStatus(url);
			if(!podStatus){
				allWatchersAlive = false;
			}
			count++;
			builder.withDetail("PodWatcher("+url+")", podStatus ? "UP" : "DOWN");
		}
		for(String url: KubernetesAPIWatcherControl.getInstance().getAllServiceStatus().keySet()){
			boolean serviceStatus = KubernetesAPIWatcherControl.getInstance().getServiceWatcherStatus(url);
			if(!serviceStatus){
				allWatchersAlive = false;
			}
			count++;
			builder.withDetail("ServiceWatcher("+url+")", serviceStatus ? "UP" : "DOWN");
		}
		
		if(System.getProperty("CPFRUN_GRM_EDGE_HEALTH_CHECK_THREAD_NAMES","true").equalsIgnoreCase("true")){
			/*now lets check thread names to double check the threads are up. This is because since we don't control the threads per say, they have crashed while our status is green
			I will be using the enumerate method on this page https://stackoverflow.com/questions/1323408/get-a-list-of-all-threads-currently-running-in-java
			The reason for this being used rather than the top answer is performance. 
			Since this is a monitoring service and we can't control how often it is called, we want the least expensive operation
			*/
			ThreadGroup rootGroup = Thread.currentThread( ).getThreadGroup( );
			ThreadGroup parentGroup;
			while ( ( parentGroup = rootGroup.getParent() ) != null ) {
			    rootGroup = parentGroup;
			}
			Thread[] threads = new Thread[ rootGroup.activeCount() ];
			while ( rootGroup.enumerate( threads, true ) == threads.length ) {
			    threads = new Thread[ threads.length * 2 ];
			}
			
			int actualCount = 0;
			for(Thread thread : threads){
				//If IO Fabric changes the name of the APIWatcher Threads in a future version then this needs to change "OkHttp WebSocket" to match the thread listening to the api server
				if (thread != null && thread.getName() != null && thread.getName().startsWith("OkHttp WebSocket")){
					actualCount++;
				}
			}
			
			builder.withDetail("Total Expected Thread Count: ", count);
			builder.withDetail("Actual Thread Count: ", actualCount);

			//now compare actual to the expected
			if(count != actualCount){
				allWatchersAlive = false;
			}
		}
		else{
			builder.withDetail("Health Check on Watcher Thread Names (CPFRUN_GRM_EDGE_HEALTH_CHECK_THREAD_NAMES)", "disabled");
		}
		
		if(allWatchersAlive){
			builder.up();
		}
		else{
			builder.down();
		}
		
		return builder.build();
	}
}
