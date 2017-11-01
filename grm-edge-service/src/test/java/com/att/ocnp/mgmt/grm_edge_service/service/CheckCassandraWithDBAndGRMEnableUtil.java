/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.ocnp.mgmt.grm_edge_service.service;


import com.datastax.driver.core.*;
import java.util.*;

public class CheckCassandraWithDBAndGRMEnableUtil {
    static String[] CONTACT_POINTS = {"dummy.com"};

	static Cluster cluster;
	static Session session;

    public static void setUp(){
	  	cluster = Cluster.builder().addContactPoints(CONTACT_POINTS).withClusterName("null").withPort(20004).withCredentials("null", "null").build();
	  	session = cluster.connect("topology");    	
    }
    
    static{
    }
    
    public static void cleanup(){
    	cluster.close();
    	session.close();
    }
    
    public static void main(String[] args) {

			Cluster cluster;
			Session session;

		// Connect to the cluster and keyspace "demo"
			
			//Modified cluster values default set to "root"
		  	cluster = Cluster.builder().addContactPoints(CONTACT_POINTS).withClusterName("null").withPort(20004).withCredentials("root", "root").build();
		  	session = cluster.connect("topology");
            ResultSet rs = session.execute("select release_version from system.local");
            //  Extract the first row (which is the only one in this case).
            Row row = rs.one();

            // Extract the value of the first (and only) column from the row.
            String releaseVersion = row.getString("release_version");
            System.out.printf("Cassandra version is: {}%n", releaseVersion);
			Metadata md = cluster.getMetadata();
			List<KeyspaceMetadata> keyspaces = md.getKeyspaces();
            System.out.printf("Cassandra keyspaces is: {}%n", keyspaces);

            cluster.close();
	}

    public static ResultSet getResult(String query){
    	System.out.println("Executing Testing Query : " + query);
        ResultSet rs = session.execute(query);
        System.out.println("Executing Testing Query  - ResultSet from Test Query " + query + " resultset: " + rs);
        return rs;
    	
    }
}
