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
    //implementation(libs.jakarta.rsApi)

    // apache Jena
    implementation("org.apache.jena:jena-core:4.8.0")
    implementation("org.apache.jena:jena-arq:4.8.0")
}

application {
    mainClass.set("org.eclipse.edc.boot.system.runtime.BaseRuntime")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    exclude("**/pom.properties", "**/pom.xm")
    mergeServiceFiles()
    archiveFileName.set("connector-possible.jar")
}
