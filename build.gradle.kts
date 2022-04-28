plugins {
    id("org.springframework.boot") version "2.6.7"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("org.jetbrains.kotlin.plugin.noarg") version "1.6.21"
    kotlin("jvm") version "1.6.21"
    kotlin("kapt") version "1.6.21"
    kotlin("plugin.spring") version "1.6.21"
}

group = "com.leijendary.spring"
version = "0.0.1-SNAPSHOT"
description = "Spring WebFlux Template for the Microservice Architecture or general purpose"
java.sourceCompatibility = JavaVersion.VERSION_17

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

kapt {
    arguments {
        arg("mapstruct.defaultComponentModel", "spring")
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator:2.6.7")
    implementation("org.springframework.boot:spring-boot-starter-aop:2.6.7")
    implementation("org.springframework.boot:spring-boot-starter-data-elasticsearch:2.6.7")
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc:2.6.7")
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive:2.6.7")
    implementation("org.springframework.boot:spring-boot-starter-validation:2.6.7")
    implementation("org.springframework.boot:spring-boot-starter-webflux:2.6.7")
    implementation("org.springframework.cloud:spring-cloud-starter-aws:2.2.6.RELEASE")
    implementation("org.springframework.cloud:spring-cloud-starter-loadbalancer:3.1.1")
    implementation("org.springframework.cloud:spring-cloud-starter-stream-kafka:3.2.2")
    implementation("org.springframework.cloud:spring-cloud-stream-binder-kafka-streams:3.2.2")
    implementation("net.devh:grpc-spring-boot-starter:2.13.1.RELEASE")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.13.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.2")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions:1.1.6")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1-native-mt")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.6.1-native-mt")
    implementation("org.liquibase:liquibase-core:4.9.1")
    implementation("org.mapstruct:mapstruct:1.4.2.Final")
    implementation("org.springframework:spring-jdbc:5.3.19")
    implementation("com.github.ben-manes.caffeine:caffeine:3.0.6")
    developmentOnly("org.springframework.boot:spring-boot-devtools:2.6.7")
    kapt("org.mapstruct:mapstruct-processor:1.4.2.Final")
    runtimeOnly("io.r2dbc:r2dbc-postgresql:0.8.12.RELEASE")
    runtimeOnly("org.postgresql:postgresql:42.3.4")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor:2.6.7")
    testImplementation("org.springframework.boot:spring-boot-starter-test:2.6.7")
    testImplementation("io.projectreactor:reactor-test:3.4.17")
}

tasks.compileKotlin {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict", "-Xjvm-default=all")
        jvmTarget = "17"
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.processResources {
    filesMatching("application.yaml") {
        expand(project.properties)
    }
}
