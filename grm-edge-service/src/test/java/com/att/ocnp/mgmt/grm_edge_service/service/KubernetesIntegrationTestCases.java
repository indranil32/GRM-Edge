/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.ocnp.mgmt.grm_edge_service.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.att.ocnp.mgmt.grm_edge_service.util.GRMEdgeUtil;
import com.att.scld.grm.types.v1.NameValuePair;
import com.att.scld.grm.types.v1.ServiceEndPoint;
import com.att.scld.grm.v1.FindServiceEndPointResponse;

public class KubernetesIntegrationTestCases {
	
	private static String jsonOriginalConfig = "";
	private static String jsonStartUpConfig = "";
	private static String syncTimeOriginal = "";
	
	/*
	 * Setup for all test cases
	 */
	@BeforeClass
	public static void setup() throws InterruptedException, JSONException{
		System.out.println("====Beginning Setup===");
		GRMEdgeTestConstants.resetNames();
	
		System.setProperty("javax.net.ssl.trustStore", "src/test/resources/cacerts");
		GRMEdgeTestUtil.SSLCertificateValidation.disable();
		GRMEdgeTestUtil.allowPatch();

		jsonStartUpConfig = GRMEdgeTestUtil.getCurrentConfig();
		System.out.println(jsonStartUpConfig);
		String config = setupCustomConfigForEdge(jsonStartUpConfig);
		GRMEdgeTestUtil.resetConfig(config);
		jsonOriginalConfig = GRMEdgeTestUtil.getCurrentConfig();
		
		syncTimeOriginal = GRMEdgeTestUtil.getSynchronizeTime(jsonOriginalConfig);
		System.out.println("Sync time Original: " + syncTimeOriginal);
		System.out.println("JSON START CONFIG: " + jsonStartUpConfig);
		System.out.println("JSON ORIGINAL CONFIG: " + jsonOriginalConfig);
		
		
//		jsonOriginalConfig = setupCustomConfigForEdge(jsonOriginalConfig);
		
		cleanUPSEPSInGRM();
		
		//create test namespace com.att.ocnp.testcase.DEV
		assertEquals("Not running test suite, testendpoints ("+GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_1+") found in GRM (LAB) will mess up the test.",GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunning(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_1,GRMEdgeTestConstants.namespace,"LAB").getServiceEndPointList().size(),0);
		assertEquals("Not running test suite, testendpoints ("+GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_2+") found in GRM (LAB) will mess up the test.",GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunning(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_2, GRMEdgeTestConstants.namespace,"LAB").getServiceEndPointList().size(),0);
		assertEquals("Not running test suite, testendpoints ("+GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_3+") found in GRM (LAB) will mess up the test.",GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunning(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_3, GRMEdgeTestConstants.namespace,"LAB").getServiceEndPointList().size(),0);
		assertEquals("Not running test suite, testendpoints ("+GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_1+") found in GRM (DEV) will mess up the test.",GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunning(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_1, GRMEdgeTestConstants.namespace,"DEV").getServiceEndPointList().size(),0);
		assertEquals("Not running test suite, testendpoints ("+GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_2+") found in GRM (DEV) will mess up the test.",GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunning(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_2, GRMEdgeTestConstants.namespace,"DEV").getServiceEndPointList().size(),0);
		assertEquals("Not running test suite, testendpoints ("+GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_3+") found in GRM (DEV) will mess up the test.",GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunning(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_3, GRMEdgeTestConstants.namespace,"DEV").getServiceEndPointList().size(),0);
		
		GRMEdgeTestUtil.createNamespace(GRMEdgeTestConstants.testnamespace);
		GRMEdgeTestUtil.createNamespace(GRMEdgeTestConstants.dummynamespace);
					
		System.out.println("====Ending Setup===");
	}
	
