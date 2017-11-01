/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.ocnp.mgmt.grm_edge_service.service;

import com.att.ocnp.mgmt.grm_edge_service.util.GRMEdgeUtil;

public class GRMEdgeTestConstants {

	//should be the same
	public static final String TEST_POD_1 = "com.att.scld.grmedge.testcase-1";
	public static final String TEST_SERVICE_1 = TEST_POD_1;
	//concatenated together
	public static final String TEST_SERVICE_NAME_1 = TEST_POD_1+"." + TEST_POD_1;
	
	//should be different
	public static final String TEST_POD_2 = "com.att.scld.grmedge.testcase-2";
	public static final String TEST_SERVICE_2 = "com.att.scld.grmedge.testcase-2b";
	
	//should be different
	public static final String TEST_POD_3 = "com.att.scld.grmedge.testcase-3";
	public static final String TEST_SERVICE_3 = "com.att.scld.grmedge.testcase-3b";
	
	//should be the same
	public static final String TEST_POD_4 = "com.att.scld.grmedge.testcase-4";
	public static final String TEST_SERVICE_4 = TEST_POD_4;
	
	//should be the same
	public static final String TEST_POD_5 = "com.att.scld.grmedge.testcase-5";
	public static final String TEST_SERVICE_5 = TEST_POD_5;
	
	//should be the same
	public static final String TEST_POD_6 = "com.att.scld.grmedge.testcase-6";
	public static final String TEST_SERVICE_6 = TEST_POD_6;
	
	//should be the same
	public static final String TEST_POD_7 = "com.att.scld.grmedge.testcase-7";
	public static final String TEST_SERVICE_7 = TEST_POD_7;
	
	//should be the same
	public static final String TEST_POD_8 = "com.att.scld.grmedge.testcase-8";
	public static final String TEST_SERVICE_8 = TEST_POD_8;
	
	//should be different
	public static final String TEST_SERVICE_9 = "com.att.scld.grmedge.testcase-9";
	public static final String TEST_POD_9 = "com.att.scld.grmedge.testcase-9b";
	
	public static final String TEST_SERVICE_10 = "com.att.scld.grmedge.testcase-10";
	public static final String TEST_POD_10 = "com.att.scld.grmedge.testcase-10b";
	
	public static final String ENV = "DEV";
	public static final String LABENV = "DEV";

	public static final String TEST_POD_DB = "com.att.scld.grmedge.testcase-DB";
	public static final String TEST_SERVICE_DB = TEST_POD_DB;
	public static final String KUBE_TEST_POD_DB = "grmedgedb-testcase-svc-m-1";


	public static final String TEST_POD_CASS_DB = "com.att.scld.grmedge.testcase-cassdata-DB";
	public static final String TEST_SERVICE_CASS_DB = TEST_POD_CASS_DB;
	public static final String KUBE_TEST_POD_CASS_DB = "grmedgedb-testcase-cassdata-2";

	public static final String TEST_POD_CASS_DB_22 = "com.att.scld.grmedge.testcase-cassdata-DB-22";
	public static final String TEST_SERVICE_CASS_DB_22 = TEST_POD_CASS_DB_22;
	public static final String KUBE_TEST_POD_CASS_DB_22 = "grmedgedb-testcase-cassdata-22";
	
	
	public static final String TEST_POD_DELETE_DB = "com.att.scld.grmedge.testcase-DELETE-DB";
	public static final String TEST_SERVICE_DELETE_DB = TEST_POD_DELETE_DB;

	public static final String TEST_POD_DUPLICATE_DB_2 = "com.att.scld.grmedge.testcase-DUPLICATE-DB-2";
	public static final String TEST_SERVICE_DUPLICATE_DB_2 = TEST_POD_DUPLICATE_DB_2;

	public static final String TEST_POD_MULTIPLEPOD_DB = "com.att.scld.grmedge.testcase-MULTIPLEPOD-DB";
	public static final String TEST_SERVICE_MULTIPLEPOD_DB = TEST_POD_MULTIPLEPOD_DB;

