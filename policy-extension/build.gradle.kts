plugins {
    `java-library`
    id("application")
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

val edcGroup: String by project
val edcVersion: String by project

dependencies {
    implementation("${edcGroup}:boot:${edcVersion}")
    implementation("${edcGroup}:connector-core:${edcVersion}")
	implementation("${edcGroup}:core-spi:${edcVersion}")
    implementation("${edcGroup}:http:${edcVersion}")
	implementation("${edcGroup}:control-plane-core:${edcVersion}")
}

application {
    mainClass.set("org.eclipse.edc.boot.system.runtime.BaseRuntime")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    exclude("**/pom.properties", "**/pom.xm")
    mergeServiceFiles()
    archiveFileName.set("connector-possible.jar")
}
