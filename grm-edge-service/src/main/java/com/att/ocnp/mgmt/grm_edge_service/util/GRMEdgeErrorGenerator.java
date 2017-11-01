/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.ocnp.mgmt.grm_edge_service.util;

public class GRMEdgeErrorGenerator {

	public static String generateErrorMessage(String errorConstant, String errorLocalMessage){
		return String.format(errorConstant, errorLocalMessage);
	}
	public static String generateErrorMessage(String errorConstant){
		return errorConstant;
	}
	public static String generateErrorMessage(String errorConstant, String string1, String string2) {
		return String.format(errorConstant, string1,string2);
	}
}