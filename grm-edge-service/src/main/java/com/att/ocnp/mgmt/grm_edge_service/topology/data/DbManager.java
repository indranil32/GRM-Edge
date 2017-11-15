/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.ocnp.mgmt.grm_edge_service.topology.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.datatype.DatatypeFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.att.ocnp.mgmt.grm_edge_service.EdgeException;
import com.att.ocnp.mgmt.grm_edge_service.topology.domain.ServiceDefinition;
import com.att.ocnp.mgmt.grm_edge_service.topology.domain.ServiceEndPoint;
import com.att.ocnp.mgmt.grm_edge_service.topology.domain.ServiceVersionDefinition;
import com.att.ocnp.mgmt.grm_edge_service.util.Configuration;
import com.att.ocnp.mgmt.grm_edge_service.util.DateUtil;
import com.att.ocnp.mgmt.grm_edge_service.util.GRMEdgeConstants;
import com.att.ocnp.mgmt.grm_edge_service.util.InputDataValidator;

public class DbManager {
	private static final Logger logger = LoggerFactory.getLogger(DbManager.class);
	public static boolean logicalDelete = Configuration.getBootStrapConfig()
			.getBooleanValue("GRM_CASSANDRA_LOGICAL_DELETE", false);
	private static DbManager dbManager = null;
	@Autowired
	private EdgeDao edgeDao;

	private DbManager() {
	}

	public static DbManager getInstance() {
		if (dbManager == null) {
			synchronized (DbManager.class) {
				dbManager = new DbManager();
				logger.trace("dbManager instantiated sucessfully");
			}
		}
		return dbManager;
	}

	/*
	 * public String getEffectiveRouteXML(String env, String svcName) { long
	 * startTime = System.currentTimeMillis(); String routeXml = null; try {
	 * GridBag<String, String> syncGrid =
	 * odesseusGridProvider.createGridBag(String.class.getName());
	 * Collection<String> routeXmlList;
	 * 
	 * String query = "select * from effectiveroute"; Map<String, String> params
	 * = new HashMap<>(); params.put("env_serviceid", env + "_" + svcName);
	 * 
	 * logger.trace("getEffectiveRouteXML env_serviceid : {}_{}",env , svcName);
	 * routeXmlList = syncGrid.query(query, params);
	 * 
	 * if ((routeXmlList != null) && routeXmlList.size() > 1) { String error =
	 * "More than one definition of routeinfo XML found for service " + env +
	 * "_" + svcName; logger.error(error); throw new Exception(error); }
	 * 
	 * if ((routeXmlList != null) && !routeXmlList.isEmpty()) { Iterator iter =
	 * routeXmlList.iterator(); while (iter.hasNext() && (routeXml == null)) {
	 * routeXml = (String) iter.next(); } }
	 * 
	 * logger.trace("Code=getEffectiveRouteXML.PerfMessage" +
	 * "; routeXml.length=" + (routeXml!=null?routeXml.length():0) +
	 * "; elapsedTimeMS=" + (System.currentTimeMillis()-startTime)); } catch
	 * (Exception e) { logger.error("Error while finding Effective Route XML",
	 * e); throw new
	 * EdgeException(GRMEdgeConstants.INTERNAL_ERROR_CASSANDRA_EXECUTING_QUERY,
	 * GRMEdgeConstants.INTERNAL_ERROR_CASSANDRA_EXECUTING_QUERY_MSG,
	 * "DbManager.getEffectiveRouteXML", e); } return routeXml; }
	 */

	public ServiceEndPoint findServiceEndPointById(String sepId) {
		ServiceEndPoint result = null;
		try {
			long startTime = System.currentTimeMillis();

			Collection<ServiceEndPoint> sdList;

			String query = "select * from serviceendpoint";
			Map<String, String> params = new HashMap<>();
			params.put("serviceEndPointId", sepId);

			logger.trace("serviceEndPointId : {}", sepId);
			sdList = edgeDao.findServiceEndPointById(sepId);

			if ((sdList != null) && !sdList.isEmpty()) {
				Iterator iter = sdList.iterator();
				while (iter.hasNext() && (result == null)) {
					result = (ServiceEndPoint) iter.next();
					if ((result != null) && (result.getDeletedtime() != null))
						result = null;
				}
			}

			logger.trace("ServiceEndPointDAO.find; elapsedTimeMS={}", System.currentTimeMillis() - startTime);
		} catch (Exception e) {
			logger.error("Error while finding Service Endpoint", e);
			throw new EdgeException(GRMEdgeConstants.INTERNAL_ERROR_CASSANDRA_EXECUTING_QUERY,
					GRMEdgeConstants.INTERNAL_ERROR_CASSANDRA_EXECUTING_QUERY_MSG, "DbManager.findServiceEndPointById",
					e);
		}
		return result;
	}

