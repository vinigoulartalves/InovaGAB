using System.ComponentModel.DataAnnotations;

namespace InovaGAB.Application.Auth.Dtos;

public sealed class RefreshRequestDto
{
    [Required]
    public string RefreshToken { get; set; } = string.Empty;
}
