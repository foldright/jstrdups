import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar


plugins {
  val kotlinVersion = "2.1.21"

  java
  kotlin("jvm") version kotlinVersion
  kotlin("kapt") version kotlinVersion
  application
  id("com.github.johnrengelman.shadow") version "8.1.1"
  // https://graalvm.github.io/native-build-tools/latest/gradle-plugin.html
  id("org.graalvm.buildtools.native") version "0.10.6"
}

group = "io.foldright"
version = "1.0.0-SNAPSHOT"

repositories { mavenCentral() }

val picocliVersion = "4.7.7"
dependencies {
  implementation("com.github.javaparser:javaparser-core:3.27.0")

  implementation("info.picocli:picocli:$picocliVersion")
  kapt("info.picocli:picocli-codegen:$picocliVersion")

  testImplementation(kotlin("test"))
}

kotlin { jvmToolchain(17) }

tasks.test { useJUnitPlatform() }

val mainClassName = "io.foldright.dslf.DuplicateStringLiteralFinder"
application { mainClass.set(mainClassName) }
tasks.withType<ShadowJar> {
  manifest { attributes["Main-Class"] = mainClassName }
}
tasks.build { dependsOn(tasks.shadowJar) }

graalvmNative {
  agent {
    enabled.set(true)
  }
  graalvmNative {
    binaries {
      named("main") {
        jvmArgs.add("-Xmx4g")
      }
    }
  }
}