	public List<ServiceEndPoint> findServiceEndPointByEnvAndName(String env, String serviceName) {
		List<ServiceEndPoint> result = new ArrayList<>();
		try {
			long startTime = System.currentTimeMillis();

			Collection<ServiceEndPoint> sdList;

			logger.trace("ServiceEndPointDAO.find; servicedefinitionname= {} environment={}", serviceName, env);

			// id = version env name and host
			// serviceName = namespace.serviceNameActual
			String query = "select * from serviceendpoint where servicedefinitionname ='" + serviceName
					+ "' and environment = '" + env + "' ALLOW FILTERING";

			try {
				sdList = edgeDao.findServiceEndPointByEnvAndName(env, serviceName);
			} catch (Exception e) {
				logger.warn("Got Exception while querying cassandra. ", e);
				return null;
			}

			if ((sdList != null) && !sdList.isEmpty()) {
				Iterator iter = sdList.iterator();
				while (iter.hasNext()) {
					result.add((ServiceEndPoint) iter.next());
				}
			}

			logger.trace("List returned from Cassandra: {}", result.toString());
			logger.trace("ServiceEndPointDAO.find; elapsedTimeMS=Ts", System.currentTimeMillis() - startTime);
		} catch (Exception e) {
			logger.error("Error while finding Service Endpoints", e);
			throw new EdgeException(GRMEdgeConstants.INTERNAL_ERROR_CASSANDRA_EXECUTING_QUERY,
					GRMEdgeConstants.INTERNAL_ERROR_CASSANDRA_EXECUTING_QUERY_MSG, "DbManager.findServiceEndPointById",
					e);
		}
		return result;
	}

	public void addServiceEndPoint(ServiceEndPoint sepExt) {
		try {
			long startTime = System.currentTimeMillis();
//			InputDataValidator.validateResourceName(sepExt.getName());
//			InputDataValidator.validateSEPFields(sepExt);
			logger.trace("addServiceEndPoint sepExt : {}", sepExt);

			edgeDao.addServiceEndPoint(sepExt);
			logger.trace("ServiceEndPointDAO.insert; elapsedTimeMS={}", System.currentTimeMillis() - startTime);
		} catch (Exception e) {
			logger.error("Error while adding Service Endpoint", e);
			throw new EdgeException(GRMEdgeConstants.INTERNAL_ERROR_CASSANDRA_EXECUTING_QUERY,
					GRMEdgeConstants.INTERNAL_ERROR_CASSANDRA_EXECUTING_QUERY_MSG, "DbManager.addServiceEndPoint", e);
		}
	}

