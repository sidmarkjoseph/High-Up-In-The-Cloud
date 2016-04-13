using System;
using System.Collections.Generic;
using System.Text;
using System.Security.Cryptography.X509Certificates;
using System.Xml;

using Microsoft.Web.Services3;
using Microsoft.Web.Services3.Design;
using Microsoft.Web.Services3.Security;
using Microsoft.Web.Services3.Security.Tokens;

namespace ValidateTokenSample
{
    class CustomSecurityAssertion : SecurityPolicyAssertion
    {
        public String username;
        public String password;

        public String Username
        {
            get { return username; }
            set { username = value; }
        }

        public String Password
        {
            get { return password; }
            set { password = value; }
        }
        public CustomSecurityAssertion()
            : base()
        { }

        public override SoapFilter CreateClientOutputFilter(FilterCreationContext context)
        {
            return new CustomSecurityClientOutputFilter(this);
        }

        public override SoapFilter CreateClientInputFilter(FilterCreationContext context)
        {
            return null;
        }

        public override SoapFilter CreateServiceInputFilter(FilterCreationContext context)
        {
            return null;
        }

        public override SoapFilter CreateServiceOutputFilter(FilterCreationContext context)
        {
            return null;
        }
    }

    class CustomSecurityClientOutputFilter : SendSecurityFilter
    {
        UsernameToken userToken = null;
        X509SecurityToken signatureToken = null;
        MessageSignature sig = null;

        public CustomSecurityClientOutputFilter(CustomSecurityAssertion parentAssertion)
            : base(parentAssertion.ServiceActor, true)
        {
            String username = parentAssertion.username;
            String password = parentAssertion.password;

            userToken = new UsernameToken(username.Trim(), password.Trim(), PasswordOption.SendPlainText);
            signatureToken = GetSecurityToken("CN=TestSSSCert");
            sig = new MessageSignature(signatureToken);
        }

        /// <summary>
        ///  SecureMessage 
        /// </summary>
        /// <param name="envelope">SoapEnvelope</param>
        /// <param name="security">Security</param>
        public override void SecureMessage(SoapEnvelope envelope, Security security)
        {
            security.Tokens.Add(userToken);
            security.Tokens.Add(signatureToken);
            security.Elements.Add(sig);
        }

        /// <summary>
        ///  This method is used to extract the security token from certificate 
        /// </summary>
        /// <param name="subjectName">string</param>   
        internal static X509SecurityToken GetSecurityToken(string subjectName)
        {
            X509SecurityToken securityToken = null;
            X509Store store = new X509Store(StoreName.My, StoreLocation.CurrentUser);

            X509Certificate2 certToBeAdded = new X509Certificate2(ValidateTokenSample.Properties.Resources._1234);
            store = new X509Store(StoreName.TrustedPeople, StoreLocation.CurrentUser);
            store.Open(OpenFlags.ReadWrite);
            store.Add(certToBeAdded);

            store.Open(OpenFlags.ReadOnly);
            try
            {
                X509Certificate2Collection certs = store.Certificates.Find(X509FindType.FindBySubjectDistinguishedName, subjectName, false);
                X509Certificate2 cert;
                if (certs.Count == 1)
                {
                    cert = certs[0];
                    securityToken = new X509SecurityToken(cert);
                }
                else
                {
                    securityToken = null;
                }
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex.ToString());
            }
            finally
            {
                if (store != null)
                    store.Close();
            }
            return securityToken;
        }
    }
}
