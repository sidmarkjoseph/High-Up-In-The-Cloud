package com.syr.cc;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.ws.soap.SOAPFaultException;

import com.vmware.common.Main;
import com.vmware.connection.ConnectedVimServiceBase;
import com.vmware.connection.Connection;
import com.vmware.connection.helpers.GetMOREF;
import com.vmware.vim25.DynamicProperty;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.ObjectContent;
import com.vmware.vim25.ObjectSpec;
import com.vmware.vim25.PropertyFilterSpec;
import com.vmware.vim25.PropertySpec;
import com.vmware.vim25.RetrieveOptions;
import com.vmware.vim25.RetrieveResult;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.SelectionSpec;
import com.vmware.vim25.TraversalSpec;

public class ServerMonitor extends ConnectedVimServiceBase implements VMManagementConstants{
	private ManagedObjectReference propCollectorRef;
	private ManagedObjectReference rootFolder;

	protected GetMOREF getMOREFs;
	File file;
	FileWriter writer;

	private String virtualMachineName;
	private String hostName;
	
	private static final List<String> hostSystemAttributesArr = new ArrayList<String>();	

	/**
	 * Uses the new RetrievePropertiesEx method to emulate the now deprecated
	 * RetrieveProperties method.
	 *
	 * @param listpfs
	 * @return list of object content
	 * @throws Exception
	 */
	List<ObjectContent> retrievePropertiesAllObjects(
			List<PropertyFilterSpec> listpfs) {

		RetrieveOptions propObjectRetrieveOpts = new RetrieveOptions();

		List<ObjectContent> listobjcontent = new ArrayList<ObjectContent>();

		try {
			RetrieveResult rslts = vimPort.retrievePropertiesEx(
					propCollectorRef, listpfs, propObjectRetrieveOpts);
			if (rslts != null && rslts.getObjects() != null
					&& !rslts.getObjects().isEmpty()) {
				listobjcontent.addAll(rslts.getObjects());
			}
			String token = null;
			if (rslts != null && rslts.getToken() != null) {
				token = rslts.getToken();
			}
			while (token != null && !token.isEmpty()) {
				rslts = vimPort.continueRetrievePropertiesEx(propCollectorRef,
						token);
				token = null;
				if (rslts != null) {
					token = rslts.getToken();
					if (rslts.getObjects() != null
							&& !rslts.getObjects().isEmpty()) {
						listobjcontent.addAll(rslts.getObjects());
					}
				}
			}
		} catch (SOAPFaultException sfe) {
			printSoapFaultException(sfe);
		} catch (Exception e) {
			System.out.println(" : Failed Getting Contents");
			e.printStackTrace();
		}

		return listobjcontent;
	}

	/**
	 * Prints exception details if SoapFaultException is thrown
	 *
	 * @param sfe
	 * @return void
	 * @throws Exception
	 */
	void printSoapFaultException(SOAPFaultException sfe) {
		System.out.println("SOAP Fault -");
		if (sfe.getFault().hasDetail()) {
			System.out.println(sfe.getFault().getDetail().getFirstChild()
					.getLocalName());
		}
		if (sfe.getFault().getFaultString() != null) {
			System.out
					.println("\n Message: " + sfe.getFault().getFaultString());
		}
	}