	public void updateServiceEndPoint(ServiceEndPoint sepExt, ServiceEndPoint exisSep) {
		try {
			long startTime = System.currentTimeMillis();

			ServiceEndPoint sep = new ServiceEndPoint();
			sep.setServiceEndPointId(exisSep.getServiceEndPointId());
			sep.setName(exisSep.getName());
			sep.setServiceEndPointName(exisSep.getServiceEndPointName());
			sep.setMajorVersion(exisSep.getMajorVersion());
			sep.setMinorVersion(exisSep.getMinorVersion());
			sep.setHostAddress(exisSep.getHostAddress());
			sep.setListenPort(exisSep.getListenPort());
			sep.setPatchVersion(exisSep.getPatchVersion());
			sep.setServiceNamespaceId(exisSep.getServiceNamespaceId());
			sep.setServiceDefinitionName(exisSep.getServiceDefinitionName());
			sep.setServiceVersionDefinitionName(exisSep.getServiceVersionDefinitionName());
			sep.setEp(exisSep.getEp());
			sep.setEnvironment(exisSep.getEnvironment());
			sep.setDeletedtime(null);

			// when populating the updatable fields, we first populate the
			// newSep value and if it doesnot exist, populate the oldSep value.
			if ((sepExt.getLatitude() != null) && !sepExt.getLatitude().equals("0.0")
					&& !sepExt.getLatitude().equals("0") && !sepExt.getLatitude().equals(exisSep.getLatitude())) {
				sep.setLatitude(sepExt.getLatitude());
			} else {
				sep.setLatitude(exisSep.getLatitude());
			}

			if ((sepExt.getLongitude() != null) && !sepExt.getLongitude().equals("0.0")
					&& !sepExt.getLongitude().equals("0") && !sepExt.getLongitude().equals(exisSep.getLongitude())) {
				sep.setLongitude(sepExt.getLongitude());
			} else {
				sep.setLongitude(exisSep.getLongitude());
			}

			if ((sepExt.getContextPath() != null) && !sepExt.getContextPath().equals(exisSep.getContextPath())) {
				sep.setContextPath(sepExt.getContextPath());
			} else {
				sep.setContextPath(exisSep.getContextPath());
			}

			if ((sepExt.getRegistrationTime() != null)
					&& !sepExt.getRegistrationTime().equals(exisSep.getRegistrationTime())) {
				sep.setRegistrationTime(sepExt.getRegistrationTime());
			} else {
				sep.setRegistrationTime(exisSep.getRegistrationTime());
			}

			if ((sepExt.getExpirationTime() != null)
					&& !sepExt.getExpirationTime().equals(exisSep.getExpirationTime())) {
				sep.setExpirationTime(sepExt.getExpirationTime());
			} else {
				sep.setExpirationTime(exisSep.getExpirationTime());
			}

			if ((sepExt.getClientSupportedVersions() != null)
					&& !sepExt.getClientSupportedVersions().equals(exisSep.getClientSupportedVersions())) {
				sep.setClientSupportedVersions(sepExt.getClientSupportedVersions());
			} else {
				sep.setClientSupportedVersions(exisSep.getClientSupportedVersions());
			}

			if ((sepExt.getDme2Version() != null) && !sepExt.getDme2Version().equals(exisSep.getDme2Version())) {
				sep.setDme2Version(sepExt.getDme2Version());
			} else {
				sep.setDme2Version(exisSep.getDme2Version());
			}

			if ((sepExt.getProtocol() != null) && !sepExt.getProtocol().equals(exisSep.getProtocol())) {
				sep.setProtocol(sepExt.getProtocol());
			} else {
				sep.setProtocol(exisSep.getProtocol());
			}

			if ((sepExt.getRouteOffer() != null) && !sepExt.getRouteOffer().equals(exisSep.getRouteOffer())) {
				sep.setRouteOffer(sepExt.getRouteOffer());
			} else {
				sep.setRouteOffer(exisSep.getRouteOffer());
			}
			if (sepExt.getProperties() != null && !sepExt.getProperties().isEmpty()) {
				sep.setProperties(sepExt.getProperties());
			} else {
				sep.setProperties(exisSep.getProperties());
			}

			if ((sepExt.getStatus() != null) && !sepExt.getStatus().equals(exisSep.getStatus())) {
				sep.setStatus(sepExt.getStatus());
			} else {
				sep.setStatus(exisSep.getStatus());
			}

			if ((sepExt.getStatusCheckTime() != null)) {
				sep.setStatusCheckTime(sepExt.getStatusCheckTime());
			} else {
				sep.setStatusCheckTime(new Date());          
			}

			if ((sepExt.getEventCheckStatusCode() != null)
					&& !sepExt.getEventCheckStatusCode().equals(exisSep.getEventCheckStatusCode())) {
				sep.setEventCheckStatusCode(sepExt.getEventCheckStatusCode());
			} else {
				sep.setEventCheckStatusCode(exisSep.getEventCheckStatusCode());
			}

			if ((sepExt.getEventCheckMessage() != null)
					&& !sepExt.getEventCheckMessage().equals(exisSep.getEventCheckMessage())) {
				sep.setEventCheckMessage(sepExt.getEventCheckMessage());
			} else {
				sep.setEventCheckMessage(exisSep.getEventCheckMessage());
			}

			if ((sepExt.getEventCheckStatus() != null)
					&& !sepExt.getEventCheckStatus().equals(exisSep.getEventCheckStatus())) {
				sep.setEventCheckStatus(sepExt.getEventCheckStatus());
			} else {
				sep.setEventCheckStatus(exisSep.getEventCheckStatus());
			}

			if ((sepExt.getEventCheckTime() != null)) {
				sep.setEventcheckTime(sepExt.getEventCheckTime());
			} else {
				sep.setEventcheckTime(new Date());                
			}

			if ((sepExt.getDme2JDBCDatabaseName() != null)
					&& !sepExt.getDme2JDBCDatabaseName().equals(exisSep.getDme2JDBCDatabaseName())) {
				sep.setDme2JDBCDatabaseName(sepExt.getDme2JDBCDatabaseName());
			} else {
				sep.setDme2JDBCDatabaseName(exisSep.getDme2JDBCDatabaseName());
			}

			if ((sepExt.getDme2JDBCHealthCheckUser() != null)
					&& !sepExt.getDme2JDBCHealthCheckUser().equals(exisSep.getDme2JDBCHealthCheckUser())) {
				sep.setDme2JDBCHealthCheckUser(sepExt.getDme2JDBCHealthCheckUser());
			} else {
				sep.setDme2JDBCHealthCheckUser(exisSep.getDme2JDBCHealthCheckUser());
			}

			if ((sepExt.getDme2JDBCHealthCheckPassword() != null)
					&& !sepExt.getDme2JDBCHealthCheckPassword().equals(exisSep.getDme2JDBCHealthCheckPassword())) {
				sep.setDme2JDBCHealthCheckPassword(sepExt.getDme2JDBCHealthCheckPassword());
			} else {
				sep.setDme2JDBCHealthCheckPassword(exisSep.getDme2JDBCHealthCheckPassword());
			}

			if ((sepExt.getDme2JDBCHealthCheckDriver() != null)
					&& !sepExt.getDme2JDBCHealthCheckDriver().equals(exisSep.getDme2JDBCHealthCheckDriver())) {
				sep.setDme2JDBCHealthCheckDriver(sepExt.getDme2JDBCHealthCheckDriver());
			} else {
				sep.setDme2JDBCHealthCheckDriver(exisSep.getDme2JDBCHealthCheckDriver());
			}

			if (exisSep.getCreatedBy() != null) {
				sep.setCreatedBy(exisSep.getCreatedBy());
			}

			if (exisSep.getCreatedTimestamp() != null) {
				sep.setCreatedTimestamp(exisSep.getCreatedTimestamp());
			}

			if (sepExt.getUpdatedBy() != null) {
				sep.setUpdatedBy(sepExt.getUpdatedBy());
			} else {
				sep.setUpdatedBy("edge");
			}
			if (sepExt.getUpdatedTimestamp() != null) {
				sep.setUpdatedTimestamp(sepExt.getUpdatedTimestamp());
			} else {
				sep.setUpdatedTimestamp(new Date());
			}
			logger.trace("updateServiceEndPoint sep : {}", sep);

			edgeDao.updateServiceEndPoint(sepExt);
			logger.trace("DbManager.updateServiceEndPoint; elapsedTimeMS=", System.currentTimeMillis() - startTime);
		} catch (Exception e) {
			logger.error("Error while finding serviceendpoint", e);
			throw new EdgeException(GRMEdgeConstants.INTERNAL_ERROR_CASSANDRA_EXECUTING_QUERY,
					GRMEdgeConstants.INTERNAL_ERROR_CASSANDRA_EXECUTING_QUERY_MSG, "DbManager.UpdateServiceEndPoint",
					e);
		}
	}

