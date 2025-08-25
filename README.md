AuthService

AuthService is a Spring Boot microservice responsible for handling authentication and user signup for the Gym Management System. It manages user registration, login, JWT-based authentication, email/phone verification, and integration with the Member Service for promoting users.

Tech Stack
# AuthService

**AuthService** is a Spring Boot microservice responsible for handling authentication and user signup for the Gym Management System. It manages user registration, login, JWT-based authentication, email/phone verification, and integration with the Member Service for promoting users.

---

## Table of Contents
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [Docker](#docker)
- [API Endpoints](#api-endpoints)
- [Future Enhancements](#future-enhancements)

---

## Features
- User signup with custom ID generation
- Password hashing using BCrypt
- JWT-based authentication and authorization
- Role-based access control (Admin, Member, Trainer)
- Email & phone OTP verification (template-based)
- Integration with Member Service to promote verified users
- Modular microservice architecture ready for scaling

---

## Tech Stack
- **Backend:** Java, Spring Boot, Spring Security, Spring Data JPA
- **Database:** MySQL (RDS-ready)
- **Authentication:** JWT
- **Email & SMS:** AWS SES & SNS
- **Caching:** Redis (optional for OTP and token optimization)
- **Containerization:** Docker & Docker Compose
- **Deployment:** AWS ECS / Kubernetes ready

---

## Architecture
```text
AuthService
│
├── Controller         # REST endpoints for signup, login, verification
├── Service            # Business logic: signup, JWT generation, OTP sending
├── Repository         # JPA Repositories for SignedUps table
├── Entity             # DB entities (SignedUps)
├── DTO                # Request & Response objects for APIs
├── Security
│   ├── Jwt            # JWT utility and filter
│   ├── DetailsService # Loads user details for Spring Security
│   └── Config         # Security configurations
├── Util               # ID generation, helper utilities
└── Exception          # Global exception handling
