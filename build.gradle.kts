import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "2.1.20"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    application
}

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.github.javaparser:javaparser-core:3.27.0")
    implementation("com.google.guava:guava:33.4.8-jre")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

val mainClassName = "com.example.scf.StringConstantFinder"

application {
    mainClass.set(mainClassName)
}

tasks.withType<ShadowJar> {
    manifest {
        attributes["Main-Class"] = mainClassName
    }
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