	public static final String TEST_POD_MULTIPLEPOD2_DB = "com.att.scld.grmedge.testcase-MULTIPLEPOD2-DB";
	public static final String TEST_SERVICE_MULTIPLEPOD2_DB = TEST_POD_MULTIPLEPOD2_DB;

	public static final String TEST_POD_DUPLICATEPOD_DB = "com.att.scld.grmedge.testcase-DUPLICATEPOD-DB";
	public static final String TEST_SERVICE_DUPLICATEPOD_DB = TEST_POD_DUPLICATEPOD_DB;
		
	public static final String TEST_POD_DUPLICATEPOD_DB_2 = "com.att.scld.grmedge.testcase-DUPLICATEPOD-DB-2";
	public static final String TEST_SERVICE_DUPLICATEPOD_DB_2 = TEST_POD_DUPLICATEPOD_DB_2;

	public static final String  TEST_POD_INVALIDJSON = "com.att.scld.grmedge.testcase-INVALIDJSON-DB";
	public static final String  TEST_SERVICE_INVALIDJSON = TEST_POD_INVALIDJSON;
	
	public static final String TEST_POD_ADDPOD_DB = "com.att.scld.grmedge.testcase-ADDPOD-DB";
	public static final String TEST_SERVICE_ADDPOD_DB = TEST_POD_ADDPOD_DB;
	public static final String KUBE_TEST_POD_CASS_DB_ADDPOD = "grmedgedb-addpod-cassdata";


	public static final String TEST_POD_DELETESVC_DB = "com.att.scld.grmedge.testcase-DELETESVC-DB";
	public static final String TEST_SERVICE_DELETESVC_DB = TEST_POD_DELETESVC_DB;
	
	public static final String TEST_POD_UPDATESERVICE_DB = "com.att.scld.grmedge.testcase-UPDATE_SERVICE_DB";
	public static final String TEST_SERVICE_UPDATESERVICE_DB = TEST_POD_UPDATESERVICE_DB;

	
	public static final String KUBE_TEST_POD_CASS_DB_MATCH_NAME_1 = "grmedgedb-testcase-pod-m-1";
	public static final String KUBE_TEST_POD_CASS_DB_MATCH_NAME_2 = "grmedgedb-testcase-pod-m-2";

	/*
	 * KUBERNETES INTEGRATION TESTS 
	 */
	public static final String IMAGE_NAME = "helloworld";
	//public static final String kubernetesBaseURL = "https://" + System.getProperty("apiserver_master_host") + ":" + System.getProperty("apiserver_master_port") + "/api/v1/namespaces/default";
	//public static final String kubernetesGrmEdgeURL = "http://" + System.getProperty("apiserver_master_host") + ":" + System.getProperty("apiserver_master_port") + System.getProperty("contextpath_to_grm_edge") + "/GRMService/v1/service/findRunning";
	//public static final String kubernetesBaseURL = "http://" + System.getProperty("apiserver_master_host") + ":" + System.getProperty("apiserver_master_port") + "/api/v1/namespaces/default";
	//private static final String dmenamespace = "com.att.test.grmedge";
	public static String namespace = System.getProperty("KUBE_API_GRM_NAMESPACE","com-att-ocnp-mgmt");
	public static final String testnamespace = "com-att-ocnp-testcase-dev";
	public static final String othertestnamespace = "name-space-dev";
	public static String dummynamespace = "com-att-ocnp-testcase-dummy-ns";
	
	public static String namespaceWithDots = "com.att.ocnp.mgmt";
	public static final String testnamespaceWithDots = "com.att.ocnp.testcase.dev";
	public static final String othertestnamespaceWithDots = "name.space.dev";

	public static final String dummynamespaceWithDotsNoEnv = "com.att.ocnp.testcase.dummy.ns";
	public static final String namespaceWithDotsNoEnv = "com.att.ocnp.mgmt";
	public static final String testnamespaceWithDotsNoEnv = "com.att.ocnp.testcase";
	public static final String othertestnamespaceWithDotsNoEnv = "name.space";
	
	
	
