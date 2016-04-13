/*
 * ******************************************************
 * Copyright VMware, Inc. 2010-2013.  All Rights Reserved.
 * ******************************************************
 *
 * DISCLAIMER. THIS PROGRAM IS PROVIDED TO YOU "AS IS" WITHOUT
 * WARRANTIES OR CONDITIONS # OF ANY KIND, WHETHER ORAL OR WRITTEN,
 * EXPRESS OR IMPLIED. THE AUTHOR SPECIFICALLY # DISCLAIMS ANY IMPLIED
 * WARRANTIES OR CONDITIONS OF MERCHANTABILITY, SATISFACTORY # QUALITY,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.vmware.spbm.samples;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.vmware.common.annotations.Action;
import com.vmware.common.annotations.Option;
import com.vmware.common.annotations.Sample;
import com.vmware.pbm.InvalidArgumentFaultMsg;
import com.vmware.pbm.PbmCapabilityProfile;
import com.vmware.pbm.PbmFaultFaultMsg;
import com.vmware.pbm.PbmPlacementHub;
import com.vmware.pbm.PbmServiceInstanceContent;
import com.vmware.spbm.connection.ConnectedServiceBase;
import com.vmware.spbm.connection.helpers.VCUtil;
import com.vmware.spbm.connection.helpers.PbmUtil;
import com.vmware.vim25.ArrayOfManagedObjectReference;
import com.vmware.vim25.FileFaultFaultMsg;
import com.vmware.vim25.InsufficientResourcesFaultFaultMsg;
import com.vmware.vim25.InvalidCollectorVersionFaultMsg;
import com.vmware.vim25.InvalidDatastoreFaultMsg;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.InvalidStateFaultMsg;
import com.vmware.vim25.LocalizedMethodFault;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.MigrationFaultFaultMsg;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.TaskInfoState;
import com.vmware.vim25.TimedoutFaultMsg;
import com.vmware.vim25.VirtualMachineDefinedProfileSpec;
import com.vmware.vim25.VirtualMachineRelocateSpec;
import com.vmware.vim25.VmConfigFaultFaultMsg;

/**
 * <pre>
 * VMRelocate
 *
 * Used to relocate a virtual machine's virtual disks to a datastore compliant with the given storage policy
 * based management profile. If multiple datastores are compliant, this sample picks one of them.
 *
 * <b>Parameters:</b>
 * url            [required] : url of the web service
 * username       [required] : username for the authentication
 * password       [required] : password for the authentication
 * vmpath         [required] : inventory path of the VM
 * profilename    [required] : Name of the storage profile
 *
 * <b>Command Line:</b>
 * run.bat com.vmware.vm.VMRelocate --url [URLString] --username [User] --password [Password]
 * --vmpath [vmPath] --profilename [profilename]
 * </pre>
 */
@Sample(name = "vm-relocate", description = "Used to relocate a virtual machine's virtual disks to a datastore "
      + "compliant with the given storage profile. "
      + "If multiple datastores are compliant, this sample picks one of them.")
public class VMRelocate extends ConnectedServiceBase {
   private PbmServiceInstanceContent spbmsc;
   private String vmPathName;
   private String profileName;


