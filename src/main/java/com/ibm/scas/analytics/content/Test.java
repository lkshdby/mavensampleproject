package com.ibm.scas.analytics.content;

import java.io.IOException;
import java.util.List;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			String jsonStr = "[{\"count\": 4, \"bytes\": 32562, \"name\": \"apitest\"}, {\"count\": 0, \"bytes\": 0, \"name\": \"bigdata4\"}, {\"count\": 7, \"bytes\": 5482129167, \"name\": \"bur-test\"}, {\"count\": 2, \"bytes\": 19149980, \"name\": \"deletable\"}, {\"count\": 2, \"bytes\": 1141530988, \"name\": \"edata\"}, {\"count\": 4, \"bytes\": 133316, \"name\": \"edir\"}, {\"count\": 4, \"bytes\": 14830870261, \"name\": \"hpcpe-package\"}, {\"count\": 18, \"bytes\": 9334386686, \"name\": \"sys-backup\"}, {\"count\": 2, \"bytes\": 66656, \"name\": \"test_data\"}, {\"count\": 14, \"bytes\": 1104702850, \"name\": \"testdata\"}, {\"count\": 1, \"bytes\": 273150, \"name\": \"tmp\"}, {\"count\": 7, \"bytes\": 12646793551, \"name\": \"wordcount\"}, {\"count\": 5, \"bytes\": 3906612, \"name\": \"wordfiles\"}]";
			if(jsonStr.trim().length() != 0)
			{
				ObjectMapper mapper = new ObjectMapper();
				SoftLayerContainer[] slContainers = mapper.readValue(jsonStr, SoftLayerContainer[].class);
				System.out.println(slContainers.length);
				for(SoftLayerContainer container : slContainers)
				{
					System.out.println(container.getName());
				}
			}
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