	public static String kubernetesBaseURL = "https://" + System.getProperty("KUBE_API_HOST")+":"+System.getProperty("KUBE_API_PORT","443") + "/api/v1/namespaces/";
	public static String kubernetesBetaAPIURL = kubernetesBaseURL.replace("/api/v1/namespaces/", "/apis/extensions/v1beta1/");
	public static final String KUBE_TEST_POD_MATCH_NAME_1 = "grmedge-testcase-pod-m-1" + "-"+ System.getProperty("TESTCASE_ADDITION","jenkins");
	public static final String KUBE_TEST_APP_NAME_MATCH_1 = "grmedge-testcase-m-1" + "-"+ System.getProperty("TESTCASE_ADDITION","jenkins");
	public static final String KUBE_TEST_SERVICE_MATCH_NAME_1 = "grmedge-testcase-svc-m-1" + "-"+ System.getProperty("TESTCASE_ADDITION","jenkins");
	public static  String KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_1 = namespaceWithDotsNoEnv+"."+KUBE_TEST_APP_NAME_MATCH_1;
	public static final String KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_1 = testnamespaceWithDotsNoEnv+"."+KUBE_TEST_APP_NAME_MATCH_1;
	public static final String KUBE_TEST_POD_MATCH_NAME_2 = "grmedge-testcase-pod-m-2" + "-"+ System.getProperty("TESTCASE_ADDITION","jenkins");
	public static final String KUBE_TEST_APP_NAME_MATCH_2 = "grmedge-testcase-m-2" + "-"+ System.getProperty("TESTCASE_ADDITION","jenkins");
	public static final String KUBE_TEST_SERVICE_MATCH_NAME_2 = "grmedge-testcase-svc-m-2" + "-"+ System.getProperty("TESTCASE_ADDITION","jenkins");
	public static  String KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_2 = namespaceWithDotsNoEnv+"."+KUBE_TEST_APP_NAME_MATCH_2;
	public static final String KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_2 = testnamespaceWithDotsNoEnv+"."+KUBE_TEST_APP_NAME_MATCH_2;
	public static final String KUBE_TEST_POD_MATCH_NAME_3 = "grmedge-testcase-pod-m-3" + "-"+ System.getProperty("TESTCASE_ADDITION","jenkins");
	public static final String KUBE_TEST_APP_NAME_MATCH_3 = "grmedge-testcase-m-3" + "-"+ System.getProperty("TESTCASE_ADDITION","jenkins");
	public static final String KUBE_TEST_SERVICE_MATCH_NAME_3 = "grmedge-testcase-svc-m-3" + "-"+ System.getProperty("TESTCASE_ADDITION","jenkins");
	public static  String KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_3 = namespaceWithDotsNoEnv+"."+KUBE_TEST_APP_NAME_MATCH_3;
	public static final String KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_3 = testnamespaceWithDotsNoEnv+"."+KUBE_TEST_APP_NAME_MATCH_3;
	public static final String KUBE_TEST_POD_ONLY_NAME = "grmedge-testcase-pod-n-1" + "-"+ System.getProperty("TESTCASE_ADDITION","jenkins");
	public static final String KUBE_TEST_APP_NAME_NO_MATCH_1 = "grmedge-testcase-n-1" + "-"+ System.getProperty("TESTCASE_ADDITION","jenkins");
	public static final String KUBE_TEST_POD_ONLY_ENDPOINT_NAME = "nomatchidontthink" + "-"+ System.getProperty("TESTCASE_ADDITION","jenkins");
	public static final String KUBE_TEST_APP_NAME_NO_MATCH_2 = "grmedge-testcase-n-2" + "-"+ System.getProperty("TESTCASE_ADDITION","jenkins");
	public static final String KUBE_TEST_SERVICE_ONLY_NAME = "grmedge-testcase-svc-n-2" + "-"+ System.getProperty("TESTCASE_ADDITION","jenkins");
	public static  String KUBE_TEST_SERVICE_ONLY_ENDPOINT_NAME = namespaceWithDotsNoEnv +"." +KUBE_TEST_APP_NAME_NO_MATCH_1;
	public static final String KUBE_TEST_TESTNS_SERVICE_ONLY_ENDPOINT_NAME = testnamespaceWithDotsNoEnv +"." +KUBE_TEST_APP_NAME_NO_MATCH_1;
	public static final String KUBE_TEST_POD_NO_MATCH_NAME = "grmedge-testcase-pod-n-3" + "-"+ System.getProperty("TESTCASE_ADDITION","jenkins");
	public static final String KUBE_TEST_APP_NAME_NO_MATCH_3 = "grmedge-testcase-n-3" + "-"+ System.getProperty("TESTCASE_ADDITION","jenkins");
	public static final String KUBE_TEST_SERVICE_NO_MATCH_NAME = "grmedge-testcase-svc-n-3" + "-"+ System.getProperty("TESTCASE_ADDITION","jenkins");
	public static final String KUBE_TEST_APP_NAME_NO_MATCH_4 = "grmedge-testcase-n-3b" + "-"+ System.getProperty("TESTCASE_ADDITION","jenkins");
	public static  String KUBE_TEST_SERVICE_NO_MATCH_ENDPOINT_NAME = namespaceWithDotsNoEnv+"."+KUBE_TEST_APP_NAME_NO_MATCH_3;
	public static final String KUBE_TEST_TESTNS_SERVICE_NO_MATCH_ENDPOINT_NAME = testnamespaceWithDotsNoEnv+"."+KUBE_TEST_APP_NAME_NO_MATCH_3;
	
