package com.iard.perf;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.time.Duration;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

/**
 * Simulation de charge sur les API de l'espace client IARD.
 *
 * Parcours de chaque utilisateur virtuel :
 *   inscription → connexion (JWT) → création + complétion + tarification
 *   d'un devis → consultation répétée des devis/contrats.
 *
 * Paramètres (propriétés système) :
 *   -DbaseUrl  URL de la plateforme  (défaut : http://187.77.175.250)
 *   -Dusers    nombre d'utilisateurs simulés (défaut : 20)
 *   -DrampSec  durée de montée en charge en secondes (défaut : 20)
 */
public class IardClientSimulation extends Simulation {

    private static final String BASE_URL = System.getProperty("baseUrl", "http://187.77.175.250");
    private static final int USERS = Integer.getInteger("users", 20);
    private static final int RAMP_SEC = Integer.getInteger("rampSec", 20);

    // Conforme à la policy du backend : majuscule, minuscule, chiffre, spécial
    private static final String PASSWORD = "PerfTest123!";

    private final HttpProtocolBuilder httpProtocol = http
            .baseUrl(BASE_URL)
            .acceptHeader("application/json")
            .contentTypeHeader("application/json")
            .userAgentHeader("Gatling-perf-tests");

    // Un email unique par utilisateur virtuel et par run
    private final Iterator<Map<String, Object>> emailFeeder =
            Stream.<Map<String, Object>>generate(() ->
                    Map.of("email", "perf-" + UUID.randomUUID().toString().substring(0, 12) + "@perf.iard.test"))
                    .iterator();

    private final ScenarioBuilder parcoursClient = scenario("Parcours client IARD")
            .feed(emailFeeder)

            // --- Authentification -------------------------------------------
            .exec(http("POST /auth/register")
                    .post("/api/auth/register")
                    .body(StringBody("""
                            {
                              "civilite": "MONSIEUR",
                              "prenom": "Perf",
                              "nom": "Gatling",
                              "email": "#{email}",
                              "password": "%s",
                              "confirmPassword": "%s",
                              "acceptCgu": true
                            }""".formatted(PASSWORD, PASSWORD)))
                    .check(status().is(200)))
            .pause(1)

            .exec(http("POST /auth/login")
                    .post("/api/auth/login")
                    .body(StringBody("""
                            {"email": "#{email}", "password": "%s"}""".formatted(PASSWORD)))
                    .check(status().is(200))
                    .check(jsonPath("$.token").saveAs("token")))
            .pause(1)

            // --- Parcours devis (authentifié) --------------------------------
            .exec(http("POST /devis (création)")
                    .post("/api/devis")
                    .header("Authorization", "Bearer #{token}")
                    .check(status().is(200))
                    .check(jsonPath("$.id").saveAs("devisId")))
            .pause(1)

            .exec(http("PUT /devis/{id} (complétion)")
                    .put("/api/devis/#{devisId}")
                    .header("Authorization", "Bearer #{token}")
                    .body(StringBody("""
                            {
                              "etapeCourante": 6,
                              "typeBien": "APPARTEMENT",
                              "typeResidence": "PRINCIPALE",
                              "adresse": "12 rue de la Performance",
                              "codePostal": "75011",
                              "ville": "Paris",
                              "surfaceHabitable": 65,
                              "nombrePieces": 3,
                              "etage": 2,
                              "anneeConstruction": 2005,
                              "statutOccupation": "LOCATAIRE",
                              "alarme": true,
                              "porteBlindee": false,
                              "dependances": false,
                              "piscine": false,
                              "capitalMobilier": 20000,
                              "objetsValeur": false,
                              "nombreSinistres36Mois": 0,
                              "formule": "CONFORT",
                              "optionsGaranties": ["BRIS_GLACE"],
                              "souscripteurEstAssure": true
                            }"""))
                    .check(status().is(200)))
            .pause(1)

            .exec(http("POST /devis/{id}/tarifier")
                    .post("/api/devis/#{devisId}/tarifier")
                    .header("Authorization", "Bearer #{token}")
                    .check(status().is(200))
                    .check(jsonPath("$.resultatTarif.primeTTC").exists()))
            .pause(1)

            // --- Consultations répétées --------------------------------------
            .repeat(5).on(
                    exec(http("GET /devis")
                            .get("/api/devis")
                            .header("Authorization", "Bearer #{token}")
                            .check(status().is(200)))
                    .pause(Duration.ofMillis(500))
                    .exec(http("GET /devis/stats")
                            .get("/api/devis/stats")
                            .header("Authorization", "Bearer #{token}")
                            .check(status().is(200)))
                    .pause(Duration.ofMillis(500))
                    .exec(http("GET /contrats")
                            .get("/api/contrats")
                            .header("Authorization", "Bearer #{token}")
                            .check(status().is(200)))
                    .pause(Duration.ofMillis(500))
                    .exec(http("GET /contrats/stats")
                            .get("/api/contrats/stats")
                            .header("Authorization", "Bearer #{token}")
                            .check(status().is(200)))
                    .pause(Duration.ofMillis(500))
            );

    {
        setUp(
                parcoursClient.injectOpen(rampUsers(USERS).during(Duration.ofSeconds(RAMP_SEC)))
        ).protocols(httpProtocol)
         .assertions(
                global().successfulRequests().percent().gt(95.0),
                global().responseTime().percentile3().lt(3000),   // p95 < 3s
                global().responseTime().max().lt(10000)
         );
    }
}
