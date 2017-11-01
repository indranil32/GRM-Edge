/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.ocnp.mgmt.grm_edge_service.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.att.ocnp.mgmt.grm_edge_service.EdgeException;
import com.att.ocnp.mgmt.grm_edge_service.topology.data.SupportedVersionRange;
import com.att.scld.grm.types.v1.ContainerInstance;
import com.att.scld.grm.types.v1.LRM;
import com.att.scld.grm.types.v1.ServiceEndPoint;
import com.att.scld.grm.types.v1.VersionDefinition;

public class InputDataValidator {
    private static final Logger logger = LoggerFactory.getLogger(InputDataValidator.class);

    public static void validateResourceName(String resourceName){
        final Pattern RESOURCE_NAME_PATTERN = Pattern.compile("[A-Za-z0-9\\\\.\\.(){}\\-\\_\\/]+");
        if(!RESOURCE_NAME_PATTERN.matcher(resourceName).matches()){
            List<String> invalidCharsList = new ArrayList<>();

            for(char c : resourceName.toCharArray()){
                if(!RESOURCE_NAME_PATTERN.matcher(Character.toString(c)).matches()){
                    invalidCharsList.add(Character.toString(c));
                }
            }
            throw new EdgeException(GRMEdgeConstants.INPUT_DATA_ILLEGAL_CHARS, GRMEdgeConstants.INPUT_DATA_ILLEGAL_CHARS_MSG, "validateResourceName", resourceName, invalidCharsList.toString());
        }
    }
   
    public static void validateSEPReqFields(ServiceEndPoint sep) {
        validateConfiguredSEPReqFields(sep);
        validateReqFields(sep.getListenPort(), GRMEdgeConstants.INPUT_SEP_INVALID_DATA, "listenPort");
    }
    
    public static void validateConfiguredSEPReqFields(ServiceEndPoint sep) {
        if (sep == null) {
            throw new EdgeException(GRMEdgeConstants.INPUT_REQUEST_INVALID_DATA, GRMEdgeConstants.INPUT_REQUEST_INVALID_DATA_MSG, "validateConfiguredSEPReqFields", "ServiceEndPoint");
        }
        validateReqFields(sep.getName(), GRMEdgeConstants.INPUT_SEP_INVALID_DATA, "name");
        validateReqFields(sep.getVersion(), GRMEdgeConstants.INPUT_SEP_INVALID_DATA, "version");
        validateReqFields(sep.getHostAddress(), GRMEdgeConstants.INPUT_SEP_INVALID_DATA, "hostAddress");
    }
    
    public static void validateReqFields(String field, String errorKey, String errorMessage1) {
        if (StringUtils.isEmpty(field)) {
            throw new EdgeException(errorKey, "validateReqFields", errorMessage1);
        }
    }
    
    public static void validateCIReqFields(ContainerInstance ci) {
        validateConfiguredCIReqFields(ci);
        validateReqFields(ci.getProcessId(), GRMEdgeConstants.INPUT_CI_INVALID_DATA, "processId");
     }
    
    public static void validateLRMReqFields(LRM lrm) {
        if (lrm == null) {
            throw new EdgeException(GRMEdgeConstants.INPUT_REQUEST_INVALID_DATA, GRMEdgeConstants.INPUT_REQUEST_INVALID_DATA_MSG, "validateLRMReqFields", "LRM");
        }
        validateReqFields(lrm.getHostAddress(), GRMEdgeConstants.INPUT_LRM_INVALID_DATA, "hostAddress");
    }
    
    public static void validateConfiguredCIReqFields(ContainerInstance ci) {
        if (ci == null) {
            throw new EdgeException(GRMEdgeConstants.INPUT_REQUEST_INVALID_DATA, GRMEdgeConstants.INPUT_REQUEST_INVALID_DATA_MSG, "validateConfiguredCIReqFields", "ContainerInstance");
        }
        validateReqFields(ci.getName(), GRMEdgeConstants.INPUT_CI_INVALID_DATA, "name");
        validateReqFields(ci.getVersion(), GRMEdgeConstants.INPUT_CI_INVALID_DATA, "version");
        validateReqFields(ci.getHostAddress(), GRMEdgeConstants.INPUT_CI_INVALID_DATA, "hostAddress");
    }
    
    public static void validateSupportedVersions(String clientSupportedVersions, VersionDefinition verDef)
    {
        // make sure we can parse version range
        final SupportedVersionRange range;
        try { 
        	range = new SupportedVersionRange(clientSupportedVersions);
        }
        catch(RuntimeException e)
        {
            throw new EdgeException(GRMEdgeConstants.INPUT_DATA_INVALID_SUPPORTED_VERSIONS, GRMEdgeConstants.INPUT_DATA_INVALID_SUPPORTED_VERSIONS_MSG, "validateSupportedVersions", "clientSupportedVersions", clientSupportedVersions, "minVersion,maxVersion.");
        }

        // make sure service version is in the range
        if((verDef!=null) && ! range.isSupported(verDef))
        {
            throw new EdgeException(GRMEdgeConstants.INPUT_DATA_INVALID_SUPPORTED_VERSIONS,  GRMEdgeConstants.INPUT_DATA_INVALID_SUPPORTED_VERSIONS_MSG, "validateSupportedVersions", "clientSupportedVersions", clientSupportedVersions, "SEP Version not in range: " + verDef);
        }
    }
    
    private static void validateReqFields(VersionDefinition version, String errorKey, String errorMessage1) {
        if (!isVersionValid(version)) {
            throw new EdgeException(errorKey, null, "validateReqFields", errorMessage1);
        }
    }

    public static boolean isVersionValid(VersionDefinition version) {
        if ((version != null) && (version.getMajor() >= 0) && (version.getMinor() >= 0)) {
            return true;
        } else {
            return false;
        }

    }
    
