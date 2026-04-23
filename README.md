# 🛒 E-commerce Microservices Backend

A production-style **E-commerce backend system** built using **Spring Boot Microservices Architecture**, designed for scalability, fault tolerance, and real-world backend workflows.

---

## 🚀 Overview

This project simulates a real-world e-commerce platform where multiple independent services collaborate to handle user operations, product management, cart handling, order processing, payments, and notifications.

It follows **microservices best practices** like:

* Service discovery
* API Gateway routing
* Decoupled services
* Containerized deployment

---

## 🧰 Tech Stack

* **Backend:** Java, Spring Boot
* **Microservices:** Spring Cloud
* **Service Discovery:** Eureka Server
* **API Gateway:** Spring Cloud Gateway
* **Inter-service Communication:** OpenFeign
* **Build Tool:** Maven
* **Containerization:** Docker, Docker Compose

---

## 🏗️ System Architecture

```text
                ┌───────────────┐
                │    Client     │
                └──────┬────────┘
                       │
                       ▼
              ┌──────────────────┐
              │   API Gateway    │
              └──────┬───────────┘
                     │
     ┌───────────────┼────────────────┐
     ▼               ▼                ▼
┌──────────┐   ┌──────────┐   ┌──────────┐
│ User     │   │ Product  │   │ Cart     │
│ Service  │   │ Service  │   │ Service  │
└──────────┘   └──────────┘   └──────────┘
     ▼               ▼                ▼
┌──────────┐   ┌──────────┐   ┌──────────┐
│ Order    │   │ Payment  │   │ Notification │
│ Service  │   │ Service  │   │ Service      │
└──────────┘   └──────────┘   └──────────────┘

            ┌──────────────────────┐
            │   Eureka Server      │
            │ (Service Registry)   │
            └──────────────────────┘
```

---

## 🧩 Microservices

| Service              | Responsibility                           |
| -------------------- | ---------------------------------------- |
| API Gateway          | Central entry point, routing & filtering |
| Eureka Server        | Service discovery & registration         |
| User Service         | User management                          |
| Product Service      | Product catalog                          |
| Cart Service         | Shopping cart operations                 |
| Order Service        | Order processing                         |
| Payment Service      | Payment handling                         |
| Notification Service | Email/SMS notifications                  |

---

## ⚙️ Key Features

* 🔹 Microservices-based architecture
* 🔹 Centralized API Gateway
* 🔹 Service discovery with Eureka
* 🔹 Inter-service communication using Feign
* 🔹 Scalable and loosely coupled design
* 🔹 Dockerized deployment
* 🔹 Clean modular structure

---

## 🐳 Running with Docker (Recommended)

### 🔹 Start all services

```bash
docker-compose up --build
```

### 🔹 Stop all services

```bash
docker-compose down
```

---

## ▶️ Running Locally (Without Docker)

### Step 1: Start Eureka Server

```bash
cd eureka-server
mvn spring-boot:run
```

### Step 2: Start API Gateway

```bash
cd api-gateway
mvn spring-boot:run
```

### Step 3: Start all other services

Run each service individually:

```bash
mvn spring-boot:run
```

---

## 🌐 API Gateway

All APIs are exposed through:

```
http://localhost:<gateway-port>
```

### Example APIs

```http
GET /api/v1/products
GET /api/v1/users
POST /api/v1/orders
```

---

## 🔗 Inter-Service Communication

* Uses **Feign Client**
* Services communicate via service names registered in Eureka

---

## 🔐 Configuration

Sensitive values should be configured via:

* Environment variables
* External configuration

Example:

```
DB_URL=jdbc:mysql://localhost:3306/ecommerce
DB_PASSWORD=your-password
```

---

## 📁 Project Structure

```
ecommerce-microservices-app/
 ├── api-gateway/
 ├── eureka-server/
 ├── user-service/
 ├── product-service/
 ├── cart-service/
 ├── order-service/
 ├── payment-service/
 ├── notification-service/
 ├── docker-compose.yml
 └── README.md
```

---

## ⚠️ Best Practices Followed

* Clean separation of concerns
* Independent deployable services
* Centralized routing via gateway
* Environment-based configuration
* Ignored logs/build files using `.gitignore`

---

## 🚧 Future Enhancements

* 🔹 Kafka for event-driven architecture
* 🔹 Centralized logging (ELK Stack)
* 🔹 Circuit Breaker (Resilience4j)
* 🔹 CI/CD pipeline (GitHub Actions)

---

## 👨‍💻 Author

**Akhil Janardhan**

---

## ⭐ Show your support

If you found this useful, give it a ⭐ on GitHub!

---
