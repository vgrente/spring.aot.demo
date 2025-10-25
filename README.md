# Spring Boot AOT Demo

[![Build Native Image](https://github.com/vgrente/spring.aot.demo/actions/workflows/build-native.yml/badge.svg?branch=main)](https://github.com/vgrente/spring.aot.demo/actions/workflows/build-native.yml)
[![Java](https://img.shields.io/badge/Java-25-orange?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.0--SNAPSHOT-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.6+-blue?logo=apachemaven)](https://maven.apache.org/)
[![GitHub Container Registry](https://img.shields.io/badge/GHCR-spring--aot--demo-blue?logo=docker)](https://github.com/vgrente/spring.aot.demo/pkgs/container/spring-aot-demo)

A demonstration project showcasing Spring Boot 4.0 with Ahead-of-Time (AOT) compilation and JDK 25, optimized for faster startup times and reduced memory footprint.

## Overview

This project demonstrates modern Spring Boot application development with:
- Spring Boot 4.0 (SNAPSHOT)
- JDK 25
- Spring AOT (Ahead-of-Time) compilation
- Container optimization with Paketo Buildpacks
- RESTful API with JPA/Hibernate
- In-memory H2 database
- Spring Boot Actuator for monitoring

## Features

### AOT Compilation
The application uses Spring's AOT processing to pre-compute application configuration and generate optimized code at build time, resulting in:
- Faster startup times
- Reduced memory consumption
- Smaller runtime footprint
- Better performance in containerized environments

### AOT Cache (JDK 25 Feature)
JDK 25 introduces AOT caching capabilities that can further improve startup performance. The AOT cache stores optimized code from a training run, which can then be reused in subsequent application starts.

### Product Management API
A complete CRUD REST API for managing products with the following endpoints:
- `GET /api/products` - List all products
- `GET /api/products/{id}` - Get product by ID
- `POST /api/products` - Create new product
- `PUT /api/products/{id}` - Update product
- `DELETE /api/products/{id}` - Delete product
- `GET /api/products/search?name={name}` - Search products by name

### Health Monitoring
Spring Boot Actuator endpoints for application health and metrics:
- `/actuator/health` - Application health status
- `/actuator/info` - Application information
- `/actuator/metrics` - Application metrics
- `/actuator/prometheus` - Prometheus metrics endpoint

## Prerequisites

- JDK 25 (required for this project)
- Maven 3.6+
- Docker (optional, for containerized deployment)

## Project Structure

```
spring.aot.demo/
├── src/
│   ├── main/
│   │   ├── java/io/vgrente/spring/aot/demo/
│   │   │   ├── SpringAotDemoApplication.java
│   │   │   ├── config/
│   │   │   │   └── DataInitializer.java
│   │   │   ├── controller/
│   │   │   │   └── ProductController.java
│   │   │   ├── model/
│   │   │   │   └── Product.java
│   │   │   └── repository/
│   │   │       └── ProductRepository.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/io/vgrente/spring/aot/demo/
│           ├── ApplicationTests.java
│           └── controller/
│               └── ProductControllerTests.java
├── pom.xml
└── docker-compose.yaml
```

## Building the Application

### Standard Build (without AOT)
```bash
mvn clean package
```

### Build with AOT Processing
```bash
mvn clean package -Paot
```

### Build Container Image
```bash
mvn spring-boot:build-image
```

This creates a Docker image using Paketo Buildpacks with:
- Bellsoft Liberica JRE 25
- Layered JAR structure for better caching
- Class Data Sharing (CDS) enabled
- Optimized JVM settings

## Running the Application

### Run Locally
```bash
mvn spring-boot:run
```

### Run in Docker
```bash
docker run -p 8080:8080 spring-aot-demo:0.0.1-SNAPSHOT
```

### Run with Docker Compose
```bash
docker-compose up
```

## Using AOT Cache (JDK 25)

JDK 25 introduces an AOT cache feature that can significantly improve startup performance by storing optimized code from a training run. This cache is then reused in subsequent application starts.

### Creating an AOT Cache

To use the AOT cache feature, you need to perform a training run on your application in extracted form:

#### Step 1: Extract the Application JAR
```bash
java -Djarmode=tools -jar target/spring.aot.demo-0.0.1-SNAPSHOT.jar extract --destination application
```

This extracts the Spring Boot JAR into a directory structure that allows for better layer caching and AOT optimization.

#### Step 2: Run Training to Generate AOT Cache
```bash
cd application
java -XX:AOTCacheOutput=app.aot -Dspring.context.exit=onRefresh -jar spring.aot.demo-0.0.1-SNAPSHOT.jar
```

This command:
- `-XX:AOTCacheOutput=app.aot` - Specifies the output file for the AOT cache
- `-Dspring.context.exit=onRefresh` - Tells Spring to exit after context refresh (training run)
- Creates an `app.aot` cache file in the current directory

#### Step 3: Run with AOT Cache
```bash
java -XX:AOTCache=app.aot -jar spring.aot.demo-0.0.1-SNAPSHOT.jar
```

The application will now start faster by reusing the optimized code from the AOT cache.

### AOT Cache Benefits

- **Faster Startup**: Pre-compiled code loads much faster than JIT compilation
- **Reduced Warm-up Time**: Application reaches peak performance immediately
- **Lower CPU Usage**: Less compilation work needed at startup
- **Consistent Performance**: Eliminates JIT compilation variations

### AOT Cache Best Practices

1. **Regenerate Cache After Changes**: When you update dependencies or code, regenerate the AOT cache
2. **Version Control**: Consider including the cache file in deployments but not in source control
3. **Environment-Specific**: Generate cache files for specific deployment environments
4. **Container Integration**: Can be integrated into Docker images for even faster container startup

### Example: Full Workflow

```bash
# Build the application with AOT
mvn clean package -Paot

# Extract the JAR
java -Djarmode=tools -jar target/spring.aot.demo-0.0.1-SNAPSHOT.jar extract --destination application

# Generate AOT cache
cd application
java -XX:AOTCacheOutput=app.aot -Dspring.context.exit=onRefresh -jar spring.aot.demo-0.0.1-SNAPSHOT.jar

# Run with AOT cache
java -XX:AOTCache=app.aot -jar spring.aot.demo-0.0.1-SNAPSHOT.jar

# Test the application
curl http://localhost:8080/actuator/health
```

## Testing the API

### Using curl

List all products:
```bash
curl http://localhost:8080/api/products
```

Get product by ID:
```bash
curl http://localhost:8080/api/products/1
```

Create a new product:
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{"name":"New Product","price":29.99,"description":"A new product"}'
```

Update a product:
```bash
curl -X PUT http://localhost:8080/api/products/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"Updated Product","price":39.99,"description":"Updated description"}'
```

Delete a product:
```bash
curl -X DELETE http://localhost:8080/api/products/1
```

Search products:
```bash
curl http://localhost:8080/api/products/search?name=laptop
```

Check application health:
```bash
curl http://localhost:8080/actuator/health
```

## Configuration

### Application Properties
Key configuration options in `src/main/resources/application.properties`:

```properties
# Server
server.port=8080

# Database (H2 in-memory)
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.hibernate.ddl-auto=create-drop

# AOT
spring.aot.enabled=true

# Actuator
management.endpoints.web.exposure.include=health,info,metrics,prometheus
```

### AOT Configuration
The AOT processing is configured in `pom.xml`:
- Enabled via the `aot` profile
- Integrated with Spring Boot Maven Plugin
- Uses GraalVM native build tools for metadata

### Container Optimization
The Docker image is configured with:
- JDK 25 with JRE type (smaller footprint)
- Class Data Sharing (CDS) enabled
- Layered JAR structure
- Optimized memory settings (75% RAM percentage)
- Health checks configured

## Development

### Running Tests
```bash
mvn test
```

### Verify Build
```bash
mvn verify
```

### Clean Build Artifacts
```bash
mvn clean
```

### H2 Console
When running locally, access the H2 database console at:
```
http://localhost:8080/h2-console
```

Connection details:
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: (empty)

## AOT Benefits

This project demonstrates the benefits of Spring AOT:

1. **Faster Startup**: Pre-computed configuration reduces startup time
2. **Lower Memory**: Optimized code generation reduces runtime memory
3. **Better Container Performance**: Smaller images with faster cold starts
4. **Production Ready**: Optimized for cloud-native deployments

## Technology Stack

- **Framework**: Spring Boot 4.0-SNAPSHOT
- **Java**: JDK 25
- **Database**: H2 (in-memory)
- **ORM**: Spring Data JPA / Hibernate
- **Build Tool**: Maven
- **Container**: Docker with Paketo Buildpacks
- **Monitoring**: Spring Boot Actuator

## Performance Optimizations

1. **AOT Processing**: Ahead-of-time compilation for faster startup
2. **CDS**: Class Data Sharing enabled in container
3. **Layered JARs**: Better caching in containerized environments
4. **JRE Runtime**: Smaller footprint compared to full JDK
5. **Memory Settings**: Optimized JVM heap configuration

## Troubleshooting

### Build Issues
If you encounter build issues:
```bash
mvn clean install -U
```

### Container Issues
To inspect the container:
```bash
docker run --rm -it --entrypoint /bin/sh spring-aot-demo:0.0.1-SNAPSHOT
```

View container layers:
```bash
docker history spring-aot-demo:0.0.1-SNAPSHOT
```

### AOT Processing Errors
If AOT processing fails, try building without the AOT profile first:
```bash
mvn clean package
```

## Future Enhancements

Potential improvements for this demo:
- GraalVM Native Image compilation
- PostgreSQL integration for production
- Kubernetes deployment manifests
- Integration tests with Testcontainers
- API documentation with Swagger/OpenAPI
- Caching with Spring Cache
- Security with Spring Security

## License

This is a demonstration project for educational purposes.

## References

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring AOT Documentation](https://docs.spring.io/spring-framework/reference/core/aot.html)
- [Paketo Buildpacks](https://paketo.io/)
- [GraalVM](https://www.graalvm.org/)
