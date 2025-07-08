import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  val kotlinVersion = "2.1.21"
  kotlin("jvm") version kotlinVersion
  kotlin("kapt") version kotlinVersion

  application
}

group = "io.foldright"
version = "0.2.0-SNAPSHOT"

repositories { mavenCentral() }

dependencies {
  implementation("com.github.javaparser:javaparser-core:3.27.0")

  val picocliVersion = "4.7.7"
  implementation("info.picocli:picocli:$picocliVersion")
  kapt("info.picocli:picocli-codegen:$picocliVersion")

  testImplementation(kotlin("test"))
  compileOnly("org.jetbrains:annotations:26.0.2")
}
configurations.runtimeClasspath {
  exclude("org.jetbrains", "annotations")
}

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}
tasks.withType<KotlinCompile>().configureEach {
  // https://kotlinlang.org/docs/gradle-compiler-options.html#centralize-compiler-options-and-use-types
  compilerOptions.jvmTarget = JvmTarget.JVM_1_8
}

tasks.test { useJUnitPlatform() }


val mainClassName = "io.foldright.dslf.DuplicateStringLiteralFinder"
val buildDir: File = layout.buildDirectory.get().asFile

val taskGenAutoComplete by tasks.registering(JavaExec::class) {
  classpath = sourceSets["main"].runtimeClasspath
  workingDir = buildDir
  mainClass = "picocli.AutoComplete"
  args = listOf(mainClassName)
}

distributions {
  val completionFile: File = buildDir.resolve("${project.name}_completion")
  main {
    contents {
      into("etc/bash_completion.d") { from(completionFile) }
      into("share/zsh/site-functions") { from(completionFile).rename { "_${project.name}" } }
    }
  }
}

application {
  mainClass = mainClassName
}


tasks.distZip { dependsOn(taskGenAutoComplete) }
tasks.distTar { dependsOn(taskGenAutoComplete) }