	/**
	 * Retrieves the hostname based on vmName
	 * 
	 * @param vmName            
	 * @return hostName
	 * @throws Exception
	 */
	String getHostByVMName(String vmName) throws Exception {
		String retVal = null;
		TraversalSpec tSpec = getHostSystemTraversalSpec();
		// Create Property Spec
		PropertySpec propertySpec = new PropertySpec();
		propertySpec.setAll(Boolean.FALSE);
		propertySpec.getPathSet().add("name");
		propertySpec.setType("HostSystem");

		// Now create Object Spec
		ObjectSpec objectSpec = new ObjectSpec();
		objectSpec.setObj(rootFolder);
		objectSpec.setSkip(Boolean.TRUE);
		objectSpec.getSelectSet().add(tSpec);

		// Create PropertyFilterSpec using the PropertySpec and ObjectPec
		// created above.
		PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
		propertyFilterSpec.getPropSet().add(propertySpec);
		propertyFilterSpec.getObjectSet().add(objectSpec);
		List<PropertyFilterSpec> listPfs = new ArrayList<PropertyFilterSpec>(1);
		listPfs.add(propertyFilterSpec);
		List<ObjectContent> oContList = retrievePropertiesAllObjects(listPfs);

		if (oContList != null) {
			for (ObjectContent oc : oContList) {
				ManagedObjectReference mr = oc.getObj();
				Map<String, ManagedObjectReference> vms = getMOREFs
						.inContainerByType(mr, "VirtualMachine");
				for (String vmname : vms.keySet()) {
					// ManagedObjectReference vm = vms.get(vmname);
					if (vmname.equalsIgnoreCase(vmName)) {
						String hostnm = null;
						List<DynamicProperty> listDynamicProps = oc
								.getPropSet();
						DynamicProperty[] dps = listDynamicProps
								.toArray(new DynamicProperty[listDynamicProps
										.size()]);
						if (dps != null) {
							for (DynamicProperty dp : dps) {
								hostnm = (String) dp.getVal();
								System.out.println("HostName: " + hostnm);
								this.hostName = hostnm;

							}
						}

						retVal = hostnm;
						break;
					}
				}
			}
		} else {
			System.out.println("The Object Content is Null");
		}
		return retVal;
	}

	/**
	 * @return TraversalSpec specification to get to the HostSystem managed
	 *         object.
	 */
	TraversalSpec getHostSystemTraversalSpec() {
		// Create a traversal spec that starts from the 'root' objects
		// and traverses the inventory tree to get to the Host system.
		// Build the traversal specs bottoms up
		SelectionSpec ss = new SelectionSpec();
		ss.setName("VisitFolders");

		// Traversal to get to the host from ComputeResource
		TraversalSpec computeResourceToHostSystem = new TraversalSpec();
		computeResourceToHostSystem.setName("computeResourceToHostSystem");
		computeResourceToHostSystem.setType("ComputeResource");
		computeResourceToHostSystem.setPath("host");
		computeResourceToHostSystem.setSkip(false);
		computeResourceToHostSystem.getSelectSet().add(ss);

		// Traversal to get to the ComputeResource from hostFolder
		TraversalSpec hostFolderToComputeResource = new TraversalSpec();
		hostFolderToComputeResource.setName("hostFolderToComputeResource");
		hostFolderToComputeResource.setType("Folder");
		hostFolderToComputeResource.setPath("childEntity");
		hostFolderToComputeResource.setSkip(false);
		hostFolderToComputeResource.getSelectSet().add(ss);

		// Traversal to get to the hostFolder from DataCenter
		TraversalSpec dataCenterToHostFolder = new TraversalSpec();
		dataCenterToHostFolder.setName("DataCenterToHostFolder");
		dataCenterToHostFolder.setType("Datacenter");
		dataCenterToHostFolder.setPath("hostFolder");
		dataCenterToHostFolder.setSkip(false);
		dataCenterToHostFolder.getSelectSet().add(ss);

		// TraversalSpec to get to the DataCenter from rootFolder
		TraversalSpec traversalSpec = new TraversalSpec();
		traversalSpec.setName("VisitFolders");
		traversalSpec.setType("Folder");
		traversalSpec.setPath("childEntity");
		traversalSpec.setSkip(false);

		List<SelectionSpec> sSpecArr = new ArrayList<SelectionSpec>();
		sSpecArr.add(ss);
		sSpecArr.add(dataCenterToHostFolder);
		sSpecArr.add(hostFolderToComputeResource);
		sSpecArr.add(computeResourceToHostSystem);
		traversalSpec.getSelectSet().addAll(sSpecArr);
		return traversalSpec;
	}

	/**
	 * Sets the host system attributes list
	 * 
	 * @param        
	 * @return void
	 */
	public void setHostSystemAttributesList() {
		hostSystemAttributesArr.add(HOST_NAME);
		hostSystemAttributesArr.add(HOST_CPU);
		hostSystemAttributesArr.add(HOST_MEMORY);
	}


