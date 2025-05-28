FROM openjdk:17
COPY target/*.jar ExpenseTracker.jar
CMD ["java", "-jar", "ExpenseTracker.jar"]
