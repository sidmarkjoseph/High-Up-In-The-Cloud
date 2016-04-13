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

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vmware.common.annotations.Action;
import com.vmware.common.annotations.Option;
import com.vmware.common.annotations.Sample;
import com.vmware.pbm.InvalidArgumentFaultMsg;
import com.vmware.pbm.PbmCapabilityProfile;
import com.vmware.pbm.PbmProfile;
import com.vmware.pbm.PbmProfileId;
import com.vmware.pbm.PbmServiceInstanceContent;
import com.vmware.spbm.connection.ConnectedServiceBase;
import com.vmware.spbm.connection.helpers.VCUtil;
import com.vmware.spbm.connection.helpers.PbmUtil;
import com.vmware.vim25.AlreadyExistsFaultMsg;
import com.vmware.vim25.ConfigTarget;
import com.vmware.vim25.DatastoreSummary;
import com.vmware.vim25.DuplicateNameFaultMsg;
import com.vmware.vim25.DynamicProperty;
import com.vmware.vim25.FileFaultFaultMsg;
import com.vmware.vim25.InsufficientResourcesFaultFaultMsg;
import com.vmware.vim25.InvalidCollectorVersionFaultMsg;
import com.vmware.vim25.InvalidDatastoreFaultMsg;
import com.vmware.vim25.InvalidNameFaultMsg;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.InvalidStateFaultMsg;
import com.vmware.vim25.LocalizedMethodFault;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.NetworkSummary;
import com.vmware.vim25.ObjectContent;
import com.vmware.vim25.ObjectSpec;
import com.vmware.vim25.OutOfBoundsFaultMsg;
import com.vmware.vim25.PropertyFilterSpec;
import com.vmware.vim25.PropertySpec;
import com.vmware.vim25.RetrieveOptions;
import com.vmware.vim25.RetrieveResult;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.TaskInProgressFaultMsg;
import com.vmware.vim25.TaskInfoState;
import com.vmware.vim25.TraversalSpec;
import com.vmware.vim25.VirtualCdrom;
import com.vmware.vim25.VirtualCdromIsoBackingInfo;
import com.vmware.vim25.VirtualDevice;
import com.vmware.vim25.VirtualDeviceConfigSpec;
import com.vmware.vim25.VirtualDeviceConfigSpecFileOperation;
import com.vmware.vim25.VirtualDeviceConfigSpecOperation;
import com.vmware.vim25.VirtualDisk;
import com.vmware.vim25.VirtualDiskFlatVer2BackingInfo;
import com.vmware.vim25.VirtualEthernetCard;
import com.vmware.vim25.VirtualEthernetCardNetworkBackingInfo;
import com.vmware.vim25.VirtualFloppy;
import com.vmware.vim25.VirtualFloppyDeviceBackingInfo;
import com.vmware.vim25.VirtualIDEController;
import com.vmware.vim25.VirtualLsiLogicController;
import com.vmware.vim25.VirtualMachineConfigOption;
import com.vmware.vim25.VirtualMachineConfigSpec;
import com.vmware.vim25.VirtualMachineDatastoreInfo;
import com.vmware.vim25.VirtualMachineDefinedProfileSpec;
import com.vmware.vim25.VirtualMachineFileInfo;
import com.vmware.vim25.VirtualMachineNetworkInfo;
import com.vmware.vim25.VirtualPCNet32;
import com.vmware.vim25.VirtualSCSISharing;
import com.vmware.vim25.VmConfigFaultFaultMsg;

