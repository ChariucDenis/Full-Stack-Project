# 🚗 Rent-A-Car — Spring Boot + React (AI Booking Assistant)

[![Java](https://img.shields.io/badge/Java-21+-red.svg)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18-61DAFB.svg)](https://react.dev/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-14+-316192.svg)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-ready-2496ED.svg)](https://www.docker.com/)
[![Render](https://img.shields.io/badge/Render-backend-46E3B7.svg)](https://render.com/)
[![Netlify](https://img.shields.io/badge/Netlify-frontend-00AD9F.svg)](https://www.netlify.com/)

Full-stack **rent-a-car** app with **AI assistant** that understands natural language and can:
- answer simple questions without calling the LLM (cheap, fast),
- propose actions (e.g., *“Open all cars”*, *“See details”*),
- check availability & compute prices based on dates.

---

## 🌐 Live Demo

- **Backend (Render)**: https://full-stack-project-tjhf.onrender.com  
- **Frontend (Netlify)**: https://bluerentcar.netlify.app/

> Tip: first open the backend once (cold start on Render), then the frontend.

---
## ✨ Highlights

- **AI Booking Assistant (cost-aware)**: rules-first, DB-driven answers; falls back to gpt-4o-mini only for small-talk ➜ low token usage.
- **Actionable replies**: chat messages can include actions (e.g., “Open all cars” → navigate to /cars).
- **Car catalog**: images, price/day, fuel, transmission, year, search & filters, pagination.
- **Reservations API**: availability checks, conflict detection, admin operations.
- **Auth (JWT)**: register/login; protected endpoints for admin.
- **Prod-ready deploys**: Backend on Render, Frontend on Netlify.

---
## ⚙️ Tech Stack

- **Backend**: Spring Boot, Spring Security (JWT), Spring Data JPA (Hibernate), PostgreSQL, HikariCP  
- **AI**: Spring AI (with OpenAI), plus deterministic DB tools to avoid rate-limit & cost  
- **Frontend**: React 18, Axios, React Router  
- **Infra**: Render (Postgres + Java service), Netlify (static React), optional Docker

---
## 🗺️ Architecture (high level)

    frontend/ (Netlify)
     └─ ChatBotAi.jsx  ──▶  POST /api/ai/chat  ─┐
         ▲                                  │
         │ actions: confirm_navigate, ...   │
         └──────────────────────────────────┘
    backend/  (Render)
        ├─ /api/v1/auth             register + authenticate (JWT)
        ├─ /api/v1/car              CRUD + images
        ├─ /api/v1/reservations     availability & booking
        └─ /api/ai/chat             AI assistant (DB-first + LLM fallback)
    database/
        └─ Postgres (Render)

---
## 🤖 AI Assistant (How it answers)

### Heuristics first (no LLM):

- “Which is the cheapest car?”

- “Do you have a car at 50 RON/day? If not, return nearest below/above.”

- “Show all cars” → offers an action: “Open all cars” (to /cars)

- Availability by name & date interval (e.g., “Is BMW available between 2025-10-22 and 2025-10-25?”)


### LLM fallback:
- friendly, short replies for generic chit-chat; uses gpt-4o-mini only when rules don’t match.

### Action schema (sent to frontend):
```bash
{
  "reply": "We have many options. Do you want to open the cars page?",

  "actions": [
    { "type": "confirm_navigate", "to": "/cars", "label": "Yes, show all cars" },
    { "type": "dismiss", "to": "", "label": "No, stay in chat" }
  ]
}
```
---

## 🔌 API (quick reference)

### Auth (JWT)
- POST /api/v1/auth/register — create account, returns JWT

- POST /api/v1/auth/authenticate — login, returns JWT

### Cars

- GET /api/v1/car — list cars

- GET /api/v1/car/{id} — single car

- GET /api/v1/car/{id}/image — car image

- POST /api/v1/car — create (multipart form)

- PUT /api/v1/car/{id} — update (multipart form)

- DELETE /api/v1/car/{id} — delete

### Reservations

- POST /api/v1/reservations — create

- GET /api/v1/reservations/my — my reservations

- PATCH /api/v1/reservations/{id}/cancel — cancel

- GET /api/v1/reservations — admin

- PUT /api/v1/reservations/{id} — admin

- DELETE /api/v1/reservations/{id} — admin

- GET /api/v1/reservations/unavailable?carId=..&from=YYYY-MM-DD&to=YYYY-MM-DD

### AI

- POST /api/ai/chat
```bash
curl -X POST https://full-stack-project-tjhf.onrender.com/api/ai/chat \
  -H "Content-Type: application/json" \
  -d '{"sessionId":"web","message":"What cars do you have?"}'
  ```
---
  ## 🗄️ DB Schema (PostgreSQL)

![DB Schema](./Database.png)

---

## 🖥️ Local Setup (Development)

###   Backend (Spring Boot)

Create `.env` inside backend root:


```env
DB_HOST=localhost
DB_PORT=5432
DB_NAME=rentai
PGUSER=postgres
PGPASSWORD=admin
JDBC_URL=jdbc:postgresql://localhost:5432/rentai
OPENAI_API_KEY=sk-xxxx
```
 src/main/resources/application.properties

```
server.port=${PORT:8080}

spring.datasource.url=${JDBC_URL}
spring.datasource.username=${PGUSER}
spring.datasource.password=${PGPASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect

spring.ai.openai.api-key=${OPENAI_API_KEY}
spring.ai.openai.chat.options.model=gpt-4o-mini
```
Run backend:
```
./mvnw spring-boot:run
```
### Frontend (React)
```
npm install
npm run dev
# http://localhost:5173
```
API config → src/services/api.js
```
const API_BASE_URL = "http://localhost:8080/api/v1";
```
---
## 🧯 Troubleshooting

- 403 when creating cars: missing/invalid JWT; login and attach Authorization: Bearer (token) .

- No AI answer: check OPENAI_API_KEY, rate limits, or temporarily disable LLM (the rules layer still works).

- CORS errors: align @CrossOrigin with your actual frontend origin(s).

- Postgres on Render: always use sslmode=require in JDBC URL.