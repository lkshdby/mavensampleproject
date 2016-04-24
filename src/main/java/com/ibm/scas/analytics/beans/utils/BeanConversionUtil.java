package com.ibm.scas.analytics.beans.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConvertUtilsBean2;
import org.apache.commons.beanutils.Converter;

import com.ibm.scas.analytics.beans.Cluster.ClusterStep;
import com.ibm.scas.analytics.beans.Gateway.GatewayType;
import com.ibm.scas.analytics.beans.SoftLayerIdObject;
import com.ibm.scas.analytics.beans.Subscriber.SubscriberType;
import com.ibm.scas.analytics.beans.VPNTunnel.VPNParamNames;
import com.ibm.scas.analytics.utils.CPEException;
import com.ibm.scas.analytics.utils.IPAddressUtil;

public class BeanConversionUtil {
	private static final BeanUtilsBean beanUtilsBean;
	
	static {
		final ConvertUtilsBean2 convertUtilsBean = new ConvertUtilsBean2();
		final BeanRecordConverter converter = new BeanRecordConverter();
		
		// register nested record converters from bean to record
		convertUtilsBean.register(converter, com.ibm.scas.analytics.beans.SoftLayerAccount.class);
		convertUtilsBean.register(converter, com.ibm.scas.analytics.persistence.beans.SoftLayerAccount.class);
		convertUtilsBean.register(converter, com.ibm.scas.analytics.beans.Gateway.class);
		convertUtilsBean.register(converter, com.ibm.scas.analytics.persistence.beans.Gateway.class);	
		convertUtilsBean.register(converter, com.ibm.scas.analytics.beans.GatewayMember.class);
		convertUtilsBean.register(converter, com.ibm.scas.analytics.persistence.beans.GatewayMember.class);	
		convertUtilsBean.register(converter, com.ibm.scas.analytics.beans.Vlan.class);	
		convertUtilsBean.register(converter, com.ibm.scas.analytics.persistence.beans.Vlan.class);	
		convertUtilsBean.register(converter, com.ibm.scas.analytics.beans.Subnet.class);
		convertUtilsBean.register(converter, com.ibm.scas.analytics.persistence.beans.Subnet.class);		
		convertUtilsBean.register(converter, com.ibm.scas.analytics.persistence.beans.Subscriber.class);		
		convertUtilsBean.register(converter, com.ibm.scas.analytics.beans.Subscriber.class);		
		convertUtilsBean.register(converter, com.ibm.scas.analytics.beans.Account.class);		
		convertUtilsBean.register(converter, com.ibm.scas.analytics.persistence.beans.Account.class);		
		convertUtilsBean.register(converter, com.ibm.scas.analytics.beans.IPAddress.class);		
		convertUtilsBean.register(converter, com.ibm.scas.analytics.persistence.beans.IPAddress.class);			
		convertUtilsBean.register(converter, com.ibm.scas.analytics.beans.Offering.class);		
		convertUtilsBean.register(converter, com.ibm.scas.analytics.persistence.beans.Offering.class);		
		convertUtilsBean.register(converter, com.ibm.scas.analytics.beans.Cluster.class);		
		convertUtilsBean.register(converter, com.ibm.scas.analytics.persistence.beans.Cluster.class);		
		convertUtilsBean.register(converter, com.ibm.scas.analytics.beans.VPNTunnel.class);		
		convertUtilsBean.register(converter, com.ibm.scas.analytics.persistence.beans.VPNTunnel.class);		
		convertUtilsBean.register(converter, com.ibm.scas.analytics.beans.SoftLayerOrder.class);
		convertUtilsBean.register(converter, com.ibm.scas.analytics.persistence.beans.SoftLayerOrder.class);
		convertUtilsBean.register(converter, com.ibm.scas.analytics.beans.SoftLayerIdObject.class);
		
		beanUtilsBean = new BeanUtilsBean(convertUtilsBean);	
	}
	
	public static com.ibm.scas.analytics.persistence.beans.Gateway convertToRecord(com.ibm.scas.analytics.beans.Gateway gateway) throws CPEException {
		return (com.ibm.scas.analytics.persistence.beans.Gateway) beanUtilsBean.getConvertUtils().convert(gateway, 
				com.ibm.scas.analytics.persistence.beans.Gateway.class);				
	}
	
