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
import com.vmware.common.annotations.Option;
import com.vmware.common.annotations.Sample;
import com.vmware.pbm.InvalidArgumentFaultMsg;
import com.vmware.pbm.PbmCapabilityConstraintInstance;
import com.vmware.pbm.PbmCapabilityDescription;
import com.vmware.pbm.PbmCapabilityDiscreteSet;
import com.vmware.pbm.PbmCapabilityInstance;
import com.vmware.pbm.PbmCapabilityMetadata;
import com.vmware.pbm.PbmCapabilityMetadataPerCategory;
import com.vmware.pbm.PbmCapabilityProfileCreateSpec;
import com.vmware.pbm.PbmCapabilityPropertyInstance;
import com.vmware.pbm.PbmCapabilityPropertyMetadata;
import com.vmware.pbm.PbmCapabilitySubProfile;
import com.vmware.pbm.PbmCapabilitySubProfileConstraints;
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
 * CreateProfile
 *
 * This sample creates a new Tag-Based Storage Profile with one rule-set.
 * The rule-set contains a rule based on tags from a tag-category.
 *
 * <b>Parameters:</b>
 * url              [required] : url of the web service
 * username         [required] : username for the authentication
 * password         [required] : password for the authentication
 * profilename      [required] : Name of the storage profile
 * tag_category     [required] : Category Name of the tags. All tags in this category are added to the rule.
 *
 * <b>Command Line:</b>
 * run.bat com.vmware.spbm.samples.CreateProfile --url [webserviceurl]
 * --username [username] --password [password]
 * --profilename [Storage Profile Name]
 * --tag_category [Category Name]
 * </pre>
 */
@Sample(name = "createprofile", description = "Creates a new storage tag-based storage profile with one rule-set. The rule-set contains a rule based on tags.")
public class CreateProfile extends ConnectedServiceBase {

   private PbmServiceInstanceContent spbmsc;
   private String tagCategoryName;
   private String profileName;

   @Action
   public void createProfile() throws RuntimeFaultFaultMsg,
         com.vmware.pbm.RuntimeFaultFaultMsg, InvalidArgumentFaultMsg,
         PbmDuplicateNameFaultMsg, PbmFaultProfileStorageFaultFaultMsg {

      // Get PBM Profile Manager & Associated Capability Metadata
      spbmsc = connection.getPbmServiceContent();
      ManagedObjectReference profileMgr = spbmsc.getProfileManager();
      // Get PBM Supported Capability Metadata
      List<PbmCapabilityMetadataPerCategory> metadata =
            connection.getPbmPort().pbmFetchCapabilityMetadata(profileMgr,
                  PbmUtil.getStorageResourceType(), null);

      // Step 1: Create Property Instance with tags from the specified Category
      PbmCapabilityMetadata tagCategoryInfo =
            PbmUtil.getTagCategoryMeta(tagCategoryName, metadata);
      if (tagCategoryInfo == null)
         throw new InvalidArgumentFaultMsg(
               "Specified Tag Category does not exist", null);
      // Fetch Property Metadata of the Tag Category
      List<PbmCapabilityPropertyMetadata> propMetaList =
            tagCategoryInfo.getPropertyMetadata();
      PbmCapabilityPropertyMetadata propMeta = propMetaList.get(0);
      // Create a New Property Instance based on the Tag Category ID
      PbmCapabilityPropertyInstance prop = new PbmCapabilityPropertyInstance();
      prop.setId(propMeta.getId());
      // Fetch Allowed Tag Values Metadata
      PbmCapabilityDiscreteSet tagSetMeta =
            (PbmCapabilityDiscreteSet) propMeta.getAllowedValue();
      if (tagSetMeta == null || tagSetMeta.getValues().isEmpty())
         throw new RuntimeFaultFaultMsg("Specified Tag Category '"
               + tagCategoryName + "' does not have any associated tags", null);
      // Create a New Discrete Set for holding Tag Values
      PbmCapabilityDiscreteSet tagSet = new PbmCapabilityDiscreteSet();
      for (Object obj : tagSetMeta.getValues()) {
         tagSet.getValues().add(((PbmCapabilityDescription) obj).getValue());
      }
      prop.setValue(tagSet);


      // Step 2: Associate Property Instance with a Rule
      PbmCapabilityConstraintInstance rule =
            new PbmCapabilityConstraintInstance();
      rule.getPropertyInstance().add(prop);

      // Step 3: Associate Rule with a Capability Instance
      PbmCapabilityInstance capability = new PbmCapabilityInstance();
      capability.setId(tagCategoryInfo.getId());
      capability.getConstraint().add(rule);

      // Step 4: Add Rule to a RuleSet
      PbmCapabilitySubProfile ruleSet = new PbmCapabilitySubProfile();
      ruleSet.getCapability().add(capability);

      // Step 5: Add Rule-Set to Capability Constraints
      PbmCapabilitySubProfileConstraints constraints =
            new PbmCapabilitySubProfileConstraints();
      ruleSet.setName("Rule-Set " + (constraints.getSubProfiles().size() + 1));
      constraints.getSubProfiles().add(ruleSet);

      // Step 6: Build Capability-Based Profile
      PbmCapabilityProfileCreateSpec spec =
            new PbmCapabilityProfileCreateSpec();
      spec.setName(profileName);
      spec.setDescription("Tag Based Storage Profile Created by SDK Samples. Rule based on tags from Category "
            + tagCategoryName);
      spec.setResourceType(PbmUtil.getStorageResourceType());
      spec.setConstraints(constraints);

      // Step 7: Create Storage Profile
      PbmProfileId profile =
            connection.getPbmPort().pbmCreate(profileMgr, spec);
      System.out.println("Profile " + profileName + " created with ID: "
            + profile.getUniqueId());
   }

   @Option(name = "profilename", description = "Name of the storage profile", required = true)
   public void setProfileName(String profileName) {
      this.profileName = profileName;
   }

   @Option(name = "tag_category", description = "Category Name of the tags. All tags in this category are added to the rule.", required = true)
   public void setTagCategoryName(String tagCategoryName) {
      this.tagCategoryName = tagCategoryName;
   }

}
