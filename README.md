# POSSIBLE-X Extension

This repository contains the POSSIBLE-X Extension that works with
the [Eclipse Dataspace Components](https://github.com/eclipse-edc) [Connector](https://github.com/eclipse-edc/Connector)
in the version `v0.4.1` to technically enforce the so-called _POSSIBLE-X enforced policies_.

POSSIBLE-X Dataspace participants can define a POSSIBLE-X enforced policy when they provide Service Offerings / Data
Service Offerings. The policy consists of constraints that the provider participants choose. The POSSIBLE-X Extension
implements the enforcement of following constraints which take effect during contract negotiation / data transfer
attempts:

- The consumer of an offering must be on the list of allowed consumer participants. Only then is it possible to
  negotiate a contract and trigger a data transfer in the case of Data Service Offerings. The provider participant
  defines the participant list.
- The consumer of an offering can only negotiate a contract after a certain start date. This date is defined by the provider
  participant.
- The consumer of an offering can only negotiate a contract before a certain end date. This date is defined by the provider
  participant.

The implementation can be found in the `policy-extension` folder.

## How to run an EDC connector with the POSSIBLE-X and the IONOS S3 extensions

Below is an explanation on how to run an EDC with the POSSIBLE-X and
the [IONOS S3](https://github.com/Digital-Ecosystems/edc-ionos-s3) extensions.

### Requirements

- Java 17
- Gradle
- Postman/Insomnia (_optional_)

### Steps

#### Checkout the repository

- Checkout this repository

```
git clone  https://github.com/POSSIBLE-X/possible-x-edc-extension.git
```

#### Create git token

- Open the GitHub settings for your personal access tokens: https://github.com/settings/tokens
- Generate a new token. Select only the scope `read:packages` for it.

#### Compiling

- Export your `GitHub` authentication data. Use the token created in the previous step.

```
export USERNAME_GITHUB=<YOUR USERNAME> or <YOUR TOKEN NAME>
export TOKEN_GITHUB=<YOUR TOKEN>
```

- Go to the main folder and execute the following:

```
./gradlew build
```

#### Edit config files

- Open the `connector/resources/config.properties` file and edit the following fields. Take the values from the KeePass
  DB in the `possible-x-infra` repository and insert them.

| Field name                      | Description                          |
|---------------------------------|--------------------------------------|
| `possible.connector.edcVersion` | Version of the Connector             |
| `edc.ionos.access.key`          | IONOS Access Key Id to access S3     |
| `edc.ionos.secret.key`          | IONOS Secret Access Key to access S3 |
| `edc.ionos.endpoint.region`     | IONOS S3 Endpoint Region             |
| `edc.ionos.token`               | IONOS token to allow S3 provisioning |

- Add these fields to the `provider-configuration.properties` and `consumer-configuration.properties` as well, before
  starting one dedicated consumer and one dedicated provider instance.

To know more about the `IONOS S3 Extension` please check this [site](https://github.com/ionos-cloud/edc-ionos-s3).

#### Running

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

### Examples

For experimenting with the running EDC, some Postman/Insomnia collections were added to this repository at `postman/`:

- `POSSIBLE-X-IONOS-S3.postman_collection.json` is a Postman collection with examples for performing a transfer from and
  to an IONOS S3 Bucket.
- `POSSIBLE-X.Insomnia_IONOS-S3.json` is an Insomnia collection with examples for performing a transfer from and to an
  IONOS S3 Bucket.
- `POSSIBLE-X.postman_collection.json` is a Postman collection with examples for performing a transfer from and to an
  HTTP API.
- `Test.postman_environment.json` is a Postman environment file which sets up the collections to interact with the
  POSSIBLE-X EDCs running in the IONOS cloud.

Import the collection into Postman/Insomnia. In the collection's settings, fill the Environment Variables with the path
and port of your running EDC.
Check the corresponding `[...]-configuration.properties` for the correct values.

### Continuous Integration

A GitHub Action Pipeline (Build and Deploy EDC) was implemented to build and deploy the Artifacts to the DEV environment

The Pipeline Builds a docker container and deploys it to the IONOS Cloud Possible-X Kubernetes Cluster:

| Component | Namespace           | URL                                    |
|-----------|---------------------|----------------------------------------|
| Consumer  | dev-github-consumer | https://consumer.edc.dev.possible-x.de |
| Provider  | dev-github-provider | https://provider.edc.dev.possible-x.de |