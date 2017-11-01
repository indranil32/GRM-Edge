/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.ocnp.mgmt.grm_edge_service.util;

import java.util.Locale;
import java.util.TimeZone;

public class GRMEdgeConstants {
	//cache names
	public static final String ENDPOINT_CACHE = "GRMSERVICE_SERVICEENDPOINT_CACHE";
	public static final String POD_CACHE = "POD_CACHE";
	public static final String SERVICE_CACHE = "K8_SERVICE_CACHE";
	public static final String ROUTEINFO_XML_CACHE = "ROUTEINFO_XML_CACHE";
	public static final String OLD_ENDPOINT_CACHE = "WRITE_BEHIND_OLD_SEP_CACHE";
	public static final String OLD_ENDPOINT_CASS_CACHE = "WRITE_BEHIND_OLD_SEP_CASS_CACHE";
	
	//some error messages
	public static final String JSON_PARSE_ERROR = "Unable to Parse JSON. Please make sure the JSON is valid and has the required attributes. [Error: %s]";
	public static final String JSON_MISSING_KIND = "No valid \"Kind\" detected in JSON. Please use the following values: [%s]";
	public static final String K8SERVICE_CACHE_ADD_ERROR = "Unable to add K8Service to registry. [Error: %s]";
	public static final String ENDPOINT_CACHE_ADD_ERROR = "Unable to add ServiceEndPoint to registry. [Error: %s]";
	public static final String SEP_CREATE_ERROR = "Unable to create ServiceEndPoint. [Error: %s]";
	public static final String SEP_ENV_MISSING = "Unable to create ServiceEndPoint. No Environment Detected for ServiceEndPoint. Environment must be supplied in metadata namespace for the service. [SEP: %s]";
	public static final String ERROR_MAKING_SEPS_FROM_SERVICES = "Found pods with same tag, but adding %s Service Endpoints failed. Please see logs for more detail";
	public static final String K8SERVICE_MISSING_REQUIRED = "Unable to create K8Service. Missing Required Attributes. [Required: %s] [Found: %s]";
	public static final String POD_MISSING_REQUIRED = "Unable to create K8Service. Missing Required Attributes. [Required: %s] [Found: %s]";
	public static final String WATCHER_EXCEPTION = "Exception occured while servicing the event. Please see the logs for more detail. [Error: %s]" ;
	public static final String POD_NOT_CREATED = "Unable to create pod. Please see the logs for more detail";
	public static final String K8SERVICES_NOT_CREATED = "Unable to create services. Please see the logs for more detail.";
	public static final String SEP_PORT_MISSING = "Unable to create ServiceEndPoint. No Port detected for ServiceEndPoint. Port must be supplied in the service. [SEP: %s]";
	public static final String SEP_HOST_MISSING = "Unable to create ServiceEndPoint. No Host Address detected for ServiceEndPoint. Host Address must be supplied in the service. [SEP: %s]";
	public static final String SEP_VERSION_MISSING = "Unable to create ServiceEndPoint. No Version detected for ServiceEndPoint. Version must be supplied in the service. [SEP: %s]";
	public static final String AFT_EDGE_INTERNAL_ERROR = "Error occured while sending request to downstream system.";
	public static final String GETROUTEINFO_INVALID_SERVICE_NAME = "Invalid input service name. Please validate the request. [ServiceName: %s]";
	public static final String GETROUTEINFO_PROCESS_ERROR = "Error processing GetRouteInfo request. Please check logs for detals [Error: %s]";
	public final static String SEP_HOST_UNABLE_TO_RESOLVE = "Unable to resolve the host from pod to create ServiceEndPoint. Using IP as hostname for SEP. [Error: %s]";

	/*
	 * These constants were pulled from edgecore
	 */	

	public static final String HZ_CACHE_CONFIG_FILE_NAME="/hazelcast-config.xml";
	public static String CACHE_TYPE_CONFIG_FILE_PATH = "";
	public static final String CAMEL_HTTP_METHOD = "CamelHttpMethod";
	public static final String CAMEL_HTTP_PATH = "CamelHttpPath";
	public static final String CAMEL_HTTP_QUERY = "CamelHttpQuery";
	public static final int RESPONSE_OK = 200;
	public static final String GET = "GET";
	public static final String REQ_RESP_CACHE = "req_resp_cache";
	public static final String STALE_EP_CACHE = "stale_ep_cache";
	
	
	/*
	 * Constants from KUBERNETES
	 */
	public static final String KUBERNETES_RUNNING = "Running";
	public static final String KUBERNETES_READY = "Ready";
	public static final String KUBERNETES_TRUE = "True";
	
	
	
	/*
	 * GRMEdge values and Default Values
	 */
	public static final String CPFRUN_GRMEDGE_DEFAULT_PROTOCOL = "http";
	public static final String CPFRUN_GRMEDGE_DEFAULT_KUBERNETESSUPERVISOR_THREAD_SLEEP = "5000";
	public static final String CPFRUN_GRMEDGE_DEFAULT_WRITE_DELAY = "1";
	public static final boolean PLATFORM_RUNTIME_GRMEDGE_DEFAULT_WRITE_COALESCING = false;
	public static final long PLATFORM_RUNTIME_GRMEDGE_SHUTDOWN_SLEEP = 28000;
	
