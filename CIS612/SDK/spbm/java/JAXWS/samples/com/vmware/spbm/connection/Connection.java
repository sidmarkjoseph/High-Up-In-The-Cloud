/*
 * ******************************************************
 * Copyright VMware, Inc. 2010-2013.  All Rights Reserved.
 * ******************************************************
 *
 * DISCLAIMER. THIS PROGRAM IS PROVIDED TO YOU "AS IS"
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, WHETHER
 * ORAL OR WRITTEN, EXPRESS OR IMPLIED. THE AUTHOR
 * SPECIFICALLY DISCLAIMS ANY IMPLIED WARRANTIES OR
 * CONDITIONS OF MERCHANTABILITY, SATISFACTORY QUALITY,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.vmware.spbm.connection;

import java.net.URL;

import com.vmware.common.annotations.After;
import com.vmware.common.annotations.Before;
import com.vmware.common.annotations.Option;
import com.vmware.pbm.PbmPortType;
import com.vmware.pbm.PbmService;
import com.vmware.pbm.PbmServiceInstanceContent;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.ServiceContent;
import com.vmware.vim25.UserSession;
import com.vmware.vim25.VimPortType;
import com.vmware.vim25.VimService;

/**
 * This simple object shows how to set up a SPBM connection. It is intended as a
 * utility class for use by Samples that will need to connect before they can do
 * anything useful. This is a light weight POJO that should be very easy to make
 * portable.
 * 
 * @see ConnectedServiceBase
 */
public interface Connection {

   /**
    * Establishes the authenticated connection to the various services
    * 
    * @return
    */
   @Before
   Connection connect();

   /**
    * Disconnects all the services
    * 
    * @return
    */
   @After
   Connection disconnect();

   /**
    * Returns the password used for this connection
    * 
    * @return
    */
   String getPassword();

   /**
    * Returns the {@link PbmPortType} instance for the SPBM service
    * 
    * @return
    */
   PbmPortType getPbmPort();

   /**
    * Returns the {@link PbmService} instance for the SPBM service
    * 
    * @return
    */
   PbmService getPbmService();

   /**
    * Returns the {@link PbmServiceInstanceContent}instance for the SPBM service
    * 
    * @return
    */
   PbmServiceInstanceContent getPbmServiceContent();

   /**
    * Returns the {@link ManagedObjectReference} for the ServiceInstance for the
    * SPBM Service
    * 
    * @return
    */
   ManagedObjectReference getPbmServiceInstanceReference();

   /**
    * Returns the SPBM Service URL
    * 
    * @return
    */
   URL getSpbmURL();

   /**
    * Returns the vCenter Single Sign-On STS service URL
    * 
    * @return
    */
   URL getSsoURL();

   /**
    * Returns the username used for this connection
    * 
    * @return
    */
   String getUsername();

   /**
    * Returns the {@link UserSession} instance associated with the current
    * connection
    * 
    * @return
    */
   UserSession getUserSession();

   /**
    * Returns the vSphere web services URL
    * 
    * @return
    */
   URL getVcURL();

   /**
    * Returns the {@link VimPortType} instance for the vsphere service
    * 
    * @return
    */
   VimPortType getVimPort();

   /**
    * Returns the {@link VimService} instance for the vsphere service
    * 
    * @return
    */
   VimService getVimService();

   /**
    * Returns the {@link ServiceContent} instance for the vsphere service
    * 
    * @return
    */
   ServiceContent getVimServiceContent();

   /**
    * Returns the {@link ManagedObjectReference} for the ServiceInstance for the
    * vsphere service
    * 
    * @return
    */
   ManagedObjectReference getVimServiceInstanceReference();

   /**
    * Returns if an authenticated connection to the vCenter Server is
    * established
    * 
    * @return
    */
   boolean isConnected();

   /**
    * DEV ONLY option to ignore the SSL certificate check.
    * 
    * DO NOT USE THIS OPTION FOR PRODUCTION CODE
    * 
    * @param ignorecert
    */
   @Option(name = "ignorecert", systemProperty = "ssl.trustAll.enabled", description = "Ignore the SSL certificate check", parameter = false, required = false)
   void setIgnoreCert(Boolean ignorecert);

   /**
    * Password for the user to be used to acquire SAML token from the STS
    * Service for accessing vCenter Server
    * 
    * @param password
    */
   @Option(name = "password", systemProperty = "connection.password", description = "password on remote system")
   void setPassword(String password);

   /**
    * SPBM Service URL e.g. https://<vcenter>/pbm
    * 
    * @param spbmurl
    */
   @Option(name = "spbmurl", systemProperty = "spbmService.url", description = "full url to the SPBM service on vCenter", required = false)
   void setSpbmUrl(String spbmurl);

   /**
    * vCenter Single Sign-On STS service URL
    * 
    * @param ssourl
    */
   @Option(name = "ssourl", systemProperty = "sso.url", description = "full url to the STS service", required = false)
   void setSsoUrl(String ssourl);

   /**
    * Username to be used to acquire SAML token from the STS Service for
    * accessing vCenter Server
    * 
    * @param username
    */
   @Option(name = "username", systemProperty = "connection.username", description = "username on remote system")
   void setUsername(String username);

   /**
    * vSphere web services URL e.g https://<vcenter>/sdk
    * 
    * @param vcurl
    */
   @Option(name = "vcurl", systemProperty = "vimService.url", description = "full url to the vSphere WS SDK service on vCenter")
   void setVcUrl(String vcurl);

}
