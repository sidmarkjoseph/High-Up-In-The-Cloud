namespace LoginByToken
{
    using System;
    using System.Security.Cryptography.Xml;
    using System.Xml;
    using Microsoft.Web.Services3;
    using Microsoft.Web.Services3.Design;
    using Microsoft.Web.Services3.Security;
    using Microsoft.Web.Services3.Security.Tokens;

    /// <summary>
    /// Custom policy assertion that applies security to a SOAP message exchange.
    /// </summary>
    internal class CustomSecurityAssertionHok : SecurityPolicyAssertion
    {
        public String TokenType { get; set; }

        public XmlElement BinaryToken { get; set; }

        public X509SecurityToken SecurityToken { get; set; }

        public CustomSecurityAssertionHok()
            : base()
        { }

        public override SoapFilter CreateClientOutputFilter(FilterCreationContext context)
        {
            return new CustomSecurityClientOutputFilterHok(this);
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

    /// <summary>
    /// Custom class for filtering outgoing SOAP messages that are secured
    /// by a digital signature, encryption, or authentication.
    /// </summary>
    internal class CustomSecurityClientOutputFilterHok : SendSecurityFilter
    {
        IssuedToken issuedToken = null;
        string samlAssertionId = null;
        MessageSignature messageSignature = null;

        /// <summary>
        /// Creates a custom SOAP request filter
        /// </summary>
        /// <param name="parentAssertion">Custom security assertion</param>
        public CustomSecurityClientOutputFilterHok(CustomSecurityAssertionHok parentAssertion)
            : base(parentAssertion.ServiceActor, true)
        {
            issuedToken = new IssuedToken(parentAssertion.BinaryToken, parentAssertion.TokenType);
            samlAssertionId = parentAssertion.BinaryToken.Attributes.GetNamedItem("ID").Value;
            messageSignature = new MessageSignature(parentAssertion.SecurityToken);
        }

        /// <summary>
        ///  Secures the SOAP message before its sent to the server
        /// </summary>
        /// <param name="envelope">Soap envelope</param>
        /// <param name="security">Security header element</param>
        public override void SecureMessage(SoapEnvelope envelope, Security security)
        {
            //create KeyInfo XML element
            messageSignature.KeyInfo = new KeyInfo();
            messageSignature.KeyInfo.LoadXml(CreateKeyInfoSignatureElement());

            security.Tokens.Add(issuedToken);
            security.Elements.Add(messageSignature);
        }

        /// <summary>
        /// Helper method to create a custom key info signature element
        /// </summary>
        /// <returns>Key info XML element</returns>
        private XmlElement CreateKeyInfoSignatureElement()
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
