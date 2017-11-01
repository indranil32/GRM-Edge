/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.ocnp.mgmt.grm_edge_service.util;

import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import com.att.aft.config.ErrorContext;
import com.att.ocnp.mgmt.grm_edge_service.EdgeException;

public class DateUtil {
    private static final DatatypeFactory dtFactory = initFactory();
    /**
     * Returns an XMLGregorianCalendar for the provided date/time
     *
     * @return
     */

    private static synchronized DatatypeFactory initFactory() {
        DatatypeFactory dt = null;
        try {
            dt = DatatypeFactory.newInstance();
        }catch(Exception e) {
        }
        return dt;
    }

    public static XMLGregorianCalendar toXMLCalendar(Date date) throws EdgeException {
        try {
            if (date == null) {
                return null;
            }
            GregorianCalendar cal = new GregorianCalendar(GRMEdgeConstants.getTimeZone(), GRMEdgeConstants.getLocale());
            cal.setTime(date);
            XMLGregorianCalendar xmlCal = dtFactory.newXMLGregorianCalendar(cal);
            return xmlCal;
        } catch (Exception e) {
			throw new EdgeException(GRMEdgeConstants.INTERNAL_ERROR, new ErrorContext().add("message", GRMEdgeConstants.AFT_EDGE_INTERNAL_ERROR));	
        }
    }

    /**
     * Returns an XMLGregorianCalendar for the current date/time
     * @return
     */
    public static XMLGregorianCalendar toXMLCalendar() throws EdgeException {
        try {
            GregorianCalendar cal = new GregorianCalendar(GRMEdgeConstants.getTimeZone(), GRMEdgeConstants.getLocale());
            cal.setTime(new Date());
            XMLGregorianCalendar xmlCal = dtFactory.newXMLGregorianCalendar(cal);
            return xmlCal;
        } catch (Exception e) {
            throw new EdgeException(GRMEdgeConstants.INTERNAL_ERROR, new ErrorContext().add("message","Error Converting Date Type"));
        }
    }
    
    public static XMLGregorianCalendar getDate(Date date) {

        if (date == null) {
            return null;
        } else {
            return toXMLCalendar(date);
        }
    }
    
    public static Date getDate(XMLGregorianCalendar xMLGregorianCalendar) {

        if (xMLGregorianCalendar == null) {
            return null;
        } else {
			return xMLGregorianCalendar.toGregorianCalendar().getTime();
		}
    }
 
}
