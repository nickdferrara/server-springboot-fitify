FROM gradle:8.11-jdk21-alpine AS builder

WORKDIR /app
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle
RUN gradle dependencies --no-daemon || true

COPY src ./src
RUN gradle bootJar --no-daemon -x test

FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S fitify && adduser -S fitify -G fitify

WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar

RUN chown -R fitify:fitify /app
USER fitify

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
