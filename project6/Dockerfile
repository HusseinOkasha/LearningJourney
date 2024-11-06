# Use an official Java runtime as a parent image
FROM openjdk:17-jdk-slim

# Set the working directory
WORKDIR /app


# Copy the JAR file into the container
COPY target/project6-0.0.1-SNAPSHOT.jar app.jar

# Expose the port your Spring app runs on
EXPOSE 8080

# Run the Spring app
ENTRYPOINT ["java", "-jar", "app.jar"]