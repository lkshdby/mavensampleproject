package com.ibm.scas.analytics;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * The base class for the cluster provisioning engine REST API return JSONs
 * 
 * @author Han Chen
 *
 */
@XmlRootElement
public class BaseResult {

	private String message;
	private Object detail;

	public BaseResult() {
	}

	public BaseResult(String message) {
		super();
		this.message = message;
	}
	
	public BaseResult(String message, Object detail) {
		super();
		this.message = message;
		this.detail = detail;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Object getDetail() {
		return detail;
	}

	public void setDetail(Object detail) {
		this.detail = detail;
	}
	
//	public static Response errorResponse(String message) {
//		return errorResponse(Status.INTERNAL_SERVER_ERROR, message);
//	}
//	
//	public static Response errorResponse(StatusType status, String message) {
//		return Response.status(status).entity(new Message("error", message)).build();
//	}
//	
//	public static Response okResponse() {
//		return Response.status(Status.OK).entity(new Message("success")).build();
//	}
}
