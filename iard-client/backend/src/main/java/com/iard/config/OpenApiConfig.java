package com.iard.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration de la documentation OpenAPI / Swagger UI.
 * Le schéma "bearerAuth" permet de renseigner le token JWT via le bouton
 * "Authorize" et de tester les endpoints protégés directement depuis l'UI.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI iardOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("IARD Client API")
                        .description("""
                                API de l'espace client de la plateforme d'assurance IARD :
                                authentification, tarification, devis, contrats (signature),
                                vérification KYC, factures et déclaration de sinistres.

                                Obtenir un token via POST /api/auth/login puis le renseigner
                                avec le bouton Authorize.""")
                        .version("1.0"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
