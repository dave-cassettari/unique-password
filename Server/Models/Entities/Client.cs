using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace UniquePassword.Server.Models.Entities
{
    [Table(nameof(Client))]
    public class Client
    {
        [Key]
        public string Id { get; set; }
        [Required]
        [MaxLength(100)]
        public string Name { get; set; }
        [Required]
        public bool Active { get; set; }
        [Required]
        public string Secret { get; set; }
        [Required]
        [MaxLength(100)]
        public string AllowedOrigin { get; set; }
        [Required]
        public int RefreshTokenLifeTime { get; set; }
        [Required]
        public ApplicationType ApplicationType { get; set; }
    }
}
