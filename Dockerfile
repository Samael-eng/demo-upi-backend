FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY demo-upi-backend/ .

RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

EXPOSE 8080

CMD ["java", "-jar", "target/demo-upi-backend-0.0.1-SNAPSHOT.jar"]