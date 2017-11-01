/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.ocnp.mgmt.grm_edge_service.types;

import java.util.concurrent.Future;

public class ManagedThread {

	private Runnable r;
	private Future<?> f;
	private Object params;
	
	public ManagedThread(Runnable r, Future<?> f, Object params){
		this.r = r;
		this.f = f;
		this.params = params;
	}
	
	public void setFuture(Future<?> f){
		this.f = f;
	}
	
	public Runnable getRunnable(){
		return r;
	}
	
	public Object getParams(){
		return params;
	}
	
	public Future<?> getFuture(){
		return f;
	}

	public void setRunnable(Runnable r2) {
		this.r =r2;
	}
}
