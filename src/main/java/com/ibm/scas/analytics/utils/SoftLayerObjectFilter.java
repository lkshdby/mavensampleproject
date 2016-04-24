package com.ibm.scas.analytics.utils;

import java.util.Set;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class SoftLayerObjectFilter {
	private final JsonObject objFilterObj;
	private boolean isEmpty;
	
	public SoftLayerObjectFilter() {
		objFilterObj = new JsonObject();
		isEmpty = true;
	}

	public boolean isEmpty() {
		return isEmpty;
	}
	
	/**
	 * add an id object filter to an existing object filter
	 * @param ids array of IDs -- this is going to be casted to integers
	 * @return objectFilter fragment corresponding to "in" objectFilter
	 */
	public void setPropertyFilter(String propertyPath, Set<String> idsToMatch) {
		/*
		 * The "in" filter in SoftLayer API looks like:
		 * { 
		 *     "operation": "in",
		 *     "options": [{
		 *         "name": "data",
		 *         "value": [ id1, id2, ...]
		 *     }]
		 * }
		 */
	
		final JsonArray idArray = new JsonArray();
		for (final String id : idsToMatch) {
			idArray.add(new JsonPrimitive(Integer.valueOf(id)));
		}

		final JsonObject dataObject = new JsonObject();
		dataObject.add("name", new JsonPrimitive("data"));
		dataObject.add("value", idArray);
		
		final JsonArray optionsArray = new JsonArray();
		optionsArray.add(dataObject);

		final JsonObject filterObj = new JsonObject();
		filterObj.add("operation", new JsonPrimitive("in"));
		filterObj.add("options", optionsArray);
		
		JsonUtil.addObjAtPropertyPathElem(objFilterObj, propertyPath, filterObj);
		
		isEmpty = false;
	}

	/**
	 * Construct an objectFilter (see <a href="http://sldn.softlayer.com/article/Object-Filters">object filters</a>)
	 * This constructs an EXACT MATCH filter
	 * @param propertyPath Path of the filter to add (for example for VLANs, primaryRouter.hostname)
	 */
	public void setPropertyFilter(String propertyPath, String filterValue) {
		/*
		 * A basic object filter looks like:
		 * "propertyName": {
		 *     "operation": "<value">
		 * }
		 * 
		 * for a propertyPath property/subProperty,
		 * "property": {
		 *     "subproperty": {
		 *         "operation": "<value">
	     *     }
	     * }
		 */
		
		// parse the property path, delimited by "."
		final JsonObject subFilterObj = JsonUtil.findPropertyPathElem(objFilterObj, propertyPath).getAsJsonObject();
		subFilterObj.add("operation", new JsonPrimitive(filterValue));
		
		isEmpty = false;
	}
	
	/**
	 * Construct an objectFilter (see <a href="http://sldn.softlayer.com/article/Object-Filters">object filters</a>)
	 * This constructs an IS_NULL filter
	 * @param propertyPath Path of the filter to add (for example for VLANs, primaryRouter.hostname)
	 */
	public void setNullPropertyFilter(String propertyPath) {
		/*
		 * A null object filter looks like:
		 * "propertyName": {
		 *     "operation": "is null"
		 * }
		 * 
		 * for a propertyPath property/subProperty,
		 * "property": {
		 *     "subproperty": {
		 *         "operation": "is null"
	     *     }
	     * }
		 */
		
		// parse the property path, delimited by "."
		final JsonObject subFilterObj = JsonUtil.findPropertyPathElem(objFilterObj, propertyPath).getAsJsonObject();
		subFilterObj.add("operation", new JsonPrimitive("IS NULL"));
		
		isEmpty = false;
	}
	
	/**
	 * Construct an objectFilter (see <a href="http://sldn.softlayer.com/article/Object-Filters">object filters</a>)
	 * This constructs an NOT_NULL filter
	 * @param propertyPath Path of the filter to add (for example for VLANs, primaryRouter.hostname)
	 */
	public void setNotNullPropertyFilter(String propertyPath) {
		/*
		 * A null object filter looks like:
		 * "propertyName": {
		 *     "operation": "not null"
		 * }
		 * 
		 * for a propertyPath property/subProperty,
		 * "property": {
		 *     "subproperty": {
		 *         "operation": "not null"
	     *     }
	     * }
		 */
		
		// parse the property path, delimited by "."
		final JsonObject subFilterObj = JsonUtil.findPropertyPathElem(objFilterObj, propertyPath).getAsJsonObject();
		subFilterObj.add("operation", new JsonPrimitive("NOT NULL"));
		
		isEmpty = false;
	}
	
	/**
	 * Construct an objectFilter (see <a href="http://sldn.softlayer.com/article/Object-Filters">object filters</a>)
	 * This constructs an EXACT MATCH filter
	 * @param propertyPath Path of the filter to add (for example for VLANs, primaryRouter.hostname)
	 * @param filterValue the value to match
	 */
	public void setPropertyFilter(String propertyPath, int filterValue) {
		/*
		 * A basic object filter looks like:
		 * "propertyName": {
		 *     "operation": "<value">
		 * }
		 * 
		 * for a propertyPath property/subProperty,
		 * "property": {
		 *     "subproperty": {
		 *         "operation": "<value">
	     *     }
	     * }
		 */
		
		// parse the property path, delimited by "."
		final JsonObject subFilterObj = JsonUtil.findPropertyPathElem(objFilterObj, propertyPath).getAsJsonObject();
		subFilterObj.add("operation", new JsonPrimitive(filterValue));
		
		isEmpty = false;
	}
	
	/**
	 * Construct an objectFilter (see <a href="http://sldn.softlayer.com/article/Object-Filters">object filters</a>)
	 * This constructs an BOOLEAN filter
	 * @param propertyPath Path of the filter to add (for example for VLANs, primaryRouter.hostname)
	 * @param filterValue the value to match
	 */
	public void setPropertyFilter(String propertyPath, boolean filterValue) {
		/*
		 * A basic object filter looks like:
		 * "propertyName": {
		 *     "operation": "<value">
		 * }
		 * 
		 * for a propertyPath property/subProperty,
		 * "property": {
		 *     "subproperty": {
		 *         "operation": "<value">
	     *     }
	     * }
		 */
		
		// parse the property path, delimited by "."
		final JsonObject subFilterObj = JsonUtil.findPropertyPathElem(objFilterObj, propertyPath).getAsJsonObject();
		subFilterObj.add("operation", filterValue ? new JsonPrimitive(1) : new JsonPrimitive(0));
		
		isEmpty = false;
	}
	
	public JsonObject toJsonObject() {
		return objFilterObj;
	}
	
	@Override
	public String toString() {
		return new GsonBuilder().setPrettyPrinting().create().toJson(objFilterObj);
		
	}
}
