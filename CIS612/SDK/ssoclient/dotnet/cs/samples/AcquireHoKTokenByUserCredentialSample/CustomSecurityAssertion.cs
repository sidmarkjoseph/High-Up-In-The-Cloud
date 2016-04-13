namespace AcquireHoKTokenByUserCredentialSample
{
    using System;
    using Microsoft.Web.Services3;
    using Microsoft.Web.Services3.Design;

    class CustomSecurityAssertion : SecurityPolicyAssertion
    {
        public String Username { get; set; }
        public String Password { get; set; }

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
