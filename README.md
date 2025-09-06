# Gym Project Microservices

## Overview
This project implements a microservices architecture for a gym management system. Each microservice is responsible for a distinct business capability, enabling independent development, deployment, and scaling.

Currently implemented microservices:
- **Auth Service:** Manages authentication, authorization, and user management with role-based access control.
- **Notification Service (planned):** Handles sending notifications via AWS SNS (Simple Notification Service) and email via AWS SES (Simple Email Service).

## Architecture
- Each microservice has its own database to ensure loose coupling and service autonomy.
- Services communicate asynchronously via AWS SNS or synchronously over REST APIs secured by JWT tokens.
- Role-based access control is enforced in the Auth Service using Spring Security.

## Technologies
- Java 21
- Spring Boot (Web, Data JPA, Security)
- AWS Services: SNS, SES, 
- Docker for containerization
- MySQL or appropriate RDS for databases
- Spring Cloud AWS for AWS integrations

## Auth Service
- Responsible for user login, registration, and token-based authentication.
- Exposes secured endpoints with role-based authorization.
- Maintains a local copy of essential user information to support quick access and updates.
- Publishes user-related events to SNS topics for consumption by other services like Notification Service.

### Key Dependencies
- Spring Web
- Spring Data JPA
- Spring Security
- Spring Cloud AWS (for SNS integration)
- Lombok

## Notification Service (to be implemented)
- Subscribes to relevant SNS topics for real-time notification events.
- Sends email notifications via AWS SES.
- Provides endpoints to manage notifications securely.

### Recommended Dependencies
- Spring Web
- Spring Cloud AWS (SNS and SES starters)
- Lombok

## Communication Between Services
- Auth Service publishes notification events asynchronously through AWS SNS.
- Notification Service consumes these events to trigger emails or other notifications.
- Secure service-to-service communication using JWT tokens or AWS IAM roles.
- Role-based access control is applied to all sensitive APIs within each service.

## Deployment Strategy
- Containerize services using Docker.
- Deploy on AWS using ECS/EKS with Application Load Balancer for routing.
- Use AWS Route 53 for DNS management and optionally custom domains.
- Utilize environment-based configurations and secret management for secure credentials.

## Getting Started
1. Clone the repository.
2. Build microservices using Maven/Gradle.
3. Configure AWS credentials and necessary resources (SNS topics, SES verified domains).
4. Build and run Docker containers locally or deploy on AWS.
5. Secure endpoints with appropriate roles and policies.
6. Monitor and scale services independently.

---

Feel free to contribute or extend this architecture as per your project needs!
This README guides you through the architecture, dependencies, services, and communication strategies specifically for your gym microservices project including auth and notification services using AWS SES/SNS.

