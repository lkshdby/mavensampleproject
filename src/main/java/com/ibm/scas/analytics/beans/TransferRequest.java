package com.ibm.scas.analytics.beans;

/**
 * 
 * <pre>
{
	"username" : "IBMOS278059-1:chenhan.devtest",
	"apiKey" : "213123123123012381203123", 
	"src" : "swift://han-test.softlayer/foo/bar",
	"dest" : "/han-test"
}
</pre>
 * @author chenhan
 *
 */
public class TransferRequest {
	private String username;
	private String apiKey;
	private String src;
	private String location;
	private String container;
	private String path;
	private String dest;
	private String authEndPoint;
	private String transferDirection;
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getApiKey() {
		return apiKey;
	}
	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}
	public String getSrc() {
		return src;
	}
	public void setSrc(String src) {
		this.src = src;
	}
	public String getDest() {
		return dest;
	}
	public void setDest(String dest) {
		this.dest = dest;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public String getContainer() {
		return container;
	}
	public void setContainer(String container) {
		this.container = container;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getAuthEndPoint() {
		return authEndPoint;
	}
	public void setAuthEndPoint(String authEndPoint) {
		this.authEndPoint = authEndPoint;
	}
	public String getTransferDirection() {
		return transferDirection;
	}
	public void setTransferDirection(String transferDirection) {
		this.transferDirection = transferDirection;
	}
	
}
