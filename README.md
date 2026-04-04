# 🎓 CampusConnect

<p align="center">
  <b>A full-stack student portal for campus collaboration, Q&A, lost-and-found reporting, claim handling, and admin moderation.</b>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Backend-Spring%20Boot%203.3.5-6DB33F?logo=springboot&logoColor=white" alt="Spring Boot" />
  <img src="https://img.shields.io/badge/Frontend-React%20%2B%20Vite-61DAFB?logo=react&logoColor=black" alt="React Vite" />
  <img src="https://img.shields.io/badge/Java-17%2B-orange?logo=openjdk&logoColor=white" alt="Java" />
  <img src="https://img.shields.io/badge/Database-MySQL%208-4479A1?logo=mysql&logoColor=white" alt="MySQL" />
  <img src="https://img.shields.io/badge/Auth-JWT-000000?logo=jsonwebtokens&logoColor=white" alt="JWT" />
  <img src="https://img.shields.io/badge/UI-Tailwind%20CSS-06B6D4?logo=tailwindcss&logoColor=white" alt="Tailwind" />
  <img src="https://img.shields.io/badge/Status-Feature%20Complete-success" alt="Status" />
</p>

---

## ✨ What Is CampusConnect?

CampusConnect is a campus-focused web application built as a complete student product. It brings together two high-value workflows in one platform:

- ❓ **Q&A Forum**: ask questions, answer questions, vote, accept answers, build reputation
- 🎒 **Lost & Found System**: report lost/found items, submit claims, review claims, resolve items
- 🛡️ **Admin Portal**: manage users, questions, answers, lost/found posts, and claims

This repository contains both:

- a **Spring Boot backend**
- a **React frontend**

---

## 📚 Table of Contents