	//set to 35 seconds because the podwatcher waits for 30 seconds so we need to account for that
	public static final long SLEEP = 35000;
	public static final String GRM_REST_SECONDARY_PATH_PORT_CONTEXT = " http://localhost:8080";
	public static final String GRM_REST_PATH_PORT_CONTEXT = GRM_REST_SECONDARY_PATH_PORT_CONTEXT;
	public static final String GRM_REST_SECONDARY_PATH_PORT_CONTEXT_DELETE = "http://localhost:8080";
	public static final String GRM_REST_PATH_PORT_CONTEXT_DELETE = GRM_REST_SECONDARY_PATH_PORT_CONTEXT_DELETE;
	public static final String GRM_REST_PATH_PORT_CONTEXT_ADD = "http://localhost:8080";
	public static final String GRM_REST_PATH_PORT_CONTEXT_UPDATE = "http://localhost:8080";
	public static final String GRM_EDGE_NAMESPACE = namespace;
	public static final String GRM_EDGE_SERVICE_NAME = "grm-edge";
	public static final String EDGE_CONFIG_MAP_NAME = "grmedge-config";

	public static final String CASS_KUBE_TEST_POD_MATCH_NAME = "grmedge-testcase-pod-m-cass-1";
	public static final String CASS_KUBE_TEST_SERVICE_MATCH_NAME = CASS_KUBE_TEST_POD_MATCH_NAME;
	public static  String CASS_KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME = othertestnamespaceWithDotsNoEnv+"."+CASS_KUBE_TEST_SERVICE_MATCH_NAME;

	public static final String KUBE_TEST_POD_MATCH_NAME_1_ATTR = "grmedge-testcase-pod-m-1-attr";
	public static final String KUBE_TEST_SERVICE_MATCH_NAME_1_ATTR = "grmedge-testcase-svc-m-1-attr";;
	public static final String KUBE_TEST_APP_NAME_MATCH_1_ATTR = "grmedge-testcase-m-1-attr";
	public static final String KUBE_TEST_POD_ONLY_ENDPOINT_NAME_ATTR = "nomatchidontthink";
	public static final String KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_1_ATTR = testnamespaceWithDotsNoEnv+"."+KUBE_TEST_APP_NAME_MATCH_1_ATTR;

