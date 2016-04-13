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

import java.util.List;

import com.vmware.common.annotations.Action;
import com.vmware.common.annotations.Sample;
import com.vmware.pbm.InvalidArgumentFaultMsg;
import com.vmware.pbm.PbmProfile;
import com.vmware.pbm.PbmProfileId;
import com.vmware.spbm.connection.ConnectedServiceBase;
import com.vmware.spbm.connection.helpers.PbmUtil;
import com.vmware.vim25.RuntimeFaultFaultMsg;

/**
 * <pre>
 * ListProfiles
 *
 * This sample lists the storage profiles and their basic information
 *
 * <b>Parameters:</b>
 * url              [required] : url of the web service
 * username         [required] : username for the authentication
 * password         [required] : password for the authentication
 * profilename      [required] : Name of the storage profile
 *
 * <b>Command Line:</b>
 * run.bat com.vmware.spbm.samples.ListProfiles --url [webserviceurl]
 * --username [username] --password [password]
 * --profilename [Storage Profile Name]
 * </pre>
 */
@Sample(name = "listprofiles", description = "Lists the storage profiles and their basic information")
public class ListProfiles extends ConnectedServiceBase {

   /**
    * Method retrieves the storage profiles created in the system.
    *
    * @return {@link List} of {@link PbmProfile} present in the system
    * @throws RuntimeFaultFaultMsg
    * @throws com.vmware.pbm.RuntimeFaultFaultMsg
    * @throws InvalidArgumentFaultMsg
    */
   @Action
   public void listProfiles() throws RuntimeFaultFaultMsg,
         com.vmware.pbm.RuntimeFaultFaultMsg, InvalidArgumentFaultMsg {

      // Retrieve the list of storage profile ID's
      List<PbmProfileId> profileIds =
            connection.getPbmPort().pbmQueryProfile(
                  connection.getPbmServiceContent().getProfileManager(),
                  PbmUtil.getStorageResourceType(), null);
      System.out.println("No. of storage profiles are " + profileIds.size());

      if (!profileIds.isEmpty()) {
         // Fetch more details about the retrieved profiles
         List<PbmProfile> profiles =
               connection.getPbmPort().pbmRetrieveContent(
                     connection.getPbmServiceContent().getProfileManager(),
                     profileIds);
         for (PbmProfile profile : profiles) {
            System.out.println("------------");
            System.out.println("Profile Name: " + profile.getName());
            System.out.println("Profile Id: "
                  + profile.getProfileId().getUniqueId());
            System.out.println("Description: " + profile.getDescription());
            System.out.println("Created by: " + profile.getCreatedBy());
            System.out.println("Created at: " + profile.getCreationTime());
         }
      }
   }

}
