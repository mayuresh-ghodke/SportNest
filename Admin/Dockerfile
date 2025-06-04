# Use official OpenJDK image
FROM eclipse-temurin:17

# Set working directory inside the container
WORKDIR /app

# Copy the JAR file to the container
COPY target/Admin-0.0.1-SNAPSHOT.jar admin-app.jar

# Expose the port (same as in .env -> ADMIN_PORT=8019)
EXPOSE 8019

# Run the application
ENTRYPOINT ["java", "-jar", "admin-app.jar"]
