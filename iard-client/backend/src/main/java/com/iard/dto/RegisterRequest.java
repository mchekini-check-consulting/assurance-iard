package com.iard.dto;

import com.iard.entity.Civilite;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Données d'inscription d'un nouveau client particulier")
public class RegisterRequest {

    @Schema(description = "Civilité du souscripteur", example = "MONSIEUR")
    @NotNull(message = "La civilité est obligatoire")
    private Civilite civilite;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(min = 2, max = 50, message = "Le prénom doit contenir entre 2 et 50 caractères")
    @Schema(description = "Prénom (2 à 50 caractères)", example = "Jean")
    private String prenom;

    @Schema(description = "Nom de famille (2 à 50 caractères)", example = "Dupont")
    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 50, message = "Le nom doit contenir entre 2 et 50 caractères")
    private String nom;

    @Schema(description = "Adresse email, utilisée comme identifiant de connexion", example = "jean.dupont@example.com")
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    private String email;

    @Schema(description = "Mot de passe : 8 caractères minimum, avec majuscule, minuscule, chiffre et caractère spécial (@$!%*?&)", example = "MotDePasse123!")
    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
        message = "Le mot de passe doit contenir au moins une majuscule, une minuscule, un chiffre et un caractère spécial"
    )
    private String password;

    @Schema(description = "Doit être identique au mot de passe", example = "MotDePasse123!")
    @NotBlank(message = "La confirmation du mot de passe est obligatoire")
    private String confirmPassword;

    @Schema(description = "Acceptation des conditions générales d'utilisation (doit être true)", example = "true")
    private boolean acceptCgu;
}
