using System.Security.Cryptography.X509Certificates;

using Microsoft.Web.Services3;
using Microsoft.Web.Services3.Design;

namespace AcquireHoKTokenBySolutionCertificateSample
{
    class CustomSecurityAssertion : SecurityPolicyAssertion
    {

        public X509Certificate2 SolutionCertificate { get; set; }

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
}
