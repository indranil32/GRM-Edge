/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.ocnp.mgmt.grm_edge_service.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLOutputFactory2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.att.aft.dme2.types.Route;
import com.att.aft.dme2.types.RouteGroup;
import com.att.aft.dme2.types.RouteGroups;
import com.att.aft.dme2.types.RouteInfo;
import com.att.scld.grm.routeinfometadata.v1.RouteInfoMetaData;
import com.att.scld.grm.types.v1.ServiceVersionDefinition;
import com.att.scld.grm.types.v1.VersionDefinition;
import com.att.scld.grm.v1.GetRouteInfoRequest;
import com.att.scld.grm.v1.GetRouteInfoResponse;
import com.google.gson.Gson;

public class GRMRouteInfoUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(GRMRouteInfoUtil.class);
	private static final JAXBContext CONTEXT = initContext();

	private static final String GRMEDGEHOSTNAME = System.getProperty("PLATFORM_RUNTIME_ROUTEINFO_CHECK_GRM_EDGE_HOST", "zld01854.vci.att.com");
	private static final String GRMEDGEPORT = System.getProperty("PLATFORM_RUNTIME_ROUTEINFO_CHECK_GRM_EDGE_PORT", "9427");
	private static final String GRMEDGEGETROUTEINFO = System.getProperty("PLATFORM_RUNTIME_ROUTEINFO_CHECK_GRM_EDGE_CONTEXT_PATH", "GRMLWPService/v1/routeInfo/get");
	private static final String GRMEDGEGETROUTEINFOMETHOD = System.getProperty("PLATFORM_RUNTIME_ROUTEINFO_CHECK_GRM_EDGE_METHOD", "POST");
	private static final String GRMEDGEGETROUTEINFOAUTHHEADER = System.getProperty("PLATFORM_RUNTIME_ROUTEINFO_CHECK_GRM_EDGE_AUTH_HEADER", "Basic bTcxMzY0Om03MTM2NA==");

	/*
	 * Needed so that it trusts the certificate from GRM while making connections
	 */
	static {
		HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier()
		{
			public boolean verify(String hostname, SSLSession session)
			{
				return true;
			}
		});
	}

	public static String getRouteInfoFromGRMEdge(String serviceName, VersionDefinition version, String environment) {
		HttpsURLConnection connection = null;
		GetRouteInfoResponse resp = null;

		try {
			LOGGER.debug("Sending GetRouteInfoRequest to GRM");

			//Create request
			ServiceVersionDefinition svd = new ServiceVersionDefinition();
			svd.setName(serviceName);
			svd.setVersion(version);
			GetRouteInfoRequest req = new GetRouteInfoRequest();
			req.setEnv(environment);
			req.setServiceVersionDefinition(svd);

			//Create connection
			URL url = new URL("https://"+GRMEDGEHOSTNAME+":"+GRMEDGEPORT +"/"+ GRMEDGEGETROUTEINFO);
			LOGGER.debug("Sending request to url: {}", url);


			connection = (HttpsURLConnection)url.openConnection();
			connection.setRequestMethod(GRMEDGEGETROUTEINFOMETHOD);
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Authorization", GRMEDGEGETROUTEINFOAUTHHEADER);
			connection.setUseCaches(false);
			connection.setDoOutput(true);

			Gson gson = new Gson();

			// 2. Java object to JSON, and assign to a String
			String jsonInString = gson.toJson(req);

			//Send request
			DataOutputStream wr = new DataOutputStream (connection.getOutputStream());
			LOGGER.debug("JSON object sending to grm: {}" , jsonInString);
			wr.writeBytes(jsonInString);
			wr.close();

			InputStream is;
			if(connection.getResponseCode()/100 != 2){
				is = connection.getErrorStream();
				return null;
			}
			else{
				is = connection.getInputStream();
			}
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			StringBuilder response = new StringBuilder(); // or StringBuffer if not Java 5+ 
			String line;
			while((line = rd.readLine()) != null) {
				response.append(line);
				response.append('\r');
			}
			rd.close();
			resp = gson.fromJson(response.toString(), GetRouteInfoResponse.class);
		} catch(Exception e){
			LOGGER.error("Failed to connect to GRM because of error. Please look at logs for more detail.",e);
			return null;
		} finally{
			if(connection != null) {
				connection.disconnect(); 
			}
		}

		if (resp != null) {
			return resp.getRouteInfoXml();
		} else {
			return null;
		}
	}

	public static List<String> getDomainHierarchy(String inName){
		//If com.att.aft.TestService is passed in...
		//Will return a list containing these elements: [com, com.att, com.att.aft]
		List<String> domainList = new ArrayList<String>();
		String domain = GRMEdgeUtil.getDomain(inName);

		if (!StringUtils.isEmpty(domain)) {
			String[] domainValues = domain.split("\\.");

			String tempStr = "";

			for(int i = 0; i < domainValues.length; i++){
				if(i == 0){
					tempStr += domainValues[i];
					domainList.add(tempStr);
				}else{
					tempStr += "." + domainValues[i];
					domainList.add(tempStr);
				}						
			}
		}

		return domainList;
	}

	//Filter the route xml based on version selector to remove irrelevant routes
	//and version mappings
	public String filterRouteXMLbyVersionSelector(String xmlRouteInfo, ServiceVersionDefinition servVerDef) throws Exception {
		String versionString;
		int majorVersion;
		int minorVersion;

		if(xmlRouteInfo != null) {
			majorVersion = servVerDef.getVersion().getMajor();
			minorVersion = servVerDef.getVersion().getMinor();
			if(minorVersion < 0 ) {
				versionString = String.valueOf(servVerDef.getVersion().getMajor());
			}
			else{
				versionString = GRMEdgeUtil.getVersion(majorVersion, minorVersion, servVerDef.getVersion().getPatch());
			}

			if(versionString == null){
				return xmlRouteInfo;
			}

			try {
				RouteInfo rtInfo = unmarshalStax2(xmlRouteInfo);

				RouteInfo modInfo = new RouteInfo();
				modInfo.setServiceName(rtInfo.getServiceName());
				modInfo.setServiceVersion(rtInfo.getServiceVersion());
				modInfo.setDataPartitions(rtInfo.getDataPartitions());
				modInfo.setDataPartitionKeyPath(rtInfo.getDataPartitionKeyPath());
				modInfo.setEnvContext(rtInfo.getEnvContext());
				modInfo.setServiceVersion(rtInfo.getServiceVersion());
				modInfo.setStalenessInMins(rtInfo.getStalenessInMins());
				modInfo.setVersionMappings(rtInfo.getVersionMappings());

				String rtInfoServiceVersion = rtInfo.getServiceVersion();
				if(rtInfoServiceVersion != null) {
					if(!rtInfoServiceVersion.equals("*")) {
						if(!versionString.equals(rtInfoServiceVersion) && !versionString.startsWith(rtInfoServiceVersion+".") ) {
							throw new Exception("RouteInfo XML for the service " + servVerDef.getName()
							+ "has serviceVersion=" + rtInfoServiceVersion + ", does not match client provided input version of " + versionString);
						}
					}
				}

				RouteGroups modInfoGroups = new RouteGroups();
				modInfo.setRouteGroups(modInfoGroups);
				if(rtInfo.getRouteGroups() != null ) {
					for(RouteGroup rgs: rtInfo.getRouteGroups().getRouteGroup()) {
						RouteGroup modGroup = new RouteGroup();
						for(String partner: rgs.getPartner()) {
							modGroup.getPartner().add(partner);
						}
						for (Route route : rgs.getRoute()){
							String versionSel = route.getVersionSelector();
							if((versionSel != null) && (versionString != null)) {
								if (!versionString.equals(versionSel)) {
									// ignore this route being added to list
								}
								else {
									modGroup.getRoute().add(route);
								}
							}
							else {
								modGroup.getRoute().add(route);
							}
						}
						if((modGroup.getRoute() != null) && !modGroup.getRoute().isEmpty()){
							modInfoGroups.getRouteGroup().add(modGroup);
						}
					}
					if (modInfoGroups.getRouteGroup() == null || modInfoGroups.getRouteGroup().isEmpty() ) {
						throw new Exception("No Route Groups Exists After Version Filtering");
					}
				}

				return marshalStax2(modInfo);
			} catch (Exception e) {
				throw new Exception("Error Processing version filtering with routeinfo xml.");
			}


		} else {
			return xmlRouteInfo;
		}
	}

	private static synchronized JAXBContext initContext() {
		JAXBContext context = null;
		try {
			context = JAXBContext.newInstance(RouteInfo.class, RouteInfoMetaData.class);
			return context;
		} catch (Exception e) {
			return null;
		}
	}

	private RouteInfo unmarshalStax2(String xmlRouteInfo) throws Exception {
		XMLStreamReader xmlSr = null;
		XMLInputFactory xmlIf = null;
		Unmarshaller unmarshaller = null;
		try {
			unmarshaller = CONTEXT.createUnmarshaller();
			xmlIf = XMLInputFactory2.newInstance();
			xmlSr = xmlIf.createXMLStreamReader(new StringReader(xmlRouteInfo));
			JAXBElement<RouteInfo> element = unmarshaller.unmarshal((xmlSr),RouteInfo.class);
			return element.getValue();
		} catch (Exception e) {
			throw new Exception("Error unmarshalling routeinfo xml");
		} finally {
			if (xmlSr != null) {
				try {
					xmlSr.close();
				} catch (Exception e) {
				}
			}
		}
	}

	private String marshalStax2(RouteInfo rtInfo) throws Exception {
		XMLStreamWriter xmlSw = null;
		XMLOutputFactory xof = null;
		Marshaller marshaller = null;
		StringWriter sw = new StringWriter();
		try {
			marshaller = CONTEXT.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			xof = XMLOutputFactory2.newInstance();
			xmlSw = xof.createXMLStreamWriter(sw);
			marshaller.marshal(new JAXBElement<RouteInfo>(new QName("null","null"), RouteInfo.class, rtInfo), xmlSw);
			return sw.toString();
		} catch (Exception e) {
			throw new Exception("Error marshalling routeinfo xml");
		} finally {
			if (xmlSw != null) {
				try {
					sw.close();
					xmlSw.close();
				} catch (Exception e) {
				}
			}
		}
	}
}