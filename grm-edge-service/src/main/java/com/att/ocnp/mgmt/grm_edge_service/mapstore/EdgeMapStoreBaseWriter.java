/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.ocnp.mgmt.grm_edge_service.mapstore;

import java.util.Collection;
import java.util.Map;

public abstract class EdgeMapStoreBaseWriter {
	
	protected String mapName = "";
	
	public abstract void send(Object key, Object value);
	public abstract void sendAll(Map<Object, Object> map);
	public abstract void delete(Object key);
	public abstract void deleteAll(Collection<Object> keys);
}
