import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar


plugins {
  val kotlinVersion = "2.1.21"

  java
  kotlin("jvm") version kotlinVersion
  kotlin("kapt") version kotlinVersion
  application
  id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "io.foldright"
version = "0.1.0-SNAPSHOT"

repositories { mavenCentral() }

val picocliVersion = "4.7.7"
dependencies {
  implementation("com.github.javaparser:javaparser-core:3.27.0")

  implementation("info.picocli:picocli:$picocliVersion")
  kapt("info.picocli:picocli-codegen:$picocliVersion")

  testImplementation(kotlin("test"))
  compileOnly("org.jetbrains:annotations:26.0.2")
}

kotlin { jvmToolchain(8) }

tasks.test { useJUnitPlatform() }

val mainClassName = "io.foldright.dslf.DuplicateStringLiteralFinder"
application { mainClass.set(mainClassName) }
tasks.withType<ShadowJar> {
  manifest { attributes["Main-Class"] = mainClassName }
  exclude("META-INF/native-image/")
  dependencies {
    exclude(dependency("org.jetbrains:annotations:.*"))
  }
}
tasks.build { dependsOn(tasks.shadowJar) }
