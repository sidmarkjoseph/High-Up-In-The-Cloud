Sign On Using SAML Token Readme
================================

The .NET sample project contained in the SSO Client SDK demonstrates
how to sign on to a server using a SAML token.

Requirements
-------------
Microsoft .NET Framework 3.5+
Microsoft Visual Studio 2008+
Microsoft WSE 3.0 (Runtime and Tools)

Setup Instructions
-------------------
The following list provides a brief overview of the steps to set up
the development environment and use the sample solution. See the
Developer's Setup Guide for more information.

1. Setup the development work environment - Microsoft Visual programming environment,
   Microsoft .NET Framework, and Microsoft Web Services Enhancements (WSE).

2. Generate a test certificate and STSService stubs using the build.bat file provided with the C#/.NET SSO samples.

3. Define environment variables for vSphere Web Services files (WS_SDK_HOME and WSDLHOME) and
   Microsoft WSE (WSE_HOME). Add the WSE tools directory to the PATH environment variable.

4. Generate the VimService and XmlSerializer DLLs.

5. Open the SSOSample.sln file using Visual Studio 2008 or above to compile and run the samples




Acquire HoK Token By Solution Certificate Sample Readme
==========================================================

1. This sample expects the server certificate and private key file as PFX file format
and the private key password. For example: 

	AcquireHoKTokenBySolutionCertificateSample https://ip:7444/ims/STSService rui.pfx mypassword

pfx stands for personal exchange format and is used to exchange public as well as public and private
objects in a single file.
