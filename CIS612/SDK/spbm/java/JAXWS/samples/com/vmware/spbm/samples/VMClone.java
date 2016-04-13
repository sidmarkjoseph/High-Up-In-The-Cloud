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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.vmware.common.annotations.Action;
import com.vmware.common.annotations.Option;
import com.vmware.common.annotations.Sample;
import com.vmware.pbm.InvalidArgumentFaultMsg;
import com.vmware.pbm.PbmCapabilityProfile;
import com.vmware.pbm.PbmFaultFaultMsg;
import com.vmware.pbm.PbmServiceInstanceContent;
import com.vmware.spbm.connection.ConnectedServiceBase;
import com.vmware.spbm.connection.helpers.VCUtil;
import com.vmware.spbm.connection.helpers.PbmUtil;
import com.vmware.vim25.CustomizationFaultFaultMsg;
import com.vmware.vim25.DynamicProperty;
import com.vmware.vim25.FileFaultFaultMsg;
import com.vmware.vim25.InsufficientResourcesFaultFaultMsg;
import com.vmware.vim25.InvalidCollectorVersionFaultMsg;
import com.vmware.vim25.InvalidDatastoreFaultMsg;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.InvalidStateFaultMsg;
import com.vmware.vim25.LocalizedMethodFault;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.MigrationFaultFaultMsg;
import com.vmware.vim25.ObjectContent;
import com.vmware.vim25.ObjectSpec;
import com.vmware.vim25.PropertyFilterSpec;
import com.vmware.vim25.PropertySpec;
import com.vmware.vim25.RetrieveOptions;
import com.vmware.vim25.RetrieveResult;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.TaskInProgressFaultMsg;
import com.vmware.vim25.TaskInfoState;
import com.vmware.vim25.VirtualMachineCloneSpec;
import com.vmware.vim25.VirtualMachineConfigSpec;
import com.vmware.vim25.VirtualMachineDefinedProfileSpec;
import com.vmware.vim25.VirtualMachineRelocateSpec;
import com.vmware.vim25.VmConfigFaultFaultMsg;

/**
 * <pre>
 * VMClone
 *
 * This sample makes a clone of an existing VM to a new VM and
 * associates it with the specified storage profile. Note: This sample
 * does not relocate the vm disks. Thus, the clone may or may not be
 * compliant with the given storage profile.
 *
 * <b>Parameters:</b>
 * url             [required] : url of the web service
 * username        [required] : username for the authentication
 * password        [required] : password for the authentication
 * datacentername  [required] : name of Datacenter
 * vmpath          [required] : inventory path of the VM
 * clonename       [required] : name of the clone
 * profilename     [required] : name of the storage profile
 *
 * <b>Command Line:</b>
 * java com.vmware.vm.VMClone --url [webserviceurl]
 * --username [username] --password [password]
 * --datacentername [DatacenterName]"
 * --vmpath [vmPath] --clonename [CloneName]
 * --profilename [Profile Name]
 * </pre>
 */
@Sample(name = "vm-clone", description = "This sample makes a clone of an existing VM to a new VM and"
      + "associates it with the specified storage profile. Note: This sample"
      + "does not relocate the vm disks. Thus, the clone VM may or may not be"
      + "compliant with the given storage profile.")
public class VMClone extends ConnectedServiceBase {
   private ManagedObjectReference propCollectorRef;
   private PbmServiceInstanceContent spbmsc;
   private String dataCenterName;
   private String vmPathName;
   private String cloneName;
   private String profileName;

