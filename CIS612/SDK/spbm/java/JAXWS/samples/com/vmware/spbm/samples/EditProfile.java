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
import com.vmware.pbm.PbmCapabilityProfile;
import com.vmware.pbm.PbmCapabilityProfileUpdateSpec;
import com.vmware.pbm.PbmCapabilityPropertyInstance;
import com.vmware.pbm.PbmCapabilityPropertyMetadata;
import com.vmware.pbm.PbmCapabilitySubProfile;
import com.vmware.pbm.PbmCapabilitySubProfileConstraints;
import com.vmware.pbm.PbmDuplicateNameFaultMsg;
import com.vmware.pbm.PbmFaultProfileStorageFaultFaultMsg;
import com.vmware.pbm.PbmProfile;
import com.vmware.pbm.PbmProfileId;
import com.vmware.pbm.PbmServiceInstanceContent;
import com.vmware.spbm.connection.ConnectedServiceBase;
import com.vmware.spbm.connection.helpers.PbmUtil;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.RuntimeFaultFaultMsg;

/**
 * <pre>
 * EditProfile
 *
 * This sample updates a Tag-Based Storage Profile. Adds or deletes rulesets.
 * Adds a ruleset based on tags from a tag category
 * Deletes a ruleset from a storage profile.
 *
 * <b>Parameters:</b>
 * url              [required] : url of the web service
 * username         [required] : username for the authentication
 * password         [required] : password for the authentication
 * profilename     [required] : Name of the storage profile
 * add              [optional] : Flag to denote addition of a rule set
 * tag_category     [optional] : Category Name of the tags.
 * delete           [optional] : Flag to denote deletion of a rule set
 * ruleset_name     [optional] : Name of the ruleset
 *
 * <b>Command Line:</b>
 * Update an existing profile with a new ruleset.
 * The rule-set contains a rule based on tags from a tag-category.
 * run.bat com.vmware.spbm.samples.EditProfile --url [webserviceurl]
 * --username [username] --password [password]
 * --profilename [Storage Profile Name]
 * --add --tag_category [Category Name]
 *
 * Update an existing profile by deleting a ruleset from the storage profile
 * run.bat com.vmware.spbm.samples.EditProfile --url [webserviceurl]
 * --username [username] --password [password]
 * --profilename [Storage Profile Name]
 * --delete --ruleset_name [Ruleset Name]
 * </pre>
 */
@Sample(name = "editprofile", description = "Updates a storage profile. Adds a new rule based on tags.")
public class EditProfile extends ConnectedServiceBase {

   private Boolean addRuleSet = false;
   private Boolean delRuleSet = false;
   private String tagCategoryName;
   private String profileName;
   private String ruleSetName;

   private PbmServiceInstanceContent spbmsc;
   private PbmCapabilityProfile profile;

