# Helm charts

This directory contains required Helm charts and Helm values for the deployment of the POSSIBLE-X Extension.

## Manually deploying the POSSIBLE-X Extension to a Kubernetes cluster

The POSSIBLE-X Extension can be deployed to a Kubernetes cluster using the Helm charts in this directory.

### Requirements
- [Helm](https://helm.sh/docs/intro/install/)
- [kubectl](https://kubernetes.io/docs/tasks/tools/install-kubectl/)
- [Kubernetes cluster](https://kubernetes.io/docs/setup/)


### Deploy

1. Create a secret for the GitHub container registry credentials. For the password, use a [personal access token (classic)](https://docs.github.com/en/github/authenticating-to-github/creating-a-personal-access-token) with the `read:packages` scope. The username is your GitHub username.

    ```bash
    kubectl create secret docker-registry <secret-name> --docker-server=ghcr.io --docker-username=<username> --docker-password=<password> --docker-email=<email> --namespace possible-x-edc --create-namespace
    ```
   
   Add the secret name to the `imagePullSecrets` section in the `values.yaml` file.

2. Install Hashicorp Vault Helm chart

    ```bash
    helm repo add hashicorp https://helm.releases.hashicorp.com
    helm install vault hashicorp/vault --version 0.19.0 --namespace possible-x-edc

    # Initialize Vault
    kubectl exec --namespace possible-x-edc -it vault-0 -- vault operator init -key-shares=1 -key-threshold=1 -format=json > vault-keys.json

    # Unseal Vault
    kubectl exec --namespace possible-x-edc -it vault-0 -- vault operator unseal $(jq -r ".unseal_keys_b64[]" vault-keys.json)

    # Login to Vault
    kubectl exec --namespace possible-x-edc -it vault-0 -- vault login $(jq -r ".root_token" vault-keys.json)

    # Enable KV secrets engine
    kubectl exec --namespace possible-x-edc -it vault-0 -- vault secrets enable -version=2 -path=secret kv

    # Add secrets to Vault
    kubectl exec --namespace possible-x-edc -it vault-0 -- vault kv put secret/edc.ionos.access.key content=
    kubectl exec --namespace possible-x-edc -it vault-0 -- vault kv put secret/edc.ionos.secret.key content=
    kubectl exec --namespace possible-x-edc -it vault-0 -- vault kv put secret/edc.ionos.token content=
    kubectl exec --namespace possible-x-edc -it vault-0 -- vault kv put secret/possible.catalog.jwt.token content=
    ```

3. Install the POSSIBLE-X Extension Helm chart

    ```bash
    helm install --namespace possible-x-edc possible-x-edc possible-x-edc/
    ```
