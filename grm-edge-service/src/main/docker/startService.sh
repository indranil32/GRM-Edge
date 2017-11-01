#*******************************************************************************
# Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
#*******************************************************************************
cp /grm-edge-cass-override-config/persistence-client.properties /grm-edge-cass-persistence-config/persistence-client.properties
java -Xms1024m -Xmx1024m -jar /app.jar -DGRID_CONFIG_FILE=/grm-edge-cass-persistence-config -DGRID_CONFIG_FILE_OVERRIDE=persistence-client.properties
