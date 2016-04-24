package com.ibm.scas.analytics.beans;

/**
 * POJO representing return message to AppDirect calls
 * 
 * @author Han Chen
 *
 */
public class NotificationResult {
	private String success;
	private String message;
	private String accountIdentifier;
	private String errorCode;
	
	public NotificationResult(String success, String message,
			String accountIdentifier) {
		super();
		this.success = success;
		this.message = message;
		this.accountIdentifier = accountIdentifier;
	}
	public NotificationResult(String success, String errorCode, String message,
			String accountIdentifier) {
		super();
		this.success = success;
		this.errorCode = errorCode;
		this.message = message;
		this.accountIdentifier = accountIdentifier;
	}
	public String getSuccess() {
		return success;
	}
	public void setSuccess(String success) {
		this.success = success;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getAccountIdentifier() {
		return accountIdentifier;
	}
	public void setAccountIdentifier(String accountIdentifier) {
		this.accountIdentifier = accountIdentifier;
	}
	public String getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}
}
