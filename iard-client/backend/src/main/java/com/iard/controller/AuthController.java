package com.iard.controller;

import com.iard.dto.AuthResponse;
import com.iard.dto.LoginRequest;
import com.iard.dto.RegisterRequest;
import com.iard.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentification", description = "Création de compte et connexion. Ces endpoints sont publics ; "
        + "le token JWT retourné doit être renseigné via le bouton Authorize pour appeler les autres API.")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Créer un compte client",
            description = "Inscrit un nouveau souscripteur particulier. Le mot de passe doit contenir au moins "
                    + "8 caractères avec une majuscule, une minuscule, un chiffre et un caractère spécial (@$!%*?&). "
                    + "Retourne directement un token JWT : il n'est pas nécessaire de se connecter ensuite.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Compte créé, token JWT retourné"),
            @ApiResponse(responseCode = "400", description = "Données invalides (email déjà utilisé, mot de passe trop faible, CGU non acceptées…)")
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @Operation(summary = "Se connecter",
            description = "Authentifie un client avec son email et son mot de passe et retourne un token JWT "
                    + "valable 24 heures, à passer en header `Authorization: Bearer <token>`.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Connexion réussie, token JWT retourné"),
            @ApiResponse(responseCode = "401", description = "Identifiants incorrects")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
