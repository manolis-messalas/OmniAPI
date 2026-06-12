FROM eclipse-temurin:23-jdk

ARG JAR_FILE=target/OmniAPI-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} /app.jar

EXPOSE 9090
ENTRYPOINT ["java","-jar","/app.jar"]

