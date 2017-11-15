/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.ocnp.mgmt.grm_edge_service.topology.domain;

import java.io.Serializable;
import java.util.List;
import java.util.Date;
//import javax.xml.datatype.Date;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.Transient;
import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKey;
import org.springframework.data.cassandra.mapping.Table;


@Table("serviceversiondefinition")
public class ServiceVersionDefinition  implements Serializable {
	@Transient
	public static final String VERSION_SEP = ".";
	
    private final static long serialVersionUID = 1L;
    
    @PrimaryKey
	private String serviceVersionDefinitionId;
	
    @Column( "serviceversiondefinitionname")
	private String serviceVersionDefinitionName;
	
    @Column("minorVersion")
	private int minorVersion;
    @Column( "majorVersion")
	private int majorVersion;
    @Column("ver")
	private String ver;
	
    @Column("metadata")
	private String metadata;
    @Column("reregistrationinterval")
	private int reRegistrationInterval;
    @Column("partner")
	private String partner;    
    @Column("patchversion")
    protected String patchVersion;
    @Column("lrm")
	private String lrm;
    @Column("containerversiondefinitionname")
	private String containerVersionDefinitionName;
    @Column("servicenamespaceid")
	private String serviceNamespaceId;
    @Column("servicedefinitionname")
	private String serviceDefinitionName;
    @Column("environment")
	private String environment;
    @Column("properties")
	private List<String> properties;
     
    @Column("createdby")
    String createdBy;
    @Column("createdtimestamp")
    protected Date createdTimestamp;
    @Column("updatedby")
    String updatedBy;
    @Column("updatedtimestamp")
    protected Date updatedTimestamp ;    
    @Column("deletetime")
    protected long deletetime;
    
	public String getServiceVersionDefinitionId() {
		return serviceVersionDefinitionId;
	}
	public void setServiceVersionDefinitionId(String serviceVersionDefinitionId) {
		this.serviceVersionDefinitionId = serviceVersionDefinitionId;
	}
	public String getServiceVersionDefinitionName() {
		return serviceVersionDefinitionName;
	}
	public void setServiceVersionDefinitionName(String serviceVersionDefinitionName) {
		this.serviceVersionDefinitionName = serviceVersionDefinitionName;
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
	public String getVer() {
		return ver;
	}
	public void setVer(String ver) {
		this.ver = ver;
	}
	public String getMetadata() {
		return metadata;
	}
	public void setMetadata(String metadata) {
		this.metadata = metadata;
	}
	public int getReRegistrationInterval() {
		return reRegistrationInterval;
	}
	public void setReRegistrationInterval(int reRegistrationInterval) {
		this.reRegistrationInterval = reRegistrationInterval;
	}
	public String getPartner() {
		return partner;
	}
	public void setPartner(String partner) {
		this.partner = partner;
	}
	public String getPatchVersion() {
		return patchVersion;
	}
	public void setPatchVersion(String patchVersion) {
		this.patchVersion = patchVersion;
	}
	public String getLrm() {
		return lrm;
	}
	public void setLrm(String lrm) {
		this.lrm = lrm;
	}
	public String getContainerVersionDefinitionName() {
		return containerVersionDefinitionName;
	}
	public void setContainerVersionDefinitionName(String containerVersionDefinitionName) {
		this.containerVersionDefinitionName = containerVersionDefinitionName;
	}
	public String getServiceNamespaceId() {
		return serviceNamespaceId;
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
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	public Date getCreatedTimestamp() {
		return createdTimestamp;
	}
	public void setCreatedTimestamp(Date createdTimestamp) {
		this.createdTimestamp = createdTimestamp;
	}
	public String getUpdatedBy() {
		return updatedBy;
	}
	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}
	public Date getUpdatedTimestamp() {
		return updatedTimestamp;
	}
	public void setUpdatedTimestamp(Date updatedTimestamp) {
		this.updatedTimestamp = updatedTimestamp;
	}
	public long getDeletedtime() {
		return deletetime;
	}
	public void setDeletetime(long deletedtime) {
		this.deletetime = deletedtime;
	}
    
	public String getVersion() {
		String version = majorVersion + VERSION_SEP + minorVersion;
		if (!StringUtils.isEmpty(patchVersion)) {
			version = version + VERSION_SEP + patchVersion;
		}
		return version;
	}
}
