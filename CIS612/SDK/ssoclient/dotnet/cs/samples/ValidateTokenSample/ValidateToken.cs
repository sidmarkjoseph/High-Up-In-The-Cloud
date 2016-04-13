using System;
using System.Net.Security;
using System.Security.Cryptography.X509Certificates;
using System.Xml;

namespace ValidateTokenSample
{
    class ValidateToken
    {
        # region variable declaration
        static STSService service;
        static string strAssertionId = "ID";
        static string strIssueInstant = "IssueInstant";
        static string strSubjectConfirmationNode = "saml2:SubjectConfirmation";
        static string strSubjectConfirmationMethodValueAttribute = "Method";
        static string strSubjectConfirmationMethodValueTypeBearer = "urn:oasis:names:tc:SAML:2.0:cm:bearer";
        static string strSubjectConfirmationMethodValueTypeHoK = "urn:oasis:names:tc:SAML:2.0:cm:holder-of-key";
        # endregion

        # region private function Definition

        /// <summary>
        ///  This method is used to print message if there is insufficient parameter 
        /// </summary>
        private static void PrintUsage()
        {
            Console.WriteLine("ValidateTokenSample [sso url] [username] [password]");
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

        /// <summary>
        /// Method to do intentional tampering of token to invalidate it for verification
        /// </summary>
        /// <param name="token">SAML token</param>
        private static void TamperToken(XmlElement token)
        {
            String issueInstanct = token.Attributes.GetNamedItem(strIssueInstant).Value;
            TimeSpan duration = new TimeSpan(2, 0, 0);
            String expireDate = XmlConvert.ToString(DateTime.Now.Subtract(duration), XmlDateTimeSerializationMode.Utc);
            Console.WriteLine("Changing the Issue time to " + expireDate + " from " + issueInstanct);
            token.Attributes.GetNamedItem(strIssueInstant).Value = expireDate;
        }

        /// <summary>
        /// This method is used to validate Token
        /// </summary>
        /// <param name="url">SSO Server Url</param>
        /// <param name="token">SAML Token to be validated</param>
        public static void TokenValidation(String url, XmlElement token)
        {
            service = new STSService();

            // Setting SSO Server URL
            service.Url = url;

            RequestSecurityTokenType tokenType = new RequestSecurityTokenType();
            tokenType.TokenType = TokenTypeEnum.httpdocsoasisopenorgwssxwstrust200512RSTRStatus;
            tokenType.RequestType = RequestTypeEnum.httpdocsoasisopenorgwssxwstrust200512Validate;
            tokenType.ValidateTarget = token;

            try
            {
                RequestSecurityTokenResponseType responseToken =
                    service.Validate(tokenType);
                StatusType status = (StatusType)responseToken.Status;
                //checking for the validity of token
                if (status.Code.Equals(StatusCodeEnum.httpdocsoasisopenorgwssxwstrust200512statusvalid))
                {
                    Console.WriteLine("Token Status : Valid");
                }
                else
                {
                    Console.WriteLine("Token Status : Invalid");
                    Console.WriteLine("Reason : " + status.Reason);
                }
            }
            catch (Exception ex)
            {
                Console.WriteLine("{0}", ex.Message);
            }
        }
        # endregion

        # region public function Definition

        /// <summary>
        /// Main function of the application
        /// </summary>
        /// <param name="args">string args [sso url] [username] [password]</param>
        static void Main(string[] args)
        {
            if (args.Length < 3)
            {
                PrintUsage();
            }
            else
            {
                XmlElement token = AcquireBearerTokenByUserCredentialSample.AcquireBearerTokenByUserCredential.GetToken(args);
                PrintToken(token);
                if (token != null)
                {
                    TokenValidation(args[0], token);
                    Console.WriteLine("Tampering the token locally - Validation should fail now");
                    TamperToken(token);
                    TokenValidation(args[0], token);
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
