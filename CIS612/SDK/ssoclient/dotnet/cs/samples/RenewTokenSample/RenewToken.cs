using System;
using System.Net;
using System.Net.Security;
using System.Security.Cryptography.X509Certificates;
using System.Xml;
using Microsoft.Web.Services3.Design;

namespace RenewTokenSample
{
    public class RenewToken
    {
        # region variable declaration
        static STSService service;
        static string strAssertionId = "ID";
        static string strIssueInstant = "IssueInstant";
        static string strSubjectConfirmationNode = "saml2:SubjectConfirmation";
        static string strSubjectConfirmationMethodValueAttribute = "Method";
        static string strSubjectConfirmationMethodValueTypeBearer = "urn:oasis:names:tc:SAML:2.0:cm:bearer";
        static string strSubjectConfirmationMethodValueTypeHoK = "urn:oasis:names:tc:SAML:2.0:cm:holder-of-key";
        static string strDateFormat = "{0:yyyy'-'MM'-'dd'T'HH':'mm':'ss.fff'Z'}";
        # endregion

        # region private function Definition

        /// <summary>
        ///  This method is used to print message if there is insufficient parameter 
        /// </summary>
        private static void PrintUsage()
        {
            Console.WriteLine("RenewTokenSample [sso url] [username] [password] [durationInSeconds]");
        }

        /// <summary>
        ///  This method ignores the server certificate validation
        ///  THIS IS ONLY FOR SAMPLES USE. PROVIDE PROPER VALIDATION FOR PRODUCTION CODE.
        /// </summary>
        /// <param name="sender">string Array</param>
        /// <param name="certificate">X509Certificate certificate</param>
        /// <param name="chain">X509Chain chain</param>
        /// <param name="policyErrors">SslPolicyErrors policyErrors</param>
        private static bool ValidateRemoteCertificate(object sender, X509Certificate certificate, X509Chain chain, SslPolicyErrors policyErrors)
        {
            return true;
        }

        /// <summary>
        /// Prints basic information about the token
        /// </summary>
        /// <param name="token">SAML Token</param>
        private static void PrintToken(XmlElement token)
        {
            if (token != null)
            {
                String assertionId = token.Attributes.GetNamedItem(strAssertionId).Value;
                String issueInstanct = token.Attributes.GetNamedItem(strIssueInstant).Value;
                String typeOfToken = "";
                XmlNode subjectConfirmationNode = token.GetElementsByTagName(strSubjectConfirmationNode).Item(0);
                String subjectConfirmationMethodValue = subjectConfirmationNode.Attributes.GetNamedItem(strSubjectConfirmationMethodValueAttribute).Value;
                if (subjectConfirmationMethodValue == strSubjectConfirmationMethodValueTypeHoK)
                {
                    typeOfToken = "Holder-Of-Key";
                }
                else if (subjectConfirmationMethodValue == strSubjectConfirmationMethodValueTypeBearer)
                {
                    typeOfToken = "Bearer";
                }
                Console.WriteLine("Token Details");
                Console.WriteLine("\tAssertionId =  " + assertionId);
                Console.WriteLine("\tToken Type =  " + typeOfToken);
                Console.WriteLine("\tIssued On =  " + issueInstanct);
            }
        }
        # endregion

        # region public function Definition

        /// <summary>
        ///  This method is used to renew Token 
        /// </summary>
        /// <param name="args">string Array  [sso url] [username] [password] [durationInSeconds]</param>        
        public static XmlElement GetRenewToken(String url, XmlElement token, int durationInSeconds)
        {
            service = new STSService();

            // Setting SSO Server URL
            service.Url = url;

            //Creating object of CustomSecurityAssertion
            CustomSecurityAssertion objCustomSecurityAssertion = new CustomSecurityAssertion();
            objCustomSecurityAssertion.BinaryToken = token;

            Policy policy = new Policy();
            policy.Assertions.Add(objCustomSecurityAssertion);

            service.SetPolicy(policy);

            ServicePointManager.ServerCertificateValidationCallback += new RemoteCertificateValidationCallback(
                 ValidateRemoteCertificate
             );

            RequestSecurityTokenType tokenType = new RequestSecurityTokenType();
            /**
            * For this request we need at least the following element in the
            * RequestSecurityTokenType set
            * 
            * 1. Lifetime - represented by LifetimeType which specifies the
            * lifetime for the token to be issued
            * 
            * 2. Tokentype - "urnoasisnamestcSAML20assertion", which is the
            * class that models the requested token
            * 
            * 3. RequestType -
            * "httpdocsoasisopenorgwssxwstrust200512Renew", as we want
            * to get a token issued
            * 
            */
            tokenType.TokenType = TokenTypeEnum.urnoasisnamestcSAML20assertion;
            tokenType.RequestType = RequestTypeEnum.httpdocsoasisopenorgwssxwstrust200512Renew;
            tokenType.RenewTarget = token;

            LifetimeType lifetime = new LifetimeType();
            AttributedDateTime created = new AttributedDateTime();
            String createdDate = String.Format(strDateFormat, DateTime.Now.ToUniversalTime());
            created.Value = createdDate;
            lifetime.Created = created;

            AttributedDateTime expires = new AttributedDateTime();
            TimeSpan duration = new TimeSpan(0, 0, durationInSeconds);
            String expireDate = String.Format(strDateFormat, DateTime.Now.Add(duration).ToUniversalTime());
            expires.Value = expireDate;
            lifetime.Expires = expires;

            try
            {
                RequestSecurityTokenResponseType responseToken =
                    service.Renew(tokenType);
                return responseToken.RequestedSecurityToken;

            }
            catch (Exception ex)
            {
                Console.WriteLine("Exception : " + ex.Message);
                throw ex;
            }
        }

        /// <summary>
        /// Main function of the application
        /// </summary>
        /// <param name="args">string args</param>
        public static void Main(string[] args)
        {
            if (args.Length < 4)
            {
                PrintUsage();
            }
            else
            {
                XmlElement token = AcquireHoKTokenByUserCredentialSample.AcquireHoKTokenByUserCredential.GetToken(args);
                Console.WriteLine("Original SAML Token");
                PrintToken(token);
                if (token != null)
                {
                    Console.WriteLine("Renewed SAML Token");
                    PrintToken(GetRenewToken(args[0], token, Int32.Parse(args[3])));
                }
                else
                {
                    Console.WriteLine("Not able to get token from SSO server");
                }
            }
            Console.WriteLine("Press Any Key To Exit.");
            Console.Read();
        }
        # endregion
    }
}
