# Use official OpenJDK image
FROM eclipse-temurin:17

# Set working directory inside the container
WORKDIR /app

# Copy the JAR file to the container
COPY target/Customer-0.0.1-SNAPSHOT.jar customer-app.jar

# Expose the port (same as in .env -> ADMIN_PORT=8019)
EXPOSE 8020

# Run the application
ENTRYPOINT ["java", "-jar", "customer-app.jar"]
