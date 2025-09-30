import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget


plugins {
  val kotlinVersion = "2.2.20"
  kotlin("jvm") version kotlinVersion
  kotlin("kapt") version kotlinVersion

  application

  id("org.asciidoctor.jvm.convert") version "4.0.5"
}

group = "io.foldright"
version = "0.3.0-SNAPSHOT"

repositories.mavenCentral()

dependencies {
  implementation("com.github.javaparser:javaparser-core:3.27.0")

  val picocliVersion = "4.7.7"
  implementation("info.picocli:picocli:$picocliVersion")
  kapt("info.picocli:picocli-codegen:$picocliVersion")

  testImplementation(platform("org.junit:junit-bom:6.0.0"))
  // In order to run JUnit 5 test cases in IntelliJ IDEA, need include this dependency. more info see:
  // https://junit.org/junit5/docs/current/user-guide/#running-tests-ide-intellij-idea
  // https://github.com/junit-team/junit5-samples/blob/main/junit5-jupiter-starter-maven/pom.xml#L29
  testImplementation("org.junit.jupiter:junit-jupiter")
  val kotestVersion = "5.9.1"
  testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
  testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
  testImplementation("io.kotest:kotest-property:$kotestVersion")

  compileOnly("org.jetbrains:annotations:26.0.2-1")
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

// https://picocli.info/autocomplete.html#_generating_completion_scripts_during_the_build
val genCliAutoComplete by tasks.registering(JavaExec::class) {
  classpath = sourceSets.main.get().runtimeClasspath
  workingDir = buildDir
  mainClass = "picocli.AutoComplete"
  args(mainClassName, "--force")
}
val generatedPicocliDocsDir = "${buildDir}/generated-picocli-docs"
// https://github.com/remkop/picocli/tree/v4.7.7/picocli-examples
// https://picocli.info/man/gen-manpage.html
val genManpageAsciiDoc by tasks.registering(JavaExec::class) {
  dependsOn(tasks.classes)
  group = "Documentation"
  description = "Generate AsciiDoc manpage"
  classpath(sourceSets.main.get().runtimeClasspath, configurations.kapt)
  mainClass = "picocli.codegen.docgen.manpage.ManPageGenerator"
  args(mainClassName, "--outdir=$generatedPicocliDocsDir", "-v", "--force")
  // "--template-dir=src/docs/mantemplates"
}
tasks.asciidoctor {
  dependsOn(genManpageAsciiDoc)
  setSourceDir(generatedPicocliDocsDir)
  setOutputDir("${buildDir}/docs")
  logDocuments = true
  outputOptions { backends("manpage", "html5") }
  jvm {
    if (!JavaVersion.current().isJava9Compatible) return@jvm
    jvmArgs("--add-opens", "java.base/sun.nio.ch=ALL-UNNAMED", "--add-opens", "java.base/java.io=ALL-UNNAMED")
  }
}
arrayOf(tasks.assemble, tasks.distZip, tasks.distTar).forEach {
  it { dependsOn(genCliAutoComplete, tasks.asciidoctor) }
}


distributions.main {
  val completionFile: File = buildDir.resolve("${project.name}_completion")
  contents {
    into("etc/bash_completion.d") { from(completionFile) }
    into("share/zsh/site-functions") { from(completionFile).rename { "_${project.name}" } }
    into("share/man/man1") { from("$buildDir/docs/manpage") }
    into("share/doc/${project.name}/html") { from("$buildDir/docs/html5") }
  }
}

application.mainClass = mainClassName