	public void addServiceDefinition(ServiceDefinition sdExt) {
		try {
			long startTime = System.currentTimeMillis();
			InputDataValidator.validateResourceName(sdExt.getServiceDefinitionName());
			logger.trace("addServiceDefinition sdExt : {}", sdExt);
			edgeDao.addServiceDefinition(sdExt);
			logger.trace("ServiceDefinitionDAO.insert; elapsedTimeMS={}", System.currentTimeMillis() - startTime);
		} catch (Exception e) {
			logger.error("Error while finding servicedefinition", e);
			throw new EdgeException(GRMEdgeConstants.INTERNAL_ERROR_CASSANDRA_EXECUTING_QUERY,
					GRMEdgeConstants.INTERNAL_ERROR_CASSANDRA_EXECUTING_QUERY_MSG, "DbManager.addServiceDefinition", e);
		}
	}

	/**
	 * 
	 * @param serviceDefinitionName
	 * @param environment
	 * @return List of serviceDefinitions instance using input parameters and
	 *         returns as List<ServiceDefinitionsExt> object
	 */
	public Collection<ServiceDefinition> findServiceDefinitionList(String serviceDefinitionId) {
		Collection<ServiceDefinition> sdList = null;

		try {
			long startTime = System.currentTimeMillis();

			String query = "select * from servicedefinition";
			Map<String, String> params = new HashMap<>();
			logger.trace("findServiceDefinitionList serviceDefinitionId : {}", serviceDefinitionId);
			params.put("servicedefinitionid", serviceDefinitionId);
			sdList = edgeDao.findServiceDefinitionList(serviceDefinitionId);
			logger.trace("ServiceDefinitionDAO.find; elapsedTimeMS={}; returnCount={}",
					System.currentTimeMillis() - startTime, sdList.size());
		} catch (Exception e) {
			logger.error("Error while finding servicedefinition", e);
			throw new EdgeException(GRMEdgeConstants.INTERNAL_ERROR_CASSANDRA_EXECUTING_QUERY,
					GRMEdgeConstants.INTERNAL_ERROR_CASSANDRA_EXECUTING_QUERY_MSG,
					"DbManager.findServiceDefinitionList", e);
		}
		return sdList;
	}