	/**
	 * Prints host quickstat details
	 * 
	 * @param hostName        
	 * @return void
	 * @throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, IOException
	 */
	public void printHostProductDetails()
			throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, IOException {
		Calendar calendar = Calendar.getInstance();
		
		/*String name = "name";
		String cpuUsage = "summary.quickStats.overallCpuUsage";
		String memUsage = "summary.quickStats.overallMemoryUsage";*/
		writer.append("Logging Time: "+
				calendar.get(Calendar.HOUR)+":"+calendar.get(Calendar.MINUTE)+":"+
				calendar.get(Calendar.SECOND)+":"+calendar.get(Calendar.MILLISECOND)+" "+
				calendar.get(Calendar.MONTH)+"/"+calendar.get(Calendar.DAY_OF_MONTH)+"/"+
				calendar.get(Calendar.YEAR)+"\n");
		String hostName = this.hostName;
		Map<ManagedObjectReference, Map<String, Object>> hosts = getMOREFs
				.inContainerByType(serviceContent.getRootFolder(),
						"HostSystem",
						hostSystemAttributesArr.toArray(new String[] {}));

		for (ManagedObjectReference host : hosts.keySet()) {
			Map<String, Object> hostprops = hosts.get(host);
			for (String prop : hostprops.keySet()) {
				if (prop.equalsIgnoreCase(HOST_NAME)
						&& hostprops.get(prop).toString()
								.equalsIgnoreCase(hostName)) {
					System.out.println(HOST_NAME + " : " + hostprops.get(HOST_NAME));
					writer.append(HOST_NAME + " : " + hostprops.get(HOST_NAME)+"\n");
					System.out.println(HOST_CPU + " : " + hostprops.get(HOST_CPU));
					writer.append(HOST_CPU + " : " + hostprops.get(HOST_CPU)+"\n");
					System.out.println(HOST_MEMORY + " : " + hostprops.get(HOST_MEMORY));
					writer.append(HOST_MEMORY + " : " + hostprops.get(HOST_MEMORY)+"\n");
					writer.flush();
					return;
				}
				
			}
		}
	}

	/**
	 * Initializes the server monitor
	 * 
	 * @param connection, VMname        
	 * @return void
	 * @throws Exception
	 */
	public void init(Connection connection, String VMname) throws Exception {
		this.serviceContent = connection.getServiceContent();
		this.vimPort = connection.getVimPort();
		this.rootFolder = serviceContent.getRootFolder();
		this.getMOREFs = new GetMOREF(connection);

		propCollectorRef = serviceContent.getPropertyCollector();
		setHostSystemAttributesList();
		this.virtualMachineName = VMname;
		String hostName = getHostByVMName(virtualMachineName);
		this.hostName = hostName;
		file = new File("ServerLogs.txt");
		file.createNewFile();
		writer = new FileWriter(file);
		writer.write("This is the server log file: -\n \n");		
		writer.flush();
	}

	
	/**
	 * Closes the file writer for current instance
	 * 
	 * @param         
	 * @return void
	 * @throws IOException
	 */
	public void closeWriter() throws IOException{
		writer.close();
	}
	
	/**
	 * Determines whether host cpu or memory usage has exceeded threshold
	 * 
	 * @param         
	 * @return true if threshold exceeded, false otherwise
	 * @throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg
	 */
	public boolean isThresholdExceeded() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg{
		Map<ManagedObjectReference, Map<String, Object>> hosts = getMOREFs
				.inContainerByType(serviceContent.getRootFolder(),
						"HostSystem",
						hostSystemAttributesArr.toArray(new String[] {}));
		boolean isThresholdExceeded = false;
		for (ManagedObjectReference host : hosts.keySet()) {
			Map<String, Object> hostprops = hosts.get(host);
			for (String prop : hostprops.keySet()) {
				if (prop.equalsIgnoreCase(HOST_NAME)
						&& hostprops.get(prop).toString()
								.equalsIgnoreCase(hostName)) {
						if(new Integer(hostprops.get(HOST_CPU).toString()) > CPUTHRESHOLD)
							isThresholdExceeded = true;
						if(new Integer(hostprops.get(HOST_MEMORY).toString()) > MEMORYTHRESHOLD)
							isThresholdExceeded = true;
						return isThresholdExceeded;
				}
								
			}
		}
		return isThresholdExceeded;
	}
	