   void cloneVM() throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg,
         InvalidCollectorVersionFaultMsg, CustomizationFaultFaultMsg,
         TaskInProgressFaultMsg, VmConfigFaultFaultMsg,
         InsufficientResourcesFaultFaultMsg, InvalidDatastoreFaultMsg,
         FileFaultFaultMsg, MigrationFaultFaultMsg, InvalidStateFaultMsg,
         InvocationTargetException, NoSuchMethodException,
         IllegalAccessException, InvalidArgumentFaultMsg,
         com.vmware.pbm.RuntimeFaultFaultMsg, PbmFaultFaultMsg {

      // Step 1: Instantiate SPBM Service
      spbmsc = connection.getPbmServiceContent();

      // Step 2: Find the Datacenter reference by using findByInventoryPath().
      ManagedObjectReference datacenterRef =
            connection.getVimPort().findByInventoryPath(
                  connection.getVimServiceContent().getSearchIndex(),
                  dataCenterName);
      if (datacenterRef == null) {
         System.out.printf("The specified datacenter [ %s ]is not found %n",
               dataCenterName);
         return;
      }
      // Step 3: Find the virtual machine folder for this datacenter.
      ManagedObjectReference vmFolderRef =
            (ManagedObjectReference) getDynamicProperty(datacenterRef,
                  "vmFolder");
      if (vmFolderRef == null) {
         System.out.println("The virtual machine is not found");
         return;
      }

      // Step 4: Find the virtual machine reference
      ManagedObjectReference vmRef =
            connection.getVimPort().findByInventoryPath(
                  connection.getVimServiceContent().getSearchIndex(),
                  vmPathName);
      if (vmRef == null) {
         System.out.printf("The VMPath specified [ %s ] is not found %n",
               vmPathName);
         return;
      }

      // Step 5: Create Specs
      VirtualMachineRelocateSpec relocSpec = new VirtualMachineRelocateSpec();
      VirtualMachineCloneSpec cloneSpec = new VirtualMachineCloneSpec();
      VirtualMachineConfigSpec configSpec = new VirtualMachineConfigSpec();

      // Step 6: Associate Storage Profile
      relocSpec.getProfile().add(getVMDefinedProfileSpec(profileName));
      cloneSpec.setConfig(configSpec);
      cloneSpec.setLocation(relocSpec);
      cloneSpec.setPowerOn(false);
      cloneSpec.setTemplate(false);

      // Step 7: Clone VM
      System.out.printf("Cloning Virtual Machine [%s] to clone name [%s] %n",
            vmPathName.substring(vmPathName.lastIndexOf("/") + 1), cloneName);
      ManagedObjectReference cloneTask =
            connection.getVimPort().cloneVMTask(vmRef, vmFolderRef, cloneName,
                  cloneSpec);
      if (getTaskResultAfterDone(cloneTask)) {
         System.out
               .printf(
                     "Successfully cloned Virtual Machine [%s] to clone name [%s] %n",
                     vmPathName.substring(vmPathName.lastIndexOf("/") + 1),
                     cloneName);
      } else {
         System.out
               .printf(
                     "Failure Cloning Virtual Machine [%s] to clone name [%s] %n",
                     vmPathName.substring(vmPathName.lastIndexOf("/") + 1),
                     cloneName);
      }
   }

   /**
    * This method returns a object for the specified property name of the
    * Managed Object
    *
    * @param mor
    * @param propertyName
    * @return
    * @throws NoSuchMethodException
    * @throws InvocationTargetException
    * @throws IllegalAccessException
    * @throws RuntimeFaultFaultMsg
    * @throws InvalidPropertyFaultMsg
    */
   Object getDynamicProperty(ManagedObjectReference mor, String propertyName)
         throws NoSuchMethodException, InvocationTargetException,
         IllegalAccessException, RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
      ObjectContent[] objContent =
            getObjectProperties(mor, new String[] { propertyName });

