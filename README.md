Rent-A-Car Fullstack (Spring Boot + React + AI)




A modern car-rental platform with:

Spring Boot REST API + JWT auth

PostgreSQL + JPA/Hibernate

React SPA frontend

Lightweight AI Booking Assistant that answers common questions and can propose navigation actions (low token usage)

🌐 Live Demo

Backend (Render): https://full-stack-project-tjhf.onrender.com

Frontend (Netlify): https://bluerentcar.netlify.app/

Tip: Open the frontend link, use the floating chat widget, and try prompts like:

“What cars do you have?”

“Do you have a car at 150 RON/day?”

“Is BMW available between 2025-10-22 and 2025-10-25?”

“Calculate price for Audi RS6 from 2025-11-01 to 2025-11-05”

✨ Features

Browse, filter, sort cars (brand/model, transmission, fuel, price, year)

Upload/view car images

Create/update/cancel reservations

Admin endpoints for full reservation control

AI Booking Assistant:

Answers DB-backed questions without hallucinations (cheapest car, cars at/near a price, availability by brand & date)

For general questions, uses a small LLM (gpt-4-mini) to keep costs low

Can propose actions (e.g., “Open all cars?” → /cars) that the frontend turns into buttons

🧠 AI Assistant (API)

Endpoint: POST /api/ai/chat
Body:

{ "sessionId": "web", "message": "What cars do you have?" }


Response:

{
  "reply": "We have a wide range of cars (economy, SUV, luxury). Do you want me to open the page with all cars?",
  "actions": [
    { "type": "confirm_navigate", "to": "/cars", "label": "Yes, show all cars" },
    { "type": "dismiss", "to": "", "label": "No, stay in chat" }
  ]
}


Other supported queries (DB-first):

“What’s the cheapest car?”

“Do you have a car at 50 RON/day?” (if exact not found → suggests the closest cheaper/greater option)

“Is BMW available between YYYY-MM-DD and YYYY-MM-DD?”

“Calculate price for Audi RS6 from YYYY-MM-DD to YYYY-MM-DD” (price = days × price_per_day; checks availability; prefers exact brand+model, otherwise suggests closest by brand)

🚀 Local Development
Prereqs

Java 17+

Node 18+

PostgreSQL 14+

Maven

Backend (Spring Boot)
cd backend
# application.properties (local)
# --------------------------------
# server.port=8080
# spring.datasource.url=jdbc:postgresql://localhost:5432/postgres
# spring.datasource.username=postgres
# spring.datasource.password=admin
# spring.jpa.hibernate.ddl-auto=update
# spring.jpa.show-sql=true
# spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

mvn spring-boot:run


Key endpoints:

GET /api/v1/car — list cars

POST /api/v1/car — create car (multipart form, includes optional image)

GET /api/v1/car/{id} — get one

GET /api/v1/car/{id}/image — serve image bytes

POST /api/v1/reservations — create

GET /api/v1/reservations/my — user’s reservations

PATCH /api/v1/reservations/{id}/cancel — cancel

POST /api/ai/chat — AI chat

Frontend (React)
cd frontend
npm install
npm run dev


Configure API base URL: src/services/api.js

// For local dev:
const API_BASE_URL = 'http://localhost:8080/api/v1';

// AI base is derived automatically as /api/ai
// const AI_BASE_URL = API_BASE_URL.replace(/\/api\/v1$/, '/api/ai');

🧩 Frontend AI Widget (integrated)

The chat component:

Keeps a stable sessionId in localStorage

Calls POST /api/ai/chat

Renders reply as assistant message

If actions are present (e.g., confirm_navigate), it shows buttons.
When user presses “Yes, show all cars”, the frontend navigates to /cars.

🗄️ Database Schema
<details> <summary><b>Show DB Schema (Car, Reservation, User)</b></summary>
Car (table: masini)
Column	Type	Notes
id	BIGINT (PK)	sequence car_sequence
brand	VARCHAR	e.g., Audi
model	VARCHAR	e.g., RS6
year	INT	e.g., 2024
color	VARCHAR	
transmission	VARCHAR	Automată / Manuală
fuel_type	VARCHAR	Benzină / Motorină / Electrică…
price_per_day	INT	RON/day
emissions	REAL	CO₂ g/km
fuel_consumption	REAL	L/100km
image	BYTEA (LOB)	optional
imageName	VARCHAR	optional
imageType	VARCHAR	optional (e.g., image/jpeg)
Reservation (table: reservations)
Column	Type	Notes
id	BIGINT (PK)	
user_id	BIGINT (FK → _user.id)	Many-to-one User
car_id	BIGINT (FK → masini.id)	Many-to-one Car
start_at	TIMESTAMP	rent start (inclusive 00:00 if date)
end_at	TIMESTAMP	rent end (inclusive 23:59 if date)
status	VARCHAR	CONFIRMED (default), etc.
created_at	TIMESTAMP	auto
updated_at	TIMESTAMP	auto

Overlap rule is checked in queries (no intersecting reservations for the same car in the chosen window).

User (table: _user)
Column	Type	Notes
id	INT (PK)	
firstname	VARCHAR	
lastname	VARCHAR	
email	VARCHAR (unique)	used as username
password	VARCHAR (hashed)	
role	VARCHAR	e.g., USER, ADMIN
</details>
🔐 Auth (JWT)

Register: POST /api/v1/auth/register

Login: POST /api/v1/auth/authenticate

Token is stored client-side and attached as Authorization: Bearer <token>.

🛠️ Deployment Notes

Render (Backend):

Use a managed Postgres. Provide env vars:

JDBC_URL (or standard spring.datasource.url)

PGUSER, PGPASSWORD (or standard username/password)

If using JDBC_URL, map it to spring.datasource.url in application.properties:

spring.datasource.url=${JDBC_URL}
spring.datasource.username=${PGUSER}
spring.datasource.password=${PGPASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver


Netlify (Frontend):

Point API_BASE_URL to your Render backend base (…/api/v1).

🧪 Quick API Tests
# List cars
curl http://localhost:8080/api/v1/car

# AI: what cars do you have?
curl -X POST http://localhost:8080/api/ai/chat \
  -H "Content-Type: application/json" \
  -d '{"sessionId":"web","message":"What cars do you have?"}'