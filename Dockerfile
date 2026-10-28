FROM node:22-alpine AS frontend
WORKDIR /workspace
RUN apk add --no-cache bash
COPY package.json package-lock.json ./
RUN npm ci --no-audit --no-fund
COPY scripts/build-frontend.mjs scripts/build-frontend.mjs
RUN npm run build

FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace
COPY gradle gradle
COPY gradlew build.gradle.kts settings.gradle.kts gradle.properties ./
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies
COPY src src
COPY --from=frontend /workspace/src/main/resources/static src/main/resources/static
RUN ./gradlew --no-daemon bootJar

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY --from=build /workspace/build/libs/app.jar app.jar
USER app
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=65.0 -XX:+UseSerialGC -Dfile.encoding=UTF-8"
EXPOSE 10000
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