	public void addServiceVersionDefinition(ServiceVersionDefinition svdExt) {
		try {
			long startTime = System.currentTimeMillis();
			logger.trace("addServiceVersionDefinition svdExt : {}", svdExt);
			edgeDao.addServiceVersionDefinition(svdExt);
			logger.trace("ServiceVersionDefinitionDAO.insert; elapsedTimeMS={}",
					System.currentTimeMillis() - startTime);
		} catch (Exception e) {
			logger.error("Error while adding service version definition", e);
			throw new EdgeException(GRMEdgeConstants.INTERNAL_ERROR_CASSANDRA_EXECUTING_QUERY,
					GRMEdgeConstants.INTERNAL_ERROR_CASSANDRA_EXECUTING_QUERY_MSG,
					"DbManager.addServiceVersionDefinition", e);
		}
	}

	public Collection<ServiceVersionDefinition> findSVDListById(String svdId) {
		Collection<ServiceVersionDefinition> svdList = null;

		try {
			long startTime = System.currentTimeMillis();
			String query = "select * from serviceversiondefinition";
			Map<String, String> params = new HashMap<String, String>();
			params.put("serviceVersionDefinitionId", svdId);
			svdList = edgeDao.findSVDListById(svdId);
			logger.trace("ServiceVersionDefinitionDAO.findSVDListBySD; elapsedTimeMS={}; returnCount={}",
					System.currentTimeMillis() - startTime, svdList.size());
			return svdList;
		} catch (Exception e) {
			logger.error("Error while finding serviceversiondefinition", e);
			throw new EdgeException(GRMEdgeConstants.INTERNAL_ERROR_CASSANDRA_EXECUTING_QUERY,
					GRMEdgeConstants.INTERNAL_ERROR_CASSANDRA_EXECUTING_QUERY_MSG, "findSVDListBySD", e);
		}
	}

	public void deleteServiceEndPoint(ServiceEndPoint sepExt) {
		try {
			long startTime = System.currentTimeMillis();
			Map<String, String> params = new HashMap<String, String>();
			params.put("serviceendpointid", sepExt.getServiceEndPointId());

			if (!logicalDelete) {
				logger.trace("sepExt : {}", sepExt);
				edgeDao.deleteServiceEndPoint(sepExt);
			} else {
				sepExt.setDeletedtime(startTime);
				edgeDao.updateServiceEndPoint(sepExt);
			}
			logger.trace("ServiceEndPointDAO.insert; elapsedTimeMS={}", System.currentTimeMillis() - startTime);
		} catch (Exception e) {
			logger.error("Error while deleting Service Endpoint", e);
			throw new EdgeException(GRMEdgeConstants.INTERNAL_ERROR_CASSANDRA_EXECUTING_QUERY,
					GRMEdgeConstants.INTERNAL_ERROR_CASSANDRA_EXECUTING_QUERY_MSG, "DbManager.deleteServiceEndPoint",
					e);
		}
	}

}
