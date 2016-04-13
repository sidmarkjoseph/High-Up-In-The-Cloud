package com.syr.cc;

import com.vmware.connection.ConnectedVimServiceBase;
import com.vmware.connection.Connection;
import com.vmware.connection.helpers.GetMOREF;
import com.vmware.vim25.*;

import javax.xml.ws.soap.SOAPFaultException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class VMMonitor extends ConnectedVimServiceBase implements VMManagementConstants{

	private static final List<String> vmSystemAttributesArr = new ArrayList<String>();
	File file;
	FileWriter writer;

	private String virtualMachineName;

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
	 * Initializes the vm monitor
	 * 
	 * @param connection, VMname        
	 * @return void
	 * @throws Exception
	 */
	public void init(Connection connection, String VMname) throws IOException {
		this.serviceContent = connection.getServiceContent();
		this.vimPort = connection.getVimPort();
		this.getMOREFs = new GetMOREF(connection);

		setVMSystemAttributesList();
		this.virtualMachineName = VMname;
		file = new File("VMLogs.txt");
		file.createNewFile();
		writer = new FileWriter(file);
		writer.write("This is the VM log file: -\n \n");		
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
	 * Sets the vm system attributes list
	 * 
	 * @param        
	 * @return void
	 */
	public void setVMSystemAttributesList() {
		vmSystemAttributesArr.add(VM_NAME);
		vmSystemAttributesArr.add(VM_CPU);
		vmSystemAttributesArr.add(VM_HOST_MEMORY);
		vmSystemAttributesArr.add(VM_GUEST_MEMORY);		
	}

	/**
	 * Prints vm quickstat details
	 * 
	 * @param vmName        
	 * @return void
	 * @throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, IOException
	 */
	public void printVMDetails() throws InvalidPropertyFaultMsg,
			RuntimeFaultFaultMsg, IOException {
		String vmName = this.virtualMachineName;
		Calendar calendar = Calendar.getInstance();
		/*String cpuUsage = "summary.quickStats.overallCpuUsage";
		String hostMemUsage = "summary.quickStats.hostMemoryUsage";
		String guestMemUsage = "summary.quickStats.guestMemoryUsage";
		String name = "name";*/
		writer.append("Logging Time: "+
				calendar.get(Calendar.HOUR)+":"+calendar.get(Calendar.MINUTE)+":"+
				calendar.get(Calendar.SECOND)+":"+calendar.get(Calendar.MILLISECOND)+" "+
				calendar.get(Calendar.MONTH)+"/"+calendar.get(Calendar.DAY_OF_MONTH)+"/"+
				calendar.get(Calendar.YEAR)+"\n");
		Map<ManagedObjectReference, Map<String, Object>> vms = getMOREFs
				.inContainerByType(serviceContent.getRootFolder(),
						"VirtualMachine",
						vmSystemAttributesArr.toArray(new String[] {}));

		for (ManagedObjectReference vm : vms.keySet()) {
			Map<String, Object> vmprops = vms.get(vm);
			for (String prop : vmprops.keySet()) {
				if (prop.equalsIgnoreCase("name")
						&& vmprops.get(prop).toString()
								.equalsIgnoreCase(vmName)) {
					System.out.println(VM_NAME + " : " + vmprops.get(VM_NAME));
					writer.append(VM_NAME + " : " + vmprops.get(VM_NAME)+"\n");
					System.out.println(VM_CPU + " : " + vmprops.get(VM_CPU));
					writer.append(VM_CPU + " : " + vmprops.get(VM_CPU)+"\n");
					System.out.println(VM_HOST_MEMORY + " : " + vmprops.get(VM_HOST_MEMORY));
					writer.append(VM_HOST_MEMORY + " : " + vmprops.get(VM_HOST_MEMORY)+"\n");
					System.out.println(VM_GUEST_MEMORY + " : " + vmprops.get(VM_GUEST_MEMORY));
					writer.append(VM_GUEST_MEMORY + " : " + vmprops.get(VM_GUEST_MEMORY)+"\n");
					writer.flush();					
					return;
				}
				
			}
		}
	}

}

