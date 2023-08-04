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
	
    maven {// while runtime-metamodel dependency is still a snapshot
		url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
    }
	  maven {
        url = uri("https://maven.iais.fraunhofer.de/artifactory/eis-ids-public/")
    }
	maven {
		url = uri("https://maven.pkg.github.com/Digital-Ecosystems/edc-ionos-s3")
		credentials {
			username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME_GITHUB")
			password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN_GITHUB")
		}
	}
	
	mavenLocal()
	mavenCentral()
}

val javaVersion: String by project
val edcGroup: String by project
val edcVersion: String by project
val okHttpVersion: String by project
val rsApi: String by project
val metaModelVersion: String by project
val fraunhoferVersion: String by project




dependencies {
	implementation("${edcGroup}:boot:${edcVersion}")
	
    implementation("${edcGroup}:control-plane-core:${edcVersion}")
	
	implementation("${edcGroup}:api-observability:${edcVersion}")
	
	implementation("${edcGroup}:configuration-filesystem:${edcVersion}")

	implementation("${edcGroup}:http:${edcVersion}")
	implementation("${edcGroup}:dsp:${edcVersion}")	
	
	implementation("${edcGroup}:auth-tokenbased:${edcVersion}")	

    implementation("$edcGroup:management-api:$edcVersion")

	implementation("${edcGroup}:iam-mock:${edcVersion}")

	implementation(project(":extension"))
 
	testImplementation ("${edcGroup}:junit:${edcVersion}")	
	
	
	implementation("${edcGroup}:data-plane-selector-core:${edcVersion}")

	implementation("${edcGroup}:data-plane-client:${edcVersion}")

	implementation("${edcGroup}:transfer-data-plane:${edcVersion}")
	
	implementation("${edcGroup}:data-plane-selector-client:${edcVersion}")

	//IONOS
	implementation ("com.ionoscloud.edc:provisioning-ionos-s3:0.0.1-SNAPSHOT")
	implementation ("com.ionoscloud.edc:data-plane-ionos-s3:0.0.1-SNAPSHOT")
	
	implementation("de.fraunhofer.iais.eis.ids.infomodel:java:${fraunhoferVersion}")

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