	public static final String KUBE_UPD_TEST_POD_MATCH_NAME_1_ATTR = "grmedge-testcase-pod-upd-m-1-attr";
	public static final String KUBE_UPD_TEST_SERVICE_MATCH_NAME_1_ATTR = "grmedge-testcase-svc-upd-m-1-attr";;
	public static final String KUBE_UPD_TEST_APP_NAME_MATCH_1_ATTR = "grmedge-testcase-upd-m-1-attr";
	public static final String KUBE_UPD_TEST_POD_ONLY_ENDPOINT_NAME_ATTR = "nomatchidontthink";
	public static final String KUBE_UPD_TEST_SERVICE_MATCH_ENDPOINT_NAME_1_ATTR = testnamespaceWithDotsNoEnv+"."+KUBE_UPD_TEST_APP_NAME_MATCH_1_ATTR;

	public static final String CASS_KUBE_TEST_POD_MATCH_NAME_22 = TEST_POD_CASS_DB_22;
	public static final String CASS_KUBE_TEST_SERVICE_MATCH_NAME_22 = CASS_KUBE_TEST_POD_MATCH_NAME_22;
	public static final String SYNC_TEST_CASE_NAME = "com.att.ocnp.mgmt.grmedge-testcase-sync" + "-"+ System.getProperty("TESTCASE_ADDITION","jenkins");
	public static final String SYNC_TEST_CASE_K8_SERVICE_NAME = "grmedge-testcase-sync-" + System.getProperty("TESTCASE_ADDITION","jenkins");
	
	public static final String DUMMY_TESTCASE_NAME = "grmedge-dummy-testcase" + System.getProperty("TESTCASE_ADDITION","jenkins");
	public static final String DUMMY_TESTCASE_SERVICE_NAME = dummynamespaceWithDotsNoEnv +"." + DUMMY_TESTCASE_NAME;
	
	public static  String REFRESH_CACHE_INVALID_LOGIN_ERROR_MESSAGE="Invalid Username and Password supplied";
	public static  String WRITE_BEHIND_ENABLED_ERROR_MESSAGE="Not Allowing This Call. Write behind is or was enabled for GRM Edge.";
	public static  String CACHE_REFRESH_SUCCESS_MESSAGE="Cache was refreshed successfully!";


	public static  String CASS_KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_22 = othertestnamespaceWithDotsNoEnv+"."+CASS_KUBE_TEST_SERVICE_MATCH_NAME_22;

	
	public static void resetNames(){
		namespace = System.getProperty("KUBE_API_GRM_NAMESPACE","com-att-ocnp-mgmt");
		CASS_KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_22 = othertestnamespaceWithDotsNoEnv +"."+CASS_KUBE_TEST_SERVICE_MATCH_NAME_22;
		CASS_KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME = othertestnamespaceWithDotsNoEnv+"."+CASS_KUBE_TEST_SERVICE_MATCH_NAME;
		KUBE_TEST_SERVICE_NO_MATCH_ENDPOINT_NAME = namespaceWithDotsNoEnv+"."+KUBE_TEST_APP_NAME_NO_MATCH_3;
		KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_1 = namespaceWithDotsNoEnv+"."+KUBE_TEST_APP_NAME_MATCH_1;
		KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_2 = namespaceWithDotsNoEnv+"."+KUBE_TEST_APP_NAME_MATCH_2;		
		KUBE_TEST_SERVICE_ONLY_ENDPOINT_NAME = namespaceWithDotsNoEnv +"." +KUBE_TEST_APP_NAME_NO_MATCH_1;
		KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_3 = namespaceWithDotsNoEnv+"."+KUBE_TEST_APP_NAME_MATCH_3;
		kubernetesBaseURL = "https://" + System.getProperty("KUBE_API_HOST")+":"+System.getProperty("KUBE_API_PORT","443") + "/api/v1/namespaces/";
		kubernetesBetaAPIURL = kubernetesBaseURL.replace("/api/v1/namespaces/", "/apis/extensions/v1beta1/");
	}
	
}

