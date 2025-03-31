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
    implementation(libs.edc.controlplane.base.bom)
    implementation(libs.edc.controlplane.feature.sql.bom)
    //implementation(libs.edc.controlplane.oauth2.bom)
    implementation(libs.edc.iam.mock)
    implementation(libs.edc.dataplane.base.bom) {
        exclude(module = "data-plane-selector-client")
        exclude(module = "data-plane-iam")  // to support PULL flow this would need to be added back
    }
    implementation(libs.edc.dataplane.feature.sql.bom)

    implementation(libs.edc.data.plane.aws.s3)
	implementation(libs.edc.validator.data.address.aws.s3)
	//implementation(libs.edc.monitor.jdk.logger)
	implementation(libs.edc.vault.hashicorp)
	//implementation(libs.edc.oauth2.daps)

    implementation(project(":policy-extension"))
}

repositories {
	mavenLocal()
	mavenCentral()
}

application {
    mainClass.set("org.eclipse.edc.boot.system.runtime.BaseRuntime")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
	exclude("**/pom.properties", "**/pom.xm")
    mergeServiceFiles()
    archiveFileName.set("connector.jar")
}
