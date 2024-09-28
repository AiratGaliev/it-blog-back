# Use the official Maven image to create a build artifact.
# This image will have both JDK and Maven installed.
FROM maven:3.9.7-eclipse-temurin-21-alpine AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the pom.xml file and download the dependencies
COPY pom.xml .

# Download all dependencies
RUN mvn dependency:go-offline -B

# Copy the project files
COPY src ./src

# Package the application
RUN mvn clean package -DskipTests

# Second stage: Create the final image
FROM eclipse-temurin:21.0.3_9-jre-ubi9-minimal AS app

# Set the working directory inside the container
WORKDIR /app

# Copy the JAR file from the first stage
COPY --from=build /app/target/*.jar app.jar

# Create necessary directories
RUN mkdir -p /app/users/avatars && mkdir -p /app/articles/images && mkdir -p /app/categories/images

# Expose the port that your application runs on
EXPOSE 9000

# Command to run the JAR file
ENTRYPOINT ["java", "--enable-native-access=ALL-UNNAMED", "-jar", "app.jar"]