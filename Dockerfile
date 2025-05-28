FROM openjdk:17
COPY target/expense-tracker.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]


