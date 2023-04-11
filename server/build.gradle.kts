import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestLogEvent.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm") version "1.8.10"
  kotlin("kapt") version "1.8.10"
  application
  id("com.github.johnrengelman.shadow") version "8.1.1"
  id("com.google.devtools.ksp") version "1.8.10-1.0.9"
}

group = "cn.enaium"
version = "1.0.0-SNAPSHOT"

repositories {
  mavenCentral()
}

val vertxVersion = "4.4.1"
val junitJupiterVersion = "5.9.1"
val jimmer = "0.7.22"
val jackson = "2.14.2"
val mariadb = "3.1.2"

val mainVerticleName = "cn.enaium.todo.App"
val launcherClassName = "io.vertx.core.Launcher"

val watchForChange = "src/**/*"
val doOnChange = "${projectDir}/gradlew classes"

application {
  mainClass.set(launcherClassName)
}

dependencies {
  implementation("org.babyfish.jimmer:jimmer-sql-kotlin:$jimmer")
  ksp("org.babyfish.jimmer:jimmer-ksp:$jimmer")
  implementation("org.mapstruct:mapstruct:1.5.3.Final")
  kapt("org.mapstruct:mapstruct-processor:1.5.3.Final")
  kapt("org.babyfish.jimmer:jimmer-mapstruct-apt:${jimmer}")

  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jackson")
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jackson")
  implementation("org.mariadb.jdbc:mariadb-java-client:$mariadb")

  implementation(platform("io.vertx:vertx-stack-depchain:$vertxVersion"))
  implementation("io.vertx:vertx-core")
  implementation("io.vertx:vertx-web")
  implementation("io.vertx:vertx-lang-kotlin-coroutines")
  implementation("io.vertx:vertx-lang-kotlin")
  testImplementation("io.vertx:vertx-junit5")
  testImplementation("org.junit.jupiter:junit-jupiter:$junitJupiterVersion")
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions.jvmTarget = "17"

tasks.withType<ShadowJar> {
  archiveClassifier.set("fat")
  manifest {
    attributes(mapOf("Main-Verticle" to mainVerticleName))
  }
  mergeServiceFiles()
}

tasks.withType<Test> {
  useJUnitPlatform()
  testLogging {
    events = setOf(PASSED, SKIPPED, FAILED)
  }
}

tasks.withType<JavaExec> {
  args = listOf(
    "run",
    mainVerticleName,
    "--redeploy=$watchForChange",
    "--launcher-class=$launcherClassName",
    "--on-redeploy=$doOnChange"
  )
}

kotlin {
  sourceSets.main {
    kotlin.srcDir("build/generated/ksp/main/kotlin")
  }
}
