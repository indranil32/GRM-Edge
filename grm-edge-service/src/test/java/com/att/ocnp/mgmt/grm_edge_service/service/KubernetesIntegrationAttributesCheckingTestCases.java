/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.ocnp.mgmt.grm_edge_service.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Iterator;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.att.ocnp.mgmt.grm_edge_service.util.GRMEdgeUtil;
import com.att.scld.grm.types.v1.NameValuePair;
import com.att.scld.grm.types.v1.OperationalInfo;
import com.att.scld.grm.types.v1.ServiceEndPoint;
import com.att.scld.grm.types.v1.VersionDefinition;
import com.att.scld.grm.v1.FindServiceEndPointResponse;


public class KubernetesIntegrationAttributesCheckingTestCases {

	
	static {
		if(System.getProperty("TIME_DIFF") == null)
			System.setProperty("TIME_DIFF","0");
//		System.setProperty("CPFRUN_GRMEDGE_GRM_PORT","9427");
		HttpsURLConnection.setDefaultHostnameVerifier(
	    new javax.net.ssl.HostnameVerifier(){

	            @Override
	        public boolean verify(String hostname,
	                javax.net.ssl.SSLSession sslSession) {
	            	System.out.println(" ************  Host name verified ************ ");
	            	return true;
	        }
	    });
		
		if (System.getProperty("AFT_LATITUDE") != null && System.getProperty("AFT_LONGITUDE") != null) {
			GRMEdgeUtil.setLatAndLong(System.getProperty("AFT_LATITUDE"), System.getProperty("AFT_LONGITUDE"));
		} else {
			GRMEdgeUtil.setLatAndLong("1.0", "1.0");
		}
		System.setProperty("javax.net.ssl.trustStore", "src\\test\\resources\\cacerts");
		GRMEdgeTestUtil.SSLCertificateValidation.disable();

//		System.setProperty("KUBE_API_USER", "root");
//		System.setProperty("KUBE_API_PWD", "password");
//		System.setProperty("CHECK_GRM","true");		
//		System.setProperty("KUBE_API_GRM_NAMESPACE", "name-space-DEV");
	}
	
	/*
	 * Setup for all test cases
	 */
	@BeforeClass
	public static void setup() throws InterruptedException{
		System.out.println("====Beginning Setup===");
		System.setProperty("javax.net.ssl.trustStore", "src/test/resources/cacerts");
		GRMEdgeTestUtil.SSLCertificateValidation.disable();
		
		assertEquals("Not running test suite, testendpoints ("+GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_1_ATTR+") found in GRM (DEV) will mess up the test.",GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunning(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_1_ATTR,GRMEdgeTestConstants.namespace,"DEV").getServiceEndPointList().size(),0);
		
		GRMEdgeTestUtil.createNamespace(GRMEdgeTestConstants.testnamespace);
		
		GRMEdgeTestUtil.allowPatch();
		
		System.out.println("====Ending Setup===");
	}

	
	
	@AfterClass
	public static void shutdown() throws InterruptedException{
		System.out.println("====Shutdown started====");
		
		//delete test namespace com.att.ocnp.testcase.DEV
		
		GRMEdgeTestUtil.deleteNamespace(GRMEdgeTestConstants.testnamespace);
		
		Thread.sleep(GRMEdgeTestConstants.SLEEP);
		
		System.out.println("====Shutdown ended====");
	}
	
	@Before
	public void startUp() throws InterruptedException{
		//sleeping so that it kubernetes doesnt throw a alraedy modified error at us
		Thread.sleep(500);
	}
	
	@After
	public void tearDown() throws InterruptedException{
		
		Thread.sleep(10000 );
		
		System.out.println("===Tear down started===");
		
		GRMEdgeTestUtil.deleteRc(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_1_ATTR,GRMEdgeTestConstants.testnamespace);
		GRMEdgeTestUtil.deleteService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_1_ATTR,GRMEdgeTestConstants.testnamespace);

		GRMEdgeTestUtil.deleteRc(GRMEdgeTestConstants.KUBE_UPD_TEST_POD_MATCH_NAME_1_ATTR,GRMEdgeTestConstants.testnamespace);
		GRMEdgeTestUtil.deleteService(GRMEdgeTestConstants.KUBE_UPD_TEST_SERVICE_MATCH_NAME_1_ATTR,GRMEdgeTestConstants.testnamespace);

		GRMEdgeTestUtil.deleteRc(GRMEdgeTestConstants.KUBE_UPD_TEST_POD_MATCH_NAME_1_ATTR+"r",GRMEdgeTestConstants.testnamespace);
		
		System.out.println("Confirming tear down completed by checking each endpoint");
		
		Thread.sleep(30000 );
		
