# Short Code Generation System

## Overview

The Short Code Generation System is a Spring Boot application that enables the generation, approval, validation, and management of customer short codes linked to bank accounts.

The system follows a Maker-Checker workflow where one user initiates a request and another user approves it before a short code becomes active.

The application has been modernized to improve security, maintainability, auditability, and deployment readiness.

---

## Technology Stack

### Backend

* Java 17
* Spring Boot 2.7.4
* Spring Security
* Keycloak

### Database

* PostgreSQL

### Documentation

* Swagger / OpenAPI

### Build Tools

* Maven

### Containerization

* Docker
* Docker Compose

---

## Key Features

### Account Validation

* Validates customer account information using Finacle integration.
* Retrieves customer details before shortcode generation.

### Shortcode Initiation

* Maker user submits a shortcode request.
* System validates request details.
* Prevents duplicate pending requests.

### Shortcode Approval

* Checker user approves shortcode requests.
* Generates a unique shortcode.
* Generates and emails a PDF confirmation slip.

### Shortcode Deletion Workflow

* Maker initiates deletion.
* Checker approves deletion.
* Full audit history maintained.

### Audit Trail

* Tracks all significant actions:

  * INITIATE
  * APPROVE
  * DELETE_REQUEST
  * DELETE_APPROVE

### Security

* Keycloak Authentication
* JWT Authorization
* Role-Based Access Control

### Integrity Protection

* SHA-256 hashing of shortcode records.
* Integrity verification before approval and lookup operations.

---

# Roles

## Maker

Can:

* Validate accounts
* Initiate shortcode requests
* Initiate shortcode deletion

## Checker

Can:

* Approve shortcode requests
* Approve shortcode deletion

## API Caller

Can:

* Access protected APIs
* Perform system integrations

---

# Improvements Implemented

The following improvements were added during modernization:

## Security Enhancements

### Keycloak Integration

Added enterprise authentication and authorization.

### JWT Security

Protected APIs using JWT bearer tokens.

### Role-Based Access Control

Implemented:

* maker
* checker
* apicaller

### Deterministic SHA-256 Hashing

Replaced object hash-based validation with deterministic SHA-256 hashing.

Benefits:

* Consistent across environments
* Resistant to accidental modifications
* Improved integrity verification

---

## Audit and Compliance Enhancements

### Audit Trail Table

Created audit trail persistence.

Tracks:

* User
* Action
* Date
* Remarks
* Account Number
* Shortcode

### Audit Trail API

GET /shortcodes/api/audit/{shortCode}

Allows auditors and supervisors to review shortcode activity without database access.

---

## Code Quality Improvements

### DTO Mapping Refactor

Removed repetitive DTO conversion logic.

Introduced reusable mapping layer.

### Global Exception Handling

Added centralized exception management.

Benefits:

* Cleaner controllers
* Consistent error responses
* Improved maintainability

### Null Safety Improvements

Removed potential NullPointerExceptions when converting dates.

---

## API Documentation

Implemented Swagger/OpenAPI documentation.

Accessible via:

http://localhost:8088/swagger-ui/index.html

---

# Database Tables

## short_code

Stores shortcode requests and approvals.

Key fields:

* id
* account_number
* short_code
* approved
* deleted
* hash
* date_initiated
* date_approved

---

## audit_trail

Stores user activity history.

Key fields:

* id
* short_code_id
* account_number
* short_code
* action
* performed_by
* remarks
* action_date

---

# Setup Instructions

## Prerequisites

Install:

* Java 17
* Maven 3.8+
* Docker
* Docker Compose
* PostgreSQL (or Dockerized PostgreSQL)

---

## Clone Repository

git clone <repository-url>

cd shortcode

---

## Build Project

mvn clean install

---

## Run PostgreSQL Container

docker-compose up -d

---

## Configure Application

Update application.properties:

spring.datasource.url=
spring.datasource.username=
spring.datasource.password=

keycloak.realm=
keycloak.auth-server-url=
keycloak.resource=

---

## Run Application

mvn spring-boot:run

or

./mvnw spring-boot:run -Dspring-boot.run.profiles=dev -Dspring-boot.run.jvmArguments="-Djasypt.encryptor.password=jasyptkey"

---

# Testing

## Swagger

Access:

http://localhost:8088/swagger-ui/index.html

Authenticate using a valid JWT token from Keycloak.

---

# Available APIs

Account Validation:
GET /shortcodes/api/validate/{accountNumber}

Initiate:
POST /shortcodes/api/initiate

Approve:
POST /shortcodes/api/approve

Delete Request:
DELETE /shortcodes/api/delete

Approve Delete:
POST /shortcodes/api/approve-delete

Pending:
GET /shortcodes/api/pending

Approved:
GET /shortcodes/api/approved

Pending Delete:
GET /shortcodes/api/pending-delete

Account Lookup:
GET /shortcodes/api/get-account/{shortCode}

Account Details:
GET /shortcodes/api/get-account-details/{shortCode}

Audit Trail:
GET /shortcodes/api/audit/{shortCode}

---

# Future Enhancements

The following were identified but intentionally deferred:

* Redis caching
* RabbitMQ asynchronous processing
* Bean Validation annotations
* Expanded unit testing
* API response standardization
* CI/CD pipeline automation

---

# Project Status

The application is fully functional and has been modernized with enterprise-grade security, auditing, documentation, and maintainability improvements while preserving the original business workflow.
