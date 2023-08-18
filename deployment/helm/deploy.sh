#!/bin/bash

set -xe

source .config

# read S3_BUCKET argument
EDC_NAMESPACE=$1
# print usage if no argument is provided
if [ -z "$EDC_NAMESPACE" ]
then
    echo "Usage: ./deploy.sh <EDC_NAMESPACE>"
    exit 1
fi
EDC_VAULT_NAME="${EDC_NAMESPACE}-vault"

kubectl delete namespace "${EDC_NAMESPACE}" || true
kubectl create namespace "${EDC_NAMESPACE}"

kubectl -n "${EDC_NAMESPACE}" create secret docker-registry github-registry-auth \
    --docker-server=ghcr.io \
    --docker-username=$GITHUB_USER \
    --docker-password=$GITHUB_TOKEN

helm repo add hashicorp https://helm.releases.hashicorp.com
helm install "${EDC_VAULT_NAME}" hashicorp/vault --version 0.19.0 -n "${EDC_NAMESPACE}"

# Wait for Vault to be ready
sleep 10
kubectl wait --for=jsonpath='{.status.phase}'=Running pod "${EDC_VAULT_NAME}-0" -n "${EDC_NAMESPACE}" --timeout=120s

# Initialize Vault
kubectl exec -n "${EDC_NAMESPACE}" -it "${EDC_VAULT_NAME}-0" -- vault operator init -key-shares=1 -key-threshold=1 -format=json > vault-keys.json

# Unseal Vault
kubectl exec -n "${EDC_NAMESPACE}" -it "${EDC_VAULT_NAME}-0" -- vault operator unseal $(jq -r ".unseal_keys_b64[]" vault-keys.json)

# Login to Vault
kubectl exec -n "${EDC_NAMESPACE}" -it "${EDC_VAULT_NAME}-0" -- vault login $(jq -r ".root_token" vault-keys.json)

# Enable KV secrets engine
kubectl exec -n "${EDC_NAMESPACE}" -it "${EDC_VAULT_NAME}-0" -- vault secrets enable -version=2 -path=secret kv

# Add secrets to Vault
kubectl exec -n "${EDC_NAMESPACE}" -it "${EDC_VAULT_NAME}-0" -- vault kv put secret/edc.ionos.access.key content=$EDC_S3_ACCESS_KEY
kubectl exec -n "${EDC_NAMESPACE}" -it "${EDC_VAULT_NAME}-0" -- vault kv put secret/edc.ionos.secret.key content=$EDC_S3_SECRET_KEY
kubectl exec -n "${EDC_NAMESPACE}" -it "${EDC_VAULT_NAME}-0" -- vault kv put secret/edc.ionos.endpoint content=$EDC_S3_ENDPOINT
kubectl exec -n "${EDC_NAMESPACE}" -it "${EDC_VAULT_NAME}-0" -- vault kv put secret/edc.ionos.token content=$EDC_S3_IONOS_TOKEN
kubectl exec -n "${EDC_NAMESPACE}" -it "${EDC_VAULT_NAME}-0" -- vault kv put secret/possible.catalog.jwt.token content=$EDC_CATALOG_JWT_TOKEN


helm install -n "${EDC_NAMESPACE}" -f provider.yaml possible-x-edc possible-x-edc/

sleep 20

CONNECTOR_ADDRESS=$(kubectl get svc -n $EDC_NAMESPACE possible-x-edc -o jsonpath='{.status.loadBalancer.ingress[0].ip}')

# Check if the connector address is empty
if [ -z "$CONNECTOR_ADDRESS" ]; then
    echo "Connector address is empty"
    exit 1
fi

# Change public address in the config.properties in the configmap
kubectl -n $EDC_NAMESPACE get configmap possible-x-edc-config -o yaml | sed "s/edc.dsp.callback.address=.*/edc.dsp.callback.address=http:\/\/$CONNECTOR_ADDRESS:8281\/protocol/g" | kubectl apply -f -

kubectl -n $EDC_NAMESPACE get configmap possible-x-edc-config -o yaml | sed "s/edc.receiver.http.endpoint=.*/edc.receiver.http.endpoint=http:\/\/$CONNECTOR_ADDRESS:4000\/receiver\/urn:connector:provider\/callback/g" | kubectl apply -f -

kubectl -n $EDC_NAMESPACE get configmap possible-x-edc-config -o yaml | sed "s/edc.dataplane.token.validation.endpoint=.*/edc.dataplane.token.validation.endpoint=http:\/\/$CONNECTOR_ADDRESS:8283\/control\/token/g" | kubectl apply -f -

# Restart the pods
kubectl -n $EDC_NAMESPACE delete pod -l app.kubernetes.io/name=possible-x-edc