	//Data Access Layer
    private static TimeZone timezone = TimeZone.getDefault();
    private static Locale locale = Locale.getDefault();
    
	public static final String SEP_EXPIRATION_INTERVAL_MINS = "SEP_EXPIRATION_INTERVAL_MINS";
	public static final int SEP_EXPIRATION_INTERVAL_MINS_DEFAULT = 30;
	public static final String REREGISTRATION_INTERVAL_DEFAULT_PARAM = "REREGISTRATION_INTERVAL_DEFAULT";
	public static final int REREGISTRATION_INTERVAL_DEFAULT_VAL = 15;
	public static final String NAME_SEP = ":";

	public static final String PARENT_NOT_FOUND="AFT-EDGE-0410";
	public static final String PARENT_NOT_FOUND_MSG="Parent for {%s}={%s} not found in DataStore for request {%s}";
	public static final String ADD_ALREADY_EXISTS="AFT-EDGE-0411";
	public static final String ADD_ALREADY_EXISTS_MSG="You are trying to add a";
	public static final String ADD_ALREADY_EXISTS_ERR_MSG="You are trying to add a {%s}={%s} that already exists for request {%s}";
	public static final String INVALID_INPUT_DOMAIN="AFT-EDGE-0413";
	public static final String SERVICE_NAME_REGEX_PATTERN = "(?<!\\\\)\\.";
	public static final String INVALID_NAMESPACE="AFT-EDGE-0412";
	public static final String INPUT_REQUEST_INVALID_DATA="AFT-EDGE-0414";
	public static final String INPUT_REQUEST_INVALID_DATA_MSG="Input data invalid for request {%s}. {%s} is required.";
	public static final String INPUT_CI_INVALID_DATA="AFT-EDGE-0415";
	public static final String INPUT_LRM_INVALID_DATA="AFT-EDGE-0416";
	public static final String INPUT_DATA_INVALID_SUPPORTED_VERSIONS="AFT-EDGE-0417";
	public static final String INPUT_DATA_INVALID_SUPPORTED_VERSIONS_MSG="Input {%s}={%s} is invalid for request {%s}. Valid values: {%s}.";
	public static final String INPUT_DATA_ILLEGAL_CHARS="AFT-EDGE-0418";
	public static final String INPUT_DATA_ILLEGAL_CHARS_MSG="Illegal characters were found for request {%s}. The following characters are not allowed in {%s}: {%s}.";
	public static final String INPUT_SEP_INVALID_DATA="AFT-EDGE-0419";
	public static final String INPUT_SEP_INVALID_DATA_MSG="Input ServiceEndPoint data invalid for request {%s}. {%s} is required.";
	public static final String PARENT_NOT_FOUND_IN_LDAP="AFT-EDGE-0420";
	public static final String INPUT_DATA_NOT_FOUND_IN_LDAP="AFT-EDGE-0421";
	public static final String INTERNAL_ERROR = "AFT-EDGE-0500";

	
	public static final String INTERNAL_ERROR_CASSANDRA_CONNECT="AFT-EDGE-0510";
	public static final String INTERNAL_ERROR_CASSANDRA_EXECUTING_QUERY="AFT-EDGE-0511";
	public static final String INTERNAL_ERROR_CASSANDRA_EXECUTING_QUERY_MSG="1failover, Internal Error executing query {%s}. Detailed Message: {%s}";

	
	public static final String DELETE_SEP_THREAD_POOL = "DELETE_SEP_THREAD_POOL";
	public static final String DELETE_SEP_THREAD_POOL_DEFAULT = "DeleteSEPThreadPool";
	public static final String DELETE_INVALID_SEP = "DELETE_INVALID_SEP";
	public static final String SEP_REQUIRED_VALIDATION_DEFAULT = "http,ContextPath;https,ContextPath;dme2jdbc,dme2JDBCDatabaseName";
	public static final String SEP_REQUIRED_VALIDATION = "SEP_REQUIRED_VALIDATION";
	public static final String Tracking = "TrackingID";
	public static final long PLATFORM_RUNTIME_GRMEDGE_DEFAULT_CACHESYNCHRONIZER_SLEEP = 300000;

	public static final String CRIT_ERROR_UNABLE_TO_PARSE_ENV_SERVICE_NAME="AFT-EDGE-0600";
	public static final String CRIT_ERROR_UNABLE_TO_PARSE_ENV_SERVICE_NAME_MSG="CRITICAL ERROR. Please contact Platform Runtime support. Not able to retrieve env or servicename from the object key: %s";
	public static final String GRM_HELPER_ERRORSTREAM = "AFT-EDGE-0601";
	public static final String GRM_HELPER_ERRORSTREAM_MSG = "Received Error stream from GRM during %s request instead of input stream. Message sent back: %s";
	
	public static TimeZone getTimeZone() {
        return timezone;
    }

    public static Locale getLocale() {
        return locale;
    }
	
}
