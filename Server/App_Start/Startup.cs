using Microsoft.Owin;
using Microsoft.Owin.Cors;
using Microsoft.Owin.Security.OAuth;
using Owin;
using System;
using System.Web.Http;
using UniquePassword.Server.App_Start.Providers;
using UniquePassword.Server.Providers;

[assembly: OwinStartup(typeof(UniquePassword.Server.Startup))]
namespace UniquePassword.Server
{
    public class Startup
    {
        public void Configuration(IAppBuilder app)
        {
            ConfigureOAuth(app);

            var config = new HttpConfiguration();

            WebApiConfig.Register(config);

            app.UseCors(CorsOptions.AllowAll);
            app.UseWebApi(config);
        }

        public void ConfigureOAuth(IAppBuilder app)
        {
            var serverOptions = new OAuthAuthorizationServerOptions()
            {
                Provider = new SimpleAuthorizationServerProvider(),
                AllowInsecureHttp = true,
                TokenEndpointPath = new PathString("/token"),
                RefreshTokenProvider = new SimpleRefreshTokenProvider(),
                AccessTokenExpireTimeSpan = TimeSpan.FromMinutes(30),
            };

            app.UseOAuthAuthorizationServer(serverOptions);
            app.UseOAuthBearerAuthentication(new OAuthBearerAuthenticationOptions());
        }
    }
}