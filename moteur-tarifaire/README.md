# Moteur Tarifaire - Mock Assurance Habitation

Service de tarification mocké pour l'assurance habitation. Ce mock imite le comportement du vrai moteur tarifaire et peut être facilement remplacé par celui-ci une fois disponible.

## Démarrage

```bash
cd moteur-tarifaire
mvn spring-boot:run
```

Le service démarre sur le port **8081**.

## Endpoint

### POST /api/pricing/habitation

Calcule le tarif d'une assurance habitation.

## Contrat d'interface

### Requête (JSON)

| Champ | Type | Requis | Description |
|-------|------|--------|-------------|
| `typeHabitation` | enum | Oui | `APPARTEMENT` ou `MAISON` |
| `surfaceHabitable` | integer | Oui | Surface en m² (min: 1) |
| `nombrePieces` | integer | Oui | Nombre de pièces (min: 1) |
| `statutOccupation` | enum | Oui | `PROPRIETAIRE_OCCUPANT`, `LOCATAIRE`, `PROPRIETAIRE_NON_OCCUPANT` |
| `codePostal` | string | Oui | Code postal (5 chiffres) |
| `nombreSinistres36Mois` | integer | Oui | Nombre de sinistres sur 36 mois (min: 0) |
| `alarme` | boolean | Non | Présence d'une alarme |
| `porteBlindee` | boolean | Non | Présence d'une porte blindée |
| `capitalMobilier` | decimal | Oui | Capital mobilier à assurer (min: 0) |
| `formule` | enum | Oui | `ESSENTIELLE`, `CONFORT`, `PREMIUM` |
| `garantiesOptionnelles` | array | Non | Liste: `PISCINE`, `DEPENDANCES`, `OBJETS_VALEUR`, `EQUIPEMENT_JARDIN` |

### Réponse (JSON)

| Champ | Type | Description |
|-------|------|-------------|
| `formule` | string | Formule retenue |
| `periodicite` | string | Périodicité (annuelle) |
| `primeHT` | decimal | Prime hors taxes |
| `taxes` | decimal | Montant des taxes (TSCA 30%) |
| `primeTTC` | decimal | Prime TTC (incluant contribution fonds garantie) |
| `primeMensuelleTTC` | decimal | Prime mensuelle indicative |
| `garanties` | array | Détail des garanties |

## Exemple de requête

```bash
curl -X POST http://localhost:8081/api/pricing/habitation \
  -H "Content-Type: application/json" \
  -d '{
    "typeHabitation": "APPARTEMENT",
    "surfaceHabitable": 75,
    "nombrePieces": 4,
    "statutOccupation": "PROPRIETAIRE_OCCUPANT",
    "codePostal": "75001",
    "nombreSinistres36Mois": 0,
    "alarme": true,
    "porteBlindee": true,
    "capitalMobilier": 15000,
    "formule": "CONFORT",
    "garantiesOptionnelles": ["PISCINE"]
  }'
```

## Exemple de réponse

```json
{
  "formule": "Confort",
  "periodicite": "annuelle",
  "primeHT": 245.50,
  "taxes": 73.65,
  "primeTTC": 325.65,
  "primeMensuelleTTC": 27.14,
  "garanties": [
    { "code": "INCENDIE", "libelle": "Incendie & explosions", "incluse": true, "montantHT": 55.00 },
    { "code": "DDE", "libelle": "Dégâts des eaux", "incluse": true, "montantHT": 48.00 },
    { "code": "RC", "libelle": "Responsabilité civile", "incluse": true, "montantHT": 30.00 },
    { "code": "CATNAT", "libelle": "Catastrophes naturelles", "incluse": true, "montantHT": 22.50 },
    { "code": "VOL", "libelle": "Vol & cambriolage", "incluse": true, "montantHT": 60.00 },
    { "code": "BDG", "libelle": "Bris de glace", "incluse": true, "montantHT": 30.00 },
    { "code": "PISCINE", "libelle": "Piscine", "incluse": false, "montantHT": 30.00 }
  ]
}
```

## Règles de tarification

### Prime de base
- Appartement: 1,20 € / m² / an
- Maison: 1,60 € / m² / an

### Coefficients multiplicateurs
| Critère | Valeurs |
|---------|---------|
| Statut d'occupation | Propriétaire occupant ×1,0 · Locataire ×0,9 · Propriétaire non occupant ×1,15 |
| Zone géographique | Faible ×0,9 · Moyenne ×1,0 · Élevée ×1,25 |
| Nombre de pièces | +5% par pièce au-delà de 3 |
| Sinistralité (36 mois) | 0 sin. ×1,0 · 1 sin. ×1,15 · 2+ sin. ×1,35 |
| Sécurité | -10% si alarme ET porte blindée |

### Capital mobilier
+0,4% du capital mobilier assuré par an

### Formules
| Formule | Garanties incluses | Forfait |
|---------|-------------------|---------|
| Essentielle | Incendie, DDE, RC, CatNat | +0 € |
| Confort | Essentielle + Vol + Bris de glace | +45 € |
| Premium | Confort + Dommages élec. + Protection juridique + Assistance | +90 € |

### Garanties optionnelles
- Piscine: +30 €/an
- Dépendances: +25 €/an
- Objets de valeur: +40 €/an
- Équipement jardin: +20 €/an

### Taxes
- TSCA: 30% sur prime HT
- Contribution fonds de garantie: +6,50 € fixe

## Erreurs

En cas de champ requis manquant ou invalide, le service retourne une erreur 400:

```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Validation échouée",
  "messages": [
    "typeHabitation: Le type d'habitation est requis",
    "surfaceHabitable: La surface doit être supérieure à 0"
  ],
  "path": "/api/pricing/habitation"
}
```
