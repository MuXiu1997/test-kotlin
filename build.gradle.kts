import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.20"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.apache.commons:commons-lang3:3.12.0")
    compileOnly("javax.validation:validation-api:2.0.1.Final")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
    implementation("cn.hutool:hutool-all:5.7.18")
    compileOnly("org.springframework:spring-core:5.3.6")
    compileOnly("org.springframework:spring-context:5.3.6")
}


tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
