-- Seed de 10 000 contrats pour les tests de performance.
-- 200 utilisateurs perfseed-<n>@perf.iard.test (mot de passe : PerfTest123!)
-- portant chacun 50 contrats ACTIF avec leur devis TRANSFORME.

\set ON_ERROR_STOP on

BEGIN;

-- ------------------------------------------------------------------
-- 1) 200 utilisateurs porteurs
-- ------------------------------------------------------------------
INSERT INTO users (civilite, created_at, email, nom, password_hash, prenom, role)
SELECT
    'MONSIEUR',
    now(),
    'perfseed-' || g || '@perf.iard.test',
    'Seed-' || g,
    '$2a$10$anVZKeYHs.JjpNKJjyUR1.U3HMzziUSrnaIbEbQhp32M5fC01m/CO',
    'Perf',
    'PARTICULIER'
FROM generate_series(1, 200) g
ON CONFLICT (email) DO NOTHING;

-- ------------------------------------------------------------------
-- 2) 10 000 devis TRANSFORME (50 par utilisateur) puis leurs contrats
-- ------------------------------------------------------------------
WITH params AS (
    SELECT
        n,
        (n - 1) / 50 + 1                                   AS user_num,
        (ARRAY['ESSENTIELLE','CONFORT','PREMIUM'])[n % 3 + 1] AS formule,
        (200 + n % 300)::numeric(10,2)                     AS prime_ht,
        40 + n % 120                                       AS surface
    FROM generate_series(1, 10000) n
),
devis_ins AS (
    INSERT INTO devis (created_at, updated_at, etape_courante, produit, statut, user_id, donnees_risque, resultat_tarif)
    SELECT
        now(), now(), 6, 'HABITATION', 'TRANSFORME',
        u.id,
        jsonb_build_object(
            'typeBien',              CASE WHEN p.n % 2 = 0 THEN 'APPARTEMENT' ELSE 'MAISON' END,
            'typeResidence',         'PRINCIPALE',
            'adresse',               p.n || ' avenue de la Performance',
            'codePostal',            lpad((75000 + p.n % 20)::text, 5, '0'),
            'ville',                 'Paris',
            'surfaceHabitable',      p.surface,
            'nombrePieces',          2 + p.n % 4,
            'etage',                 p.n % 6,
            'anneeConstruction',     1970 + p.n % 55,
            'statutOccupation',      'LOCATAIRE',
            'alarme',                (p.n % 2 = 0),
            'porteBlindee',          (p.n % 3 = 0),
            'dependances',           false,
            'piscine',               false,
            'capitalMobilier',       10000 + (p.n % 5) * 5000,
            'objetsValeur',          false,
            'nombreSinistres36Mois', 0,
            'formule',               p.formule,
            'optionsGaranties',      jsonb_build_array()
        ),
        jsonb_build_object(
            'formule',        p.formule,
            'primeHT',        p.prime_ht,
            'taxes',          round(p.prime_ht * 0.30, 2),
            'primeTTC',       round(p.prime_ht * 1.30, 2),
            'primeMensuelle', round(p.prime_ht * 1.30 / 12, 2),
            'garantiesIncluses', jsonb_build_array(
                jsonb_build_object('code','INCENDIE','libelle','Incendie et explosion',
                    'plafond',500000,'franchise',150,'incluse',true,'primeSupplementaire',0),
                jsonb_build_object('code','DEGATS_EAUX','libelle','Dégâts des eaux',
                    'plafond',300000,'franchise',200,'incluse',true,'primeSupplementaire',0),
                jsonb_build_object('code','RC_VIE_PRIVEE','libelle','Responsabilité civile vie privée',
                    'plafond',3000000,'franchise',0,'incluse',true,'primeSupplementaire',0)
            ),
            'garantiesOptionnelles', jsonb_build_array()
        )
    FROM params p
    JOIN users u ON u.email = 'perfseed-' || p.user_num || '@perf.iard.test'
    RETURNING id, user_id, donnees_risque, resultat_tarif
)
INSERT INTO contrats (created_at, numero_contrat, devis_id, user_id, produit, formule,
                      garanties, prime_ht, taxes, prime_ttc, periodicite, statut,
                      date_signature, signature_id, montant_mensuel_ttc, prochaine_date_prelevement)
SELECT
    now(),
    'CTR-PERF-' || lpad(d.id::text, 8, '0'),
    d.id,
    d.user_id,
    'HABITATION',
    d.resultat_tarif->>'formule',
    d.resultat_tarif,
    (d.resultat_tarif->>'primeHT')::numeric,
    (d.resultat_tarif->>'taxes')::numeric,
    (d.resultat_tarif->>'primeTTC')::numeric,
    'ANNUELLE',
    'ACTIF',
    now(),
    'SIG-PERF-' || lpad(d.id::text, 8, '0'),
    (d.resultat_tarif->>'primeMensuelle')::numeric,
    CURRENT_DATE + INTERVAL '1 month'
FROM devis_ins d;

COMMIT;

-- Vérifications
SELECT count(*) AS contrats_perf FROM contrats WHERE numero_contrat LIKE 'CTR-PERF-%';
SELECT count(*) AS total_contrats FROM contrats;
SELECT count(*) AS users_perfseed FROM users WHERE email LIKE 'perfseed-%';
