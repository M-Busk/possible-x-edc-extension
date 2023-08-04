# POSSIBLE Extension

This document explains how to run an EDC with the `POSSIBLE-X` and the `IONOS S3` extensions.

## Requirements

- java 17
- gradle

## Steps
### Checkout the repo

- Checkout this repository
```
git clone  https://github.com/POSSIBLE-X/possible-x-edc-extension.git
```

### Compiling

- export your `GitHub` authentication data
```
export USERNAME_GITHUB=<YOUR USERNAME> or <YOUR TOKEN NAME>
export TOKEN_GITHUB=<YOUR TOKEN>
```
- go to your the main folder and execute the following:
```
./gradlew build
```

### Edit config file

- Open the `connector/resources/config.properties` file and edit the following fields:  

| Field name                      | Description                                                      |
|---------------------------------|------------------------------------------------------------------|
| `possible.catalog.jwt.token`    | Authorization token to access the Possible-X Catalog     |
| `possible.catalog.endpoint`     | Endpoint of the Possible-X Catalog for the SD registration |
| `possible.connector.edcVersion` | Version of the Connector  |
| `edc.ionos.access.key`    | IONOS Access Key Id to access S3     |
| `edc.ionos.secret.key`     | IONOS Secret Access Key to access S3 |
| `edc.ionos.endpoint` | IONOS S3 Endpoint  |
| `edc.ionos.token` | IONOS token to allow S3 provisioning  |

To know more the `IONOS S3 Extension` please check this [site](https://github.com/ionos-cloud/edc-ionos-s3).


### Running

- Execute the following command:
```
java -Dedc.fs.config=connector/resources/config.properties  -jar connector/build/libs/connector.jar
```  

### Interacting

- Please follow the documentation of the `documentation` repository (LINK TBD).