/**
 * <pre>
 * VMCreate
 *
 * This sample creates a VM with a storage policy based management profile.
 * The VM uses the datastores configured with the host.
 *
 * <b>Parameters:</b>
 * url              [required] : url of the web service
 * username         [required] : username for the authentication
 * password         [required] : password for the authentication
 * vmname           [required] : Name of the virtual machine
 * datacentername   [required] : Name of the datacenter
 * hostname         [required] : Name of the host
 * profilename      [required] : Name of the storage profile
 * guestosid        [optional] : Type of Guest OS
 * cpucount         [optional] : Total cpu count
 * disksize         [optional] : Size of the Disk
 * memorysize       [optional] : Size of the Memory in 1024MB blocks
 * datastorename    [optional] : Name of dataStore
 *
 * <b>Command Line:</b>
 * Create a VM given datacenter, host names and storage profile name
 * run.bat com.vmware.vm.VMCreate --url [webserviceurl]
 * --username [username] --password [password] --vmname [vmname]
 * --datacentername [DataCenterName] --hostname [hostname]
 * --profilename [Storage Profile Name]
 *
 * Create a VM given its name, Datacenter name, storage profile name and GuestOsId
 * run.bat com.vmware.vm.VMCreate --url [webserviceurl]
 * --username [username] --password [password] --vmname [vmname]
 * --datacentername [DataCenterName] --guestosid [GuestOsId]
 * --profilename [Storage Profile Name]
 *
 * Create a VM given its name, Datacenter name, storage profile name and its cpucount
 * run.bat com.vmware.vm.VMCreate --url [webserviceurl]
 * --username [username] --password [password] --vmname [vmname]
 * --datacentername [DataCenterName] --cpucount [cpucount]
 * --profilename [Storage Profile Name]
 * </pre>
 */
@Sample(name = "vm-create", description = "This sample creates a VM based on a storage profile.")
public class VMCreate extends ConnectedServiceBase {

   private String dataStore;
   private String virtualMachineName;
   private long vmMemory = 1024;
   private int numCpus = 1;
   private String dataCenterName;
   private int diskSize = 1;
   private String hostname;
   private String guestOsId = "windows7Guest";
   private String profileName;

   /**
    * Creates the virtual disk.
    *
    * @param volName
    *           the vol name
    * @param diskCtlrKey
    *           the disk ctlr key
    * @param datastoreRef
    *           the datastore ref
    * @param diskSizeMB
    *           the disk size in mb
    * @return the virtual device config spec object
    */
   VirtualDeviceConfigSpec createVirtualDisk(String volName, int diskCtlrKey,
         ManagedObjectReference datastoreRef) {
      String volumeName = getVolumeName(volName);
      VirtualDeviceConfigSpec diskSpec = new VirtualDeviceConfigSpec();

      diskSpec.setFileOperation(VirtualDeviceConfigSpecFileOperation.CREATE);
      diskSpec.setOperation(VirtualDeviceConfigSpecOperation.ADD);

      VirtualDisk disk = new VirtualDisk();
      VirtualDiskFlatVer2BackingInfo diskfileBacking =
            new VirtualDiskFlatVer2BackingInfo();

      diskfileBacking.setFileName(volumeName);
      diskfileBacking.setDiskMode("persistent");

      disk.setKey(new Integer(0));
      disk.setControllerKey(new Integer(diskCtlrKey));
      disk.setUnitNumber(new Integer(0));
      disk.setBacking(diskfileBacking);
      disk.setCapacityInKB(1024);

      diskSpec.setDevice(disk);

      return diskSpec;
   }

