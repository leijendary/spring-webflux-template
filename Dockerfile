FROM azul/zulu-openjdk-alpine:19-latest as build
WORKDIR /workspace/app
COPY src src
COPY gradle gradle
COPY build.gradle.kts .
COPY gradlew .
COPY settings.gradle.kts .
RUN --mount=type=cache,target=/root/.m2 ./gradlew build --console plain -x test

FROM azul/zulu-openjdk-alpine:19-jre-latest
VOLUME /app
COPY --from=build /workspace/app/build/libs/*.jar application.jar
ENTRYPOINT ["java", "--enable-preview", "-jar", "application.jar"]
