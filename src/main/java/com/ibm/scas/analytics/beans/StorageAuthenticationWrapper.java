/*
 *  
 * Licensed Materials - Property of IBM 
 *  
 * Restricted Materials of IBM 
 *  
 * (C) COPYRIGHT International Business Machines Corp. 2014 
 * All Rights Reserved 
 *  
 * US Government Users Restricted Rights - Use, duplication or 
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp. 
 *
 */

package com.ibm.scas.analytics.beans;

/**
 * This class is used for correct hierarchy mapping of
 * authentication model and java code
 */
public class StorageAuthenticationWrapper {

  /**
   * authentication response field
   */
  private Storage storage;

  /**
   * @return authentication response
   */
  public Storage getStorage() {
    return storage;
  }

  /**
   * @param access sets authentication response
   */
  public void setStorage(Storage storage) {
    this.storage = storage;
  }
}