   /**
    * Creates the virtual machine.
    *
    * @throws RemoteException
    *            the remote exception
    * @throws com.vmware.pbm.RuntimeFaultFaultMsg
    * @throws InvalidArgumentFaultMsg
    * @throws Exception
    *            the exception
    */
   void createVirtualMachine() throws RemoteException, RuntimeFaultFaultMsg,
         InvalidPropertyFaultMsg, InvalidCollectorVersionFaultMsg,
         OutOfBoundsFaultMsg, DuplicateNameFaultMsg, VmConfigFaultFaultMsg,
         InsufficientResourcesFaultFaultMsg, AlreadyExistsFaultMsg,
         InvalidDatastoreFaultMsg, FileFaultFaultMsg, InvalidStateFaultMsg,
         InvalidNameFaultMsg, TaskInProgressFaultMsg, InvalidArgumentFaultMsg,
         com.vmware.pbm.RuntimeFaultFaultMsg {

      ManagedObjectReference dcmor =
            getMOREFsInContainerByType(
                  connection.getVimServiceContent().getRootFolder(),
                  "Datacenter").get(dataCenterName);

      if (dcmor == null) {
         System.out.println("Datacenter " + dataCenterName + " not found.");
         return;
      }
      ManagedObjectReference hostmor =
            getMOREFsInContainerByType(dcmor, "HostSystem").get(hostname);
      if (hostmor == null) {
         System.out.println("Host " + hostname + " not found");
         return;
      }

      ManagedObjectReference crmor =
            (ManagedObjectReference) VCUtil.getEntityProps(connection, hostmor,
                  new String[] { "parent" }).get("parent");
      if (crmor == null) {
         System.out.println("No Compute Resource Found On Specified Host");
         return;
      }

      ManagedObjectReference resourcepoolmor =
            (ManagedObjectReference) VCUtil.getEntityProps(connection, crmor,
                  new String[] { "resourcePool" }).get("resourcePool");
      ManagedObjectReference vmFolderMor =
            (ManagedObjectReference) VCUtil.getEntityProps(connection, dcmor,
                  new String[] { "vmFolder" }).get("vmFolder");
      // VM Config Spec with a Storage Profile
      VirtualMachineConfigSpec vmConfigSpec =
            createVmConfigSpec(virtualMachineName, dataStore, diskSize, crmor,
                  hostmor, getPbmProfileSpec(profileName));

      vmConfigSpec.setName(virtualMachineName);
      vmConfigSpec.setAnnotation("VirtualMachine Annotation");
      vmConfigSpec.setMemoryMB(new Long(vmMemory));
      vmConfigSpec.setNumCPUs(numCpus);
      vmConfigSpec.setGuestId(guestOsId);

      ManagedObjectReference taskmor =
            connection.getVimPort().createVMTask(vmFolderMor, vmConfigSpec,
                  resourcepoolmor, hostmor);
      if (getTaskResultAfterDone(taskmor)) {
         System.out.printf("Success: Creating VM  - [ %s ] %n",
               virtualMachineName);
      } else {
         String msg = "Failure: Creating [ " + virtualMachineName + "] VM";
         throw new RuntimeException(msg);
      }
      ManagedObjectReference vmMor =
            (ManagedObjectReference) VCUtil.getEntityProps(connection, taskmor,
                  new String[] { "info.result" }).get("info.result");
      System.out.println("Powering on the newly created VM "
            + virtualMachineName);
      // Start the Newly Created VM.
      powerOnVM(vmMor);
   }

