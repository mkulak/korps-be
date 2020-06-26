plugins {
    kotlin("jvm") version "1.3.72"
    application
    id("com.github.johnrengelman.shadow") version "5.2.0"
    id("com.palantir.graal") version "0.6.0"
}

application {
    mainClassName = "com.palantir.test.Main"
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    archiveBaseName.set("korps")
}

configure<com.palantir.gradle.graal.GraalExtension> {
    mainClass(application.mainClassName)
    outputName("korps")
//    option("--report-unsupported-elements-at-runtime")
//    option("--initialize-at-build-time")
//    option("--no-fallback")
//    option("--no-server")
}


repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("io.vertx:vertx-web:3.9.1")
    implementation("io.vertx:vertx-lang-kotlin-coroutines:3.9.1")
    implementation("io.vertx:vertx-lang-kotlin:3.9.1")

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

