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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import com.vmware.common.annotations.After;
import com.vmware.common.annotations.Before;
import com.vmware.common.annotations.Option;

/**
 * An abstract base class, extend this class if your common needs to open a
 * connection to the virtual center server before it can do anything useful.
 * <p/>
 * Example: The simplest possible extension class merely forms a connection and
 * specifies it's own common action.
 * <p/>
 * 
 * <pre>
 * &#064;Sample(name = &quot;connect&quot;)
 * public class Connect extends ConnectedServiceBase {
 *    &#064;Action
 *    public void action() {
 *       System.out.println(&quot;currently connected: &quot; + this.isConnected());
 *    }
 * }
 * </pre>
 * <p/>
 * This is provided as an alternative to extending the Connection class
 * directly.
 * <p/>
 * For a very simple connected sample:
 * 
 * @see com.vmware.general.GetCurrentTime
 */
public abstract class ConnectedServiceBase {

   protected Connection connection;

   // Setting up the connection to be BasicConnection as there is only a single
   // choice
   static {
      System.setProperty("com.vmware.spbm.connection.Connection",
            "com.vmware.spbm.connection.BasicConnection");
   }

   /**
    * A method for dependency injection of the connection object.
    * <p/>
    * 
    * @param connect
    *           the connection object to use for this POJO
    * @see com.vmware.connection.Connection
    */
   @Option(name = "connection", type = BasicConnection.class)
   public void setConnection(Connection connect) {
      this.connection = connect;
   }

   /**
    * connects this object, returns true on successful connect
    * 
    * @return true on successful connect
    * @throws ConnectionException
    */
   @Before
   public boolean start() {

      if (validateConnection()) {
         this.connection.connect();
      } else {
         // not the best form, but without a connection these samples are
         // pointless.
         System.err.println("No valid connection available. Exiting now.");
         System.exit(0);
      }
      return this.connection.isConnected();
   }

   /**
    * disconnects this object and returns true on successful disconnect
    * 
    * @return a disconnected reference to itself
    * @throws ConnectionException
    */
   @After
   public boolean stop() {
      this.connection.disconnect();
      return !this.connection.isConnected();
   }

   public boolean validateConnection() {
      return validateUrl(this.connection.getVcURL())
            && validateUrl(this.connection.getSpbmURL())
            && validateUrl(this.connection.getSsoURL());
   }

   public static boolean testURL(final URL sourceUrl) throws IOException {
      URL url =
            new URL(sourceUrl.getProtocol(), sourceUrl.getHost(),
                  sourceUrl.getPort(), "/");
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("GET");
      conn.connect();
      return conn.getResponseCode() == 200;
   }

   public static boolean validateUrl(final URL url) {
      boolean good;
      try {
         good = testURL(url);
         if (!good) {
            System.err
                  .printf(
                        "The server at %s did not respond as expected. Is this a valid URL?",
                        url);
         }
      } catch (IOException e) {
         System.err.printf("Could not connect to %s due to %s%n", url,
               e.getLocalizedMessage());
         good = false;
      }
      return good;
   }

}
