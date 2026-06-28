# Digital Detox — REST API

Spring Boot REST API for the Digital Detox platform (`com.digitaldetox`).

> **Full documentation:** see [DIGITAL-DETOX-README.md](../DIGITAL-DETOX-README.md) in the workspace root for architecture, API reference, demo flow, and troubleshooting.

## Quick start

```bash
# 1. Create PostgreSQL database (see master README)
# 2. Run the API
./gradlew bootRun
```

- API: [http://localhost:8080](http://localhost:8080)
- Swagger: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- Default admin: `admin` / `Admin123!`

## Stack

- Java 21, Spring Boot 3.5.14
- PostgreSQL + Flyway
- JWT (jjwt), Spring Security, `@PreAuthorize`
- springdoc OpenAPI, Apache Tika (file type detection)

## Build & test

```bash
./gradlew build
./gradlew test
```

## Key config (`application-dev.properties`)

| Property | Default |
|----------|---------|
| DB | `jdbc:postgresql://localhost:5432/digitaldetox` |
| User / pass | `detox_user` / `detox_pass` |
| Upload dir | `uploads/` |
| CORS | `http://localhost:5173` |
