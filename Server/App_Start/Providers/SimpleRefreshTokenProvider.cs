using Microsoft.Owin.Security.Infrastructure;
using System;
using System.Threading.Tasks;
using UniquePassword.Server.Helpers;
using UniquePassword.Server.Models.Entities;
using UniquePassword.Server.Models.Repositories;

namespace UniquePassword.Server.App_Start.Providers
{
    public class SimpleRefreshTokenProvider : IAuthenticationTokenProvider
    {
        public void Create(AuthenticationTokenCreateContext context)
        {
            CreateAsync(context).Wait();
        }

        public async Task CreateAsync(AuthenticationTokenCreateContext context)
        {
            var clientid = context.Ticket.Properties.Dictionary["as:client_id"];

            if (string.IsNullOrEmpty(clientid))
            {
                return;
            }

            var refreshTokenId = Guid.NewGuid().ToString("n");

            using (var repository = new AuthRepository())
            {
                var lifetime = context.OwinContext.Get<string>("as:clientRefreshTokenLifeTime");
                var token = new RefreshToken()
                {
                    Id = AuthHelper.GetHash(refreshTokenId),
                    ClientId = clientid,
                    Subject = context.Ticket.Identity.Name,
                    IssuedUTC = DateTime.UtcNow,
                    ExpiresUTC = DateTime.UtcNow.AddMinutes(Convert.ToDouble(lifetime))
                };

                context.Ticket.Properties.IssuedUtc = token.IssuedUTC;
                context.Ticket.Properties.ExpiresUtc = token.ExpiresUTC;

                token.ProtectedTicket = context.SerializeTicket();

                var result = await repository.AddRefreshToken(token);

                if (result)
                {
                    context.SetToken(refreshTokenId);
                }
            }
        }

        public void Receive(AuthenticationTokenReceiveContext context)
        {
            ReceiveAsync(context).Wait();
        }

        public async Task ReceiveAsync(AuthenticationTokenReceiveContext context)
        {
            var hashedTokenId = AuthHelper.GetHash(context.Token);
            var allowedOrigin = context.OwinContext.Get<string>("as:clientAllowedOrigin");

            context.OwinContext.Response.Headers.Add("Access-Control-Allow-Origin", new[] { allowedOrigin });

            using (var repository = new AuthRepository())
            {
                var refreshToken = await repository.FindRefreshToken(hashedTokenId);

                if (refreshToken != null)
                {
                    context.DeserializeTicket(refreshToken.ProtectedTicket);

                    var result = await repository.RemoveRefreshToken(hashedTokenId);
                }
            }
        }
    }
}
