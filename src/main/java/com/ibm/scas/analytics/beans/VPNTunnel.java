package com.ibm.scas.analytics.beans;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VPNTunnel extends BaseBean {
	public static enum VPNTunnelConnectionStatus {
		UNKNOWN,
		DISCONNECTED,
		CONNECTED
	}
	
	public static enum VPNParamNames {
		IKE_ENCRYPTION_ALG { @Override public String toString() { return "IKE Encryption Algorithm"; } },
		IKE_HASH_ALG { @Override public String toString() { return "IKE Hash Algorithm"; } },
		IKE_DH_GROUP { @Override public String toString() { return "IKE DH-Group"; } },
		IKE_KEY_LIFETIME { @Override public String toString() { return "IKE Key Lifetime (seconds)"; } },
		ESP_ENCRYPTION_ALG { @Override public String toString() { return "ESP Encryption Algorithm"; } },
		ESP_PFS { @Override public String toString() { return "ESP Perfect Forward Secrecy"; } },
		ESP_HASH_ALG { @Override public String toString() { return "ESP Hash Algorithm"; } },	
		ESP_KEY_LIFETIME { @Override public String toString() { return "ESP Key Lifetime (seconds)"; } },
		AUTHENTICATION_MODE { @Override public String toString() { return "Authentication Mode"; } },
		PRE_SHARED_SECRET { @Override public String toString() { return "Pre-Shared Secret"; } },
		CUST_SUBNETS { @Override public String toString() { return "Customer Subnets"; } },
	}

	public static enum IPSecVPNEncAlgorithms {
		AES256 { @Override public String toString() { return "AES-256"; } },
		AES128 { @Override public String toString() { return "AES-128"; } },
		TRIPLEDES { @Override public String toString() { return "3DES"; } };
		
		public static IPSecVPNEncAlgorithms getEnum(String value) {
	    	try{
	    		IPSecVPNEncAlgorithms enumVal = IPSecVPNEncAlgorithms.valueOf(value);
	    		return enumVal;
	    	}
            catch(IllegalArgumentException exception)
            {
            	for(IPSecVPNEncAlgorithms v : values())
    	            if(v.toString().equalsIgnoreCase(value)) return v;
            	throw new IllegalArgumentException();
            }
	    }
	}

	public static enum IPSecVPNHashAlgorithms {
		SHA1 { @Override public String toString() { return "SHA-1"; } },
		MD5  { @Override public String toString() { return "MD5"; } };
		
		public static IPSecVPNHashAlgorithms getEnum(String value) {
	    	try{
	    		IPSecVPNHashAlgorithms enumVal = IPSecVPNHashAlgorithms.valueOf(value);
	    		return enumVal;
	    	}
            catch(IllegalArgumentException exception)
            {
            	for(IPSecVPNHashAlgorithms v : values())
    	            if(v.toString().equalsIgnoreCase(value)) return v;
            	throw new IllegalArgumentException();
            }
	    }
	}

	public static enum IPSecVPNAuthModes {
		PRE_SHARED_KEY { @Override public String toString() { return "Pre-Shared Key"; } };
		
		public static IPSecVPNAuthModes getEnum(String value) {
	    	try{
	    		IPSecVPNAuthModes enumVal = IPSecVPNAuthModes.valueOf(value);
	    		return enumVal;
	    	}
            catch(IllegalArgumentException exception)
            {
            	for(IPSecVPNAuthModes v : values())
    	            if(v.toString().equalsIgnoreCase(value)) return v;
            	throw new IllegalArgumentException();
            }
	    }
	}
	
	public static enum IPSecVPNDHGroup {
		DH_GROUP_2 { @Override public String toString() { return "2"; } },
		DH_GROUP_5 { @Override public String toString() { return "5"; } };
		
		public static IPSecVPNDHGroup getEnum(String value) {
			for(IPSecVPNDHGroup v : values()) {
				// possibility: just the number
				if (v.toString().equals(value)) {
					return v;
				}
			}

	    	try {
	    		IPSecVPNDHGroup enumVal = IPSecVPNDHGroup.valueOf(value);
	    		return enumVal;
	    	} catch(IllegalArgumentException exception) {
            	for(IPSecVPNDHGroup v : values()) {
    	            if (v.toString().equalsIgnoreCase(value)) {
    	            	return v;
    	            }
            	}
            	
            	throw new IllegalArgumentException();
            }
	    }
	}
	
	public static enum IPSecVPNESP_PFS {
		ENABLE { @Override public String toString() { return "Enabled"; } },
		DH_GROUP_2 { @Override public String toString() { return "2"; } },
		DH_GROUP_5 { @Override public String toString() { return "5"; } },
		DISABLE { @Override public String toString() { return "Disabled"; } };
		
		public static IPSecVPNESP_PFS getEnum(String value) {
			for(final IPSecVPNESP_PFS v : values()) {
				// possibility: just the number
				if (v.toString().equals(value)) {
					return v;
				}
			}
	    	try {
	    		IPSecVPNESP_PFS enumVal = IPSecVPNESP_PFS.valueOf(value);
	    		return enumVal;
	    	} catch(IllegalArgumentException exception) {
            	for (final IPSecVPNESP_PFS v : values()) {
    	            if (v.toString().equalsIgnoreCase(value)) {
    	            	return v;
    	            }
            	}
            	throw new IllegalArgumentException();
            }
	    }
	}
	
	public static Map<VPNParamNames, String> DEFAULT_IPSEC_PARAMS;
	static {
		DEFAULT_IPSEC_PARAMS = new HashMap<VPNParamNames, String>();
		DEFAULT_IPSEC_PARAMS.put(VPNParamNames.IKE_ENCRYPTION_ALG, IPSecVPNEncAlgorithms.AES256.name());
		DEFAULT_IPSEC_PARAMS.put(VPNParamNames.IKE_HASH_ALG, IPSecVPNHashAlgorithms.SHA1.name());
		DEFAULT_IPSEC_PARAMS.put(VPNParamNames.IKE_DH_GROUP, IPSecVPNDHGroup.DH_GROUP_2.name());
		DEFAULT_IPSEC_PARAMS.put(VPNParamNames.ESP_ENCRYPTION_ALG, IPSecVPNEncAlgorithms.AES256.name());
		DEFAULT_IPSEC_PARAMS.put(VPNParamNames.ESP_HASH_ALG, IPSecVPNHashAlgorithms.SHA1.name());
		DEFAULT_IPSEC_PARAMS.put(VPNParamNames.ESP_PFS, IPSecVPNESP_PFS.DISABLE.name());
	}
	
	private String custIpAddr;
	private String gatewayId;		// read only
	private String cloudIpAddr;		// read only
	
	private List<Subnet> cloudSubnets;			// read only
	private final Map<VPNParamNames, String> params = new EnumMap<VPNTunnel.VPNParamNames, String>(VPNParamNames.class);
	private VPNTunnelConnectionStatus isConnected;	// read only

	public Map<VPNParamNames, String> getParams() {
		return params;
	}

	public VPNTunnelConnectionStatus getStatus() {
		return isConnected;
	}

	public void setStatus(VPNTunnelConnectionStatus isConnected) {
		this.isConnected = isConnected;
	}
	
	public String getGatewayId() {
		return gatewayId;
	}

	public void setGatewayId(String gatewayId) {
		this.gatewayId = gatewayId;
	}

	public String getCloudIpAddr() {
		return cloudIpAddr;
	}
	

	public void setCloudIpAddr(String cloudVpnEndpointIpAddress) {
		this.cloudIpAddr = cloudVpnEndpointIpAddress;
	}
	

	public String getCustIpAddr() {
		return custIpAddr;
	}
	

	public void setCustIpAddr(String custVpnEndpointIpAddress) {
		this.custIpAddr = custVpnEndpointIpAddress;
	}
	
	public List<Subnet> getCloudSubnets() {
		return cloudSubnets;
	}
	

	public void setCloudSubnets(List<Subnet> cloudSubnets) {
		this.cloudSubnets = cloudSubnets;
	}

}
