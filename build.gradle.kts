import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.palantir.gradle.graal.GraalExtension

plugins {
    kotlin("jvm") version "1.4.0"
    application
    id("com.github.johnrengelman.shadow") version "5.2.0"
    id("com.palantir.graal") version "0.6.0"
}

application {
    mainClassName = "korps.MainKt"
}

tasks.withType<ShadowJar> {
    archiveBaseName.set("korps")
}

configure<GraalExtension> {
    mainClass(application.mainClassName)
    outputName("korps")
    graalVersion("19.2.1")
//    option("--report-unsupported-elements-at-runtime")
    option("--no-fallback")
    option("--allow-incomplete-classpath")
    option("-H:ReflectionConfigurationFiles=reflection-config.json")
//    option("--no-server")
    option("--initialize-at-build-time=org.slf4j.LoggerFactory,org.slf4j.helpers.SubstituteLoggerFactory,org.slf4j.helpers.NOPLoggerFactory")
}


repositories {
    mavenCentral()
}

dependencies {
    implementation("io.vertx:vertx-web:3.9.2")
    implementation("io.vertx:vertx-lang-kotlin-coroutines:3.9.2")
    implementation("io.vertx:vertx-lang-kotlin:3.9.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.11.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.11.2")

//    compileOnly("com.oracle.substratevm:svm:19.2.0")
//    implementation("org.apache.logging.log4j:log4j-core:2.8.2")
//    implementation("org.apache.logging.log4j:log4j-api:2.8.2")
//    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.8.2")
//    implementation("org.slf4j:slf4j-api:1.7.21")

}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}

