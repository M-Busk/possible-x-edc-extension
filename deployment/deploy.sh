#!/bin/bash

set -xe

cd "$(dirname "$0")"

source .config

cd helm

EDC_ROLE="${1:-$EDC_ROLE}"
EDC_NAMESPACE="${2:-$EDC_NAMESPACE}"

# print usage if no argument is provided
if [[ -z $EDC_ROLE || -z $EDC_NAMESPACE ]]; then
    echo "Usage: ./deploy.sh <EDC_ROLE> <EDC_NAMESPACE>"
    echo "e.g ./deploy.sh consumer possible-x-edc-consumer"
    echo "Or set the EDC_ROLE and EDC_NAMESPACE environment variables"
    exit 1
fi

if [[ -z $EDC_HOST ]]; then
    export EDC_HOST="${EDC_ROLE}.possible-x.de"
    echo "* * * * * * * * * * * * * * * * * * * * * * * * * * * * * *"
    echo "WARNING: EDC_HOST is not set, using ${EDC_HOST} as default"
    echo "* * * * * * * * * * * * * * * * * * * * * * * * * * * * * *"
fi

export EDC_VAULT_NAME="${EDC_NAMESPACE}-${EDC_ROLE}-vault"

kubectl --kubeconfig=$KUBECONFIG_EDC create namespace "${EDC_NAMESPACE}" || true

if [[ $EDC_DISABLE_HASHI_VAULT ]] ; then
  EDC_VAULT_ROOT_TOKEN="VAULT_DISABLED"
else
  helm --kubeconfig=$KUBECONFIG_EDC repo add hashicorp https://helm.releases.hashicorp.com
  helm --kubeconfig=$KUBECONFIG_EDC install "${EDC_VAULT_NAME}" hashicorp/vault --version 0.19.0 -n "${EDC_NAMESPACE}"

  # Wait for Vault to be ready
  sleep 10
  kubectl --kubeconfig=$KUBECONFIG_EDC wait --for=jsonpath='{.status.phase}'=Running pod "${EDC_VAULT_NAME}-0" -n "${EDC_NAMESPACE}" --timeout=120s

  # Initialize Vault
  kubectl --kubeconfig=$KUBECONFIG_EDC exec -n "${EDC_NAMESPACE}" -it "${EDC_VAULT_NAME}-0" -- vault operator init -key-shares=1 -key-threshold=1 -format=json > "${EDC_VAULT_NAME}-keys.json"

  EDC_VAULT_ROOT_TOKEN=$(cat "${EDC_VAULT_NAME}-vault-keys.json" | jq -r '.root_token')
  export EDC_VAULT_URL="http://${EDC_VAULT_NAME}:8200"

  # Unseal Vault
  kubectl --kubeconfig=$KUBECONFIG_EDC exec -n "${EDC_NAMESPACE}" -it "${EDC_VAULT_NAME}-0" -- vault operator unseal $(jq -r ".unseal_keys_b64[]" "${EDC_VAULT_NAME}-keys.json")

  # Login to Vault
  kubectl --kubeconfig=$KUBECONFIG_EDC exec -n "${EDC_NAMESPACE}" -it "${EDC_VAULT_NAME}-0" -- vault login $(jq -r ".root_token" "${EDC_VAULT_NAME}-keys.json")

  # Enable KV secrets engine
  kubectl --kubeconfig=$KUBECONFIG_EDC exec -n "${EDC_NAMESPACE}" -it "${EDC_VAULT_NAME}-0" -- vault secrets enable -version=2 -path=secret kv

  # Add secrets to Vault
  kubectl --kubeconfig=$KUBECONFIG_EDC exec -n "${EDC_NAMESPACE}" -it "${EDC_VAULT_NAME}-0" -- vault kv put secret/edc.ionos.access.key content=$EDC_S3_ACCESS_KEY
  kubectl --kubeconfig=$KUBECONFIG_EDC exec -n "${EDC_NAMESPACE}" -it "${EDC_VAULT_NAME}-0" -- vault kv put secret/edc.ionos.secret.key content=$EDC_S3_SECRET_KEY
  kubectl --kubeconfig=$KUBECONFIG_EDC exec -n "${EDC_NAMESPACE}" -it "${EDC_VAULT_NAME}-0" -- vault kv put secret/edc.ionos.endpoint content=$EDC_S3_ENDPOINT
  kubectl --kubeconfig=$KUBECONFIG_EDC exec -n "${EDC_NAMESPACE}" -it "${EDC_VAULT_NAME}-0" -- vault kv put secret/edc.ionos.token content=$DCD_IONOS_TOKEN
  kubectl --kubeconfig=$KUBECONFIG_EDC exec -n "${EDC_NAMESPACE}" -it "${EDC_VAULT_NAME}-0" -- vault kv put secret/possible.catalog.jwt.token content=$EDC_CATALOG_JWT_TOKEN
fi

export EDC_VAULT_ROOT_TOKEN
export EDC_IMAGE_PULL_SECRET_NAME="${EDC_ROLE}-github-registry-auth"
kubectl --kubeconfig=$KUBECONFIG_EDC -n "${EDC_NAMESPACE}" create secret docker-registry "${EDC_IMAGE_PULL_SECRET_NAME}" \
    --docker-server=ghcr.io \
    --docker-username=$GITHUB_USER \
    --docker-password=$GITHUB_TOKEN

export EDC_TLS_SECRET_NAME="${EDC_ROLE}-tls"

# write local values file
EDC_HELM_VALUES="${EDC_NAMESPACE}-${EDC_ROLE}.yaml"

rm -f ${EDC_HELM_VALUES} temp.yaml
( echo "cat <<EOF >${EDC_HELM_VALUES}";
  cat template.yaml;
  echo "EOF";
) >temp.yaml
. temp.yaml

helm --kubeconfig=$KUBECONFIG_EDC install -n "${EDC_NAMESPACE}" -f "${EDC_HELM_VALUES}" "possible-x-edc-${EDC_ROLE}" possible-x-edc/

cd ..
