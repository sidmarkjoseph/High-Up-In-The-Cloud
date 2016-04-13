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
import com.vmware.pbm.PbmCapabilityConstraintInstance;
import com.vmware.pbm.PbmCapabilityInstance;
import com.vmware.pbm.PbmCapabilityMetadata;
import com.vmware.pbm.PbmCapabilityMetadataPerCategory;
import com.vmware.pbm.PbmCapabilityProfileCreateSpec;
import com.vmware.pbm.PbmCapabilityPropertyInstance;
import com.vmware.pbm.PbmCapabilitySubProfile;
import com.vmware.pbm.PbmCapabilitySubProfileConstraints;
import com.vmware.pbm.PbmCapabilityVendorNamespaceInfo;
import com.vmware.pbm.PbmCapabilityVendorResourceTypeInfo;
import com.vmware.pbm.PbmDuplicateNameFaultMsg;
import com.vmware.pbm.PbmFaultProfileStorageFaultFaultMsg;
import com.vmware.pbm.PbmProfileId;
import com.vmware.pbm.PbmServiceInstanceContent;
import com.vmware.spbm.connection.ConnectedServiceBase;
import com.vmware.spbm.connection.helpers.PbmUtil;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.RuntimeFaultFaultMsg;

/**
 * <pre>
 * CreateVSANProfile
 *
 * This sample creates a new Storage Profile with one rule-set based on vSAN Capabilities.
 *
 * <b>Parameters:</b>
 * url              [required] : url of the web service
 * username         [required] : username for the authentication
 * password         [required] : password for the authentication
 * profilename      [required] : name of the storage profile
 * stripewidth      [required] : minimum stripe width of each mirror
 * forceprovision   [optional] : if set, the object will be provisioned even if the policy is not satisfiable
 *
 * <b>Command Line:</b>
 * run.bat com.vmware.spbm.samples.CreateVSANProfile --url [webserviceurl]
 * --username [username] --password [password]
 * --profilename [Storage Profile Name]
 * --stripewidth [Stripe Width]
 * --forceprovision
 * </pre>
 */
@Sample(name = "createprofile", description = "Creates a new storage profile with one rule-set based on vSAN capabilities.")
public class CreateVSANProfile extends ConnectedServiceBase {

   private PbmServiceInstanceContent spbmsc;
   private Integer stripeWidth;
   private String profileName;
   private Boolean forceProvision = false;

   /**
    * This method builds a capability instance based on the capability name
    * associated with a vSAN provider
    *
    * @param capabilityName
    * @param value
    * @param metadata
    * @return
    * @throws InvalidArgumentFaultMsg
    */
   PbmCapabilityInstance buildCapability(String capabilityName, Object value,
         List<PbmCapabilityMetadataPerCategory> metadata)
         throws InvalidArgumentFaultMsg {

      // Create Property Instance with capability stripeWidth
      PbmCapabilityMetadata capabilityMeta =
            PbmUtil.getCapabilityMeta(capabilityName, metadata);
      if (capabilityMeta == null)
         throw new InvalidArgumentFaultMsg(
               "Specified Capability does not exist", null);

      // Create a New Property Instance based on the Stripe Width Capability
      PbmCapabilityPropertyInstance prop = new PbmCapabilityPropertyInstance();
      prop.setId(capabilityName);
      prop.setValue(value);

      // Associate Property Instance with a Rule
      PbmCapabilityConstraintInstance rule =
            new PbmCapabilityConstraintInstance();
      rule.getPropertyInstance().add(prop);


      // Associate Rule with a Capability Instance
      PbmCapabilityInstance capability = new PbmCapabilityInstance();
      capability.setId(capabilityMeta.getId());
      capability.getConstraint().add(rule);

      return capability;
   }

   @Action
   public void createProfile() throws RuntimeFaultFaultMsg,
         com.vmware.pbm.RuntimeFaultFaultMsg, InvalidArgumentFaultMsg,
         PbmDuplicateNameFaultMsg, PbmFaultProfileStorageFaultFaultMsg {

      // Get PBM Profile Manager & Associated Capability Metadata
      spbmsc = connection.getPbmServiceContent();
      ManagedObjectReference profileMgr = spbmsc.getProfileManager();

      // Step 1: Check if there is a vSAN Provider
      Boolean vSanCapabale = false;
      List<PbmCapabilityVendorResourceTypeInfo> vendorInfo =
            connection.getPbmPort().pbmFetchVendorInfo(profileMgr, null);
      for (PbmCapabilityVendorResourceTypeInfo vendor : vendorInfo)
         for (PbmCapabilityVendorNamespaceInfo vnsi : vendor
               .getVendorNamespaceInfo())
            if (vnsi.getNamespaceInfo().getNamespace().equals("vSan")) {
               vSanCapabale = true;
               break;
            }

      if (!vSanCapabale)
         throw new RuntimeFaultFaultMsg(
               "Cannot create storage profile. vSAN Provider not found.", null);

      // Step 2: Get PBM Supported Capability Metadata
      List<PbmCapabilityMetadataPerCategory> metadata =
            connection.getPbmPort().pbmFetchCapabilityMetadata(profileMgr,
                  PbmUtil.getStorageResourceType(), null);

      // Step 3: Add Provider Specific Capabilities
      List<PbmCapabilityInstance> capabilities =
            new ArrayList<PbmCapabilityInstance>();
      capabilities.add(buildCapability("stripeWidth", stripeWidth, metadata));
      if (forceProvision)
         capabilities.add(buildCapability("forceProvisioning", forceProvision,
               metadata));


      // Step 4: Add Capabilities to a RuleSet
      PbmCapabilitySubProfile ruleSet = new PbmCapabilitySubProfile();
      ruleSet.getCapability().addAll(capabilities);


      // Step 5: Add Rule-Set to Capability Constraints
      PbmCapabilitySubProfileConstraints constraints =
            new PbmCapabilitySubProfileConstraints();
      ruleSet.setName("Rule-Set " + (constraints.getSubProfiles().size() + 1));
      constraints.getSubProfiles().add(ruleSet);

      // Step 6: Build Capability-Based Profile
      PbmCapabilityProfileCreateSpec spec =
            new PbmCapabilityProfileCreateSpec();
      spec.setName(profileName);
      spec.setDescription("Storage Profile Created by SDK Samples. Rule based on vSAN capability");
      spec.setResourceType(PbmUtil.getStorageResourceType());
      spec.setConstraints(constraints);

      // Step 7: Create Storage Profile
      PbmProfileId profile =
            connection.getPbmPort().pbmCreate(profileMgr, spec);
      System.out.println("Profile " + profileName + " created with ID: "
            + profile.getUniqueId());
   }

   @Option(name = "forceprovision", description = "If set, the object will be provisioned even if the policy is not satisfiable", required = false, parameter = false)
   public void setForceProvision(Boolean forceProvision) {
      this.forceProvision = forceProvision;
   }

   @Option(name = "profilename", description = "Name of the storage profile", required = true)
   public void setProfileName(String profileName) {
      this.profileName = profileName;
   }

   @Option(name = "stripewidth", description = "Minimum stripe width of each mirror", required = true)
   public void setStripeWidth(Integer stripeWidth) {
      this.stripeWidth = stripeWidth;
   }

}
