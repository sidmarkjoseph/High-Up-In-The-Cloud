package com.syr.cc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import com.vmware.common.annotations.Sample;
import com.vmware.common.ssl.TrustAllTrustManager;
import com.vmware.connection.BasicConnection;
import com.vmware.connection.ConnectedVimServiceBase;
import com.vmware.connection.Connection;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.ServiceContent;

/**
 * <pre>
 * CloudManager
 * 
 * This sample which monitors the CPU and memory of VMs and migrates the VM if threshold exceeds. 
 * 
 * <b>Parameters:</b>
 * url         [required] : url of the web service
 * username    [required] : username for the authentication
 * password    [required] : password for the authentication
 * vmname      [required] : Name of the virtual machine
 * 
 * <b>Command Line:</b>
 * Create an alarm AlarmABC on a virtual machine
 * run.bat com.vmware.vm.VMPowerStateAlarm --url [webserviceurl]
 * --username [username] --password  [password] --vmname [vmname] --alarm [alarm]
 * </pre>
 */
@Sample(name = "VMManager", description = "This is the sample which monitors the server utilization and reallocates VMs")
public class VMManager extends ConnectedVimServiceBase {

	public static void main(String args[]) throws RuntimeFaultFaultMsg,
			IOException, InterruptedException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				System.in));
		String userName = null;
		String passWord = null;
		String serverIp = null;
		ServerMonitor svrMtr = null;
		VMMonitor vmMtr = null;
		// Read the username, password and servername from user
		try {
			System.out.println("Please enter username: ");
			userName = reader.readLine();
			System.out.println("Please enter password: ");
			passWord = reader.readLine();
			System.out.println("Please enter servername: ");
			serverIp = reader.readLine();

			// Username and serverIp are mandatory
			if (userName == null || serverIp == null) {
				System.out
						.println("Please enter username and servername. Exiting service!!!");
				return;
			}
			String url = "https://" + serverIp + "/sdk/vimService";

			// Set up connection using BasicConnection utility
			Connection connection = null;
			connection = new BasicConnection();
			connection.getServiceInstanceReference();

			// Declare a host name verifier that will automatically enable
			// the connection. The host name verifier is invoked during
			// the SSL handshake.
			javax.net.ssl.HostnameVerifier verifier = new HostnameVerifier() {
				public boolean verify(String urlHostName, SSLSession session) {
					return true;
				}
			};
			// Create the trust manager.
			javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];
			javax.net.ssl.TrustManager trustManager = new TrustAllTrustManager();
			trustAllCerts[0] = trustManager;

			// Create the SSL context
			javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext
					.getInstance("SSL");

			// Create the session context
			javax.net.ssl.SSLSessionContext sslsc = sc
					.getServerSessionContext();

			// Initialize the contexts; the session context takes the trust
			// manager.
			sslsc.setSessionTimeout(0);
			sc.init(null, trustAllCerts, null);

			// Use the default socket factory to create the socket for the
			// secure connection
			javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc
					.getSocketFactory());
			// Set the default host name verifier to enable the connection.
			HttpsURLConnection.setDefaultHostnameVerifier(verifier);

			//Set the connection properties
			connection.setUsername(userName);
			connection.setPassword(passWord);
			connection.setUrl(url);
			connection.connect();

			//Additional information printed to confirm that connection is successful
			ServiceContent serviceContent = connection.getServiceContent();
			System.out.println(serviceContent.getAbout().getFullName());
			System.out.printf("Server type is %s\n", serviceContent.getAbout()
					.getApiType() + "\n");
			System.out.printf("API version is %s", serviceContent.getAbout()
					.getVersion() + "\n");

			//Instantiate server monitor and vm monitor
			svrMtr = new ServerMonitor();
			vmMtr = new VMMonitor();
			
			//Read the vmname from user
			System.out.println("Please enter the virtual machine name: ");
			String VMname = null;
			VMname = reader.readLine();

			//Initialize the server monitor and vm monitor
			svrMtr.init(connection, VMname);
			vmMtr.init(connection, VMname);
			Long startTime = System.currentTimeMillis();

			//Run the server monitor and vm monitor infinitely
			while (true) {
				svrMtr.printHostProductDetails();
				vmMtr.printVMDetails();
				System.out.println("Sleeping 15 seconds...");
				Thread.sleep(15 * 1000);

				Long currentTime = System.currentTimeMillis();
				long timeLapse = currentTime - startTime;
				
				//If elapsed time is 3 minutes, call the track method
				if (timeLapse >= 3 * 60 * 1000) {
					System.out.println("Time Elapsed " + timeLapse
							+ " milliseconds");
					svrMtr.track();
					startTime = System.currentTimeMillis();
				}

			}
		} catch (IOException e) {			
			System.out.println("Error reading inputs. Exiting service!!!"+e.getMessage());
			return;
		} 
		catch (Throwable e) {			
			System.out.println("GenericException thrown. "+e.getMessage());
			return;
		}
		finally {
			svrMtr.closeWriter();
			vmMtr.closeWriter();
		}
	}

}
