buildscript {
    dependencies {
        classpath("com.github.jengelman.gradle.plugins:shadow:5.2.0")
    }
}

plugins {
    kotlin("jvm") version "1.4.10"
    application
    id("com.github.johnrengelman.shadow") version "4.0.1"
}

repositories {
    jcenter()
    maven("https://jitpack.io")
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("com.google.cloud.functions:functions-framework-api:1.0.1")
    implementation("com.google.cloud:google-cloud-firestore:1.32.0")

    implementation("io.ktor:ktor-client-core:1.4.1")
    implementation("io.ktor:ktor-client-cio:1.4.1")
    implementation("io.ktor:ktor-client-json:1.4.1")
    implementation("io.ktor:ktor-client-gson:1.4.1")

    implementation("io.github.kotlin-telegram-bot.kotlin-telegram-bot:telegram:5.0.0")

    implementation("org.slf4j:slf4j-api:1.7.30")
    implementation("ch.qos.logback:logback-classic:1.2.3")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

application {
    mainClassName = "ch.homegate.crawler.MainKt"
}

tasks.shadowJar {
    mergeServiceFiles()
}
