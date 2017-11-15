/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.ocnp.mgmt.grm_edge_service.topology.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.att.ocnp.mgmt.grm_edge_service.EdgeException;
import com.att.ocnp.mgmt.grm_edge_service.topology.domain.ServiceDefinition;
import com.att.ocnp.mgmt.grm_edge_service.topology.domain.ServiceEndPoint;
import com.att.ocnp.mgmt.grm_edge_service.topology.domain.ServiceVersionDefinition;
import com.att.ocnp.mgmt.grm_edge_service.util.Configuration;
import com.att.ocnp.mgmt.grm_edge_service.util.DateUtil;
import com.att.ocnp.mgmt.grm_edge_service.util.GRMEdgeConstants;
import com.att.ocnp.mgmt.grm_edge_service.util.GRMEdgeUtil;
import com.att.ocnp.mgmt.grm_edge_service.util.InputDataValidator;
import com.att.scld.grm.types.v1.Status;
import com.att.scld.grm.types.v1.VersionDefinition;
import com.att.scld.grm.v1.AddServiceEndPointRequest;
import com.att.scld.grm.v1.DeleteServiceEndPointRequest;


public class DataAccessManager {
	private static final Logger logger = LoggerFactory.getLogger(DataAccessManager.class);

	private static DataAccessManager dataManager = null;
    public static boolean logicalDelete = Configuration.getBootStrapConfig().getBooleanValue("GRM_CASSANDRA_LOGICAL_DELETE", false);

    private static final int SEP_EXPIRATION_INTERVAL_MINS_VAL = Configuration.getBootStrapConfig().getIntValue(GRMEdgeConstants.SEP_EXPIRATION_INTERVAL_MINS, GRMEdgeConstants.SEP_EXPIRATION_INTERVAL_MINS_DEFAULT);
    private static final int REREGISTRATION_INTERVAL_DEFAULT = Configuration.getBootStrapConfig().getIntValue(GRMEdgeConstants.REREGISTRATION_INTERVAL_DEFAULT_PARAM, GRMEdgeConstants.REREGISTRATION_INTERVAL_DEFAULT_VAL);
	
	public static final String NS_MGMT_GRP = "com.att.scld.namespacemgmt";
	private DbManager dbManager = null;
	
	private DataAccessManager() {
		dbManager = DbManager.getInstance();
	}
	
	public static DataAccessManager getInstance() {
		if (dataManager == null) {
			synchronized (DataAccessManager.class) {
				dataManager = new DataAccessManager();
			}
		} 
		return dataManager;		
	}
	
	public void deleteServiceEndPoint(DeleteServiceEndPointRequest request) {
		logger.trace("start with parameter : {}" , request);
		try {
			for (com.att.scld.grm.types.v1.ServiceEndPoint sep : request.getServiceEndPoint()) {
				ServiceEndPoint sepExt = DomainObjectFactory.createSEPExt(sep, false, request.getEnv());
		        ServiceEndPoint exisSep = getServiceEndPointById(sepExt.getServiceEndPointId());
		        if (exisSep != null)
		        	dbManager.deleteServiceEndPoint(exisSep);
		        else 
		        	logger.error("Endpoint being deleted does not exist in the db.Id: {}" , sepExt.getServiceEndPointId());
			}
		} catch (Exception e) {
			logger.error("Error while deleting service endpoint", e); 
		}
    }
    
    public void addOrUpdateServiceEndPoint(AddServiceEndPointRequest request) throws EdgeException {
		logger.trace("start with parameter {}:" , request);
		InputDataValidator.validateSEPReqFields(request.getServiceEndPoint());
		InputDataValidator.validateResourceName(request.getServiceEndPoint().getName());
		if (request.getServiceEndPoint().getClientSupportedVersions() != null) {
			final VersionDefinition versionToValidate = 
					Configuration.getBootStrapConfig().getBooleanValue("service-endpoint.validate-version-in-range", true) ? request.getServiceEndPoint().getVersion() : null;
			InputDataValidator.validateSupportedVersions(request.getServiceEndPoint().getClientSupportedVersions(), versionToValidate);
		}
		ServiceEndPoint sepExt = DomainObjectFactory.createSEPExt(request.getServiceEndPoint(), false, request.getEnv());
		
        // create a temp SEP.
        ServiceEndPoint exisSep = getServiceEndPointById(sepExt.getServiceEndPointId());
        if (exisSep != null) { // if size is 1, the sep exists.
            logger.trace( "Code=Server.Process; addRUpdateServiceEndPoint: updating ServiceEndPoint.");
            updateServiceEndPoint(sepExt, exisSep, false);
            logger.trace( "Code=Server.Process; addRUpdateServiceEndPoint: updated ServiceEndPoint.");
        } else { // if size is 0, the sep does not exist
            logger.trace( "Code=Server.Process; addRUpdateServiceEndPoint: adding ServiceEndPoint.");
            addServiceEndPoint(sepExt, request.isCheckNcreateParents());
        }
		
    }
    
    public List<ServiceEndPoint> getServiceEndPointByEnvAndName(String env, String serviceName) throws EdgeException {
        return dbManager.findServiceEndPointByEnvAndName(env, serviceName);
    }
    
