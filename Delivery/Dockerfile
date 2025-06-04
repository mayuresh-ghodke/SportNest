# Use a minimal Java image
FROM eclipse-temurin:17

# Set the working directory inside the container
WORKDIR /app

# Copy the JAR file into the container
COPY target/Delivery-0.0.1-SNAPSHOT.jar delivery-app.jar

# Expose the application port (optional for documentation, actual binding is in docker-compose)
EXPOSE 8021

# Define the entry point to run the Spring Boot application
ENTRYPOINT ["java", "-jar", "delivery-app.jar"]
