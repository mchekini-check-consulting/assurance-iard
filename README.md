# Plateforme IARD

Plateforme d'assurance IARD organisée en **trois projets** :

| Projet | Contenu | Techno | Base de données |
|---|---|---|---|
| `iard-client/` | Application dédiée au client (front `frontend/` + back `backend/`) | Angular 20 · Spring Boot 3.2 / Java 24 | PostgreSQL `iard_db` |
| `moteur-tarifaire/` | API de tarification habitation | Spring Boot 3.4 / Java 24 | — |
| `sinistre-treatment/` | Gestion des sinistres (front `frontend/` + back `backend/`) | Angular 20 · Spring Boot 3.4 / Java 24 | PostgreSQL `sinistre_db` |

Les applications communiquent de façon asynchrone via Kafka (déclaration de
sinistre → traitement → décision).

## Images Docker

Cinq images publiées sur DockerHub :

| Image | Contexte de build |
|---|---|
| `mchekini/iard-client-frontend:1.0` | `iard-client/frontend` |
| `mchekini/iard-client-backend:1.0` | `iard-client/backend` |
| `mchekini/moteur-tarifaire:1.0` | `moteur-tarifaire` |
| `mchekini/sinistre-treatment-frontend:1.0` | `sinistre-treatment/frontend` |
| `mchekini/sinistre-treatment-backend:1.0` | `sinistre-treatment/backend` |

### Build depuis un Mac (cible VM Linux)

Les images sont buildées avec `--platform linux/amd64` pour fonctionner sur la
VM Linux même quand elles sont construites sur un Mac Apple Silicon (arm64) :

```bash
docker login                      # compte mchekini
./scripts/build-push.sh           # build linux/amd64 + push DockerHub
./scripts/build-push.sh --no-push # build local uniquement
```

## Déploiement sur la VM (`deploy/`)

`deploy/docker-compose.yml` tire les images DockerHub et démarre l'ensemble de
la plateforme derrière un **reverse proxy nginx**, seul point d'entrée HTTP :

| Port VM | Service |
|---|---|
| 80 | Espace client (front + `/api`) |
| 81 | Gestion des sinistres (front + `/api`) |
| 8081 | Moteur tarifaire (API) |
| 8085 | Kafka UI |

Pour la **formation**, les infrastructures sont aussi exposées directement :

| Port VM | Service | Identifiants |
|---|---|---|
| 5432 | PostgreSQL `iard_db` | `iard_user` / `iard_password` |
| 5433 | PostgreSQL `sinistre_db` | `sinistre_user` / `sinistre_password` |
| 9092 | Kafka (listener EXTERNAL) | — |

Sur la VM :

```bash
mkdir -p ~/iard && cd ~/iard
# y copier deploy/docker-compose.yml, deploy/nginx/reverse-proxy.conf et deploy/.env.example
cp .env.example .env   # puis renseigner OPENAI_API_KEY, JWT_SECRET, KAFKA_EXTERNAL_HOST (IP de la VM)
docker compose up -d
```

> `KAFKA_EXTERNAL_HOST` doit contenir l'IP publique de la VM, sinon les clients
> Kafka externes (postes des stagiaires) ne pourront pas se connecter sur `VM:9092`.

## Pipeline CI/CD (GitHub Actions)

`.github/workflows/build-and-deploy.yml` : à chaque push sur `main` (ou
manuellement via *workflow_dispatch*) :

1. **build** — build en parallèle des 5 images (`linux/amd64`) et push sur DockerHub ;
2. **deploy** — copie de `deploy/` sur la VM par SCP puis `docker compose pull && up -d` par SSH.

Secrets GitHub à configurer (*Settings → Secrets and variables → Actions*) :

| Secret | Rôle |
|---|---|
| `DOCKERHUB_USERNAME` | Compte DockerHub (`mchekini`) |
| `DOCKERHUB_TOKEN` | Access token DockerHub |
| `VM_HOST` | IP de la VM |
| `VM_USER` | Utilisateur SSH |
| `VM_SSH_KEY` | Clé privée SSH |

Le fichier `~/iard/.env` de la VM est créé au premier déploiement à partir de
l'exemple puis **jamais écrasé** : les secrets applicatifs restent sur la VM.

## Secrets

Copier `.env.example` en `.env` à la racine et renseigner `OPENAI_API_KEY`
(utilisée pour l'OCR des documents KYC ; si absente, l'OCR est simplement désactivé).

**Ne jamais commiter `.env`** (il est dans `.gitignore`). Aucun secret n'est
copié dans les images Docker : ils sont injectés à l'exécution par docker compose.

## Lancement full Docker en local (développement)

Le `docker-compose.yml` à la racine builde les images localement :

```bash
docker compose up --build
```

URLs : espace client http://localhost:4200 · gestion sinistres http://localhost:4300 ·
API souscription http://localhost:8080 · moteur tarifaire http://localhost:8081 ·
API sinistres http://localhost:8090 · Kafka UI http://localhost:8085

En mode Docker, les frontends sont servis par nginx qui proxifie `/api` vers leur
backend respectif (même comportement que le `proxy.conf.json` du mode dev).
Les pièces jointes de sinistres sont persistées dans le volume `uploads_data`.

## Lancement local (développement hors Docker)

Démarrer uniquement l'infrastructure :

```bash
docker compose up -d postgres postgres-sinistre-treatment kafka kafka-ui
```

Puis lancer les applications sur la machine hôte — les valeurs par défaut
(`localhost:5432`, `localhost:5433`, `localhost:9092`) s'appliquent sans aucune
variable d'environnement :

```bash
# Backends
cd iard-client/backend && mvn spring-boot:run          # nécessite OPENAI_API_KEY pour l'OCR : source .env
cd moteur-tarifaire && mvn spring-boot:run
cd sinistre-treatment/backend && mvn spring-boot:run

# Frontends
cd iard-client/frontend && npm start                   # http://localhost:4200
cd sinistre-treatment/frontend && npm start            # http://localhost:4300
```
