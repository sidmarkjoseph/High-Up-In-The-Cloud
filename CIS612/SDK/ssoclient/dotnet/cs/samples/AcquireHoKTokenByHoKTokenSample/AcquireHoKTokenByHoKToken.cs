using System;
using System.Net;
using System.Net.Security;
using System.Security.Cryptography.X509Certificates;
using System.Xml;
using Microsoft.Web.Services3;
using Microsoft.Web.Services3.Design;

namespace AcquireHoKTokenByHoKTokenSample
{
    public class AcquireHoKTokenByHoKToken
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
            Console.WriteLine("AcquireHoKTokenByHoKTokenSample [sso url] [username] [password]");
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
        ///  This method is used to get Token 
        /// </summary>
        /// <param name="args">string Array  [sso url] [username] [password]</param>        
        public static XmlElement GetToken(String[] args)
        {
            service = new STSService();

            // Setting SSO Server URL
            service.Url = args[0];

            // Request SOAP Context - Required to add secruity headers
            SoapContext requestContext = service.RequestSoapContext;

            /*Code to Acquire HoK Token By User Credential starts Here*/

            //Creating object of CustomSecurityAssertion
            CustomSecurityAssertion objCustomSecurityAssertion = new CustomSecurityAssertion();
            objCustomSecurityAssertion.Username = args[1].Trim();
            objCustomSecurityAssertion.Password = args[2].Trim();

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
            * "httpdocsoasisopenorgwssxwstrust200512Issue", as we want
            * to get a token issued
            * 
            * 4. KeyType -
            * "httpdocsoasisopenorgwssxwstrust200512PublicKey",
            * representing the kind of key the token will have. There are two
            * options namely bearer and holder-of-key
            * 
            * 5. SignatureAlgorithm -
            * "httpwwww3org200104xmldsigmorersasha256", representing the
            * algorithm used for generating signature
            * 
            * 6. Renewing - represented by the RenewingType which specifies whether
            * the token is renewable or not
            */

            tokenType.TokenType = TokenTypeEnum.urnoasisnamestcSAML20assertion;
            tokenType.RequestType = RequestTypeEnum.httpdocsoasisopenorgwssxwstrust200512Issue;
            tokenType.KeyType = KeyTypeEnum.httpdocsoasisopenorgwssxwstrust200512PublicKey;
            tokenType.SignatureAlgorithm = SignatureAlgorithmEnum.httpwwww3org200104xmldsigmorersasha256;

            LifetimeType lifetime = new LifetimeType();
            AttributedDateTime created = new AttributedDateTime();
            String createdDate = String.Format(strDateFormat, DateTime.Now.ToUniversalTime());
            created.Value = createdDate;
            lifetime.Created = created;

            AttributedDateTime expires = new AttributedDateTime();
            TimeSpan duration = new TimeSpan(1, 10, 10);
            String expireDate = String.Format(strDateFormat, DateTime.Now.Add(duration).ToUniversalTime());
            expires.Value = expireDate;
            lifetime.Expires = expires;

            tokenType.Lifetime = lifetime;

            RenewingType renewing = new RenewingType();
            renewing.Allow = false;
            renewing.OK = false;
            tokenType.Renewing = renewing;

            try
            {
                RequestSecurityTokenResponseCollectionType responseToken =
                    service.Issue(tokenType);
                RequestSecurityTokenResponseType rstResponse =
                    responseToken.RequestSecurityTokenResponse;


                XmlElement token = rstResponse.RequestedSecurityToken;
                Console.WriteLine("Original SAML Token");
                PrintToken(token);
                /*Code to Acquire HoK Token By User Credential ends Here*/

                /*Code to Acquire HoK Token By HoK Token Sample starts Here*/

                //Creating object of CustomSecurityAssertion for next token
                CustomSecurityAssertion objCustomSecurityAssertionAdditional = new CustomSecurityAssertion();
                objCustomSecurityAssertionAdditional.BinaryToken = token;
                objCustomSecurityAssertionAdditional.TokenType = rstResponse.TokenType.ToString();
                objCustomSecurityAssertionAdditional.SecurityToken = objCustomSecurityAssertion.SecurityToken;

                Policy policySAML = new Policy();
                policySAML.Assertions.Add(objCustomSecurityAssertionAdditional);

                // Setting policy of the service
                service.SetPolicy(policySAML);

                RequestSecurityTokenResponseCollectionType responseToken1 =
                    service.Issue(tokenType);
                RequestSecurityTokenResponseType rstResponse1 =
                    responseToken1.RequestSecurityTokenResponse;

                XmlElement tokenAdditional = rstResponse1.RequestedSecurityToken;
                Console.WriteLine("Renewed SAML Token");
                PrintToken(tokenAdditional);
                return tokenAdditional;

                /*Code to Acquire HoK Token By HoK Token Sample ends Here*/
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex.ToString());

                throw ex;
            }
        }

        /// <summary>
        /// Main function of the application
        /// </summary>
        /// <param name="args">string args  [sso url] [username] [password]</param>
        public static void Main(string[] args)
        {
            if (args.Length < 3)
            {
                PrintUsage();
            }
            else
            {
                GetToken(args);
            }

            Console.WriteLine("Press Any Key To Exit.");
            Console.ReadLine();
        }

        # endregion
    }
}
