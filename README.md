# Gatekeeper

Gatekeeper is a lightweight Java CLI API security scanner that detects insecure CORS, Spring Boot actuator exposure, and basic SQL injection indicators. It's designed to run in CI/CD (GitHub Actions) and as a container.

## Build

Requirements: Java 17 and Maven.

```bash
mvn clean package
```

## Run (jar)

```bash
java -jar target/gatekeeper-1.0.jar --url https://staging.example.com --check-cors --check-actuator --check-sql
```

If you omit `--check-*` flags, Gatekeeper will run all checks by default.

## Docker

```bash
docker build -t gatekeeper:1.0 .
docker run --rm gatekeeper:1.0 --url "https://httpbin.org/response-headers?Access-Control-Allow-Origin=%2A" --check-cors
```

## GitHub Actions

Use the supplied workflow `.github/workflows/security-scan.yml`. Trigger via `workflow_dispatch` and provide `target-url` input.

## Report

Gatekeeper writes a simple monochrome professional report to stdout. Use `--report=filename` to write results to disk for CI artifact upload.
