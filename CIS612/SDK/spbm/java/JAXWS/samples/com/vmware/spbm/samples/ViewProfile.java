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
import java.util.Map;

import com.vmware.common.annotations.Action;
import com.vmware.common.annotations.Option;
import com.vmware.common.annotations.Sample;
import com.vmware.pbm.InvalidArgumentFaultMsg;
import com.vmware.pbm.PbmCapabilityConstraintInstance;
import com.vmware.pbm.PbmCapabilityDiscreteSet;
import com.vmware.pbm.PbmCapabilityInstance;
import com.vmware.pbm.PbmCapabilityProfile;
import com.vmware.pbm.PbmCapabilityPropertyInstance;
import com.vmware.pbm.PbmCapabilityRange;
import com.vmware.pbm.PbmCapabilitySubProfile;
import com.vmware.pbm.PbmCapabilitySubProfileConstraints;
import com.vmware.pbm.PbmDuplicateNameFaultMsg;
import com.vmware.pbm.PbmFaultFaultMsg;
import com.vmware.pbm.PbmFaultProfileStorageFaultFaultMsg;
import com.vmware.pbm.PbmPlacementHub;
import com.vmware.pbm.PbmProfile;
import com.vmware.pbm.PbmProfileId;
import com.vmware.pbm.PbmServerObjectRef;
import com.vmware.pbm.PbmServiceInstanceContent;
import com.vmware.spbm.connection.ConnectedServiceBase;
import com.vmware.spbm.connection.helpers.VCUtil;
import com.vmware.spbm.connection.helpers.PbmUtil;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.RuntimeFaultFaultMsg;

/**
 * <pre>
 * ViewProfile
 *
 * This sample prints the contents of a storage Profile.
 *
 * <b>Parameters:</b>
 * url              [required] : url of the web service
 * username         [required] : username for the authentication
 * password         [required] : password for the authentication
 * profilename      [required] : Name of the storage profile
 *
 * <b>Command Line:</b>
 * run.bat com.vmware.spbm.samples.ViewProfile --url [webserviceurl]
 * --username [username] --password [password]
 * --profilename [Storage Profile Name]
 * </pre>
 */
@Sample(name = "viewprofile", description = "Display the contents of a storage profile.")
public class ViewProfile extends ConnectedServiceBase {

   private PbmServiceInstanceContent spbmsc;
   private String profileName;
   private PbmCapabilityProfile profile;

   @Option(name = "profilename", description = "Name of the storage profile", required = true)
   public void setProfileName(String profileName) {
      this.profileName = profileName;
   }

   @Action
   public void viewProfile() throws RuntimeFaultFaultMsg,
         com.vmware.pbm.RuntimeFaultFaultMsg, InvalidArgumentFaultMsg,
         PbmDuplicateNameFaultMsg, PbmFaultProfileStorageFaultFaultMsg,
         PbmFaultFaultMsg, InvalidPropertyFaultMsg {

      // Step 1: Get PBM Profile Manager & Associated Capability Metadata
      spbmsc = connection.getPbmServiceContent();
      ManagedObjectReference profileMgr = spbmsc.getProfileManager();
      ManagedObjectReference placementSolver = spbmsc.getPlacementSolver();

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

      // Step 3: Print the basic information of the Profile
      System.out.println("Profile Name: " + profile.getName());
      System.out.println("Profile Id: " + profile.getProfileId().getUniqueId());
      System.out.println("Description: " + profile.getDescription());

      // Step 4: Fetch Rulesets defined in the profile
      PbmCapabilitySubProfileConstraints constraints =
            (PbmCapabilitySubProfileConstraints) profile.getConstraints();
      List<PbmCapabilitySubProfile> ruleSets = constraints.getSubProfiles();
      System.out.println("\nNo. of Rule-Sets: " + ruleSets.size());
      System.out.println("List of Rules");
      System.out.println("-------------");
      for (PbmCapabilitySubProfile ruleSet : ruleSets) {
         System.out.println("RuleSet Name: '" + ruleSet.getName() + "'");
         for (PbmCapabilityInstance capability : ruleSet.getCapability())
            for (PbmCapabilityConstraintInstance rule : capability
                  .getConstraint())
               for (PbmCapabilityPropertyInstance prop : rule
                     .getPropertyInstance()) {
                  if (capability.getId().getNamespace().contains("tag")) {
                     System.out.println(" Tag Category: "
                           + capability.getId().getId());
                     System.out.println(" Selected Tags:");
                     PbmCapabilityDiscreteSet tagSet =
                           (PbmCapabilityDiscreteSet) prop.getValue();
                     for (Object tag : tagSet.getValues())
                        System.out.println(" " + tag);
                  }
                  if (capability.getId().getNamespace().contains("vSan")) {
                     System.out.println(" Capability: "
                           + capability.getId().getId());
                     if (capability.getId().getId()
                           .equals("proportionalCapacity")) {
                        PbmCapabilityRange range =
                              (PbmCapabilityRange) prop.getValue();
                        System.out.println(" Min: " + range.getMin()
                              + ", Max: " + range.getMax());
                     } else
                        System.out.println(" Value: " + prop.getValue());
                  }
                  System.out.println(" ---");
               }
      }
      // Step 5: Print Associated VM's
      List<PbmServerObjectRef> entities =
            connection.getPbmPort().pbmQueryAssociatedEntity(profileMgr,
                  profile.getProfileId(), "virtualMachine");
      System.out.println("\nNo. of Associated VM's: " + entities.size());
      if (entities.size() > 0) {
         System.out.println("List of VM's");
         System.out.println("----------- ");
         for (PbmServerObjectRef entity : entities) {
            // Construct MOR
            ManagedObjectReference mor = new ManagedObjectReference();
            mor.setType("VirtualMachine");
            mor.setValue(entity.getKey());
            Map<String, Object> vmname =
                  VCUtil.getEntityProps(connection, mor, new String[] { "name" });
            System.out.println(vmname.get("name"));
         }
      }

      // Step 6: Print Matching Resources (e.g. Datastores)
      List<PbmPlacementHub> hubs =
            connection.getPbmPort().pbmQueryMatchingHub(placementSolver, null,
                  profile.getProfileId());
      System.out.println("\nNo. of Matching Resources: " + hubs.size());
      if (hubs.size() > 0) {
         System.out.println("List of Resources:");
         System.out.println("----------- ");
         for (PbmPlacementHub hub : hubs) {
            // Construct MOR
            ManagedObjectReference mor = new ManagedObjectReference();
            mor.setType(hub.getHubType());
            mor.setValue(hub.getHubId());
            String[] props = { "name" };
            Map<String, Object> hubname = VCUtil.getEntityProps(connection,mor, props);
            System.out.println(hubname.get("name") + " : " + hub.getHubType());
         }
      }
   }


}
