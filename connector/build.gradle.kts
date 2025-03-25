/*
 *  Copyright (c) 2020, 2021 Microsoft Corporation
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Microsoft Corporation - initial API and implementation
 *
 */

plugins {
    `java-library`
    id("application")
    id("com.github.johnrengelman.shadow") version "7.0.0"
}
repositories {
	mavenLocal()
	mavenCentral()
}

val javaVersion: String by project
val edcGroup: String by project
val edcVersion: String by project
val ionosExtensionVersion: String by project


dependencies {
	implementation("${edcGroup}:control-plane-core:${edcVersion}")
	implementation("${edcGroup}:configuration-filesystem:${edcVersion}")
	implementation("${edcGroup}:auth-tokenbased:${edcVersion}")
	implementation("${edcGroup}:management-api:${edcVersion}")
	implementation("${edcGroup}:control-plane-api:${edcVersion}")
	implementation("${edcGroup}:management-api-configuration:${edcVersion}")
	implementation("${edcGroup}:api-observability:${edcVersion}")
	implementation("${edcGroup}:dsp:${edcVersion}")
	implementation("${edcGroup}:jwt-spi:${edcVersion}")
	implementation("${edcGroup}:transfer-data-plane:${edcVersion}")
	implementation("${edcGroup}:data-plane-selector-core:${edcVersion}")
	implementation("${edcGroup}:data-plane-selector-client:${edcVersion}")
	implementation("${edcGroup}:data-plane-selector-spi:${edcVersion}")
	implementation("${edcGroup}:http:${edcVersion}")
	implementation("${edcGroup}:micrometer-core:${edcVersion}")
	implementation("${edcGroup}:jersey-micrometer:${edcVersion}")
	implementation("${edcGroup}:jetty-micrometer:${edcVersion}")
	implementation("${edcGroup}:transfer-pull-http-dynamic-receiver:${edcVersion}")
	implementation("${edcGroup}:callback-event-dispatcher:${edcVersion}")
	implementation("${edcGroup}:callback-http-dispatcher:${edcVersion}")

	implementation("${edcGroup}:data-plane-aws-s3:${edcVersion}")
	implementation("${edcGroup}:data-plane-http-oauth2:${edcVersion}")
	implementation("${edcGroup}:data-plane-http:${edcVersion}")
	implementation("${edcGroup}:data-plane-core:${edcVersion}")
	implementation("${edcGroup}:control-plane-api-client:${edcVersion}")
	implementation("${edcGroup}:data-plane-api:${edcVersion}")
	implementation("${edcGroup}:connector-core:${edcVersion}")
	implementation("${edcGroup}:boot:${edcVersion}")


	implementation("${edcGroup}:vault-hashicorp:${edcVersion}")
	//implementation("${edcGroup}:vault-filesystem:${edcVersion}")
	implementation("${edcGroup}:iam-mock:${edcVersion}")
	//implementation("${edcGroup}:oauth2-service:${edcVersion}")
	//implementation("${edcGroup}:oauth2-daps:${edcVersion}")
	implementation(project(":policy-extension"))

	//implementation("software.amazon.awssdk:s3:2.20.75")
	//implementation("${edcGroup}:provision-aws-s3:${edcVersion}")

  //implementation("${edcGroup}:asset-index-sql:${edcVersion}")
  //implementation("${edcGroup}:contract-definition-store-sql:${edcVersion}")
  //implementation("${edcGroup}:contract-negotiation-store-sql:${edcVersion}")
  //implementation("${edcGroup}:contract-core:${edcVersion}")
  //implementation("${edcGroup}:policy-definition-store-sql:${edcVersion}")
  //implementation("${edcGroup}:policy-monitor-store-sql:${edcVersion}")
  //implementation("${edcGroup}:sql-lease:${edcVersion}")
  //implementation("${edcGroup}:sql-pool-apache-commons:${edcVersion}")
  //implementation("${edcGroup}:transaction-local:$edcVersion")
  //implementation("${edcGroup}:transaction-datasource-spi:$edcVersion")
  //implementation ("org.postgresql:postgresql:42.7.2")
}

repositories {
	mavenLocal()
	mavenCentral()
    maven {// while runtime-metamodel dependency is still a snapshot
		url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
    }
}

application {
    mainClass.set("org.eclipse.edc.boot.system.runtime.BaseRuntime")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
	exclude("**/pom.properties", "**/pom.xm")
    mergeServiceFiles()
    archiveFileName.set("connector.jar")
}
