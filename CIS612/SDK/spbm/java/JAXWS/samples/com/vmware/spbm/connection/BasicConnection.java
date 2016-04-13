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

import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;

import org.w3c.dom.Element;

import com.vmware.common.Main;
import com.vmware.common.annotations.Option;
import com.vmware.common.ssl.TrustAll;
import com.vmware.pbm.PbmPortType;
import com.vmware.pbm.PbmService;
import com.vmware.pbm.PbmServiceInstanceContent;
import com.vmware.sso.client.samples.AcquireHoKTokenByUserCredentialSample;
import com.vmware.sso.client.utils.SecurityUtil;
import com.vmware.vim25.DynamicProperty;
import com.vmware.vim25.InvalidLocaleFaultMsg;
import com.vmware.vim25.InvalidLoginFaultMsg;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.ObjectContent;
import com.vmware.vim25.ObjectSpec;
import com.vmware.vim25.PropertyFilterSpec;
import com.vmware.vim25.PropertySpec;
import com.vmware.vim25.RetrieveOptions;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.ServiceContent;
import com.vmware.vim25.UserSession;
import com.vmware.vim25.VimPortType;
import com.vmware.vim25.VimService;
import com.vmware.vsphere.samples.LoginByTokenSample;

/**
 * This is the basic connection implementation that establishes an authenticated
 * session with the STS Service, VC Service, and SPBM service in that order
 * 
 * SPBM utilizes the per-authenticated http session cookie from the vCenter
 * server for its authentication.
 * 
 * The implementation provides Login functionality as follows:
 * 
 * 1. Uses the sample {@link AcquireHoKTokenByUserCredentialSample} from the
 * ssoclient sample set for acquiring a HoK token
 * 
 * 2. Use this HoK token to perform LoginByToken using another sample
 * {@link LoginByTokenSample} from the ssoclient sample set to establish
 * authenticated session
 * 
 * 3. Uses the authenticates session cookie from the established session and
 * sets the PbmPortType to use the same session to make further SPBM API calls
 * 
 * @see ConnectedServiceBase
 */
public class BasicConnection implements Connection {

   private static final String VIMSERVICEINSTANCETYPE = "ServiceInstance";
   private static final String VIMSERVICEINSTANCEVALUE = "ServiceInstance";
   private static final String PBMSERVICEINSTANCETYPE = "PbmServiceInstance";
   private static final String PBMSERVICEINSTANCEVALUE = "ServiceInstance";
   private final static String SSO_URL = "sso.url";
   private VimService vimService = new VimService();
   private VimPortType vimPort = vimService.getVimPort();
   private ServiceContent vimServiceContent;
   private UserSession userSession;

   private ManagedObjectReference vimSvcInstRef;
   private ManagedObjectReference pbmSvcInstRef;
   private URL spbmurl;
   private URL vcurl;
   private String username;
   private String password = ""; // default password is empty since on rare
   // occasion passwords are not set
   private PbmService pbmService = new PbmService();
   private PbmPortType pbmPort = pbmService.getPbmPort();
   private PbmServiceInstanceContent pbmServiceContent;
   private URL ssoUrl;