   /**
    * This method returns name of a datastore corresponding to a Placement Hub
    *
    * @param hubId
    *           The MO ID of the Datastore
    * @return
    * @throws RuntimeFaultFaultMsg
    * @throws InvalidPropertyFaultMsg
    */
   private String getDSName(PbmPlacementHub hub)
         throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
      ManagedObjectReference ds = new ManagedObjectReference();
      ds.setType(hub.getHubType());
      ds.setValue(hub.getHubId());
      Map<String, Object> propsMap =
            VCUtil.getEntityProps(connection, ds, new String[] { "name" });
      return (String) propsMap.get("name");
   }

   /**
    * This method returns a boolean value specifying whether the Task is
    * succeeded or failed.
    *
    * @param task
    *           ManagedObjectReference representing the Task.
    * @return boolean value representing the Task result.
    * @throws InvalidCollectorVersionFaultMsg
    *
    * @throws RuntimeFaultFaultMsg
    * @throws InvalidPropertyFaultMsg
    */
   private boolean getTaskResultAfterDone(ManagedObjectReference task)
         throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg,
         InvalidCollectorVersionFaultMsg {

      boolean retVal = false;

      // info has a property - state for state of the task
      Object[] result =
            VCUtil.waitForTask(connection, task,
                  new String[] { "info.state", "info.error" },
                  new String[] { "state" }, new Object[][] { new Object[] {
                        TaskInfoState.SUCCESS, TaskInfoState.ERROR } });

      if (result[0].equals(TaskInfoState.SUCCESS)) {
         retVal = true;
      }
      if (result[1] instanceof LocalizedMethodFault) {
         throw new RuntimeException(
               ((LocalizedMethodFault) result[1]).getLocalizedMessage());
      }
      return retVal;
   }


   /**
    * This method returns the VirtualMachineDefinedProfileSpec for a given
    * storage profile name
    *
    * @param profileName
    *           name of the policy based management profile
    * @return
    * @throws InvalidArgumentFaultMsg
    * @throws com.vmware.pbm.RuntimeFaultFaultMsg
    * @throws RuntimeFaultFaultMsg
    */
   private VirtualMachineDefinedProfileSpec getVMDefinedProfileSpec(
         String profileName) throws InvalidArgumentFaultMsg,
         com.vmware.pbm.RuntimeFaultFaultMsg, RuntimeFaultFaultMsg {

      PbmCapabilityProfile profile =
            PbmUtil.getPbmProfile(connection, profileName);
      VirtualMachineDefinedProfileSpec pbmProfile =
            new VirtualMachineDefinedProfileSpec();
      pbmProfile.setProfileId(profile.getProfileId().getUniqueId());
      return pbmProfile;

   }


   void relocate() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg,
         VmConfigFaultFaultMsg, InsufficientResourcesFaultFaultMsg,
         InvalidDatastoreFaultMsg, FileFaultFaultMsg, MigrationFaultFaultMsg,
         InvalidStateFaultMsg, TimedoutFaultMsg,
         InvalidCollectorVersionFaultMsg, InvalidArgumentFaultMsg,
         com.vmware.pbm.RuntimeFaultFaultMsg, PbmFaultFaultMsg {

      // Step 1: Instantiate SPBM Service
      spbmsc = connection.getPbmServiceContent();

      // Step 2: Verify if specified VM Path Exists
      ManagedObjectReference vmRef =
            connection.getVimPort().findByInventoryPath(
                  connection.getVimServiceContent().getSearchIndex(),
                  vmPathName);
      if (vmRef == null) {
         System.out.printf("The VMPath specified [ %s ] is not found %n",
               vmPathName);
         return;
      }

      // Step 3: Get VM's Host
      Map<String, Object> propsMap =
            VCUtil.getEntityProps(connection, vmRef, new String[] { "runtime.host" });
      ManagedObjectReference hostRef =
            (ManagedObjectReference) propsMap.get("runtime.host");

      // Step 4: Get available Datastores in the host
      propsMap = VCUtil.getEntityProps(connection, hostRef, new String[] { "datastore" });
      ArrayOfManagedObjectReference dsRefs =
            (ArrayOfManagedObjectReference) propsMap.get("datastore");

      // Step 5: Populate Allowed Hubs
      List<PbmPlacementHub> allowedHubs = new ArrayList<PbmPlacementHub>();
      for (ManagedObjectReference dsRef : dsRefs.getManagedObjectReference()) {
         PbmPlacementHub hub = new PbmPlacementHub();
         hub.setHubId(dsRef.getValue());
         hub.setHubType(dsRef.getType());
         allowedHubs.add(hub);
      }
      if (allowedHubs.size() == 0) {
         System.out
               .println("Error: There should be at least one datastore available on the current host to relocate.");
         return;
      }


      // Step 6: Get compliant datastores for the given storage profile
      PbmCapabilityProfile pbmProfile =
            PbmUtil.getPbmProfile(connection, profileName);
      List<PbmPlacementHub> hubs =
            connection.getPbmPort().pbmQueryMatchingHub(
                  spbmsc.getPlacementSolver(), allowedHubs,
                  pbmProfile.getProfileId());
      if (hubs.size() == 0) {
         System.out
               .println("Error: No compliant datastores matching the storage profile found on the host");
         return;
      }

      // Step 7: Use the first compliant datastore
      ManagedObjectReference relocDSRef = new ManagedObjectReference();
      PbmPlacementHub targetHub = hubs.get(0);
      relocDSRef.setType(targetHub.getHubType());
      relocDSRef.setValue(targetHub.getHubId());

      // Step 8: Create Relocate Spec & Relocate
      VirtualMachineRelocateSpec rSpec = new VirtualMachineRelocateSpec();
      rSpec.setDatastore(relocDSRef);
      rSpec.getProfile().add(getVMDefinedProfileSpec(profileName));
      ManagedObjectReference taskMOR =
            connection.getVimPort().relocateVMTask(vmRef, rSpec, null);
      if (getTaskResultAfterDone(taskMOR)) {
         System.out.println("VM's storage relocated successfully to datastore "
               + getDSName(targetHub));
      } else {
         System.out.println("Failure -: VM's storage " + "cannot be relocated");
      }

   }

   @Action
   public void run() throws RuntimeFaultFaultMsg,
         InsufficientResourcesFaultFaultMsg, VmConfigFaultFaultMsg,
         InvalidDatastoreFaultMsg, InvalidPropertyFaultMsg, FileFaultFaultMsg,
         InvalidStateFaultMsg, MigrationFaultFaultMsg,
         InvalidCollectorVersionFaultMsg, TimedoutFaultMsg,
         InvalidArgumentFaultMsg, com.vmware.pbm.RuntimeFaultFaultMsg,
         PbmFaultFaultMsg {
      relocate();
   }


   @Option(name = "profilename", description = "Name of the storage profile", required = true)
   public void setProfileName(String profileName) {
      this.profileName = profileName;
   }

   @Option(name = "vmpath", description = "inventory path of the VM", required = true)
   public void setVmPathName(String vmPathName) {
      this.vmPathName = vmPathName;
   }
}
