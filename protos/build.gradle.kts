plugins {
    `java-library`
}

java {
    sourceCompatibility = JavaVersion.VERSION_17

    sourceSets.getByName("main").resources.srcDir("src/main/proto")
}