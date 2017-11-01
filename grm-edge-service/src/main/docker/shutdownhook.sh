#*******************************************************************************
# Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
#*******************************************************************************
unset http_proxy
unset https_proxy
curl -X GET https://localhost:8080/GRMLWPService/v1/lifecycle/shutdownhook -k