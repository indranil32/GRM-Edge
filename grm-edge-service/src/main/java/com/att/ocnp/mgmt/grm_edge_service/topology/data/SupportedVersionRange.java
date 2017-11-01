/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.ocnp.mgmt.grm_edge_service.topology.data;

import com.att.scld.grm.types.v1.VersionDefinition;

/** represents a defined range of versions */
public class SupportedVersionRange
{
	private int[] start, end;
	private boolean endIsWildcard;
	
	public SupportedVersionRange(String range)
	{
		// parse string into values
		final String[] parts = range.split(",");
		if(parts.length!=2) 
			throw new IllegalArgumentException("version range must have form min,max: " + range);
		start = parseVersion(parts[0]);
		if(parts[1].equals("*")) 
			endIsWildcard = true;
		else end = parseVersion(parts[1]);
		
		// validate min<=max
		if(endIsWildcard) 
			return;
		if(start.length!=end.length) 
			throw new IllegalArgumentException("version range min,max must either both have just major or both have major.minor");
		if(start[0]>end[0] || (start[0]==end[0] && start.length==2 && start[1]>end[1])) 
			throw new IllegalArgumentException("version range must have min <= max: " + range);
	}
	
	private static int[] parseVersion(String version)
	{
		final String[] parts = version.split("\\.");
		final int major = Integer.parseInt(parts[0]);
		if(major<0) throw new IllegalArgumentException("version major must not be negative: " + version);
		if(parts.length==1) return new int[] { major };
		if(parts.length!=2) throw new IllegalArgumentException("version in range can only be major or major.minor: " + version);
		final int minor = Integer.parseInt(parts[1]);
		if(minor<0) throw new IllegalArgumentException("version minor must not be negative: " + version);
		return new int[] { major, minor };
	}
	
	public boolean isSupported(VersionDefinition version)
	{
		// first, check against start of range
		final int major = version.getMajor();
		if(major<start[0]) return false;
		final int minor = version.getMinor();
		
		// MAGIC NUMBER ANTIPATTERN: minor==-1 means version is major only
		if(minor!=-1) 
		{
			if(major==start[0] && start.length==2 && minor<start[1]) return false;
		}
		
		// from here down, check against end of range
		if(endIsWildcard) return true;
		if(major>end[0]) return false;
		
		// don't need special check for MAGIC NUMBER ANTIPATTERN here
		// b/c -1 will never be more than end[1] / minor version of range end
		if(major==end[0] && end.length==2 && minor>end[1]) return false;
		return true;
	}

}
