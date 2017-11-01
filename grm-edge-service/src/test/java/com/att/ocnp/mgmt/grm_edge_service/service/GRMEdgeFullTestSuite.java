/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.ocnp.mgmt.grm_edge_service.service;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

//specify a runner class: Suite.class
@RunWith(Suite.class)

//specify an array of test classes
@Suite.SuiteClasses({
	KubernetesIntegrationAttributesCheckingTestCases.class,
	KubernetesIntegrationTestCases.class,
	GRMEdgeUnitTests.class
}
)

public class GRMEdgeFullTestSuite {

}