   @SuppressWarnings({ "unchecked" })
   private void _connect() throws RuntimeFaultFaultMsg, InvalidLocaleFaultMsg,
         InvalidLoginFaultMsg, com.vmware.pbm.RuntimeFaultFaultMsg,
         InvalidPropertyFaultMsg {
      System.out.println("Initiating connection...");
      System.out.println("========================");
      // Connect through the SSO Service and retrieve the token
      String cookieVal =
            ssoConnect(getSsoURL().toString(), username, password,
                  vcurl.toString());
      // Set the extracted cookie into both the VimPortType and PbmPortType

      // VimPortType
      vimService = new VimService();
      vimPort = vimService.getVimPort();
      Map<String, Object> ctxt =
            ((BindingProvider) vimPort).getRequestContext();
      ctxt.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, vcurl.toString());
      ctxt.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);
      Map<String, List<String>> headers =
            (Map<String, List<String>>) ctxt
                  .get(MessageContext.HTTP_REQUEST_HEADERS);
      if (headers == null) {
         headers = new HashMap<String, List<String>>();
      }
      headers.put("Cookie", Arrays.asList(cookieVal));
      ctxt.put(MessageContext.HTTP_REQUEST_HEADERS, headers);
      vimServiceContent =
            vimPort.retrieveServiceContent(this
                  .getVimServiceInstanceReference());
      // Retrieving the current user session
      userSession =
            (UserSession) entityProps(vimServiceContent.getSessionManager(),
                  new String[] { "currentSession" }).get("currentSession");

      // PbmPortType
      // Need to extract only the cookie value
      String[] tokens = cookieVal.split(";");
      tokens = tokens[0].split("=");
      String extractedCookie = tokens[1];
      pbmService = new PbmService();
      // Setting the header resolver for adding the VC session cookie to the
      // requests for authentication
      HeaderHandlerResolver headerResolver = new HeaderHandlerResolver();
      headerResolver.addHandler(new VcSessionHandler(extractedCookie));
      pbmService.setHandlerResolver(headerResolver);

      // An alternate way to add the header for VCSession is by using few internal JDK libraries. 
      // Please use these at your own risk. If you choose to do so you can comment the 3 lines of 
      // code above and use the following instead
      /* 
       * import com.sun.xml.internal.ws.api.message.Headers;
       * import com.sun.xml.internal.ws.developer.WSBindingProvider;
       * WSBindingProvider bp = (WSBindingProvider) pbmPort;
       * bp.setOutboundHeaders(Headers.create(new javax.xml.namespace.QName("vcSessionCookie"), extractedCookie));
       * bp.setAddress(spbmurl.toString());
       */

      pbmPort = pbmService.getPbmPort();
      Map<String, Object> pbmCtxt =
            ((BindingProvider) pbmPort).getRequestContext();
      pbmCtxt.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);
      pbmCtxt
            .put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, spbmurl.toString());
      pbmServiceContent =
            pbmPort.pbmRetrieveServiceContent(getPbmServiceInstanceReference());
      System.out.println("Connection established...");
      System.out.println("=========================\n");
   }

   /**
    * Method to retrieve properties of a {@link ManagedObjectReference}
    * 
    * @param entityMor
    *           {@link ManagedObjectReference} of the entity
    * @param props
    *           Array of properties to be looked up
    * @return Map of the property name and its corresponding value
    * @throws InvalidPropertyFaultMsg
    *            If a property does not exist
    * @throws RuntimeFaultFaultMsg
    */
   private Map<String, Object> entityProps(ManagedObjectReference entityMor,
         String[] props) throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {

      final HashMap<String, Object> retVal = new HashMap<String, Object>();

      // Create Property Spec
      PropertySpec propertySpec = new PropertySpec();
      propertySpec.setAll(Boolean.FALSE);
      propertySpec.setType(entityMor.getType());
      propertySpec.getPathSet().addAll(Arrays.asList(props));
      // Now create Object Spec
      ObjectSpec objectSpec = new ObjectSpec();
      objectSpec.setObj(entityMor);

      PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
      propertyFilterSpec.getPropSet().add(propertySpec);
      propertyFilterSpec.getObjectSet().add(objectSpec);

      List<ObjectContent> oCont =
            vimPort.retrievePropertiesEx(
                  vimServiceContent.getPropertyCollector(),
                  Arrays.asList(propertyFilterSpec), new RetrieveOptions())
                  .getObjects();

      if (oCont != null) {
         for (ObjectContent oc : oCont) {
            List<DynamicProperty> dps = oc.getPropSet();
            for (DynamicProperty dp : dps) {
               retVal.put(dp.getName(), dp.getVal());
            }
         }
      }
      return retVal;
   }

   /**
    * Invokes the {@link AcquireHoKTokenByUserCredentialSample} sample to
    * acquire a HoK token followed by invoking {@link LoginByTokenSample} to
    * perform the login to the vcenter server using the acquired token
    * 
    * @param args
    * @return
    */
   private String ssoConnect(String... args) {
      try {
         // Generating a self-signed certificate and private key pair to
         // be used in the sample. This is to be used for ONLY development 
         // purpose.
         // For production environment please use proper certificate and 
         // private keys. See {@link AcquireHoKTokenByUserCredentialSample}
         // for more details.
         SecurityUtil userCert = SecurityUtil.generateKeyCertPair();
         Element token =
               AcquireHoKTokenByUserCredentialSample.getToken(args,
                     userCert.getPrivateKey(), userCert.getUserCert());
         return LoginByTokenSample.loginUsingSAMLToken(token, args[3],
               userCert.getPrivateKey(), userCert.getUserCert());
      } catch (Exception e) {
         throw new ConnectionException(
               "Error connecting to the vCenter Server", e);
      }
   }

   @Override
   public Connection connect() {
      if (!isConnected()) {
         try {
            _connect();
         } catch (Exception e) {
            Throwable cause = (e.getCause() != null) ? e.getCause() : e;
            throw new ConnectionException("failed to connect: "
                  + e.getMessage() + " : " + cause.getMessage(), cause);
         }
      }
      return this;
   }

   @Override
   public Connection disconnect() {
      if (this.isConnected()) {
         try {
            vimPort.logout(vimServiceContent.getSessionManager());
         } catch (Exception e) {
            Throwable cause = e.getCause();
            throw new ConnectionException("failed to disconnect properly: "
                  + e.getMessage() + " : " + cause.getMessage(), cause);
         } finally {
            // A connection is very memory intensive, I'm helping the
            // garbage collector here
            userSession = null;
            vimServiceContent = null;
            vimPort = null;
            vimService = null;
            pbmPort = null;
            pbmService = null;
         }
      }
      return this;
   }

   /**
    * Generates a default SSO URL to use if none was supplied on the command
    * line. This will attempt to use the system properties <code>sso.host</code>
    * <code>sso.port</code> and <code>sso.path</code> to construct a URL for the
    * SSO server. These properties are all optional.
    * <p>
    * If no value is set <em>sso.host</em> will default to the url of the WS
    * server (assuming SSO and WS are hosted on the same IP)
    * </p>
    * <p>
    * If no value is set <em>sso.port</em> will default to 7444
    * </p>
    * <p>
    * If no value is set, <em>sso.path</em> will default to
    * <code>/ims/STSService</code> which <i>may not</i> be correct.
    * </p>
    * 
    * @return the URL to the SSO server to try
    */
   protected String getDefaultSsoUrl() {
      String host = System.getProperty("sso.host", vcurl.getHost());
      String port = System.getProperty("sso.port", "7444");
      String path = System.getProperty("sso.path", "/ims/STSService");
      return String.format("https://%s:%s%s", host, port, path);
   }

   @Override
   public String getPassword() {
      return password;
   }

   @Override
   public PbmPortType getPbmPort() {
      return pbmPort;
   }

   @Override
   public PbmService getPbmService() {
      return pbmService;
   }

   @Override
   public PbmServiceInstanceContent getPbmServiceContent() {
      return pbmServiceContent;
   }

   @Override
   public ManagedObjectReference getPbmServiceInstanceReference() {
      if (pbmSvcInstRef == null) {
         ManagedObjectReference ref = new ManagedObjectReference();
         ref.setType(PBMSERVICEINSTANCETYPE);// "PbmServiceInstance"
         ref.setValue(PBMSERVICEINSTANCEVALUE);
         pbmSvcInstRef = ref;
      }
      return pbmSvcInstRef;
   }

   @Override
   public URL getSpbmURL() {
      if (this.spbmurl == null) {
         try {
            this.spbmurl =
                  new URL(getVcURL().toString().replace("/sdk", "/pbm"));
         } catch (MalformedURLException e) {
            throw new ConnectionMalformedUrlException(
                  "malformed URL argument: '" + spbmurl + "'", e);
         }
      }
      return this.spbmurl;
   }

   /**
    * Will attempt to return the SSO URL you set from the command line, if you
    * forgot or didn't set one it will call getDefaultSsoUrl to attempt to
    * calculate what the URL should have been.
    * 
    * @return the URL for the SSO services
    * @throws MalformedURLException
    */
   @Override
   public URL getSsoURL() {
      if (ssoUrl != null) {
         return ssoUrl;
      }
      String ssoUrlString = System.getProperty(SSO_URL, getDefaultSsoUrl());
      try {
         ssoUrl = new URL(ssoUrlString);
      } catch (MalformedURLException e) {
         throw new ConnectionMalformedUrlException("malformed URL argument: '"
               + spbmurl + "'", e);
      }
      return ssoUrl;
   }

   @Override
   public String getUsername() {
      return username;
   }

   @Override
   public UserSession getUserSession() {
      return userSession;
   }

   @Override
   public URL getVcURL() {
      return this.vcurl;
   }

   @Override
   public VimPortType getVimPort() {
      return vimPort;
   }

   @Override
   public VimService getVimService() {
      return vimService;
   }

   @Override
   public ServiceContent getVimServiceContent() {
      return vimServiceContent;
   }

   @Override
   public ManagedObjectReference getVimServiceInstanceReference() {
      if (vimSvcInstRef == null) {
         ManagedObjectReference ref = new ManagedObjectReference();
         ref.setType(VIMSERVICEINSTANCETYPE);
         ref.setValue(VIMSERVICEINSTANCEVALUE);
         vimSvcInstRef = ref;
      }
      return vimSvcInstRef;
   }

   @Override
   public boolean isConnected() {
      if (userSession == null) {
         return false;
      }
      long startTime =
            userSession.getLastActiveTime().toGregorianCalendar().getTime()
                  .getTime();
      // 30 minutes in milliseconds = 30 minutes * 60 seconds * 1000
      // milliseconds
      return new Date().getTime() < startTime + 30 * 60 * 1000;
   }

   @Override
   public void setIgnoreCert(Boolean ignorecert) {
      System.setProperty(Main.Properties.TRUST_ALL, ignorecert.toString());
      try {
         TrustAll.trust();
      } catch (KeyManagementException e) {
         e.printStackTrace();
      } catch (NoSuchAlgorithmException e) {
         e.printStackTrace();
      }
   }

   @Override
   public void setPassword(String password) {
      this.password = password;
   }

   @Override
   public void setSpbmUrl(String spbmurl) {
      try {
         this.spbmurl = new URL(spbmurl);
      } catch (MalformedURLException e) {
         throw new ConnectionMalformedUrlException("malformed URL argument: '"
               + spbmurl + "'", e);
      }
   }

   @Override
   public void setSsoUrl(String ssourl) {
      try {
         this.ssoUrl = new URL(ssourl);
      } catch (MalformedURLException e) {
         throw new ConnectionMalformedUrlException("malformed URL argument: '"
               + spbmurl + "'", e);
      }
   }

   @Override
   public void setUsername(String username) {
      this.username = username;
   }

   @Option(name = "vcurl", systemProperty = "vimService.url", description = "full url to the vSphere WS SDK service on vCenter")
   @Override
   public void setVcUrl(String vcurl) {
      try {
         this.vcurl = new URL(vcurl);
      } catch (MalformedURLException e) {
         throw new ConnectionMalformedUrlException("malformed URL argument: '"
               + vcurl + "'", e);
      }
   }

}
