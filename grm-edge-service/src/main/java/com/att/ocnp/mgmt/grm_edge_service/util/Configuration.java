/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.ocnp.mgmt.grm_edge_service.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Configuration {

    private static Logger logger = LoggerFactory.getLogger(Configuration.class.getCanonicalName());

    private static Map<String,Configuration> fileNameToConfigMap = new HashMap<String,Configuration>();
   private static final String CONFIG_FILE_DIR_DEFAULT = "/opt/app/aft/edgeproxy/etc";
    public static final String BOOT_STRAP_FILENAME = "edgeconfig.properties";
    public static final String ENDPOINT_CONFIG_FILENAME = "edgeendpoints.properties";
	private static ResourceBundle errorTable =  null;
	private static Configuration bootstrapInstance = null;
	private static Configuration epInstance = null;
	
    //local variables to Configuration instance.
    private Properties props = new Properties();

    private Configuration(String configFileName) {
        try {
            props = loadProps(configFileName);
            logger.debug("Props loaded for file: {}" , configFileName);
            for (Entry<Object, Object> entry : props.entrySet()) {
                logger.debug("... key = " + entry.getKey() + ", value = " + entry.getValue());
            }
        } catch (Exception e) {
            logger.error("Exception while processing action", e);
            logger.error("Ignoring the exception.");
        }
    }

    private static Properties loadProps(String fileName) {
        ClassLoader[] cls = new ClassLoader[] {
            ClassLoader.getSystemClassLoader(),
            Configuration.class.getClassLoader(),
            Thread.currentThread().getContextClassLoader() };

        Properties props = new Properties();
        boolean loaded = false;
        // Check system classpath
        for (ClassLoader cl : cls) {
            InputStream in = cl.getResourceAsStream(fileName);
            logger.trace("Loading from: " + cl.getResource(fileName));
            if (in != null) {
                try {
                    logger.trace("Loading " + cl.toString() + ": " + fileName);
                    props.load(in);
                    loaded = true;
                    in.close();
                    break;
                } catch (IOException e) {
  //                  throw new EdgeException(GRMEdgeConstants.CONFIG_FILE_LOAD_ERROR, e, fileName);
                }
            }
        }

        return props;
    }

    private static synchronized Configuration getInstanceFromMap(String configFileName) {
        return fileNameToConfigMap.get(configFileName);
    }

    static Map<String, Configuration> getFileNameToConfigMap() {
        return fileNameToConfigMap;
    }

    protected static Configuration getInstance(String configFileName) {
        Configuration configInstance = getInstanceFromMap(configFileName);
        //if configInstance does not exist for the fileName, create one and add it to map.
        if (configInstance == null) {
            synchronized(Configuration.class) {
                if (configInstance == null) {
                    configInstance = new Configuration(configFileName);
                    fileNameToConfigMap.put(configFileName, configInstance);
                }
            }
        }
        return configInstance;
    }

    public Properties getProperties() {
        return props;
    }

    public String getProperty(String key) {
        return getProperty(key, null);
    }

    public String getProperty(String key, String defaultValue) {
    	String value = props.getProperty(key);
        if (value == null)
            value = System.getProperty(key, props.getProperty(key, defaultValue));

        if (value == null)
        	value = defaultValue;
        return value;
    }

    public  String getStringValue(String property, String defaultVal) {
        try {
            if (isNullREmpty(getProperty(property))) {
                return defaultVal;
            }
            return getProperty(property);
        } catch (Exception e) {
            logger.error("Exception in Configuration.", e);
            return defaultVal;
        }
    }

    public  boolean getBooleanValue(String property, boolean defaultVal) {
        try {
            if (isNullREmpty(getProperty(property))) {
                return defaultVal;
            }
            return Boolean.parseBoolean(getProperty(property));
        } catch (Exception e) {
            logger.error("Exception in Configuration.", e);
            return defaultVal;
        }
    }

    public  int getIntValue(String property, int defaultVal) {
        try {
            if (isNullREmpty(getProperty(property))) {
                return defaultVal;
            }
            return Integer.parseInt(getProperty(property));
        } catch (Exception e) {
            logger.error("Exception in Configuration.", e);
            return defaultVal;
        }
    }

    public  double getDoubleValue(String property, double defaultVal) {
        try {
            if (isNullREmpty(getProperty(property))) {
                return defaultVal;
            }
            return Double.parseDouble(getProperty(property));
        } catch (Exception e) {
            logger.error("Exception in Configuration.", e);
            return defaultVal;
        }
    }

    public Long getLongValue(String property, long defaultVal) {
        try {
            if (isNullREmpty(getProperty(property))) {
                return defaultVal;
            }
            return Long.parseLong(getProperty(property));
        } catch (Exception e) {
            logger.error("Exception in Configuration.", e);
            return defaultVal;
        }
    }

    public static Configuration getBootStrapConfig() {
    	if (bootstrapInstance == null)
    		bootstrapInstance = Configuration.getInstance(BOOT_STRAP_FILENAME);
    	return bootstrapInstance;
    }

    public static Configuration getEndpointConfig() {
    	if (epInstance == null)
        	epInstance = Configuration.getInstance(ENDPOINT_CONFIG_FILENAME);
    	return epInstance;
    }

   
	public static boolean isNullREmpty(String inStr) {
		if (inStr == null || inStr.isEmpty()) {
			return true;
		}
		return false;
	}
}