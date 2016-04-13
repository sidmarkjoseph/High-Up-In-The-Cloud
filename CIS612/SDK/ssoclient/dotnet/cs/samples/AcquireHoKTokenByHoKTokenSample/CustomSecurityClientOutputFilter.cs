using System;
using System.Configuration;
using System.Security.Cryptography.X509Certificates;
using System.Xml;
using Microsoft.Web.Services3;
using Microsoft.Web.Services3.Design;
using Microsoft.Web.Services3.Security;
using Microsoft.Web.Services3.Security.Tokens;
using System.Security.Cryptography.Xml;

namespace AcquireHoKTokenByHoKTokenSample
{
    class CustomSecurityClientOutputFilter : SendSecurityFilter
    {
        UsernameToken userToken = null;
        X509SecurityToken signatureToken = null;
        MessageSignature sig = null;
        IssuedToken issuedToken = null;
        string samlAssertionId = null;

        public CustomSecurityClientOutputFilter(CustomSecurityAssertion parentAssertion)
            : base(parentAssertion.ServiceActor, true)
        {
            if (parentAssertion.BinaryToken == null)
            {
                userToken = new UsernameToken(parentAssertion.Username.Trim(), parentAssertion.Password.Trim(), PasswordOption.SendPlainText);
                signatureToken = GetSecurityToken();
                parentAssertion.SecurityToken = signatureToken;
            }
            else
            {
                issuedToken = new IssuedToken(parentAssertion.BinaryToken);
                signatureToken = parentAssertion.SecurityToken;
                samlAssertionId = parentAssertion.BinaryToken.Attributes.GetNamedItem("ID").Value;
            }
            sig = new MessageSignature(signatureToken);
        }

        /// <summary>
        ///  SecureMessage 
        /// </summary>
        /// <param name="envelope">SoapEnvelope</param>
        /// <param name="security">Security</param>
        public override void SecureMessage(SoapEnvelope envelope, Security security)
        {
            if (issuedToken == null)
            {
                security.Tokens.Add(userToken);
                security.Tokens.Add(signatureToken);
                security.Elements.Add(sig);
            }
            else
            {
                //create KeyInfo XML element
                sig.KeyInfo = new KeyInfo();
                sig.KeyInfo.LoadXml(CreateKeyInfoSignatureElement());

                security.Tokens.Add(issuedToken);
                security.Elements.Add(sig);
            }
        }

        /// <summary>
        ///  This method is used to create the security token from certificate from pfx file
        /// </summary>           
        internal static X509SecurityToken GetSecurityToken()
        {
            X509Certificate2 certificateToBeAdded = new X509Certificate2();
            string certificateFile = ConfigurationManager.AppSettings["PfxCertificateFile"];
            certificateToBeAdded.Import(certificateFile, "", X509KeyStorageFlags.MachineKeySet);
            return new X509SecurityToken(certificateToBeAdded);
        }

        /// <summary>
        /// Helper method to create a custom key info signature element
        /// </summary>
        /// <returns>Key info XML element</returns>
        internal XmlElement CreateKeyInfoSignatureElement()
        {
            var xmlDocument = new XmlDocument();
            xmlDocument.LoadXml(@"<root><SecurityTokenReference 
                xmlns=""http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd"" 
                xmlns:wsse=""http://docs.oasis-open.org/wss/oasis-wss-wssecurity-secext-1.1.xsd"" 
                wsse:TokenType=""http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLV2.0"">
                <KeyIdentifier xmlns=""http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd"" 
                ValueType=""http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLID"">" + samlAssertionId +
                @"</KeyIdentifier></SecurityTokenReference></root>");
            return xmlDocument.DocumentElement;
        }
    }
}