	/**
	 * Finalizes the target server to migrate the vm
	 * 
	 * @param         
	 * @return Target server name
	 * @throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg
	 */
	public String findTargetServer() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg{
		Map<ManagedObjectReference, Map<String, Object>> hosts = getMOREFs
				.inContainerByType(serviceContent.getRootFolder(),
						"HostSystem",
						hostSystemAttributesArr.toArray(new String[] {}));
		Map<String, Integer> vmUsage = getVmUsage();
		boolean isCpu = false;
		boolean isMemory = false;
		String targetHostName= null;
		
		Integer vmCpuUsage = vmUsage.get(VM_CPU);
		Integer vmMemUsage = vmUsage.get(VM_HOST_MEMORY);
		
		for (ManagedObjectReference host : hosts.keySet()) {
			Map<String, Object> hostprops = hosts.get(host);
			for (String prop : hostprops.keySet()) {
				if (prop.equalsIgnoreCase(HOST_NAME)&&!hostprops.get(prop).toString()
						.equalsIgnoreCase(hostName))
				{
					Integer hostCpuUsage = new Integer(hostprops.get(HOST_CPU).toString());
					Integer hostMemUsage = new Integer(hostprops.get(HOST_MEMORY).toString());	
					if(hostCpuUsage + vmCpuUsage < CPUTHRESHOLD){
						isCpu = true;
					}				
					if(hostMemUsage + vmMemUsage < MEMORYTHRESHOLD){
						isMemory = true;
					}
					if(isCpu && isMemory){
						targetHostName = hostprops.get(HOST_NAME).toString();
					}
				}				
			}
			isCpu = false;
			isMemory = false;
		}
			
		return targetHostName;
	}
	
	/**
	 * Obtains the VM usage
	 * 
	 * @param         
	 * @return map containing vm property and corresponding value
	 * @throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg
	 */
	public Map<String, Integer> getVmUsage() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg{
		final List<String> vmSystemAttributesArr = new ArrayList<String>();
		vmSystemAttributesArr.add(VM_NAME);
		vmSystemAttributesArr.add(VM_CPU);
		vmSystemAttributesArr.add(VM_HOST_MEMORY);
		//String cpuUsage = "summary.quickStats.overallCpuUsage";
		//String memUsage = "summary.quickStats.hostMemoryUsage";
		
		Map<String, Integer> vmUsage = new HashMap<String, Integer>();
		Map<ManagedObjectReference, Map<String, Object>> vms = getMOREFs
				.inContainerByType(serviceContent.getRootFolder(),
						"VirtualMachine",
						vmSystemAttributesArr.toArray(new String[] {}));		
		for (ManagedObjectReference vm : vms.keySet()) {
			Map<String, Object> vmprops = vms.get(vm);
			for (String prop : vmprops.keySet()) {
				if (prop.equalsIgnoreCase(VM_NAME)
						&& vmprops.get(prop).toString()
								.equalsIgnoreCase(virtualMachineName)) {					
						vmUsage.put(VM_CPU, new Integer(vmprops.get(VM_CPU).toString()));
						vmUsage.put(VM_HOST_MEMORY, new Integer(vmprops.get(VM_HOST_MEMORY).toString()));
						return vmUsage;
				}	
				
			}
		}
		return null;		
	}
	
	/**
	 * Method used to track the server utilization
	 * 
	 * @param         
	 * @return void
	 * @throws Throwable 
	 */
	public void track() throws Throwable{
		System.out.println("Tracking Host Memory and Cpu Usage");
		if(!isThresholdExceeded()){
			return;
		}
		System.out.println("Server Threshold Exceeded.");
		System.out.println("Preparing to Migrate/Relocate Server");
		String targetServer = findTargetServer();
		System.out.println("Target Server for VM Migration: "+targetServer);
		if(targetServer == null){
			System.out.println("No eligible target servers found. VM Migration not possible!!");
			return;
		}
		String[] argument = {"VMotion", "--vmname", virtualMachineName, "--targethost", targetServer, 
				"--sourcehost", hostName, "--targetpool", "riraju", 
				"--targetdatastore", "Storm_store1", "--priority", "default_Priority"};				
		Main.main(argument);		
		this.hostName = getHostByVMName(this.virtualMachineName);
	}
}
