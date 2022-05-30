FROM gradle:7-jdk17 as build
WORKDIR /workspace/app
COPY src src
COPY build.gradle.kts .
COPY gradle.properties .
COPY settings.gradle.kts .
RUN --mount=type=cache,target=/root/.m2 gradle bootJar -x test
RUN mkdir -p build/dependency && (cd build/dependency; jar -xf ../libs/*.jar)

FROM eclipse-temurin:17-jre-jammy
VOLUME /app
ARG DEPENDENCY=/workspace/app/build/dependency
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app
ENTRYPOINT ["java", "-cp", "app:app/lib/*", "com.leijendary.spring.webflux.template.ApplicationKt"]
