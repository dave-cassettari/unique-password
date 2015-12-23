using Microsoft.Owin.Security;
using Microsoft.Owin.Security.OAuth;
using System.Collections.Generic;
using System.Security.Claims;
using System.Threading.Tasks;
using UniquePassword.Server.Helpers;
using UniquePassword.Server.Models;
using UniquePassword.Server.Models.Entities;
using UniquePassword.Server.Models.Repositories;

namespace UniquePassword.Server.Providers
{
    public class SimpleAuthorizationServerProvider : OAuthAuthorizationServerProvider
    {
        public override async Task ValidateClientAuthentication(OAuthValidateClientAuthenticationContext context)
        {
            string clientId;
            string clientSecret;

            if (!context.TryGetBasicCredentials(out clientId, out clientSecret))
            {
                context.TryGetFormCredentials(out clientId, out clientSecret);
            }

            if (context.ClientId == null)
            {
                //Remove the comments from the below line context.SetError, and invalidate context 
                //if you want to force sending clientId/secrects once obtain access tokens. 
                context.Validated();

                //context.SetError("invalid_clientId", "ClientId should be sent.");
                return;
            }

            Client client = null;

            using (var repository = new AuthRepository())
            {
                client = repository.FindClient(context.ClientId);
            }

            if (client == null)
            {
                context.SetError("invalid_clientId", string.Format("Client '{0}' is not registered in the system.", context.ClientId));

                return;
            }

            if (client.ApplicationType == ApplicationType.Native)
            {
                if (string.IsNullOrWhiteSpace(clientSecret))
                {
                    context.SetError("invalid_clientId", "Client secret should be sent.");

                    return;
                }
                else
                {
                    if (client.Secret != AuthHelper.GetHash(clientSecret))
                    {
                        context.SetError("invalid_clientId", "Client secret is invalid.");

                        return;
                    }
                }
            }

            if (!client.Active)
            {
                context.SetError("invalid_clientId", "Client is inactive.");

                return;
            }

            context.OwinContext.Set("as:clientAllowedOrigin", client.AllowedOrigin);
            context.OwinContext.Set("as:clientRefreshTokenLifeTime", client.RefreshTokenLifeTime.ToString());

            context.Validated();
        }

        public override async Task GrantResourceOwnerCredentials(OAuthGrantResourceOwnerCredentialsContext context)
        {
            var allowedOrigin = context.OwinContext.Get<string>("as:clientAllowedOrigin");

            if (allowedOrigin == null)
            {
                allowedOrigin = "*";
            }

            context.OwinContext.Response.Headers.Add("Access-Control-Allow-Origin", new[] { allowedOrigin });

            using (var repository = new AuthRepository())
            {
                var user = await repository.FindUser(context.UserName, context.Password);

                if (user == null)
                {
                    context.SetError("invalid_grant", "The user name or password is incorrect.");

                    return;
                }
            }

            var identity = new ClaimsIdentity(context.Options.AuthenticationType);

            identity.AddClaim(new Claim(ClaimTypes.Name, context.UserName));
            identity.AddClaim(new Claim("sub", context.UserName));
            identity.AddClaim(new Claim("role", "user"));

            var props = new AuthenticationProperties(new Dictionary<string, string>()
            {
                { "userName", context.UserName },
                { "as:client_id", (context.ClientId == null) ? string.Empty : context.ClientId },
            });

            var ticket = new AuthenticationTicket(identity, props);

            context.Validated(ticket);
        }

        public override Task TokenEndpoint(OAuthTokenEndpointContext context)
        {
            foreach (var property in context.Properties.Dictionary)
            {
                context.AdditionalResponseParameters.Add(property.Key, property.Value);
            }

            return Task.FromResult<object>(null);
        }

        public override Task GrantRefreshToken(OAuthGrantRefreshTokenContext context)
        {
            var currentClient = context.ClientId;
            var originalClient = context.Ticket.Properties.Dictionary["as:client_id"];

            if (originalClient != currentClient)
            {
                context.SetError("invalid_clientId", "Refresh token is issued to a different clientId.");

                return Task.FromResult<object>(null);
            }

            var newIdentity = new ClaimsIdentity(context.Ticket.Identity);
            var newTicket = new AuthenticationTicket(newIdentity, context.Ticket.Properties);

            newIdentity.AddClaim(new Claim("newClaim", "newValue"));

            context.Validated(newTicket);

            return Task.FromResult<object>(null);
        }
    }
}