	public static com.ibm.scas.analytics.beans.Gateway convertToBean(com.ibm.scas.analytics.persistence.beans.Gateway gateway) throws CPEException {
		return (com.ibm.scas.analytics.beans.Gateway) beanUtilsBean.getConvertUtils().convert(gateway, 
				com.ibm.scas.analytics.beans.Gateway.class);			
	}
	
	public static com.ibm.scas.analytics.persistence.beans.Vlan convertToRecord(com.ibm.scas.analytics.beans.Vlan vlan) throws CPEException {
		final com.ibm.scas.analytics.persistence.beans.Vlan toReturn = new com.ibm.scas.analytics.persistence.beans.Vlan();

		try {
			beanUtilsBean.copyProperties(toReturn, vlan);
		} catch (IllegalAccessException e) {
			throw new CPEException(e);
		} catch (InvocationTargetException e) {
			throw new CPEException(e);
		}
		
		return toReturn;
		
	}
	
	public static com.ibm.scas.analytics.beans.Vlan convertToBean(com.ibm.scas.analytics.persistence.beans.Vlan vlan) throws CPEException {
		final com.ibm.scas.analytics.beans.Vlan toReturn = new com.ibm.scas.analytics.beans.Vlan();

		try {
			beanUtilsBean.copyProperties(toReturn, vlan);
		} catch (IllegalAccessException e) {
			throw new CPEException(e);
		} catch (InvocationTargetException e) {
			throw new CPEException(e);
		}
		
		return toReturn;
		
	}

	public static com.ibm.scas.analytics.persistence.beans.Subnet convertToRecord(com.ibm.scas.analytics.beans.Subnet subnet) throws CPEException {
		final com.ibm.scas.analytics.persistence.beans.Subnet toReturn = new com.ibm.scas.analytics.persistence.beans.Subnet();

		try {
			beanUtilsBean.copyProperties(toReturn, subnet);
		} catch (IllegalAccessException e) {
			throw new CPEException(e);
		} catch (InvocationTargetException e) {
			throw new CPEException(e);
		}
		
		return toReturn;
	}
	
	public static com.ibm.scas.analytics.beans.Subnet convertToBean(com.ibm.scas.analytics.persistence.beans.Subnet subnet) throws CPEException {
		final com.ibm.scas.analytics.beans.Subnet toReturn = new com.ibm.scas.analytics.beans.Subnet();

		try {
			beanUtilsBean.copyProperties(toReturn, subnet);
		} catch (IllegalAccessException e) {
			throw new CPEException(e);
		} catch (InvocationTargetException e) {
			throw new CPEException(e);
		}
		
		return toReturn;
	}

	public static com.ibm.scas.analytics.beans.IPAddress convertToBean(com.ibm.scas.analytics.persistence.beans.IPAddress ipAddress) throws CPEException {
		return (com.ibm.scas.analytics.beans.IPAddress) beanUtilsBean.getConvertUtils().convert(ipAddress, 
				com.ibm.scas.analytics.beans.IPAddress.class);					
	}
	
	/**
	 * Convert a concrete object to a reference object.  Reference objects only have "Id" and "softLayerId" properties
	 * @param someObj
	 * @return
	 * @throws CPEException
	 */
	public static SoftLayerIdObject convertToRef(com.ibm.scas.analytics.beans.SoftLayerIdObject someObj) throws CPEException {
		if (someObj == null) {
			return null;
		}
		
		final SoftLayerIdObject obj = new SoftLayerIdObject();
		try {
			BeanUtils.copyProperties(obj, someObj);
		} catch (IllegalAccessException e) {
			throw new CPEException(e);
		} catch (InvocationTargetException e) {
			throw new CPEException(e);
		}
		
		return obj;
	}
	
	public static com.ibm.scas.analytics.beans.Subscriber convertToBean(com.ibm.scas.analytics.persistence.beans.Subscriber subscriberRec) throws CPEException {
		return (com.ibm.scas.analytics.beans.Subscriber) beanUtilsBean.getConvertUtils().convert(subscriberRec, 
				com.ibm.scas.analytics.beans.Subscriber.class);				
	}
	
	public static com.ibm.scas.analytics.beans.Account convertToBean(com.ibm.scas.analytics.persistence.beans.Account accountRec) throws CPEException {
		return (com.ibm.scas.analytics.beans.Account) beanUtilsBean.getConvertUtils().convert(accountRec, 
				com.ibm.scas.analytics.beans.Account.class);		
	}
	
