using System.ComponentModel.DataAnnotations;

namespace InovaGAB.Application.Auth.Dtos;

public sealed class LoginRequestDto
{
    [Required]
    [EmailAddress]
    public string Email { get; set; } = string.Empty;

    [Required]
    [MinLength(8)]
    public string Senha { get; set; } = string.Empty;
}
