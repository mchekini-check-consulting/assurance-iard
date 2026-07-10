package com.iard.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SignatureRequest {

    @NotBlank(message = "Le code de signature est obligatoire")
    private String code;
}
