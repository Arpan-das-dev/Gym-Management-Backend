# Auth Service Microservice

## Overview
Auth Service is a dedicated microservice in the Gym Management System handling all authentication, authorization, and user credential management. It manages secure user signup, login, email and phone verification via OTP, password reset, and role-based access control using JSON Web Tokens (JWT). Redis caching is leveraged for OTP performance, and asynchronous notifications ensure timely communication with users.

This service is designed for scalability, security, and smooth interaction with other microservices in the ecosystem.

---

## Features

- User Signup with role support (`member`, `trainer_pending`)
- Email & Phone OTP verification with Redis caching
- JWT-based login authentication supporting email or custom ID
- Password management: forgot, reset, and change password workflows
- Async email and SMS notification dispatch
- Exception handling with detailed custom exceptions and global handler
- Utility components for secure ID and OTP generation

---

## Architecture & Technologies

- **Spring Boot** — core framework  
- **Spring Security** — authentication & password encoding  
- **JWT (JSON Web Tokens)** — stateless session management  
- **Redis** — caching OTPs with TTL for verification  
- **WebClient** — reactive non-blocking HTTP calls for notifications  
- **MySQL Aurora RDS** — persistent user data storage  
- **Lombok** — code reduction annotations
- **Asynchronous Processing (@Async)** — notifications sent without blocking user flows

---

## Project Structure
```
auth-service
└── src/main/java/com/gym/authservice
├── Config # Redis, WebClient, Security config classes
├── DetailsService # Spring Security UserDetailsService implementation
├── Jwt # JWT utility and filters
├── Security # Security configurations and method-based security
├── Controller # REST API controllers for signup, login, verification, credentials
├── Dto # Request and Response DTOs
├── Entity # JPA entities representing users and signups
├── Exceptions # Custom exceptions and global exception handler
├── Repository # Spring Data repositories
├── Roles # User role definitions
├── Service # Business logic services (signup, login, credentials, verification, notification)
├── Utils # Utilities for ID and OTP generation
└── AuthServiceApplication.java # Main Spring Boot application class
---
```

## API Endpoints

| Endpoint                       | Method | Description                                   |
|-------------------------------|--------|-----------------------------------------------|
| `/signup`                     | POST   | Register new user; trainer role set to pending approval |
| `/login`                      | POST   | Authenticate user by email or ID, return JWT  |
| `/verify/email`               | POST   | Verify user email with OTP                     |
| `/verify/phone`               | POST   | Verify user phone with OTP (after email verified) |
| `/password/forgot`            | POST   | Initiate forgot password; sends email OTP     |
| `/password/reset`             | POST   | Reset password by validating old password     |
| `/password/change`            | POST   | Change password (email verification required) |

---

## How It Works

### Signup Service
- Validates that email/phone don't exist.
- Creates user with encoded password.
- Assigns `TRAINER_PENDING` role for trainers.
- Sends approval request to Admin service asynchronously.
- Generates and stores OTPs for email and phone verification in Redis.
- Sends welcome message and OTPs via Notification service asynchronously.

### Verification Service
- Stores OTPs in Redis with 15-minute TTL.
- Validates OTP against cached value, updates user verification status in DB.
- Phone verification requires prior email verification.
- Deletes OTP from Redis upon successful verification.

### Credential Service
- Handles forgot password by generating OTP and sending email.
- Resets password validating old password and updating with new hashed password.
- Changes password post email verification.

### Login Service
- Authenticates user by email or ID.
- Validates password with Spring Security encoder.
- Generates JWT token embedding user email, role, and ID.
- Returns JWT token for use in secured API calls.

### Notification Service
- Sends various notifications asynchronously using Spring's @Async.
- Uses reactive WebClient to make non-blocking REST calls to notification endpoints.
- Supports email OTP, phone OTP, password reset, and welcome messages.

---

## Security

- Passwords stored encrypted using BCrypt.
- JWT tokens provide secure stateless authentication.
- Role-based access control enabled via Spring Security.
- OTPs stored temporarily in Redis, never persisted in DB.
- Asynchronous notification ensures responsive user interactions.

---

## Prerequisites

- Java 11 or higher
- Maven 3.x
- Redis Server (for OTP caching)
- MySQL / AWS Aurora RDS (for persistent user data)
- SMTP service or Email API for sending mails

---

## Setup & Running Locally

1. Clone the repository:

2. 2. Configure application properties (`application.properties` or environment variables) for:
- Database (MySQL or Aurora endpoint, username, password)
- Redis connection details
- JWT secret key
- Notification service base URL and credentials

3. Build and run:
mvn clean install
mvn spring-boot:run

text

4. Access API at `http://localhost:8080/api/auth/`

---

## Future Enhancements

- Integrate fully with Admin microservice for approval workflows.
- Support user profile management including profile images.
- Add OAuth2 / Social Login support.
- Expand logging and monitoring for production readiness.
- Containerize with Docker and orchestrate with Kubernetes.

---

## Contact & Support

For bugs, feature requests, or questions, please open an issue on the GitHub repository or contact the maintainer:

- **Name:** Arpan Das 
- **Email:** dasarpan915@gmail.com

---
