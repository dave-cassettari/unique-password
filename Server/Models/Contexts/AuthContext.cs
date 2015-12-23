using Microsoft.AspNet.Identity.EntityFramework;
using System.Data.Entity;
using UniquePassword.Server.Models.Entities;

namespace UniquePassword.Server.Models.Contexts
{
    public class AuthContext : IdentityDbContext<IdentityUser>
    {
        public const string ConnectionName = "AuthContext";

        public DbSet<Client> Clients
        {
            get { return Set<Client>(); }
        }

        public DbSet<RefreshToken> RefreshTokens
        {
            get { return Set<RefreshToken>(); }
        }

        public AuthContext()
            : base(ConnectionName)
        {
            var initialiser = new MigrateDatabaseToLatestVersion<AuthContext, AuthConfiguration>();

            Database.SetInitializer(initialiser);
        }
    }
}
