# Helm charts

This directory contains required Helm charts and Helm values for the deployment of the POSSIBLE-X Extension.

## Manually deploying the POSSIBLE-X Extension to a Kubernetes cluster

The POSSIBLE-X Extension can be deployed to a Kubernetes cluster using the Helm charts in this directory.

### Requirements
- [Helm](https://helm.sh/docs/intro/install/)
- [kubectl](https://kubernetes.io/docs/tasks/tools/install-kubectl/)
- [Kubernetes cluster](https://kubernetes.io/docs/setup/)
- domain name and DNS records pointing to the Kubernetes cluster


### Deploy

1. Copy the .config-template file to .config and fill in the values.

   ```bash
   cp .config-template .config
   ```

2. Call the deployment script. This will create the required configuration from the values defined in the .config file and deploy the Helm chart.

   The script can be called without any parameters, in which case it will use the default values defined in the .config file. Alternatively, the script can be called with the following parameters:

   ```bash
   ./deploy.sh <EDC_ROLE> <EDC_NAMESPACE>
   ```

### Config Parameters

| Parameter name             | Description                                                                                                                                                                                                                                                                                                                                          |
|----------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `EDC_ROLE`                 | The "role" of the EDC instance e.g. consumer or provider. This value is also used for some names of resources in the kubernetes cluster e.g. <ul><li>the Helm release name is possible-x-edc-`EDC_ROLE`</li> <li>the name of the tls secret is `EDC_ROLE`-tls</li> <li>the name of the imagePullSecrets is `EDC_ROLE`-github-registry-auth</li></ul> |
| `EDC_NAMESPACE`            | The namespace of the EDC instance.                                                                                                                                                                                                                                                                                                                   |
| `EDC_HOST`                 | The domain name of the EDC instance. Leave empty to automatically use `EDC_ROLE`.possible-x.de                                                                                                                                                                                                                                                       |
| `EDC_S3_ACCESS_KEY`        | IONOS Access Key Id to access S3                                                                                                                                                                                                                                                                                                                     |
| `EDC_S3_SECRET_KEY`        | IONOS Secret Access Key to access S3                                                                                                                                                                                                                                                                                                                 |
| `EDC_S3_ENDPOINT`          | IONOS S3 Endpoint                                                                                                                                                                                                                                                                                                                                    |
| `DCD_IONOS_TOKEN`       | IONOS token to allow S3 provisioning                                                                                                                                                                                                                                                                                                                 |
| `EDC_CATALOG_JWT_TOKEN`    | JWT Token for accessing the POSSIBLE catalog                                                                                                                                                                                                                                                                                                         |
| `GITHUB_USER`              | GitHub Username and auth token for the Github Package Registry                                                                                                                                                                                                                                                                                       |
| `GITHUB_TOKEN`             | Classic GitHub token with at least package:read scope                                                                                                                                                                                                                                                                                                |
| `EDC_DISABLE_HASHI_VAULT`  | Disable the Hashicorp Vault integration                                                                                                                                                                                                                                                                                                              |
