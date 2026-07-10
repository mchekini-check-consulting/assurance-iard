package com.iard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonneAssuree implements Serializable {

    private Civilite civilite;
    private String prenom;
    private String nom;
    private String email;
    private String telephone;
    private String adresse;
    private String codePostal;
    private String ville;
}
