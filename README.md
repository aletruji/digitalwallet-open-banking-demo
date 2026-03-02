
Backend

This project is built using Java and Spring Boot.
The backend follows a layered architecture and integrates with the NatWest Open Banking API using the OAuth2 Authorization Code flow.

Access and refresh tokens are securely stored in the database, and access tokens are automatically refreshed when expired.
The system uses a modular structure with a connector pattern to decouple the wallet logic from the bank provider.


Frontend

The frontend is built with Angular and TypeScript.
It provides a simple user interface to connect to the bank, view account balances, and inspect recent transactions.

The UI communicates with the Spring Boot backend via REST endpoints.

<img width="491" height="293" alt="Bildschirmfoto 2026-03-02 um 12 42 13" src="https://github.com/user-attachments/assets/b862e4a8-5732-4603-948f-fb8456c1aede" />

 About the Bank API & Authentication


Open Banking Integration

The application integrates with the NatWest Open Banking API.
It uses the OAuth2 Authorization Code flow:

The application creates a consent.

The user authorization step is simulated in sandbox mode.

An authorization code is exchanged for an access token.

Access tokens are used to retrieve accounts, balances, and transactions.

All secured API calls include a Bearer token and required Open Banking headers.


 What the Web App Does

Web Application Overview

<img width="454" height="454" alt="Bildschirmfoto 2026-03-02 um 12 42 25" src="https://github.com/user-attachments/assets/a1ef1448-f300-42ae-9ff4-205ac9b3050b" />

The web application allows users to:

Connect to a bank account

View all linked accounts

See balances per account

View recent transactions

See an aggregated overview

Transactions are filtered for the last 30 days and amounts are rounded according to business rules.




# DigitalWallet – Local Setup

## Requirements

Install:
- Java (JDK 21 or higher)
- Node.js (LTS)
- npm (comes with Node)
- Angular CLI


## Setup

1. Clone the repository.
2. Place the provided `.env` file inside:
backend/.env

---

## Start Backend
cd backend 
/

Windows (CMD only):

for /f "usebackq delims=" %i in (.env) do @set %i
mvnw.cmd spring-boot:run

macOS:

export $(grep -v '^#' .env | xargs)
./mvnw spring-boot:run

Backend runs on:
http://localhost:8080

---

## Start Frontend (Windows & mac)
cd frontend
/

ng serve --proxy-config proxy.conf.json

Frontend runs on:
http://localhost:4200
