package com.att.ocnp.mgmt.grm_edge_service.topology.data;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.stereotype.Repository;

import com.att.ocnp.mgmt.grm_edge_service.topology.domain.ServiceDefinition;
import com.att.ocnp.mgmt.grm_edge_service.topology.domain.ServiceEndPoint;
import com.att.ocnp.mgmt.grm_edge_service.topology.domain.ServiceVersionDefinition;

@Repository
public class EdgeDao {

	@Autowired
	private CassandraOperations cassandraTemplate;

	public void addServiceEndPoint(ServiceEndPoint sepExt) {
		cassandraTemplate.insert(sepExt);
	}

	public void updateServiceEndPoint(ServiceEndPoint sepExt) {
		cassandraTemplate.update(sepExt);
	}

	public void addServiceDefinition(ServiceDefinition sdExt) {
		cassandraTemplate.insert(sdExt);
	}

	public void addServiceVersionDefinition(ServiceVersionDefinition svdExt) {
		cassandraTemplate.insert(svdExt);
	}

	public void deleteServiceEndPoint(ServiceEndPoint sepExt) {
		cassandraTemplate.delete(sepExt);

	}

	public List<ServiceEndPoint> findServiceEndPointById(String sepId) {

		List<ServiceEndPoint> serviceEndPoints = null;
		String query = "select * from serviceendpoint where serviceEndPointId='" + sepId + "'";
		serviceEndPoints = cassandraTemplate.select(query, ServiceEndPoint.class);
		return serviceEndPoints;

	}

	public List<ServiceEndPoint> findServiceEndPointByEnvAndName(String env, String serviceName) {

		List<ServiceEndPoint> serviceEndPoints = null;
		String query = "select * from serviceendpoint where servicedefinitionname ='" + serviceName
				+ "' and environment = '" + env + "' ALLOW FILTERING";
		serviceEndPoints = cassandraTemplate.select(query, ServiceEndPoint.class);
		return serviceEndPoints;

	}

	public Collection<ServiceDefinition> findServiceDefinitionList(String serviceDefinitionId) {
		Collection<ServiceDefinition> serviceDefinitions = null;
		String query = "select * from servicedefinition where serviceDefinitionId='" + serviceDefinitionId + "'";
		serviceDefinitions = cassandraTemplate.select(query, ServiceDefinition.class);
		return serviceDefinitions;

	}

	public Collection<ServiceVersionDefinition> findSVDListById(String svdId) {

		Collection<ServiceVersionDefinition> serviceVersionDefinition = null;
		String query = "select * from serviceversiondefinition where serviceVersionDefinitionId='" + svdId + "'";
		serviceVersionDefinition = cassandraTemplate.select(query, ServiceVersionDefinition.class);
		return serviceVersionDefinition;

	}

}
