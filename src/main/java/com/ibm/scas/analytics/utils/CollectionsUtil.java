package com.ibm.scas.analytics.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CollectionsUtil {
	
	public static <K,V> void addToMap(Map<K, List<V>> map, K key, V value) {
		List<V> valueList = map.get(key);
		if (valueList == null) {
			valueList = new ArrayList<V>();
			map.put(key, valueList);
		}
		
		valueList.add(value);
	}
	
	public static Collection<String> fromStringList(String stringList, String delim) {
		final String[] arr = stringList.split(delim);
		
		return Arrays.asList(arr);
	}
	
	public static String[] tokenizeExcludingQuotes(String stringToTokenize) {
		// use regular expressions to tokenize the string
		final Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(stringToTokenize);
		final List<String> tokens = new ArrayList<String>();
		while (m.find()) {
			tokens.add(m.group(1));
		}
		
		return tokens.toArray(new String[] {});
	}
	
	public static String toStringList(Collection<? extends Object> collection) {
		return toStringList(collection, ";");
	}
	
	public static String toStringList(Collection<? extends Object> collection, String delim) {
		final StringBuilder sb = new StringBuilder();
		
		for (final Object obj : collection) {
			if (sb.length() == 0) {
				sb.append(obj.toString());
				continue;
			} 
			
			sb.append(delim).append(obj.toString());
		}
		
		
		return sb.toString();
	}

}
