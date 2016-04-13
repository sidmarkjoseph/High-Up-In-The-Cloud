namespace LoginByToken
{
    using System;
    using System.Configuration;
    using System.Net;
    using System.Security.Cryptography.X509Certificates;
    using System.Xml;
    using Microsoft.Web.Services3.Design;
    using Microsoft.Web.Services3.Security.Tokens;
    using Vim25Api;

    public class LoginByTokenSample
    {
        #region Private members

        static string strAssertionId = "ID";
        static string strIssueInstant = "IssueInstant";
        static string strSamlV2TokenType = "urnoasisnamestcSAML20assertion";
        static string strSubjectConfirmationNode = "saml2:SubjectConfirmation";
        static string strSubjectConfirmationMethodValueAttribute = "Method";
        static string strSubjectConfirmationMethodValueTypeBearer = "urn:oasis:names:tc:SAML:2.0:cm:bearer";
        static string strSubjectConfirmationMethodValueTypeHoK = "urn:oasis:names:tc:SAML:2.0:cm:holder-of-key";

        private VimService _service;
        private ManagedObjectReference _svcRef;
        private ServiceContent _sic;
        private string _serverUrl;

        #endregion
        /// <summary>
        /// Resetting the VimService without the security policies
        /// as we need the policy only for the LoginByToken method
        /// and not the other API. The method also maintains the 
        /// authenticated session cookie post LoginByLogin.
        /// 
        /// This method needs to be called only after successful 
        /// login
        /// </summary>
        private void resetService()
        {
            var _cookie = getCookie();
            _service = new VimService();
            _service.Url = _serverUrl;
            _service.CookieContainer = new CookieContainer();
            if (_cookie != null)
            {
                _service.CookieContainer.Add(_cookie);
            }
        }

        /// <summary>
        /// Method to save the session cookie
        /// </summary>
        private Cookie getCookie()
        {
            if (_service != null)
            {
                var container = _service.CookieContainer;
                if (container != null)
                {
                    var _cookies = container.GetCookies(new Uri(_service.Url));
                    if (_cookies.Count > 0)
                    {
                        return _cookies[0];
                    }
                }
            }
            return null;
        }
        /// <summary>
        /// Login by using holder-of-key SAML token
        /// </summary>
        /// <param name="xmlToken">Holder-of-key saml token</param>
        private SecurityPolicyAssertion GetSecurityPolicyAssertionForHokToken(XmlElement xmlToken)
        {
            Console.WriteLine();
            Console.WriteLine("Trying to login to server '{0}' by using Holder-of-Key token ...", _service.Url);

            //When this property is set to true, client requests that use the POST method 
            //expect to receive a 100-Continue response from the server to indicate that 
            //the client should send the data to be posted. This mechanism allows clients 
            //to avoid sending large amounts of data over the network when the server, 
            //based on the request headers, intends to reject the request
            ServicePointManager.Expect100Continue = true;
            ServicePointManager.SecurityProtocol = SecurityProtocolType.Tls;

            X509Certificate2 certificateToBeAdded = new X509Certificate2();
            string certificateFile = ConfigurationManager.AppSettings["PfxCertificateFile"];
            string password = ConfigurationManager.AppSettings["PfxCertificateFilePassword"];
            certificateToBeAdded.Import(certificateFile, password ?? string.Empty, X509KeyStorageFlags.MachineKeySet);

            var customSecurityAssertion = new CustomSecurityAssertionHok();
            customSecurityAssertion.BinaryToken = xmlToken;
            customSecurityAssertion.TokenType = strSamlV2TokenType;
            customSecurityAssertion.SecurityToken = new X509SecurityToken(certificateToBeAdded);

            return customSecurityAssertion;
        }

        /// <summary>
        /// Login by using bearer token
        /// </summary>
        /// <param name="xmlBearerToken">bearer saml token</param>
        private SecurityPolicyAssertion GetSecurityPolicyAssertionForBearerToken(XmlElement xmlBearerToken)
        {
            Console.WriteLine();
            Console.WriteLine("Trying to login to server '{0}' by using Bearer token ...", _service.Url);

            //When this property is set to true, client requests that use the POST method 
            //expect to receive a 100-Continue response from the server to indicate that 
            //the client should send the data to be posted. This mechanism allows clients 
            //to avoid sending large amounts of data over the network when the server, 
            //based on the request headers, intends to reject the request
            ServicePointManager.Expect100Continue = true;
            ServicePointManager.SecurityProtocol = SecurityProtocolType.Tls;

            var customSecurityAssertion = new CustomSecurityAssertionBearer();
            customSecurityAssertion.BinaryToken = xmlBearerToken;

            return customSecurityAssertion;
        }

        /// <summary>
        /// Creates new LoginByTokenSample object
        /// </summary>
        /// <param name="serverUrl">Server url to login to</param>
        public LoginByTokenSample(string serverUrl)
        {
            _service = new VimService();
            _service.Url = serverUrl;
            _serverUrl = serverUrl;
            _service.CookieContainer = new System.Net.CookieContainer();

            _svcRef = new ManagedObjectReference();
            _svcRef.type = "ServiceInstance";
            _svcRef.Value = "ServiceInstance";
        }

        /// <summary>
        /// Calls the right login method based on the SAML token type (bearer or holder-of-Key)
        /// </summary>
        /// <param name="xmlToken">Token</param>
        public void LoginByToken(XmlElement xmlToken)
        {
            var subjectConfirmationNode =
                xmlToken.GetElementsByTagName(strSubjectConfirmationNode).Item(0);

            var subjectConfirmationMethodValue =
                subjectConfirmationNode.Attributes.GetNamedItem(strSubjectConfirmationMethodValueAttribute).Value;
            SecurityPolicyAssertion securityPolicyAssertion = null;
            if (subjectConfirmationMethodValue == strSubjectConfirmationMethodValueTypeHoK)
            {
                securityPolicyAssertion = GetSecurityPolicyAssertionForHokToken(xmlToken);
            }
            else if (subjectConfirmationMethodValue == strSubjectConfirmationMethodValueTypeBearer)
            {
                securityPolicyAssertion = GetSecurityPolicyAssertionForBearerToken(xmlToken);
            }

            //Setting up the security policy for the request
            Policy policySAML = new Policy();
            policySAML.Assertions.Add(securityPolicyAssertion);

            // Setting policy of the service
            _service.SetPolicy(policySAML);

            _sic = _service.RetrieveServiceContent(_svcRef);
            if (_sic.sessionManager != null)
            {
                _service.LoginByToken(_sic.sessionManager, null);
            }
            resetService();
        }

        /// <summary>
        /// Simple method to query and print the server time
        /// </summary>
        public void PrintTime()
        {
            Console.WriteLine("Getting server time!");
            Console.WriteLine("Server time is {0}", _service.CurrentTime(_svcRef));
        }

        /// <summary>
        /// Prints basic information about a token
        /// </summary>
        /// <param name="token">SAML Token</param>
        public void PrintToken(XmlElement token)
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
        /// Disconnects the Connection
        /// </summary>
        public void Logout()
        {
            if (_service != null)
            {
                _service.Logout(_sic.sessionManager);
                Console.WriteLine("Logged out successfully");
            }
        }

        /// <summary>
        /// Main method
        /// </summary>
        /// <param name="args">Expects four arguments
        /// (vCenter single sign on server url, username, password, and vCenter server url)</param>
        public static void Main(string[] args)
        {
            if (args != null && args.Length >= 4)
            {
                var ssoArgs = new string[] { args[0], args[1], args[2] };
                var serverUrl = args[3];

                var program = new LoginByTokenSample(serverUrl);
                Console.WriteLine("------------------------------------");
                Console.WriteLine("Acquiring Bearer token");
                var xmlBearerToken =
                    AcquireBearerTokenByUserCredentialSample.AcquireBearerTokenByUserCredential.GetToken(ssoArgs);
                program.PrintToken(xmlBearerToken);
                Console.WriteLine("Performing loginByToken using the Bearer token above");
                program.LoginByToken(xmlBearerToken);
                program.PrintTime();
                program.Logout();
                Console.WriteLine("------------------------------------");
                Console.WriteLine("Acquiring HolderOfKey token");
                var xmlHokToken =
                    AcquireHoKTokenByUserCredentialSample.AcquireHoKTokenByUserCredential.GetToken(ssoArgs);
                program.PrintToken(xmlHokToken);
                Console.WriteLine("Performing loginByToken using the HolderOfKey token above");
                program.LoginByToken(xmlHokToken);
                program.PrintTime();
                program.Logout();
                Console.ReadLine();
            }
            else
            {
                //print usage
                Console.WriteLine("Usage: LoginByToken [sso url] [sso username] [sso password] [server url]");
            }
        }
    }
}
