using System;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace UniquePassword.Server.Models.Entities
{
    [Table(nameof(RefreshToken))]
    public class RefreshToken
    {
        [Key]
        public string Id { get; set; }
        [Required]
        [MaxLength(50)]
        public string Subject { get; set; }
        [Required]
        public string ClientId { get; set; }
        [Required]
        public DateTime IssuedUTC { get; set; }
        [Required]
        public DateTime ExpiresUTC { get; set; }
        [Required]
        public string ProtectedTicket { get; set; }
    }
}