   /**
    * Creates the vm config spec object.
    *
    * @param vmName
    *           the vm name
    * @param datastoreName
    *           the datastore name
    * @param diskSizeMB
    *           the disk size in mb
    * @param computeResMor
    *           the compute res moref
    * @param hostMor
    *           the host mor
    * @return the virtual machine config spec object
    * @throws Exception
    *            the exception
    */
   VirtualMachineConfigSpec createVmConfigSpec(String vmName,
         String datastoreName, int diskSizeMB,
         ManagedObjectReference computeResMor, ManagedObjectReference hostMor,
         VirtualMachineDefinedProfileSpec spbmProfile)
         throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {

      ConfigTarget configTarget =
            getConfigTargetForHost(computeResMor, hostMor);
      List<VirtualDevice> defaultDevices =
            getDefaultDevices(computeResMor, hostMor);
      VirtualMachineConfigSpec configSpec = new VirtualMachineConfigSpec();
      // Set SPBM profile
      configSpec.getVmProfile().add(spbmProfile);

      String networkName = null;
      if (configTarget.getNetwork() != null) {
         for (int i = 0; i < configTarget.getNetwork().size(); i++) {
            VirtualMachineNetworkInfo netInfo =
                  configTarget.getNetwork().get(i);
            NetworkSummary netSummary = netInfo.getNetwork();
            if (netSummary.isAccessible()) {
               networkName = netSummary.getName();
               break;
            }
         }
      }
      ManagedObjectReference datastoreRef = null;
      if (datastoreName != null) {
         boolean flag = false;
         for (int i = 0; i < configTarget.getDatastore().size(); i++) {
            VirtualMachineDatastoreInfo vdsInfo =
                  configTarget.getDatastore().get(i);
            DatastoreSummary dsSummary = vdsInfo.getDatastore();
            if (dsSummary.getName().equals(datastoreName)) {
               flag = true;
               if (dsSummary.isAccessible()) {
                  datastoreRef = dsSummary.getDatastore();
               } else {
                  throw new RuntimeException(
                        "Specified Datastore is not accessible");
               }
               break;
            }
         }
         if (!flag) {
            throw new RuntimeException("Specified Datastore is not Found");
         }
      } else {
         boolean flag = false;
         for (int i = 0; i < configTarget.getDatastore().size(); i++) {
            VirtualMachineDatastoreInfo vdsInfo =
                  configTarget.getDatastore().get(i);
            DatastoreSummary dsSummary = vdsInfo.getDatastore();
            if (dsSummary.isAccessible()) {
               datastoreName = dsSummary.getName();
               datastoreRef = dsSummary.getDatastore();
               flag = true;
               break;
            }
         }
         if (!flag) {
            throw new RuntimeException("No Datastore found on host");
         }
      }
      String datastoreVolume = getVolumeName(datastoreName);
      VirtualMachineFileInfo vmfi = new VirtualMachineFileInfo();
      vmfi.setVmPathName(datastoreVolume);
      configSpec.setFiles(vmfi);
      // Add a scsi controller
      int diskCtlrKey = 1;
      VirtualDeviceConfigSpec scsiCtrlSpec = new VirtualDeviceConfigSpec();
      scsiCtrlSpec.setOperation(VirtualDeviceConfigSpecOperation.ADD);
      VirtualLsiLogicController scsiCtrl = new VirtualLsiLogicController();
      scsiCtrl.setBusNumber(0);
      scsiCtrlSpec.setDevice(scsiCtrl);
      scsiCtrl.setKey(diskCtlrKey);
      scsiCtrl.setSharedBus(VirtualSCSISharing.NO_SHARING);
      String ctlrType = scsiCtrl.getClass().getName();
      ctlrType = ctlrType.substring(ctlrType.lastIndexOf(".") + 1);

      // Find the IDE controller
      VirtualDevice ideCtlr = null;
      for (int di = 0; di < defaultDevices.size(); di++) {
         if (defaultDevices.get(di) instanceof VirtualIDEController) {
            ideCtlr = defaultDevices.get(di);
            break;
         }
      }

      // Add a floppy
      VirtualDeviceConfigSpec floppySpec = new VirtualDeviceConfigSpec();
      floppySpec.setOperation(VirtualDeviceConfigSpecOperation.ADD);
      VirtualFloppy floppy = new VirtualFloppy();
      VirtualFloppyDeviceBackingInfo flpBacking =
            new VirtualFloppyDeviceBackingInfo();
      flpBacking.setDeviceName("/dev/fd0");
      floppy.setBacking(flpBacking);
      floppy.setKey(3);
      floppySpec.setDevice(floppy);

      // Add a cdrom based on a physical device
      VirtualDeviceConfigSpec cdSpec = null;

      if (ideCtlr != null) {
         cdSpec = new VirtualDeviceConfigSpec();
         cdSpec.setOperation(VirtualDeviceConfigSpecOperation.ADD);
         VirtualCdrom cdrom = new VirtualCdrom();
         VirtualCdromIsoBackingInfo cdDeviceBacking =
               new VirtualCdromIsoBackingInfo();
         cdDeviceBacking.setDatastore(datastoreRef);
         cdDeviceBacking.setFileName(datastoreVolume + "testcd.iso");
         cdrom.setBacking(cdDeviceBacking);
         cdrom.setKey(20);
         cdrom.setControllerKey(new Integer(ideCtlr.getKey()));
         cdrom.setUnitNumber(new Integer(0));
         cdSpec.setDevice(cdrom);
      }

      // Create a new disk - file based - for the vm
      VirtualDeviceConfigSpec diskSpec = null;
      diskSpec = createVirtualDisk(datastoreName, diskCtlrKey, datastoreRef);

      // Add a NIC. the network Name must be set as the device name to create
      // the NIC.
      VirtualDeviceConfigSpec nicSpec = new VirtualDeviceConfigSpec();
      if (networkName != null) {
         nicSpec.setOperation(VirtualDeviceConfigSpecOperation.ADD);
         VirtualEthernetCard nic = new VirtualPCNet32();
         VirtualEthernetCardNetworkBackingInfo nicBacking =
               new VirtualEthernetCardNetworkBackingInfo();
         nicBacking.setDeviceName(networkName);
         nic.setAddressType("generated");
         nic.setBacking(nicBacking);
         nic.setKey(4);
         nicSpec.setDevice(nic);
      }

      List<VirtualDeviceConfigSpec> deviceConfigSpec =
            new ArrayList<VirtualDeviceConfigSpec>();
      deviceConfigSpec.add(scsiCtrlSpec);
      deviceConfigSpec.add(floppySpec);
      deviceConfigSpec.add(diskSpec);
      if (ideCtlr != null) {
         deviceConfigSpec.add(cdSpec);
         deviceConfigSpec.add(nicSpec);
      } else {
         deviceConfigSpec = new ArrayList<VirtualDeviceConfigSpec>();
         deviceConfigSpec.add(nicSpec);
      }
      configSpec.getDeviceChange().addAll(deviceConfigSpec);
      return configSpec;
   }