    public static boolean isVersionValid(com.att.ocnp.mgmt.grm_edge_service.topology.domain.ServiceEndPoint sep) {
        if ((sep.getMajorVersion() >= 0) && (sep.getMinorVersion() >= 0)) {
            return true;
        } else {
            return false;
        }

    }

    public static void validateSEPFields(com.att.ocnp.mgmt.grm_edge_service.topology.domain.ServiceEndPoint sep) {
    	logger.trace("started with parameter: {}" , sep);
        validateReqFields(sep.getVersion(), GRMEdgeConstants.INPUT_SEP_INVALID_DATA, "version");
        if(!isVersionValid(sep)){
            throw new EdgeException(GRMEdgeConstants.INPUT_SEP_INVALID_DATA, "InputDataValidator.validateSEPFields", "version");
        }
        validateReqFields(sep.getPatchVersion(), GRMEdgeConstants.INPUT_SEP_INVALID_DATA, "patchversion");
        validateReqFields(sep.getHostAddress(), GRMEdgeConstants.INPUT_SEP_INVALID_DATA, "hostAddress");
        validateReqFields(sep.getListenPort(), GRMEdgeConstants.INPUT_SEP_INVALID_DATA, "listenPort");
        validateReqFields(sep.getProtocol(), GRMEdgeConstants.INPUT_SEP_INVALID_DATA, "protocol");
        validateReqFields(sep.getLatitude(), GRMEdgeConstants.INPUT_SEP_INVALID_DATA, "latitude");
        validateReqFields(sep.getLongitude(), GRMEdgeConstants.INPUT_SEP_INVALID_DATA, "longitude");
        validateReqFields(sep.getRouteOffer(), GRMEdgeConstants.INPUT_SEP_INVALID_DATA, "routeoffer");
        validateReqFields(sep.getName(), GRMEdgeConstants.INPUT_SEP_INVALID_DATA, "name");

        String[] requiredValidationProtocolAndFields = null;

        if (Configuration.getBootStrapConfig().getStringValue(GRMEdgeConstants.SEP_REQUIRED_VALIDATION, GRMEdgeConstants.SEP_REQUIRED_VALIDATION_DEFAULT) != null) {
            requiredValidationProtocolAndFields = Configuration.getBootStrapConfig().getStringValue(GRMEdgeConstants.SEP_REQUIRED_VALIDATION, GRMEdgeConstants.SEP_REQUIRED_VALIDATION_DEFAULT).split("\\;");
        }

        if (requiredValidationProtocolAndFields != null && requiredValidationProtocolAndFields.length != 0) {
            for (String protocolToValidate : requiredValidationProtocolAndFields) {
                String[] fields = protocolToValidate.split("\\,");
                if (sep.getProtocol().equalsIgnoreCase(fields[0]) && (fields.length > 1)) {
                    for (String field : fields) {
                        if (!field.equalsIgnoreCase(sep.getProtocol())) {
                            validateSEPByProtocol(sep, field, GRMEdgeConstants.INPUT_SEP_INVALID_DATA);
                        }
                    }

                    break;
                }

            }
        }
    }
    
    public static void validateSEPByProtocol(com.att.ocnp.mgmt.grm_edge_service.topology.domain.ServiceEndPoint sep, String field, String errorKey) {
        if ((sep != null) && (field != null) && (errorKey != null)) {
            if (field.equalsIgnoreCase("ClientSupportedVersions")) {
                validateReqFields(sep.getClientSupportedVersions(), GRMEdgeConstants.INPUT_SEP_INVALID_DATA, field.toLowerCase());
                return;
            }
            if (field.equalsIgnoreCase("ContextPath")) {
                validateReqFields(sep.getContextPath(), GRMEdgeConstants.INPUT_SEP_INVALID_DATA, field.toLowerCase());
                return;
            }
            if (field.equalsIgnoreCase("DME2JDBCDatabaseName")) {
                validateReqFields(sep.getDme2JDBCDatabaseName(), GRMEdgeConstants.INPUT_SEP_INVALID_DATA, field.toLowerCase());
                return;
            }
            if (field.equalsIgnoreCase("DME2JDBCHealthCheckDriver")) {
                validateReqFields(sep.getDme2JDBCHealthCheckDriver(), GRMEdgeConstants.INPUT_SEP_INVALID_DATA, field.toLowerCase());
                return;
            }
            if (field.equalsIgnoreCase("DME2JDBCHealthCheckPassword")) {
                validateReqFields(sep.getDme2JDBCHealthCheckPassword(), GRMEdgeConstants.INPUT_SEP_INVALID_DATA, field.toLowerCase());
                return;
            }
            if (field.equalsIgnoreCase("DME2JDBCHealthCheckUser")) {
                validateReqFields(sep.getDme2JDBCHealthCheckUser(), GRMEdgeConstants.INPUT_SEP_INVALID_DATA, field.toLowerCase());
                return;
            }
            if (field.equalsIgnoreCase("DME2Version")) {
                validateReqFields(sep.getDme2Version(), GRMEdgeConstants.INPUT_SEP_INVALID_DATA, field.toLowerCase());
                return;
            }

            if (field.startsWith("props.")) {
                for (String nameValuePair : sep.getProperties()) {
                        validateReqFields(nameValuePair, GRMEdgeConstants.INPUT_SEP_INVALID_DATA, field.toLowerCase());
                        return;
                }
                throw new EdgeException(GRMEdgeConstants.INPUT_SEP_INVALID_DATA, GRMEdgeConstants.INPUT_SEP_INVALID_DATA_MSG, "InputDataValidator.validateSEPByProtocol", field.toLowerCase());
            }
        }
    }

}
