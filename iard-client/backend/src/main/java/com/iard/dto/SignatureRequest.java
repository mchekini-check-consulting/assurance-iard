package com.iard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Code OTP de signature électronique")
public class SignatureRequest {

    @NotBlank(message = "Le code de signature est obligatoire")
    @Schema(description = "Code à 4 chiffres reçu par SMS (mock : toujours 6208)", example = "6208")
    private String code;
}
