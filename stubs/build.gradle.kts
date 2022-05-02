import com.google.protobuf.gradle.*
import org.gradle.api.file.DuplicatesStrategy.EXCLUDE

plugins {
    id("com.google.protobuf")
    kotlin("jvm")
}

java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

dependencies {
    protobuf(project(":protos"))

    api("org.jetbrains.kotlin:kotlin-stdlib:1.6.21")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1-native-mt")
    api("io.grpc:grpc-googleapis:1.46.0")
    api("io.grpc:grpc-stub:1.46.0")
    api("io.grpc:grpc-protobuf:1.46.0")
    api("com.google.protobuf:protobuf-java-util:3.20.1")
    api("com.google.protobuf:protobuf-kotlin:3.20.1")
    api("io.grpc:grpc-kotlin-stub:1.2.1")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.19.4"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.46.0"
        }
        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:1.2.1:jdk7@jar"
        }
    }
    generateProtoTasks {
        all().forEach {
            it.plugins {
                id("grpc")
                id("grpckt")
            }
            it.builtins {
                id("kotlin")
            }
        }
    }
}

tasks.compileKotlin {
    kotlinOptions {
        freeCompilerArgs = listOf("-opt-in=kotlin.RequiresOptIn")
    }
}

tasks.withType(Jar::class) {
    duplicatesStrategy = EXCLUDE
}