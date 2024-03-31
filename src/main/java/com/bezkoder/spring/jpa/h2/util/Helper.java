package com.bezkoder.spring.jpa.h2.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class Helper {
	private final Logger LOGGER = LoggerFactory.getLogger(Helper.class);

	private DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
	private DateFormat dateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	private DateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");

	private Helper() {}

	public static Gson getGson() {
		Helper helper = new Helper();
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeHierarchyAdapter(Date.class, helper.new DateTimeJsonSerializer())
				.registerTypeHierarchyAdapter(Date.class, helper.new DateTimeJsonDeserializer());
		return gsonBuilder.create();
	}

	private class DateTimeJsonSerializer implements JsonSerializer<Date> {
		@Override
		public JsonElement serialize(Date src, java.lang.reflect.Type typeOfSrc, JsonSerializationContext context) {
			JsonElement jsonElement = null;
			if (Objects.nonNull(src)) {
				try {
					Calendar c = Calendar.getInstance();
					c.setTime(src);
					if (c.get(Calendar.HOUR_OF_DAY) + c.get(Calendar.MINUTE) + c.get(Calendar.SECOND) > 0) {
						jsonElement = new JsonPrimitive(dateTimeFormatter.format(src));
					} else {
						jsonElement = new JsonPrimitive(dateFormatter.format(src));
					}
				} catch (Exception e) {
					LOGGER.error("in DateTimeJsonSerializer() exception ", e);
					try {
						jsonElement = new JsonPrimitive(src.getTime());
					} catch (Exception e1) {
						LOGGER.error("in DateTimeJsonSerializer() exception ", e1);
					}
				}
			}
			return jsonElement;
		}
	}

	private class DateTimeJsonDeserializer implements JsonDeserializer<Date> {
		@Override
		public Date deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			Date zdt = null;
			try {
				if (Objects.nonNull(json)) {
					if (json.getAsString().contains(":")) {
						if (json.getAsString().contains("-")) {
							zdt = dateTimeFormatter.parse(json.getAsString());
						} else {
							zdt = timeFormatter.parse(json.getAsString());
						}
					} else {
						zdt = dateFormatter.parse(json.getAsString());
					}
				}
			} catch (ParseException e) {
				LOGGER.error("in DateTimeJsonDeserializer() exception ", e);
				try {
					zdt = new Date(json.getAsLong());
				} catch (Exception e1) {
					LOGGER.error("in DateTimeJsonDeserializer() exception ", e1);
				}
			}
			return zdt;
		}
	}

	public static Map convertRowToMap(Object[] row, Collection<String> entityAttributesAddedInSelectClause) {
		Map attributeNameValueMap = new HashMap<>();
		int attributeCount = 0;
		for (String attributeName : entityAttributesAddedInSelectClause) {
			Object value = row[attributeCount];
			if (Objects.nonNull(value)) {
				if (attributeName.contains(".") && attributeName.split(Pattern.quote(".")).length > 1) {
					Map innerAttributeNameValueMap = null;
					Map outerAttributeNameValueMap = attributeNameValueMap;
					while (attributeName.contains(".") && attributeName.split(Pattern.quote(".")).length > 1) {
						String innerEntity = attributeName.trim().split(Pattern.quote("."))[0];
						innerAttributeNameValueMap = (Map) outerAttributeNameValueMap.getOrDefault(innerEntity,
								new HashMap<>());
						outerAttributeNameValueMap.put(innerEntity, innerAttributeNameValueMap);
						attributeName = attributeName.trim().split(Pattern.quote("."), 2)[1];
						outerAttributeNameValueMap = innerAttributeNameValueMap;
					}
					innerAttributeNameValueMap.put(attributeName, value);
				} else {
					attributeNameValueMap.put(attributeName, value);
				}
			}
			attributeCount++;
		}
		return attributeNameValueMap;
	}
}