   /**
    * This method returns the ConfigTarget for a HostSystem.
    *
    * @param computeResMor
    *           A MoRef to the ComputeResource used by the HostSystem
    * @param hostMor
    *           A MoRef to the HostSystem
    * @return Instance of ConfigTarget for the supplied
    *         HostSystem/ComputeResource
    * @throws Exception
    *            When no ConfigTarget can be found
    */
   ConfigTarget getConfigTargetForHost(ManagedObjectReference computeResMor,
         ManagedObjectReference hostMor) throws RuntimeFaultFaultMsg,
         InvalidPropertyFaultMsg {
      ManagedObjectReference envBrowseMor =
            (ManagedObjectReference) VCUtil.getEntityProps(connection,computeResMor,
                  new String[] { "environmentBrowser" }).get(
                  "environmentBrowser");
      ConfigTarget configTarget =
            connection.getVimPort().queryConfigTarget(envBrowseMor, hostMor);
      if (configTarget == null) {
         throw new RuntimeException("No ConfigTarget found in ComputeResource");
      }
      return configTarget;
   }

   /**
    * The method returns the default devices from the HostSystem.
    *
    * @param computeResMor
    *           A MoRef to the ComputeResource used by the HostSystem
    * @param hostMor
    *           A MoRef to the HostSystem
    * @return Array of VirtualDevice containing the default devices for the
    *         HostSystem
    * @throws Exception
    */
   List<VirtualDevice> getDefaultDevices(ManagedObjectReference computeResMor,
         ManagedObjectReference hostMor) throws RuntimeFaultFaultMsg,
         InvalidPropertyFaultMsg {
      ManagedObjectReference envBrowseMor =
            (ManagedObjectReference) VCUtil.getEntityProps(connection,computeResMor,
                  new String[] { "environmentBrowser" }).get(
                  "environmentBrowser");
      VirtualMachineConfigOption cfgOpt =
            connection.getVimPort().queryConfigOption(envBrowseMor, null,
                  hostMor);
      List<VirtualDevice> defaultDevs = null;
      if (cfgOpt == null) {
         throw new RuntimeException(
               "No VirtualHardwareInfo found in ComputeResource");
      } else {
         List<VirtualDevice> lvds = cfgOpt.getDefaultDevice();
         if (lvds == null) {
            throw new RuntimeException("No Datastore found in ComputeResource");
         } else {
            defaultDevs = lvds;
         }
      }
      return defaultDevs;
   }

   /**
    * Returns all the MOREFs of the specified type that are present under the
    * container
    *
    * @param folder
    *           {@link ManagedObjectReference} of the container to begin the
    *           search from
    * @param morefType
    *           Type of the managed entity that needs to be searched
    * @return Map of name and MOREF of the managed objects present. If none
    *         exist then empty Map is returned
    * @throws InvalidPropertyFaultMsg
    * @throws RuntimeFaultFaultMsg
    */
   Map<String, ManagedObjectReference> getMOREFsInContainerByType(
         ManagedObjectReference folder, String morefType)
         throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
      String PROP_ME_NAME = "name";
      ManagedObjectReference viewManager =
            connection.getVimServiceContent().getViewManager();
      ManagedObjectReference containerView =
            connection.getVimPort().createContainerView(viewManager, folder,
                  Arrays.asList(morefType), true);

