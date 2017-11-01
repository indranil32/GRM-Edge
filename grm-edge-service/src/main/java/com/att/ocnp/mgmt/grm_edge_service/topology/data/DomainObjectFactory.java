/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.ocnp.mgmt.grm_edge_service.topology.data;

import java.util.ArrayList;
import java.util.List;

import com.att.ocnp.mgmt.grm_edge_service.topology.domain.ServiceEndPoint;
import com.att.ocnp.mgmt.grm_edge_service.util.GRMEdgeUtil;

public class DomainObjectFactory {

    public static ServiceEndPoint createSEPExt(com.att.scld.grm.types.v1.ServiceEndPoint sep, boolean loadRefs, String env) {
        if (sep == null) {
            return null;
        }

        return createSEPExtNoChecks(sep, env);
    }
    
    public static ServiceEndPoint createSEPExtNoChecks(com.att.scld.grm.types.v1.ServiceEndPoint sep, String env) {
        if (sep == null) {
            return null;
        }
        ServiceEndPoint sepExt = new ServiceEndPoint();
        sepExt.setEnvironment(env);
        
        sepExt.setName(sep.getName());
        sepExt.setServiceDefinitionName(sep.getName());
        sepExt.setServiceNamespaceId(GRMEdgeUtil.getId(GRMEdgeUtil.getDomain(sep.getName()),env));
        sepExt.setDme2Version(sep.getDME2Version());
        sepExt.setClientSupportedVersions(sep.getClientSupportedVersions());
        sepExt.setMajorVersion(sep.getVersion().getMajor());
        sepExt.setMinorVersion(sep.getVersion().getMinor());
        sepExt.setPatchVersion(sep.getVersion().getPatch());
        sepExt.setHostAddress(sep.getHostAddress());
        sepExt.setListenPort(sep.getListenPort());
        sepExt.setLatitude(sep.getLatitude());
        sepExt.setLongitude(sep.getLongitude());
        sepExt.setRegistrationTime(sep.getRegistrationTime());
        sepExt.setExpirationTime(sep.getExpirationTime());
        sepExt.setContextPath(sep.getContextPath());
        sepExt.setRouteOffer(sep.getRouteOffer());
        sepExt.setStatus(sep.getStatusInfo().getStatus());
		sepExt.setDeletedtime(null);
   
        if (sep.getEventStatusInfo() != null) {
	        sepExt.setEventCheckStatus(sep.getEventStatusInfo().getStatus());
	        sepExt.setEventCheckStatusCode(sep.getEventStatusInfo().getStatusReasonCode());
	        sepExt.setEventcheckTime(sep.getEventStatusInfo().getStatusCheckTime());
	        sepExt.setEventCheckMessage(sep.getEventStatusInfo().getStatusReasonDescription());
        }
        sepExt.setDme2JDBCDatabaseName(sep.getDME2JDBCDatabaseName());
        sepExt.setDme2JDBCHealthCheckUser(sep.getDME2JDBCHealthCheckUser());
        sepExt.setDme2JDBCHealthCheckPassword(sep.getDME2JDBCHealthCheckPassword());
        sepExt.setDme2JDBCHealthCheckDriver(sep.getDME2JDBCHealthCheckDriver());

        if (sep.getProperties() != null) {
        	List<String> nameValues = sepExt.getProperties();
        	if (nameValues == null) {
        		nameValues = new ArrayList<>();
        		sepExt.setProperties(nameValues);
        	}
        	
            sepExt.getProperties().addAll(GRMEdgeUtil.getProperties(sep.getProperties()));
        }
        sepExt.setProtocol(sep.getProtocol());
        String ep;
		if (sep.getListenPort() == null) {
			ep = sep.getHostAddress();
		} else {
			ep = sep.getHostAddress() + "|" + sep.getListenPort();
		}
		//make sure this is the last property that is set since it is derived out of other props.
        sepExt.setServiceEndPointName(GRMEdgeUtil.getSEPName(sepExt.getServiceDefinitionName(), sepExt.getVersion(), sepExt.getHostAddress(), sepExt.getListenPort()));
	    sepExt.setServiceEndPointId(sepExt.getServiceEndPointId());        
	       
		sepExt.setEp(ep);

		if (sep.getOperationalInfo() != null){
			if(sep.getOperationalInfo().getCreatedBy() != null)
				sepExt.setCreatedBy(sep.getOperationalInfo().getCreatedBy());
			if(sep.getOperationalInfo().getCreatedTimestamp() != null)
				sepExt.setCreatedTimestamp(sep.getOperationalInfo().getCreatedTimestamp());
			if(sep.getOperationalInfo().getUpdatedBy() != null)
				sepExt.setUpdatedBy(sep.getOperationalInfo().getUpdatedBy());
			if(sep.getOperationalInfo().getUpdatedTimestamp() != null)
				sepExt.setUpdatedTimestamp(sep.getOperationalInfo().getUpdatedTimestamp());
		}
		
        return sepExt;
    }
    
 
 
}
