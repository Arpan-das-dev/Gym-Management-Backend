# 📌 Member Service - FitStudio Microservice

The **Member Service** is a core microservice in the **FitStudio ecosystem**, responsible for managing gym members.  
It handles member profiles, trainers, plans, sessions, and integrates with external services for notifications and approvals.

---

## 🚀 Features

- Member management (create, fetch, update, delete)
- Trainer assignment & request approvals
- Plan tracking (active/expired, renewals, eligibility)
- Profile management with **AWS S3** for image storage
- Fitness & streak tracking (BMI, login streaks)
- Freeze/unfreeze member accounts
- Session scheduling with notifications **30 mins before start**
- Caching with **Redis** for optimized performance
- Communication with other services using **Spring WebClient**
- Centralized exception handling with custom exceptions

---

## 🏗️ Architecture

This service is part of the **FitStudio Microservices Architecture**:

- **Auth Service** → Authentication, signup/login, OTP, email/SMS verification
- **Member Service (this repo)** → Manages member data & operations
- **Trainer Service** → Handles trainer details & schedules
- **Plan Service** → Manages membership plans
- **Notification Service** → Sends reminders & alerts
- **Admin Service** → Approves/rejects trainer/member requests

📡 **Interactions**:
- Communicates with **Plan Service** to fetch/renew plans
- Notifies **Notification Service** for reminders
- Integrates with **Admin Service** for request approvals
- Uses **AWS S3** for profile image storage

---

## ⚙️ Tech Stack

- **Java 21**
- **Spring Boot 3.x**
    - Spring Data JPA (MySQL RDS)
    - Spring Cache (Redis)
    - Spring Scheduler
    - Spring Web (WebClient)
- **MySQL (AWS RDS)**
- **Redis (Docker / ElastiCache)**
- **AWS S3** (file storage)
- **Lombok** for reducing boilerplate

---

## 📂 Project Structure

## 📡 API Endpoints (Sample)
```
GET /fitStudio/member-service/members → Get all members
GET /fitStudio/member-service/members/{id} → Get member by ID
POST /fitStudio/member-service/members → Create new member
DELETE /fitStudio/member-service/members/{id} → Delete member
PUT /fitStudio/member-service/members/freeze → Freeze/unfreeze member
```
yaml
Copy code

> Full endpoint details are documented in the corresponding **controllers**.

---

## 🔧 Configuration

All environment-specific configuration (DB, Redis, AWS keys, service URLs) must be externalized using:

- **Environment Variables**
- **Kubernetes Secrets / ConfigMaps**
- **Spring Cloud Config**

Example (environment variables in production):

```bash
export DB_URL=jdbc:mysql://<host>:3306/members
export DB_USER=admin
export DB_PASS=********
export REDIS_HOST=localhost
export REDIS_PORT=6379
export AWS_ACCESS_KEY=********
export AWS_SECRET_KEY=********
export AWS_REGION=ap-south-1
export AWS_BUCKET=fitstudio-members
```
🏃 Running Locally
Prerequisites

Java 21

Docker (for Redis)

MySQL (local or AWS RDS)

Steps
# 1. Clone repository
git clone https://github.com/your-org/member-service.git
cd member-service

# 2. Start Redis locally
docker run --name redis -p 6379:6379 -d redis

# 3. Export environment variables (DB, Redis, AWS)

# 4. Run service
./mvnw spring-boot:run


Service will be available at:
👉 http://localhost:8083/fitStudio/member-service

📦 Deployment

Local/EC2: Run with mvn spring-boot:run or package as JAR.

Docker: Build image and deploy to AWS ECS / ECR.

Kubernetes: Use Helm charts or manifests with externalized config.

AWS:

DB → MySQL RDS

Cache → ElastiCache (Redis)

File storage → S3

Service → ECS/EKS or EC2

🛡️ Caching Strategy

memberListCache → caches all members

memberCache::{id} → caches single member details

Cache updated via @CacheEvict whenever data changes

📅 Scheduler

SchedulerTaskService runs every 30 minutes

Finds sessions starting within the next 30 mins

Sends notifications via Notification Service

🔒 Security

Authentication is handled by Auth Service (JWT)

API Gateway forwards the JWT to this service

This service validates tokens before processing requests

✅ Future Improvements

Add unit & integration test coverage

Centralized logging with ELK stack

Metrics with Prometheus & Grafana

Circuit breakers with Resilience4j

Kubernetes deployment with autoscaling

👨‍💻 Author

Arpan Das
FitStudio Microservices Project 🚀