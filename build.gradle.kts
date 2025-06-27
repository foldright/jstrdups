import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar


plugins {
  val kotlinVersion = "2.1.21"

  java
  kotlin("jvm") version kotlinVersion
  kotlin("kapt") version kotlinVersion
  application
  id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.example"
version = "1.0-SNAPSHOT"

repositories { mavenCentral() }

val picocliVersion = "4.7.7"
dependencies {
  implementation("com.github.javaparser:javaparser-core:3.27.0")

  implementation("info.picocli:picocli:$picocliVersion")
  kapt("info.picocli:picocli-codegen:$picocliVersion")

  testImplementation(kotlin("test"))
}

kotlin { jvmToolchain(17) }
sourceSets {
  main { java.srcDir("src/main/java") }
  test { java.srcDir("src/test/java") }
}

tasks.test { useJUnitPlatform() }

val mainClassName = "com.example.scf.DuplicateStringLiteralFinder"
application { mainClass.set(mainClassName) }
tasks.withType<ShadowJar> {
  manifest { attributes["Main-Class"] = mainClassName }
}
tasks.build { dependsOn(tasks.shadowJar) }
