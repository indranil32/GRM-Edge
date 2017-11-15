/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.ocnp.mgmt.grm_edge_service.topology.domain;

import java.io.Serializable;
import java.util.List;
import java.util.Date;
import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKey;
import org.springframework.data.cassandra.mapping.Table;
//import javax.xml.datatype.Date;



@Table("servicedefinition")
public class ServiceDefinition implements Serializable {
	private static final long serialVersionUID = 1L;

	@PrimaryKey
	private String serviceDefinitionId;
	
//	@Column ("servicedefinitionname ")
	private String serviceDefinitionName;
	
	@Column ("availabilityRules")
	private String availabilityRules;

	@Column ("availabilityRulesLockedByUID")
	private String availabilityRulesLockedByUID;

	@Column ("policyMetaData")
	private String policyMetaData;

	@Column ("policyMetaDataLockedByUID")
	private String policyMetaDataLockedByUID;

	@Column ("routingMetaDataLockedByUID")
	private String routingMetaDataLockedByUID;

	@Column ("partner")
    protected String partner;

	@Column ("metaData")
	private String metaData;

	@Column ("servicenamespaceId")
	private String serviceNamespaceId;

	@Column ("properties")
    protected List<String> properties;
	
	@Column ("environment")
	private String environment;
    
    @Column("createdby")
    String createdBy;
    @Column("createdtimestamp")
    protected Date createdTimestamp;
    @Column("updatedby")
    String updatedBy;
    @Column("updatedtimestamp")
    protected Date updatedTimestamp ;

    
    @Column("deletetime")
    protected long deletedtime;


	public String getServiceDefinitionId() {
		return serviceDefinitionId;
	}


	public void setServiceDefinitionId(String serviceDefinitionId) {
		this.serviceDefinitionId = serviceDefinitionId;
	}


	public String getServiceDefinitionName() {
		return serviceDefinitionName;
	}


	public void setServiceDefinitionName(String serviceDefinitionName) {
		this.serviceDefinitionName = serviceDefinitionName;
	}


	public String getPolicyMetaData() {
		return policyMetaData;
	}


	public void setPolicyMetaData(String policyMetaData) {
		this.policyMetaData = policyMetaData;
	}


	public String getPartner() {
		return partner;
	}


	public void setPartner(String partner) {
		this.partner = partner;
	}


	public String getMetaData() {
		return metaData;
	}


	public void setMetaData(String metaData) {
		this.metaData = metaData;
	}


	public String getServiceNamespaceId() {
		return serviceNamespaceId;
	}


	public void setServiceNamespaceId(String serviceNamespaceId) {
		this.serviceNamespaceId = serviceNamespaceId;
	}


	public List<String> getProperties() {
		return properties;
	}


	public void setProperties(List<String> properties) {
		this.properties = properties;
	}


	public String getEnvironment() {
		return environment;
	}


	public void setEnvironment(String environment) {
		this.environment = environment;
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
		return deletedtime;
	}


	public void setDeletedtime(long deletedtime) {
		this.deletedtime = deletedtime;
	}

    
}
