using System.Data.Entity.Migrations;
using UniquePassword.Server.Helpers;
using UniquePassword.Server.Models.Entities;

namespace UniquePassword.Server.Models.Contexts
{
    public class AuthConfiguration : DbMigrationsConfiguration<AuthContext>
    {
        private const string SecretBrowser = "a";
        private const string SecretAndroid = "b";

        public AuthConfiguration()
        {
            AutomaticMigrationsEnabled = true;
            AutomaticMigrationDataLossAllowed = true;
        }

        protected override void Seed(AuthContext context)
        {
            base.Seed(context);

            var clients = new Client[]
            {
                new Client()
                {
                    Id = "browser",
                    Name = "Browser Application",
                    Active = true,
                    Secret = AuthHelper.GetHash(SecretBrowser),
                    AllowedOrigin = "http://localhost:56184/",
                    ApplicationType = ApplicationType.JavaScript,
                    RefreshTokenLifeTime = 7200,
                },
                new Client()
                {
                    Id = "android",
                    Name = "Android Application",
                    Active = true,
                    Secret = AuthHelper.GetHash(SecretAndroid),
                    AllowedOrigin = "*",
                    ApplicationType = ApplicationType.Native,
                    RefreshTokenLifeTime = 14400,
                },
            };

            foreach (var client in clients)
            {
                context.Clients.AddOrUpdate(x => x.Id, client);
            }

            context.SaveChanges();
        }
    }
}