	@AfterClass
	public static void shutdown() throws InterruptedException, JSONException{
		System.out.println("====Shutdown started====");
		
		//delete test namespace com.att.ocnp.testcase.DEV
		
		GRMEdgeTestUtil.deleteNamespace(GRMEdgeTestConstants.testnamespace);
		GRMEdgeTestUtil.deleteNamespace(GRMEdgeTestConstants.dummynamespace);
		
		GRMEdgeTestUtil.deleteRc(GRMEdgeTestConstants.KUBE_TEST_POD_ONLY_NAME,GRMEdgeTestConstants.namespace);
		GRMEdgeTestUtil.deleteService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_ONLY_NAME,GRMEdgeTestConstants.namespace);
		GRMEdgeTestUtil.deleteRc(GRMEdgeTestConstants.KUBE_TEST_POD_NO_MATCH_NAME,GRMEdgeTestConstants.namespace);
		GRMEdgeTestUtil.deleteService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_NO_MATCH_NAME,GRMEdgeTestConstants.namespace);
		GRMEdgeTestUtil.deleteService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_1,GRMEdgeTestConstants.namespace);
		GRMEdgeTestUtil.deleteRc(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_1,GRMEdgeTestConstants.namespace);
		GRMEdgeTestUtil.deleteRc(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_2,GRMEdgeTestConstants.namespace);
		GRMEdgeTestUtil.deleteService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_2,GRMEdgeTestConstants.namespace);
		GRMEdgeTestUtil.deleteRc(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_3,GRMEdgeTestConstants.namespace);
		GRMEdgeTestUtil.deleteService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_3,GRMEdgeTestConstants.namespace);
		
		//make suer deleted from testnamespace
		GRMEdgeTestUtil.deleteRc(GRMEdgeTestConstants.KUBE_TEST_POD_ONLY_NAME,GRMEdgeTestConstants.testnamespace);
		GRMEdgeTestUtil.deleteService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_ONLY_NAME,GRMEdgeTestConstants.testnamespace);
		GRMEdgeTestUtil.deleteRc(GRMEdgeTestConstants.KUBE_TEST_POD_NO_MATCH_NAME,GRMEdgeTestConstants.testnamespace);
		GRMEdgeTestUtil.deleteService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_NO_MATCH_NAME,GRMEdgeTestConstants.testnamespace);
		GRMEdgeTestUtil.deleteRc(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_1,GRMEdgeTestConstants.testnamespace);
		GRMEdgeTestUtil.deleteService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_1,GRMEdgeTestConstants.testnamespace);
		GRMEdgeTestUtil.deleteRc(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_2,GRMEdgeTestConstants.testnamespace);
		GRMEdgeTestUtil.deleteService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_2,GRMEdgeTestConstants.testnamespace);
		GRMEdgeTestUtil.deleteRc(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_3,GRMEdgeTestConstants.testnamespace);
		GRMEdgeTestUtil.deleteService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_3,GRMEdgeTestConstants.testnamespace);
		
		GRMEdgeTestUtil.deleteService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_1+"d",GRMEdgeTestConstants.testnamespace);
		GRMEdgeTestUtil.deleteRc(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_1+"d",GRMEdgeTestConstants.testnamespace);
		
		if(!GRMEdgeTestUtil.getCurrentConfig().equalsIgnoreCase(jsonStartUpConfig)){
			Thread.sleep(GRMEdgeTestConstants.SLEEP);
			GRMEdgeTestUtil.resetConfig(jsonStartUpConfig);
			//restart GRM Edge
			GRMEdgeTestUtil.scaleDeployment(GRMEdgeTestConstants.GRM_EDGE_SERVICE_NAME, "0",GRMEdgeTestConstants.namespace);
			//allow old GRMEdge to use proper shutdownhook
			Thread.sleep(GRMEdgeTestConstants.SLEEP);
			GRMEdgeTestUtil.scaleDeployment(GRMEdgeTestConstants.GRM_EDGE_SERVICE_NAME, "1",GRMEdgeTestConstants.namespace);
		}
		
		GRMEdgeTestUtil.deleteRc(GRMEdgeTestConstants.SYNC_TEST_CASE_K8_SERVICE_NAME, GRMEdgeTestConstants.namespace);
		GRMEdgeTestUtil.deleteService(GRMEdgeTestConstants.SYNC_TEST_CASE_K8_SERVICE_NAME, GRMEdgeTestConstants.namespace);
		
		GRMEdgeTestUtil.deleteRc(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_1+"r", GRMEdgeTestConstants.namespace);
		GRMEdgeTestUtil.deleteService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_1+"r", GRMEdgeTestConstants.namespace);

		cleanUPSEPSInGRM();
		
		System.out.println("====Shutdown ended====");
	}
	
	private static String addSettingToListWithOverwrite(String list, String property, String value){
		String returnList = list;
		if(list.contains(property)){
			int indexStart = list.indexOf(property);
			int indexEnd = list.substring(indexStart).indexOf("\n")+indexStart;
			System.out.println(indexStart);
			System.out.println(indexEnd);
			returnList = list.substring(0, indexStart)+property+"\u003d"+value+list.substring(indexEnd);
		}
		else{
			returnList=property+"\u003d"+value+"\n"+list;
		}
		return returnList;
	}
	
	/**
	 * This is to setup the correct environment before test suite execution.
	 * 
	 * @param jsonToModify
	 * @return
	 */
	public static String setupCustomConfigForEdge(String jsonToModify){
		//right now all these props need to be set for this to run
		String configCSV = "CPFRUN_GRMEDGE_WRITE_BEHIND_DELAY=1,GRM_EDGE_ENABLE_DASH_TO_DOT_CONVERSION=true,PLATFORM_RUNTIME_GRMEDGE_DEFAULT_CACHESYNCHRONIZER_SLEEP=30000,CPFRUN_GRMEDGE_DB_WRITE_BEHIND_ENABLE=true,GRM_EDGE_DISABLE_ENV_PARSING=false,GRM_EDGE_CONVERT_DASH_TO_DOT_ON_FIND_RUNNING=FALSE,GRM_EDGE_FINDRUNNING_RETRIEVE_FROM_GRM_REST=true,CPFRUN_GRMEDGE_GRM_HOST=hlth451.hydc.sbc.com,CPFRUN_GRMEDGE_GRM_PORT=9427,CPFRUN_GRMEDGE_DEFAULT_ENV=LAB";
		
		List<NameValuePair> list = new ArrayList<>();
		for(String cvp: configCSV.split(",")){
			String[] cvparray = cvp.split("=");
			NameValuePair nvp = new NameValuePair();
			nvp.setName(cvparray[0]);
			nvp.setValue(cvparray[1]);
		}
		
		String config = jsonToModify;
		for(NameValuePair nv: list){
			config = addSettingToListWithOverwrite(config, nv.getName(), nv.getValue());
		}
		return config;
	}
	
	static void cleanUPSEPSInGRM(){

		GRMEdgeTestUtil.deleteSEPFromGRM(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_1, "DEV");
		GRMEdgeTestUtil.deleteSEPFromGRM(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_2, "DEV");
		GRMEdgeTestUtil.deleteSEPFromGRM(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_3, "DEV");
		
		GRMEdgeTestUtil.deleteSEPFromGRM(GRMEdgeTestConstants.SYNC_TEST_CASE_NAME, "DEV");

		GRMEdgeTestUtil.deleteSEPFromGRM(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_1, "LAB");
		GRMEdgeTestUtil.deleteSEPFromGRM(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_2, "LAB");
		GRMEdgeTestUtil.deleteSEPFromGRM(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_3, "LAB");
		GRMEdgeTestUtil.deleteSyncTestEndPoint(1);
		GRMEdgeTestUtil.deleteSyncTestEndPoint(2);

		GRMEdgeTestUtil.deleteVersionFindRunningTestEndPoint(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_1, 10, 1, "3");
		GRMEdgeTestUtil.deleteVersionFindRunningTestEndPoint(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_1, 10, 2, "1");

		
	}
	
	@After
	public void tearDown() throws InterruptedException, JSONException{
		
		Thread.sleep(1100);
		
		System.out.println("===Tear down started===");
		GRMEdgeTestUtil.deleteRc(GRMEdgeTestConstants.KUBE_TEST_POD_ONLY_NAME,GRMEdgeTestConstants.namespace);
		GRMEdgeTestUtil.deleteService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_ONLY_NAME,GRMEdgeTestConstants.namespace);
		GRMEdgeTestUtil.deleteRc(GRMEdgeTestConstants.KUBE_TEST_POD_NO_MATCH_NAME,GRMEdgeTestConstants.namespace);
		GRMEdgeTestUtil.deleteService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_NO_MATCH_NAME,GRMEdgeTestConstants.namespace);
		GRMEdgeTestUtil.deleteRc(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_1,GRMEdgeTestConstants.namespace);
		GRMEdgeTestUtil.deleteService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_1,GRMEdgeTestConstants.namespace);
		GRMEdgeTestUtil.deleteRc(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_2,GRMEdgeTestConstants.namespace);
		GRMEdgeTestUtil.deleteService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_2,GRMEdgeTestConstants.namespace);
		GRMEdgeTestUtil.deleteRc(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_3,GRMEdgeTestConstants.namespace);
		GRMEdgeTestUtil.deleteService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_3,GRMEdgeTestConstants.namespace);

		//make suer deleted from testnamespace
		GRMEdgeTestUtil.deleteRc(GRMEdgeTestConstants.KUBE_TEST_POD_ONLY_NAME,GRMEdgeTestConstants.testnamespace);
		GRMEdgeTestUtil.deleteService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_ONLY_NAME,GRMEdgeTestConstants.testnamespace);
		GRMEdgeTestUtil.deleteRc(GRMEdgeTestConstants.KUBE_TEST_POD_NO_MATCH_NAME,GRMEdgeTestConstants.testnamespace);
		GRMEdgeTestUtil.deleteService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_NO_MATCH_NAME,GRMEdgeTestConstants.testnamespace);
		GRMEdgeTestUtil.deleteRc(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_1,GRMEdgeTestConstants.testnamespace);
		GRMEdgeTestUtil.deleteService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_1,GRMEdgeTestConstants.testnamespace);
		GRMEdgeTestUtil.deleteRc(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_2,GRMEdgeTestConstants.testnamespace);
		GRMEdgeTestUtil.deleteService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_2,GRMEdgeTestConstants.testnamespace);
		GRMEdgeTestUtil.deleteRc(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_3,GRMEdgeTestConstants.testnamespace);
		GRMEdgeTestUtil.deleteService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_3,GRMEdgeTestConstants.testnamespace);
		GRMEdgeTestUtil.deleteRc(GRMEdgeTestConstants.SYNC_TEST_CASE_K8_SERVICE_NAME, GRMEdgeTestConstants.namespace);
		GRMEdgeTestUtil.deleteService(GRMEdgeTestConstants.SYNC_TEST_CASE_K8_SERVICE_NAME, GRMEdgeTestConstants.namespace);		
		GRMEdgeTestUtil.deleteRc(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_1+"r", GRMEdgeTestConstants.namespace);
		GRMEdgeTestUtil.deleteService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_1+"r", GRMEdgeTestConstants.namespace);
		cleanUPSEPSInGRM();

		if(!GRMEdgeTestUtil.getCurrentConfig().equals(jsonOriginalConfig)){
			Thread.sleep(GRMEdgeTestConstants.SLEEP); //sleep for any write ehind possiblity
			GRMEdgeTestUtil.resetConfig(jsonOriginalConfig);
			//restart GRM Edge
			GRMEdgeTestUtil.scaleDeployment(GRMEdgeTestConstants.GRM_EDGE_SERVICE_NAME, "0",GRMEdgeTestConstants.namespace);
			GRMEdgeTestUtil.scaleDeployment(GRMEdgeTestConstants.GRM_EDGE_SERVICE_NAME, "1",GRMEdgeTestConstants.namespace);
			//allow grm to start up
			Thread.sleep(GRMEdgeTestConstants.SLEEP*2);
		}
		
		System.out.println("Confirming tear down completed by checking each endpoint");
		GRMEdgeTestUtil.getEndPointsfromKubernetesWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_1, "LAB", GRMEdgeTestConstants.SLEEP*3, 0);
		GRMEdgeTestUtil.getEndPointsfromKubernetesWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_2, "LAB", GRMEdgeTestConstants.SLEEP*3, 0);
		GRMEdgeTestUtil.getEndPointsfromKubernetesWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_3, "LAB", GRMEdgeTestConstants.SLEEP*3, 0);
		

		GRMEdgeTestUtil.getEndPointsfromKubernetesWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_1, "DEV", GRMEdgeTestConstants.SLEEP*3, 0);
		GRMEdgeTestUtil.getEndPointsfromKubernetesWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_2, "DEV", GRMEdgeTestConstants.SLEEP*3, 0);
		GRMEdgeTestUtil.getEndPointsfromKubernetesWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_3, "DEV", GRMEdgeTestConstants.SLEEP*3, 0);		

		// make sure we scale GRMEdge ack to one as it could be more than one from hazelcast test - if its already one, it doesnt hurt
		if(GRMEdgeTestUtil.getGRMEdgePodNames().size() != 1){
			GRMEdgeTestUtil.scaleDeployment(GRMEdgeTestConstants.GRM_EDGE_SERVICE_NAME, "1", GRMEdgeTestConstants.namespace);	
		}
		
		Thread.sleep(1100 );
		
		
		System.out.println("===Tear down ended===");
	}

  	@Test
  	public void addMatchingPodAndServiceDashToDotDisabledAndEnvParseDisabled() throws InterruptedException, JSONException {
  		System.out.println("=================================");
  		System.out.println("Beginning test: " +Thread.currentThread().getStackTrace()[1].getMethodName());
  		//SET GRM TO TRUE, SET CASSANDRA TO FALSE
  		GRMEdgeTestUtil.setConfigValue(GRMEdgeTestUtil.getCurrentConfig(),"GRM_EDGE_ENABLE_DASH_TO_DOT_CONVERSION=false");
  		GRMEdgeTestUtil.setConfigValue(GRMEdgeTestUtil.getCurrentConfig(),"GRM_EDGE_DISABLE_ENV_PARSING=true");
  		Thread.sleep(100);		
  		//restart GRM Edge
  		GRMEdgeTestUtil.scaleDeployment(GRMEdgeTestConstants.GRM_EDGE_SERVICE_NAME, "0",GRMEdgeTestConstants.namespace);
  		//allow old GRMEdge to use proper shutdownhook
  		GRMEdgeTestUtil.scaleDeployment(GRMEdgeTestConstants.GRM_EDGE_SERVICE_NAME, "1",GRMEdgeTestConstants.namespace);
  		
  		GRMEdgeTestUtil.addRC(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_2, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_2, "1",GRMEdgeTestConstants.testnamespace);
		GRMEdgeTestUtil.addService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_2, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_2,GRMEdgeTestConstants.testnamespace);
		
		GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunningWithAutomatedSleep(GRMEdgeTestConstants.testnamespace+"."+GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_2, "LAB",GRMEdgeTestConstants.SLEEP*2,1);
  		
  		System.out.println("=================================");
  	}
	
	/**
	 * This test retrieves the health from GRMEdge and makes sure the Watchers are both set as UP status
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void retrieveWatcherHealth() throws InterruptedException{
		System.out.println("=================================");
		System.out.println("Beginning test: " +Thread.currentThread().getStackTrace()[1].getMethodName());
		
		String json = GRMEdgeTestUtil.getEdgeHealth();
		
		System.out.println(json);
		assertTrue(json.contains("GRMEdgeWatcherHealth\":{\"status\":\"UP"));
		System.out.println("=================================");
	}
	
	/*
	 * No SEP should be created
	 */
	@Test
	public void addPodOnly() throws InterruptedException{
		System.out.println("=================================");
		System.out.println("Beginning test: " +Thread.currentThread().getStackTrace()[1].getMethodName());
		//this test should pretty much always pass... do we need a test?
		GRMEdgeTestUtil.addRC(GRMEdgeTestConstants.KUBE_TEST_POD_ONLY_NAME, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_NO_MATCH_1, "1", GRMEdgeTestConstants.namespace);
		
		//Adding in a sleep so that if changes were to happen, they would have plenty of time to occur
		Thread.sleep(GRMEdgeTestConstants.SLEEP);
		
		assertEquals(GRMEdgeTestUtil.getEndPointsFromKubernetes(GRMEdgeTestConstants.KUBE_TEST_POD_ONLY_ENDPOINT_NAME, "LAB").getServiceEndPointList().size(),0);
		assertEquals(GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunning(GRMEdgeTestConstants.KUBE_TEST_POD_ONLY_ENDPOINT_NAME, GRMEdgeTestConstants.namespace,"LAB").getServiceEndPointList().size(),0);

		if (System.getProperty("CHECK_GRM","true").equalsIgnoreCase("true")){
			assertEquals(GRMEdgeTestUtil.getEndPointsFromGRM(GRMEdgeTestConstants.KUBE_TEST_POD_ONLY_ENDPOINT_NAME,"LAB").getServiceEndPointList().size(),0);
		}
		System.out.println("=================================");
	}
	
	/*
	 * Lots Endpoints being created. Need to make sure one grm edge returns the same number as multiple grm edges
	 */
	@Test
	public void testManyEndPointsRestartsWithMultipleGRMEdges() throws InterruptedException, JSONException{
		
		GRMEdgeTestUtil.setConfigValue(GRMEdgeTestUtil.getCurrentConfig(),"CPFRUN_GRMEDGE_GRM_WRITE_BEHIND_ENABLE=false");
		Thread.sleep(100);
		GRMEdgeTestUtil.setConfigValue(GRMEdgeTestUtil.getCurrentConfig(),"GRM_EDGE_FINDRUNNING_RETRIEVE_FROM_GRM_REST=false");
		Thread.sleep(100);
		GRMEdgeTestUtil.setConfigValue(GRMEdgeTestUtil.getCurrentConfig(),"CPFRUN_GRMEDGE_DB_WRITE_BEHIND_ENABLE=false");
		Thread.sleep(100);
		
		GRMEdgeTestUtil.scaleDeployment(GRMEdgeTestConstants.GRM_EDGE_SERVICE_NAME, "0", GRMEdgeTestConstants.namespace);
		Thread.sleep(1000);
		GRMEdgeTestUtil.scaleDeployment(GRMEdgeTestConstants.GRM_EDGE_SERVICE_NAME, "1", GRMEdgeTestConstants.namespace);
		Thread.sleep(GRMEdgeTestConstants.SLEEP*2);
		
		int numberOfNodes = GRMEdgeTestUtil.getNumberOfNodes()-1; // -1 to remove master node
		int targetEndpoints = 25; //may not be this exact number
		GRMEdgeTestUtil.createDummyServices(targetEndpoints/numberOfNodes);
		Thread.sleep(5000);
		GRMEdgeTestUtil.createDummyPods(targetEndpoints/numberOfNodes,numberOfNodes);
		
		Thread.sleep(GRMEdgeTestConstants.SLEEP*3);
		List<ServiceEndPoint> seps = (List<ServiceEndPoint>) GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunning(GRMEdgeTestConstants.DUMMY_TESTCASE_SERVICE_NAME+"*", GRMEdgeTestConstants.namespace,"LAB").getServiceEndPointList();
		int sizeToCheck = seps.size();
		assertTrue("Endpoints exist but none were found. Result on Check is: ",sizeToCheck > 0);
		
		GRMEdgeTestUtil.scaleDeployment(GRMEdgeTestConstants.GRM_EDGE_SERVICE_NAME, "0", GRMEdgeTestConstants.namespace);
		GRMEdgeTestUtil.scaleDeployment(GRMEdgeTestConstants.GRM_EDGE_SERVICE_NAME, "3", GRMEdgeTestConstants.namespace);
		Thread.sleep(GRMEdgeTestConstants.SLEEP*3);
		
		GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunningWithAutomatedSleep(GRMEdgeTestConstants.DUMMY_TESTCASE_SERVICE_NAME+"*", "LAB", GRMEdgeTestConstants.SLEEP*3,sizeToCheck);
		
		GRMEdgeTestUtil.deleteDummyPods(targetEndpoints/numberOfNodes,numberOfNodes);
		Thread.sleep(5000);
		GRMEdgeTestUtil.deleteDummyServices(targetEndpoints/numberOfNodes);
		
		Thread.sleep(5000);
		GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunningWithAutomatedSleep(GRMEdgeTestConstants.DUMMY_TESTCASE_SERVICE_NAME+"*", "LAB", GRMEdgeTestConstants.SLEEP*3,0);
		
		GRMEdgeTestUtil.createNamespace(GRMEdgeTestConstants.dummynamespace);
		GRMEdgeTestUtil.createDummyServices (targetEndpoints/numberOfNodes);
		Thread.sleep(5000);
		GRMEdgeTestUtil.createDummyPods(targetEndpoints/numberOfNodes,numberOfNodes);
		
		Thread.sleep(GRMEdgeTestConstants.SLEEP*2);
		
		//size could be different depending on which nodes the pods were put on.
		seps = (List<ServiceEndPoint>) GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunning(GRMEdgeTestConstants.DUMMY_TESTCASE_SERVICE_NAME+"*", GRMEdgeTestConstants.namespace,"LAB").getServiceEndPointList();
		sizeToCheck = seps.size();
		assertTrue("Endpoints exist but none were found. Result on Check is: ",sizeToCheck > 0);
		
		GRMEdgeTestUtil.scaleDeployment(GRMEdgeTestConstants.GRM_EDGE_SERVICE_NAME, "0", GRMEdgeTestConstants.namespace);
		Thread.sleep(1000);
		GRMEdgeTestUtil.scaleDeployment(GRMEdgeTestConstants.GRM_EDGE_SERVICE_NAME, "1", GRMEdgeTestConstants.namespace);
		Thread.sleep(GRMEdgeTestConstants.SLEEP*3);
		
		GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunningWithAutomatedSleep(GRMEdgeTestConstants.DUMMY_TESTCASE_SERVICE_NAME+"*", "LAB", GRMEdgeTestConstants.SLEEP*3,sizeToCheck);
		
		GRMEdgeTestUtil.deleteDummyPods(targetEndpoints/numberOfNodes,numberOfNodes);
		Thread.sleep(5000);
		GRMEdgeTestUtil.deleteDummyServices(targetEndpoints/numberOfNodes);
		
		GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunningWithAutomatedSleep(GRMEdgeTestConstants.DUMMY_TESTCASE_SERVICE_NAME+"*", "LAB", GRMEdgeTestConstants.SLEEP*3,0);
		
		GRMEdgeTestUtil.deleteNamespace(GRMEdgeTestConstants.dummynamespace);
		
	}
	
	/*
	 * Lots Endpoints being created and deleted at the same time.
	 */
	@Test
	public void testManyEndPointsDeletedAtSameTime() throws InterruptedException, JSONException{
		
		GRMEdgeTestUtil.setConfigValue(GRMEdgeTestUtil.getCurrentConfig(),"CPFRUN_GRMEDGE_GRM_WRITE_BEHIND_ENABLE=false");
		Thread.sleep(100);
		GRMEdgeTestUtil.setConfigValue(GRMEdgeTestUtil.getCurrentConfig(),"GRM_EDGE_FINDRUNNING_RETRIEVE_FROM_GRM_REST=false");
		Thread.sleep(100);
		GRMEdgeTestUtil.setConfigValue(GRMEdgeTestUtil.getCurrentConfig(),"CPFRUN_GRMEDGE_DB_WRITE_BEHIND_ENABLE=false");
		Thread.sleep(100);
		
		GRMEdgeTestUtil.scaleDeployment(GRMEdgeTestConstants.GRM_EDGE_SERVICE_NAME, "0", GRMEdgeTestConstants.namespace);
		Thread.sleep(1000);
		GRMEdgeTestUtil.scaleDeployment(GRMEdgeTestConstants.GRM_EDGE_SERVICE_NAME, "1", GRMEdgeTestConstants.namespace);
		Thread.sleep(GRMEdgeTestConstants.SLEEP);
		
		int numberOfNodes = GRMEdgeTestUtil.getNumberOfNodes()-1; // -1 to remove master node
		int targetEndpoints = 25; //may not be this exact number
		GRMEdgeTestUtil.createDummyServices(targetEndpoints/numberOfNodes);
		GRMEdgeTestUtil.createDummyPods(targetEndpoints/numberOfNodes,numberOfNodes);
		
		Thread.sleep(GRMEdgeTestConstants.SLEEP*5);
		List<ServiceEndPoint> seps = (List<ServiceEndPoint>) GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunning(GRMEdgeTestConstants.DUMMY_TESTCASE_SERVICE_NAME+"*", GRMEdgeTestConstants.namespace,"LAB").getServiceEndPointList();
		int sizeToCheck = seps.size();
		assertTrue("Endpoints exist but none were found. Result on Check is: ",sizeToCheck > 0);
		
		GRMEdgeTestUtil.deleteNamespace(GRMEdgeTestConstants.dummynamespace);
		
		GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunningWithAutomatedSleep(GRMEdgeTestConstants.DUMMY_TESTCASE_SERVICE_NAME+"*", "LAB", GRMEdgeTestConstants.SLEEP*3,0);
		
	}

	/*
	 * Two service end points being created
	 */
	@Test 
	public void addTwoMatchingPodAndServiceTestWildCard() throws InterruptedException{

		System.out.println("=================================");
		System.out.println("Beginning test: " +Thread.currentThread().getStackTrace()[1].getMethodName());
		
		GRMEdgeTestUtil.addRC(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_2, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_2, "1",GRMEdgeTestConstants.namespace);
		GRMEdgeTestUtil.addService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_2, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_2,GRMEdgeTestConstants.namespace);
		
		GRMEdgeTestUtil.addRC(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_1, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_1, "1",GRMEdgeTestConstants.namespace);
		GRMEdgeTestUtil.addService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_1, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_1,GRMEdgeTestConstants.namespace);
		
		GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunningWithAutomatedSleep(GRMEdgeUtil.convertSEPNamespaceToDots(GRMEdgeTestConstants.namespace+".grmedge-testcase-m*"),"LAB",GRMEdgeTestConstants.SLEEP*3,2);
		System.out.println("=================================");
	
	}
	
	@Test
	public void testSynchronizer() throws JSONException, InterruptedException{
		System.out.println("=================================");
		System.out.println("Beginning test: " +Thread.currentThread().getStackTrace()[1].getMethodName());
		
		//Tests Scenarios 2,8,11,12
		//Tests unable to test: 1, 7 and 5 are hard to test and should never occur
		//Tests inherintly done by all test cases: 3, 4, 6, 9, 10
		//scenarios are described in CacheSynchronizer.java
		
		long syncSleep = 5000;
		GRMEdgeTestUtil.setConfigValue(GRMEdgeTestUtil.getCurrentConfig(),"PLATFORM_RUNTIME_GRMEDGE_DEFAULT_CACHESYNCHRONIZER_SLEEP="+String.valueOf(syncSleep));
		GRMEdgeTestUtil.scaleDeployment(GRMEdgeTestConstants.GRM_EDGE_SERVICE_NAME, "0",GRMEdgeTestConstants.namespace);
		//allow old GRMEdge to use proper shutdownhook
		GRMEdgeTestUtil.scaleDeployment(GRMEdgeTestConstants.GRM_EDGE_SERVICE_NAME, "1",GRMEdgeTestConstants.namespace);
		//allow grm to start up
		Thread.sleep(GRMEdgeTestConstants.SLEEP*2);
		//SCENARIO 2
		//add endpoint to grm 
		GRMEdgeTestUtil.addSyncTestEndPoint(1);
//		Thread.sleep(syncSleep);
		//get call to Edge to pull endpoint should be 1
		GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunningWithAutomatedSleep(GRMEdgeTestConstants.SYNC_TEST_CASE_NAME, "LAB",syncSleep*4,1);
		//add another endpoint to grm
		GRMEdgeTestUtil.addSyncTestEndPoint(2);
		//get call to Edge to pull endpoints should be 2
		//should be 2
		GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunningWithAutomatedSleep(GRMEdgeTestConstants.SYNC_TEST_CASE_NAME, "LAB",syncSleep*4,2);
		
		//SCENARIO 8
		GRMEdgeTestUtil.deleteSyncTestEndPoint(1);
		//delete endpoint from grm
		//sleep
		Thread.sleep(5000);
		//get call to edge should be 1 
		GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunningWithAutomatedSleep(GRMEdgeTestConstants.SYNC_TEST_CASE_NAME, "LAB",syncSleep*6,1);
		
		//SCENARIO 12
		//modify SEP in GRM
		GRMEdgeTestUtil.modifySyncTestEndPoint(2);
		//sleep
		//get call to Edge should have 1 and equal modified value //SCENARIO 2
		Thread.sleep(syncSleep*4);
		GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunningWithAutomatedSleep(GRMEdgeTestConstants.SYNC_TEST_CASE_NAME, "LAB",syncSleep*6,1);
		GRMEdgeTestUtil.getEndPointsfromGRMWithAutomatedSleep(GRMEdgeTestConstants.SYNC_TEST_CASE_NAME, "LAB",syncSleep*5,1);
		List<ServiceEndPoint> eps = GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunning(GRMEdgeTestConstants.SYNC_TEST_CASE_NAME, GRMEdgeTestConstants.namespace,"LAB").getServiceEndPointList();
		List<ServiceEndPoint> grmeps = GRMEdgeTestUtil.getEndPointsFromGRM(GRMEdgeTestConstants.SYNC_TEST_CASE_NAME, "LAB").getServiceEndPointList();
		System.out.println(eps.size());
		System.out.println(grmeps.size());
        assertTrue("Endpoints did not equal each other after modify in GRM",GRMEdgeUtil.checkEqualServiceEndPoints(eps.get(0), grmeps.get(0)));
		
		//SCENARIO 8 again
		//delete 1
		GRMEdgeTestUtil.deleteSyncTestEndPoint(2);
		//sleep
		//get call to edge should be 0
		GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunningWithAutomatedSleep(GRMEdgeTestConstants.SYNC_TEST_CASE_NAME, "LAB",GRMEdgeTestConstants.SLEEP,0);
	
		
		//This test will make sure deletion in GRM will not delete current cluster endpoints
		GRMEdgeTestUtil.addRC(GRMEdgeTestConstants.SYNC_TEST_CASE_K8_SERVICE_NAME, GRMEdgeTestConstants.SYNC_TEST_CASE_K8_SERVICE_NAME, "1", GRMEdgeTestConstants.namespace);
		GRMEdgeTestUtil.addService(GRMEdgeTestConstants.SYNC_TEST_CASE_K8_SERVICE_NAME, GRMEdgeTestConstants.SYNC_TEST_CASE_K8_SERVICE_NAME, GRMEdgeTestConstants.namespace);
		GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunningWithAutomatedSleep(GRMEdgeTestConstants.SYNC_TEST_CASE_NAME,"LAB",GRMEdgeTestConstants.SLEEP*3,1);
		Thread.sleep(5000);
		//This makes sure it gets delete from GRM
		GRMEdgeTestUtil.deleteSEPFromGRMSpecificWithCheck(GRMEdgeTestConstants.SYNC_TEST_CASE_NAME, "LAB");
		Thread.sleep(30000);
		//This test makes sure we add back in GRM Endpoint and still exists in cache
		GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunningWithAutomatedSleep(GRMEdgeTestConstants.SYNC_TEST_CASE_NAME,"LAB",GRMEdgeTestConstants.SLEEP*3,1);
		GRMEdgeTestUtil.getEndPointsfromGRMWithAutomatedSleep(GRMEdgeTestConstants.SYNC_TEST_CASE_NAME,"LAB",GRMEdgeTestConstants.SLEEP*3,1);
		
		//This test will make sure cache does not get overriden by faulty GRM data and will send data back to GRM if it is faulty to update it
		//edit same EP in GRM
		//Wait 30 seconds
		//Check to see if it has been re-corrected to proper value
		GRMEdgeTestUtil.updateSyncEndpointInGRMToHttps();
		Thread.sleep(30000);
		List<ServiceEndPoint> seps = GRMEdgeTestUtil.getEndPointsFromGRM(GRMEdgeTestConstants.SYNC_TEST_CASE_NAME,"LAB").getServiceEndPointList();
		assertEquals(1,seps.size());
		assertEquals(seps.get(0).getProtocol(),"http");
		String clusterName = GRMEdgeUtil.getClusterNameFromSEP(seps.get(0));
		
		//This test will make sure to delete endpoints in GRM that do not exist in current cluster
		//Add endpoint in a different port with same key to GRM
		GRMEdgeTestUtil.addSyncTestEndPointWithSameClusterInfo(clusterName);
		Thread.sleep(30000);
		//check to see if query is to GRM is 0
		seps = GRMEdgeTestUtil.getEndPointsFromGRM(GRMEdgeTestConstants.SYNC_TEST_CASE_NAME,"LAB").getServiceEndPointList();
		assertEquals(1,seps.size());
		assertEquals(seps.get(0).getProtocol(),"http");
		assertNotEquals("8085",seps.get(0).getListenPort());
				
		GRMEdgeTestUtil.setConfigValue(GRMEdgeTestUtil.getCurrentConfig(),"PLATFORM_RUNTIME_GRMEDGE_DEFAULT_CACHESYNCHRONIZER_SLEEP="+syncTimeOriginal);
		System.out.println("=================================");
	}
	
	/*
	 * Pod with multiple labels should give the matching service end-point
	 */
	@Test 
	public void addMultipleLabelsPodwithMatchingService() throws InterruptedException{

		System.out.println("=================================");
		System.out.println("Beginning test: " +Thread.currentThread().getStackTrace()[1].getMethodName());

		long syncSleep = 5000;
		
		GRMEdgeTestUtil.addRCLabel(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_1, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_1, "1",GRMEdgeTestConstants.testnamespace,"Runing");
		GRMEdgeTestUtil.addService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_1, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_1,GRMEdgeTestConstants.testnamespace);
		
		Thread.sleep(syncSleep*1);
		GRMEdgeTestUtil.getEndPointsfromKubernetesWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_1,"DEV",GRMEdgeTestConstants.SLEEP*3,1);
		List<ServiceEndPoint> seps = GRMEdgeTestUtil.getEndPointsFromKubernetes(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_1,"DEV").getServiceEndPointList();
		
		//verify the result end-point
		assertEquals(seps.get(0).getName().equalsIgnoreCase(GRMEdgeUtil.convertSEPNamespaceToDots(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_1)),true);
		GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunningWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_1,"DEV",GRMEdgeTestConstants.SLEEP,1);
		if (System.getProperty("CHECK_GRM","true").equalsIgnoreCase("true")){
			assertEquals(GRMEdgeTestUtil.getEndPointsFromGRM(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_1,"DEV").getServiceEndPointList().size(),1);
		}
		System.out.println("=================================");
	}

		
	/*
	 * Service with multiple labels which pod doesn't have should not provide the end-point
	 */
	@Test 
	public void addMultipleLabelsServicesWhichhPodNotHave() throws InterruptedException{

		System.out.println("=================================");
		System.out.println("Beginning test: " +Thread.currentThread().getStackTrace()[1].getMethodName());

		
		GRMEdgeTestUtil.addRC(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_1, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_1, "1",GRMEdgeTestConstants.testnamespace);
		GRMEdgeTestUtil.addServiceLabel(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_1, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_1,GRMEdgeTestConstants.testnamespace,"Serving");
		
		GRMEdgeTestUtil.getEndPointsfromKubernetesWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_1,"DEV",GRMEdgeTestConstants.SLEEP,0);
		List<ServiceEndPoint> seps = GRMEdgeTestUtil.getEndPointsFromKubernetes(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_1,"DEV").getServiceEndPointList();
		assertEquals(seps.size(),0);
		if (System.getProperty("CHECK_GRM","true").equalsIgnoreCase("true")){
			assertEquals(GRMEdgeTestUtil.getEndPointsFromGRM(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_1,"DEV").getServiceEndPointList().size(),0);
		}
		System.out.println("=================================");
	}
	
	@Test 
	public void getEndPointWithMatchingServiceNameAndEnvFromKubernetesServiceCache() throws InterruptedException{

		System.out.println("=================================");
		System.out.println("Beginning test: " +Thread.currentThread().getStackTrace()[1].getMethodName());
		
	
		GRMEdgeTestUtil.addRC(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_1, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_1, "1",GRMEdgeTestConstants.testnamespace);
		GRMEdgeTestUtil.addService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_1, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_1,GRMEdgeTestConstants.testnamespace);
	
		Thread.sleep(GRMEdgeTestConstants.SLEEP);
		FindServiceEndPointResponse  sepResponse=GRMEdgeTestUtil.getEndPointsFromKubernetesServiceCache(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_1,"DEV");
		List<ServiceEndPoint> seps=sepResponse.getServiceEndPointList();
		//check endpoint assertEquals(GRMEdgeTestUtil, "serviceName");
		assertEquals(1, seps.size());
		ServiceEndPoint serviceEndPoint = seps.get(0);
		assertEquals(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_1,serviceEndPoint.getName());
		assertEquals("DEV",GRMEdgeTestUtil.getEnvName(serviceEndPoint));
		
		System.out.println("=================================");
	
				
	}
	
	
	@Test 
	public void getEndPointWithWildcardServiceNameAndMatchingEnvFromKubernetesServiceCache() throws InterruptedException{

		System.out.println("=================================");
		System.out.println("Beginning test: " +Thread.currentThread().getStackTrace()[1].getMethodName());
	
		
		GRMEdgeTestUtil.addRC(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_2, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_2, "1",GRMEdgeTestConstants.namespace);
		GRMEdgeTestUtil.addService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_2, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_2,GRMEdgeTestConstants.namespace);
		Thread.sleep(GRMEdgeTestConstants.SLEEP);
		FindServiceEndPointResponse  sepResponse=GRMEdgeTestUtil.getEndPointsFromKubernetesServiceCache("%2A","LAB");
		List<ServiceEndPoint> seps=sepResponse.getServiceEndPointList();
		//filtering the services as we are getting other default services
		assertTrue(seps.size()>0);
		ServiceEndPoint labEndPoint=GRMEdgeTestUtil.getEndpointByName(seps,GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_2);
		assertNotNull(labEndPoint);
		assertEquals(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_2,labEndPoint.getName());
		assertEquals("LAB",GRMEdgeTestUtil.getEnvName(labEndPoint));
	
		//Verify the result
	    System.out.println("=================================");
	
				
		}
	
	
	
	@Test 
	public void getEndPointWithWildcardEnvNameAndMatchingServiceNameFromKubernetesServiceCache() throws InterruptedException{

		System.out.println("=================================");
		System.out.println("Beginning test: " +Thread.currentThread().getStackTrace()[1].getMethodName());
		GRMEdgeTestUtil.addRC(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_2, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_2, "1",GRMEdgeTestConstants.namespace);
		GRMEdgeTestUtil.addService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_2, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_2,GRMEdgeTestConstants.namespace);
		Thread.sleep(GRMEdgeTestConstants.SLEEP);
		FindServiceEndPointResponse  sepResponse=GRMEdgeTestUtil.getEndPointsFromKubernetesServiceCache(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_2,"%2A");
		List<ServiceEndPoint> seps=sepResponse.getServiceEndPointList();
		//Verify the reesult
		assertEquals(1,seps.size());
		ServiceEndPoint serviceEndPoint = seps.get(0);
		assertEquals(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_2,serviceEndPoint.getName());
		assertEquals("LAB",GRMEdgeTestUtil.getEnvName(serviceEndPoint));
		
		System.out.println("=================================");
				
	}
	
	
	@Test 
	public void getEndPointWithWildcardEnvNameAndMatchingServiceNameFromKubernetesServiceCacheDevEnv() throws InterruptedException{

		System.out.println("=================================");
		System.out.println("Beginning test: " +Thread.currentThread().getStackTrace()[1].getMethodName());
		Thread.sleep(1100 );
		GRMEdgeTestUtil.addRC(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_1, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_1, "1",GRMEdgeTestConstants.testnamespace);
		GRMEdgeTestUtil.addService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_1, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_1,GRMEdgeTestConstants.testnamespace);
		Thread.sleep(GRMEdgeTestConstants.SLEEP);
		FindServiceEndPointResponse  sepResponse=GRMEdgeTestUtil.getEndPointsFromKubernetesServiceCache(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_1,"%2A");
		List<ServiceEndPoint> seps=sepResponse.getServiceEndPointList();
		//Verify the reesult
		assertEquals(1,seps.size());
		ServiceEndPoint serviceEndPoint = seps.get(0);
		assertEquals(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_1,serviceEndPoint.getName());
		assertEquals("DEV",GRMEdgeTestUtil.getEnvName(serviceEndPoint));
				
		System.out.println("=================================");
				
	}

	
	
	@Test 
	public void getEndPointWithWildcardEnvNameAndWildcardServiceNameFromKubernetesServiceCache() throws InterruptedException{

		System.out.println("=================================");
		System.out.println("Beginning test: " +Thread.currentThread().getStackTrace()[1].getMethodName());
	
		
		GRMEdgeTestUtil.addRC(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_2, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_2, "1",GRMEdgeTestConstants.namespace);
		GRMEdgeTestUtil.addService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_2, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_2,GRMEdgeTestConstants.namespace);
		
		Thread.sleep(1100 );
		
		GRMEdgeTestUtil.addRC(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_1, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_1, "1",GRMEdgeTestConstants.testnamespace);
		GRMEdgeTestUtil.addService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_1, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_1,GRMEdgeTestConstants.testnamespace);
	
		Thread.sleep(GRMEdgeTestConstants.SLEEP*3);
		FindServiceEndPointResponse  sepResponse=GRMEdgeTestUtil.getEndPointsFromKubernetesServiceCache("%2A","%2A");
		//filtering the services as we are getting other default services
		
		List<ServiceEndPoint> seps=sepResponse.getServiceEndPointList();
		// LAB end-point
		assertTrue(seps.size()>0);
		ServiceEndPoint labEndPoint=GRMEdgeTestUtil.getEndpointByName(seps,GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_2);
		assertNotNull(labEndPoint);
		assertEquals(GRMEdgeUtil.convertSEPNamespaceToDots(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_2),labEndPoint.getName());
		assertEquals("LAB",GRMEdgeTestUtil.getEnvName(labEndPoint));
		//DEV end-point
		ServiceEndPoint devEndPoint=GRMEdgeTestUtil.getEndpointByName(seps,GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_1);
		assertNotNull(devEndPoint);
		assertEquals(GRMEdgeUtil.convertSEPNamespaceToDots(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_1),devEndPoint.getName());
		assertEquals("DEV",GRMEdgeTestUtil.getEnvName(devEndPoint));
		
		System.out.println("=================================");
	
				
	}
	

	
	@Test
	public void multipleRouteOffersForService() throws InterruptedException{
		System.out.println("=================================");
		System.out.println("Beginning test: " +Thread.currentThread().getStackTrace()[1].getMethodName());
		
		GRMEdgeTestUtil.addRCRouteOffer(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_1, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_1, "1",GRMEdgeTestConstants.namespace,"DEFAULT");
		GRMEdgeTestUtil.addServiceRouteOffer(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_1, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_1,GRMEdgeTestConstants.namespace,"DEFAULT");
		
		Thread.sleep(1000);
		GRMEdgeTestUtil.addRCRouteOffer(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_1+"r", GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_1, "1",GRMEdgeTestConstants.namespace,"TEST");
		GRMEdgeTestUtil.addServiceRouteOffer(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_1+"r", GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_1,GRMEdgeTestConstants.namespace,"TEST");
		
		GRMEdgeTestUtil.getEndPointsfromKubernetesWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_1,"LAB",GRMEdgeTestConstants.SLEEP*3,2);
		GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunningWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_1,"LAB",GRMEdgeTestConstants.SLEEP*3,2);
		List<ServiceEndPoint> seps = GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunning(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_1,GRMEdgeTestConstants.namespace,"LAB").getServiceEndPointList();
		GRMEdgeTestUtil.getEndPointsfromGRMWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_1,"LAB",GRMEdgeTestConstants.SLEEP*3,2);		
		
		ServiceEndPoint sep = seps.get(0);
		ServiceEndPoint sep2 = seps.get(1);
		
		assertTrue("Route offer did not equal DEFAULT OR TEST",sep.getRouteOffer().equalsIgnoreCase("DEFAULT") || sep.getRouteOffer().equalsIgnoreCase("TEST"));
		assertTrue("Route offer did not equal DEFAULT or TEST",sep2.getRouteOffer().equalsIgnoreCase("DEFAULT") || sep2.getRouteOffer().equalsIgnoreCase("TEST"));
		assertNotEquals("The two endpoints found had the same routeOffer. There should be one DEFAULT and one TEST",sep.getRouteOffer(),sep2.getRouteOffer());
		
		System.out.println("=================================");
	}
	
	@Test
	public void multipleRouteOffersForServiceWithRestart() throws InterruptedException{
		System.out.println("Calling child multipleRouteOffers test");
		multipleRouteOffersForService();
		
		GRMEdgeTestUtil.scaleDeployment(GRMEdgeTestConstants.GRM_EDGE_SERVICE_NAME, "0",GRMEdgeTestConstants.namespace);
		GRMEdgeTestUtil.scaleDeployment(GRMEdgeTestConstants.GRM_EDGE_SERVICE_NAME, "1",GRMEdgeTestConstants.namespace);
		//allow grm to start up
		Thread.sleep(GRMEdgeTestConstants.SLEEP*2);		
		
		List<ServiceEndPoint> seps = GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunning(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_1,GRMEdgeTestConstants.namespace,"LAB").getServiceEndPointList();
		GRMEdgeTestUtil.getEndPointsfromGRMWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_1,"LAB",GRMEdgeTestConstants.SLEEP*3,2);		
		
		ServiceEndPoint sep = seps.get(0);
		ServiceEndPoint sep2 = seps.get(1);
		
		assertTrue("Route offer did not equal DEFAULT OR TEST",sep.getRouteOffer().equalsIgnoreCase("DEFAULT") || sep.getRouteOffer().equalsIgnoreCase("TEST"));
		assertTrue("Route offer did not equal DEFAULT or TEST",sep2.getRouteOffer().equalsIgnoreCase("DEFAULT") || sep2.getRouteOffer().equalsIgnoreCase("TEST"));
		assertNotEquals("The two endpoints found had the same routeOffer. There should be one DEFAULT and one TEST",sep.getRouteOffer(),sep2.getRouteOffer());
	}
	
	/*
	 * Tests findRunning returning multiple versions
	 * 
	 */
	@Test
	public void addMatchingTwoPodAndServiceFindRunningMultipleVersionCheck() throws InterruptedException, JSONException{
		System.out.println("=================================");
		System.out.println("Beginning test: " +Thread.currentThread().getStackTrace()[1].getMethodName());
		
		long syncSleep = 5000;
		GRMEdgeTestUtil.setConfigValue(GRMEdgeTestUtil.getCurrentConfig(),"PLATFORM_RUNTIME_GRMEDGE_DEFAULT_CACHESYNCHRONIZER_SLEEP="+String.valueOf(syncSleep));
		Thread.sleep(Long.valueOf(syncTimeOriginal)); //sleep so that the thread will be started up. It can only check value once the thread starts up
		
		GRMEdgeTestUtil.addRCVersion(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_1, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_1, "1",GRMEdgeTestConstants.namespace,"10.1.1");
		GRMEdgeTestUtil.addServiceVersion(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_1, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_1,GRMEdgeTestConstants.namespace,"10.1.1");
		Thread.sleep(1000);
		//need to add another with the same app name but different rc name and different version
		GRMEdgeTestUtil.addVersionFindRunningTestEndPoint(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_1, 10, 1, "3");
		GRMEdgeTestUtil.addVersionFindRunningTestEndPoint(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_1, 10, 2, "1");
		
		//add in sync wait change
		Thread.sleep(syncSleep*1);
		
		GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunningWithAutomatedSleepVersion(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_1,"LAB",GRMEdgeTestConstants.SLEEP*3,1,10,1,"1");
		GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunningWithAutomatedSleepVersion(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_1,"LAB",GRMEdgeTestConstants.SLEEP*3,2,10,1,null);
		GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunningWithAutomatedSleepVersion(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_1,"LAB",GRMEdgeTestConstants.SLEEP*3,3,10,-1,null);
		//remove sync wait change
		
		GRMEdgeTestUtil.deleteVersionFindRunningTestEndPoint(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_1, 10, 1, "3");
		GRMEdgeTestUtil.deleteVersionFindRunningTestEndPoint(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_1, 10, 2, "1");
		System.out.println("=================================");
	}
	
	
	
	/*
	 * Makes sure a SEP is created with version
	 */
	@Test 
	public void addMatchingPodAndServiceVersion() throws InterruptedException{

		System.out.println("=================================");
		System.out.println("Beginning test: " +Thread.currentThread().getStackTrace()[1].getMethodName());

		GRMEdgeTestUtil.addRCVersion(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_1, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_1, "1",GRMEdgeTestConstants.namespace,"100.1.5");
		GRMEdgeTestUtil.addServiceVersion(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_1, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_1,GRMEdgeTestConstants.namespace,"100.1.5");
		
		GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunningWithAutomatedSleepVersion(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_1,"LAB",GRMEdgeTestConstants.SLEEP*3,1,100,1,"5");
		List<ServiceEndPoint> seps = GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunningVersion(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_1, GRMEdgeTestConstants.namespace, "LAB").getServiceEndPointList();
		assertEquals(seps.size(),1);
		assertEquals(seps.get(0).getVersion().getMajor(),100);
		assertEquals(seps.get(0).getVersion().getMinor(),1);
		assertEquals(seps.get(0).getVersion().getPatch(),"5");
		//check endpoint assertEquals(GRMEdgeTestUtil, "serviceName");
		assertEquals(seps.get(0).getName().equalsIgnoreCase(GRMEdgeUtil.convertSEPNamespaceToDots(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_1)),true);
		if (System.getProperty("CHECK_GRM","true").equalsIgnoreCase("true")){
			assertEquals(GRMEdgeTestUtil.getEndPointsFromGRMVersion(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_1,"LAB").getServiceEndPointList().size(),1);
		}
		System.out.println("=================================");
	}
	
	@Test
	public void testDataLoadingFromCassandra() throws InterruptedException, JSONException{
		System.out.println("=================================");
		System.out.println("Beginning test: " +Thread.currentThread().getStackTrace()[1].getMethodName());
		//SET GRM TO FALSE, SET CASSANDRA TO TRUE
		GRMEdgeTestUtil.setConfigValue(GRMEdgeTestUtil.getCurrentConfig(),"CPFRUN_GRMEDGE_GRM_WRITE_BEHIND_ENABLE=false");
		Thread.sleep(100);
		GRMEdgeTestUtil.setConfigValue(GRMEdgeTestUtil.getCurrentConfig(),"GRM_EDGE_FINDRUNNING_RETRIEVE_FROM_GRM_REST=false");
		Thread.sleep(100);
		GRMEdgeTestUtil.setConfigValue(GRMEdgeTestUtil.getCurrentConfig(),"CPFRUN_GRMEDGE_DB_WRITE_BEHIND_ENABLE=TRUE");
		Thread.sleep(100);
		//restart GRM Edge
		GRMEdgeTestUtil.scaleDeployment(GRMEdgeTestConstants.GRM_EDGE_SERVICE_NAME, "0",GRMEdgeTestConstants.namespace);
		GRMEdgeTestUtil.scaleDeployment(GRMEdgeTestConstants.GRM_EDGE_SERVICE_NAME, "1",GRMEdgeTestConstants.namespace);
		//allow grm to start up
		Thread.sleep(GRMEdgeTestConstants.SLEEP*2);
		//add in matching pod and service
		GRMEdgeTestUtil.addRC(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_1, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_1, "1",GRMEdgeTestConstants.namespace);
		GRMEdgeTestUtil.addService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_1, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_1,GRMEdgeTestConstants.namespace);
		GRMEdgeTestUtil.getEndPointsfromKubernetesWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_1,"LAB",GRMEdgeTestConstants.SLEEP*3,1);
		Thread.sleep(15000);
		//restart GRM Edge
		GRMEdgeTestUtil.scaleDeployment(GRMEdgeTestConstants.GRM_EDGE_SERVICE_NAME, "0",GRMEdgeTestConstants.namespace);
		//allow old GRMEdge to use proper shutdownhook
		Thread.sleep(GRMEdgeTestConstants.SLEEP);
		//Scale down old isntances so restart doesnt add them
		GRMEdgeTestUtil.deleteService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_1, GRMEdgeTestConstants.namespace);
		Thread.sleep(GRMEdgeTestConstants.SLEEP);
		GRMEdgeTestUtil.scaleDeployment(GRMEdgeTestConstants.GRM_EDGE_SERVICE_NAME, "1",GRMEdgeTestConstants.namespace);
		//allow grm to start up
		Thread.sleep(GRMEdgeTestConstants.SLEEP*2);
		//findRunning should return 1 endpoint
		GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunningWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_1, "LAB", GRMEdgeTestConstants.SLEEP*3, 1);			
		System.out.println("=================================");
	}
	
	@Test
	public void testDataLoadingFromGRM() throws InterruptedException, JSONException{
		System.out.println("=================================");
		System.out.println("Beginning test: " +Thread.currentThread().getStackTrace()[1].getMethodName());
		//SET GRM TO TRUE, SET CASSANDRA TO FALSE
		GRMEdgeTestUtil.setConfigValue(GRMEdgeTestUtil.getCurrentConfig(),"CPFRUN_GRMEDGE_GRM_WRITE_BEHIND_ENABLE=true");
		Thread.sleep(100);
		GRMEdgeTestUtil.setConfigValue(GRMEdgeTestUtil.getCurrentConfig(),"GRM_EDGE_FINDRUNNING_RETRIEVE_FROM_GRM_REST=true");
		Thread.sleep(100);
		GRMEdgeTestUtil.setConfigValue(GRMEdgeTestUtil.getCurrentConfig(),"CPFRUN_GRMEDGE_DB_WRITE_BEHIND_ENABLE=false");
		Thread.sleep(100);		
		//restart GRM Edge
		GRMEdgeTestUtil.scaleDeployment(GRMEdgeTestConstants.GRM_EDGE_SERVICE_NAME, "0",GRMEdgeTestConstants.namespace);
		//allow old GRMEdge to use proper shutdownhook
		Thread.sleep(GRMEdgeTestConstants.SLEEP);
		GRMEdgeTestUtil.scaleDeployment(GRMEdgeTestConstants.GRM_EDGE_SERVICE_NAME, "1",GRMEdgeTestConstants.namespace);
		//allow grm to start up
		Thread.sleep(GRMEdgeTestConstants.SLEEP*2);
		//add in matching pod and service
		GRMEdgeTestUtil.addRC(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_1, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_1, "1",GRMEdgeTestConstants.namespace);
		GRMEdgeTestUtil.addService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_1, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_1,GRMEdgeTestConstants.namespace);
		GRMEdgeTestUtil.getEndPointsfromKubernetesWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_1,"LAB",GRMEdgeTestConstants.SLEEP*3,1);
		Thread.sleep(1500);
		//restart GRM Edge
		GRMEdgeTestUtil.scaleDeployment(GRMEdgeTestConstants.GRM_EDGE_SERVICE_NAME, "0",GRMEdgeTestConstants.namespace);
		//Scale down old isntances so restart doesnt add them
		GRMEdgeTestUtil.deleteService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_1, GRMEdgeTestConstants.namespace);
		Thread.sleep(GRMEdgeTestConstants.SLEEP);
		GRMEdgeTestUtil.scaleDeployment(GRMEdgeTestConstants.GRM_EDGE_SERVICE_NAME, "1",GRMEdgeTestConstants.namespace);
		//allow grm to start up
		Thread.sleep(GRMEdgeTestConstants.SLEEP*2);
		//findRunning should return 1 endpoint
		GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunningWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_1, "LAB", GRMEdgeTestConstants.SLEEP*3, 1);		
		
		GRMEdgeTestUtil.scaleDeployment(GRMEdgeTestConstants.GRM_EDGE_SERVICE_NAME, "0",GRMEdgeTestConstants.namespace);
		GRMEdgeTestUtil.scaleDeployment(GRMEdgeTestConstants.GRM_EDGE_SERVICE_NAME, "1",GRMEdgeTestConstants.namespace);
		//allow grm to start up
		Thread.sleep(GRMEdgeTestConstants.SLEEP*2);			
		System.out.println("=================================");
	}
	
	@Test
	public void testDataLoadingFromGRMAfterCassandraEmpty() throws JSONException, InterruptedException{
		System.out.println("=================================");
		System.out.println("Beginning test: " +Thread.currentThread().getStackTrace()[1].getMethodName());
		//SET GRM TO TRUE, SET CASSANDRA TO FALSE
		GRMEdgeTestUtil.setConfigValue(GRMEdgeTestUtil.getCurrentConfig(),"CPFRUN_GRMEDGE_GRM_WRITE_BEHIND_ENABLE=true");
		Thread.sleep(100);
		GRMEdgeTestUtil.setConfigValue(GRMEdgeTestUtil.getCurrentConfig(),"GRM_EDGE_FINDRUNNING_RETRIEVE_FROM_GRM_REST=true");
		Thread.sleep(100);
		GRMEdgeTestUtil.setConfigValue(GRMEdgeTestUtil.getCurrentConfig(),"CPFRUN_GRMEDGE_DB_WRITE_BEHIND_ENABLE=false");
		Thread.sleep(100);		
		//restart GRM Edge
		GRMEdgeTestUtil.scaleDeployment(GRMEdgeTestConstants.GRM_EDGE_SERVICE_NAME, "0",GRMEdgeTestConstants.namespace);
		//allow old GRMEdge to use proper shutdownhook
		Thread.sleep(GRMEdgeTestConstants.SLEEP);
		GRMEdgeTestUtil.scaleDeployment(GRMEdgeTestConstants.GRM_EDGE_SERVICE_NAME, "1",GRMEdgeTestConstants.namespace);
		//allow grm to start up
		Thread.sleep(GRMEdgeTestConstants.SLEEP*2);		
//		add in matching pod and service
		GRMEdgeTestUtil.addRC(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_1, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_1, "1",GRMEdgeTestConstants.namespace);
		GRMEdgeTestUtil.addService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_1, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_1,GRMEdgeTestConstants.namespace);
		GRMEdgeTestUtil.getEndPointsfromKubernetesWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_1,"LAB",GRMEdgeTestConstants.SLEEP*3,1);
		Thread.sleep(1500);
		//SET GRM TO TRUE, CASSANDRA TO TRUE
		GRMEdgeTestUtil.setConfigValue(GRMEdgeTestUtil.getCurrentConfig(),"CPFRUN_GRMEDGE_DB_WRITE_BEHIND_ENABLE=true");
		Thread.sleep(100);		
		//restart GRM Edge
		GRMEdgeTestUtil.scaleDeployment(GRMEdgeTestConstants.GRM_EDGE_SERVICE_NAME, "0",GRMEdgeTestConstants.namespace);
		//allow old GRMEdge to use proper shutdownhook
		Thread.sleep(GRMEdgeTestConstants.SLEEP);
		//Scale down old isntances so restart doesnt add them
		GRMEdgeTestUtil.deleteService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_1, GRMEdgeTestConstants.namespace);
		Thread.sleep(GRMEdgeTestConstants.SLEEP);
		GRMEdgeTestUtil.scaleDeployment(GRMEdgeTestConstants.GRM_EDGE_SERVICE_NAME, "1",GRMEdgeTestConstants.namespace);
		//allow grm to start up
		Thread.sleep(GRMEdgeTestConstants.SLEEP*2);
		//findRunning should return 1 endpoint
		GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunningWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_1, "LAB", GRMEdgeTestConstants.SLEEP*3, 1);		
		GRMEdgeTestUtil.scaleDeployment(GRMEdgeTestConstants.GRM_EDGE_SERVICE_NAME, "0",GRMEdgeTestConstants.namespace);
		GRMEdgeTestUtil.scaleDeployment(GRMEdgeTestConstants.GRM_EDGE_SERVICE_NAME, "1",GRMEdgeTestConstants.namespace);
		//allow grm to start up
		Thread.sleep(GRMEdgeTestConstants.SLEEP*2);		
		System.out.println("=================================");
	}
	
	/*
	 * No SEP should be created
	 */
	@Test
	public void addServiceOnly() throws InterruptedException{

		System.out.println("=================================");
		System.out.println("Beginning test: " +Thread.currentThread().getStackTrace()[1].getMethodName());

		GRMEdgeTestUtil.addService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_ONLY_NAME, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_NO_MATCH_2,GRMEdgeTestConstants.namespace);
		
		//Adding in a sleep so that if changes were to happen, they would have plenty of time to occur
		Thread.sleep(GRMEdgeTestConstants.SLEEP);
		
		assertEquals(GRMEdgeTestUtil.getEndPointsFromKubernetes(GRMEdgeTestConstants.KUBE_TEST_SERVICE_ONLY_ENDPOINT_NAME,"LAB").getServiceEndPointList().size(),0);
		assertEquals(GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunning(GRMEdgeTestConstants.KUBE_TEST_SERVICE_ONLY_ENDPOINT_NAME, GRMEdgeTestConstants.namespace,"LAB").getServiceEndPointList().size(),0);
		if (System.getProperty("CHECK_GRM","true").equalsIgnoreCase("true")){
			assertEquals(GRMEdgeTestUtil.getEndPointsFromGRM(GRMEdgeTestConstants.KUBE_TEST_SERVICE_ONLY_ENDPOINT_NAME, "LAB").getServiceEndPointList().size(),0);
		}
		System.out.println("=================================");
	}
	
	/*
	 * No SEP should be created
	 */
	@Test
	public void addNonMatchingPodAndService() throws InterruptedException{

		System.out.println("=================================");
		System.out.println("Beginning test: " +Thread.currentThread().getStackTrace()[1].getMethodName());
		
		GRMEdgeTestUtil.addRC(GRMEdgeTestConstants.KUBE_TEST_POD_NO_MATCH_NAME, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_NO_MATCH_3, "1",GRMEdgeTestConstants.namespace);
		GRMEdgeTestUtil.addService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_NO_MATCH_NAME, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_NO_MATCH_4,GRMEdgeTestConstants.namespace);
		
		//Adding in a sleep so that if changes were to happen, they would have plenty of time to occur
		Thread.sleep(GRMEdgeTestConstants.SLEEP*2);
		//no service name to check for the pod
		assertEquals(GRMEdgeTestUtil.getEndPointsFromKubernetes(GRMEdgeTestConstants.KUBE_TEST_SERVICE_NO_MATCH_ENDPOINT_NAME,"LAB").getServiceEndPointList().size(),0);
		assertEquals(GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunning(GRMEdgeTestConstants.KUBE_TEST_SERVICE_NO_MATCH_ENDPOINT_NAME, GRMEdgeTestConstants.namespace,"LAB").getServiceEndPointList().size(),0);
		if (System.getProperty("CHECK_GRM","true").equalsIgnoreCase("true")){
			assertEquals(GRMEdgeTestUtil.getEndPointsFromGRM(GRMEdgeTestConstants.KUBE_TEST_SERVICE_NO_MATCH_ENDPOINT_NAME,"LAB").getServiceEndPointList().size(),0);
		}
		System.out.println("=================================");
	}
	
	/*
	 * We need to match on namespace as well when creating SEPs. This will add to two namespaces (and two env) and ensure that services arent matching to other namespaces when finding matching pods 
	 */
	@Test
	public void addMatchingPodAndServiceTwoNamespacesWithoutDuplicates() throws InterruptedException{
		Thread.sleep(1000);
		GRMEdgeTestUtil.addRC(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_1, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_1, "1",GRMEdgeTestConstants.namespace);
		GRMEdgeTestUtil.addService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_1, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_1,GRMEdgeTestConstants.namespace);
		
		GRMEdgeTestUtil.addRC(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_1+"d", GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_1, "1",GRMEdgeTestConstants.testnamespace);
		GRMEdgeTestUtil.addService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_1+"d", GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_1,GRMEdgeTestConstants.testnamespace);


		GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunningWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_1, "LAB", GRMEdgeTestConstants.SLEEP*3, 1);
		GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunningWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_1, "DEV", GRMEdgeTestConstants.SLEEP*3, 1);
		
		Thread.sleep(2000);
		GRMEdgeTestUtil.deleteService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_1, GRMEdgeTestConstants.namespace);
		
		Thread.sleep(3000);
		GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunningWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_1, "LAB", GRMEdgeTestConstants.SLEEP*3, 0);
		GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunningWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_1, "DEV", GRMEdgeTestConstants.SLEEP*3, 1);
		
		GRMEdgeTestUtil.deleteRc(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_1, GRMEdgeTestConstants.namespace);
		
		Thread.sleep(25000); //since results should be the same we want to allow some time for something to get messed up
		
		GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunningWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_1, "LAB", GRMEdgeTestConstants.SLEEP*3, 0);
		GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunningWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_1, "DEV", GRMEdgeTestConstants.SLEEP*3, 1);
		
		GRMEdgeTestUtil.deleteService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_1+"d",GRMEdgeTestConstants.testnamespace);
		GRMEdgeTestUtil.deleteRc(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_1+"d",GRMEdgeTestConstants.testnamespace);
		
		Thread.sleep(1000);
		GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunningWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_1, "LAB", GRMEdgeTestConstants.SLEEP*3, 0);
		GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunningWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_1, "DEV", GRMEdgeTestConstants.SLEEP*3, 0);
	}

	/*
	 * Makes sure a SEP is created
	 */
	@Test 
	public void addMatchingPodAndServiceWildCard() throws InterruptedException{

		System.out.println("=================================");
		System.out.println("Beginning test: " +Thread.currentThread().getStackTrace()[1].getMethodName());

		GRMEdgeTestUtil.addRC(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_1, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_1, "1",GRMEdgeTestConstants.namespace);
		GRMEdgeTestUtil.addService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_1, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_1,GRMEdgeTestConstants.namespace);
		GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunningWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_1.substring(0, GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_1.length()-2)+"*", "LAB", GRMEdgeTestConstants.SLEEP*3, 1);
		System.out.println("=================================");
	}
	
	/*
	 * Makes sure a SEP is created
	 */
	@Test 
	public void addMatchingPodAndService() throws InterruptedException{

		System.out.println("=================================");
		System.out.println("Beginning test: " +Thread.currentThread().getStackTrace()[1].getMethodName());

		GRMEdgeTestUtil.addRC(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_1, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_1, "1",GRMEdgeTestConstants.namespace);
		GRMEdgeTestUtil.addService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_1, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_1,GRMEdgeTestConstants.namespace);
		
		GRMEdgeTestUtil.getEndPointsfromKubernetesWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_1,"LAB",GRMEdgeTestConstants.SLEEP*3,1);
		GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunningWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_1, "LAB", GRMEdgeTestConstants.SLEEP*3, 1);
		List<ServiceEndPoint> seps = GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunning(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_1, GRMEdgeTestConstants.namespace, "LAB").getServiceEndPointList();
		assertEquals(seps.size(),1);
		//check endpoint assertEquals(GRMEdgeTestUtil, "serviceName");
		assertEquals(seps.get(0).getName().equalsIgnoreCase(GRMEdgeUtil.convertSEPNamespaceToDots(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_1)),true);
		if (System.getProperty("CHECK_GRM","true").equalsIgnoreCase("true")){
			assertEquals(GRMEdgeTestUtil.getEndPointsFromGRM(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_1,"LAB").getServiceEndPointList().size(),1);
		}
		System.out.println("=================================");
	}
	
	
	/*
	 * Two service end points being created
	 */
	@Test 
	public void addMatchingTwoPodsAndService() throws InterruptedException{

		System.out.println("=================================");
		System.out.println("Beginning test: " +Thread.currentThread().getStackTrace()[1].getMethodName());
		
		GRMEdgeTestUtil.addRC(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_2, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_2, "2",GRMEdgeTestConstants.namespace);
		GRMEdgeTestUtil.addService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_2, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_2,GRMEdgeTestConstants.namespace);
		
		GRMEdgeTestUtil.getEndPointsfromKubernetesWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_2,"LAB",GRMEdgeTestConstants.SLEEP*3,2);
		List<ServiceEndPoint> seps = GRMEdgeTestUtil.getEndPointsFromKubernetes(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_2,"LAB").getServiceEndPointList();
		assertEquals(GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunning(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_2, GRMEdgeTestConstants.namespace,"LAB").getServiceEndPointList().size(),2);
		if (System.getProperty("CHECK_GRM","true").equalsIgnoreCase("true")){
			assertEquals(GRMEdgeTestUtil.getEndPointsFromGRM(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_2,"LAB").getServiceEndPointList().size(),2);
		}
		//CHeck both endpoints
		assertEquals(seps.get(0).getName().equalsIgnoreCase(GRMEdgeUtil.convertSEPNamespaceToDots(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_2)),true);
		assertEquals(seps.get(1).getName().equalsIgnoreCase(GRMEdgeUtil.convertSEPNamespaceToDots(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_2)),true);

		System.out.println("=================================");
	}
	
	/*
	 * Creates two SEPS and deletes service. Makes sure they are deleted when service is deleted 
	 */
	@Test 
	public void addMatchingTwoPodsAndServiceDeleteService() throws InterruptedException{

		System.out.println("=================================");
		System.out.println("Beginning test: " +Thread.currentThread().getStackTrace()[1].getMethodName());

		GRMEdgeTestUtil.addRC(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_3, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_3, "2",GRMEdgeTestConstants.namespace);
		GRMEdgeTestUtil.addService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_3, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_3,GRMEdgeTestConstants.namespace);

		GRMEdgeTestUtil.getEndPointsfromKubernetesWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_3,"LAB",GRMEdgeTestConstants.SLEEP*3,2);
		assertEquals(GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunning(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_3, GRMEdgeTestConstants.namespace,"LAB").getServiceEndPointList().size(),2);
		if (System.getProperty("CHECK_GRM","true").equalsIgnoreCase("true")){
			assertEquals(GRMEdgeTestUtil.getEndPointsFromGRM(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_3,"LAB").getServiceEndPointList().size(),2);
		}
		
		Thread.sleep(GRMEdgeTestConstants.SLEEP);
		GRMEdgeTestUtil.deleteService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_3,GRMEdgeTestConstants.namespace);
		
		GRMEdgeTestUtil.getEndPointsfromKubernetesWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_3,"LAB",GRMEdgeTestConstants.SLEEP*3,0);
		GRMEdgeTestUtil.getEndPointsfromKubernetesWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_3,"LAB",GRMEdgeTestConstants.SLEEP*3,0);
		if (System.getProperty("CHECK_GRM","true").equalsIgnoreCase("true")){
			assertEquals(GRMEdgeTestUtil.getEndPointsFromGRM(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_3,"LAB").getServiceEndPointList().size(),0);
		}
		System.out.println("=================================");
	}
	
	@Test
	public void addMatchingPodAndServiceScaleUp() throws InterruptedException{

		System.out.println("=================================");
		System.out.println("Beginning test: " +Thread.currentThread().getStackTrace()[1].getMethodName());
		
		GRMEdgeTestUtil.addRC(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_1, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_1, "1",GRMEdgeTestConstants.namespace);
		GRMEdgeTestUtil.addService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_1, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_1,GRMEdgeTestConstants.namespace);
		
		GRMEdgeTestUtil.getEndPointsfromKubernetesWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_1,"LAB",GRMEdgeTestConstants.SLEEP*3,1);
		GRMEdgeTestUtil.getEndPointsfromKubernetesWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_1,"LAB",GRMEdgeTestConstants.SLEEP*3,1);
		if (System.getProperty("CHECK_GRM","true").equalsIgnoreCase("true")){
			assertEquals(GRMEdgeTestUtil.getEndPointsFromGRM(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_1,"LAB").getServiceEndPointList().size(),1);
		}
		GRMEdgeTestUtil.scaleRc(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_1, "2",GRMEdgeTestConstants.namespace);
		
		GRMEdgeTestUtil.getEndPointsfromKubernetesWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_1,"LAB",GRMEdgeTestConstants.SLEEP*3,2);
		GRMEdgeTestUtil.getEndPointsfromKubernetesWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_1,"LAB",GRMEdgeTestConstants.SLEEP*3,2);
		if (System.getProperty("CHECK_GRM","true").equalsIgnoreCase("true")){
			GRMEdgeTestUtil.getEndPointsfromGRMWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_1,"LAB",GRMEdgeTestConstants.SLEEP*3,2);
		}
		System.out.println("=================================");
	}
	
	@Test
	public void addMatchingTwoPodAndServiceScaleDown() throws InterruptedException{
	
		System.out.println("=================================");
		System.out.println("Beginning test: " +Thread.currentThread().getStackTrace()[1].getMethodName());			
		
		GRMEdgeTestUtil.addRC(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_2, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_2, "2",GRMEdgeTestConstants.namespace);
		GRMEdgeTestUtil.addService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_2, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_2,GRMEdgeTestConstants.namespace);

		GRMEdgeTestUtil.getEndPointsfromKubernetesWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_2,"LAB",GRMEdgeTestConstants.SLEEP*3,2);
		assertEquals(GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunning(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_2, GRMEdgeTestConstants.namespace,"LAB").getServiceEndPointList().size(),2);
		if (System.getProperty("CHECK_GRM","true").equalsIgnoreCase("true")){
			assertEquals(GRMEdgeTestUtil.getEndPointsFromGRM(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_2,"LAB").getServiceEndPointList().size(),2);
		}
		GRMEdgeTestUtil.scaleRc(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_2, "1",GRMEdgeTestConstants.namespace);
		
		GRMEdgeTestUtil.getEndPointsfromKubernetesWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_2,"LAB",GRMEdgeTestConstants.SLEEP*3,1);
		GRMEdgeTestUtil.getEndPointsfromKubernetesWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_2,"LAB",GRMEdgeTestConstants.SLEEP*3,1);
		if (System.getProperty("CHECK_GRM","true").equalsIgnoreCase("true")){
			assertEquals(GRMEdgeTestUtil.getEndPointsFromGRM(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_2,"LAB").getServiceEndPointList().size(),1);
		}
		System.out.println("=================================");
	}
	
	//There should be at least 1 SEP for grm edge that gets initialized
	@Test
	public void testCacheInitialization() throws InterruptedException{
	
		System.out.println("=================================");
		System.out.println("Beginning test: " +Thread.currentThread().getStackTrace()[1].getMethodName());
		
		//Look at SEP cache
		assertTrue(GRMEdgeTestUtil.getEndPointsFromKubernetes(GRMEdgeTestConstants.namespaceWithDotsNoEnv + "." + GRMEdgeTestConstants.GRM_EDGE_SERVICE_NAME,"LAB").getServiceEndPointList().size()>=1);
//		assertEquals(GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunning(GRMEdgeTestConstants.GRM_EDGE_NAMESPACE + "." + GRMEdgeTestConstants.GRM_EDGE_SERVICE_NAME,"LAB").getServiceEndPointList().size(),1);
		boolean found = false;
		if (System.getProperty("CHECK_GRM","true").equalsIgnoreCase("true")){
			List<ServiceEndPoint> seps = GRMEdgeTestUtil.getEndPointsFromGRM(System.getProperty("KUBE_API_GRM_NAMESPACE") + "." + GRMEdgeTestConstants.GRM_EDGE_SERVICE_NAME,"LAB").getServiceEndPointList();
			for(ServiceEndPoint sep: seps){
				if(sep.getHostAddress().equals(GRMEdgeTestUtil.getGRMEdgeHostIP())){
					found = true;
				}
			}
			assertTrue("Did not find the GRMEdge SEP in GRM",found);
		}
		System.out.println("=================================");
	}
	
	/*
	 * Invalid Service details
	 */
	@Test
	public void unhappyAddInvalidService() throws InterruptedException{
	
		System.out.println("=================================");
		System.out.println("Beginning test: " +Thread.currentThread().getStackTrace()[1].getMethodName());			
		
		GRMEdgeTestUtil.addInvalidService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_2, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_2,GRMEdgeTestConstants.namespace);
		GRMEdgeTestUtil.addRC(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_2, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_2, "1",GRMEdgeTestConstants.namespace);
		
		Thread.sleep(GRMEdgeTestConstants.SLEEP);
		
		List<ServiceEndPoint> seps = GRMEdgeTestUtil.getEndPointsFromKubernetes(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_2,"LAB").getServiceEndPointList();
		assertEquals(seps.size(),0);
		GRMEdgeTestUtil.getEndPointsfromKubernetesWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_2,"LAB",GRMEdgeTestConstants.SLEEP*1,0);
		if (System.getProperty("CHECK_GRM","true").equalsIgnoreCase("true")){
			assertEquals(GRMEdgeTestUtil.getEndPointsFromGRM(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_2,"LAB").getServiceEndPointList().size(),0);
		}
	}

	/*
	 * Invalid Pod details
	 */
	@Test
	public void unhappyAddInvalidPod() throws InterruptedException{
	
		System.out.println("=================================");
		System.out.println("Beginning test: " +Thread.currentThread().getStackTrace()[1].getMethodName());
	
		
		GRMEdgeTestUtil.addInvalidRC(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_2, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_2, "1",GRMEdgeTestConstants.namespace);
		GRMEdgeTestUtil.addService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_2,GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_2,GRMEdgeTestConstants.namespace);
		
		Thread.sleep(GRMEdgeTestConstants.SLEEP);
		
		List<ServiceEndPoint> seps = GRMEdgeTestUtil.getEndPointsFromKubernetes(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_2,"LAB").getServiceEndPointList();
		assertEquals(seps.size(),0);
		GRMEdgeTestUtil.getEndPointsfromKubernetesWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_2,"LAB",GRMEdgeTestConstants.SLEEP*3,0);
		if (System.getProperty("CHECK_GRM","true").equalsIgnoreCase("true")){
			assertEquals(GRMEdgeTestUtil.getEndPointsFromGRM(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_2,"LAB").getServiceEndPointList().size(),0);
		}
		System.out.println("=================================");
	}
	
	@Test
	public void addMatchingPodAndServiceRapidDeleteandAddFirst() throws InterruptedException{
		System.out.println("=================================");
		System.out.println("Beginning test: " +Thread.currentThread().getStackTrace()[1].getMethodName());
		GRMEdgeTestUtil.addRC(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_2, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_2, "2",GRMEdgeTestConstants.namespace);
		GRMEdgeTestUtil.addService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_2, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_2,GRMEdgeTestConstants.namespace);
		Thread.sleep(100);
		GRMEdgeTestUtil.scaleRc(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_2, "1",GRMEdgeTestConstants.namespace);
		GRMEdgeTestUtil.deleteService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_2,GRMEdgeTestConstants.namespace);
		Thread.sleep(100);
		GRMEdgeTestUtil.scaleRc(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_2, "0",GRMEdgeTestConstants.namespace);
		Thread.sleep(100);
		GRMEdgeTestUtil.scaleRc(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_2, "1",GRMEdgeTestConstants.namespace);
		GRMEdgeTestUtil.addService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_2,GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_2,GRMEdgeTestConstants.namespace);
		
		GRMEdgeTestUtil.getEndPointsfromKubernetesWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_2,"LAB",GRMEdgeTestConstants.SLEEP*3,1);
		GRMEdgeTestUtil.getEndPointsfromKubernetesWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_2,"LAB",GRMEdgeTestConstants.SLEEP*3,1);
		if (System.getProperty("CHECK_GRM","true").equalsIgnoreCase("true")){
			assertEquals(GRMEdgeTestUtil.getEndPointsFromGRM(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_2,"LAB").getServiceEndPointList().size(),1);
		}
		
		System.out.println("=================================");
	}
	
		//There should be at least 1 SEP for grm edge that gets initialized
	@Test
	public void testRestartGRMWithCacheInitialization() throws InterruptedException{
	
		System.out.println("=================================");
		System.out.println("Beginning test: " +Thread.currentThread().getStackTrace()[1].getMethodName());
		
		//let any write behinds occur.
		Thread.sleep(2000);
		
		//restart GRM
		GRMEdgeTestUtil.scaleDeployment(GRMEdgeTestConstants.GRM_EDGE_SERVICE_NAME, "0",GRMEdgeTestConstants.namespace);
		GRMEdgeTestUtil.scaleDeployment(GRMEdgeTestConstants.GRM_EDGE_SERVICE_NAME, "1",GRMEdgeTestConstants.namespace);
		//allow grm to start up
		Thread.sleep(GRMEdgeTestConstants.SLEEP*2);
		
		//Look at SEP cache
		assertTrue(GRMEdgeTestUtil.getEndPointsFromKubernetes(GRMEdgeTestConstants.namespaceWithDotsNoEnv + "." + GRMEdgeTestConstants.GRM_EDGE_SERVICE_NAME,"LAB").getServiceEndPointList().size()>=1);
//		assertEquals(GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunning(GRMEdgeTestConstants.GRM_EDGE_NAMESPACE + "." + GRMEdgeTestConstants.GRM_EDGE_SERVICE_NAME,"LAB").getServiceEndPointList().size(),1);
		boolean found = false;
		if (System.getProperty("CHECK_GRM","true").equalsIgnoreCase("true")){
			List<ServiceEndPoint> seps = GRMEdgeTestUtil.getEndPointsFromGRM(System.getProperty("KUBE_API_GRM_NAMESPACE") + "." + GRMEdgeTestConstants.GRM_EDGE_SERVICE_NAME,"LAB").getServiceEndPointList();
			for(ServiceEndPoint sep: seps){
				if(sep.getHostAddress().equals(GRMEdgeTestUtil.getGRMEdgeHostIP())){
					found = true;
				}
			}
			assertTrue("Did not find the GRMEdge SEP in GRM",found);
		}
		System.out.println("=================================");
	}
	
	@Test
	public void addMatchingPodAndServiceRapidDeleteAndAddServiceOnly() throws InterruptedException{
		System.out.println("=================================");
		System.out.println("Beginning test: " +Thread.currentThread().getStackTrace()[1].getMethodName());
		GRMEdgeTestUtil.addRC(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_2, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_2, "1",GRMEdgeTestConstants.namespace);
		GRMEdgeTestUtil.addService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_2, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_2,GRMEdgeTestConstants.namespace);
		Thread.sleep(1000);
		GRMEdgeTestUtil.deleteService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_2,GRMEdgeTestConstants.namespace);
		Thread.sleep(100);
		GRMEdgeTestUtil.addService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_2,GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_2,GRMEdgeTestConstants.namespace);
		Thread.sleep(100);
		GRMEdgeTestUtil.deleteService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_2,GRMEdgeTestConstants.namespace);
		Thread.sleep(100);
		GRMEdgeTestUtil.addService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_2,GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_2,GRMEdgeTestConstants.namespace);
		
		GRMEdgeTestUtil.getEndPointsfromKubernetesWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_2,"LAB",GRMEdgeTestConstants.SLEEP*3,1);
		GRMEdgeTestUtil.getEndPointsfromKubernetesWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_2,"LAB",GRMEdgeTestConstants.SLEEP*3,1);
		if (System.getProperty("CHECK_GRM","true").equalsIgnoreCase("true")){
			assertEquals(GRMEdgeTestUtil.getEndPointsFromGRM(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_2,"LAB").getServiceEndPointList().size(),1);
		}
		System.out.println("=================================");
	}
	
	@Test
	public void addMatchingPodAndServiceRapidDeleteAndAddPodOnly() throws InterruptedException{
		System.out.println("=================================");
		System.out.println("Beginning test: " +Thread.currentThread().getStackTrace()[1].getMethodName());
		GRMEdgeTestUtil.addRC(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_2, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_2, "2",GRMEdgeTestConstants.namespace);
		GRMEdgeTestUtil.addService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_2, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_2,GRMEdgeTestConstants.namespace);
		Thread.sleep(1000);
		GRMEdgeTestUtil.scaleRc(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_2, "1",GRMEdgeTestConstants.namespace);
		Thread.sleep(100);
		GRMEdgeTestUtil.scaleRc(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_2, "0",GRMEdgeTestConstants.namespace);
		Thread.sleep(100);
		GRMEdgeTestUtil.scaleRc(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_2, "1",GRMEdgeTestConstants.namespace);
		Thread.sleep(100);
		GRMEdgeTestUtil.scaleRc(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_2, "2",GRMEdgeTestConstants.namespace);
		Thread.sleep(100);
		GRMEdgeTestUtil.scaleRc(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_2, "0",GRMEdgeTestConstants.namespace);
		Thread.sleep(100);
		GRMEdgeTestUtil.scaleRc(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_2, "1",GRMEdgeTestConstants.namespace);
		
		//addding in a sleep to avoid reading previous ep data
		Thread.sleep(7000);
		
		GRMEdgeTestUtil.getEndPointsfromKubernetesWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_2,"LAB",GRMEdgeTestConstants.SLEEP*3,1);
		GRMEdgeTestUtil.getEndPointsfromKubernetesWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_2,"LAB",GRMEdgeTestConstants.SLEEP*3,1);
		if (System.getProperty("CHECK_GRM","true").equalsIgnoreCase("true")){
			assertEquals(GRMEdgeTestUtil.getEndPointsFromGRM(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_2,"LAB").getServiceEndPointList().size(),1);
		}
		System.out.println("=================================");
	}
	
	@Test
	public void addMatchingTwoPodAndServiceUpdateService() throws InterruptedException{
		System.out.println("=================================");
		System.out.println("Beginning test: " +Thread.currentThread().getStackTrace()[1].getMethodName());
		
		GRMEdgeTestUtil.addRC(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_2, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_2, "2",GRMEdgeTestConstants.namespace);
		GRMEdgeTestUtil.addService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_2, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_2,GRMEdgeTestConstants.namespace);

		Thread.sleep(1000);
		GRMEdgeTestUtil.updateService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_2, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_2,GRMEdgeTestConstants.namespace);
		Thread.sleep(40000);
		GRMEdgeTestUtil.getEndPointsfromKubernetesWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_2,"LAB",GRMEdgeTestConstants.SLEEP*3,2);
		List<ServiceEndPoint> seps = GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunning(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_2, GRMEdgeTestConstants.namespace,"LAB").getServiceEndPointList();
		for(ServiceEndPoint sep: seps){
			//check and make sure the protocol is set properly
			assertEquals("Protocol should have updated to https. Instead it is: " + sep.getProtocol(),"https",sep.getProtocol());
		}
		assertEquals(seps.size(),2);
		if (System.getProperty("CHECK_GRM","true").equalsIgnoreCase("true")){
			assertEquals(GRMEdgeTestUtil.getEndPointsFromGRM(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_2,"LAB").getServiceEndPointList().size(),2);
		}
		
		
		System.out.println("=================================");
	}
	
	@Test
	public void addMatchingTwoPodAndServiceUpdateVersion() throws InterruptedException{
		System.out.println("=================================");
		System.out.println("Beginning test: " +Thread.currentThread().getStackTrace()[1].getMethodName());
		
		GRMEdgeTestUtil.addService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_2, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_2,GRMEdgeTestConstants.namespace);

		Thread.sleep(10000);
		GRMEdgeTestUtil.addRCVersion(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_2, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_2,"2",GRMEdgeTestConstants.namespace,"1.0.1");
		GRMEdgeTestUtil.updateServiceVersion(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_2, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_2,GRMEdgeTestConstants.namespace,"1.0.1");
	
		GRMEdgeTestUtil.getEndPointsfromKubernetesWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_2,"LAB",GRMEdgeTestConstants.SLEEP*3,2);
		
		List<ServiceEndPoint> seps = (List<ServiceEndPoint>) GRMEdgeTestUtil.getEndPointsFromKubernetes(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_2, "LAB").getServiceEndPointList();
		assertEquals(2,seps.size());
		for(ServiceEndPoint sep: seps){
			assertEquals(1,sep.getVersion().getMajor());
			assertEquals(0,sep.getVersion().getMinor());
			assertEquals("1",sep.getVersion().getPatch());
		}		
		System.out.println("=================================");
	}
	
	@Test
	public void addMatchingPodAndDuplicateServiceNamesMultipleNamespaces() throws InterruptedException{
		System.out.println("=================================");
		System.out.println("Beginning test: " +Thread.currentThread().getStackTrace()[1].getMethodName());
		
		GRMEdgeTestUtil.addService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_3, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_3,GRMEdgeTestConstants.namespace);
		GRMEdgeTestUtil.addService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_3, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_3,GRMEdgeTestConstants.testnamespace);

		GRMEdgeTestUtil.addRC(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_3, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_3,"1",GRMEdgeTestConstants.namespace);
		GRMEdgeTestUtil.addRC(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_3, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_3,"1",GRMEdgeTestConstants.testnamespace);
		
		GRMEdgeTestUtil.getEndPointsfromKubernetesWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_3,"LAB",GRMEdgeTestConstants.SLEEP*3,1);
		GRMEdgeTestUtil.getEndPointsfromKubernetesWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_3,"DEV",GRMEdgeTestConstants.SLEEP*3,1);
				
		GRMEdgeTestUtil.deleteService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_3,GRMEdgeTestConstants.namespace);
		Thread.sleep(5000);
		GRMEdgeTestUtil.getEndPointsfromKubernetesWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_3,"LAB",GRMEdgeTestConstants.SLEEP*3,0);
		GRMEdgeTestUtil.getEndPointsfromKubernetesWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_3,"DEV",GRMEdgeTestConstants.SLEEP*3,1);
		
		GRMEdgeTestUtil.addService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_3, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_3,GRMEdgeTestConstants.namespace);
		
		GRMEdgeTestUtil.getEndPointsfromKubernetesWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_3,"LAB",GRMEdgeTestConstants.SLEEP*3,1);
		
		//restart GRM
		GRMEdgeTestUtil.scaleDeployment(GRMEdgeTestConstants.GRM_EDGE_SERVICE_NAME, "0",GRMEdgeTestConstants.namespace);
		GRMEdgeTestUtil.scaleDeployment(GRMEdgeTestConstants.GRM_EDGE_SERVICE_NAME, "1",GRMEdgeTestConstants.namespace);
		//allow grm to start up
		Thread.sleep(GRMEdgeTestConstants.SLEEP*2);
		
		GRMEdgeTestUtil.getEndPointsfromKubernetesWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_3,"LAB",GRMEdgeTestConstants.SLEEP*3,1);
		GRMEdgeTestUtil.getEndPointsfromKubernetesWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_3,"DEV",GRMEdgeTestConstants.SLEEP*3,1);
		
		GRMEdgeTestUtil.deleteRc(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_3, GRMEdgeTestConstants.testnamespace);
		
		Thread.sleep(5000);
		
		GRMEdgeTestUtil.getEndPointsfromKubernetesWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_3,"DEV",GRMEdgeTestConstants.SLEEP*3,0);
		GRMEdgeTestUtil.getEndPointsfromKubernetesWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_3,"LAB",GRMEdgeTestConstants.SLEEP*3,1);
		
		System.out.println("=================================");
	}
	
	/*
	 * TEST NAMESPACE
	 */
	

	/*
	 * No SEP should be created
	 */
	@Test
	public void addPodOnlyTestNamespace() throws InterruptedException{
		System.out.println("=================================");
		System.out.println("Beginning test: " +Thread.currentThread().getStackTrace()[1].getMethodName());
		//this test should pretty much always pass... do we need a test?
		GRMEdgeTestUtil.addRC(GRMEdgeTestConstants.KUBE_TEST_POD_ONLY_NAME, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_NO_MATCH_1, "1", GRMEdgeTestConstants.testnamespace);
		
		//Need to wait for the watcher to get the changes. This will only happen once the pod actually comes up so we should probably sleep for 1 minute or more
		Thread.sleep(GRMEdgeTestConstants.SLEEP);
		
		assertEquals(GRMEdgeTestUtil.getEndPointsFromKubernetes(GRMEdgeTestConstants.KUBE_TEST_POD_ONLY_ENDPOINT_NAME, "DEV").getServiceEndPointList().size(),0);
		assertEquals(GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunning(GRMEdgeTestConstants.KUBE_TEST_POD_ONLY_ENDPOINT_NAME, GRMEdgeTestConstants.namespace,"DEV").getServiceEndPointList().size(),0);
		if (System.getProperty("CHECK_GRM","true").equalsIgnoreCase("true")){
			assertEquals(GRMEdgeTestUtil.getEndPointsFromGRM(GRMEdgeTestConstants.KUBE_TEST_POD_ONLY_ENDPOINT_NAME,"DEV").getServiceEndPointList().size(),0);
		}
		System.out.println("=================================");
	}
	
	/*
	 * No SEP should be created
	 */
	@Test
	public void addServiceOnlyTestNamespace() throws InterruptedException{

		System.out.println("=================================");
		System.out.println("Beginning test: " +Thread.currentThread().getStackTrace()[1].getMethodName());

		GRMEdgeTestUtil.addService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_ONLY_NAME, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_NO_MATCH_2,GRMEdgeTestConstants.testnamespace);
		
		//Need to wait for the watcher to get the changes. This will only happen once the pod actually comes up so we should probably sleep for 1 minute or more
		Thread.sleep(GRMEdgeTestConstants.SLEEP);
		
		assertEquals(GRMEdgeTestUtil.getEndPointsFromKubernetes(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_ONLY_ENDPOINT_NAME,"DEV").getServiceEndPointList().size(),0);
		assertEquals(GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunning(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_ONLY_ENDPOINT_NAME, GRMEdgeTestConstants.namespace,"DEV").getServiceEndPointList().size(),0);
		if (System.getProperty("CHECK_GRM","true").equalsIgnoreCase("true")){
			assertEquals(GRMEdgeTestUtil.getEndPointsFromGRM(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_ONLY_ENDPOINT_NAME, "DEV").getServiceEndPointList().size(),0);
		}
		System.out.println("=================================");
	}
	
	/*
	 * No SEP should be created
	 */
	@Test
	public void addNonMatchingPodAndServicTestNamespacee() throws InterruptedException{

		System.out.println("=================================");
		System.out.println("Beginning test: " +Thread.currentThread().getStackTrace()[1].getMethodName());
		
		GRMEdgeTestUtil.addRC(GRMEdgeTestConstants.KUBE_TEST_POD_NO_MATCH_NAME, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_NO_MATCH_3, "1",GRMEdgeTestConstants.testnamespace);
		GRMEdgeTestUtil.addService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_NO_MATCH_NAME, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_NO_MATCH_4,GRMEdgeTestConstants.testnamespace);
		
		//Need to wait for the watcher to get the changes. This will only happen once the pod actually comes up so we should probably sleep for 1 minute or more
		Thread.sleep(GRMEdgeTestConstants.SLEEP*3);
		//no service name to check for the pod
		assertEquals(GRMEdgeTestUtil.getEndPointsFromKubernetes(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_NO_MATCH_ENDPOINT_NAME,"DEV").getServiceEndPointList().size(),0);
		assertEquals(GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunning(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_NO_MATCH_ENDPOINT_NAME, GRMEdgeTestConstants.namespace,"DEV").getServiceEndPointList().size(),0);
		if (System.getProperty("CHECK_GRM","true").equalsIgnoreCase("true")){
			assertEquals(GRMEdgeTestUtil.getEndPointsFromGRM(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_NO_MATCH_ENDPOINT_NAME,"DEV").getServiceEndPointList().size(),0);
		}
		System.out.println("=================================");
	}
	
	/*
	 * Makes sure a SEP is created
	 */
	@Test 
	public void addMatchingPodAndServiceTestNamespace() throws InterruptedException{

		System.out.println("=================================");
		System.out.println("Beginning test: " +Thread.currentThread().getStackTrace()[1].getMethodName());

		
		GRMEdgeTestUtil.addRC(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_1, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_1, "1",GRMEdgeTestConstants.testnamespace);
		GRMEdgeTestUtil.addService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_1, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_1,GRMEdgeTestConstants.testnamespace);
		
		GRMEdgeTestUtil.getEndPointsfromKubernetesWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_1,"DEV",GRMEdgeTestConstants.SLEEP*3,1);
		List<ServiceEndPoint> seps = GRMEdgeTestUtil.getEndPointsFromKubernetes(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_1,"DEV").getServiceEndPointList();
		//check endpoint assertEquals(GRMEdgeTestUtil, "serviceName");
		assertEquals(seps.get(0).getName().equalsIgnoreCase(GRMEdgeUtil.convertSEPNamespaceToDots(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_1)),true);
		GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunningWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_1,"DEV",GRMEdgeTestConstants.SLEEP,1);
		if (System.getProperty("CHECK_GRM","true").equalsIgnoreCase("true")){
			assertEquals(GRMEdgeTestUtil.getEndPointsFromGRM(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_1,"DEV").getServiceEndPointList().size(),1);
		}
		System.out.println("=================================");
	}
	
	/*
	 * Two service end points being created
	 */
	@Test 
	public void addMatchingTwoPodsAndServiceTestNamespace() throws InterruptedException{

		System.out.println("=================================");
		System.out.println("Beginning test: " +Thread.currentThread().getStackTrace()[1].getMethodName());
		
		GRMEdgeTestUtil.addRC(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_2, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_2, "2",GRMEdgeTestConstants.testnamespace);
		GRMEdgeTestUtil.addService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_2, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_2,GRMEdgeTestConstants.testnamespace);

		GRMEdgeTestUtil.getEndPointsfromKubernetesWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_2,"DEV",GRMEdgeTestConstants.SLEEP*3,2);
		GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunningWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_2,"DEV",GRMEdgeTestConstants.SLEEP,2);
		List<ServiceEndPoint> seps = GRMEdgeTestUtil.getEndPointsFromKubernetes(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_2,"DEV").getServiceEndPointList();
		if (System.getProperty("CHECK_GRM","true").equalsIgnoreCase("true")){
			assertEquals(GRMEdgeTestUtil.getEndPointsFromGRM(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_2,"DEV").getServiceEndPointList().size(),2);
		}
		//CHeck both endpoints
		assertEquals(seps.get(0).getName().equalsIgnoreCase(GRMEdgeUtil.convertSEPNamespaceToDots(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_2)),true);
		assertEquals(seps.get(1).getName().equalsIgnoreCase(GRMEdgeUtil.convertSEPNamespaceToDots(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_2)),true);

		System.out.println("=================================");
	}
	
	/*
	 * Creates two SEPS and deletes service. Makes sure they are deleted when service is deleted 
	 */
	@Test 
	public void addMatchingTwoPodsAndServiceDeleteServiceTestNamespace() throws InterruptedException{

		System.out.println("=================================");
		System.out.println("Beginning test: " +Thread.currentThread().getStackTrace()[1].getMethodName());

		GRMEdgeTestUtil.addRC(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_3, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_3, "2",GRMEdgeTestConstants.testnamespace);
		GRMEdgeTestUtil.addService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_3, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_3,GRMEdgeTestConstants.testnamespace);

		GRMEdgeTestUtil.getEndPointsfromKubernetesWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_3,"DEV",GRMEdgeTestConstants.SLEEP*3,2);
		GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunningWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_3,"DEV",GRMEdgeTestConstants.SLEEP,2);
		if (System.getProperty("CHECK_GRM","true").equalsIgnoreCase("true")){
			assertEquals(GRMEdgeTestUtil.getEndPointsFromGRM(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_3,"DEV").getServiceEndPointList().size(),2);
		}
		
		Thread.sleep(GRMEdgeTestConstants.SLEEP);
		GRMEdgeTestUtil.deleteService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_3,GRMEdgeTestConstants.testnamespace);
		GRMEdgeTestUtil.getEndPointsfromKubernetesWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_3,"DEV",GRMEdgeTestConstants.SLEEP*3,0);
		Thread.sleep(5000); //sleep some for write behind
		GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunningWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_3,"DEV",GRMEdgeTestConstants.SLEEP,0);
		if (System.getProperty("CHECK_GRM","true").equalsIgnoreCase("true")){
			assertEquals(GRMEdgeTestUtil.getEndPointsFromGRM(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_3,"DEV").getServiceEndPointList().size(),0);
		}
		System.out.println("=================================");
	}
	
	@Test
	public void addMatchingPodAndServiceScaleUpTestNamespace() throws InterruptedException{

		System.out.println("=================================");
		System.out.println("Beginning test: " +Thread.currentThread().getStackTrace()[1].getMethodName());
		
		GRMEdgeTestUtil.addRC(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_1, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_1, "1",GRMEdgeTestConstants.testnamespace);
		GRMEdgeTestUtil.addService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_1, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_1,GRMEdgeTestConstants.testnamespace);
		

		GRMEdgeTestUtil.getEndPointsfromKubernetesWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_1,"DEV",GRMEdgeTestConstants.SLEEP*3,1);
		GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunningWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_1,"DEV",GRMEdgeTestConstants.SLEEP,1);
		if (System.getProperty("CHECK_GRM","true").equalsIgnoreCase("true")){
			assertEquals(GRMEdgeTestUtil.getEndPointsFromGRM(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_1,"DEV").getServiceEndPointList().size(),1);
		}
		GRMEdgeTestUtil.scaleRc(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_1, "2",GRMEdgeTestConstants.testnamespace);
	
		GRMEdgeTestUtil.getEndPointsfromKubernetesWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_1,"DEV",GRMEdgeTestConstants.SLEEP*3,2);
		GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunningWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_1,"DEV",GRMEdgeTestConstants.SLEEP,2);
		if (System.getProperty("CHECK_GRM","true").equalsIgnoreCase("true")){
			assertEquals(GRMEdgeTestUtil.getEndPointsFromGRM(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_1,"DEV").getServiceEndPointList().size(),2);
		}
		System.out.println("=================================");
	}
	
	@Test
	public void addMatchingTwoPodAndServiceScaleDownTestNamespace() throws InterruptedException{
	
		System.out.println("=================================");
		System.out.println("Beginning test: " +Thread.currentThread().getStackTrace()[1].getMethodName());
				
		
		GRMEdgeTestUtil.addRC(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_2, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_2, "2",GRMEdgeTestConstants.testnamespace);
		GRMEdgeTestUtil.addService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_2, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_2,GRMEdgeTestConstants.testnamespace);
		
		GRMEdgeTestUtil.getEndPointsfromKubernetesWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_2,"DEV",GRMEdgeTestConstants.SLEEP*3,2);
		GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunningWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_2,"DEV",GRMEdgeTestConstants.SLEEP,2);
		if (System.getProperty("CHECK_GRM","true").equalsIgnoreCase("true")){
			assertEquals(GRMEdgeTestUtil.getEndPointsFromGRM(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_2,"DEV").getServiceEndPointList().size(),2);
		}
		GRMEdgeTestUtil.scaleRc(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_2, "1",GRMEdgeTestConstants.testnamespace);
		
		GRMEdgeTestUtil.getEndPointsfromKubernetesWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_2,"DEV",GRMEdgeTestConstants.SLEEP*3,1);
		GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunningWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_2,"DEV",GRMEdgeTestConstants.SLEEP,1);
		if (System.getProperty("CHECK_GRM","true").equalsIgnoreCase("true")){
			assertEquals(GRMEdgeTestUtil.getEndPointsFromGRM(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_2,"DEV").getServiceEndPointList().size(),1);
		}
		System.out.println("=================================");
	}
	
	/*
	 * Invalid Service details
	 */
	@Test
	public void unhappyAddInvalidServiceTestNamespace() throws InterruptedException{
	
		System.out.println("=================================");
		System.out.println("Beginning test: " +Thread.currentThread().getStackTrace()[1].getMethodName());
					
		
		GRMEdgeTestUtil.addInvalidService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_2, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_2,GRMEdgeTestConstants.testnamespace);
		GRMEdgeTestUtil.addRC(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_2, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_2, "1",GRMEdgeTestConstants.testnamespace);
		
		Thread.sleep(GRMEdgeTestConstants.SLEEP);
		
		List<ServiceEndPoint> seps = GRMEdgeTestUtil.getEndPointsFromKubernetes(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_2,"DEV").getServiceEndPointList();
		assertEquals(seps.size(),0);
		GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunningWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_2,"DEV",GRMEdgeTestConstants.SLEEP,0);
		if (System.getProperty("CHECK_GRM","true").equalsIgnoreCase("true")){
			assertEquals(GRMEdgeTestUtil.getEndPointsFromGRM(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_2,"DEV").getServiceEndPointList().size(),0);
		}
	}

	/*
	 * Invalid Pod details
	 */
	@Test
	public void unhappyAddInvalidPodTestNamespace() throws InterruptedException{
	
		System.out.println("=================================");
		System.out.println("Beginning test: " +Thread.currentThread().getStackTrace()[1].getMethodName());
	
		
		GRMEdgeTestUtil.addInvalidRC(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_2, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_2, "1",GRMEdgeTestConstants.testnamespace);
		GRMEdgeTestUtil.addService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_2,GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_2,GRMEdgeTestConstants.testnamespace);
		
		Thread.sleep(GRMEdgeTestConstants.SLEEP*2);
		
		List<ServiceEndPoint> seps = GRMEdgeTestUtil.getEndPointsFromKubernetes(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_2,"DEV").getServiceEndPointList();
		assertEquals(seps.size(),0);
		GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunningWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_2,"DEV",GRMEdgeTestConstants.SLEEP,0);
		if (System.getProperty("CHECK_GRM","true").equalsIgnoreCase("true")){
			assertEquals(GRMEdgeTestUtil.getEndPointsFromGRM(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_2,"DEV").getServiceEndPointList().size(),0);
		}
		
	}
	
	@Test
	public void addMatchingPodAndServiceRapidDeleteandAddFirstTestNamespace() throws InterruptedException{
	
		GRMEdgeTestUtil.addRC(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_2, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_2, "2",GRMEdgeTestConstants.testnamespace);
		GRMEdgeTestUtil.addService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_2, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_2,GRMEdgeTestConstants.testnamespace);
		Thread.sleep(100);
		GRMEdgeTestUtil.scaleRc(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_2, "1",GRMEdgeTestConstants.testnamespace);
		GRMEdgeTestUtil.deleteService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_2,GRMEdgeTestConstants.testnamespace);
		Thread.sleep(100);
		GRMEdgeTestUtil.scaleRc(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_2, "0",GRMEdgeTestConstants.testnamespace);
		Thread.sleep(100);
		GRMEdgeTestUtil.scaleRc(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_2, "1",GRMEdgeTestConstants.testnamespace);
		GRMEdgeTestUtil.addService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_2,GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_2,GRMEdgeTestConstants.testnamespace);
		
		GRMEdgeTestUtil.getEndPointsfromKubernetesWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_2,"DEV",GRMEdgeTestConstants.SLEEP*3,1);
		GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunningWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_2,"DEV",GRMEdgeTestConstants.SLEEP,1);
		if (System.getProperty("CHECK_GRM","true").equalsIgnoreCase("true")){
			assertEquals(GRMEdgeTestUtil.getEndPointsFromGRM(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_2,"DEV").getServiceEndPointList().size(),1);
		}
		
		System.out.println("=================================");
	}
	
	@Test
	public void addMatchingPodAndServiceRapidDeleteAndAddServiceOnlyTestNamespace() throws InterruptedException{
		System.out.println("=================================");
		System.out.println("Beginning test: " +Thread.currentThread().getStackTrace()[1].getMethodName());
		GRMEdgeTestUtil.addRC(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_2, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_2, "1",GRMEdgeTestConstants.testnamespace);
		GRMEdgeTestUtil.addService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_2, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_2,GRMEdgeTestConstants.testnamespace);
		Thread.sleep(100);
		GRMEdgeTestUtil.deleteService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_2,GRMEdgeTestConstants.testnamespace);
		Thread.sleep(100);
		GRMEdgeTestUtil.addService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_2,GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_2,GRMEdgeTestConstants.testnamespace);
		Thread.sleep(50);
		GRMEdgeTestUtil.deleteService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_2,GRMEdgeTestConstants.testnamespace);
		Thread.sleep(50);
		GRMEdgeTestUtil.addService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_2,GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_2,GRMEdgeTestConstants.testnamespace);
		
		GRMEdgeTestUtil.getEndPointsfromKubernetesWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_2,"DEV",GRMEdgeTestConstants.SLEEP*3,1);
		GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunningWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_2,"DEV",GRMEdgeTestConstants.SLEEP,1);
		if (System.getProperty("CHECK_GRM","true").equalsIgnoreCase("true")){
			assertEquals(GRMEdgeTestUtil.getEndPointsFromGRM(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_2,"DEV").getServiceEndPointList().size(),1);
		}
		System.out.println("=================================");
	}
	
	@Test
	public void addMatchingPodAndServiceRapidDeleteAndAddPodOnlyTestNamespace() throws InterruptedException{
		System.out.println("=================================");
		System.out.println("Beginning test: " +Thread.currentThread().getStackTrace()[1].getMethodName());
		GRMEdgeTestUtil.addRC(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_2, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_2, "2",GRMEdgeTestConstants.testnamespace);
		GRMEdgeTestUtil.addService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_2, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_2,GRMEdgeTestConstants.testnamespace);
		Thread.sleep(1000);
		GRMEdgeTestUtil.scaleRc(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_2, "1",GRMEdgeTestConstants.testnamespace);
		Thread.sleep(100);
		GRMEdgeTestUtil.scaleRc(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_2, "0",GRMEdgeTestConstants.testnamespace);
		Thread.sleep(100);
		GRMEdgeTestUtil.scaleRc(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_2, "1",GRMEdgeTestConstants.testnamespace);
		Thread.sleep(100);
		GRMEdgeTestUtil.scaleRc(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_2, "2",GRMEdgeTestConstants.testnamespace);
		Thread.sleep(100);
		GRMEdgeTestUtil.scaleRc(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_2, "0",GRMEdgeTestConstants.testnamespace);
		Thread.sleep(100);
		GRMEdgeTestUtil.scaleRc(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_2, "1",GRMEdgeTestConstants.testnamespace);
		
		//addding in a sleep to avoid reading previous ep data
		Thread.sleep(7000);
		
		GRMEdgeTestUtil.getEndPointsfromKubernetesWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_2,"DEV",GRMEdgeTestConstants.SLEEP*3,1);
		GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunningWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_2,"DEV",GRMEdgeTestConstants.SLEEP,1);
		if (System.getProperty("CHECK_GRM","true").equalsIgnoreCase("true")){
			assertEquals(GRMEdgeTestUtil.getEndPointsFromGRM(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_2,"DEV").getServiceEndPointList().size(),1);
		}
		System.out.println("=================================");
	}
	
	@Test
	public void addMatchingTwoPodAndServiceUpdateServiceTestNamespace() throws InterruptedException{
		System.out.println("=================================");
		System.out.println("Beginning test: " +Thread.currentThread().getStackTrace()[1].getMethodName());
		
		GRMEdgeTestUtil.addRC(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_2, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_2, "2",GRMEdgeTestConstants.testnamespace);
		GRMEdgeTestUtil.addService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_2, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_2,GRMEdgeTestConstants.testnamespace);

		Thread.sleep(2000);
		
		GRMEdgeTestUtil.updateService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_2, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_2,GRMEdgeTestConstants.testnamespace);
		GRMEdgeTestUtil.getEndPointsfromKubernetesWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_2,"DEV",GRMEdgeTestConstants.SLEEP*3,2);
		Thread.sleep(GRMEdgeTestConstants.SLEEP*2);
		List<ServiceEndPoint> seps = GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunning(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_2, GRMEdgeTestConstants.namespace,"DEV").getServiceEndPointList();
		for(ServiceEndPoint sep: seps){
			//check and make sure the protocol is set properly
			assertEquals("Protocol should have updated to https. Instead it is: " + sep.getProtocol(),"https",sep.getProtocol());
		}
		assertEquals(seps.size(),2);
		if (System.getProperty("CHECK_GRM","true").equalsIgnoreCase("true")){
			assertEquals(GRMEdgeTestUtil.getEndPointsFromGRM(GRMEdgeTestConstants.KUBE_TEST_TESTNS_SERVICE_MATCH_ENDPOINT_NAME_2,"DEV").getServiceEndPointList().size(),2);
		}
		
		
		System.out.println("=================================");
	}
	
	/*
	 * Should return default RouteInfo XML
	 */
	@Test
	public void getRouteInfoReturnDefaultXML() throws InterruptedException{

		System.out.println("=================================");
		System.out.println("Beginning test: " +Thread.currentThread().getStackTrace()[1].getMethodName());
		
		assertTrue(GRMEdgeTestUtil.getRouteInfofromKubernetes(
				(System.currentTimeMillis() + GRMEdgeTestConstants.KUBE_TEST_SERVICE_ONLY_ENDPOINT_NAME + System.currentTimeMillis()),
				GRMEdgeTestConstants.namespace,
				"2",
				"100",
				"1000",
				"LAB").getRouteInfoXml()
				.equalsIgnoreCase("<routeInfo xmlns:ns2=\"http://scld.att.com/grm/RouteInfoMetaData/v1\" xmlns=\"http://aft.att.com/dme2/types\"><routeGroups><routeGroup name=\"DEFAULT\"><partner>*</partner><route name=\"DEFAULT\"><routeOffer name=\"DEFAULT\" sequence=\"1\" active=\"true\"/></route></routeGroup></routeGroups></routeInfo>"
		));
				
				
		
		System.out.println("=================================");
	}
    
    /**
     * GRMEdge uses Hazelcast as a distributed cache. We need a test case to
     * scale up GRM Edge so that more than one Edge instance shares a cache.
     * Make sure the data is propagated by checking log files, scale back down
     * and make sure the cache still persists in the GRM instance left running.
     * 
     * @throws InterruptedException
     * @author Hector Mendoza
     */
    @Test
    public void testHazelcastPersistsAfterScaleUpAndDown() throws InterruptedException
    {
        System.out.println("=================================");
        System.out.println("Beginning test: " + Thread.currentThread().getStackTrace()[1].getMethodName());
        
        // start fresh, scale GRMEdge to 0 to clear all logs
        GRMEdgeTestUtil.scaleDeployment(GRMEdgeTestConstants.GRM_EDGE_SERVICE_NAME, "0", GRMEdgeTestConstants.namespace);
//        Thread.sleep(GRMEdgeTestConstants.SLEEP*3);
        
        // now scale to 3 pods
        // Note: after the pods are created, it can take hazelcast up to 6 minutes to log the correct amount of members
        GRMEdgeTestUtil.scaleDeployment(GRMEdgeTestConstants.GRM_EDGE_SERVICE_NAME, "3", GRMEdgeTestConstants.namespace);
        Thread.sleep(GRMEdgeTestConstants.SLEEP*12);
        
        // get all the names of all the pods we just created
        List<String> podNames = GRMEdgeTestUtil.getGRMEdgePodNames();        
        String podName = null;
        if (podNames != null && podNames.size() > 0)
        {
            podName = podNames.get(0);
        }
        
        // get the log content for this pod
        String logContent = GRMEdgeTestUtil.getLogContentForGivenPod(podName);
        
        // get current number of hazelcast members from the log
        int numberOfHazelcastMembers = GRMEdgeTestUtil.getNumberOfHazelcastMembersFromLog(logContent);
        
        // make sure is the right amount
        assertEquals(3, numberOfHazelcastMembers);
        
        // now, scale down to 1 pod
        // Note: after the pods are created, it can take hazelcast up to 6 minutes to log the correct amount of members
        GRMEdgeTestUtil.scaleDeployment(GRMEdgeTestConstants.GRM_EDGE_SERVICE_NAME, "1", GRMEdgeTestConstants.namespace);
        Thread.sleep(GRMEdgeTestConstants.SLEEP*12);
        
        // now get the log content for the remaining pod
        podNames = GRMEdgeTestUtil.getGRMEdgePodNames();
        if (podNames != null && podNames.size() > 0)
        {
            podName = podNames.get(0);
        }
        
        // get the log content for this pod
        logContent = GRMEdgeTestUtil.getLogContentForGivenPod(podName);
        
        // get number of current hazelcast members from the log
        numberOfHazelcastMembers = GRMEdgeTestUtil.getNumberOfHazelcastMembersFromLog(logContent);
        
        // make sure is the right amount        
        assertEquals(1, numberOfHazelcastMembers);
        
        System.out.println("=================================");
    }
    
  //Invalid login details
  	@Test
  	public void refreshServiceCacheWithInvaldLoginDetailsAndWriteBehindOff() throws InterruptedException, JSONException{
  		System.out.println("=================================");
  		System.out.println("Beginning test: " +Thread.currentThread().getStackTrace()[1].getMethodName());
  		//SET GRM TO TRUE, SET CASSANDRA TO FALSE
  		GRMEdgeTestUtil.setConfigValue(GRMEdgeTestUtil.getCurrentConfig(),"CPFRUN_GRMEDGE_GRM_WRITE_BEHIND_ENABLE=false");
  		Thread.sleep(100);
  		GRMEdgeTestUtil.setConfigValue(GRMEdgeTestUtil.getCurrentConfig(),"GRM_EDGE_FINDRUNNING_RETRIEVE_FROM_GRM_REST=false");
  		Thread.sleep(100);
  		GRMEdgeTestUtil.setConfigValue(GRMEdgeTestUtil.getCurrentConfig(),"CPFRUN_GRMEDGE_DB_WRITE_BEHIND_ENABLE=false");
  		Thread.sleep(100);		
  		
  		//String response =GRMEdgeTestUtil.refreshServiceCache("admin", "ocnpmgmt1");
  		
  		//restart GRM Edge
  		GRMEdgeTestUtil.scaleDeployment(GRMEdgeTestConstants.GRM_EDGE_SERVICE_NAME, "0",GRMEdgeTestConstants.namespace);
  		//allow old GRMEdge to use proper shutdownhook
  		GRMEdgeTestUtil.scaleDeployment(GRMEdgeTestConstants.GRM_EDGE_SERVICE_NAME, "1",GRMEdgeTestConstants.namespace);
  		//allow grm to start up
  		Thread.sleep(GRMEdgeTestConstants.SLEEP*2);
            
  		String response =GRMEdgeTestUtil.refreshServiceCache("admin", "ocnpmgmt222");
  		
  		assertEquals(GRMEdgeTestConstants.REFRESH_CACHE_INVALID_LOGIN_ERROR_MESSAGE, response);

  		System.out.println("=================================");
  	}
  	
  	
  	//Write Behind error scenario 
  	@Test
  	public void refreshServiceCacheWithWriteBehindError() throws InterruptedException, JSONException{
  		System.out.println("=================================");
  		System.out.println("Beginning test: " +Thread.currentThread().getStackTrace()[1].getMethodName());
  		//SET GRM TO TRUE, SET CASSANDRA TO FALSE
  		GRMEdgeTestUtil.setConfigValue(GRMEdgeTestUtil.getCurrentConfig(),"CPFRUN_GRMEDGE_GRM_WRITE_BEHIND_ENABLE=true");
  		Thread.sleep(100);
  		GRMEdgeTestUtil.setConfigValue(GRMEdgeTestUtil.getCurrentConfig(),"GRM_EDGE_FINDRUNNING_RETRIEVE_FROM_GRM_REST=true");
  		Thread.sleep(100);
  		GRMEdgeTestUtil.setConfigValue(GRMEdgeTestUtil.getCurrentConfig(),"CPFRUN_GRMEDGE_DB_WRITE_BEHIND_ENABLE=true");
  		Thread.sleep(100);		
  		
  		//String response =GRMEdgeTestUtil.refreshServiceCache("admin", "ocnpmgmt1");
  		
  		//restart GRM Edge
  		GRMEdgeTestUtil.scaleDeployment(GRMEdgeTestConstants.GRM_EDGE_SERVICE_NAME, "0",GRMEdgeTestConstants.namespace);
  		//allow old GRMEdge to use proper shutdownhook
  		Thread.sleep(GRMEdgeTestConstants.SLEEP);
  		GRMEdgeTestUtil.scaleDeployment(GRMEdgeTestConstants.GRM_EDGE_SERVICE_NAME, "1",GRMEdgeTestConstants.namespace);
  		//allow grm to start up
  		Thread.sleep(GRMEdgeTestConstants.SLEEP*3);

  		String response =GRMEdgeTestUtil.refreshServiceCache("admin", "ocnpmgmt222");
  		
  		assertEquals(GRMEdgeTestConstants.WRITE_BEHIND_ENABLED_ERROR_MESSAGE, response);
  		
  		System.out.println("Service Respionse ::::"+response);
  			
  		
  		System.out.println("=================================");
  	}
  	
   	
  	//Valid login details  with Write behind disabled
  	@Test
  	public void refreshServiceCacheWithValidLoginAndWriteBehindOff() throws InterruptedException, JSONException {
  		System.out.println("=================================");
  		System.out.println("Beginning test: " +Thread.currentThread().getStackTrace()[1].getMethodName());
  		//SET GRM TO TRUE, SET CASSANDRA TO FALSE
  		GRMEdgeTestUtil.setConfigValue(GRMEdgeTestUtil.getCurrentConfig(),"CPFRUN_GRMEDGE_GRM_WRITE_BEHIND_ENABLE=false");
  		Thread.sleep(100);
  		GRMEdgeTestUtil.setConfigValue(GRMEdgeTestUtil.getCurrentConfig(),"GRM_EDGE_FINDRUNNING_RETRIEVE_FROM_GRM_REST=false");
  		Thread.sleep(100);
  		GRMEdgeTestUtil.setConfigValue(GRMEdgeTestUtil.getCurrentConfig(),"CPFRUN_GRMEDGE_DB_WRITE_BEHIND_ENABLE=false");
  		Thread.sleep(100);		
  		//restart GRM Edge
  		GRMEdgeTestUtil.scaleDeployment(GRMEdgeTestConstants.GRM_EDGE_SERVICE_NAME, "0",GRMEdgeTestConstants.namespace);
  		//allow old GRMEdge to use proper shutdownhook
  		GRMEdgeTestUtil.scaleDeployment(GRMEdgeTestConstants.GRM_EDGE_SERVICE_NAME, "1",GRMEdgeTestConstants.namespace);
  		//allow grm to start up
  		// Get the end-point before refresh  
  		FindServiceEndPointResponse sep=GRMEdgeTestUtil.getEndPointsFromKubernetes(GRMEdgeTestConstants.namespaceWithDotsNoEnv + "." + GRMEdgeTestConstants.GRM_EDGE_SERVICE_NAME,"LAB");
  		assertTrue(GRMEdgeTestUtil.getEndPointsFromKubernetes(GRMEdgeTestConstants.namespaceWithDotsNoEnv + "." + GRMEdgeTestConstants.GRM_EDGE_SERVICE_NAME,"LAB").getServiceEndPointList().size()==1);
  		
  		long  beforeRefreshSepRegistrationDate=sep.getServiceEndPointList().get(0).getRegistrationTime().toGregorianCalendar().getTimeInMillis();
  		
  		String response =GRMEdgeTestUtil.refreshServiceCache("admin", "ocnpmgmt1");
  		// Verify the response
  		 assertEquals(GRMEdgeTestConstants.CACHE_REFRESH_SUCCESS_MESSAGE, response);
  		 Thread.sleep(1000);
  		 // Get the end-point after refresh
  		 FindServiceEndPointResponse sep1=GRMEdgeTestUtil.getEndPointsFromKubernetes(GRMEdgeTestConstants.namespaceWithDotsNoEnv + "." + GRMEdgeTestConstants.GRM_EDGE_SERVICE_NAME,"LAB");
  		
  		 long  afterRefreshSepRegistrationDate=sep1.getServiceEndPointList().get(0).getRegistrationTime().toGregorianCalendar().getTimeInMillis();
  		 //verify the sep time-stamp no longer valid as it doesnt delete endpoints
//  		 assertTrue(afterRefreshSepRegistrationDate>beforeRefreshSepRegistrationDate);
  		
  		assertTrue(GRMEdgeTestUtil.getEndPointsFromKubernetes(GRMEdgeTestConstants.namespaceWithDotsNoEnv + "." + GRMEdgeTestConstants.GRM_EDGE_SERVICE_NAME,"LAB").getServiceEndPointList().size()==1);
  		
  		System.out.println("=================================");
  	}
  	
    /**
     * Tests that "simple" annotations are registered as properties in both GRM
     * Edge and GRM Rest.
     * 
     * @throws InterruptedException
     */
    @Test
    public void testGRMRestPropertyRegistration() throws InterruptedException
    {
        System.out.println("=================================");
        System.out.println("Beginning test: " +Thread.currentThread().getStackTrace()[1].getMethodName());
        
        //add RC and service with annotations, these annotations should get registered with GRMRest as properties
        GRMEdgeTestUtil.addRC(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_1, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_1, "1", GRMEdgeTestConstants.namespace);
        GRMEdgeTestUtil.addServiceWithAnnotations(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_1, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_1, GRMEdgeTestConstants.namespace);
        
        Thread.sleep(GRMEdgeTestConstants.SLEEP);
        
        List<NameValuePair> properties = new ArrayList<NameValuePair>();
        
        NameValuePair nameValuePair1 = new NameValuePair();
        nameValuePair1.setName("property1");
        nameValuePair1.setValue("value1");
        properties.add(nameValuePair1);
        
        NameValuePair nameValuePair2 = new NameValuePair();
        nameValuePair2.setName("property2");
        nameValuePair2.setValue("value2");
        properties.add(nameValuePair2);
                
        //check properties were registered in GRM Edge
        //get endpoints from GRM Edge that match our service name
        List<ServiceEndPoint> grmEdgeSepList = GRMEdgeTestUtil.getEndPointsFromKubernetes(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_1, "LAB").getServiceEndPointList();
        findPropertyMatchInEndpointList(grmEdgeSepList, properties);
        
        //check properties were registered in GRM Rest
        if (System.getProperty("CHECK_GRM", "true").equalsIgnoreCase("true"))
        {
            //get endpoints from GRM Rest that match our service name
            List<ServiceEndPoint> grmRestSepList = GRMEdgeTestUtil.getEndPointsFromGRM(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_1, "LAB").getServiceEndPointList();
            findPropertyMatchInEndpointList(grmRestSepList, properties);
        }
        
        System.out.println("=================================");
    }
    
    /**
     * Helper method that tests if the given properties have a match inside the
     * properties of the given end-point list.
     * 
     * @param endpointList List of end-points
     * @param properties List of properties to check end-point properties against
     */
    private void findPropertyMatchInEndpointList(List<ServiceEndPoint> endpointList, List<NameValuePair> properties)
    {
	    boolean matchFound = false;
	    
	    //for every property specified above, exhaust all properties
        //found in all service end points until we find a match
        //error if no match found
        for (int i = 0; i < properties.size(); i++)
        {
        	matchFound = false;
		    for (int j = 0; j < endpointList.size(); j++)
		    {
		        List<NameValuePair> sepProperties = endpointList.get(j).getProperties();
		        
		        for (int k = 0; k < sepProperties.size(); k++)
		        {
		            if (properties.get(i).getName().equals(sepProperties.get(k).getName()) &&
		                properties.get(i).getValue().equals(sepProperties.get(k).getValue()))
		            {
		                matchFound = true;
		                break;
		            }
		        }
		        
		        if (matchFound)
		        {
		            break;
		        }
		    }
		    
		    assertTrue("No property match found", matchFound);
        }
    }

}
