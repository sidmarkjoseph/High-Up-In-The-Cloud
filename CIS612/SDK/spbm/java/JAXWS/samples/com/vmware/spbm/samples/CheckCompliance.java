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
import com.vmware.pbm.PbmCapabilityProfile;
import com.vmware.pbm.PbmComplianceResult;
import com.vmware.pbm.PbmFaultFaultMsg;
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
 * CheckCompliance
 *
 * This sample checks the compliance of the VM's associated with a storage profile.
 *
 * <b>Parameters:</b>
 * url              [required] : url of the web service
 * username         [required] : username for the authentication
 * password         [required] : password for the authentication
 * profilename      [required] : Name of the storage profile
 *
 * <b>Command Line:</b>
 * run.bat com.vmware.spbm.samples.CheckCompliance --url [webserviceurl]
 * --username [username] --password [password]
 * --profilename [storage profile]
 * </pre>
 */
@Sample(name = "checkcompliance", description = "Check compliance of the VM's associated with a storage profile.")
public class CheckCompliance extends ConnectedServiceBase {

   private PbmServiceInstanceContent spbmsc;
   private String profileName;
   private PbmCapabilityProfile profile;

   /**
    * This method checks the compliance of entities against a profile.
    *
    * @throws InvalidArgumentFaultMsg
    * @throws com.vmware.pbm.RuntimeFaultFaultMsg
    * @throws PbmFaultFaultMsg
    * @throws InvalidPropertyFaultMsg
    */
   @Action
   public void checkProfileCompliance() throws InvalidArgumentFaultMsg,
         com.vmware.pbm.RuntimeFaultFaultMsg, PbmFaultFaultMsg,
         RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {

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

      // Step 3: Retrieve Associated Entities
      List<PbmServerObjectRef> entities =
            connection.getPbmPort().pbmQueryAssociatedEntity(
                  connection.getPbmServiceContent().getProfileManager(),
                  profile.getProfileId(), "virtualMachine");

      // Step 4: Check Compliance Results of associated entities
      if (entities.isEmpty()) {
         System.out.println("Storage Profile should have associated VM's.");
         return;
      }
      List<PbmComplianceResult> complianceResults =
            connection.getPbmPort().pbmCheckCompliance(
                  connection.getPbmServiceContent().getComplianceManager(),
                  entities, profile.getProfileId());
      for (PbmComplianceResult result : complianceResults)
         System.out.println("Compliance status of VM "
               + getVMName(result.getEntity()) + ": "
               + result.getComplianceStatus().toUpperCase());


   }


   /**
    * @param entity
    * @return
    * @throws RuntimeFaultFaultMsg
    * @throws InvalidPropertyFaultMsg
    */
   private String getVMName(PbmServerObjectRef entity)
         throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
      ManagedObjectReference mor = new ManagedObjectReference();
      mor.setType("VirtualMachine");
      mor.setValue(entity.getKey());
      Map<String, Object> propsMap =
            VCUtil.getEntityProps(connection, mor, new String[] { "name" });
      return (String) propsMap.get("name");
   }


   @Option(name = "profilename", description = "name of the storage profile")
   public void setProfileName(String profileName) {
      this.profileName = profileName;
   }

}
