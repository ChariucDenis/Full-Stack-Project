# 🚗 RentAI Car — Spring Boot + React (AI Booking Assistant)

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

## ⚙️ Tech Stack

- **Backend**: Spring Boot, Spring Security (JWT), Spring Data JPA (Hibernate), PostgreSQL, HikariCP  
- **AI**: Spring AI (with OpenAI), plus deterministic DB tools to avoid rate-limit & cost  
- **Frontend**: React 18, Axios, React Router  
- **Infra**: Render (Postgres + Java service), Netlify (static React), optional Docker

---

## 🚀 Quick Start

### 1) Backend

```bash
# in /backend
cp src/main/resources/application.properties.example src/main/resources/application.properties
# set your env (local)
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=postgres
export PGUSER=postgres
export PGPASSWORD=admin
export JDBC_URL="jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}"
# OpenAI (optional for LLM fallback)
export SPRING_AI_OPENAI_API_KEY=sk-...

./mvnw spring-boot:run
# app runs at http://localhost:8080