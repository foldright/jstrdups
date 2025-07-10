import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget


plugins {
  val kotlinVersion = "2.2.0"
  kotlin("jvm") version kotlinVersion
  kotlin("kapt") version kotlinVersion

  application
}

group = "io.foldright"
version = "0.2.0-SNAPSHOT"

repositories.mavenCentral()

dependencies {
  implementation("com.github.javaparser:javaparser-core:3.27.0")

  val picocliVersion = "4.7.7"
  implementation("info.picocli:picocli:$picocliVersion")
  kapt("info.picocli:picocli-codegen:$picocliVersion")

  testImplementation(kotlin("test"))

  val kotestVersion = "5.9.1"
  testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
  testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
  testImplementation("io.kotest:kotest-property:$kotestVersion")

  compileOnly("org.jetbrains:annotations:26.0.2")
}
configurations.runtimeClasspath {
  exclude("org.jetbrains", "annotations")
}

java.sourceCompatibility = JavaVersion.VERSION_1_8
// https://kotlinlang.org/docs/gradle-compiler-options.html#centralize-compiler-options-and-use-types
kotlin.compilerOptions.jvmTarget = JvmTarget.JVM_1_8

tasks.jar { exclude("META-INF/native-image") }

tasks.test {
  useJUnitPlatform()
  testLogging.exceptionFormat = TestExceptionFormat.FULL
}


val mainClassName = "io.foldright.dslf.DuplicateStringLiteralFinder"
val buildDir: File = layout.buildDirectory.get().asFile

/**
 * https://picocli.info/autocomplete.html#_generating_completion_scripts_during_the_build
 */
val genAutoComplete by tasks.registering(JavaExec::class) {
  classpath = sourceSets.main.get().runtimeClasspath
  workingDir = buildDir
  mainClass = "picocli.AutoComplete"
  args = listOf(mainClassName)
}
tasks.distZip { dependsOn(genAutoComplete) }
tasks.distTar { dependsOn(genAutoComplete) }

distributions.main {
  val completionFile: File = buildDir.resolve("${project.name}_completion")
  contents {
    into("etc/bash_completion.d") { from(completionFile) }
    into("share/zsh/site-functions") { from(completionFile).rename { "_${project.name}" } }
  }
}

application.mainClass = mainClassName
