/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.ocnp.mgmt.grm_edge_service.cache;

public class CacheException extends RuntimeException {
	public enum Severity
	{
		WARN(1),
		ERROR(2);
		
		private int value;
		private Severity(int value) 
		{
			this.value = value;
		}
		public int getValue() 
		{
			return value;
		}
	}
	public enum ErrorCatalog 
	{
		CACHE_001("unable to retreive cacheable data for cache [Name: {}], [Key: {}], [Value: {}], [Event: {}]", Severity.WARN, "AFT-ERR-TBD"),
		CACHE_002("interrupted the process waiting to retreive cacheable data for cache; will work with the old data [Name: {}], [Key: {}], [Value: {}], [Event: {}]", Severity.WARN, "AFT-ERR-TBD"),
		CACHE_003("Unknown cache event callback [Name: {}], [Event: {}]", Severity.ERROR, "AFT-ERR-TBD"),
		CACHE_004("unable to reload data [Name: {}], [Key: {}]", Severity.ERROR, "AFT-ERR-TBD"),
		CACHE_005("unable to obtain lock, some other thread might already be reloading same data [Name: {}], [Key: {}]", Severity.WARN, "AFT-ERR-TBD"),
		CACHE_006("failure to retreive cache data [Key: {}], [Error: {}]", Severity.WARN, "AFT-DME3-0605"),
		CACHE_007("unable to renewAllLeases;[Error: {}]; [{}]", Severity.WARN, "AFT-ERR-TBD"),
		CACHE_009("timer cannot invoke method [Error: {}]; Class [{}] Method [{}]", Severity.WARN, "AFT-ERR-TBD"),
		CACHE_010("cache configuration file load failed;[Error: {}]; file path [{}]", Severity.WARN, "AFT-ERR-TBD"),
		CACHE_011("Unknown cache type callback [Name: {}], [Event: {}]", Severity.ERROR, "AFT-ERR-TBD"),
		CACHE_012("configuration issue; cannot instantiate data handler [Error: {}], [Name: {}]", Severity.ERROR, "AFT-ERR-TBD"),
		CACHE_013("entry cannot be removed  [Name: {}]", Severity.ERROR, "AFT-ERR-TBD"),
		CACHE_014("cache by this name already exists  [Name: {}]", Severity.ERROR, "AFT-ERR-TBD"),
		CACHE_015("cache name cannot be [null]", Severity.ERROR, "AFT-ERR-TBD"),
		CACHE_016("pre-configured cache type name cannot be [null]", Severity.ERROR, "AFT-ERR-TBD"),
		CACHE_017("cache configuration for custom cache cannot be [null]", Severity.ERROR, "AFT-ERR-TBD"),
		CACHE_018("pre-configured cache type cannot be found with cache type name [ {} ]", Severity.ERROR, "AFT-ERR-TBD"),
		CACHE_019("cache type config file cannot be loaded from the configuration or from the classpath", Severity.ERROR, "AFT-ERR-TBD"),
		CACHE_020("cache type config file path is not set properly", Severity.ERROR, "AFT-ERR-TBD"),
		CACHE_021("hazelcast cache config file [{}] is not set properly", Severity.ERROR, "AFT-ERR-TBD");

		private String message;
		private Severity severity;
		private String aftErrorCode;
		private ErrorCatalog(String message, Severity severity, String aftErrorCode) 
		{
			this.message = message;
			this.severity = severity;
			this.aftErrorCode = aftErrorCode;
		}
		public String getMessage() 
		{
			return message;
		}
		public Severity getSeverity()
		{
			return severity;
		}
		public String getCode()
		{
			return this.toString();
		}
		public String getAftErrorCode()
		{
			return this.aftErrorCode;
		}
	}	
	private static final long serialVersionUID = 2992095809435518865L;
	private String code = null;
	private String msg = null;
	private String severity = null;

	public CacheException(ErrorCatalog catalogue, Object... objs) { 
		this(catalogue.getCode(), catalogue.getSeverity().toString(), catalogue.getMessage(), objs);
	}

	public CacheException(ErrorCatalog catalogue, Throwable t, Object... objs) { 
		this(catalogue.getCode(), catalogue.getSeverity().toString(), catalogue.getMessage(), t.getLocalizedMessage(),  objs);
	}
	public CacheException(ErrorCatalog catalogue, Throwable t, String arg1) { 
		this(catalogue.getCode(), catalogue.getSeverity().toString(), catalogue.getMessage(), t.getLocalizedMessage(),  arg1);
	}
	public CacheException(ErrorCatalog catalogue, Throwable t, String arg1, String arg2) { 
		this(catalogue.getCode(), catalogue.getSeverity().toString(), catalogue.getMessage(), t.getLocalizedMessage(),  arg1, arg2);
	}
	
	public CacheException(String code, String severity, String format, Object... objs) {
		super("["+severity+":" + code + "]: " + String.format(format, objs));
		this.code = code;
		this.msg = String.format(format, objs);
		this.severity = severity;
	}
	public CacheException(String code, String severity, String format, String arg1) {
		super("["+severity+":" + code + "]: " + String.format(format, arg1));
		this.code = code;
		this.msg = String.format(format, arg1);
		this.severity = severity;
	}
	public CacheException(String code, String severity, String format, String localMsg, String arg1) {
		super("["+severity+":" + code + "]: " + String.format(format, localMsg, arg1));
		this.code = code;
		this.msg = String.format(format, localMsg, arg1);
		this.severity = severity;
	}
	public CacheException(String code, String severity, String format, String localMsg, String arg1, String arg2) {
		super("["+severity+":" + code + "]: " + String.format(format, localMsg, arg1, arg2));
		this.code = code;
		this.msg = String.format(format, localMsg, arg1, arg2);
		this.severity = severity;
	}
	
	/**
	 * Gets the error code.
	 * 
	 * @return the error code
	 */
	public String getErrorCode() {
		return code;
	}

	/**
	 * Gets the error message.
	 * 
	 * @return the error message
	 */
	public String getErrorMessage() {
		return msg;
	}


}
