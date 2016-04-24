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

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Class for storage portion of an 
 * authentication response
 */
public class Storage {

  /**
   *  The defaultOption indicates whether to use
   *  the public or private storage url by default
   */
  @JsonProperty(value = "default") 
  private String defaultOption;

  /**
   *  The public URL for storage
   */
  @JsonProperty(value = "public") 
  private String publicURL;

  /**
   *  The private URL for storage
   */
  @JsonProperty(value = "private")
  private String privateURL;

  /**
   * @return default option
   */
  public String getDefaultOption() {
    return defaultOption;
  }

  /**
   * @param defaultOption sets defaultOption
   */
  public void setDefaultOption(String defaultOption) {
    this.defaultOption = defaultOption;
  }

  /**
   * @return public URL 
   */
  public String getPublicURL() {
    return publicURL;
  }

  /**
   * @param publicURL sets publicURL
   */
  public void setPublicURL(String publicURL) {
    this.publicURL = publicURL;
  }

  /**
   * @return private URL 
   */
  public String getPrivateURL() {
    return privateURL;
  }

  /**
   * @param privateURL sets privateURL
   */
  public void setPrivateURL(String privateURL) {
    this.privateURL = privateURL;
  }
}
