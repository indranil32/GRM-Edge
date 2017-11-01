/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.ocnp.mgmt.grm_edge_service.topology.domain;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.annotation.Transient;
import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKey;
import org.springframework.data.cassandra.mapping.Table;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang3.StringUtils;

import com.att.ocnp.mgmt.grm_edge_service.util.GRMEdgeUtil;
import com.att.scld.grm.types.v1.Status;


@Table("serviceendpoint")
public class ServiceEndPoint implements Serializable {
	@Transient
	public static final String VERSION_SEP = ".";

    private final static long serialVersionUID = 1L;
    @PrimaryKey
    protected String serviceEndPointId;
    
    @Column("serviceendpointname")
    protected String serviceEndPointName;
    
//    protected String name;
    
	@Column("minorVersion" )
    protected int minorVersion;
 
    @Column("majorVersion" )
    protected int majorVersion;

    @Column("hostaddress")
    protected String hostAddress;
    
    @Column("listenPort")
    protected String listenPort;
    
    @Column("latitude")
    protected String latitude;
    
    @Column("longitude")
    protected String longitude;
    
    @Column("registrationtime")
    protected XMLGregorianCalendar registrationTime;
    
    @Column("expirationtime")
    protected XMLGregorianCalendar expirationTime;
    
    @Column("clientsupportedversions")
    protected String clientSupportedVersions;
    
    @Column("contextpath")
    protected String contextPath;
    
    @Column("dme2version")
    protected String dme2Version;

    @Column("dme2jdbcdatabasename")
    protected String dme2JDBCDatabaseName;
    
    @Column("dme2jdbchealthcheckuser")
    protected String dme2JDBCHealthCheckUser;
   
    @Column("dme2jdbchealthcheckpassword")
    protected String dme2JDBCHealthCheckPassword;
  
    @Column("dme2jdbchealthcheckdriver")
    protected String dme2JDBCHealthCheckDriver;
    
    @Column("protocol")
    protected String protocol;
    
    @Column("status")
    protected Status status;
/*    @Column("statusreasoncode ")
    protected String statusReasonCode;
    @Column("statusreasondescription ")
    protected String statusReasonDescription;
*/    @Column("statuschecktime ")
    protected XMLGregorianCalendar statusCheckTime;

    @Column("eventcheckstatus")
    protected Status eventCheckStatus;
    @Column("eventcheckstatusCode")
    protected String eventCheckStatusCode;
    @Column("eventcheckmessage")
    protected String eventCheckMessage;
    @Column("eventcheckTime")
    protected XMLGregorianCalendar eventCheckTime ;
    
    @Column("patchversion")    
    protected String patchVersion;
    @Column("metadata")    
    protected String metaData;
    @Column("routeoffer")    
    protected String routeOffer;
    @Column("lrm")    
    protected String lrm;
    
    @Column("containerversionName")  
    protected String containerVersionName;
    @Column("containerinstanceName")  
    protected String containerInstanceName;

    
    @Column("servicenamespaceId")
    private String serviceNamespaceId;
    @Column("servicedefinitionName")
    private String serviceDefinitionName;
    @Column("serviceversiondefinitionname")
    private String serviceVersionDefinitionName;
    @Column("environment")
    private String environment;
    
    @Column("properties")
    protected List<String> properties;

    @Column
    private String ep;

    @Column("createdby")
    String createdBy;
    @Column("createdtimestamp")
    protected XMLGregorianCalendar createdTimestamp;
    @Column("updatedby")
    String updatedBy;
    @Column("updatedtimestamp ")
    protected XMLGregorianCalendar updatedTimestamp ;

    
    @Column("deletetime")
    protected Long deletedtime;

	public void setServiceEndPointId(String serviceEndPointId) {
		this.serviceEndPointId = serviceEndPointId;
	}


	public String getName() {
		return getServiceDefinitionName();
	}


	public void setName(String name) {
		setServiceDefinitionName(name);
	}

    public String getServiceEndPointName() {
		return serviceEndPointName;
	}


	public void setServiceEndPointName(String serviceEndPointName) {
		this.serviceEndPointName = serviceEndPointName;
	}
	
	public int getMinorVersion() {
		return minorVersion;
	}


	public void setMinorVersion(int minorVersion) {
		this.minorVersion = minorVersion;
	}


	public int getMajorVersion() {
		return majorVersion;
	}


	public void setMajorVersion(int majorVersion) {
		this.majorVersion = majorVersion;
	}


	public String getHostAddress() {
		return hostAddress;
	}


	public void setHostAddress(String hostAddress) {
		this.hostAddress = hostAddress;
	}


	public String getListenPort() {
		return listenPort;
	}


	public void setListenPort(String listenPort) {
		this.listenPort = listenPort;
	}


