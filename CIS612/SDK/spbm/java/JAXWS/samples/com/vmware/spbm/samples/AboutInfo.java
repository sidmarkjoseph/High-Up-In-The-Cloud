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

package com.vmware.spbm.samples;

import java.text.SimpleDateFormat;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import com.vmware.common.annotations.Action;
import com.vmware.common.annotations.Sample;
import com.vmware.pbm.PbmCapabilityVendorNamespaceInfo;
import com.vmware.pbm.PbmCapabilityVendorResourceTypeInfo;
import com.vmware.spbm.connection.ConnectedServiceBase;
import com.vmware.vim25.RuntimeFaultFaultMsg;

/**
 * <pre>
 * AboutInfo
 *
 * This sample prints the basic information about the spbm end-point.
 *
 * <b>Parameters:</b>
 * url              [required] : url of the web service
 * username         [required] : username for the authentication
 * password         [required] : password for the authentication
 * profilename      [required] : Name of the storage profile
 *
 * <b>Command Line:</b>
 * run.bat com.vmware.spbm.samples.AboutInfo --url [webserviceurl]
 * --username [username] --password [password]
 * </pre>
 */
@Sample(name = "aboutinfo", description = "Displays the basic information about the spbm end-point")
public class AboutInfo extends ConnectedServiceBase {

   /**
    * Executes vim API to print the server time
    *
    * @throws RuntimeFaultFaultMsg
    */
   void executeVimAPI() throws RuntimeFaultFaultMsg {
      XMLGregorianCalendar ct =
            connection.getVimPort().currentTime(
                  connection.getVimServiceInstanceReference());
      SimpleDateFormat sdf =
            new SimpleDateFormat("yyyy-MM-dd 'T' HH:mm:ss.SSSZ");
      System.out.println("Information retrieved at: "
            + sdf.format(ct.toGregorianCalendar().getTime()));
   }

   /**
    * The method exercise both the vim API and spbm API side by side Both these
    * APIs can be executed independently, and no particular order is implied
    *
    * @throws RuntimeFaultFaultMsg
    * @throws com.vmware.pbm.RuntimeFaultFaultMsg
    */
   @Action
   public void printInfo() throws RuntimeFaultFaultMsg,
         com.vmware.pbm.RuntimeFaultFaultMsg {

      executeVimAPI();
      printSpbmAPIInfo();
      printVendorInfo();
   }

   /**
    * Executes the SPBM API to print the endpoint information
    */
   void printSpbmAPIInfo() {
      System.out.println("SPBM Endpoint Information");
      System.out.println(connection.getPbmServiceContent().getAboutInfo()
            .getName()
            + " - "
            + connection.getPbmServiceContent().getAboutInfo().getVersion());
   }

   /**
    * Executes the spbm API to print the supported vendors information
    *
    * @throws com.vmware.pbm.RuntimeFaultFaultMsg
    */
   void printVendorInfo() throws com.vmware.pbm.RuntimeFaultFaultMsg {
      List<PbmCapabilityVendorResourceTypeInfo> vendorInfo =
            connection.getPbmPort().pbmFetchVendorInfo(
                  connection.getPbmServiceContent().getProfileManager(), null);
      System.out.println("The No. of supported vendors are "
            + vendorInfo.size());
      for (PbmCapabilityVendorResourceTypeInfo vendor : vendorInfo) {
         System.out.println("======");
         System.out.println("Resource Type:" + vendor.getResourceType());
         List<PbmCapabilityVendorNamespaceInfo> vNamespaceInfo =
               vendor.getVendorNamespaceInfo();
         for (PbmCapabilityVendorNamespaceInfo vnsi : vNamespaceInfo) {
            System.out.println("vendor UUID: "
                  + vnsi.getVendorInfo().getVendorUuid());
            System.out.println("vendor.info.key: "
                  + vnsi.getVendorInfo().getInfo().getKey());
         }
      }
   }

}
