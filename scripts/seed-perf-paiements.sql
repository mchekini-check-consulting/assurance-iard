\set ON_ERROR_STOP on
BEGIN;

-- 3 mois de prélèvements sur les contrats de perf, selon la règle du mock :
-- montant mensuel > 30 € => FAILED, sinon SUCCES
INSERT INTO paiements (created_at, date_prelevement, montant, periode, statut, contrat_id)
SELECT
    now(),
    make_date(2026, mois, 5),
    c.montant_mensuel_ttc,
    '2026-' || lpad(mois::text, 2, '0'),
    CASE WHEN c.montant_mensuel_ttc > 30 THEN 'FAILED' ELSE 'SUCCES' END,
    c.id
FROM contrats c
CROSS JOIN generate_series(4, 6) AS mois
WHERE c.numero_contrat LIKE 'CTR-PERF-%'
ON CONFLICT (contrat_id, periode) DO NOTHING;

-- Une facture par prélèvement réussi
INSERT INTO factures (created_at, date_emission, montant_ht, montant_ttc, taxes, numero_facture, periode, contrat_id, paiement_id, user_id)
SELECT
    now(),
    p.date_prelevement,
    round(p.montant / 1.30, 2),
    p.montant,
    p.montant - round(p.montant / 1.30, 2),
    'FAC-PERF-' || lpad(p.id::text, 10, '0'),
    p.periode,
    p.contrat_id,
    p.id,
    c.user_id
FROM paiements p
JOIN contrats c ON c.id = p.contrat_id
WHERE p.statut = 'SUCCES'
  AND c.numero_contrat LIKE 'CTR-PERF-%'
  AND NOT EXISTS (SELECT 1 FROM factures f WHERE f.paiement_id = p.id);

COMMIT;

SELECT statut, count(*) FROM paiements GROUP BY statut;
SELECT count(*) AS factures FROM factures;
