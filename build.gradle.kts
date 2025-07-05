import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar


plugins {
  val kotlinVersion = "2.1.21"

  java
  kotlin("jvm") version kotlinVersion
  kotlin("kapt") version kotlinVersion
  application
  distribution
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


val appName = "jstrdups"
val mainClassName = "io.foldright.dslf.DuplicateStringLiteralFinder"
application {
  applicationName = appName
  mainClass.set(mainClassName)
}

tasks.withType<ShadowJar> {
  manifest { attributes["Main-Class"] = mainClassName }
  exclude("META-INF/native-image/")
  dependencies {
    exclude(dependency("org.jetbrains:annotations:.*"))
  }
}

val taskGenAutoComplete = "genAutoComplete"
tasks.register<JavaExec>(taskGenAutoComplete) {
  // The module path: typically the runtimeClasspath of your main source set
  classpath = sourceSets["main"].runtimeClasspath
  workingDir = buildDir
  mainClass = "picocli.AutoComplete"
  args = listOf(mainClassName)
}

distributions {
  main {
    contents {
      into("etc/bash_completion.d") {
        from(buildDir.resolve("${appName}_completion"))
      }
      into("zsh/site-functions") {
        from(buildDir.resolve("${appName}_completion")) {
          rename { "_$appName" }
        }
      }
    }
  }
}

tasks.distZip { dependsOn(taskGenAutoComplete) }
tasks.distTar { dependsOn(taskGenAutoComplete) }

tasks.build { dependsOn(tasks.shadowJar, taskGenAutoComplete) }
