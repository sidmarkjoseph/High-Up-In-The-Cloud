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

import java.util.ArrayList;
import java.util.List;

import com.vmware.common.annotations.Action;
import com.vmware.common.annotations.Option;
import com.vmware.common.annotations.Sample;
import com.vmware.pbm.InvalidArgumentFaultMsg;
import com.vmware.pbm.PbmCapabilityProfile;
import com.vmware.pbm.PbmDuplicateNameFaultMsg;
import com.vmware.pbm.PbmFaultProfileStorageFaultFaultMsg;
import com.vmware.pbm.PbmProfile;
import com.vmware.pbm.PbmProfileId;
import com.vmware.pbm.PbmProfileOperationOutcome;
import com.vmware.pbm.PbmServiceInstanceContent;
import com.vmware.spbm.connection.ConnectedServiceBase;
import com.vmware.spbm.connection.helpers.PbmUtil;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.RuntimeFaultFaultMsg;

/**
 * <pre>
 * DeleteProfile
 *
 * This sample deletes a Storage Profile.
 *
 * <b>Parameters:</b>
 * url              [required] : url of the web service
 * username         [required] : username for the authentication
 * password         [required] : password for the authentication
 * profilename     [required] : Name of the storage profile
 *
 * <b>Command Line:</b>
 * run.bat com.vmware.spbm.samples.DeleteProfile --url [webserviceurl]
 * --username [username] --password [password]
 * --profilename [Storage Profile Name]
 * </pre>
 */
@Sample(name = "deleteprofile", description = "Deletes a specified storage profile.")
public class DeleteProfile extends ConnectedServiceBase {

   private PbmServiceInstanceContent spbmsc;
   private String profileName;
   private PbmCapabilityProfile profile;

   @Action
   public void deleteProfile() throws RuntimeFaultFaultMsg,
         com.vmware.pbm.RuntimeFaultFaultMsg, InvalidArgumentFaultMsg,
         PbmDuplicateNameFaultMsg, PbmFaultProfileStorageFaultFaultMsg {

      // Step 1: Get PBM Profile Manager & Associated Capability Metadata
      spbmsc = connection.getPbmServiceContent();
      ManagedObjectReference profileMgr = spbmsc.getProfileManager();

      // Step 2: Search for the given Profile Name
      List<PbmProfileId> profileIds =
            connection.getPbmPort().pbmQueryProfile(profileMgr,
                  PbmUtil.getStorageResourceType(), null);
      if (profileIds == null || profileIds.isEmpty())
         throw new RuntimeFaultFaultMsg("No storage Profiles exist.", null);
      List<PbmProfile> pbmProfiles =
            connection.getPbmPort().pbmRetrieveContent(profileMgr, profileIds);
      for (PbmProfile pbmProfile : pbmProfiles)
         if (pbmProfile.getName().equals(profileName))
            profile = (PbmCapabilityProfile) pbmProfile;
      if (profile == null)
         throw new InvalidArgumentFaultMsg(
               "Specified storage profile name does not exist.", null);

      // Step 3: Delete the profile
      List<PbmProfileId> deleteProfiles = new ArrayList<PbmProfileId>();
      deleteProfiles.add(profile.getProfileId());
      List<PbmProfileOperationOutcome> results =
            connection.getPbmPort().pbmDelete(profileMgr, deleteProfiles);

      // Step 4: Check the Outcome
      for (PbmProfileOperationOutcome result : results) {
         if (result.getFault() != null)
            throw new RuntimeFaultFaultMsg("Unable to delete the Profile."
                  + result.getFault().getFault(), null);

      }
      // Step 5: Print the contents of the Profile
      System.out.println("Profile " + profileName + " deleted. ");


   }

   @Option(name = "profilename", description = "Name of the storage profile", required = true)
   public void setProfileName(String profileName) {
      this.profileName = profileName;
   }


}
