using Server.Models.Entities;
using System.Data.Entity;

namespace Server.Models.Contexts
{
    public class DatabaseContext : DbContext
    {
        public const string ConnectionName = "DatabaseContext";

        public DbSet<Domain> Domains
        {
            get { return Set<Domain>(); }
        }

        public DatabaseContext()
            : base(ConnectionName)
        {
            var initialiser = new MigrateDatabaseToLatestVersion<DatabaseContext, DatabaseConfiguration>();

            Database.SetInitializer(initialiser);
        }
    }
}