		System.out.println("===Tear down ended===");
	}	
	
	@Test 
	public void addMatchingPodAndServiceAndCheckAttributes() throws InterruptedException{

		System.out.println("=================================");
		System.out.println("Beginning test: " +Thread.currentThread().getStackTrace()[1].getMethodName());

		GRMEdgeTestUtil.addRC(GRMEdgeTestConstants.KUBE_TEST_POD_MATCH_NAME_1_ATTR, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_1_ATTR, "1",GRMEdgeTestConstants.testnamespace);
		GRMEdgeTestUtil.addService(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_NAME_1_ATTR, GRMEdgeTestConstants.KUBE_TEST_APP_NAME_MATCH_1_ATTR,GRMEdgeTestConstants.testnamespace);

		GRMEdgeTestUtil.getEndPointsfromKubernetesWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_1_ATTR,"DEV",GRMEdgeTestConstants.SLEEP*3,1);
		GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunningWithAutomatedSleep(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_1_ATTR, "DEV", GRMEdgeTestConstants.SLEEP*3, 1);
		List<ServiceEndPoint> seps = GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunning(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_1_ATTR, GRMEdgeTestConstants.namespace, "DEV").getServiceEndPointList();
		assertEquals(seps.size(),1);

		assertEquals(seps.get(0).getName().equalsIgnoreCase(GRMEdgeUtil.convertSEPNamespaceToDots(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_1_ATTR)),true);

		Thread.sleep(35000);
		if (System.getProperty("CHECK_GRM","true").equalsIgnoreCase("true")){
			assertEquals(GRMEdgeTestUtil.getEndPointsFromGRM(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_1_ATTR,"DEV").getServiceEndPointList().size(),1);
		}
		
		
		FindServiceEndPointResponse responseRunningFromKube = GRMEdgeTestUtil.getEndPointsFromKubernetes(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_1_ATTR,"DEV");
		ServiceEndPoint endpointFromKube = responseRunningFromKube.getServiceEndPointList().get(0);
		System.out.println("endpointFromKube " + endpointFromKube);
		assertEquals(endpointFromKube.getLatitude(), "1.0");
		assertEquals(endpointFromKube.getLongitude(), "1.0");
		assertEquals(endpointFromKube.getRouteOffer(), "TEST");
		assertEquals(endpointFromKube.getProtocol(), "http");
		OperationalInfo operInfoFromKube = endpointFromKube.getOperationalInfo();
		
		String host = endpointFromKube.getHostAddress();
		String port  = endpointFromKube.getListenPort();
		VersionDefinition ver = endpointFromKube.getVersion();	
		String contextPath = endpointFromKube.getContextPath();
		String name = endpointFromKube.getName();
		XMLGregorianCalendar expirationTime = endpointFromKube.getExpirationTime();

		List<NameValuePair> propsFromKube = endpointFromKube.getProperties();
		

		Thread.sleep(35000);
		FindServiceEndPointResponse responseFromGrm = GRMEdgeTestUtil.getEndPointsFromGRM(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_1_ATTR,"DEV");
		ServiceEndPoint endpointFromGrm = responseFromGrm.getServiceEndPointList().get(0);
		System.out.println("endpointFromGrm " + endpointFromGrm);
		assertEquals(endpointFromGrm.getLatitude(), "1.0");
		assertEquals(endpointFromGrm.getLongitude(), "1.0");
		assertEquals(endpointFromGrm.getRouteOffer(), "TEST");
		assertEquals(endpointFromGrm.getProtocol(), "http");
		assertEquals(endpointFromGrm.getListenPort(), port);
		assertEquals(endpointFromGrm.getHostAddress(), host);
		assertEquals(endpointFromGrm.getVersion().getMajor(), ver.getMajor());
		assertEquals(endpointFromGrm.getVersion().getMinor(), ver.getMinor());
		assertEquals(endpointFromGrm.getVersion().getPatch(), ver.getPatch());
		assertEquals(endpointFromGrm.getContextPath(), contextPath);
		assertEquals(endpointFromGrm.getName(), name);		
		XMLGregorianCalendar expirationTimeGrm = endpointFromGrm.getExpirationTime();
 		assertEquals(expirationTime.getDay(), expirationTimeGrm.getDay());		
		assertEquals(expirationTime.getHour(), expirationTimeGrm.getHour()+Integer.valueOf(System.getProperty("TIME_DIFF")));		
		assertEquals(expirationTime.getMinute(), expirationTimeGrm.getMinute());		
		assertEquals(expirationTime.getSecond(), expirationTimeGrm.getSecond());		
		assertEquals(expirationTime.getYear(), expirationTimeGrm.getYear());		
		assertEquals(expirationTime.getMonth(), expirationTimeGrm.getMonth());		
		assertEquals(operInfoFromKube.getCreatedBy(),endpointFromGrm.getOperationalInfo().getCreatedBy());		
		if(operInfoFromKube != null )
			assertEquals(operInfoFromKube.getUpdatedBy(),endpointFromGrm.getOperationalInfo().getUpdatedBy());		
		assertEquals(operInfoFromKube.getCreatedTimestamp().getDay(), endpointFromGrm.getOperationalInfo().getCreatedTimestamp().getDay());
		assertEquals(operInfoFromKube.getCreatedTimestamp().getHour(), endpointFromGrm.getOperationalInfo().getCreatedTimestamp().getHour()+Integer.valueOf(System.getProperty("TIME_DIFF")));
		assertEquals(operInfoFromKube.getCreatedTimestamp().getMinute(), endpointFromGrm.getOperationalInfo().getCreatedTimestamp().getMinute());
		assertEquals(operInfoFromKube.getCreatedTimestamp().getSecond(), endpointFromGrm.getOperationalInfo().getCreatedTimestamp().getSecond());
		assertEquals(operInfoFromKube.getCreatedTimestamp().getYear(), endpointFromGrm.getOperationalInfo().getCreatedTimestamp().getYear());
		assertEquals(operInfoFromKube.getCreatedTimestamp().getMonth(), endpointFromGrm.getOperationalInfo().getCreatedTimestamp().getMonth());
		if(operInfoFromKube.getUpdatedTimestamp() != null){
			assertEquals(operInfoFromKube.getUpdatedTimestamp().getDay(), endpointFromGrm.getOperationalInfo().getUpdatedTimestamp().getDay());
			assertEquals(operInfoFromKube.getUpdatedTimestamp().getHour(), endpointFromGrm.getOperationalInfo().getUpdatedTimestamp().getHour()+Integer.valueOf(System.getProperty("TIME_DIFF")));
			assertEquals(operInfoFromKube.getUpdatedTimestamp().getMinute(), endpointFromGrm.getOperationalInfo().getUpdatedTimestamp().getMinute());
			assertEquals(operInfoFromKube.getUpdatedTimestamp().getSecond(), endpointFromGrm.getOperationalInfo().getUpdatedTimestamp().getSecond());
			assertEquals(operInfoFromKube.getUpdatedTimestamp().getYear(), endpointFromGrm.getOperationalInfo().getUpdatedTimestamp().getYear());
			assertEquals(operInfoFromKube.getUpdatedTimestamp().getMonth(), endpointFromGrm.getOperationalInfo().getUpdatedTimestamp().getMonth());
		}
		FindServiceEndPointResponse responseRunningFromKubeRunning = GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunning(GRMEdgeTestConstants.KUBE_TEST_SERVICE_MATCH_ENDPOINT_NAME_1_ATTR,GRMEdgeTestConstants.namespace, "DEV");
		ServiceEndPoint endpointFromKubeRunning = responseRunningFromKubeRunning.getServiceEndPointList().get(0);
		System.out.println("endpointFromKubeRunning " + endpointFromKubeRunning);
		assertEquals(endpointFromKubeRunning.getLatitude(), "1.0");
		assertEquals(endpointFromKubeRunning.getLongitude(), "1.0");
		assertEquals(endpointFromKubeRunning.getRouteOffer(), "TEST");
		assertEquals(endpointFromKubeRunning.getProtocol(), "http");
		assertEquals(endpointFromKubeRunning.getListenPort(), port);
		assertEquals(endpointFromKubeRunning.getHostAddress(), host);
		assertEquals(endpointFromKubeRunning.getVersion().getMajor(), ver.getMajor());
		assertEquals(endpointFromKubeRunning.getVersion().getMinor(), ver.getMinor());
		assertEquals(endpointFromKubeRunning.getVersion().getPatch(), ver.getPatch());
		assertEquals(endpointFromKubeRunning.getContextPath(), contextPath);
		assertEquals(endpointFromKubeRunning.getName(), name);		
		XMLGregorianCalendar expirationTimeFromKubeRunning = endpointFromKubeRunning.getExpirationTime();
		assertEquals(expirationTime.getDay(), expirationTimeFromKubeRunning.getDay());		
		assertEquals(expirationTime.getHour(), expirationTimeFromKubeRunning.getHour());		
		assertEquals(expirationTime.getMinute(), expirationTimeFromKubeRunning.getMinute());		
		assertEquals(expirationTime.getSecond(), expirationTimeFromKubeRunning.getSecond());		
		assertEquals(expirationTime.getYear(), expirationTimeFromKubeRunning.getYear());		
		assertEquals(expirationTime.getMonth(), expirationTimeFromKubeRunning.getMonth());		
		
		XMLGregorianCalendar regTimeFromKubeRunning = endpointFromKubeRunning.getRegistrationTime();
		XMLGregorianCalendar regTimeKube = endpointFromKube.getRegistrationTime();
		assertEquals(regTimeFromKubeRunning.getDay(), regTimeKube.getDay());		
		assertEquals(regTimeFromKubeRunning.getHour(), regTimeKube.getHour());		
		assertEquals(regTimeFromKubeRunning.getMinute(), regTimeKube.getMinute());		
		assertEquals(regTimeFromKubeRunning.getSecond(), regTimeKube.getSecond());		
		assertEquals(regTimeFromKubeRunning.getYear(), regTimeKube.getYear());		
		assertEquals(regTimeFromKubeRunning.getMonth(), regTimeKube.getMonth());		
		
		OperationalInfo operInfoFromKubeRunning = endpointFromKubeRunning.getOperationalInfo();
		
		assertEquals(operInfoFromKubeRunning.getCreatedBy(), operInfoFromKube.getCreatedBy());		
		assertEquals(operInfoFromKubeRunning.getUpdatedBy(), operInfoFromKube.getUpdatedBy());		
		assertEquals(operInfoFromKubeRunning.getCreatedTimestamp(), operInfoFromKube.getCreatedTimestamp());		
		assertEquals(operInfoFromKubeRunning.getUpdatedTimestamp(), operInfoFromKube.getUpdatedTimestamp());		

		List<NameValuePair> propsFromKubeRunning = endpointFromKubeRunning.getProperties();
		Iterator<NameValuePair> iter = propsFromKube.iterator();
		while(iter.hasNext()){
			NameValuePair pair = iter.next();
			boolean flag = false;
			Iterator<NameValuePair> iter2 = propsFromKubeRunning.iterator();
			while(iter2.hasNext()){
				NameValuePair pair2 = iter2.next();
				if (pair.getName().equals(pair2.getName()) && pair.getValue().equals(pair2.getValue())){
					flag = true;
					break;
				}else{
					flag = false;
				}
			}
			if(!flag){
				fail("NameValue pair Match failed between two Kube endpoints : " + pair);
			}
		}
		
		List<NameValuePair> propsFromGrm = endpointFromGrm.getProperties();
		iter = propsFromKube.iterator();
		while(iter.hasNext()){
			NameValuePair pair = iter.next();
			boolean flag = false;
			Iterator<NameValuePair> iter2 = propsFromGrm.iterator();
			while(iter2.hasNext()){
				NameValuePair pair2 = iter2.next();
				if (pair.getName().equals(pair2.getName()) && pair.getValue().equals(pair2.getValue())){
					flag = true;
					break;
				}else{
					flag = false;
				}
			}
			if(!flag){
				fail("NameValue pair Match failed between Kube and Grm endpoints : " + pair);
			}
		}

		
		OperationalInfo operInfoFromGrm = endpointFromGrm.getOperationalInfo();
		
