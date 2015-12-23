using Server.Controllers;
using Server.Models.Entities;
using System;
using System.Data.Entity.Migrations;

namespace Server.Models.Contexts
{
    public class DatabaseConfiguration : DbMigrationsConfiguration<DatabaseContext>
    {
        public DatabaseConfiguration()
        {
            AutomaticMigrationsEnabled = true;
            AutomaticMigrationDataLossAllowed = true;
        }

        protected override void Seed(DatabaseContext context)
        {
            base.Seed(context);

            var domains = new Domain[]
            {
                new Domain()
                {
                    UserID = DomainsController.AnonymousUserID,
                    Website = "www.twitter.com",
                    LastModifiedOn = DateTime.UtcNow,
                },
                new Domain()
                {
                    UserID = DomainsController.AnonymousUserID,
                    Website = "www.facebook.com",
                    MaximumLength = 30,
                    LastModifiedOn = DateTime.UtcNow,
                },
                new Domain()
                {
                    UserID = DomainsController.AnonymousUserID,
                    Website = "www.linkedin.com",
                    SpecialCharacters = "ABCDEF$%^&*()",
                    LastModifiedOn = DateTime.UtcNow,
                },
            };

            foreach (var domain in domains)
            {
                context.Domains.AddOrUpdate(x => new { x.UserID, x.Website }, domain);
            }
        }
    }
}