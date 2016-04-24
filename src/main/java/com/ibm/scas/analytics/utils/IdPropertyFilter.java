package com.ibm.scas.analytics.utils;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.ser.BeanPropertyWriter;
import org.codehaus.jackson.map.ser.impl.SimpleBeanPropertyFilter;

import com.ibm.scas.analytics.beans.Account;
import com.ibm.scas.analytics.beans.Cluster;
import com.ibm.scas.analytics.beans.SoftLayerAccount;
import com.ibm.scas.analytics.beans.Subscriber;

public class IdPropertyFilter extends SimpleBeanPropertyFilter {

    public IdPropertyFilter() {
    }
    
    @Override
    public void serializeAsField(Object bean, JsonGenerator jgen,
            SerializerProvider provider, BeanPropertyWriter writer)
            		throws Exception {
    	final Object prop =  writer.get(bean);
    	
      	/*
     	 * maybe one day we'll remove nested beans   	
     	if (prop instanceof SoftLayerIdObject) {
    		// write the id and softlayer id to json
     		final SoftLayerIdObject baseBean = (SoftLayerIdObject)prop;
    		jgen.writeFieldName(writer.getName());
    		jgen.writeStartObject();
    		jgen.writeStringField("id", baseBean.getId());
    		jgen.writeStringField("softLayerId", baseBean.getSoftLayerId());
    		jgen.writeEndObject();    		
     		return;
    	}
 

    	if (prop instanceof BaseBean) {
    		// write just the ID to JSON

    		final BaseBean baseBean = (BaseBean)prop;
    		jgen.writeFieldName(writer.getName());
    		jgen.writeStartObject();
    		jgen.writeStringField("id", baseBean.getId());
    		jgen.writeEndObject();
    		return;
    	}
    	*/
    	if (prop instanceof Cluster) {
    		// write just the id to JSON to reduce clutter
    		final Cluster sla = (Cluster)prop;
    		jgen.writeFieldName(writer.getName());
    		jgen.writeStartObject();
    		jgen.writeStringField("id", sla.getId());
    		jgen.writeEndObject();
    		return;
    	}   	
       	if (prop instanceof SoftLayerAccount) {
    		// write just the id to JSON to reduce clutter
    		final SoftLayerAccount sla = (SoftLayerAccount)prop;
    		jgen.writeFieldName(writer.getName());
    		jgen.writeStartObject();
    		jgen.writeStringField("id", sla.getId());
    		jgen.writeEndObject();
    		return;
    	}   	   	
       	
      	if (prop instanceof Subscriber) {
    		// write just the id to JSON to reduce clutter
    		final Subscriber subscriber = (Subscriber)prop;
    		jgen.writeFieldName(writer.getName());
    		jgen.writeStartObject();
    		jgen.writeStringField("id", subscriber.getId());
    		jgen.writeStringField("name", subscriber.getName());
    		jgen.writeEndObject();
    		return;
    	}   	
    	
     	if (prop instanceof Account) {
    		// write just the accountIdentifier to JSON to reduce clutter
    		final Account account = (Account)prop;
    		jgen.writeFieldName(writer.getName());
    		jgen.writeStartObject();
    		jgen.writeStringField("accountIdentifier", account.getAccountIdentifier());
    		jgen.writeStringField("id", account.getId());
    		jgen.writeEndObject();
    		return;
    	}   	
    	
    	// write the entire property to json
    	writer.serializeAsField(bean, jgen, provider);
    }
    
    
}