	public String getLatitude() {
		return latitude;
	}


	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}


	public String getLongitude() {
		return longitude;
	}


	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}


	public XMLGregorianCalendar getRegistrationTime() {
		return registrationTime;
	}


	public void setRegistrationTime(XMLGregorianCalendar registrationTime) {
		this.registrationTime = registrationTime;
	}


	public XMLGregorianCalendar getExpirationTime() {
		return expirationTime;
	}


	public void setExpirationTime(XMLGregorianCalendar expirationTime) {
		this.expirationTime = expirationTime;
	}


	public String getClientSupportedVersions() {
		return clientSupportedVersions;
	}


	public void setClientSupportedVersions(String clientSupportedVersions) {
		this.clientSupportedVersions = clientSupportedVersions;
	}


	public String getContextPath() {
		return contextPath;
	}


	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}


	public String getDme2Version() {
		return dme2Version;
	}


	public void setDme2Version(String dme2Version) {
		this.dme2Version = dme2Version;
	}


	public String getProtocol() {
		return protocol;
	}


	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}


	public Status getStatus() {
		return status;
	}


	public void setStatus(Status status) {
		this.status = status;
	}


	public Status getEventCheckStatus() {
		return eventCheckStatus;
	}


	public void setEventCheckStatus(Status eventCheckStatus) {
		this.eventCheckStatus = eventCheckStatus;
	}


	public String getEventCheckStatusCode() {
		return eventCheckStatusCode;
	}


	public void setEventCheckStatusCode(String eventCheckStatusCode) {
		this.eventCheckStatusCode = eventCheckStatusCode;
	}


	public String getEventCheckMessage() {
		return eventCheckMessage;
	}


	public void setEventCheckMessage(String eventCheckMessage) {
		this.eventCheckMessage = eventCheckMessage;
	}


	public XMLGregorianCalendar getEventCheckTime() {
		return eventCheckTime;
	}


	public void setEventcheckTime(XMLGregorianCalendar eventCheckTime) {
		this.eventCheckTime = eventCheckTime;
	}


	public String getPatchVersion() {
		return patchVersion;
	}


	public void setPatchVersion(String patchVersion) {
		this.patchVersion = patchVersion;
	}


	public String getMetaData() {
		return metaData;
	}


	public void setMetaData(String metaData) {
		this.metaData = metaData;
	}


	public String getRouteOffer() {
		return routeOffer;
	}


	public void setRouteOffer(String routeOffer) {
		this.routeOffer = routeOffer;
	}


	public String getLrm() {
		return lrm;
	}


	public void setLrm(String lrm) {
		this.lrm = lrm;
	}


	public String getContainerVersionName() {
		return containerVersionName;
	}


	public void setContainerVersionName(String containerVersionName) {
		this.containerVersionName = containerVersionName;
	}


	public String getContainerInstanceName() {
		return containerInstanceName;
	}


	public void setContainerInstanceName(String containerInstanceName) {
		this.containerInstanceName = containerInstanceName;
	}


    public String getServiceNamespaceId() {
       return this.serviceNamespaceId;
    }

    public void setServiceNamespaceId(String serviceNamespaceId) {
        this.serviceNamespaceId = serviceNamespaceId;
    }


	public String getServiceDefinitionName() {
		return serviceDefinitionName;
	}


	public void setServiceDefinitionName(String serviceDefinitionName) {
		this.serviceDefinitionName = serviceDefinitionName;
	}


	public String getServiceVersionDefinitionName() {
		return serviceVersionDefinitionName;
	}


	public void setServiceVersionDefinitionName(String serviceVersionDefinitionName) {
		this.serviceVersionDefinitionName = serviceVersionDefinitionName;
	}


	public String getEnvironment() {
		return environment;
	}


	public void setEnvironment(String environment) {
		this.environment = environment;
	}


	public List<String> getProperties() {
		return properties;
	}


	public void setProperties(List<String> properties) {
		this.properties = properties;
	}


	public String getEp() {
		return this.ep;
	}


	public void setEp(String ep) {
		this.ep = ep;
	}


	public String getCreatedBy() {
		return createdBy;
	}


	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}


	public XMLGregorianCalendar getCreatedTimestamp() {
		return createdTimestamp;
	}


	public void setCreatedTimestamp(XMLGregorianCalendar createdTimestamp) {
		this.createdTimestamp = createdTimestamp;
	}


	public String getUpdatedBy() {
		return updatedBy;
	}


	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}


	public XMLGregorianCalendar getUpdatedTimestamp() {
		return updatedTimestamp;
	}


	public void setUpdatedTimestamp(XMLGregorianCalendar updatedTimestamp) {
		this.updatedTimestamp = updatedTimestamp;
	}


	public Long getDeletedtime() {
		return deletedtime;
	}


	public void setDeletedtime(Long deletedtime) {
		this.deletedtime = deletedtime;
	} 
    
	
