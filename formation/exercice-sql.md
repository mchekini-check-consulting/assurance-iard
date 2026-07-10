# Exercice SQL — Base `iard_db`

**Connexion** : `jdbc:postgresql://187.77.175.250:5432/iard_db` — utilisateur `iard_user`, mot de passe `iard_password`.

**Règle du jeu** : uniquement des requêtes de **lecture** (`SELECT`) — aucune écriture.

**Tables principales** : `users`, `devis`, `contrats`, `paiements`, `factures`, `sinistres`.
Particularités : `devis.donnees_risque` et `contrats.garanties` sont des colonnes **JSONB** ;
un contrat référence son devis (`devis_id`) et son souscripteur (`user_id`).

---

## Niveau facile (8 requêtes)

**1.** Combien y a-t-il de contrats en base ?

**2.** Affichez l'email, le nom et le prénom des 10 derniers utilisateurs inscrits.

**3.** Listez le numéro et la prime TTC des contrats de formule `PREMIUM` dont la prime TTC dépasse 500 €, du plus cher au moins cher.

**4.** Quels sont les différents statuts présents dans la table `contrats` ?

**5.** Combien de devis y a-t-il pour chaque statut ?

**6.** Combien de contrats ont été signés aujourd'hui ?

**7.** Quelle est la prime TTC minimale, maximale et moyenne des contrats `ACTIF` ? (arrondir la moyenne à 2 décimales)

**8.** Combien d'utilisateurs ont une adresse email de test (se terminant par `@perf.iard.test`) ?

---

## Niveau intermédiaire (8 requêtes)

**9.** Top 10 des utilisateurs qui possèdent le plus de contrats : email et nombre de contrats.

**10.** Pour chaque formule, le nombre de contrats, la prime TTC totale et la prime TTC moyenne.

**11.** Listez les utilisateurs qui n'ont **aucun** contrat (deux écritures possibles : `LEFT JOIN` ou `NOT EXISTS`).

**12.** Par période (`paiements.periode`), affichez le nombre de prélèvements réussis, le nombre d'échecs et le montant total encaissé (statut `SUCCES` uniquement).

**13.** En joignant les contrats à leur devis, comptez le nombre de contrats par **type de bien** (`APPARTEMENT` / `MAISON`) — le type est dans le JSONB `devis.donnees_risque`.

**14.** Quels utilisateurs détiennent des contrats d'au moins 2 **formules différentes** ? Affichez l'email et le nombre de formules distinctes.

**15.** Listez les prélèvements en échec (`FAILED`) avec l'email du client, le numéro de contrat et le montant (jointure sur 3 tables).

**16.** Quelle est la surface habitable moyenne des biens assurés, par formule de contrat ? (la surface est dans le JSONB du devis — attention à la conversion de type)

---

## Niveau difficile (4 requêtes)

**17.** Pour **chaque formule**, le top 3 des contrats les plus chers (numéro, formule, prime TTC, rang).
*Indice : fonction fenêtre `ROW_NUMBER() OVER (PARTITION BY ...)*.

**18.** Pour chaque contrat actif, comptez le nombre de **garanties incluses** (tableau JSONB `garanties->'garantiesIncluses'`), puis donnez la moyenne de ce nombre par formule.
*Indice : `jsonb_array_length`.*

**19.** Affichez l'évolution **cumulée** du nombre de contrats créés jour par jour : date, contrats créés ce jour, total cumulé.
*Indice : `SUM(...) OVER (ORDER BY ...)`.*

**20.** Pour chaque contrat, calculez l'écart entre sa prime TTC et la prime moyenne de **sa** formule, et affichez les 10 contrats les plus « au-dessus de la moyenne » (numéro, formule, prime, moyenne de la formule arrondie, écart arrondi).
*Indice : `AVG(...) OVER (PARTITION BY ...)` — pas de `GROUP BY`.*

---
---

# Corrigé

<details>
<summary>⚠️ Déplier uniquement après avoir cherché !</summary>

```sql
-- 1
SELECT count(*) FROM contrats;

-- 2
SELECT email, nom, prenom
FROM users
ORDER BY created_at DESC
LIMIT 10;

-- 3
SELECT numero_contrat, prime_ttc
FROM contrats
WHERE formule = 'PREMIUM' AND prime_ttc > 500
ORDER BY prime_ttc DESC;

-- 4
SELECT DISTINCT statut FROM contrats;

-- 5
SELECT statut, count(*)
FROM devis
GROUP BY statut;

-- 6
SELECT count(*)
FROM contrats
WHERE date_signature::date = CURRENT_DATE;

-- 7
SELECT min(prime_ttc), max(prime_ttc), round(avg(prime_ttc), 2)
FROM contrats
WHERE statut = 'ACTIF';

-- 8
SELECT count(*)
FROM users
WHERE email LIKE '%@perf.iard.test';

-- 9
SELECT u.email, count(*) AS nb_contrats
FROM contrats c
JOIN users u ON u.id = c.user_id
GROUP BY u.email
ORDER BY nb_contrats DESC
LIMIT 10;

-- 10
SELECT formule,
       count(*)              AS nb_contrats,
       sum(prime_ttc)        AS prime_totale,
       round(avg(prime_ttc), 2) AS prime_moyenne
FROM contrats
GROUP BY formule;

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
SELECT periode,
       count(*) FILTER (WHERE statut = 'SUCCES') AS nb_succes,
       count(*) FILTER (WHERE statut = 'FAILED') AS nb_echecs,
       coalesce(sum(montant) FILTER (WHERE statut = 'SUCCES'), 0) AS total_encaisse
FROM paiements
GROUP BY periode
ORDER BY periode;

-- 13
SELECT d.donnees_risque->>'typeBien' AS type_bien, count(*)
FROM contrats c
JOIN devis d ON d.id = c.devis_id
GROUP BY type_bien;

-- 14
SELECT u.email, count(DISTINCT c.formule) AS nb_formules
FROM contrats c
JOIN users u ON u.id = c.user_id
GROUP BY u.email
HAVING count(DISTINCT c.formule) >= 2;

-- 15
SELECT u.email, c.numero_contrat, p.montant
FROM paiements p
JOIN contrats c ON c.id = p.contrat_id
JOIN users u    ON u.id = c.user_id
WHERE p.statut = 'FAILED';

-- 16
SELECT c.formule,
       round(avg((d.donnees_risque->>'surfaceHabitable')::int), 1) AS surface_moyenne
FROM contrats c
JOIN devis d ON d.id = c.devis_id
GROUP BY c.formule;

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
SELECT formule,
       round(avg(nb_garanties), 2) AS moyenne_garanties_incluses
FROM (
    SELECT formule,
           jsonb_array_length(garanties->'garantiesIncluses') AS nb_garanties
    FROM contrats
    WHERE statut = 'ACTIF'
) t
GROUP BY formule;

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
       round(moyenne_formule, 2) AS moyenne_formule,
       round(prime_ttc - moyenne_formule, 2) AS ecart
FROM (
    SELECT numero_contrat, formule, prime_ttc,
           AVG(prime_ttc) OVER (PARTITION BY formule) AS moyenne_formule
    FROM contrats
) t
ORDER BY ecart DESC
LIMIT 10;
```

</details>