   @Action
   public void editProfile() throws RuntimeFaultFaultMsg,
         com.vmware.pbm.RuntimeFaultFaultMsg, InvalidArgumentFaultMsg,
         PbmDuplicateNameFaultMsg, PbmFaultProfileStorageFaultFaultMsg {

      // Validate Arguments
      if (!addRuleSet && !delRuleSet)
         throw new InvalidArgumentFaultMsg(
               "Either of add or delete argument is required.", null);


      // Step 1: Get PBM Profile Manager & Associated Capability Metadata
      spbmsc = connection.getPbmServiceContent();
      ManagedObjectReference profileMgr = spbmsc.getProfileManager();
      // Get PBM Supported Capability Metadata
      List<PbmCapabilityMetadataPerCategory> metadata =
            connection.getPbmPort().pbmFetchCapabilityMetadata(profileMgr,
                  PbmUtil.getStorageResourceType(), null);

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

      // Step 3: Retrieve Existing RuleSets
      PbmCapabilitySubProfileConstraints constraints =
            (PbmCapabilitySubProfileConstraints) profile.getConstraints();
      List<PbmCapabilitySubProfile> ruleSets = constraints.getSubProfiles();

      // Step 4: Add a Rule-Set based on the tag Category
      if (addRuleSet) {
         if (tagCategoryName == null)
            throw new InvalidArgumentFaultMsg(
                  "Missing tag_category option with add", null);
         // Create Tag Instances based on Category
         PbmCapabilityMetadata tagCategoryInfo =
               PbmUtil.getTagCategoryMeta(tagCategoryName, metadata);
         if (tagCategoryInfo == null)
            throw new InvalidArgumentFaultMsg("Specified Tag Category '"
                  + tagCategoryName + "' does not exist", null);
         // Fetch Metadata for Tag Category
         List<PbmCapabilityPropertyMetadata> propMetaList =
               tagCategoryInfo.getPropertyMetadata();
         PbmCapabilityPropertyMetadata propMeta = propMetaList.get(0);
         // Create a New Property Instance based on the Tag Category ID
         PbmCapabilityPropertyInstance prop =
               new PbmCapabilityPropertyInstance();
         prop.setId(propMeta.getId());
         // Fetch Allowed Tag Values Metadata
         PbmCapabilityDiscreteSet tagSetMeta =
               (PbmCapabilityDiscreteSet) propMeta.getAllowedValue();
         if (tagSetMeta == null || tagSetMeta.getValues().isEmpty())
            throw new RuntimeFaultFaultMsg(
                  "Specified Tag Category does not have any associated tags",
                  null);
         // Create a New Discrete Set for holding Tag Values
         PbmCapabilityDiscreteSet tagSet = new PbmCapabilityDiscreteSet();
         for (Object obj : tagSetMeta.getValues()) {
            tagSet.getValues().add(((PbmCapabilityDescription) obj).getValue());
         }
         prop.setValue(tagSet);

         // Associate Tag Instance with a Rule
         PbmCapabilityConstraintInstance rule =
               new PbmCapabilityConstraintInstance();
         rule.getPropertyInstance().add(prop);

         // Associate Rule with a Capability Instance
         PbmCapabilityInstance capability = new PbmCapabilityInstance();
         capability.setId(tagCategoryInfo.getId());
         capability.getConstraint().add(rule);

         // Add Rule to a RuleSet
         PbmCapabilitySubProfile ruleSet = new PbmCapabilitySubProfile();
         ruleSet.setName("Rule-Set " + (ruleSets.size() + 1));
         ruleSet.getCapability().add(capability);

         // Add Rule-Set to Existing Rule-Sets
         ruleSets.add(ruleSet);
      }


      // Step 5: Delete a specified Rule-Set
      if (delRuleSet) {
         if (ruleSetName == null)
            throw new InvalidArgumentFaultMsg(
                  "Missing ruleset_name option with delete", null);

         PbmCapabilitySubProfile deleteRuleSet = null;
         for (PbmCapabilitySubProfile ruleSet : ruleSets)
            if (ruleSet.getName().equals(ruleSetName))
               deleteRuleSet = ruleSet;

         if (deleteRuleSet == null)
            throw new RuntimeFaultFaultMsg("Specified Rule-Set name "
                  + ruleSetName + " does not exist", null);
         else if (ruleSets.size() == 1)
            throw new RuntimeFaultFaultMsg(
                  "Cannot delete the ruleset. At least one ruleset is required for a profile.",
                  null);
         else
            ruleSets.remove(deleteRuleSet);
      }

      // Step 6: Build Capability-Based Profile Update Spec
      PbmCapabilityProfileUpdateSpec spec =
            new PbmCapabilityProfileUpdateSpec();
      spec.setName(profileName);
      spec.setDescription("Tag Based Storage Profile Created by SDK Samples. Rule based on tags from Category "
            + tagCategoryName);
      spec.setConstraints(constraints);

      // Step 7: Update Storage Profile
      connection.getPbmPort().pbmUpdate(profileMgr, profile.getProfileId(),
            spec);
      System.out.println("Profile " + profileName + " Updated.");
   }

   @Option(name = "add", description = "Flag to denote addition of a rule set. Requires tag_category property to be specified.", parameter = false, required = false)
   public void setAddRuleSetName(Boolean addRuleSet) {
      this.addRuleSet = addRuleSet;
   }

   @Option(name = "delete", description = "Flag to denote deletion of a rule set. Requires ruleset_name property to be specified.", parameter = false, required = false)
   public void setDelRuleSetName(Boolean delRuleSet) {
      this.delRuleSet = delRuleSet;
   }

   @Option(name = "profilename", description = "Name of an existing storage profile", required = true)
   public void setProfileName(String profileName) {
      this.profileName = profileName;
   }

   @Option(name = "ruleset_name", description = "Name of the rule-set to be deleted.", required = false)
   public void setRuleSetName(String ruleSetName) {
      this.ruleSetName = ruleSetName;
   }

   @Option(name = "tag_category", description = "Category Name of the tags. All tags in this category are added to the rule.", required = false)
   public void setTagCategoryName(String tagCategoryName) {
      this.tagCategoryName = tagCategoryName;
   }


}
