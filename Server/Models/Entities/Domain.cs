using System;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace Server.Models.Entities
{
    [Table(nameof(Domain))]
    public class Domain
    {
        [Key]
        [Column(Order = 0)]
        [Required]
        public string UserID { get; set; }

        [Key]
        [Column(Order = 1)]
        [Required]
        public string Website { get; set; }

        public int? MaximumLength { get; set; }

        public string SpecialCharacters { get; set; }

        [Required]
        public DateTime LastModifiedOn { get; set; }
    }
}