    public ServiceEndPoint getServiceEndPointById(String sepId) throws EdgeException {
        return dbManager.findServiceEndPointById(sepId);
    }

    private void updateServiceEndPoint(ServiceEndPoint sepExt, ServiceEndPoint exisSepExt, boolean updateLease) throws EdgeException {
		logger.trace("start with parameters {}:" , sepExt, exisSepExt, updateLease);
        if(updateLease){
            Calendar now = Calendar.getInstance(GRMEdgeConstants.getTimeZone(), GRMEdgeConstants.getLocale());
            now.add(Calendar.MINUTE, SEP_EXPIRATION_INTERVAL_MINS_VAL);
           sepExt.setExpirationTime(new Date()); 
        }

        dbManager.updateServiceEndPoint(sepExt, exisSepExt);
    }

    private void addServiceEndPoint(ServiceEndPoint serviceEp, boolean checkNcreateParents) throws EdgeException {
		logger.trace("start with parameter: {}" , serviceEp);    	
        if (checkNcreateParents) {
            ServiceDefinition serviceDefinition = new ServiceDefinition();
            serviceDefinition.setServiceDefinitionName(serviceEp.getName());
            serviceDefinition.setServiceDefinitionId(GRMEdgeUtil.getId(serviceEp.getName(), serviceEp.getEnvironment()));
            serviceDefinition.setEnvironment(serviceEp.getEnvironment());
            serviceDefinition.setServiceNamespaceId(serviceEp.getServiceNamespaceId());
            
            checkNAddServiceDefintion(serviceDefinition);

            ServiceVersionDefinition serviceVersionDefinition = new ServiceVersionDefinition();
            
            serviceVersionDefinition.setServiceVersionDefinitionName(GRMEdgeUtil.getSVDName(serviceEp.getName() + GRMEdgeUtil.NAME_SEP + serviceEp.getVersion()));
            serviceEp.setServiceVersionDefinitionName(serviceVersionDefinition.getServiceVersionDefinitionName());
            serviceVersionDefinition.setServiceVersionDefinitionId(GRMEdgeUtil.getId(serviceEp.getName() + GRMEdgeUtil.NAME_SEP + serviceEp.getVersion(), serviceEp.getEnvironment()));
            //TODO tbd what is this for?
            serviceVersionDefinition.setMajorVersion(serviceEp.getMajorVersion());
            serviceVersionDefinition.setMinorVersion(serviceEp.getMinorVersion());
            serviceVersionDefinition.setPatchVersion(serviceEp.getPatchVersion());
            serviceVersionDefinition.setReRegistrationInterval(REREGISTRATION_INTERVAL_DEFAULT);
            serviceVersionDefinition.setServiceDefinitionName(serviceDefinition.getServiceDefinitionName());
            serviceVersionDefinition.setServiceNamespaceId(serviceEp.getServiceNamespaceId());
            serviceVersionDefinition.setVer(serviceEp.getVersion());
            serviceVersionDefinition.setEnvironment(serviceEp.getEnvironment());
            checkNAddServiceVersionDefintion(serviceVersionDefinition, serviceEp.getServiceNamespaceId(), serviceEp.getEnvironment());
        } else {
            ServiceVersionDefinition svd = getServiceVersionDefinition(serviceEp.getServiceDefinitionName(), serviceEp.getVersion(), serviceEp.getServiceNamespaceId(), serviceEp.getEnvironment());
            if (svd == null) {
                throw new EdgeException(GRMEdgeConstants.PARENT_NOT_FOUND, GRMEdgeConstants.PARENT_NOT_FOUND_MSG, "addServiceEndPoint", "ServiceEndPoint", serviceEp.getServiceEndPointId());
            }
        }

        //This should already be set and never be entered
        if (serviceEp.getCreatedTimestamp() == null) {
        	serviceEp.setCreatedTimestamp(new Date());// 
        }
        

        //This should already be set and never be entered
        if (serviceEp.getRegistrationTime() == null) {
            serviceEp.setRegistrationTime(serviceEp.getCreatedTimestamp()); 
        }
        
        //This should already be set and never be entered
        if (serviceEp.getExpirationTime() == null) {
			try {
				GregorianCalendar c2 = new GregorianCalendar();
	        	Date myDate;
				Calendar cal = Calendar.getInstance();
				cal.setTimeZone(TimeZone.getTimeZone("GMT"));
				cal.set(Calendar.MONTH, 9);
				cal.set(Calendar.DATE, 9);
				cal.set(Calendar.YEAR, 9999);
				myDate = cal.getTime();
			
				serviceEp.setExpirationTime(myDate);
			} catch (Exception e) {
				//ignore this block shouldn't even occur
			}
        }

        if (serviceEp.getStatus() == null) {
            // when adding a service end point, if status is null, initialize to RUNNING.
            serviceEp.setStatus(Status.RUNNING);
           serviceEp.setStatusCheckTime(new Date());
        }

        if ((serviceEp.getStatus() != null) && (serviceEp.getStatusCheckTime() == null)) {
            serviceEp.setStatusCheckTime(new Date()); 
        }

        if ((serviceEp.getEventCheckStatus() != null) && (serviceEp.getEventCheckTime() == null)) {
            serviceEp.setEventcheckTime(new Date()); 
        }

        dbManager.addServiceEndPoint(serviceEp);
    }

