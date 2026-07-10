package com.iard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Identifiants de connexion")
public class LoginRequest {

    @Schema(description = "Email du compte", example = "jean.dupont@example.com")
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    private String email;

    @Schema(description = "Mot de passe du compte", example = "MotDePasse123!")
    @NotBlank(message = "Le mot de passe est obligatoire")
    private String password;
}
