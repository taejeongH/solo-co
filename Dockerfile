# 1단계: build
FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /build

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# 2단계: run
FROM eclipse-temurin:17-jdk
WORKDIR /

COPY --from=builder /build/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]
