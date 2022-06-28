val springVersion: String by project
val starterAwsVersion: String by project
val starterLoadBalancerVersion: String by project
val starterSleuthVersion: String by project
val starterStreamVersion: String by project
val jacksonVersion: String by project
val reactorKotlinVersion: String by project
val kotlinxVersion: String by project
val liquibaseVersion: String by project
val mapstructVersion: String by project
val springJdbcVersion: String by project
val openapiVersion: String by project
val r2dbcPostgresqlVersion: String by project
val postgresqlVersion: String by project
val reactorTestVersion: String by project
val blockhoundVersion: String by project
val springCloudVersion: String by project
val prometheusVersion: String by project
val opentelemetryVersion: String by project
val springCloudOtelVersion: String by project

plugins {
    id("org.springframework.boot") version "2.7.0"
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
    maven("https://repo.spring.io/snapshot")
    maven("https://repo.spring.io/milestone")
    mavenCentral()
}

kapt {
    arguments {
        arg("mapstruct.defaultComponentModel", "spring")
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator:$springVersion")
    implementation("org.springframework.boot:spring-boot-starter-data-elasticsearch:$springVersion")
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc:$springVersion")
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive:$springVersion")
    implementation("org.springframework.boot:spring-boot-starter-security:$springVersion")
    implementation("org.springframework.boot:spring-boot-starter-validation:$springVersion")
    implementation("org.springframework.boot:spring-boot-starter-webflux:$springVersion")
    implementation("org.springframework.cloud:spring-cloud-starter-aws:$starterAwsVersion")
    implementation("org.springframework.cloud:spring-cloud-starter-loadbalancer:$starterLoadBalancerVersion")
    implementation("org.springframework.cloud:spring-cloud-starter-sleuth:$starterSleuthVersion") {
        configurations {
            all {
                exclude("org.springframework.cloud", "spring-cloud-sleuth-brave")
                exclude("io.zipkin.brave")
            }
        }
    }
    implementation("org.springframework.cloud:spring-cloud-sleuth-otel-autoconfigure")
    implementation("org.springframework.cloud:spring-cloud-starter-stream-kafka:$starterStreamVersion")
    implementation("org.springframework.cloud:spring-cloud-stream-binder-kafka-streams:$starterStreamVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions:$reactorKotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:$kotlinxVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$kotlinxVersion")
    implementation("org.liquibase:liquibase-core:$liquibaseVersion")
    implementation("org.mapstruct:mapstruct:$mapstructVersion")
    implementation("org.springframework:spring-jdbc:$springJdbcVersion")
    implementation("org.springdoc:springdoc-openapi-webflux-ui:$openapiVersion")
    implementation("io.r2dbc:r2dbc-postgresql:$r2dbcPostgresqlVersion")
    implementation("io.opentelemetry:opentelemetry-extension-kotlin:$opentelemetryVersion")
    implementation("io.opentelemetry:opentelemetry-extension-trace-propagators:$opentelemetryVersion")
    implementation("io.opentelemetry:opentelemetry-exporter-jaeger:$opentelemetryVersion")
    implementation("io.opentelemetry:opentelemetry-exporter-otlp-common:$opentelemetryVersion")
    developmentOnly("org.springframework.boot:spring-boot-devtools:$springVersion")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus:$prometheusVersion")
    runtimeOnly("org.postgresql:postgresql:$postgresqlVersion")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor:$springVersion")
    annotationProcessor("org.mapstruct:mapstruct-processor:$mapstructVersion")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-debug:$kotlinxVersion")
    testImplementation("org.springframework.boot:spring-boot-starter-test:$springVersion")
    testImplementation("io.projectreactor:reactor-test:$reactorTestVersion")
    testImplementation("io.projectreactor.tools:blockhound:$blockhoundVersion")
    kapt("org.mapstruct:mapstruct-processor:$mapstructVersion")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:$springCloudVersion")
        mavenBom("org.springframework.cloud:spring-cloud-sleuth-otel-dependencies:$springCloudOtelVersion")
    }
}

tasks.compileKotlin {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict", "-Xjvm-default=all")
        jvmTarget = "17"
    }
}

tasks.test {
    jvmArgs = listOf("-XX:+AllowRedefinitionToAddDeleteMethods")
    useJUnitPlatform()
}

tasks.processResources {
    filesMatching("application.yaml") {
        expand(project.properties)
    }
}
