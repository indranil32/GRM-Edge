/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.ocnp.mgmt.grm_edge_service.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class DNSIPResolver {
	
	/**
	 * connect to DNS server and get the initial server list
	 * 
	 * @throws UnknownHostException
	 */
	public List<String> getListIPForName(String dnsName) throws UnknownHostException {
		// connect to DNS and load the cache content
		List<String> newList = new ArrayList<String>();
		InetAddress[] addresses = InetAddress.getAllByName(dnsName); // UnknownHostException
		for (int i = 0; i < addresses.length; i++) {
			newList.add(ipToString(addresses[i].getAddress()));
		}
		return newList;
	}

	String ipToString(byte[] ip) {
		StringBuilder builder = new StringBuilder(23); // IP6 size
		for (int part = 0; part < ip.length; part++) {
			if (builder.length() > 0) {
				builder.append('.');
			}
			int code = ip[part];
			// numbers above 127 is interpreted as negative as byte is signed in java
			if (code<0) code += 256;	
			builder.append(code);
		}
		return builder.toString();
	}
}