//		assertEquals(operInfoFromGrm.getCreatedBy(), operInfoFromKube.getCreatedBy());		
//		assertEquals(operInfoFromGrm.getUpdatedBy(), operInfoFromKube.getUpdatedBy());		
//		assertEquals(operInfoFromGrm.getCreatedTimestamp(), operInfoFromKube.getCreatedTimestamp());		
//		assertEquals(operInfoFromGrm.getUpdatedTimestamp(), operInfoFromKube.getUpdatedTimestamp());		
		
		System.out.println("=================================");
			
	}



	@Test 
	public void updateMatchingPodAndServiceAndCheckAttributes() throws InterruptedException{

		Thread.sleep(1000);
		
		System.out.println("=================================");
		System.out.println("Beginning test: " +Thread.currentThread().getStackTrace()[1].getMethodName());

		GRMEdgeTestUtil.addRC(GRMEdgeTestConstants.KUBE_UPD_TEST_POD_MATCH_NAME_1_ATTR, GRMEdgeTestConstants.KUBE_UPD_TEST_APP_NAME_MATCH_1_ATTR, "1",GRMEdgeTestConstants.testnamespace);
		GRMEdgeTestUtil.addService(GRMEdgeTestConstants.KUBE_UPD_TEST_SERVICE_MATCH_NAME_1_ATTR, GRMEdgeTestConstants.KUBE_UPD_TEST_APP_NAME_MATCH_1_ATTR,GRMEdgeTestConstants.testnamespace);

		GRMEdgeTestUtil.getEndPointsfromKubernetesWithAutomatedSleep(GRMEdgeTestConstants.KUBE_UPD_TEST_SERVICE_MATCH_ENDPOINT_NAME_1_ATTR,"DEV",GRMEdgeTestConstants.SLEEP*3,1);
		GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunningWithAutomatedSleep(GRMEdgeTestConstants.KUBE_UPD_TEST_SERVICE_MATCH_ENDPOINT_NAME_1_ATTR, "DEV", GRMEdgeTestConstants.SLEEP*3, 1);
		List<ServiceEndPoint> seps = GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunning(GRMEdgeTestConstants.KUBE_UPD_TEST_SERVICE_MATCH_ENDPOINT_NAME_1_ATTR, GRMEdgeTestConstants.namespace, "DEV").getServiceEndPointList();
		assertEquals(seps.size(),1);

		assertEquals(seps.get(0).getName().equalsIgnoreCase(GRMEdgeUtil.convertSEPNamespaceToDots(GRMEdgeTestConstants.KUBE_UPD_TEST_SERVICE_MATCH_ENDPOINT_NAME_1_ATTR)),true);
		
		Thread.sleep(30000);
		if (System.getProperty("CHECK_GRM","true").equalsIgnoreCase("true")){
			assertEquals(GRMEdgeTestUtil.getEndPointsFromGRM(GRMEdgeTestConstants.KUBE_UPD_TEST_SERVICE_MATCH_ENDPOINT_NAME_1_ATTR,"DEV").getServiceEndPointList().size(),1);
		}
		
		
		FindServiceEndPointResponse responseRunningFromKube = GRMEdgeTestUtil.getEndPointsFromKubernetes(GRMEdgeTestConstants.KUBE_UPD_TEST_SERVICE_MATCH_ENDPOINT_NAME_1_ATTR,"DEV");
		ServiceEndPoint endpointFromKube = responseRunningFromKube.getServiceEndPointList().get(0);
		System.out.println("endpointFromKube " + endpointFromKube);
		assertEquals(endpointFromKube.getLatitude(), "1.0");
		assertEquals(endpointFromKube.getLongitude(), "1.0");
		assertEquals(endpointFromKube.getRouteOffer(), "TEST");
		assertEquals(endpointFromKube.getProtocol(), "http");
		OperationalInfo operInfoFromKube = endpointFromKube.getOperationalInfo();
		
		String host = endpointFromKube.getHostAddress();
		String port  = endpointFromKube.getListenPort();
		VersionDefinition ver = endpointFromKube.getVersion();	
		String contextPath = endpointFromKube.getContextPath();
		String name = endpointFromKube.getName();
		XMLGregorianCalendar expirationTime = endpointFromKube.getExpirationTime();

		List<NameValuePair> propsFromKube = endpointFromKube.getProperties();
		

		Thread.sleep(35000);
		FindServiceEndPointResponse responseFromGrm = GRMEdgeTestUtil.getEndPointsFromGRM(GRMEdgeTestConstants.KUBE_UPD_TEST_SERVICE_MATCH_ENDPOINT_NAME_1_ATTR,"DEV");
		ServiceEndPoint endpointFromGrm = responseFromGrm.getServiceEndPointList().get(0);
		System.out.println("endpointFromGrm " + endpointFromGrm);
		assertEquals(endpointFromGrm.getLatitude(), "1.0");
		assertEquals(endpointFromGrm.getLongitude(), "1.0");
		assertEquals(endpointFromGrm.getRouteOffer(), "TEST");
		assertEquals(endpointFromGrm.getProtocol(), "http");
		assertEquals(endpointFromGrm.getListenPort(), port);
		assertEquals(endpointFromGrm.getHostAddress(), host);
		assertEquals(endpointFromGrm.getVersion().getMajor(), ver.getMajor());
		assertEquals(endpointFromGrm.getVersion().getMinor(), ver.getMinor());
		assertEquals(endpointFromGrm.getVersion().getPatch(), ver.getPatch());
		assertEquals(endpointFromGrm.getContextPath(), contextPath);
		assertEquals(endpointFromGrm.getName(), name);		
		XMLGregorianCalendar expirationTimeGrm = endpointFromGrm.getExpirationTime();
		assertEquals(expirationTime.getDay(), expirationTimeGrm.getDay());		
		assertEquals(expirationTime.getHour(), expirationTimeGrm.getHour()+Integer.valueOf(System.getProperty("TIME_DIFF")));		
		assertEquals(expirationTime.getMinute(), expirationTimeGrm.getMinute());		
		assertEquals(expirationTime.getSecond(), expirationTimeGrm.getSecond());		
		assertEquals(expirationTime.getYear(), expirationTimeGrm.getYear());		
		assertEquals(expirationTime.getMonth(), expirationTimeGrm.getMonth());				
		assertEquals(operInfoFromKube.getCreatedBy(),endpointFromGrm.getOperationalInfo().getCreatedBy());		
		if(operInfoFromKube != null )
			assertEquals(operInfoFromKube.getUpdatedBy(),endpointFromGrm.getOperationalInfo().getUpdatedBy());		
		assertEquals(operInfoFromKube.getCreatedTimestamp().getDay(), endpointFromGrm.getOperationalInfo().getCreatedTimestamp().getDay());
		assertEquals(operInfoFromKube.getCreatedTimestamp().getHour(), endpointFromGrm.getOperationalInfo().getCreatedTimestamp().getHour()+Integer.valueOf(System.getProperty("TIME_DIFF")));
		assertEquals(operInfoFromKube.getCreatedTimestamp().getMinute(), endpointFromGrm.getOperationalInfo().getCreatedTimestamp().getMinute());
		assertEquals(operInfoFromKube.getCreatedTimestamp().getSecond(), endpointFromGrm.getOperationalInfo().getCreatedTimestamp().getSecond());
		assertEquals(operInfoFromKube.getCreatedTimestamp().getYear(), endpointFromGrm.getOperationalInfo().getCreatedTimestamp().getYear());
		assertEquals(operInfoFromKube.getCreatedTimestamp().getMonth(), endpointFromGrm.getOperationalInfo().getCreatedTimestamp().getMonth());
		if(operInfoFromKube.getUpdatedTimestamp() != null){
			assertEquals(operInfoFromKube.getUpdatedTimestamp().getDay(), endpointFromGrm.getOperationalInfo().getUpdatedTimestamp().getDay());
			assertEquals(operInfoFromKube.getUpdatedTimestamp().getHour(), endpointFromGrm.getOperationalInfo().getUpdatedTimestamp().getHour()+Integer.valueOf(System.getProperty("TIME_DIFF")));
			assertEquals(operInfoFromKube.getUpdatedTimestamp().getMinute(), endpointFromGrm.getOperationalInfo().getUpdatedTimestamp().getMinute());
			assertEquals(operInfoFromKube.getUpdatedTimestamp().getSecond(), endpointFromGrm.getOperationalInfo().getUpdatedTimestamp().getSecond());
			assertEquals(operInfoFromKube.getUpdatedTimestamp().getYear(), endpointFromGrm.getOperationalInfo().getUpdatedTimestamp().getYear());
			assertEquals(operInfoFromKube.getUpdatedTimestamp().getMonth(), endpointFromGrm.getOperationalInfo().getUpdatedTimestamp().getMonth());
		}
		
		XMLGregorianCalendar registrationTimeGrm = endpointFromGrm.getRegistrationTime();
		String registrationTimeGrmStr = registrationTimeGrm.toString();
		
		int day = registrationTimeGrm.getDay();
		int hour = registrationTimeGrm.getHour();
		int month = registrationTimeGrm.getMonth();
		int year = registrationTimeGrm.getYear();
		int msec = registrationTimeGrm.getSecond();		
		
		FindServiceEndPointResponse responseRunningFromKubeRunning = GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunning(GRMEdgeTestConstants.KUBE_UPD_TEST_SERVICE_MATCH_ENDPOINT_NAME_1_ATTR,GRMEdgeTestConstants.namespace, "DEV");
		ServiceEndPoint endpointFromKubeRunning = responseRunningFromKubeRunning.getServiceEndPointList().get(0);
		System.out.println("endpointFromKubeRunning " + endpointFromKubeRunning);
		assertEquals(endpointFromKubeRunning.getLatitude(), "1.0");
		assertEquals(endpointFromKubeRunning.getLongitude(), "1.0");
		assertEquals(endpointFromKubeRunning.getRouteOffer(), "TEST");
		assertEquals(endpointFromKubeRunning.getProtocol(), "http");
		assertEquals(endpointFromKubeRunning.getListenPort(), port);
		assertEquals(endpointFromKubeRunning.getHostAddress(), host);
		assertEquals(endpointFromKubeRunning.getVersion().getMajor(), ver.getMajor());
		assertEquals(endpointFromKubeRunning.getVersion().getMinor(), ver.getMinor());
		assertEquals(endpointFromKubeRunning.getVersion().getPatch(), ver.getPatch());
		assertEquals(endpointFromKubeRunning.getContextPath(), contextPath);
		assertEquals(endpointFromKubeRunning.getName(), name);		
		XMLGregorianCalendar expirationTimeFromKubeRunning = endpointFromKubeRunning.getExpirationTime();
		assertEquals(expirationTime.getDay(), expirationTimeFromKubeRunning.getDay());		
		assertEquals(expirationTime.getHour(), expirationTimeFromKubeRunning.getHour());		
		assertEquals(expirationTime.getMinute(), expirationTimeFromKubeRunning.getMinute());		
		assertEquals(expirationTime.getSecond(), expirationTimeFromKubeRunning.getSecond());		
		assertEquals(expirationTime.getYear(), expirationTimeFromKubeRunning.getYear());		
		assertEquals(expirationTime.getMonth(), expirationTimeFromKubeRunning.getMonth());		
		
		XMLGregorianCalendar regTimeFromKubeRunning = endpointFromKubeRunning.getRegistrationTime();
		XMLGregorianCalendar regTimeKube = endpointFromKube.getRegistrationTime();
		assertEquals(regTimeFromKubeRunning.getDay(), regTimeKube.getDay());		
		assertEquals(regTimeFromKubeRunning.getHour(), regTimeKube.getHour());		
		assertEquals(regTimeFromKubeRunning.getMinute(), regTimeKube.getMinute());		
		assertEquals(regTimeFromKubeRunning.getSecond(), regTimeKube.getSecond());		
		assertEquals(regTimeFromKubeRunning.getYear(), regTimeKube.getYear());		
		assertEquals(regTimeFromKubeRunning.getMonth(), regTimeKube.getMonth());		
		
		OperationalInfo operInfoFromKubeRunning = endpointFromKubeRunning.getOperationalInfo();
		
		assertEquals(operInfoFromKubeRunning.getCreatedBy(), operInfoFromKube.getCreatedBy());		
		assertEquals(operInfoFromKubeRunning.getUpdatedBy(), operInfoFromKube.getUpdatedBy());		
		assertEquals(operInfoFromKubeRunning.getCreatedTimestamp(), operInfoFromKube.getCreatedTimestamp());		
		assertEquals(operInfoFromKubeRunning.getUpdatedTimestamp(), operInfoFromKube.getUpdatedTimestamp());		

		List<NameValuePair> propsFromKubeRunning = endpointFromKubeRunning.getProperties();
		Iterator<NameValuePair> iter = propsFromKube.iterator();
		while(iter.hasNext()){
			NameValuePair pair = iter.next();
			boolean flag = false;
			Iterator<NameValuePair> iter2 = propsFromKubeRunning.iterator();
			while(iter2.hasNext()){
				NameValuePair pair2 = iter2.next();
				if (pair.getName().equals(pair2.getName()) && pair.getValue().equals(pair2.getValue())){
					flag = true;
					break;
				}else{
					flag = false;
				}
			}
			if(!flag){
				fail("NameValue pair Match failed between two Kube endpoints : " + pair);
			}
		}
		
		List<NameValuePair> propsFromGrm = endpointFromGrm.getProperties();
		iter = propsFromKube.iterator();
		while(iter.hasNext()){
			NameValuePair pair = iter.next();
			boolean flag = false;
			Iterator<NameValuePair> iter2 = propsFromGrm.iterator();
			while(iter2.hasNext()){
				NameValuePair pair2 = iter2.next();
				if (pair.getName().equals(pair2.getName()) && pair.getValue().equals(pair2.getValue())){
					flag = true;
					break;
				}else{
					flag = false;
				}
			}
			if(!flag){
				fail("NameValue pair Match failed between Kube and Grm endpoints : " + pair);
			}
		}

		
		XMLGregorianCalendar regTimeFromKubeRunning1 = endpointFromKubeRunning.getRegistrationTime();
		String registrationTimeKubeStr1 = regTimeFromKubeRunning1.toString();

		
		OperationalInfo operInfoFromGrm = endpointFromGrm.getOperationalInfo();
		
//		assertEquals(operInfoFromGrm.getCreatedBy(), operInfoFromKube.getCreatedBy());		
//		assertEquals(operInfoFromGrm.getUpdatedBy(), operInfoFromKube.getUpdatedBy());		
//		assertEquals(operInfoFromGrm.getCreatedTimestamp(), operInfoFromKube.getCreatedTimestamp());		
//		assertEquals(operInfoFromGrm.getUpdatedTimestamp(), operInfoFromKube.getUpdatedTimestamp());		
		
		System.out.println("=================================");
	
		GRMEdgeTestUtil.updateService2(GRMEdgeTestConstants.KUBE_UPD_TEST_SERVICE_MATCH_NAME_1_ATTR, GRMEdgeTestConstants.KUBE_UPD_TEST_APP_NAME_MATCH_1_ATTR,GRMEdgeTestConstants.testnamespace);
		
		Thread.sleep(10000);
		
		GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunningWithAutomatedSleep(GRMEdgeTestConstants.KUBE_UPD_TEST_SERVICE_MATCH_ENDPOINT_NAME_1_ATTR,"DEV", 30000l,1);
		responseRunningFromKube = GRMEdgeTestUtil.getEndPointsFromKubernetes(GRMEdgeTestConstants.KUBE_UPD_TEST_SERVICE_MATCH_ENDPOINT_NAME_1_ATTR,"DEV");
		endpointFromKube = responseRunningFromKube.getServiceEndPointList().get(0);
		System.out.println("endpointFromKube " + endpointFromKube);
		assertEquals(endpointFromKube.getLatitude(), "1.0");
		assertEquals(endpointFromKube.getLongitude(), "1.0");
		assertEquals(endpointFromKube.getRouteOffer(), "TEST");
		assertEquals(endpointFromKube.getProtocol(), "https");
		operInfoFromKube = endpointFromKube.getOperationalInfo();
		
		host = endpointFromKube.getHostAddress();
		port  = endpointFromKube.getListenPort();
		ver = endpointFromKube.getVersion();	
		contextPath = endpointFromKube.getContextPath();
		name = endpointFromKube.getName();
		expirationTime = endpointFromKube.getExpirationTime();

		propsFromKube = endpointFromKube.getProperties();
		
		Thread.sleep(35000);
		responseFromGrm = GRMEdgeTestUtil.getEndPointsFromGRM(GRMEdgeTestConstants.KUBE_UPD_TEST_SERVICE_MATCH_ENDPOINT_NAME_1_ATTR,"DEV");
		endpointFromGrm = responseFromGrm.getServiceEndPointList().get(0);
		System.out.println("endpointFromGrm " + endpointFromGrm);
		assertEquals(endpointFromGrm.getLatitude(), "1.0");
		assertEquals(endpointFromGrm.getLongitude(), "1.0");
		assertEquals(endpointFromGrm.getRouteOffer(), "TEST");
		assertEquals(endpointFromGrm.getProtocol(), "https");
		assertEquals(endpointFromGrm.getListenPort(), port);
		assertEquals(endpointFromGrm.getHostAddress(), host);
		assertEquals(endpointFromGrm.getVersion().getMajor(), ver.getMajor());
		assertEquals(endpointFromGrm.getVersion().getMinor(), ver.getMinor());
		assertEquals(endpointFromGrm.getVersion().getPatch(), ver.getPatch());
		assertEquals(endpointFromGrm.getContextPath(), contextPath);
		assertEquals(endpointFromGrm.getName(), name);		
		expirationTimeGrm = endpointFromGrm.getExpirationTime();
		assertEquals(expirationTime.getDay(), expirationTimeGrm.getDay());		
		assertEquals(expirationTime.getHour(), expirationTimeGrm.getHour()+Integer.valueOf(System.getProperty("TIME_DIFF")));		
		assertEquals(expirationTime.getMinute(), expirationTimeGrm.getMinute());		
		assertEquals(expirationTime.getSecond(), expirationTimeGrm.getSecond());		
		assertEquals(expirationTime.getYear(), expirationTimeGrm.getYear());		
		assertEquals(expirationTime.getMonth(), expirationTimeGrm.getMonth());				
		assertEquals(operInfoFromKube.getCreatedBy(),endpointFromGrm.getOperationalInfo().getCreatedBy());		
		if(operInfoFromKube != null )
			assertEquals(operInfoFromKube.getUpdatedBy(),endpointFromGrm.getOperationalInfo().getUpdatedBy());		
		assertEquals(operInfoFromKube.getCreatedTimestamp().getDay(), endpointFromGrm.getOperationalInfo().getCreatedTimestamp().getDay());
		assertEquals(operInfoFromKube.getCreatedTimestamp().getHour(), endpointFromGrm.getOperationalInfo().getCreatedTimestamp().getHour()+Integer.valueOf(System.getProperty("TIME_DIFF")));
		assertEquals(operInfoFromKube.getCreatedTimestamp().getMinute(), endpointFromGrm.getOperationalInfo().getCreatedTimestamp().getMinute());
		assertEquals(operInfoFromKube.getCreatedTimestamp().getSecond(), endpointFromGrm.getOperationalInfo().getCreatedTimestamp().getSecond());
		assertEquals(operInfoFromKube.getCreatedTimestamp().getYear(), endpointFromGrm.getOperationalInfo().getCreatedTimestamp().getYear());
		assertEquals(operInfoFromKube.getCreatedTimestamp().getMonth(), endpointFromGrm.getOperationalInfo().getCreatedTimestamp().getMonth());
		if(operInfoFromKube.getUpdatedTimestamp() != null){
			assertEquals(operInfoFromKube.getUpdatedTimestamp().getDay(), endpointFromGrm.getOperationalInfo().getUpdatedTimestamp().getDay());
			assertEquals(operInfoFromKube.getUpdatedTimestamp().getHour(), endpointFromGrm.getOperationalInfo().getUpdatedTimestamp().getHour()+Integer.valueOf(System.getProperty("TIME_DIFF")));
			assertEquals(operInfoFromKube.getUpdatedTimestamp().getMinute(), endpointFromGrm.getOperationalInfo().getUpdatedTimestamp().getMinute());
			assertEquals(operInfoFromKube.getUpdatedTimestamp().getSecond(), endpointFromGrm.getOperationalInfo().getUpdatedTimestamp().getSecond());
			assertEquals(operInfoFromKube.getUpdatedTimestamp().getYear(), endpointFromGrm.getOperationalInfo().getUpdatedTimestamp().getYear());
			assertEquals(operInfoFromKube.getUpdatedTimestamp().getMonth(), endpointFromGrm.getOperationalInfo().getUpdatedTimestamp().getMonth());
		}
		
		
		registrationTimeGrm = endpointFromGrm.getRegistrationTime();
		String registrationTimeGrmStr2 = registrationTimeGrm.toString();
		
		System.out.println("registrationTimeGrmStr2 : " + registrationTimeGrmStr2);
		System.out.println("registrationTimeGrmStr : " + registrationTimeGrmStr);		
		Assert.assertNotEquals(registrationTimeGrmStr2, registrationTimeGrmStr);

		responseRunningFromKubeRunning = GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunning(GRMEdgeTestConstants.KUBE_UPD_TEST_SERVICE_MATCH_ENDPOINT_NAME_1_ATTR,GRMEdgeTestConstants.namespace, "DEV");
		endpointFromKubeRunning = responseRunningFromKubeRunning.getServiceEndPointList().get(0);
		System.out.println("endpointFromKubeRunning " + endpointFromKubeRunning);
		assertEquals(endpointFromKubeRunning.getLatitude(), "1.0");
		assertEquals(endpointFromKubeRunning.getLongitude(), "1.0");
		assertEquals(endpointFromKubeRunning.getRouteOffer(), "TEST");
		assertEquals(endpointFromKubeRunning.getProtocol(), "https");
		assertEquals(endpointFromKubeRunning.getListenPort(), port);
		assertEquals(endpointFromKubeRunning.getHostAddress(), host);
		assertEquals(endpointFromKubeRunning.getVersion().getMajor(), ver.getMajor());
		assertEquals(endpointFromKubeRunning.getVersion().getMinor(), ver.getMinor());
		assertEquals(endpointFromKubeRunning.getVersion().getPatch(), ver.getPatch());
		assertEquals(endpointFromKubeRunning.getContextPath(), contextPath);
		assertEquals(endpointFromKubeRunning.getName(), name);		
		expirationTimeFromKubeRunning = endpointFromKubeRunning.getExpirationTime();
		assertEquals(expirationTime.getDay(), expirationTimeFromKubeRunning.getDay());		
		assertEquals(expirationTime.getHour(), expirationTimeFromKubeRunning.getHour());		
		assertEquals(expirationTime.getMinute(), expirationTimeFromKubeRunning.getMinute());		
		assertEquals(expirationTime.getSecond(), expirationTimeFromKubeRunning.getSecond());		
		assertEquals(expirationTime.getYear(), expirationTimeFromKubeRunning.getYear());		
		assertEquals(expirationTime.getMonth(), expirationTimeFromKubeRunning.getMonth());		
		
		regTimeFromKubeRunning = endpointFromKubeRunning.getRegistrationTime();
		regTimeKube = endpointFromKube.getRegistrationTime();
		assertEquals(regTimeFromKubeRunning.getDay(), regTimeKube.getDay());		
		assertEquals(regTimeFromKubeRunning.getHour(), regTimeKube.getHour());		
		assertEquals(regTimeFromKubeRunning.getMinute(), regTimeKube.getMinute());		
		assertEquals(regTimeFromKubeRunning.getSecond(), regTimeKube.getSecond());		
		assertEquals(regTimeFromKubeRunning.getYear(), regTimeKube.getYear());		
		assertEquals(regTimeFromKubeRunning.getMonth(), regTimeKube.getMonth());		
		
		operInfoFromKubeRunning = endpointFromKubeRunning.getOperationalInfo();
		
		assertEquals(operInfoFromKubeRunning.getCreatedBy(), operInfoFromKube.getCreatedBy());		
		assertEquals(operInfoFromKubeRunning.getUpdatedBy(), operInfoFromKube.getUpdatedBy());		
		assertEquals(operInfoFromKubeRunning.getCreatedTimestamp(), operInfoFromKube.getCreatedTimestamp());		
		assertEquals(operInfoFromKubeRunning.getUpdatedTimestamp(), operInfoFromKube.getUpdatedTimestamp());		

		XMLGregorianCalendar regTimeFromKubeRunning2 = endpointFromKubeRunning.getRegistrationTime();
		String registrationTimeKubeStr2 = regTimeFromKubeRunning2.toString();
		
		System.out.println("registrationTimeKubeStr2 : " + registrationTimeKubeStr2);
		System.out.println("registrationTimeKubeStr1 : " + registrationTimeKubeStr1);		
		Assert.assertNotEquals(registrationTimeKubeStr2, registrationTimeKubeStr1);

		propsFromKubeRunning = endpointFromKubeRunning.getProperties();
		iter = propsFromKube.iterator();
		while(iter.hasNext()){
			NameValuePair pair = iter.next();
			boolean flag = false;
			Iterator<NameValuePair> iter2 = propsFromKubeRunning.iterator();
			while(iter2.hasNext()){
				NameValuePair pair2 = iter2.next();
				if (pair.getName().equals(pair2.getName()) && pair.getValue().equals(pair2.getValue())){
					flag = true;
					break;
				}else{
					flag = false;
				}
			}
			if(!flag){
				fail("NameValue pair Match failed between two Kube endpoints : " + pair);
			}
		}
		
		propsFromGrm = endpointFromGrm.getProperties();
		iter = propsFromKube.iterator();
		while(iter.hasNext()){
			NameValuePair pair = iter.next();
			boolean flag = false;
			Iterator<NameValuePair> iter2 = propsFromGrm.iterator();
			while(iter2.hasNext()){
				NameValuePair pair2 = iter2.next();
				if (pair.getName().equals(pair2.getName()) && pair.getValue().equals(pair2.getValue())){
					flag = true;
					break;
				}else{
					flag = false;
				}
			}
			if(!flag){
				fail("NameValue pair Match failed between Kube and Grm endpoints : " + pair);
			}
		}

//change routeoffer		
		GRMEdgeTestUtil.updateService3(GRMEdgeTestConstants.KUBE_UPD_TEST_SERVICE_MATCH_NAME_1_ATTR, GRMEdgeTestConstants.KUBE_UPD_TEST_APP_NAME_MATCH_1_ATTR,GRMEdgeTestConstants.testnamespace);

		GRMEdgeTestUtil.addRCRouteOffer(GRMEdgeTestConstants.KUBE_UPD_TEST_POD_MATCH_NAME_1_ATTR+"r", GRMEdgeTestConstants.KUBE_UPD_TEST_APP_NAME_MATCH_1_ATTR, "1",GRMEdgeTestConstants.testnamespace, "DEFAULT");
		
		Thread.sleep(10000);
		
		responseRunningFromKube = GRMEdgeTestUtil.getEndPointsFromKubernetes(GRMEdgeTestConstants.KUBE_UPD_TEST_SERVICE_MATCH_ENDPOINT_NAME_1_ATTR,"DEV");
		endpointFromKube = responseRunningFromKube.getServiceEndPointList().get(0);
		System.out.println("endpointFromKube * " + endpointFromKube);
		assertEquals(endpointFromKube.getLatitude(), "1.0");
		assertEquals(endpointFromKube.getLongitude(), "1.0");
		assertEquals(endpointFromKube.getRouteOffer(), "DEFAULT");
		assertEquals(endpointFromKube.getProtocol(), "https");
		operInfoFromKube = endpointFromKube.getOperationalInfo();
		
		host = endpointFromKube.getHostAddress();
		port  = endpointFromKube.getListenPort();
		assertEquals(port, "31990");		
		ver = endpointFromKube.getVersion();	
		contextPath = endpointFromKube.getContextPath();
		name = endpointFromKube.getName();
		expirationTime = endpointFromKube.getExpirationTime();

		propsFromKube = endpointFromKube.getProperties();
		

		Thread.sleep(35000);
		responseFromGrm = GRMEdgeTestUtil.getEndPointsFromGRM(GRMEdgeTestConstants.KUBE_UPD_TEST_SERVICE_MATCH_ENDPOINT_NAME_1_ATTR,"DEV");
		endpointFromGrm = responseFromGrm.getServiceEndPointList().get(0);
		System.out.println("endpointFromGrm * " + endpointFromGrm);
		assertEquals(endpointFromGrm.getLatitude(), "1.0");
		assertEquals(endpointFromGrm.getLongitude(), "1.0");
		assertEquals(endpointFromGrm.getRouteOffer(), "DEFAULT");
		assertEquals(endpointFromGrm.getProtocol(), "https");
		assertEquals(endpointFromGrm.getListenPort(), port);
		assertEquals(endpointFromGrm.getHostAddress(), host);
		assertEquals(endpointFromGrm.getVersion().getMajor(), ver.getMajor());
		assertEquals(endpointFromGrm.getVersion().getMinor(), ver.getMinor());
		assertEquals(endpointFromGrm.getVersion().getPatch(), ver.getPatch());
		assertEquals(endpointFromGrm.getContextPath(), contextPath);
		assertEquals(endpointFromGrm.getName(), name);		
		expirationTimeGrm = endpointFromGrm.getExpirationTime();
		assertEquals(expirationTime.getDay(), expirationTimeGrm.getDay());		
		assertEquals(expirationTime.getHour(), expirationTimeGrm.getHour()+Integer.valueOf(System.getProperty("TIME_DIFF")));		
		assertEquals(expirationTime.getMinute(), expirationTimeGrm.getMinute());		
		assertEquals(expirationTime.getSecond(), expirationTimeGrm.getSecond());		
		assertEquals(expirationTime.getYear(), expirationTimeGrm.getYear());		
		assertEquals(expirationTime.getMonth(), expirationTimeGrm.getMonth());				
		assertEquals(operInfoFromKube.getCreatedBy(),endpointFromGrm.getOperationalInfo().getCreatedBy());		
		if(operInfoFromKube != null )
			assertEquals(operInfoFromKube.getUpdatedBy(),endpointFromGrm.getOperationalInfo().getUpdatedBy());		
		assertEquals(operInfoFromKube.getCreatedTimestamp().getDay(), endpointFromGrm.getOperationalInfo().getCreatedTimestamp().getDay());
		assertEquals(operInfoFromKube.getCreatedTimestamp().getHour(), endpointFromGrm.getOperationalInfo().getCreatedTimestamp().getHour()+Integer.valueOf(System.getProperty("TIME_DIFF")));
		assertEquals(operInfoFromKube.getCreatedTimestamp().getMinute(), endpointFromGrm.getOperationalInfo().getCreatedTimestamp().getMinute());
		assertEquals(operInfoFromKube.getCreatedTimestamp().getSecond(), endpointFromGrm.getOperationalInfo().getCreatedTimestamp().getSecond());
		assertEquals(operInfoFromKube.getCreatedTimestamp().getYear(), endpointFromGrm.getOperationalInfo().getCreatedTimestamp().getYear());
		assertEquals(operInfoFromKube.getCreatedTimestamp().getMonth(), endpointFromGrm.getOperationalInfo().getCreatedTimestamp().getMonth());
		if(operInfoFromKube.getUpdatedTimestamp() != null){
			assertEquals(operInfoFromKube.getUpdatedTimestamp().getDay(), endpointFromGrm.getOperationalInfo().getUpdatedTimestamp().getDay());
			assertEquals(operInfoFromKube.getUpdatedTimestamp().getHour(), endpointFromGrm.getOperationalInfo().getUpdatedTimestamp().getHour()+Integer.valueOf(System.getProperty("TIME_DIFF")));
			assertEquals(operInfoFromKube.getUpdatedTimestamp().getMinute(), endpointFromGrm.getOperationalInfo().getUpdatedTimestamp().getMinute());
			assertEquals(operInfoFromKube.getUpdatedTimestamp().getSecond(), endpointFromGrm.getOperationalInfo().getUpdatedTimestamp().getSecond());
			assertEquals(operInfoFromKube.getUpdatedTimestamp().getYear(), endpointFromGrm.getOperationalInfo().getUpdatedTimestamp().getYear());
			assertEquals(operInfoFromKube.getUpdatedTimestamp().getMonth(), endpointFromGrm.getOperationalInfo().getUpdatedTimestamp().getMonth());
		}
		responseRunningFromKubeRunning = GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunning(GRMEdgeTestConstants.KUBE_UPD_TEST_SERVICE_MATCH_ENDPOINT_NAME_1_ATTR,GRMEdgeTestConstants.namespace, "DEV");
		endpointFromKubeRunning = responseRunningFromKubeRunning.getServiceEndPointList().get(0);
		System.out.println("endpointFromKubeRunning * " + endpointFromKubeRunning);
		assertEquals(endpointFromKubeRunning.getLatitude(), "1.0");
		assertEquals(endpointFromKubeRunning.getLongitude(), "1.0");
		assertEquals(endpointFromKubeRunning.getRouteOffer(), "DEFAULT");
		assertEquals(endpointFromKubeRunning.getProtocol(), "https");
		assertEquals(endpointFromKubeRunning.getListenPort(), port);
		assertEquals(endpointFromKubeRunning.getHostAddress(), host);
		assertEquals(endpointFromKubeRunning.getVersion().getMajor(), ver.getMajor());
		assertEquals(endpointFromKubeRunning.getVersion().getMinor(), ver.getMinor());
		assertEquals(endpointFromKubeRunning.getVersion().getPatch(), ver.getPatch());
		assertEquals(endpointFromKubeRunning.getContextPath(), contextPath);
		assertEquals(endpointFromKubeRunning.getName(), name);		
		expirationTimeFromKubeRunning = endpointFromKubeRunning.getExpirationTime();
		assertEquals(expirationTime.getDay(), expirationTimeFromKubeRunning.getDay());		
		assertEquals(expirationTime.getHour(), expirationTimeFromKubeRunning.getHour());		
		assertEquals(expirationTime.getMinute(), expirationTimeFromKubeRunning.getMinute());		
		assertEquals(expirationTime.getSecond(), expirationTimeFromKubeRunning.getSecond());		
		assertEquals(expirationTime.getYear(), expirationTimeFromKubeRunning.getYear());		
		assertEquals(expirationTime.getMonth(), expirationTimeFromKubeRunning.getMonth());		
		
		regTimeFromKubeRunning = endpointFromKubeRunning.getRegistrationTime();
		regTimeKube = endpointFromKube.getRegistrationTime();
		assertEquals(regTimeFromKubeRunning.getDay(), regTimeKube.getDay());		
		assertEquals(regTimeFromKubeRunning.getHour(), regTimeKube.getHour());		
		assertEquals(regTimeFromKubeRunning.getMinute(), regTimeKube.getMinute());		
		assertEquals(regTimeFromKubeRunning.getSecond(), regTimeKube.getSecond());		
		assertEquals(regTimeFromKubeRunning.getYear(), regTimeKube.getYear());		
		assertEquals(regTimeFromKubeRunning.getMonth(), regTimeKube.getMonth());		
		
		operInfoFromKubeRunning = endpointFromKubeRunning.getOperationalInfo();
		
		assertEquals(operInfoFromKubeRunning.getCreatedBy(), operInfoFromKube.getCreatedBy());		
		assertEquals(operInfoFromKubeRunning.getUpdatedBy(), operInfoFromKube.getUpdatedBy());		
		assertEquals(operInfoFromKubeRunning.getCreatedTimestamp(), operInfoFromKube.getCreatedTimestamp());		
		assertEquals(operInfoFromKubeRunning.getUpdatedTimestamp(), operInfoFromKube.getUpdatedTimestamp());		

		registrationTimeGrm = endpointFromGrm.getRegistrationTime();
		String registrationTimeGrmStr3 = registrationTimeGrm.toString();
		

		System.out.println("Unrelated but debug: registrationTimeGrmStr2 : " + registrationTimeGrmStr2);
		System.out.println("registrationTimeGrmStr3 : " + registrationTimeGrmStr3);
		System.out.println("registrationTimeGrmStr : " + registrationTimeGrmStr);		
		Assert.assertNotEquals(registrationTimeGrmStr3, registrationTimeGrmStr);
		
		XMLGregorianCalendar regTimeFromKubeRunning3 = endpointFromKubeRunning.getRegistrationTime();
		String registrationTimeKubeStr3 = regTimeFromKubeRunning3.toString();
		
		System.out.println("registrationTimeKubeStr3 : " + registrationTimeKubeStr3);
		System.out.println("registrationTimeKubeStr1 : " + registrationTimeKubeStr1);		
		Assert.assertNotEquals(registrationTimeKubeStr3, registrationTimeKubeStr1);
		
		propsFromKubeRunning = endpointFromKubeRunning.getProperties();
		iter = propsFromKube.iterator();
		while(iter.hasNext()){
			NameValuePair pair = iter.next();
			boolean flag = false;
			Iterator<NameValuePair> iter2 = propsFromKubeRunning.iterator();
			while(iter2.hasNext()){
				NameValuePair pair2 = iter2.next();
				if (pair.getName().equals(pair2.getName()) && pair.getValue().equals(pair2.getValue())){
					flag = true;
					break;
				}else{
					flag = false;
				}
			}
			if(!flag){
				fail("NameValue pair Match failed between two Kube endpoints : " + pair);
			}
		}
		
		propsFromGrm = endpointFromGrm.getProperties();
		iter = propsFromKube.iterator();
		while(iter.hasNext()){
			NameValuePair pair = iter.next();
			boolean flag = false;
			Iterator<NameValuePair> iter2 = propsFromGrm.iterator();
			while(iter2.hasNext()){
				NameValuePair pair2 = iter2.next();
				if (pair.getName().equals(pair2.getName()) && pair.getValue().equals(pair2.getValue())){
					flag = true;
					break;
				}else{
					flag = false;
				}
			}
			if(!flag){
				fail("NameValue pair Match failed between Kube and Grm endpoints : " + pair);
			}
		}
		
//change port to 31992 from 31990
		GRMEdgeTestUtil.updateService4(GRMEdgeTestConstants.KUBE_UPD_TEST_SERVICE_MATCH_NAME_1_ATTR, GRMEdgeTestConstants.KUBE_UPD_TEST_APP_NAME_MATCH_1_ATTR,GRMEdgeTestConstants.testnamespace);		
		
		Thread.sleep(10000);
		
		responseRunningFromKube = GRMEdgeTestUtil.getEndPointsFromKubernetes(GRMEdgeTestConstants.KUBE_UPD_TEST_SERVICE_MATCH_ENDPOINT_NAME_1_ATTR,"DEV");
		endpointFromKube = responseRunningFromKube.getServiceEndPointList().get(0);
		System.out.println("endpointFromKube ** " + endpointFromKube);
		assertEquals(endpointFromKube.getLatitude(), "1.0");
		assertEquals(endpointFromKube.getLongitude(), "1.0");
		assertEquals(endpointFromKube.getRouteOffer(), "DEFAULT");
		assertEquals(endpointFromKube.getProtocol(), "https");
		operInfoFromKube = endpointFromKube.getOperationalInfo();
		
		host = endpointFromKube.getHostAddress();
		port  = endpointFromKube.getListenPort();
		assertEquals(port, "31992");
		
		ver = endpointFromKube.getVersion();	

		contextPath = endpointFromKube.getContextPath();
		name = endpointFromKube.getName();
		expirationTime = endpointFromKube.getExpirationTime();

		propsFromKube = endpointFromKube.getProperties();
		


		Thread.sleep(35000);
		responseFromGrm = GRMEdgeTestUtil.getEndPointsFromGRM(GRMEdgeTestConstants.KUBE_UPD_TEST_SERVICE_MATCH_ENDPOINT_NAME_1_ATTR,"DEV");
		endpointFromGrm = responseFromGrm.getServiceEndPointList().get(0);
		System.out.println("endpointFromGrm ** " + endpointFromGrm);
		assertEquals(endpointFromGrm.getLatitude(), "1.0");
		assertEquals(endpointFromGrm.getLongitude(), "1.0");
		assertEquals(endpointFromGrm.getRouteOffer(), "DEFAULT");
		assertEquals(endpointFromGrm.getProtocol(), "https");
		assertEquals(endpointFromGrm.getListenPort(), port);
		assertEquals(endpointFromGrm.getHostAddress(), host);
		assertEquals(endpointFromGrm.getVersion().getMajor(), ver.getMajor());
		assertEquals(endpointFromGrm.getVersion().getMinor(), ver.getMinor());
		assertEquals(endpointFromGrm.getVersion().getPatch(), ver.getPatch());
		assertEquals(endpointFromGrm.getContextPath(), contextPath);
		assertEquals(endpointFromGrm.getName(), name);		
		expirationTimeGrm = endpointFromGrm.getExpirationTime();
		assertEquals(expirationTime.getDay(), expirationTimeGrm.getDay());		
		assertEquals(expirationTime.getHour(), expirationTimeGrm.getHour()+Integer.valueOf(System.getProperty("TIME_DIFF")));		
		assertEquals(expirationTime.getMinute(), expirationTimeGrm.getMinute());		
		assertEquals(expirationTime.getSecond(), expirationTimeGrm.getSecond());		
		assertEquals(expirationTime.getYear(), expirationTimeGrm.getYear());		
		assertEquals(expirationTime.getMonth(), expirationTimeGrm.getMonth());				
		assertEquals(operInfoFromKube.getCreatedBy(),endpointFromGrm.getOperationalInfo().getCreatedBy());		
		if(operInfoFromKube != null )
			assertEquals(operInfoFromKube.getUpdatedBy(),endpointFromGrm.getOperationalInfo().getUpdatedBy());		
		assertEquals(operInfoFromKube.getCreatedTimestamp().getDay(), endpointFromGrm.getOperationalInfo().getCreatedTimestamp().getDay());
		assertEquals(operInfoFromKube.getCreatedTimestamp().getHour(), endpointFromGrm.getOperationalInfo().getCreatedTimestamp().getHour()+Integer.valueOf(System.getProperty("TIME_DIFF")));
		assertEquals(operInfoFromKube.getCreatedTimestamp().getMinute(), endpointFromGrm.getOperationalInfo().getCreatedTimestamp().getMinute());
		assertEquals(operInfoFromKube.getCreatedTimestamp().getSecond(), endpointFromGrm.getOperationalInfo().getCreatedTimestamp().getSecond());
		assertEquals(operInfoFromKube.getCreatedTimestamp().getYear(), endpointFromGrm.getOperationalInfo().getCreatedTimestamp().getYear());
		assertEquals(operInfoFromKube.getCreatedTimestamp().getMonth(), endpointFromGrm.getOperationalInfo().getCreatedTimestamp().getMonth());
		if(operInfoFromKube.getUpdatedTimestamp() != null){
			assertEquals(operInfoFromKube.getUpdatedTimestamp().getDay(), endpointFromGrm.getOperationalInfo().getUpdatedTimestamp().getDay());
			assertEquals(operInfoFromKube.getUpdatedTimestamp().getHour(), endpointFromGrm.getOperationalInfo().getUpdatedTimestamp().getHour()+Integer.valueOf(System.getProperty("TIME_DIFF")));
			assertEquals(operInfoFromKube.getUpdatedTimestamp().getMinute(), endpointFromGrm.getOperationalInfo().getUpdatedTimestamp().getMinute());
			assertEquals(operInfoFromKube.getUpdatedTimestamp().getSecond(), endpointFromGrm.getOperationalInfo().getUpdatedTimestamp().getSecond());
			assertEquals(operInfoFromKube.getUpdatedTimestamp().getYear(), endpointFromGrm.getOperationalInfo().getUpdatedTimestamp().getYear());
			assertEquals(operInfoFromKube.getUpdatedTimestamp().getMonth(), endpointFromGrm.getOperationalInfo().getUpdatedTimestamp().getMonth());
		}
		responseRunningFromKubeRunning = GRMEdgeTestUtil.getEndPointsfromKubernetesFindRunning(GRMEdgeTestConstants.KUBE_UPD_TEST_SERVICE_MATCH_ENDPOINT_NAME_1_ATTR,GRMEdgeTestConstants.namespace, "DEV");
		endpointFromKubeRunning = responseRunningFromKubeRunning.getServiceEndPointList().get(0);
		System.out.println("endpointFromKubeRunning ** " + endpointFromKubeRunning);
		assertEquals(endpointFromKubeRunning.getLatitude(), "1.0");
		assertEquals(endpointFromKubeRunning.getLongitude(), "1.0");
		assertEquals(endpointFromKubeRunning.getRouteOffer(), "DEFAULT");
		assertEquals(endpointFromKubeRunning.getProtocol(), "https");
		assertEquals(endpointFromKubeRunning.getListenPort(), port);
		assertEquals(endpointFromKubeRunning.getHostAddress(), host);
		assertEquals(endpointFromKubeRunning.getVersion().getMajor(), ver.getMajor());
		assertEquals(endpointFromKubeRunning.getVersion().getMinor(), ver.getMinor());
		assertEquals(endpointFromKubeRunning.getVersion().getPatch(), ver.getPatch());
		assertEquals(endpointFromKubeRunning.getContextPath(), contextPath);
		assertEquals(endpointFromKubeRunning.getName(), name);		
		expirationTimeFromKubeRunning = endpointFromKubeRunning.getExpirationTime();
		assertEquals(expirationTime.getDay(), expirationTimeFromKubeRunning.getDay());		
		assertEquals(expirationTime.getHour(), expirationTimeFromKubeRunning.getHour());		
		assertEquals(expirationTime.getMinute(), expirationTimeFromKubeRunning.getMinute());		
		assertEquals(expirationTime.getSecond(), expirationTimeFromKubeRunning.getSecond());		
		assertEquals(expirationTime.getYear(), expirationTimeFromKubeRunning.getYear());		
		assertEquals(expirationTime.getMonth(), expirationTimeFromKubeRunning.getMonth());		
		
		regTimeFromKubeRunning = endpointFromKubeRunning.getRegistrationTime();
		regTimeKube = endpointFromKube.getRegistrationTime();
		assertEquals(regTimeFromKubeRunning.getDay(), regTimeKube.getDay());		
		assertEquals(regTimeFromKubeRunning.getHour(), regTimeKube.getHour());		
		assertEquals(regTimeFromKubeRunning.getMinute(), regTimeKube.getMinute());		
		assertEquals(regTimeFromKubeRunning.getSecond(), regTimeKube.getSecond());		
		assertEquals(regTimeFromKubeRunning.getYear(), regTimeKube.getYear());		
		assertEquals(regTimeFromKubeRunning.getMonth(), regTimeKube.getMonth());		
		
		operInfoFromKubeRunning = endpointFromKubeRunning.getOperationalInfo();
		
		assertEquals(operInfoFromKubeRunning.getCreatedBy(), operInfoFromKube.getCreatedBy());		
		assertEquals(operInfoFromKubeRunning.getUpdatedBy(), operInfoFromKube.getUpdatedBy());		
		assertEquals(operInfoFromKubeRunning.getCreatedTimestamp(), operInfoFromKube.getCreatedTimestamp());		
		assertEquals(operInfoFromKubeRunning.getUpdatedTimestamp(), operInfoFromKube.getUpdatedTimestamp());		

		registrationTimeGrm = endpointFromGrm.getRegistrationTime();
		String registrationTimeGrmStr4 = registrationTimeGrm.toString();
		
		System.out.println("registrationTimeGrmStr4 : " + registrationTimeGrmStr4);
		System.out.println("registrationTimeGrmStr : " + registrationTimeGrmStr);		
		Assert.assertNotEquals(registrationTimeGrmStr4, registrationTimeGrmStr);

		XMLGregorianCalendar regTimeFromKubeRunning4 = endpointFromKubeRunning.getRegistrationTime();
		String registrationTimeKubeStr4 = regTimeFromKubeRunning4.toString();
		
		System.out.println("registrationTimeKubeStr4 : " + registrationTimeKubeStr4);
		System.out.println("registrationTimeKubeStr1 : " + registrationTimeKubeStr1);		
		Assert.assertNotEquals(registrationTimeKubeStr4, registrationTimeKubeStr1);

		propsFromKubeRunning = endpointFromKubeRunning.getProperties();
		iter = propsFromKube.iterator();
		while(iter.hasNext()){
			NameValuePair pair = iter.next();
			boolean flag = false;
			Iterator<NameValuePair> iter2 = propsFromKubeRunning.iterator();
			while(iter2.hasNext()){
				NameValuePair pair2 = iter2.next();
				if (pair.getName().equals(pair2.getName()) && pair.getValue().equals(pair2.getValue())){
					flag = true;
					break;
				}else{
					flag = false;
				}
			}
			if(!flag){
				fail("NameValue pair Match failed between two Kube endpoints : " + pair);
			}
		}
		
		propsFromGrm = endpointFromGrm.getProperties();
		iter = propsFromKube.iterator();
		while(iter.hasNext()){
			NameValuePair pair = iter.next();
			boolean flag = false;
			Iterator<NameValuePair> iter2 = propsFromGrm.iterator();
			while(iter2.hasNext()){
				NameValuePair pair2 = iter2.next();
				if (pair.getName().equals(pair2.getName()) && pair.getValue().equals(pair2.getValue())){
					flag = true;
					break;
				}else{
					flag = false;
				}
			}
			if(!flag){
				fail("NameValue pair Match failed between Kube and Grm endpoints : " + pair);
			}
		}
	
	}
	
}
