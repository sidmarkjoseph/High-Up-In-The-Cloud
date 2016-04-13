using System;
using System.Collections.Generic;
using System.Text;
using Microsoft.Web.Services3.Security;

using Microsoft.Web.Services3;
using Microsoft.Web.Services3.Design;
using System.Security.Cryptography.X509Certificates;
using Microsoft.Web.Services3.Security.Tokens;

namespace AcquireHoKTokenBySolutionCertificateSample
{
    class CustomSecurityClientOutputFilter : SendSecurityFilter
    {
        X509SecurityToken solutionCertificateToken = null;
        MessageSignature sig = null;

        public CustomSecurityClientOutputFilter(CustomSecurityAssertion parentAssertion)
            : base(parentAssertion.ServiceActor, true)
        {
            X509Certificate2 solutionCertificate = parentAssertion.SolutionCertificate;
            solutionCertificateToken = new X509SecurityToken(solutionCertificate);
            sig = new MessageSignature(solutionCertificateToken);
        }

        /// <summary>
        ///  SecureMessage 
        /// </summary>
        /// <param name="envelope">SoapEnvelope</param>
        /// <param name="security">Security</param>
        public override void SecureMessage(SoapEnvelope envelope, Security security)
        {
            security.Tokens.Add(solutionCertificateToken);
            security.Elements.Add(sig);
        }
    }
}