/*	public String getStatusReasonCode() {
		return statusReasonCode;
	}


	public void setStatusReasonCode(String statusReasonCode) {
		this.statusReasonCode = statusReasonCode;
	}


	public String getStatusReasonDescription() {
		return statusReasonDescription;
	}


	public void setStatusReasonDescription(String statusReasonDescription) {
		this.statusReasonDescription = statusReasonDescription;
	}

*/
	public XMLGregorianCalendar getStatusCheckTime() {
		return statusCheckTime;
	}


	public void setStatusCheckTime(XMLGregorianCalendar statusCheckTime) {
		this.statusCheckTime = statusCheckTime;
	}


	public String getVersion() {
		String version = majorVersion + VERSION_SEP + minorVersion;
		if (!StringUtils.isEmpty(patchVersion)) {
			version = version + VERSION_SEP + patchVersion;
		}
		return version;
	}


	public String getDme2JDBCDatabaseName() {
		return dme2JDBCDatabaseName;
	}


	public void setDme2JDBCDatabaseName(String dme2jdbcDatabaseName) {
		dme2JDBCDatabaseName = dme2jdbcDatabaseName;
	}


	public String getDme2JDBCHealthCheckUser() {
		return dme2JDBCHealthCheckUser;
	}


	public void setDme2JDBCHealthCheckUser(String dme2jdbcHealthCheckUser) {
		dme2JDBCHealthCheckUser = dme2jdbcHealthCheckUser;
	}


	public String getDme2JDBCHealthCheckPassword() {
		return dme2JDBCHealthCheckPassword;
	}


	public void setDme2JDBCHealthCheckPassword(String dme2jdbcHealthCheckPassword) {
		dme2JDBCHealthCheckPassword = dme2jdbcHealthCheckPassword;
	}


	public String getDme2JDBCHealthCheckDriver() {
		return dme2JDBCHealthCheckDriver;
	}


	public void setDme2JDBCHealthCheckDriver(String dme2jdbcHealthCheckDriver) {
		dme2JDBCHealthCheckDriver = dme2jdbcHealthCheckDriver;
	}
	
    public String getUniqueId() {
        return GRMEdgeUtil.getSEPId(getName(), getVersion(), getHostAddress(), getListenPort(), getEnvironment());
    }
    
    public String getSEPName() {
        return GRMEdgeUtil.getSEPName(getName(), getVersion(), getHostAddress(), getListenPort());
    }
    	
    
    public String getServiceEndPointId() {
        return getUniqueId();
    }


	@Override
	public String toString() {
		return "ServiceEndPoint [serviceEndPointId=" + serviceEndPointId + ", name=" + serviceDefinitionName + ", minorVersion="
				+ minorVersion + ", majorVersion=" + majorVersion + ", hostAddress=" + hostAddress + ", listenPort="
				+ listenPort + ", latitude=" + latitude + ", longitude=" + longitude + ", registrationTime="
				+ registrationTime + ", expirationTime=" + expirationTime + ", clientSupportedVersions="
				+ clientSupportedVersions + ", contextPath=" + contextPath + ", dme2Version=" + dme2Version
				+ ", dme2JDBCDatabaseName=" + dme2JDBCDatabaseName + ", dme2JDBCHealthCheckUser="
				+ dme2JDBCHealthCheckUser + ", dme2JDBCHealthCheckPassword=" + dme2JDBCHealthCheckPassword
				+ ", dme2JDBCHealthCheckDriver=" + dme2JDBCHealthCheckDriver + ", protocol=" + protocol + ", status="
				+ status + ", statusCheckTime=" + statusCheckTime + ", eventCheckStatus=" + eventCheckStatus
				+ ", eventCheckStatusCode=" + eventCheckStatusCode + ", eventCheckMessage=" + eventCheckMessage
				+ ", eventCheckTime=" + eventCheckTime + ", patchVersion=" + patchVersion + ", metaData=" + metaData
				+ ", routeOffer=" + routeOffer + ", lrm=" + lrm + ", containerVersionName=" + containerVersionName
				+ ", containerInstanceName=" + containerInstanceName + ", serviceNamespaceId=" + serviceNamespaceId
				+ ", serviceDefinitionName=" + serviceDefinitionName + ", serviceVersionDefinitionName="
				+ serviceVersionDefinitionName + ", environment=" + environment + ", properties=" + properties + ", ep="
				+ ep + ", createdBy=" + createdBy + ", createdTimestamp=" + createdTimestamp + ", updatedBy="
				+ updatedBy + ", updatedTimestamp=" + updatedTimestamp + ", deletedtime=" + deletedtime + "]";
	}


}
