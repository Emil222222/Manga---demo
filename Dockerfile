FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon

COPY src src
RUN ./gradlew bootJar --no-daemon -x test

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S manga && adduser -S manga -G manga

COPY --from=builder /app/build/libs/*.jar app.jar

RUN mkdir -p /app/content /app/logs && \
    chown -R manga:manga /app

USER manga

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]