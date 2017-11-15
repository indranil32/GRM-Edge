package com.att.ocnp.mgmt.grm_edge_service;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.cassandra.config.CassandraClusterFactoryBean;
import org.springframework.data.cassandra.config.CassandraSessionFactoryBean;
import org.springframework.data.cassandra.config.SchemaAction;
import org.springframework.data.cassandra.convert.CassandraConverter;
import org.springframework.data.cassandra.convert.MappingCassandraConverter;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.mapping.BasicCassandraMappingContext;
import org.springframework.data.cassandra.mapping.CassandraMappingContext;

import com.att.ocnp.mgmt.grm_edge_service.topology.data.DbManager;
import com.att.ocnp.mgmt.grm_edge_service.topology.data.EdgeDao;

@Configuration
public class CassandraConfiguration {

	private static final String KEYSPACE = "cassandra.keyspace";
	private static final String CONTACTPOINTS = "cassandra.contactpoints";
	private static final String PORT = "cassandra.port";
	private static final String USERNAME = "cassandra.username";
	private static final String PASSWORD = "cassandra.password";

	private @Autowired AutowireCapableBeanFactory beanFactory;

	@Autowired
	private Environment environment;

	@Bean
	public CassandraClusterFactoryBean cluster() {
		CassandraClusterFactoryBean cluster = new CassandraClusterFactoryBean();
/*		cluster.setContactPoints((System.getProperty(CONTACTPOINTS) != null) ? System.getProperty(CONTACTPOINTS)
				: environment.getProperty(CONTACTPOINTS));
		cluster.setPort(Integer.parseInt(
				(System.getProperty(PORT) != null) ? System.getProperty(PORT) : environment.getProperty(PORT)));
		cluster.setUsername((System.getProperty(USERNAME) != null) ? System.getProperty(USERNAME)
				: environment.getProperty(USERNAME));
		cluster.setPassword((System.getProperty(PASSWORD) != null) ? System.getProperty(PASSWORD)
				: environment.getProperty(PASSWORD));*/
		
		cluster.setContactPoints("your_host_name"); //cassandra host_name
		cluster.setPort(9042); //default port for cassandra 
		cluster.setUsername("xyz"); //your cluster uname
		cluster.setPassword("xyz"); //your cluster password
		return cluster;
	}

	@Bean
	public CassandraMappingContext mappingContext() {
		return new BasicCassandraMappingContext();
	}

	@Bean
	public CassandraConverter converter() {
		return new MappingCassandraConverter(mappingContext());
	}

	@Bean
	public CassandraSessionFactoryBean session() throws Exception {
		CassandraSessionFactoryBean cassandraSessionFactoryBean = new CassandraSessionFactoryBean();
		cassandraSessionFactoryBean.setCluster(cluster().getObject());
		/*cassandraSessionFactoryBean.setKeyspaceName((System.getProperty(KEYSPACE) != null)
				? System.getProperty(KEYSPACE) : environment.getProperty(KEYSPACE));*/
		cassandraSessionFactoryBean.setConverter(converter());
		cassandraSessionFactoryBean.setSchemaAction(SchemaAction.NONE);
		cassandraSessionFactoryBean.setKeyspaceName("topology");
		return cassandraSessionFactoryBean;
	}

	@Bean
	public CassandraOperations cassandraTemplate() throws Exception {
		return new CassandraTemplate(session().getObject());
	}

	@PostConstruct
	public void bundDBManager() {
		DbManager dbManager = DbManager.getInstance();
		beanFactory.autowireBean(dbManager);
	}

}
