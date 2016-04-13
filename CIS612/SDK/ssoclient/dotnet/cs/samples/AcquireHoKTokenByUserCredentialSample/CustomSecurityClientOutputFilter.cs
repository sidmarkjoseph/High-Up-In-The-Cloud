namespace AcquireHoKTokenByUserCredentialSample
{
    using System.Configuration;
    using System.Security.Cryptography.X509Certificates;
    using Microsoft.Web.Services3;
    using Microsoft.Web.Services3.Security;
    using Microsoft.Web.Services3.Security.Tokens;

    internal class CustomSecurityClientOutputFilter : SendSecurityFilter
    {
        UsernameToken userToken = null;
        X509SecurityToken signatureToken = null;
        MessageSignature sig = null;

        public CustomSecurityClientOutputFilter(CustomSecurityAssertion parentAssertion)
            : base(parentAssertion.ServiceActor, true)
        {
            userToken = new UsernameToken(parentAssertion.Username.Trim(), parentAssertion.Password.Trim(), PasswordOption.SendPlainText);
            signatureToken = GetSecurityToken();
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
        ///  This method is used to create the security token from certificate from pfx file
        /// </summary>           
        internal static X509SecurityToken GetSecurityToken()
        {
            X509Certificate2 certificateToBeAdded = new X509Certificate2();
            string certificateFile = ConfigurationManager.AppSettings["PfxCertificateFile"];
            certificateToBeAdded.Import(certificateFile, "", X509KeyStorageFlags.MachineKeySet);
            return new X509SecurityToken(certificateToBeAdded);
        }
    }
}
