# Exercice SQL — Base `iard_db`

**Connexion** : `jdbc:postgresql://187.77.175.250:5432/iard_db` — utilisateur `iard_user`, mot de passe `iard_password`.

**Règle du jeu** : uniquement des requêtes de **lecture** (`SELECT`) — aucune écriture.

**Tables principales** : `users`, `devis`, `contrats`, `paiements`, `factures`, `sinistres`.
Particularités : `devis.donnees_risque` et `contrats.garanties` sont des colonnes **JSONB** ;
un contrat référence son devis (`devis_id`) et son souscripteur (`user_id`).

---

## Niveau facile (8 requêtes) — maîtrise du SELECT et des conditions

**1.** Affichez le numéro, la formule et la prime TTC de tous les contrats.

**2.** Affichez les contrats dont le statut est `ACTIF` (numéro et prime TTC).

**3.** Affichez les contrats de formule `PREMIUM` dont la prime TTC est comprise **entre 400 et 600 €** (`BETWEEN`).

**4.** Affichez les contrats dont la formule est `CONFORT` **ou** `PREMIUM`, en une seule condition (`IN`).

**5.** Affichez l'email et le nom des utilisateurs dont le nom **commence par** `Seed` (`LIKE`).

**6.** Affichez les contrats **non résiliés** dont la prime TTC dépasse 600 €, **ou** ceux de formule `ESSENTIELLE` dont la prime TTC est inférieure à 300 € — en une seule requête (attention aux parenthèses entre `AND` et `OR`).

**7.** Affichez les devis qui n'ont **pas encore de tarif calculé** (`resultat_tarif` vide — `IS NULL`).

**8.** Affichez les 5 contrats les plus chers : numéro, formule et prime TTC (`ORDER BY` + `LIMIT`).

---

## Niveau intermédiaire (8 requêtes)

**9.** Comptez le nombre de contrats pour chaque statut (`GROUP BY`).

**10.** Top 10 des utilisateurs qui possèdent le plus de contrats : email et nombre de contrats (`JOIN` + `GROUP BY`).

**11.** Listez les utilisateurs qui n'ont **aucun** contrat (deux écritures possibles : `LEFT JOIN` ou `NOT EXISTS`).

**12.** Pour chaque formule : le nombre de contrats, la prime TTC totale, la prime minimale et la prime maximale.

**13.** Par période (`paiements.periode`), affichez le nombre de prélèvements réussis, le nombre d'échecs et le montant total encaissé (statut `SUCCES` uniquement).

**14.** En joignant les contrats à leur devis, comptez le nombre de contrats par **type de bien** (`APPARTEMENT` / `MAISON`) — le type est dans le JSONB `devis.donnees_risque`.

**15.** Quels utilisateurs détiennent des contrats d'au moins 2 **formules différentes** ? Affichez l'email et le nombre de formules distinctes (`HAVING`).

**16.** Listez les prélèvements en échec (`FAILED`) avec l'email du client, le numéro de contrat et le montant (jointure sur 3 tables).

---

## Niveau difficile (4 requêtes)

**17.** Pour **chaque formule**, le top 3 des contrats les plus chers (numéro, formule, prime TTC, rang).
*Indice : fonction fenêtre `ROW_NUMBER() OVER (PARTITION BY ...)*.

**18.** Listez les **libellés distincts** des garanties incluses dans les contrats — elles sont dans le tableau JSONB `garanties->'garantiesIncluses'`.
*Indice : `jsonb_array_elements`.*

**19.** Affichez l'évolution **cumulée** du nombre de contrats créés jour par jour : date, contrats créés ce jour, total cumulé.
*Indice : `SUM(...) OVER (ORDER BY ...)`.*

**20.** Pour chaque contrat, calculez l'écart entre sa prime TTC et la prime moyenne de **sa** formule, et affichez les 10 contrats les plus « au-dessus de la moyenne » (numéro, formule, prime, écart).
*Indice : `AVG(...) OVER (PARTITION BY ...)` — pas de `GROUP BY`.*

---
---

# Corrigé

<details>
<summary>⚠️ Déplier uniquement après avoir cherché !</summary>

```sql
-- 1
SELECT numero_contrat, formule, prime_ttc
FROM contrats;

