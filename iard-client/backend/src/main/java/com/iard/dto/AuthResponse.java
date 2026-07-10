package com.iard.dto;

import com.iard.entity.Civilite;
import com.iard.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String type = "Bearer";
    private Long id;
    private Civilite civilite;
    private String prenom;
    private String nom;
    private String email;
    private Role role;
}
