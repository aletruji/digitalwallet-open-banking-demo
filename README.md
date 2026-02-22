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
