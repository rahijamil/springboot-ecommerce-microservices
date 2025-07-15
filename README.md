# 🛒 Ecommerce Microservices Project

A polyglot, containerized microservices architecture for an e-commerce platform, built with Spring Boot, Kafka, PostgreSQL, MongoDB, Keycloak for authentication, Eureka for service discovery, and observability via Zipkin, Prometheus, and Grafana.

---

## 🗺️ Architecture Overview

This project uses a microservice architecture with the following components:

- An **API Gateway** (Spring Cloud Gateway) as the single entrypoint.
- Multiple **domain microservices** (Product, Order, Inventory, Notification) communicating via HTTP and Kafka.
- **Service Discovery** with Eureka.
- **Centralized authentication** using Keycloak (OAuth2 / OIDC).
- **Messaging** with Kafka (broker + Zookeeper).
- **Persistence** with PostgreSQL and MongoDB.
- **Tracing** using Zipkin.
- **Monitoring** with Prometheus and Grafana.
- Fully containerized and orchestrated via Docker Compose.

---

## ✅ Features

- Microservice architecture with Spring Boot 3.x and Spring Cloud 2023.x
- Service discovery with Eureka
- API Gateway with Spring Cloud Gateway
- Synchronous and asynchronous communication (HTTP, Kafka)
- Kafka for event-driven messaging
- Resilience4j circuit breaking, retry, timeouts
- Distributed tracing with Zipkin
- Metrics monitoring with Prometheus and Grafana
- OAuth2 / OIDC authentication with Keycloak
- SQL (PostgreSQL) and NoSQL (MongoDB) databases
- Docker Compose orchestration

---

## 🧩 Services

| Service                | Description                                  | Tech Stack               |
|-------------------------|----------------------------------------------|--------------------------|
| **API Gateway**         | Single entrypoint with routing               | Spring Cloud Gateway     |
| **Product Service**     | Product catalog CRUD                         | Spring Boot, MongoDB     |
| **Order Service**       | Order placement, transactional boundaries    | Spring Boot, PostgreSQL  |
| **Inventory Service**   | Stock management                             | Spring Boot, PostgreSQL  |
| **Notification Service**| Kafka consumer for order events              | Spring Boot, Kafka       |
| **Discovery Server**    | Service registry                             | Eureka                   |
| **Auth Server**         | User login, OAuth2, OIDC                     | Keycloak, MySQL          |
| **Messaging**           | Asynchronous events                          | Kafka, Zookeeper         |
| **Tracing**             | Distributed tracing                          | Zipkin                   |
| **Monitoring**          | Metrics and visualization                    | Prometheus, Grafana      |

---

## ⚙️ Prerequisites

- Docker & Docker Compose
- Java 17+
- Maven

---

## 🚀 Getting Started

Clone the repo:

```bash
git clone https://github.com/rahijamil/springboot-ecommerce-microservices.git
cd springboot-ecommerce-microservices
````

---

## 🐳 Running with Docker Compose

### 1️⃣ Build all services

Make sure you’ve built your Spring Boot microservice JARs into Docker images (they're tagged in the Compose file as `rahijamil/{service}:latest`):

```bash
# Example for all modules
mvn clean package -DskipTests

docker build -t rahijamil/product-service ./product-service
docker build -t rahijamil/order-service ./order-service
docker build -t rahijamil/inventory-service ./inventory-service
docker build -t rahijamil/notification-service ./notification-service
docker build -t rahijamil/api-gateway ./api-gateway
docker build -t rahijamil/discovery-server ./discovery-server
```

### 2️⃣ Start the full stack

```bash
docker-compose up --build
```

> This will launch:
>
> * Databases (Postgres, Mongo, MySQL for Keycloak)
> * Zookeeper, Kafka broker
> * Zipkin
> * Eureka Server
> * API Gateway
> * All microservices
> * Prometheus & Grafana

---

## ⚡ Ports & Endpoints

| Service              | Port (Host) | Notes                         |
| -------------------- | ----------- | ----------------------------- |
| API Gateway          | 8181        | Entry point for frontend      |
| Discovery Server     | 8761        | Eureka dashboard              |
| Keycloak             | 8080        | Auth server admin UI          |
| Kafka Broker         | 9092/29092  | External / internal listeners |
| Zookeeper            | 2181        | Kafka dependency              |
| Zipkin               | 9411        | Tracing dashboard             |
| Prometheus           | 9090        | Metrics dashboard             |
| Grafana              | 3000        | Visualization dashboard       |
| MongoDB              | 27017       | Product DB                    |
| Postgres (Order)     | 5431        | Order DB                      |
| Postgres (Inventory) | 5432        | Inventory DB                  |
| Keycloak MySQL       | 3306        | Auth DB                       |

---

## 🔑 Authentication

* Auth server (Keycloak) runs at [http://localhost:8080](http://localhost:8080)
* Default Admin Credentials:

  * Username: `admin`
  * Password: `admin`
* Uses imported realm from `./realms/`

---

## 🛠️ Prometheus Monitoring

Prometheus is configured to scrape `/actuator/prometheus` endpoints of:

* Product Service
* Order Service
* Inventory Service
* Notification Service

Example scrape config:

```yaml
scrape_configs:
  - job_name: 'order_service'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['order-service:8080']
```

**Important**: Each service must include:

```properties
management.endpoints.web.exposure.include=prometheus
```

And have dependency:

```xml
<dependency>
  <groupId>io.micrometer</groupId>
  <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

---

## 📈 Observability Dashboards

* **Zipkin**: [http://localhost:9411](http://localhost:9411)
* **Prometheus**: [http://localhost:9090](http://localhost:9090)
* **Grafana**: [http://localhost:3000](http://localhost:3000)

  * Default login:

    * User: `admin`
    * Password: `password`

---

## 💬 Example cURL

Example to place an order via the API Gateway:

```bash
curl -X POST http://localhost:8181/api/order/place \
  -H "Content-Type: application/json" \
  -d '{
    "orderLineItemsDtoList": [
        {
            "skuCode": "iphone_17",
            "price": 1200,
            "quantity": 1
        }
    ]
}'
```

---

## ✅ Notes & Troubleshooting

* Services must register with Eureka before API Gateway can route.
* Ensure Kafka Broker and Zookeeper are running before producers/consumers.
* If Prometheus target shows DOWN with 404, ensure `/actuator/prometheus` is exposed.
* If Keycloak fails to start, verify MySQL connectivity and volume paths.

---

## 🙏 Acknowledgements

* Spring Boot
* Spring Cloud
* Apache Kafka
* PostgreSQL & MongoDB
* Keycloak
* Zipkin
* Prometheus & Grafana