	public static com.ibm.scas.analytics.beans.Cluster convertToBean(com.ibm.scas.analytics.persistence.beans.Cluster clusterRec) throws CPEException {
		return (com.ibm.scas.analytics.beans.Cluster) beanUtilsBean.getConvertUtils().convert(clusterRec, 
				com.ibm.scas.analytics.beans.Cluster.class);		
	}
	
	public static com.ibm.scas.analytics.persistence.beans.Subscriber convertToRecord(com.ibm.scas.analytics.beans.Subscriber bean) throws CPEException {
		return (com.ibm.scas.analytics.persistence.beans.Subscriber) beanUtilsBean.getConvertUtils().convert(bean, 
				com.ibm.scas.analytics.persistence.beans.Subscriber.class);				
	}
	
	public static com.ibm.scas.analytics.persistence.beans.Account convertToRecord(com.ibm.scas.analytics.beans.Account bean) throws CPEException {
		return (com.ibm.scas.analytics.persistence.beans.Account) beanUtilsBean.getConvertUtils().convert(bean, 
				com.ibm.scas.analytics.persistence.beans.Account.class);
	}
	
	public static com.ibm.scas.analytics.persistence.beans.VPNTunnel convertToRecord(com.ibm.scas.analytics.beans.VPNTunnel vpnTunnel) {
		return (com.ibm.scas.analytics.persistence.beans.VPNTunnel) beanUtilsBean.getConvertUtils().convert(vpnTunnel, 
				com.ibm.scas.analytics.persistence.beans.VPNTunnel.class);
	}
	
	public static com.ibm.scas.analytics.beans.VPNTunnel convertToBean(com.ibm.scas.analytics.persistence.beans.VPNTunnel vpnTunnel) {
		return (com.ibm.scas.analytics.beans.VPNTunnel) beanUtilsBean.getConvertUtils().convert(vpnTunnel, 
				com.ibm.scas.analytics.beans.VPNTunnel.class);
	}
	
	public static com.ibm.scas.analytics.persistence.beans.SoftLayerOrder convertToRecord(com.ibm.scas.analytics.beans.SoftLayerOrder slOrder) {
		return (com.ibm.scas.analytics.persistence.beans.SoftLayerOrder) beanUtilsBean.getConvertUtils().convert(slOrder, 
				com.ibm.scas.analytics.persistence.beans.SoftLayerOrder.class);
	}
	
	public static com.ibm.scas.analytics.beans.SoftLayerOrder convertToBean(com.ibm.scas.analytics.persistence.beans.SoftLayerOrder slOrder) {
		return (com.ibm.scas.analytics.beans.SoftLayerOrder) beanUtilsBean.getConvertUtils().convert(slOrder, 
				com.ibm.scas.analytics.beans.SoftLayerOrder.class);
	}
	
	public static com.ibm.scas.analytics.persistence.beans.Offering convertToRecord(com.ibm.scas.analytics.beans.Offering bean) throws CPEException {
		final com.ibm.scas.analytics.persistence.beans.Offering toReturn = new com.ibm.scas.analytics.persistence.beans.Offering();

		try {
			beanUtilsBean.copyProperties(toReturn, bean);
		} catch (IllegalAccessException e) {
			throw new CPEException(e);
		} catch (InvocationTargetException e) {
			throw new CPEException(e);
		}
		
		return toReturn;
	}
	
