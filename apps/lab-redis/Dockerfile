FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy prebuilt Spring Boot jar (built on host via ./gradlew :apps:lab-redis:bootJar)
COPY apps/lab-redis/build/libs/lab-redis-1.0-SNAPSHOT.jar /app/app.jar

# Copy extracted Pinpoint agent (host: docker/pinpoint/pinpoint-agent/)
COPY docker/pinpoint/pinpoint-agent/ /pinpoint-agent/

EXPOSE 8080

ENTRYPOINT ["java", \
  "-Xmx2g", \
  "-javaagent:/pinpoint-agent/pinpoint-bootstrap.jar", \
  "-Dpinpoint.agentId=lab-redis", \
  "-Dpinpoint.applicationName=lab-redis", \
  "-Dpinpoint.profiler.profiles.active=release", \
  "-Dprofiler.transport.grpc.collector.ip=host.docker.internal", \
  "-jar", "/app/app.jar"]
