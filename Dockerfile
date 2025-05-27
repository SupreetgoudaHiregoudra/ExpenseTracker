# Use OpenJDK base image
FROM openjdk:17

# Set working directory
WORKDIR /app

# Copy project files into container
COPY . .

# Compile the Java Swing program
RUN javac ExpenseTracker.java

# Run the Java application
CMD ["java", "ExpenseTracker"]
