using System.ComponentModel.DataAnnotations;

namespace UniquePassword.Server.Models.ViewModels
{
    public class UserViewModel
    {
        [Required]
        [EmailAddress]
        [DataType(DataType.EmailAddress)]
        [Display(Name = "Email")]
        public string Username { get; set; }

        [Required]
        [StringLength(int.MaxValue, ErrorMessage = "The {0} must be at least {2} characters long", MinimumLength = 6)]
        [DataType(DataType.Password)]
        [Display(Name = "Password")]
        public string Password { get; set; }

        [DataType(DataType.Password)]
        [Display(Name = "Confirm password")]
        [Compare("Password", ErrorMessage = "The password and confirmation password must be the same")]
        public string ConfirmPassword { get; set; }
    }
}