      Map<String, ManagedObjectReference> tgtMoref =
            new HashMap<String, ManagedObjectReference>();

      // Create Property Spec
      PropertySpec propertySpec = new PropertySpec();
      propertySpec.setAll(Boolean.FALSE);
      propertySpec.setType(morefType);
      propertySpec.getPathSet().add(PROP_ME_NAME);

      TraversalSpec ts = new TraversalSpec();
      ts.setName("view");
      ts.setPath("view");
      ts.setSkip(false);
      ts.setType("ContainerView");

      // Now create Object Spec
      ObjectSpec objectSpec = new ObjectSpec();
      objectSpec.setObj(containerView);
      objectSpec.setSkip(Boolean.TRUE);
      objectSpec.getSelectSet().add(ts);

      // Create PropertyFilterSpec using the PropertySpec and ObjectPec
      // created above.
      PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
      propertyFilterSpec.getPropSet().add(propertySpec);
      propertyFilterSpec.getObjectSet().add(objectSpec);

      List<PropertyFilterSpec> propertyFilterSpecs =
            new ArrayList<PropertyFilterSpec>();
      propertyFilterSpecs.add(propertyFilterSpec);

      RetrieveResult rslts =
            connection.getVimPort().retrievePropertiesEx(
                  connection.getVimServiceContent().getPropertyCollector(),
                  propertyFilterSpecs, new RetrieveOptions());
      List<ObjectContent> listobjcontent = new ArrayList<ObjectContent>();
      if (rslts != null && rslts.getObjects() != null
            && !rslts.getObjects().isEmpty()) {
         listobjcontent.addAll(rslts.getObjects());
      }
      String token = null;
      if (rslts != null && rslts.getToken() != null) {
         token = rslts.getToken();
      }
      while (token != null && !token.isEmpty()) {
         rslts =
               connection.getVimPort().continueRetrievePropertiesEx(
                     connection.getVimServiceContent().getPropertyCollector(),
                     token);
         token = null;
         if (rslts != null) {
            token = rslts.getToken();
            if (rslts.getObjects() != null && !rslts.getObjects().isEmpty()) {
               listobjcontent.addAll(rslts.getObjects());
            }
         }
      }
      for (ObjectContent oc : listobjcontent) {
         ManagedObjectReference mr = oc.getObj();
         String entityNm = null;
         List<DynamicProperty> dps = oc.getPropSet();
         if (dps != null) {
            for (DynamicProperty dp : dps) {
               entityNm = (String) dp.getVal();
            }
         }
         tgtMoref.put(entityNm, mr);
      }
      return tgtMoref;
   }

   /**
    * This method returns the Profile Spec for the given Storage Profile name
    *
    * @return
    * @throws InvalidArgumentFaultMsg
    * @throws com.vmware.pbm.RuntimeFaultFaultMsg
    * @throws RuntimeFaultFaultMsg
    */
   VirtualMachineDefinedProfileSpec getPbmProfileSpec(String name)
         throws InvalidArgumentFaultMsg, com.vmware.pbm.RuntimeFaultFaultMsg,
         RuntimeFaultFaultMsg {

      // Get PBM Profile Manager
      PbmServiceInstanceContent spbmsc = connection.getPbmServiceContent();
      ManagedObjectReference profileMgr = spbmsc.getProfileManager();

      // Search for the given Profile Name
      List<PbmProfileId> profileIds =
            connection.getPbmPort().pbmQueryProfile(profileMgr,
                  PbmUtil.getStorageResourceType(), null);
      if (profileIds == null || profileIds.isEmpty())
         throw new RuntimeFaultFaultMsg("No storage Profiles exist.", null);
      List<PbmProfile> pbmProfiles =
            connection.getPbmPort().pbmRetrieveContent(profileMgr, profileIds);
      for (PbmProfile pbmProfile : pbmProfiles) {
         if (pbmProfile.getName().equals(name)) {
            PbmCapabilityProfile profile = (PbmCapabilityProfile) pbmProfile;
            VirtualMachineDefinedProfileSpec spbmProfile =
                  new VirtualMachineDefinedProfileSpec();
            spbmProfile.setProfileId(profile.getProfileId().getUniqueId());
            return spbmProfile;
         }
      }

      // Throw exception if none found
      throw new InvalidArgumentFaultMsg(
            "Specified storage profile name does not exist.", null);
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
   boolean getTaskResultAfterDone(ManagedObjectReference task)
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

   String getVolumeName(String volName) {
      String volumeName = null;
      if (volName != null && volName.length() > 0) {
         volumeName = "[" + volName + "]";
      } else {
         volumeName = "[Local]";
      }

      return volumeName;
   }

   /**
    * Power on vm.
    *
    * @param vmMor
    *           the vm moref
    * @throws RemoteException
    *            the remote exception
    * @throws Exception
    *            the exception
    */
   void powerOnVM(ManagedObjectReference vmMor) throws RemoteException,
         RuntimeFaultFaultMsg, InvalidPropertyFaultMsg,
         InvalidCollectorVersionFaultMsg, TaskInProgressFaultMsg,
         VmConfigFaultFaultMsg, InsufficientResourcesFaultFaultMsg,
         FileFaultFaultMsg, InvalidStateFaultMsg {
      ManagedObjectReference cssTask =
            connection.getVimPort().powerOnVMTask(vmMor, null);
      if (getTaskResultAfterDone(cssTask)) {
         System.out.println("Success: VM started Successfully");
      } else {
         String msg = "Failure: starting [ " + vmMor.getValue() + "] VM";
         throw new RuntimeException(msg);
      }
   }

   @Action
   public void run() throws RuntimeFaultFaultMsg, VmConfigFaultFaultMsg,
         AlreadyExistsFaultMsg, InvalidDatastoreFaultMsg, FileFaultFaultMsg,
         InvalidStateFaultMsg, InvalidCollectorVersionFaultMsg,
         InvalidNameFaultMsg, OutOfBoundsFaultMsg, DuplicateNameFaultMsg,
         InsufficientResourcesFaultFaultMsg, InvalidPropertyFaultMsg,
         RemoteException, TaskInProgressFaultMsg, InvalidArgumentFaultMsg,
         com.vmware.pbm.RuntimeFaultFaultMsg {
      createVirtualMachine();
   }

   @Option(name = "cpucount", required = false, description = "Total cpu count")
   public void setCpuCount(String cpuCount) {
      this.numCpus = Integer.parseInt(cpuCount);
   }

   @Option(name = "datacentername", description = "Name of the datacenter")
   public void setDataCenterName(String dcname) {
      this.dataCenterName = dcname;
   }

   @Option(name = "datastorename", required = false, description = "Name of dataStore")
   public void setDataStore(String dsname) {
      this.dataStore = dsname;
   }

   @Option(name = "disksize", required = false, description = "Size of the Disk")
   public void setDiskSize(String size) {
      this.diskSize = Integer.parseInt(size);
   }

   @Option(name = "guestosid", required = false, description = "Type of Guest OS [Windows|Posix]")
   public void setGuestOsId(String guestOsId) {
      this.guestOsId = guestOsId;
   }

   @Option(name = "hostname", description = "Name of the host")
   public void setHostname(String hostname) {
      this.hostname = hostname;
   }

   @Option(name = "memorysize", required = false, description = "Size of Memory in 1024MB blocks. eg. 2048")
   public void setMemorySize(String memorySize) {
      this.vmMemory = Long.parseLong(memorySize);
   }


   @Option(name = "profilename", description = "Name of the storage profile", required = true)
   public void setProfileName(String profileName) {
      this.profileName = profileName;
   }

   @Option(name = "vmname", description = "Name of the virtual machine")
   public void setVirtualMachineName(String vmname) {
      this.virtualMachineName = vmname;
   }
}
