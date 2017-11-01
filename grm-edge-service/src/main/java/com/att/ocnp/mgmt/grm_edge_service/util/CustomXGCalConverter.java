/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.ocnp.mgmt.grm_edge_service.util;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class CustomXGCalConverter {
	public static class Serializer implements JsonSerializer
	{
		public Serializer()
		{
			super();
		}
		@Override
		public JsonElement serialize(Object src, Type typeOfSrc, JsonSerializationContext context) {
			XMLGregorianCalendar xgcal=(XMLGregorianCalendar)src;
			return new JsonPrimitive(xgcal.toXMLFormat());
		}

	}

	public static class Deserializer implements JsonDeserializer
	{

		@Override
		public Object deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			try {
				try
				{
					json.getAsString();
					return DatatypeFactory.newInstance().newXMLGregorianCalendar(json.getAsString());
				}
				catch(IllegalArgumentException ex)
				{
					Date date;
					DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSSZZZZ");
					date = df.parse(json.getAsString());
					GregorianCalendar cal = new GregorianCalendar();
					cal.setTime(date);
					int ms = (int)(long)(date.getTime()%1000L);
					XMLGregorianCalendar xmlDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)+1, cal.get(Calendar.DAY_OF_MONTH),date.getHours(),date.getMinutes(),date.getSeconds(),ms, -1*date.getTimezoneOffset());
					return xmlDate;
				}
				catch(UnsupportedOperationException ex)
				{
					JsonObject obj = json.getAsJsonObject();
					XMLGregorianCalendar cal = DatatypeFactory.newInstance().newXMLGregorianCalendar();
					if (obj.get("year") != null) {
						cal.setYear(obj.get("year").getAsBigInteger());
					}
					if (obj.get("month") != null) {
						cal.setMonth(obj.get("month").getAsInt());
					}
					if (obj.get("day") != null) {
						cal.setDay(obj.get("day").getAsInt());
					}
					if (obj.get("timezone") != null) {
						cal.setTimezone(obj.get("timezone").getAsInt());
					}
					if (obj.get("hour") != null) {
						cal.setHour(obj.get("hour").getAsInt());
					}
					if (obj.get("minute") != null) {
						cal.setMinute(obj.get("minute").getAsInt());
					}
					if (obj.get("second") != null) {
						cal.setSecond(obj.get("second").getAsInt());
					}
					if (obj.get("fractionalSecond") != null) {
						cal.setFractionalSecond(obj.get("fractionalSecond").getAsBigDecimal());
					}
					return cal;
				}
			} catch (Exception e) {
				return null;
			}

		}
	}
}