    //this is doing a check and add.
    public void addServiceDefintion(ServiceDefinition serviceDef) throws EdgeException {
		logger.trace("start with parameter: {}" , serviceDef);    	
        //if exists throw an exception.
        if (this.getServiceDefinition(serviceDef.getServiceDefinitionId()) != null) {
            throw new EdgeException(GRMEdgeConstants.ADD_ALREADY_EXISTS, GRMEdgeConstants.ADD_ALREADY_EXISTS_ERR_MSG,
                    "addServiceDefintion", "ServiceDefinition", serviceDef.getServiceDefinitionId());
        } 

        serviceDef.setCreatedTimestamp(new Date()); 
        serviceDef.setCreatedBy("edge");
        
        dbManager.addServiceDefinition(serviceDef);
    }

    public void checkNAddServiceDefintion(ServiceDefinition inServiceDef) {
		logger.trace("start with parameter: {}" , inServiceDef);    	    	
        ServiceDefinition sd = getServiceDefinition(inServiceDef.getServiceDefinitionId());
        if (sd == null) {
            try {
                addServiceDefintion(inServiceDef);
            } catch (EdgeException ex) {
                if (ex.getMessage().contains(GRMEdgeConstants.ADD_ALREADY_EXISTS_MSG)) { //ignoring ADD_ALREADY_EXISTS.
                    logger.warn("Code=Server.Process.ignoreException; SD already exists exception while adding. message=" + ex.getMessage());
                } else {
                    throw ex;
                }
            }
        }
    }

    //(perf) we can pass in loadRef/loadBasic as old code to improve some performance.
    public ServiceDefinition getServiceDefinition(String serviceId) throws EdgeException {
		logger.trace("start with parameter: {}" , serviceId);    	    	    	
        ServiceDefinition ServiceDefinition = null;
        Collection<ServiceDefinition> servDefList = getServiceDefinitionList(serviceId);
        if(!servDefList.isEmpty()){
            ServiceDefinition = servDefList.iterator().next(); //there should only be one returned so we just get next
        }
        return ServiceDefinition;
    }
    
    public Collection<ServiceDefinition> getServiceDefinitionList(String serviceId) throws EdgeException {
		logger.trace("start with parameter: {}" , serviceId);    	    	    	    	
    	return dbManager.findServiceDefinitionList(serviceId);
    }
    
    public void addServiceVersionDefintion(ServiceVersionDefinition inSVD, String namespaceId, String env) {
        //if SVD already exist, throw Duplicate Object exception.
		logger.trace("start with parameter: {}" , inSVD);    	    	    	    	

    	ServiceVersionDefinition svdTemp = getServiceVersionDefinition(inSVD.getServiceDefinitionName(), inSVD.getVersion(), namespaceId, env);
        if (svdTemp != null) {
            throw new EdgeException(GRMEdgeConstants.ADD_ALREADY_EXISTS, GRMEdgeConstants.ADD_ALREADY_EXISTS_ERR_MSG,
                    "addServiceVersionDefintion", "ServiceVersionDefinition", inSVD.getServiceVersionDefinitionId());
        }
        inSVD.setCreatedTimestamp(new Date());
        inSVD.setCreatedBy("edge");

        dbManager.addServiceVersionDefinition(inSVD);
    }

    public void checkNAddServiceVersionDefintion(ServiceVersionDefinition inServiceVerDef, String namespaceId, String env) {

        ServiceVersionDefinition svd = getServiceVersionDefinition(inServiceVerDef.getServiceDefinitionName(), inServiceVerDef.getVersion(), namespaceId, env);

        if (svd == null) {
            try {
                addServiceVersionDefintion(inServiceVerDef, namespaceId, env);
            } catch (EdgeException ex) {
                if (ex.getMessage().equals(GRMEdgeConstants.ADD_ALREADY_EXISTS)) {
                    logger.warn("Code=Server.Process.ignoreException; SVD already exists exception while adding. message=" + ex.getMessage());
                } else {
                    throw ex;
                }
            }
        }

    }
    
    public ServiceVersionDefinition getServiceVersionDefinition(String sdName, String verDef, String namespaceId, String env) throws EdgeException {
    	Collection<ServiceVersionDefinition> svdList = getServiceVersionDefinitionList(sdName, verDef, namespaceId, env);
        if (!svdList.isEmpty()) {
            return svdList.iterator().next(); //should only be one returned so we just return it
        }

        return null;
    }
    
    private Collection<ServiceVersionDefinition> getServiceVersionDefinitionList(String sdName, String version, String namespaceId, String env) throws EdgeException {
		logger.trace("start with parameter: {}" , sdName);    	    	    	    	
    	String id = namespaceId + "." + sdName + GRMEdgeUtil.VERSION_SEP + version + GRMEdgeUtil.VERSION_SEP + env;		
        return dbManager.findSVDListById(id);
    }
	
}