- [1. First-Time Setup on Windows](#-1-first-time-setup-on-windows)
- [2. What You Need Installed](#-2-what-you-need-installed)
- [3. Download the Project](#-3-download-the-project)
- [4. Project Structure](#-4-project-structure)
- [5. Fastest Local Run Guide](#-5-fastest-local-run-guide)
- [6. Run MySQL with Docker](#-6-run-mysql-with-docker)
- [7. Run the Backend](#-7-run-the-backend)
- [8. Run the Frontend](#-8-run-the-frontend)
- [9. Open the App](#-9-open-the-app)
- [10. Demo Accounts](#-10-demo-accounts)
- [11. Main Features](#-11-main-features)
- [12. Application Flow](#-12-application-flow)
- [13. Backend Architecture](#-13-backend-architecture)
- [14. Frontend Architecture](#-14-frontend-architecture)
- [15. Security Model](#-15-security-model)
- [16. Data Model](#-16-data-model)
- [17. API Overview](#-17-api-overview)
- [18. Validation and Business Rules](#-18-validation-and-business-rules)
- [19. Admin Capabilities](#-19-admin-capabilities)
- [20. Tunnel Sharing](#-20-tunnel-sharing)
- [21. Testing](#-21-testing)
- [22. Common Errors and Fixes](#-22-common-errors-and-fixes)
- [23. Helpful Commands](#-23-helpful-commands)
- [24. Important Files to Study](#-24-important-files-to-study)

---

# 🪟 1. First-Time Setup on Windows

This section is written for someone who wants exact steps from zero.

If you are using a Windows laptop and you are not comfortable with development tools yet, follow this in order and do not skip steps.

## ✅ Goal

By the end of this section, you should be able to:

- install all required software
- open the project
- start database, backend, and frontend
- open the website in your browser

## 🧭 Basic idea

CampusConnect needs **three things running**:

1. **MySQL database**
2. **Spring Boot backend**
3. **React frontend**

Think of them like this:

- the **database** stores all data
- the **backend** handles business logic and APIs
- the **frontend** is the website you click and use

---

# 💻 2. What You Need Installed

Install these tools first.

| Tool | Why you need it | Recommended version |
|---|---|---|
| **Git** | to download and manage the project | latest |
| **Java JDK** | to run the backend | **17 or newer** |
| **Node.js** | to run the frontend | **20 or newer** |
| **npm** | comes with Node.js, installs frontend packages | included |
| **Docker Desktop** | easiest way to run MySQL locally | latest |
| **VS Code** | easiest editor to work in | latest |

## 2.1 Install Git

1. Open browser
2. Go to `https://git-scm.com/download/win`
3. Download Git for Windows
4. Install it using default options
5. Open a new terminal

Check installation:

```powershell
git --version
```

## 2.2 Install Java JDK

1. Open browser
2. Go to `https://adoptium.net/`
3. Download **JDK 17** for Windows
4. Install it
5. Allow PATH setup if installer asks

Check installation:

```powershell
java -version
```

You should see Java 17 or newer.

## 2.3 Install Node.js

1. Open browser
2. Go to `https://nodejs.org/`
3. Download the **LTS** version
4. Install it
5. Open a new terminal

Check installation:

```powershell
node -v
npm -v
```

## 2.4 Install Docker Desktop

1. Open browser
2. Go to `https://www.docker.com/products/docker-desktop/`
3. Download Docker Desktop for Windows
4. Install it
5. Open Docker Desktop
6. Wait until Docker shows it is running

Check installation:

```powershell
docker --version
docker compose version
```

## 2.5 Install VS Code

1. Open browser
2. Go to `https://code.visualstudio.com/`
3. Download and install VS Code

---

# 📥 3. Download the Project

## Option A: Clone with Git

Open terminal and go to a folder where you want the project.

Example:

```powershell
cd $HOME\Desktop
```

Then run:

```powershell
git clone <YOUR_REPOSITORY_URL>
cd CampusConnect
```

## Option B: Download ZIP

1. Download ZIP from GitHub
2. Extract it
3. Open the extracted folder in VS Code
4. Open terminal inside the project folder

---

# 🗂️ 4. Project Structure

## Root structure

```text
CampusConnect/
├── src/                  # Spring Boot backend source
├── frontend/             # React frontend source
├── docs/                 # extra documentation
├── docker-compose.yml    # MySQL container setup
├── pom.xml               # backend dependency + build file
├── mvnw / mvnw.cmd       # Maven wrapper
└── README.md             # this file
```

## Backend package structure

```text
com.campusconnect
├── admin
├── auth
├── common
├── config
├── enums
├── lostfound
├── qa
└── user
```

## Frontend structure

```text
frontend/src/
├── api/
├── components/
├── context/
├── hooks/
├── pages/
├── utils/
├── App.jsx
└── main.jsx
```

---

# 🚀 5. Fastest Local Run Guide

If you want the shortest possible path to a running app, use this exact order.

## Terminal 1: Project root

```powershell
cd path\to\CampusConnect
docker compose up -d
$env:DB_PASSWORD="password"
./mvnw spring-boot:run
```

## Terminal 2: Frontend

```powershell
cd path\to\CampusConnect\frontend
npm install
npm run dev
```

Then open:

```text
http://localhost:5173
```

If you want the full explanation instead of the short version, continue below.

---

# 🐬 6. Run MySQL with Docker

The easiest local database setup is Docker.

## 6.1 Start the database

From project root:

```powershell
docker compose up -d
```

This uses [docker-compose.yml](./docker-compose.yml).

It starts:

- MySQL image: `mysql:8.4`
- container: `campusconnect-mysql`
- database: `campusconnect`
- root password: `password`
- port: `3306`

## 6.2 Check that MySQL is running

```powershell
docker ps
```

You should see the MySQL container in the list.

## 6.3 Stop MySQL later

```powershell
docker compose down
```

If you also want to delete stored DB data:

```powershell
docker compose down -v
```

⚠️ `-v` removes the MySQL volume and deletes the database data.

---

# ☕ 7. Run the Backend

## 7.1 Backend tech summary

The backend uses:

- Java 17+
- Spring Boot 3.3.5
- Spring Security
- JWT authentication
- Spring Data JPA
- MySQL
- Swagger/OpenAPI

## 7.2 Important password note

The backend configuration in [application.yml](./src/main/resources/application.yml) defaults to an empty password for some laptop setups.

But **Docker Compose in this project uses password `password`**.

So for Docker-based local development, start backend like this.

### PowerShell

```powershell
$env:DB_PASSWORD="password"
./mvnw spring-boot:run
```

### Command Prompt

```cmd
set DB_PASSWORD=password
mvnw spring-boot:run
```

## 7.3 Windows Maven wrapper note

If `./mvnw` does not work in your shell, use:

```cmd
mvnw.cmd spring-boot:run
```

or:

```powershell
.\mvnw.cmd spring-boot:run
```

## 7.4 Backend URLs

Once backend is running:

- Base URL: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui`
- OpenAPI docs: `http://localhost:8080/v3/api-docs`

## 7.5 What success looks like

You should eventually see lines like:

```text
Tomcat started on port 8080
Started CampusConnectApplication
```

---

# ⚛️ 8. Run the Frontend

## 8.1 Open a second terminal

Keep backend terminal running.

## 8.2 Go to frontend folder

```powershell
cd frontend
```

## 8.3 Install dependencies

Run this at least once:

```powershell
npm install
```

## 8.4 Start development server

```powershell
npm run dev
```

Frontend runs at:

- `http://localhost:5173`

## 8.5 How frontend talks to backend

Frontend API handling is centralized in [frontend/src/api/axios.js](./frontend/src/api/axios.js).

Local behavior:

- if `VITE_API_BASE_URL` is **not set**, frontend uses `/api`
- Vite proxy forwards `/api`, `/v3`, and `/swagger-ui` to `http://localhost:8080`

So local development works without extra env setup.

---

# 🌐 9. Open the App

After backend and frontend are running, open this in your browser:

```text
http://localhost:5173
```

That is the main website.

Useful pages:

- Frontend app: `http://localhost:5173`
- Backend Swagger: `http://localhost:8080/swagger-ui`

---

# 👤 10. Demo Accounts

The project includes demo seeding through [DemoDataSeeder.java](./src/main/java/com/campusconnect/config/DemoDataSeeder.java).

Current seed flag in [application.yml](./src/main/resources/application.yml):

```yml
app:
  demo:
    seed: true
```

## Demo password

```text
password123
```

## Demo users that may be available

- `admin@campusconnect.com`
- `aarav@demo.com`
- `diya@demo.com`
- `kabir@demo.com`

## Important note

If the database already contains users, bulk demo data may be skipped depending on current seed behavior.

If demo accounts do not work, the cleanest reset is:

```powershell
docker compose down -v
docker compose up -d
$env:DB_PASSWORD="password"
./mvnw spring-boot:run
```

---

# 🌟 11. Main Features

## 🔐 Authentication

- register account
- login with email and password
- JWT-based stateless authentication
- current user endpoint
- role support:
  - `ROLE_STUDENT`
  - `ROLE_ADMIN`

## ❓ Q&A Forum

- create questions
- add description and tags
- list questions
- search by title keyword
- answer questions
- vote on answers
- accept one answer per question
- reputation updates

## 🎒 Lost & Found

- create LOST or FOUND post
- add title, description, image URL, location, incident date
- public list page
- public detail page
- filter by type and status

## 📨 Claim Workflow

- claim a FOUND item with a message
- post owner can review claims
- approve or reject claim
- approval resolves item
- other pending claims are auto-rejected

## 🛡️ Admin Portal

- dashboard statistics
- user listing
- activate/deactivate users
- question moderation
- answer moderation
- lost/found moderation
- claims overview

---

# 🔄 12. Application Flow

## 12.1 Authentication flow

1. user registers
2. backend hashes password using BCrypt
3. backend saves user + role
4. user logs in
5. backend returns JWT token
6. frontend stores token in `localStorage`
7. Axios automatically sends `Authorization: Bearer <token>`

## 12.2 Q&A flow

1. user creates a question
2. tags are normalized and reused
3. other users answer it
4. users vote on answers
5. question owner accepts best answer
6. reputation updates based on actions

## 12.3 Lost & Found flow

1. user creates LOST or FOUND post
2. public users browse posts
3. another logged-in user can claim a FOUND post
4. owner reviews the claims
5. owner approves or rejects
6. approved claim resolves the post

## 12.4 Admin moderation flow

1. admin logs in
2. opens admin dashboard
3. views users/questions/posts/claims
4. deletes or manages records when needed
5. cleanup is handled safely and transactionally

---

# 🧱 13. Backend Architecture

CampusConnect backend follows a layered modular structure.

## 13.1 Main flow

```text
Controller -> Service -> Repository -> Entity
```

## 13.2 Layer responsibilities

### Controller

- accepts requests
- validates request bodies and params
- returns `ApiResponse`

### Service

- business logic
- permission checks
- workflow handling
- transactional operations

### Repository

- database access
- custom queries
- count/find/delete operations

### Entity

- database table mapping
- relationships
- persistence structure

## 13.3 Shared backend utilities

- [ApiResponse.java](./src/main/java/com/campusconnect/common/ApiResponse.java)
- [BaseEntity.java](./src/main/java/com/campusconnect/common/BaseEntity.java)
- [GlobalExceptionHandler.java](./src/main/java/com/campusconnect/common/GlobalExceptionHandler.java)

---

# 🎨 14. Frontend Architecture

The frontend is page-driven, modular, and built around reusable UI components.

## 14.1 Main folders

### `frontend/src/api/`

Contains all API wrappers:

- auth
- questions
- lost/found
- claims
- admin

### `frontend/src/context/`

Contains app-wide state:

- auth context
- toast context

### `frontend/src/components/`

Contains reusable UI pieces:

- buttons
- inputs
- badges
- loaders
- modals
- cards
- layouts
- admin layout

### `frontend/src/pages/`

Contains page-level screens:

- auth pages
- Q&A pages
- lost/found pages
- admin pages

## 14.2 API strategy

Frontend API calls are centralized through:

- [frontend/src/api/axios.js](./frontend/src/api/axios.js)

That file:

- defines shared API base handling
- injects JWT token automatically
- normalizes backend response usage

---

# 🔒 15. Security Model

## 15.1 Authentication

JWT is used for stateless authentication.

Key files:

- [JwtService.java](./src/main/java/com/campusconnect/config/JwtService.java)
- [JwtAuthenticationFilter.java](./src/main/java/com/campusconnect/config/JwtAuthenticationFilter.java)
- [SecurityConfig.java](./src/main/java/com/campusconnect/config/SecurityConfig.java)

## 15.2 Password handling

- password is hashed with BCrypt
- password is not returned in safe DTO responses
- users must be active to log in

## 15.3 Route access summary

### Public

- register/login
- question listing and detail
- lost/found listing and detail
- Swagger docs

### Auth-only

- create question
- answer question
- vote
- accept answer
- create lost/found post
- submit claim
- owner claim management

### Admin-only

- all `/api/admin/**`

---

# 🗃️ 16. Data Model

## User

Includes:

- id
- fullName
- email
- password
- course
- yearOfStudy
- reputationPoints
- isActive
- createdAt
- updatedAt
- roles

## Role

Supported values:

- `ROLE_STUDENT`
- `ROLE_ADMIN`

## Question

Includes:

- title
- description
- user
- tags
- createdAt
- updatedAt

## Answer

Includes:

- content
- question
- user
- isAccepted
- createdAt
- updatedAt

## Tag

Includes:

- name

## Vote

Includes:

- answer
- user
- voteType

Rule:

- one vote per user per answer

## LostFoundPost

Includes:

- type
- title
- description
- imageUrl
- location
- dateOfIncident
- status

## ClaimRequest

Includes:

- post
- claimer
- message
- status

Rule:

- one pending claim per user per post

---

# 📡 17. API Overview

Use Swagger for full request/response details. This section is the quick map.

## Auth

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/me`

## Questions

- `GET /api/questions`
- `GET /api/questions/search`
- `GET /api/questions/{id}`
- `POST /api/questions`
- `POST /api/questions/{id}/answers`
- `GET /api/questions/{id}/answers`

## Answer actions

- `POST /api/answers/{id}/vote`
- `POST /api/answers/{id}/accept`

## Lost & Found

- `GET /api/lost-found`
- `GET /api/lost-found/{id}`
- `POST /api/lost-found`

## Claims

- `POST /api/lost-found/{id}/claim`
- `GET /api/lost-found/{id}/claims`
- `POST /api/claims/{id}/approve`
- `POST /api/claims/{id}/reject`

## Admin

- `GET /api/admin/stats`
- `GET /api/admin/users`
- `PATCH /api/admin/users/{id}/activate`
- `PATCH /api/admin/users/{id}/deactivate`
- `GET /api/admin/questions`
- `GET /api/admin/questions/{id}`
- `GET /api/admin/questions/{id}/answers`
- `DELETE /api/admin/questions/{id}`
- `DELETE /api/admin/answers/{id}`
- `GET /api/admin/lost-found`
- `GET /api/admin/lost-found/{id}`
- `DELETE /api/admin/lost-found/{id}`
- `GET /api/admin/claims`

---

# ✅ 18. Validation and Business Rules

## Question rules

- title required
- title length limits enforced
- tags normalized:
  - trim
  - lowercase
  - duplicate removal
  - empty ignore

## Answer rules

- content required
- practical length limits enforced
- consistent answer ordering

## Voting rules

- cannot vote on own answer
- one vote per answer per user
- same vote toggles off
- opposite vote changes vote type

## Accepted answer rules

- only question owner can accept
- only one accepted answer per question
- switching accepted answer keeps question state valid

## Lost & Found rules

- title required
- location required
- incident date required
- blank description stored as `null`
- filtering and pagination are DB-backed

## Claim rules

- only FOUND posts can be claimed
- post must be OPEN
- owner cannot claim own post
- one pending claim per user per post
- approval resolves post and rejects other pending claims

## Admin rules

- admin routes require admin role
- deletes are transactional
- dependent data cleanup is handled safely

---

# 🧑‍💼 19. Admin Capabilities

Admin users can:

- view platform stats
- see all users
- activate/deactivate users
- see all questions
- inspect answers for a question
- delete any question
- delete any answer
- see all lost/found posts
- delete any lost/found post
- see all claims

Admin frontend pages:

- dashboard
- users
- questions
- question detail
- lost/found
- claims

---

# 🌍 20. Tunnel Sharing

If you want to share the app temporarily without deploying, use tunnel URLs.

See:

- [docs/SHARE_WITH_FRIEND.md](./docs/SHARE_WITH_FRIEND.md)

Environment variables involved:

- `VITE_API_BASE_URL`
- `APP_CORS_ALLOWED_ORIGINS`

Frontend env example:

- [frontend/.env.example](./frontend/.env.example)

---

# 🧪 21. Testing

## Backend tests

From project root:

```powershell
./mvnw test
```

Coverage includes:

- auth
- Q&A
- voting
- accepted answers
- lost/found
- claims
- admin moderation

## Frontend checks

```powershell
cd frontend
npm run lint
npm run build
```

---

# 🛠️ 22. Common Errors and Fixes

## Java not found

Fix:

- reinstall JDK
- reopen terminal
- confirm PATH is set

Check:

```powershell
java -version
```

## npm not found

Fix:

- reinstall Node.js
- reopen terminal

Check:

```powershell
node -v
npm -v
```

## MySQL connection fails

Most common reason: password mismatch.

If using Docker from this repo, run backend with:

```powershell
$env:DB_PASSWORD="password"
./mvnw spring-boot:run
```

## Port 8080 busy

Another backend/app is using that port.

Stop the other app or change port temporarily.

## Port 5173 busy

Another Vite process is already running.

Stop it or let Vite choose another free port.

## Frontend network error

Check:

- backend terminal is still running
- browser opens `http://localhost:8080/swagger-ui`
- frontend is running on `http://localhost:5173`

## Demo account not working

Reason is usually current DB contents and seed state.

Best clean reset:

```powershell
docker compose down -v
docker compose up -d
$env:DB_PASSWORD="password"
./mvnw spring-boot:run
```

---

# ⌨️ 23. Helpful Commands

## Backend

Run app:

```powershell
./mvnw spring-boot:run
```

Run tests:

```powershell
./mvnw test
```

## Frontend

Install packages:

```powershell
cd frontend
npm install
```

Start dev server:

```powershell
npm run dev
```

Lint:

```powershell
npm run lint
```

Build:

```powershell
npm run build
```

Preview production build:

```powershell
npm run preview
```

## Docker

Start DB:

```powershell
docker compose up -d
```

Stop DB:

```powershell
docker compose down
```

Reset DB:

```powershell
docker compose down -v
docker compose up -d
```

---

# 📘 24. Important Files to Study

## Backend

- [SecurityConfig.java](./src/main/java/com/campusconnect/config/SecurityConfig.java)
- [JwtService.java](./src/main/java/com/campusconnect/config/JwtService.java)
- [AuthService.java](./src/main/java/com/campusconnect/auth/service/AuthService.java)
- [QuestionService.java](./src/main/java/com/campusconnect/qa/service/QuestionService.java)
- [AnswerService.java](./src/main/java/com/campusconnect/qa/service/AnswerService.java)
- [VoteService.java](./src/main/java/com/campusconnect/qa/service/VoteService.java)
- [LostFoundService.java](./src/main/java/com/campusconnect/lostfound/service/LostFoundService.java)
- [ClaimService.java](./src/main/java/com/campusconnect/lostfound/service/ClaimService.java)
- [AdminService.java](./src/main/java/com/campusconnect/admin/service/AdminService.java)

## Frontend

- [frontend/src/api/axios.js](./frontend/src/api/axios.js)
- [frontend/src/context/AuthContext.jsx](./frontend/src/context/AuthContext.jsx)
- [frontend/src/App.jsx](./frontend/src/App.jsx)
- [frontend/src/components/layout/Navbar.jsx](./frontend/src/components/layout/Navbar.jsx)
- [frontend/src/pages/HomePage.jsx](./frontend/src/pages/HomePage.jsx)
- [frontend/src/pages/QuestionDetailPage.jsx](./frontend/src/pages/QuestionDetailPage.jsx)
- [frontend/src/pages/LostFoundDetailPage.jsx](./frontend/src/pages/LostFoundDetailPage.jsx)
- [frontend/src/pages/AdminDashboardPage.jsx](./frontend/src/pages/AdminDashboardPage.jsx)

---

## 🎉 Final Note

CampusConnect is designed to be:

- modular
- full-stack
- role-aware
- demo-ready
- locally runnable
- easy to extend in future

If you follow this README step by step, you should be able to run, explore, and understand the project comfortably.
