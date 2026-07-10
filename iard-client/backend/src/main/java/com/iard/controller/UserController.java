package com.iard.controller;

import com.iard.dto.UserResponse;
import com.iard.entity.User;
import com.iard.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@Tag(name = "Utilisateur", description = "Informations du compte connecté.")
@RequiredArgsConstructor
public class UserController {

    @Operation(summary = "Mon profil",
            description = "Retourne les informations du compte associé au token JWT (identité, email, rôle).")
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userDetails.getUser();

        UserResponse response = UserResponse.builder()
                .id(user.getId())
                .civilite(user.getCivilite())
                .prenom(user.getPrenom())
                .nom(user.getNom())
                .email(user.getEmail())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();

        return ResponseEntity.ok(response);
    }
}
