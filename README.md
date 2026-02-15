# Fitify

Fitness platform backend built with **Kotlin**, **Spring Boot 3**, and **Spring Modulith**. Manages coaching, scheduling, subscriptions, locations, and identity through a modular monolith architecture.

## Quick Start

```bash
git clone https://github.com/nickdferrara/server-springboot-fitify.git
cd server-springboot-fitify
cp .env.example .env          # edit .env with your Stripe keys, etc.
docker compose up -d
```

The app will be available at `http://localhost:8080` once all services are healthy.

### Default Credentials

| Service          | URL                        | Username           | Password |
|------------------|----------------------------|--------------------|----------|
| Keycloak Admin   | http://localhost:8180      | `admin`            | `admin`  |
| Fitify Admin     | (via API)                  | `admin@fitify.com` | `admin`  |
| Mailpit Web UI   | http://localhost:8025      | —                  | —        |
| Grafana          | http://localhost:3000      | `admin`            | `admin`  |

## Architecture

Fitify is a **modular monolith** using [Spring Modulith](https://spring.io/projects/spring-modulith) for module boundary enforcement.

### Modules

| Module         | Description                                         |
|----------------|-----------------------------------------------------|
| `admin`        | Admin-facing endpoints and business rules            |
| `coaching`     | Coach assignment and management                      |
| `identity`     | Authentication, registration, Keycloak integration   |
| `location`     | Gym location management                              |
| `logging`      | Centralized logging and observability AOP             |
| `notification` | Email notifications (SMTP / SendGrid)                |
| `scheduling`   | Session scheduling and availability                  |
| `security`     | Rate limiting, token hashing, security utilities     |
| `shared`       | Cross-cutting: encryption, domain errors, events     |
| `subscription` | Stripe-based subscription and billing                |

### Cross-Module Communication

- **Public API interfaces** (`*Api`) for synchronous calls between modules
- **Application events** (data classes in each module's public package) for async communication
- Internal code lives under `internal/` subpackages and is not accessible to other modules

## Environment Variables

See [`.env.example`](.env.example) for all variables with descriptions. Key groups:

| Variable                     | Description                                  | Default                     |
|------------------------------|----------------------------------------------|-----------------------------|
| `POSTGRES_DB`                | Database name                                | `fitify`                    |
| `POSTGRES_USER`              | Database user                                | `fitify`                    |
| `POSTGRES_PASSWORD`          | Database password                            | `fitify`                    |
| `KEYCLOAK_ADMIN`             | Keycloak admin console username              | `admin`                     |
| `KEYCLOAK_ADMIN_PASSWORD`    | Keycloak admin console password              | `admin`                     |
| `SPRING_PROFILES_ACTIVE`     | Spring profile (`dev`, `prod`)               | `dev`                       |
| `KEYCLOAK_CLIENT_SECRET`     | Keycloak `fitify-api` client secret          | `fitify-dev-secret`         |
| `FITIFY_KEYCLOAK_SERVER_URL` | Keycloak base URL                            | `http://localhost:8180`     |
| `ENCRYPTION_KEY`             | Base64-encoded 32-byte AES key               | test default                |
| `TOKEN_PEPPER`               | Random string for token hashing              | dev default                 |
| `STRIPE_SECRET_KEY`          | Stripe API secret key                        | placeholder                 |
| `STRIPE_WEBHOOK_SECRET`      | Stripe webhook signing secret                | placeholder                 |
| `SMTP_HOST`                  | SMTP server hostname                         | `localhost`                 |
| `SMTP_PORT`                  | SMTP server port                             | `1025`                      |

## Bare-Metal Development

To run without Docker (requires local PostgreSQL and Keycloak):

```bash
# Start PostgreSQL on port 5432 and Keycloak on port 8180
# Import the realm: use Keycloak admin UI or CLI to import infra/keycloak/fitify-realm.json

./gradlew bootRun --args='--spring.profiles.active=dev'
```

## Running Tests

```bash
./gradlew test
```

> **Note:** `FitifyApplicationTests` uses Testcontainers and requires Docker. It is skipped automatically when Docker is unavailable.

## Observability

Start the logging stack (Loki + Promtail + Grafana) alongside the app:

```bash
docker compose --profile observability up -d
```

Access Grafana at `http://localhost:3000` — Loki is pre-configured as a data source.

## Keycloak Roles

The realm export (`infra/keycloak/fitify-realm.json`) includes these roles:

| Role             | Description                     |
|------------------|---------------------------------|
| `ADMIN`          | Full system administrator       |
| `LOCATION_ADMIN` | Manages a single gym location   |
| `COACH`          | Personal trainer / coach        |
| `CLIENT`         | Gym member / client             |

The `fitify-api` client uses `client_credentials` grant with `manage-users` and `view-users` permissions for programmatic user management.