-- 2
SELECT numero_contrat, prime_ttc
FROM contrats
WHERE statut = 'ACTIF';

-- 3
SELECT numero_contrat, prime_ttc
FROM contrats
WHERE formule = 'PREMIUM'
  AND prime_ttc BETWEEN 400 AND 600;

-- 4
SELECT numero_contrat, formule, prime_ttc
FROM contrats
WHERE formule IN ('CONFORT', 'PREMIUM');

-- 5
SELECT email, nom
FROM users
WHERE nom LIKE 'Seed%';

-- 6
SELECT numero_contrat, formule, statut, prime_ttc
FROM contrats
WHERE (statut <> 'RESILIE' AND prime_ttc > 600)
   OR (formule = 'ESSENTIELLE' AND prime_ttc < 300);

-- 7
SELECT id, statut, created_at
FROM devis
WHERE resultat_tarif IS NULL;

-- 8
SELECT numero_contrat, formule, prime_ttc
FROM contrats
ORDER BY prime_ttc DESC
LIMIT 5;

-- 9
SELECT statut, count(*)
FROM contrats
GROUP BY statut;

-- 10
SELECT u.email, count(*) AS nb_contrats
FROM contrats c
JOIN users u ON u.id = c.user_id
GROUP BY u.email
ORDER BY nb_contrats DESC
LIMIT 10;

-- 11 (variante LEFT JOIN)
SELECT u.email
FROM users u
LEFT JOIN contrats c ON c.user_id = u.id
WHERE c.id IS NULL;

-- 11 (variante NOT EXISTS)
SELECT u.email
FROM users u
WHERE NOT EXISTS (SELECT 1 FROM contrats c WHERE c.user_id = u.id);

-- 12
SELECT formule,
       count(*)       AS nb_contrats,
       sum(prime_ttc) AS prime_totale,
       min(prime_ttc) AS prime_min,
       max(prime_ttc) AS prime_max
FROM contrats
GROUP BY formule;

-- 13
SELECT periode,
       count(*) FILTER (WHERE statut = 'SUCCES') AS nb_succes,
       count(*) FILTER (WHERE statut = 'FAILED') AS nb_echecs,
       coalesce(sum(montant) FILTER (WHERE statut = 'SUCCES'), 0) AS total_encaisse
FROM paiements
GROUP BY periode
ORDER BY periode;

-- 14
SELECT d.donnees_risque->>'typeBien' AS type_bien, count(*)
FROM contrats c
JOIN devis d ON d.id = c.devis_id
GROUP BY type_bien;

-- 15
SELECT u.email, count(DISTINCT c.formule) AS nb_formules
FROM contrats c
JOIN users u ON u.id = c.user_id
GROUP BY u.email
HAVING count(DISTINCT c.formule) >= 2;

-- 16
SELECT u.email, c.numero_contrat, p.montant
FROM paiements p
JOIN contrats c ON c.id = p.contrat_id
JOIN users u    ON u.id = c.user_id
WHERE p.statut = 'FAILED';

-- 17
SELECT numero_contrat, formule, prime_ttc, rang
FROM (
    SELECT numero_contrat, formule, prime_ttc,
           ROW_NUMBER() OVER (PARTITION BY formule ORDER BY prime_ttc DESC) AS rang
    FROM contrats
) t
WHERE rang <= 3
ORDER BY formule, rang;

-- 18
SELECT DISTINCT g->>'libelle' AS garantie
FROM contrats,
     jsonb_array_elements(garanties->'garantiesIncluses') g
ORDER BY garantie;

-- 19
SELECT jour,
       nb_crees,
       sum(nb_crees) OVER (ORDER BY jour) AS cumul
FROM (
    SELECT created_at::date AS jour, count(*) AS nb_crees
    FROM contrats
    GROUP BY created_at::date
) t
ORDER BY jour;

-- 20
SELECT numero_contrat, formule, prime_ttc,
       prime_ttc - moyenne_formule AS ecart
FROM (
    SELECT numero_contrat, formule, prime_ttc,
           AVG(prime_ttc) OVER (PARTITION BY formule) AS moyenne_formule
    FROM contrats
) t
ORDER BY ecart DESC
LIMIT 10;
```

</details>
