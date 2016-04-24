package com.ibm.scas.analytics.backend.impl.mock;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.log4j.Logger;

import com.ibm.scas.analytics.backend.impl.SoftLayerNetworkService;
import com.ibm.scas.analytics.beans.Gateway;
import com.ibm.scas.analytics.persistence.util.WhereClause;
import com.ibm.scas.analytics.utils.CPEException;
import com.ibm.scas.analytics.utils.CPEParamException;

/**
 * This class fetches the network (vlan, gateway, subnet) information from
 * database
 * 
 * @author rekha_udabale
 * 
 */

public class MockNetworkService extends SoftLayerNetworkService {
	private final static Logger logger = Logger.getLogger(MockNetworkService.class);

    /* (non-Javadoc)
	 * @see com.ibm.scas.analytics.backend.NetworkProvider#deleteGateway(java.lang.String)
	 */
	@Override
	public void deleteGateway(String gatewayId) throws CPEException {
		// note: caller starts the transaction
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("deleteGateway(): %s", gatewayId));
		}

		final com.ibm.scas.analytics.persistence.beans.Gateway gateway = persistence.getObjectById(com.ibm.scas.analytics.persistence.beans.Gateway.class, gatewayId);

		if (gateway == null) {
			throw new CPEParamException(String.format("Gateway %s doesn't exist in the database for deletion", gatewayId));
		}
		
		final Gateway gwObj = this.getGatewaysFromGatewayRecords(Arrays.asList(gateway)).get(0);
		final List<com.ibm.scas.analytics.persistence.beans.Vlan> associatedVlans = persistence.getObjectsBy(com.ibm.scas.analytics.persistence.beans.Vlan.class, new WhereClause("gateway.id", gatewayId));
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("deleteGateway(): Associated VLANs to gateway %s (%s): %s", gatewayId, gwObj.getName(), ReflectionToStringBuilder.toString(associatedVlans)));
		}
		
        if (associatedVlans != null && !associatedVlans.isEmpty()) {       
        	for (final com.ibm.scas.analytics.persistence.beans.Vlan vlan : associatedVlans){
        		try {
        			verifyVlanUsedByCluster(vlan.getId());
        		} catch (CPEException e) {
        			// this is strange.  management gateway has clusters on the VLAN.  
        			throw new CPEException(String.format("Cannot delete gateway %s because associated vlan %s in use: %s", gatewayId, vlan.getId(), e.getLocalizedMessage()), e);
        		}
       		 
        		// When removing the MANAGEMENT gateway, also remove the associated VLANs and Subnets.
        		persistence.deleteObjectById(com.ibm.scas.analytics.persistence.beans.Vlan.class, vlan.getId());
        		deleteSubnet(vlan.getId());
        	}
        }
        
		persistence.deleteObject(com.ibm.scas.analytics.persistence.beans.Gateway.class, gateway);
		logger.info(String.format("Gateway %s (%s) deleted. Return gateway to softlayer from portal if required", gatewayId, gwObj.getName()));
	}
}
