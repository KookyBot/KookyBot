import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.0"
    application
}

group = "com.github.zly2006"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.java-websocket","Java-WebSocket","1.5.3")
    implementation("com.google.code.gson","gson","2.8.9")
    implementation("org.jetbrains.kotlinx","kotlinx-coroutines-core","1.6.3")
    implementation("com.fasterxml.jackson.core", "jackson-databind", "2.9.6")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("MainKt")
}