	public static com.ibm.scas.analytics.beans.Offering convertToBean(com.ibm.scas.analytics.persistence.beans.Offering bean) throws CPEException {
		final com.ibm.scas.analytics.beans.Offering toReturn = new com.ibm.scas.analytics.beans.Offering();

		try {
			beanUtilsBean.copyProperties(toReturn, bean);
		} catch (IllegalAccessException e) {
			throw new CPEException(e);
		} catch (InvocationTargetException e) {
			throw new CPEException(e);
		}
		
		return toReturn;
	}
	
	
	public static int subscriberTypeToInt(SubscriberType type) {
		if (type == null)  {
			// default, APPDIRECT
			return com.ibm.scas.analytics.persistence.beans.Subscriber.APPDIRECT;				
		} else if (type == com.ibm.scas.analytics.beans.Subscriber.SubscriberType.APPDIRECT_ARCHIVED) {
			return com.ibm.scas.analytics.persistence.beans.Subscriber.APPDIRECT_ARCHIVED;	
		} else if (type == com.ibm.scas.analytics.beans.Subscriber.SubscriberType.CLOUDOE){
			return com.ibm.scas.analytics.persistence.beans.Subscriber.CLOUDOE;
		} else if (type == com.ibm.scas.analytics.beans.Subscriber.SubscriberType.CLOUDOE_PULSE_DEMO) {
			return com.ibm.scas.analytics.persistence.beans.Subscriber.CLOUDOE_PULSE_DEMO;	
		} else if (type == com.ibm.scas.analytics.beans.Subscriber.SubscriberType.GARBAGE_COLLECTOR) {
			return com.ibm.scas.analytics.persistence.beans.Subscriber.GARBAGE_COLLECTOR;	
		} else if (type == com.ibm.scas.analytics.beans.Subscriber.SubscriberType.SF) {
			return com.ibm.scas.analytics.persistence.beans.Subscriber.SF;	
		} else if (type == com.ibm.scas.analytics.beans.Subscriber.SubscriberType.DEV_TEST) {
			return com.ibm.scas.analytics.persistence.beans.Subscriber.DEV_TEST;
		} else if (type == com.ibm.scas.analytics.beans.Subscriber.SubscriberType.SYSTEM) {
			return com.ibm.scas.analytics.persistence.beans.Subscriber.SYSTEM;
		} else  {
			// default, APPDIRECT
			return com.ibm.scas.analytics.persistence.beans.Subscriber.APPDIRECT;
		} 
	}
	
	public static SubscriberType intToSubscriberType(int type) {
		if (type == com.ibm.scas.analytics.persistence.beans.Subscriber.APPDIRECT_ARCHIVED) {
			return com.ibm.scas.analytics.beans.Subscriber.SubscriberType.APPDIRECT_ARCHIVED;
		} else if (type == com.ibm.scas.analytics.persistence.beans.Subscriber.CLOUDOE) {
			return com.ibm.scas.analytics.beans.Subscriber.SubscriberType.CLOUDOE;
		} else if (type == com.ibm.scas.analytics.persistence.beans.Subscriber.CLOUDOE_PULSE_DEMO) {
			return com.ibm.scas.analytics.beans.Subscriber.SubscriberType.CLOUDOE_PULSE_DEMO;
		} else if (type == com.ibm.scas.analytics.persistence.beans.Subscriber.GARBAGE_COLLECTOR) {
			return com.ibm.scas.analytics.beans.Subscriber.SubscriberType.GARBAGE_COLLECTOR;	
		} else if (type == com.ibm.scas.analytics.persistence.beans.Subscriber.DEV_TEST) {
			return com.ibm.scas.analytics.beans.Subscriber.SubscriberType.DEV_TEST;
		} else if (type == com.ibm.scas.analytics.persistence.beans.Subscriber.SF) {
			return com.ibm.scas.analytics.beans.Subscriber.SubscriberType.SF;	
		} else if (type == com.ibm.scas.analytics.persistence.beans.Subscriber.SYSTEM) {
			return com.ibm.scas.analytics.beans.Subscriber.SubscriberType.SYSTEM;	
		} else  {
			// default, APPDIRECT
			return com.ibm.scas.analytics.beans.Subscriber.SubscriberType.APPDIRECT;
		} 
	}
	
	
	private static class BeanRecordConverter implements Converter {
		/* custom conversion class for bean->record and record->bean conversion */

