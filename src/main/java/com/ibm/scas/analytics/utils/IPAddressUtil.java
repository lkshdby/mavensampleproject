package com.ibm.scas.analytics.utils;

import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * IP Address utility for String -> Long conversion.
 * 
 * Shamelessly copied from PCM-AE.
 * 
 * @author jkwong
 *
 */
public class IPAddressUtil {
	public static final String HOSTNAME_PREFIX = "node";
	public static final String HOSTNAME_DELIM = "-";
	
	public static Long ipToLong(String ipString) {
		long result = 0;
		final java.util.StringTokenizer token = new java.util.StringTokenizer(
				ipString, ".");
		result += Long.parseLong(token.nextToken()) << 24;
		result += Long.parseLong(token.nextToken()) << 16;
		result += Long.parseLong(token.nextToken()) << 8;
		result += Long.parseLong(token.nextToken());
		return result;
	}

	public static String longToIp(long ipLong) {
		final StringBuilder sb = new StringBuilder();
		sb.append(ipLong >>> 24);
		sb.append(".");
		sb.append(String.valueOf((ipLong & 0x00FFFFFF) >>> 16));
		sb.append(".");
		sb.append(String.valueOf((ipLong & 0x0000FFFF) >>> 8));
		sb.append(".");
		sb.append(String.valueOf(ipLong & 0x000000FF));
		return sb.toString();
	}
	
	
	public static boolean isValidIp(String ipAddress)  
	{  
		// this is IPv4 only
	   if (ipAddress == null || ipAddress.length() <= 0) {
		   return false;
	   }
       String ip = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";   
       Pattern pattern = Pattern.compile(ip);   
       Matcher matcher =  pattern.matcher(ipAddress);   
       return matcher.matches();
	} 
	
	public static String prefixToSubnetMask(int prefix) {
		// create binary string consisting of "prefix" 1s and 32 - prefix 0s.
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < prefix; i++) {
			sb.append("1");
		}
		
		for (int i = prefix; i < 32; i++) {
			sb.append("0");
		}
		
		return longToIp(Long.parseLong(sb.toString(), 2));	
	}
	
	public static long getNetworkAddr(long ipAddr, long subnetMask) {
		return ipAddr & subnetMask;
	}
	
	public static String getNetworkAddr(String ipAddr, String subnetMask) {
		return longToIp(getNetworkAddr(ipToLong(ipAddr), ipToLong(subnetMask)));
	}

	public static String getNetworkAddr(String ipAddr, int prefix) {
		final String subnetMask = prefixToSubnetMask(prefix);
		return longToIp(getNetworkAddr(ipToLong(ipAddr), ipToLong(subnetMask)));
	}
	
	
	public static long getBroadcastAddr(long ipAddr, long subnetMask) {
		// reverse the subnet mask
		return ipAddr | (~subnetMask & 0xFFFF);
	}
	
	public static String getBroadcastAddr(String ipAddr, String subnetMask) {
		return longToIp(getBroadcastAddr(ipToLong(ipAddr), ipToLong(subnetMask)));
	}
	
	public static String getBroadcastAddr(String ipAddr, int prefix) {
		final String subnetMask = prefixToSubnetMask(prefix);
		return longToIp(getBroadcastAddr(ipToLong(ipAddr), ipToLong(subnetMask)));
	}

	public static boolean isIpInNetwork(String ipAddr, String networkAddr, String subnetMask) {
		final long ipLong = ipToLong(ipAddr);
		final long networkLong = ipToLong(networkAddr);
		final long subnetLong = ipToLong(subnetMask);
		final long broadcastLong = getBroadcastAddr(networkLong, subnetLong);
	
		if (ipLong < networkLong) {
			return false;
		}
		
		if (ipLong > broadcastLong) {
			return false;
		}
		
		return true;
	}
	
	public static boolean isIpInNetwork(String ipAddr, String networkAddr, int prefix) {
		final String subnetMask = prefixToSubnetMask(prefix);
		return isIpInNetwork(ipAddr, networkAddr, subnetMask);
		
	}
	
	public static List<Long> expandSubnetToLongArray(String networkAddr, int cidr) {
		final Long startAddr = ipToLong(networkAddr);
		final Long endAddr = ipToLong(getBroadcastAddr(networkAddr, cidr));
		final List<Long> toReturn = new ArrayList<Long>(Long.valueOf(endAddr - startAddr + 1).intValue());
		
		for (long i = startAddr; i <= endAddr; i++) {
			toReturn.add(i);
		}
		
		return toReturn;
	}
	
	public static String generateHostnameForIP(long ipAddress) {
		// IPv4 for now
		final String ipAddrString = longToIp(ipAddress);
		final StringBuilder hostnameSB = new StringBuilder();
		final String[] octets = ipAddrString.split("\\.");
		hostnameSB.append(HOSTNAME_PREFIX).append(HOSTNAME_DELIM);
		
		for (int i = 0; i < octets.length; i++) {
			hostnameSB.append(octets[i]);
			if (i < (octets.length - 1)) {
				hostnameSB.append(HOSTNAME_DELIM);
			}
		}
		
		return hostnameSB.toString();
	}
	
	public static Set<String> getMyIPv4Addrs() {
		final Set<String> toReturn = new HashSet<String>();
		
		Enumeration<NetworkInterface> intfIter;
		try {
			intfIter = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e) {
			return toReturn;
		}
		while (intfIter.hasMoreElements()) {
			final NetworkInterface ni = intfIter.nextElement();
			for (final InterfaceAddress addr : ni.getInterfaceAddresses()){
				final String addrStr = addr.getAddress().getHostAddress();
				if (!addrStr.matches("[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+")) {
					continue;
				}
				final int prefix = addr.getNetworkPrefixLength();
				toReturn.add(addrStr + "/" + prefix);
			}
		}
		
		return toReturn;
	}
}
