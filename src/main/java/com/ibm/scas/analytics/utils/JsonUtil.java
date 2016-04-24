package com.ibm.scas.analytics.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JsonUtil {
	
	/**
	 * Get a string value from path in a JsonObject.  We assume that the paths are all object
	 * values (i.e. no arrays or primitives along the path) and the last item in the path
	 * is a string
	 * @param jsonObjElem JsonObject
	 * @param pathElem path of the form path.subpath.subsubpath
	 * @return
	 */
	public static JsonArray getArrayFromPath(JsonElement jsonObjElem, String pathElem) {
		if (!jsonObjElem.isJsonObject()) {
			// can't continue on the path if the parent object isn't a json object
			return null;
		}
		
		final String[] propertyPathArr = pathElem.split("\\.");
		JsonElement currElem = jsonObjElem;
		for (int i = 0; i < propertyPathArr.length; i++) {
			final String pathToFind = propertyPathArr[i];
			
			final JsonObject currObj = currElem.getAsJsonObject();
			JsonElement myElem = currObj.get(pathToFind);
			if (myElem == null) {
				// cannot find  the path element
				return null;
			}
			
			// move down the tree
			currElem = myElem;
		}
		
		return currElem.getAsJsonArray();
	}
	
	public static JsonObject getObjFromPath(JsonElement jsonObjElem, String pathElem) {
		if (!jsonObjElem.isJsonObject()) {
			// can't continue on the path if the parent object isn't a json object
			return null;
		}
		
		final String[] propertyPathArr = pathElem.split("\\.");
		JsonElement currElem = jsonObjElem;
		for (int i = 0; i < propertyPathArr.length; i++) {
			final String pathToFind = propertyPathArr[i];
			
			final JsonObject currObj = currElem.getAsJsonObject();
			JsonElement myElem = currObj.get(pathToFind);
			if (myElem == null) {
				// cannot find  the path element
				return null;
			}
			
			// move down the tree
			currElem = myElem;
		}
		
		return currElem.getAsJsonObject();
	}
	
	/**
	 * Get a string value from path in a JsonObject.  We assume that the paths are all object
	 * values (i.e. no arrays or primitives along the path) and the last item in the path
	 * is a string
	 * @param jsonObjElem JsonObject
	 * @param pathElem path of the form path.subpath.subsubpath
	 * @return
	 */
	public static Boolean getBooleanFromPath(JsonElement jsonObjElem, String pathElem) {
		if (!jsonObjElem.isJsonObject()) {
			// can't continue on the path if the parent object isn't a json object
			return null;
		}
		
		final String[] propertyPathArr = pathElem.split("\\.");
		JsonElement currElem = jsonObjElem;
		for (int i = 0; i < propertyPathArr.length; i++) {
			final String pathToFind = propertyPathArr[i];
			
			final JsonObject currObj = currElem.getAsJsonObject();
			JsonElement myElem = currObj.get(pathToFind);
			if (myElem == null) {
				// cannot find  the path element
				return null;
			}
			
			// move down the tree
			currElem = myElem;
		}
		
		return currElem.getAsBoolean();
	}

	/**
	 * Get a string value from path in a JsonObject.  We assume that the paths are all object
	 * values (i.e. no arrays or primitives along the path) and the last item in the path
	 * is a string
	 * @param jsonObjElem JsonObject
	 * @param pathElem path of the form path.subpath.subsubpath
	 * @return
	 */
	public static String getStringFromPath(JsonElement jsonObjElem, String pathElem) {
		if (!jsonObjElem.isJsonObject()) {
			// can't continue on the path if the parent object isn't a json object
			return null;
		}
		
		final String[] propertyPathArr = pathElem.split("\\.");
		JsonElement currElem = jsonObjElem;
		for (int i = 0; i < propertyPathArr.length; i++) {
			final String pathToFind = propertyPathArr[i];
			
			final JsonObject currObj = currElem.getAsJsonObject();
			JsonElement myElem = currObj.get(pathToFind);
			if (myElem == null) {
				// cannot find  the path element
				return null;
			}
			
			// move down the tree
			currElem = myElem;
		}
		
		return currElem.getAsString();
	}
	
	/**
	 * Get a int value from path in a JsonObject.  We assume that the paths are all object
	 * values (i.e. no arrays or primitives along the path) and the last item in the path
	 * is a string
	 * @param jsonObjElem JsonObject
	 * @param pathElem path of the form path.subpath.subsubpath
	 * @return
	 */
	public static Integer getIntFromPath(JsonElement jsonObjElem, String pathElem) {
		if (!jsonObjElem.isJsonObject()) {
			// can't continue on the path if the parent object isn't a json object
			return null;
		}
		
		final String[] propertyPathArr = pathElem.split("\\.");
		JsonElement currElem = jsonObjElem;
		for (int i = 0; i < propertyPathArr.length; i++) {
			final String pathToFind = propertyPathArr[i];
			
			final JsonObject currObj = currElem.getAsJsonObject();
			JsonElement myElem = currObj.get(pathToFind);
			if (myElem == null) {
				// cannot find the path element
				return null;
			}
			
			// move down the tree
			currElem = myElem;
		}
		
		return currElem.getAsInt();
	}
	
	/**
	 * return the property element in the JsonObject corresponding to the path
	 * If the path object(s) don't exist, create them along the way.
	 * @param parentObj
	 * @param pathElem
	 * @return
	 */
	public static JsonElement findPropertyPathElem(JsonElement parentElem, String pathElem) {
		if (!parentElem.isJsonObject()) {
			// can't continue on the path if the parent object isn't a json object
			return null;
		}
		
		final JsonObject parentObj = parentElem.getAsJsonObject();
		final String[] propertyPathArr = pathElem.split("\\.", 2);
		final String pathToFind = propertyPathArr[0];
		
		JsonElement myElem = parentObj.get(pathToFind);
		if (myElem == null) {
			// construct new object
			myElem = new JsonObject();
			parentObj.add(pathToFind, myElem);
		}
			
		if (propertyPathArr.length > 1) {
			final JsonObject myObj = myElem.getAsJsonObject();
			final String remainingPath = propertyPathArr[1];
			return findPropertyPathElem(myObj, remainingPath);
		}
		
		return myElem;
	}

	/**
	 * add the object to the path in the parentObj
	 * If the path object(s) don't exist, create them along the way.
	 * @param parentElem parent object
	 * @param pathElem path (e.g. "path.subpath.subsubpath")
	 * @param objToAdd object to add at the end of the path
	 */
	public static void addObjAtPropertyPathElem(JsonElement parentElem, String pathElem, JsonElement objToAdd) {
		if (!parentElem.isJsonObject()) {
			// can't continue on the path if the parent object isn't a json object
			return;
		}
		
		final String[] propertyPathArr = pathElem.split("\\.");
		
		JsonElement currElem = parentElem;
		for (int i = 0; i < propertyPathArr.length; i++) {
			final String pathToFind = propertyPathArr[i];
			final JsonObject currObj = currElem.getAsJsonObject();
			JsonElement myElem = currObj.get(pathToFind);
			if (myElem == null) {
				if (i == propertyPathArr.length - 1) {
					// last item in path, put the object here as child item
					currObj.add(pathToFind, objToAdd);
				} else {
					// construct new object and continue path
					myElem = new JsonObject();
					currObj.add(pathToFind, myElem);
					
	
				}
			}					
			// move down the tree
			currElem = myElem;
		}
	}

}
