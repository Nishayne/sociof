# Build a JAR File
FROM maven:3.8.3-openjdk-17-slim AS stage1
WORKDIR /home/app
COPY pom.xml /home/app
COPY src /home/app/src
RUN mvn clean package -DskipTests -f /home/app/pom.xml 
RUN ls -l /home/app/target
# Create an Image
FROM openjdk:17-jdk-alpine
WORKDIR /home/app
# Use a non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser
EXPOSE 8080
COPY --from=stage1 /home/app/target/huSpark-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["sh", "-c", "java -jar ./app.jar"]
#CMD ["java", "-jar", "app.jar"]