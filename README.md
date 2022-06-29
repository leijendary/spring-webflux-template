# Spring Boot WebFlux Template for Microservices

- This template is intended for the microservice architecture
- Kafka is included in this template
- Sample classes are included
- **This template uses annotation based routing**
- **Intended for personal use only as this does not include complete features like JHipster**

# Technologies Used:

- Kotlin Coroutines
- Spring WebFlux
- Spring Actuator
- Spring R2DBC
- Spring WebClient
- Spring Security
- Spring Cloud Loadbalancer
- Spring Data Reactive Redis
- Spring Data Elasticsearch
- Spring Cloud Stream Binder Kafka
- Spring Cloud Stream Binder Kafka Streams
- Spring Cloud AWS
- Spring Cloud Sleuth
- Spring Cloud OpenTelemetry
- Spring Configuration Processor
- Spring Autoconfigure Processor
- Spring Devtools
- Spring Validation
- PostgreSQL
- Liquibase
- MapStruct
- Caffeine
- Docker
- JUnit
- Kubernetes
- Reactor Test
- BlockHound
- OpenAPI
- Prometheus
- OpenTelemetry

# Spring Microservice WebFlux

### To run the code:

`./gradlew bootRun`

### To run tests:

`./gradlew test`

### To build a JAR file:

`./gradlew build -x test`

### To generate a certificate:

`keytool -genkeypair -alias spring-boot -keyalg RSA -keysize 2048 -validity 3650 -keypass spring-boot -storetype PKCS12 -keystore keystore.p12`
