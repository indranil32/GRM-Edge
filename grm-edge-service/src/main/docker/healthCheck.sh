#!/bin/bash
#*******************************************************************************
# Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
#*******************************************************************************
unset http_proxy
unset https_proxy
result=`curl https://localhost:8080/GRMLWPService/v1/health -k`
if [[ "$result" == '{"status":"UP",'* ]]; then
  exit 0
fi
exit 1