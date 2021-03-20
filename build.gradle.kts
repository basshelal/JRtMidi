import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    `java-library`
    `maven-publish`
    kotlin("jvm") version "1.4.30"
}

group = "dev.basshelal"
version = "1.0"

val javaVersion = JavaVersion.VERSION_1_8

java {
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.github.jnr:jnr-ffi:2.2.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:4.4.3")
    testImplementation("io.kotest:kotest-assertions-core-jvm:4.4.3")
    testImplementation("io.kotest:kotest-property-jvm:4.4.3")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.0")
}
