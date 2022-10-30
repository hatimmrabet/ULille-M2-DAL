FROM openjdk:17-alpine
COPY target/banque-0.0.1-SNAPSHOT.jar /app/banque-0.0.1-SNAPSHOT.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/banque-0.0.1-SNAPSHOT.jar"]
