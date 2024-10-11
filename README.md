# POSSIBLE Extension

This document explains how to run an EDC with the `POSSIBLE-X` and the `IONOS S3` extensions.

## Requirements

- java 17
- gradle
- Postman/Insomnia (_optional_)

## Steps
### Checkout the repo

- Checkout this repository
```
git clone  https://github.com/POSSIBLE-X/possible-x-edc-extension.git
```

### Create git token
- open the github settings for your personal access tokens: https://github.com/settings/tokens
- generate a new token. select only the scope `read:packages` for it.

### Compiling

- export your `GitHub` authentication data. use the token created in the previous step.
```
export USERNAME_GITHUB=<YOUR USERNAME> or <YOUR TOKEN NAME>
export TOKEN_GITHUB=<YOUR TOKEN>
```
- go to the main folder and execute the following:
```
./gradlew build
```

### Edit config files

- Open the `connector/resources/config.properties` file and edit the following fields. Take the values from the keepass DB in the `possible-x-infra` repo and insert them.  

| Field name                      | Description                                                |
|---------------------------------|------------------------------------------------------------|
| `possible.connector.edcVersion` | Version of the Connector                                   |
| `edc.ionos.access.key`          | IONOS Access Key Id to access S3                           |
| `edc.ionos.secret.key`          | IONOS Secret Access Key to access S3                       |
| `edc.ionos.endpoint`            | IONOS S3 Endpoint                                          |
| `edc.ionos.token`               | IONOS token to allow S3 provisioning                       |

- Add these fields to the `provider-configuration.properties` and `consumer-configuration.properties` as well, before starting one dedicated consumer and one dedicated provider instance.

To know more about the `IONOS S3 Extension` please check this [site](https://github.com/ionos-cloud/edc-ionos-s3).

### Running

Either execute the following command, for starting one instance:
```
java -Dedc.fs.config=connector/resources/config.properties  -jar connector/build/libs/connector.jar
```  
Or execute the following commands, for starting one dedicated consumer and one dedicated provider instance:
```
java -Dedc.fs.config=connector/resources/provider-configuration.properties  -jar connector/build/libs/connector.jar
```  
```
java -Dedc.fs.config=connector/resources/consumer-configuration.properties  -jar connector/build/libs/connector.jar
```  

### Interacting

- Please follow the documentation of the `documentation` repository (LINK TBD).

## Examples
For experimenting with the running EDC, some Postman/Insomnia collections were added to this repo at `postman/`:

- `POSSIBLE-X-IONOS-S3.postman_collection.json` is a Postman collection with examples for performing a transfer from and to an IONOS S3 Bucket
- `POSSIBLE-X.Insomnia_IONOS-S3.json` is an Insomnia collection with examples for performing a transfer from and to an IONOS S3 Bucket
- `POSSIBLE-X.postman_collection.json` is a Postman collection with examples for performing a transfer from and to an HTTP API
- `Test.postman_environment.json` is a Postman environment file which sets up the collections to interact with the POSSIBLE EDCs running in the IONOS cloud

Import the collection into Postman/Insomnia. In the collection's settings, fill the Environment Variables with the path and port of your running EDC.
Check the corresponding `[...]-configuration.properties` for the correct values.

## Continuous Integration
A Github Action Pipeline (Build and Deploy EDC) was implemented to build and deploy the Artifcats to the DEV environment

The Pipeline Builds a docker container and deploys it to the IONOS Cloud PossibleX Kubernetes Cluster

| Component | Namespace              | URL                                  |
|-----------|------------------------|--------------------------------------|
| Consumer  | dev-github-consumer    | https://consumer.dev.possible-x.de   |
| Provider  | dev-github-provider    | https://provider.dev.possible-x.de   |