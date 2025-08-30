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

