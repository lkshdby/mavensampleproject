package com.ibm.pcmae.cluster.beans;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Message {
	public final static String COMMENT = "comment";
	public final static String SUCCESS = "success";
	public final static String ERROR = "error";

	
	private String id;
	private String uri;
	private String message;
	private String type = COMMENT;

	public Message() {
	}

	public Message(String message) {
		super();
		this.message = message;
	}
	
	public Message(String message, String type) {
		super();
		this.message = message;
		this.type = type;
	}
	
	public Message(String id, String uri, String message, String type) {
		this(message, type);
		this.id = id;
		this.uri = uri;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
}