      Object propertyValue = null;
      if (objContent != null) {
         List<DynamicProperty> listdp = objContent[0].getPropSet();
         if (listdp != null) {
            /*
             * Check the dynamic property for ArrayOfXXX object
             */
            Object dynamicPropertyVal = listdp.get(0).getVal();
            String dynamicPropertyName =
                  dynamicPropertyVal.getClass().getName();
            if (dynamicPropertyName.indexOf("ArrayOf") != -1) {
               String methodName =
                     dynamicPropertyName.substring(
                           dynamicPropertyName.indexOf("ArrayOf")
                                 + "ArrayOf".length(),
                           dynamicPropertyName.length());
               /*
                * If object is ArrayOfXXX object, then get the XXX[] by
                * invoking getXXX() on the object.
                * For Ex:
                * ArrayOfManagedObjectReference.getManagedObjectReference()
                * returns ManagedObjectReference[] array.
                */
               if (methodExists(dynamicPropertyVal, "get" + methodName, null)) {
                  methodName = "get" + methodName;
               } else {
                  /*
                   * Construct methodName for ArrayOf primitive types
                   * Ex: For ArrayOfInt, methodName is get_int
                   */
                  methodName = "get_" + methodName.toLowerCase();
               }
               Method getMorMethod =
                     dynamicPropertyVal.getClass().getDeclaredMethod(
                           methodName, (Class[]) null);
               propertyValue =
                     getMorMethod.invoke(dynamicPropertyVal, (Object[]) null);
            } else if (dynamicPropertyVal.getClass().isArray()) {
               /*
                * Handle the case of an unwrapped array being deserialized.
                */
               propertyValue = dynamicPropertyVal;
            } else {
               propertyValue = dynamicPropertyVal;
            }
         }
      }
      return propertyValue;
   }

   /**
    * Retrieve contents for a single object based on the property collector
    * registered with the service.
    *
    * @param mobj
    *           Managed Object Reference to get contents for
    * @param properties
    *           names of properties of object to retrieve
    * @return retrieved object contents
    */
   ObjectContent[] getObjectProperties(ManagedObjectReference mobj,
         String[] properties) throws RuntimeFaultFaultMsg,
         InvalidPropertyFaultMsg {
      if (mobj == null) {
         return null;
      }

      PropertyFilterSpec spec = new PropertyFilterSpec();
      spec.getPropSet().add(new PropertySpec());
      if ((properties == null || properties.length == 0)) {
         spec.getPropSet().get(0).setAll(Boolean.TRUE);
      } else {
         spec.getPropSet().get(0).setAll(Boolean.FALSE);
      }
      spec.getPropSet().get(0).setType(mobj.getType());
      spec.getPropSet().get(0).getPathSet().addAll(Arrays.asList(properties));
      spec.getObjectSet().add(new ObjectSpec());
      spec.getObjectSet().get(0).setObj(mobj);
      spec.getObjectSet().get(0).setSkip(Boolean.FALSE);
      List<PropertyFilterSpec> listpfs = new ArrayList<PropertyFilterSpec>(1);
      listpfs.add(spec);
      List<ObjectContent> listobjcont = retrievePropertiesAllObjects(listpfs);
      return listobjcont.toArray(new ObjectContent[listobjcont.size()]);
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

   /**
    * Determines of a method 'methodName' exists for the Object 'obj'.
    *
    * @param obj
    *           The Object to check
    * @param methodName
    *           The method name
    * @param parameterTypes
    *           Array of Class objects for the parameter types
    * @return true if the method exists, false otherwise
    */
   @SuppressWarnings("rawtypes")
   boolean methodExists(Object obj, String methodName, Class[] parameterTypes)
         throws NoSuchMethodException {
      boolean exists = false;
      Method method = obj.getClass().getMethod(methodName, parameterTypes);
      if (method != null) {
         exists = true;
      }
      return exists;
   }

   /**
    * Uses the new RetrievePropertiesEx method to emulate the now deprecated
    * RetrieveProperties method
    *
    * @param listpfs
    * @return list of object content
    * @throws Exception
    */
   List<ObjectContent> retrievePropertiesAllObjects(
         List<PropertyFilterSpec> listpfs) throws RuntimeFaultFaultMsg,
         InvalidPropertyFaultMsg {

      RetrieveOptions propObjectRetrieveOpts = new RetrieveOptions();

      List<ObjectContent> listobjcontent = new ArrayList<ObjectContent>();

      RetrieveResult rslts =
            connection.getVimPort().retrievePropertiesEx(propCollectorRef,
                  listpfs, propObjectRetrieveOpts);
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
                     propCollectorRef, token);
         token = null;
         if (rslts != null) {
            token = rslts.getToken();
            if (rslts.getObjects() != null && !rslts.getObjects().isEmpty()) {
               listobjcontent.addAll(rslts.getObjects());
            }
         }
      }
      return listobjcontent;
   }

   @Action
   public void run() throws RuntimeFaultFaultMsg, TaskInProgressFaultMsg,
         VmConfigFaultFaultMsg, InvalidDatastoreFaultMsg, FileFaultFaultMsg,
         NoSuchMethodException, MigrationFaultFaultMsg, InvalidStateFaultMsg,
         InvalidCollectorVersionFaultMsg, IllegalAccessException,
         CustomizationFaultFaultMsg, InsufficientResourcesFaultFaultMsg,
         InvocationTargetException, InvalidPropertyFaultMsg,
         InvalidArgumentFaultMsg, com.vmware.pbm.RuntimeFaultFaultMsg,
         PbmFaultFaultMsg {
      propCollectorRef =
            connection.getVimServiceContent().getPropertyCollector();
      cloneVM();
   }

   @Option(name = "clonename", description = "name of the clone")
   public void setCloneName(String cloneName) {
      this.cloneName = cloneName;
   }

   @Option(name = "datacentername", description = "name of Datacenter")
   public void setDataCenterName(String dataCenterName) {
      this.dataCenterName = dataCenterName;
   }

   @Option(name = "profilename", description = "Name of the storage profile", required = true)
   public void setProfileName(String profileName) {
      this.profileName = profileName;
   }

   @Option(name = "vmpath", description = "inventory path of the VM")
   public void setVmPathName(String vmPathName) {
      this.vmPathName = vmPathName;
   }
}
