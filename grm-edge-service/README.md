Overview:
     GRMEdge is a Kubernetes pod and service that runs under the com-att-ocnp-mgmt namespace. GRMEdge listens to the cluster's Kubernetes API server and creates a ServiceEndPoint cache from it. If there is connectivity to GRM, GRMEdge posts and returns endpoints to and from GRM. Otherwise, it will serve only from its cache. Applications can query GRMEdge for service endpoints by sending a FindRunningServiceEndPointRequest to <grm-edge-ip>:<grm-edge-port>/GRMLWPService/v1/serviceEndPoint/findRunning. 
REQUIREMENTS:
     Environment:
•	Linux
    Software:
•	Maven 3.x.x
•	Java 8
•	Cassandra
    ¬Other:
•	Kubernetes running on instance ( e.g., AWS ).
•	Kubernetes namespace
•	Docker 
   Build:
1.	Install and configure Java and Maven on your local machine. Many robust installation guides exist for both products and there is not a need for an additional guide here.
2.	Clone GRM-Edge repository.
3.	Install and configure CASSANDRA in your local. Run script service_topology_schema.cql.
4.	Now build application with simple maven command : mvn clean install
5.	Navigate to CassandraConfiguration, specify your credentials and test the connectivity in your local machine.
6.	Check for “build success” message.
Running Application:
1.	GRM runs on the kubernetes pods. NOTE: GRM may not run as the other spring-boot applications do. 
2.	Create virtual environment, say for AWS, create KUBERNETES cluster in AWS. Look for https://kubernetes.io/docs/getting-started-guides/ for preferable environment. 
3.	Create a namespace(com-att-ocnp-mgmt) in kubernetes cluster.
4.	Create a new CASSANDRA instance with same procedure.
5.	Build a DOCKER image for your application using the command : mvn clean package docker:build 
6.	Specify the image information in grm-edge-service-rc.yml file. 
7.	Deploy grmedge.yml in Kubernetes. 
8.	The PODS should be up and running. 
   More Information:
   Check wiki page for more information.
