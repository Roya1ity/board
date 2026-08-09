FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle

RUN chmod +x gradlew && ./gradlew --no-daemon dependencies > /dev/null 2>&1 || true

COPY src ./src

RUN ./gradlew --no-daemon clean bootJar -x test

###############################################################

FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system spring \
    && useradd --system --gid spring --home-dir /app spring \
    && mkdir -p /app/uploads

COPY --from=build /workspace/build/libs/*.jar app.jar

RUN chown -R spring:spring /app

USER spring

ENV APP_UPLOAD_DIR=/app/uploads

EXPOSE 8099

ENTRYPOINT ["java","-jar","/app/app.jar"]