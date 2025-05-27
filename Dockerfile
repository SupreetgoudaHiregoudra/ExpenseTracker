# Dockerfile
FROM openjdk:17

WORKDIR /app

COPY . .

RUN javac ExpenseTracker.java

CMD ["java", "ExpenseTracker"]