		@Override
		public <T> T convert(Class<T> type, Object value) {
			if (value == null) {
				return null;
			}
			
			final ConvertUtilsBean2 convertUtilsBean = new ConvertUtilsBean2();

			// register nested converters from record to bean
			convertUtilsBean.register(this, com.ibm.scas.analytics.beans.SoftLayerAccount.class);
			convertUtilsBean.register(this, com.ibm.scas.analytics.beans.Gateway.class);
			convertUtilsBean.register(this, com.ibm.scas.analytics.beans.GatewayMember.class);
			convertUtilsBean.register(this, com.ibm.scas.analytics.beans.Vlan.class);
			convertUtilsBean.register(this, com.ibm.scas.analytics.beans.Subnet.class);
			convertUtilsBean.register(this, com.ibm.scas.analytics.beans.Subscriber.class);		
			convertUtilsBean.register(this, com.ibm.scas.analytics.beans.Account.class);		
			convertUtilsBean.register(this, com.ibm.scas.analytics.beans.Offering.class);		
			convertUtilsBean.register(this, com.ibm.scas.analytics.beans.Cluster.class);		
			convertUtilsBean.register(this, com.ibm.scas.analytics.beans.IPAddress.class);
			convertUtilsBean.register(this, com.ibm.scas.analytics.beans.VPNTunnel.class);
			convertUtilsBean.register(this, com.ibm.scas.analytics.beans.SoftLayerOrder.class);

			convertUtilsBean.register(this, com.ibm.scas.analytics.persistence.beans.SoftLayerAccount.class);								
			convertUtilsBean.register(this, com.ibm.scas.analytics.persistence.beans.Gateway.class);								
			convertUtilsBean.register(this, com.ibm.scas.analytics.persistence.beans.GatewayMember.class);								
			convertUtilsBean.register(this, com.ibm.scas.analytics.persistence.beans.Vlan.class);								
			convertUtilsBean.register(this, com.ibm.scas.analytics.persistence.beans.Subnet.class);								
			convertUtilsBean.register(this, com.ibm.scas.analytics.persistence.beans.Subscriber.class);		
			convertUtilsBean.register(this, com.ibm.scas.analytics.persistence.beans.Account.class);		
			convertUtilsBean.register(this, com.ibm.scas.analytics.persistence.beans.Offering.class);		
			convertUtilsBean.register(this, com.ibm.scas.analytics.persistence.beans.Cluster.class);		
			convertUtilsBean.register(this, com.ibm.scas.analytics.persistence.beans.VPNTunnel.class);
			convertUtilsBean.register(this, com.ibm.scas.analytics.persistence.beans.SoftLayerOrder.class);

			convertUtilsBean.register(this, com.ibm.scas.analytics.beans.SoftLayerIdObject.class);
			
			final BeanUtilsBean beanUtilsBean = new BeanUtilsBean(convertUtilsBean);

			try {
				T toReturn = (T) type.newInstance();

				if (type == SoftLayerIdObject.class) {
					// only copy two properties, id and softLayerId
					final Method getIdmethod = value.getClass().getMethod("getId");
					final String idVal = (String)getIdmethod.invoke(value);

					final Method setIdMethod = toReturn.getClass().getMethod("setId", String.class);
					setIdMethod.invoke(toReturn, idVal);

					final Method getSoftLayerIdmethod = value.getClass().getMethod("getSoftLayerId");
					final String softLayerIdVal = (String)getSoftLayerIdmethod.invoke(value);

					final Method setSoftLayerIdMethod = toReturn.getClass().getMethod("setSoftLayerId", String.class);
					setSoftLayerIdMethod.invoke(toReturn, softLayerIdVal);
				} else if (type == com.ibm.scas.analytics.beans.Account.class &&
					value instanceof com.ibm.scas.analytics.persistence.beans.Account) {
					// from record to bean
					final Map<String, Object> propertyMap = beanUtilsBean.getPropertyUtils().describe(value);
					
					// account id is "accountIdentifier", accountRec is "id"
					propertyMap.put("accountIdentifier", propertyMap.get("id"));
					propertyMap.remove("id");
					
					// convert the state (int) to status (String)
					final Integer state = (Integer)propertyMap.get("state");
					if (state == com.ibm.scas.analytics.persistence.beans.Account.ACTIVE) {
						propertyMap.put("status", com.ibm.scas.analytics.beans.Account.ACTIVE);
					} else if (state == com.ibm.scas.analytics.persistence.beans.Account.CANCELLED) {
						propertyMap.put("status", com.ibm.scas.analytics.persistence.beans.Account.CANCELLED);
					} else if (state == com.ibm.scas.analytics.persistence.beans.Account.FREE_TRIAL) {
						propertyMap.put("status", com.ibm.scas.analytics.persistence.beans.Account.FREE_TRIAL);
					} else if (state == com.ibm.scas.analytics.persistence.beans.Account.FREE_TRIAL_EXPIRED) {
						propertyMap.put("status", com.ibm.scas.analytics.persistence.beans.Account.FREE_TRIAL_EXPIRED);					
					} else if (state == com.ibm.scas.analytics.persistence.beans.Account.SUSPENDED) {
						propertyMap.put("status", com.ibm.scas.analytics.persistence.beans.Account.SUSPENDED);					
					} else {
						propertyMap.put("status", com.ibm.scas.analytics.persistence.beans.Account.UNKNOWN);					
					}
					propertyMap.remove("state");
					
					// copy the rest of the properties
					beanUtilsBean.populate(toReturn, propertyMap);
	
				} else if (type == com.ibm.scas.analytics.persistence.beans.Account.class &&
						value instanceof com.ibm.scas.analytics.beans.Account) {
					// from bean to record
					final Map<String, Object> propertyMap = beanUtilsBean.getPropertyUtils().describe(value);
					
					// account id is "accountIdentifier", accountRec is "id"
					propertyMap.put("id", propertyMap.get("accountIdentifier"));
					propertyMap.remove("accountIdentifier");
					
					// convert the status (String) to state (int)
					final String status = (String)propertyMap.get("status");
					if (status == null) {
						propertyMap.put("state", com.ibm.scas.analytics.persistence.beans.Account.UNKNOWN);									
					} else if (status.equals(com.ibm.scas.analytics.beans.Account.ACTIVE)) {
						propertyMap.put("state", com.ibm.scas.analytics.persistence.beans.Account.ACTIVE);
					} else if (status.equals(com.ibm.scas.analytics.beans.Account.CANCELLED)) {
						propertyMap.put("state", com.ibm.scas.analytics.persistence.beans.Account.CANCELLED);
					} else if (status.equals(com.ibm.scas.analytics.beans.Account.FREE_TRIAL)) {
						propertyMap.put("state", com.ibm.scas.analytics.persistence.beans.Account.FREE_TRIAL);
					} else if (status.equals(com.ibm.scas.analytics.beans.Account.FREE_TRIAL_EXPIRED)) {
						propertyMap.put("state", com.ibm.scas.analytics.persistence.beans.Account.FREE_TRIAL_EXPIRED);					
					} else if (status.equals(com.ibm.scas.analytics.beans.Account.SUSPENDED)) {
						propertyMap.put("state", com.ibm.scas.analytics.persistence.beans.Account.SUSPENDED);					
					} else {
						propertyMap.put("state", com.ibm.scas.analytics.persistence.beans.Account.UNKNOWN);									
					}
					propertyMap.remove("status");
					
					// copy the rest of the properties
					beanUtilsBean.populate(toReturn, propertyMap);
					
				} else if (type == com.ibm.scas.analytics.beans.Subscriber.class &&
						value instanceof com.ibm.scas.analytics.persistence.beans.Subscriber) {
					// from record to bean
					
					// convert the record "int" type to a "String" in the bean
					final Map<String, Object> propertyMap = beanUtilsBean.getPropertyUtils().describe(value);
					final com.ibm.scas.analytics.persistence.beans.Subscriber subscriber = (com.ibm.scas.analytics.persistence.beans.Subscriber)value;
					propertyMap.put("type", intToSubscriberType(subscriber.getType()));
				
					beanUtilsBean.populate(toReturn, propertyMap);
				} else if (type == com.ibm.scas.analytics.persistence.beans.Subscriber.class &&
						value instanceof com.ibm.scas.analytics.beans.Subscriber) {
					// from bean to record
					// convert the bean "String" type to a "int" in the bean
					final Map<String, Object> propertyMap = beanUtilsBean.getPropertyUtils().describe(value);
					final com.ibm.scas.analytics.beans.Subscriber subscriber = (com.ibm.scas.analytics.beans.Subscriber)value;
					propertyMap.put("type", subscriberTypeToInt(subscriber.getType()));
				
					// everything else is the same
					beanUtilsBean.populate(toReturn, propertyMap);
				} else if (type == com.ibm.scas.analytics.persistence.beans.IPAddress.class &&
						value instanceof com.ibm.scas.analytics.beans.IPAddress) {
					// from bean to record
					final Map<String, Object> propertyMap = beanUtilsBean.getPropertyUtils().describe(value);
					final com.ibm.scas.analytics.beans.IPAddress ipAddr = (com.ibm.scas.analytics.beans.IPAddress)value;
					
					// change String IP to long
					propertyMap.put("ipAddress", IPAddressUtil.ipToLong(ipAddr.getIpAddress()));
				
					// everything else is the same
					beanUtilsBean.populate(toReturn, propertyMap);				
				} else if (type == com.ibm.scas.analytics.beans.IPAddress.class &&
						value instanceof com.ibm.scas.analytics.persistence.beans.IPAddress) {
					// from bean to record
					final Map<String, Object> propertyMap = beanUtilsBean.getPropertyUtils().describe(value);
					
					// change long IP to String
					final com.ibm.scas.analytics.persistence.beans.IPAddress ipAddr = (com.ibm.scas.analytics.persistence.beans.IPAddress)value;
					propertyMap.put("ipAddress", IPAddressUtil.longToIp(ipAddr.getIpAddress()));
				
					// everything else is the same
					beanUtilsBean.populate(toReturn, propertyMap);								
				} else if (type == com.ibm.scas.analytics.persistence.beans.Vlan.class &&
						value instanceof com.ibm.scas.analytics.beans.Vlan) {
					// from bean to record
					final Map<String, Object> propertyMap = beanUtilsBean.getPropertyUtils().describe(value);
					
					// remove the location; read-only property that cannot be set from the bean
					propertyMap.remove("location");
				
					// everything else is the same
					beanUtilsBean.populate(toReturn, propertyMap);				
				} else if (type == com.ibm.scas.analytics.beans.Vlan.class &&
						value instanceof com.ibm.scas.analytics.persistence.beans.Vlan) {
					// from bean to record
					final Map<String, Object> propertyMap = beanUtilsBean.getPropertyUtils().describe(value);
					
					// change location to string
					final com.ibm.scas.analytics.persistence.beans.Vlan vlan = (com.ibm.scas.analytics.persistence.beans.Vlan)value;
					propertyMap.put("location", vlan.getLocation().getName());
				
					// everything else is the same
					beanUtilsBean.populate(toReturn, propertyMap);												
				} else if (type == com.ibm.scas.analytics.persistence.beans.Gateway.class &&
						value instanceof com.ibm.scas.analytics.beans.Gateway) {
					// from bean to record
					final Map<String, Object> propertyMap = beanUtilsBean.getPropertyUtils().describe(value);
					// convert GatewayType to a string
					propertyMap.put("type", propertyMap.get("type").toString());
					
					// remove the location; read-only property that cannot be set from the bean
					propertyMap.remove("location");
					
					final com.ibm.scas.analytics.beans.Gateway gw = (com.ibm.scas.analytics.beans.Gateway)value;
					// create an array of GatewayMembers
					final Collection<com.ibm.scas.analytics.persistence.beans.GatewayMember> recMembers = new ArrayList<com.ibm.scas.analytics.persistence.beans.GatewayMember>();
					if (gw.getGatewayMembers() != null) {
						for (final com.ibm.scas.analytics.beans.GatewayMember beanMember : gw.getGatewayMembers()) {
							final com.ibm.scas.analytics.persistence.beans.GatewayMember recMember = new com.ibm.scas.analytics.persistence.beans.GatewayMember();
							beanUtilsBean.copyProperties(recMember, beanMember);
							recMembers.add(recMember);
						}
					}
					
					propertyMap.put("gatewayMembers", recMembers);				
				
					// everything else is the same
					beanUtilsBean.populate(toReturn, propertyMap);				
				} else if (type == com.ibm.scas.analytics.beans.Gateway.class &&
						value instanceof com.ibm.scas.analytics.persistence.beans.Gateway) {
					// from bean to record
					final Map<String, Object> propertyMap = beanUtilsBean.getPropertyUtils().describe(value);
					
					// change location to string
					final com.ibm.scas.analytics.persistence.beans.Gateway gw = (com.ibm.scas.analytics.persistence.beans.Gateway)value;
					propertyMap.put("location", gw.getLocation().getName());
					// string to GatewayType
					propertyMap.put("type", GatewayType.valueOf((String) propertyMap.get("type")));
					
					// create an array of GatewayMembers
					final Collection<com.ibm.scas.analytics.beans.GatewayMember> beanMembers = new ArrayList<com.ibm.scas.analytics.beans.GatewayMember>();
					if (gw.getGatewayMembers() != null) {
						for (final com.ibm.scas.analytics.persistence.beans.GatewayMember recMember : gw.getGatewayMembers()) {
							final com.ibm.scas.analytics.beans.GatewayMember beanMember = new com.ibm.scas.analytics.beans.GatewayMember();
							beanUtilsBean.copyProperties(beanMember, recMember);
							beanMembers.add(beanMember);
						}
					}
					propertyMap.put("gatewayMembers", beanMembers);
					
					// everything else is the same
					beanUtilsBean.populate(toReturn, propertyMap);																
				} else if (type == com.ibm.scas.analytics.persistence.beans.Cluster.class &&
						value instanceof com.ibm.scas.analytics.beans.Cluster) {
					// from record to bean
					final Map<String, Object> propertyMap = beanUtilsBean.getPropertyUtils().describe(value);
					
					// convert ClusterStep from a string
					if (propertyMap.get("currentStep") == null) {
						propertyMap.put("currentStep", ClusterStep.NONE);
					} else {
						propertyMap.put("currentStep", ((ClusterStep)propertyMap.get("currentStep")).name());
					}
					
					// everything else is the same
					beanUtilsBean.populate(toReturn, propertyMap);				
				} else if (type == com.ibm.scas.analytics.beans.Cluster.class &&
						value instanceof com.ibm.scas.analytics.persistence.beans.Cluster) {
					// from bean to record
					final Map<String, Object> propertyMap = beanUtilsBean.getPropertyUtils().describe(value);
					
					// change cluster step string to enum
					final com.ibm.scas.analytics.persistence.beans.Cluster c = (com.ibm.scas.analytics.persistence.beans.Cluster)value;
					propertyMap.put("currentStep", c.getCurrentStep() != null ? ClusterStep.valueOf(c.getCurrentStep()): ClusterStep.NONE);
				
					// everything else is the same
					beanUtilsBean.populate(toReturn, propertyMap);																				
				} else if (type == com.ibm.scas.analytics.persistence.beans.VPNTunnel.class &&
						value instanceof com.ibm.scas.analytics.beans.VPNTunnel) {
					// from bean to record
					final Map<String, Object> propertyMap = beanUtilsBean.getPropertyUtils().describe(value);
					
					// convert the enum map to string map
					final com.ibm.scas.analytics.beans.VPNTunnel vpnTunnelBean = (com.ibm.scas.analytics.beans.VPNTunnel) value;
					final Map<VPNParamNames, String> params = vpnTunnelBean.getParams();
					final Map<String, String> dbParams = new HashMap<String, String>();
					for (final Entry<VPNParamNames, String> ent : params.entrySet()) {
						dbParams.put(ent.getKey().name(), ent.getValue());
					}
					
					propertyMap.put("params", dbParams);
				
					// everything else is the same
					beanUtilsBean.populate(toReturn, propertyMap);				
				} else if (type == com.ibm.scas.analytics.beans.VPNTunnel.class &&
						value instanceof com.ibm.scas.analytics.persistence.beans.VPNTunnel) {
					// from record to bean
					final Map<String, Object> propertyMap = beanUtilsBean.getPropertyUtils().describe(value);
					final com.ibm.scas.analytics.beans.VPNTunnel vpnTunnel = (com.ibm.scas.analytics.beans.VPNTunnel)toReturn; 
					
					// convert the string map to enum map
					final com.ibm.scas.analytics.persistence.beans.VPNTunnel vpnTunnelRec = (com.ibm.scas.analytics.persistence.beans.VPNTunnel) value;
					final Map<String, String> params = vpnTunnelRec.getParams();
					final Map<VPNParamNames, String> beanParams = new EnumMap<VPNParamNames, String>(VPNParamNames.class);
					for (final Entry<String, String> ent : params.entrySet()) {
						beanParams.put(VPNParamNames.valueOf(ent.getKey()), ent.getValue());
					}
					vpnTunnel.getParams().putAll(beanParams);
					propertyMap.remove("params");
					
					if (vpnTunnelRec.getGateway() != null) {
						// read only gateway property
						propertyMap.put("gatewayId", vpnTunnelRec.getGateway().getId());
						propertyMap.remove("gateway");
					}				
			
					// everything else is the same
					beanUtilsBean.populate(toReturn, propertyMap);																								
				} else {
					// copy all properties
					beanUtilsBean.copyProperties(toReturn, value);
				}

				return toReturn;
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			}

			return null;
		}

	}


}
