#!/usr/bin/env bash
# Build des 5 images depuis un Mac (Apple Silicon ou Intel) POUR une VM Linux.
# IMPORTANT : --platform linux/amd64 garantit que les images buildées sur mac
# fonctionnent sur la VM Linux (sans ce flag, un Mac M1/M2/M3 produirait des
# images arm64 inutilisables sur une VM amd64).
#
# Usage :
#   ./scripts/build-push.sh            # build + push sur GHCR
#   ./scripts/build-push.sh --no-push  # build local uniquement (chargé dans le démon)
#
# Prérequis : docker login ghcr.io -u <user-github> (mot de passe = PAT avec scope write:packages)

set -euo pipefail

REGISTRY_USER="ghcr.io/mchekini-check-consulting"
VERSION="1.0"
PLATFORM="linux/amd64"
ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"

if [[ "${1:-}" == "--no-push" ]]; then
  OUTPUT="--load"
else
  OUTPUT="--push"
fi

# image -> contexte de build
declare -a IMAGES=(
  "iard-client-backend:iard-client/backend"
  "iard-client-frontend:iard-client/frontend"
  "moteur-tarifaire:moteur-tarifaire"
  "sinistre-treatment-backend:sinistre-treatment/backend"
  "sinistre-treatment-frontend:sinistre-treatment/frontend"
)

for entry in "${IMAGES[@]}"; do
  name="${entry%%:*}"
  context="${entry#*:}"
  tag="${REGISTRY_USER}/${name}:${VERSION}"
  echo "==> Build ${tag} (${PLATFORM}) depuis ${context}"
  docker buildx build \
    --platform "${PLATFORM}" \
    -t "${tag}" \
    ${OUTPUT} \
    "${ROOT_DIR}/${context}"
done

echo "Terminé : 5 images ${REGISTRY_USER}/*:${VERSION} (${PLATFORM})"
