# CampusConnect

> A full-stack student portal built for campus collaboration, Q&A, lost-and-found reporting, claim handling, and admin moderation.

CampusConnect combines two major student workflows in one product:

- A **Q&A forum** inspired by StackOverflow
- A **Campus Lost & Found system** with claim approval flow

This repository contains:

- A **Spring Boot backend** with JWT authentication, role-based access, MySQL persistence, Swagger, and modular architecture
- A **React frontend** built with Vite, Tailwind CSS, Framer Motion, Axios, and React Router

---

# 1. Before You Do Anything: Full Windows Laptop Setup Guide

This section is written for someone who may be using this kind of project for the first time.

If you are on **Windows**, follow these steps in order.

## 1.1 What you need installed

Install these tools first:

| Tool | Why it is needed | Recommended version |
|---|---|---|
| **Git** | to clone/download the project | latest |
| **Java JDK** | to run the Spring Boot backend | **Java 17 or above** |
| **Node.js** | to run the React frontend | **Node 20 or above** |
| **npm** | comes with Node.js, used to install frontend packages | included with Node |
| **Docker Desktop** | easiest way to run MySQL without manual DB setup | latest |
| **VS Code** | easiest editor to open the project | latest |

## 1.2 How to install them

### Install Git

1. Open browser
2. Go to: `https://git-scm.com/download/win`
3. Download Git for Windows
4. Run installer
5. Keep default options unless you already know what to change

To check Git is installed:

```powershell
git --version
```

### Install Java JDK

1. Open browser
2. Go to a Java 17+ distribution such as:
   - `https://adoptium.net/`
3. Download **JDK 17** for Windows
4. Install it
5. During install, allow it to set PATH if asked

To check Java is installed:

```powershell
java -version
```

You should see Java 17 or newer.

### Install Node.js

1. Open browser
2. Go to: `https://nodejs.org/`
3. Download the **LTS** version
4. Install it
5. Keep default options

To check Node and npm:

```powershell
node -v
npm -v
```

### Install Docker Desktop

1. Open browser
2. Go to: `https://www.docker.com/products/docker-desktop/`
3. Download Docker Desktop for Windows
4. Install it
5. Open Docker Desktop after install
6. Wait until Docker says it is running

To check Docker:

```powershell
docker --version
docker compose version
```

### Install VS Code

1. Open browser
2. Go to: `https://code.visualstudio.com/`
3. Download and install VS Code

---

# 2. Get the Project on Your Laptop

## 2.1 Open a terminal in Windows

You can use:

- **PowerShell**
- **Command Prompt**
- **VS Code terminal**

PowerShell is recommended.

## 2.2 Choose a folder where you want the project

Example:

```powershell
cd $HOME\Desktop
```

## 2.3 Clone the project

If you have the Git repository URL:

```powershell
git clone <YOUR_REPOSITORY_URL>
cd CampusConnect
```

If you already downloaded the ZIP:

1. Right click ZIP
2. Extract All
3. Open the extracted folder in VS Code
4. Open terminal inside that folder

---

# 3. Understand the Folder Structure

## Root structure

```text
CampusConnect/
├── src/                  # Spring Boot backend source
├── frontend/             # React frontend source
├── docs/                 # supporting documents
├── docker-compose.yml    # MySQL container for local setup
├── pom.xml               # backend dependencies/build
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

# 4. The Easiest Local Setup

The easiest way to run CampusConnect locally is:

- MySQL through **Docker Desktop**
- Backend through **Maven + Spring Boot**
- Frontend through **Vite**

This avoids most laptop-specific issues.

---

# 5. Start the Database (MySQL)

## 5.1 Open terminal in project root

Make sure terminal is inside the root project folder:

```powershell
cd path\to\CampusConnect
```

## 5.2 Start MySQL using Docker

```powershell
docker compose up -d
```

This starts MySQL using [docker-compose.yml](./docker-compose.yml).

It creates:

- MySQL image: `mysql:8.4`
- container name: `campusconnect-mysql`
- database name: `campusconnect`
- root password: `password`
- port: `3306`

## 5.3 Check the container is running

```powershell
docker ps
```

You should see `campusconnect-mysql` in the list.

---

# 6. Run the Backend

## 6.1 Backend default behavior

The backend:

- runs on `http://localhost:8080`
- uses Spring Boot 3.3.5
- uses MySQL for persistence
- auto-creates tables with JPA/Hibernate
- exposes Swagger UI for API testing

## 6.2 Important database password note

In this project, [application.yml](./src/main/resources/application.yml) defaults to an empty DB password for some local setups.

If you are using **Docker Compose** from this repository, MySQL password is:

```text
password
```

So when using Docker MySQL, start backend like this:

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

## 6.3 If Maven wrapper does not run on Windows

Use:

```cmd
mvnw.cmd spring-boot:run
```

or in PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

## 6.4 Backend URLs

Once backend starts:

- App base: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui`
- OpenAPI docs: `http://localhost:8080/v3/api-docs`

## 6.5 Expected success message

You should eventually see something like:

```text
Tomcat started on port 8080
Started CampusConnectApplication
```

---

# 7. Run the Frontend

## 7.1 Open a new terminal

Keep backend terminal running.

Open a **second terminal**.

## 7.2 Move into frontend folder

```powershell
cd frontend
```

## 7.3 Install packages

Run this only the first time, or if dependencies changed:

```powershell
npm install
```

## 7.4 Start frontend

```powershell
npm run dev
```

Frontend runs at:

- `http://localhost:5173`

## 7.5 Local API behavior

For local development:

- if `VITE_API_BASE_URL` is **not set**, frontend uses `/api`
- Vite proxy forwards `/api`, `/v3`, and `/swagger-ui` to `http://localhost:8080`

That means local development works without changing anything.

---

# 8. Open the Website

After both servers are running:

1. Open browser
2. Go to:

```text
http://localhost:5173
```

You should see the CampusConnect frontend.

---

# 9. How to Stop the App

## Stop frontend

Go to frontend terminal and press:

```text
Ctrl + C
```

## Stop backend

Go to backend terminal and press:

```text
Ctrl + C
```

## Stop MySQL container

```powershell
docker compose down
```

If you also want to remove DB data:

```powershell
docker compose down -v
```

Be careful: `-v` deletes the MySQL volume data.

---

# 10. If It Does Not Run: Common Fixes

## Problem: Java command not found

Fix:

- reinstall JDK
- ensure Java is added to PATH
- open a fresh terminal

Check again:

```powershell
java -version
```

## Problem: npm command not found

Fix:

- reinstall Node.js
- open a fresh terminal

Check again:

```powershell
node -v
npm -v
```

## Problem: MySQL connection error

Most common reason: backend password does not match running DB.

If using Docker from this repo, use:

### PowerShell

```powershell
$env:DB_PASSWORD="password"
./mvnw spring-boot:run
```

## Problem: port 8080 already in use

Another app is already using backend port.

Fix the other app, or run backend on another port temporarily.

## Problem: port 5173 already in use

Another frontend/Vite process is already running.

Stop it, or let Vite choose another port.

## Problem: frontend shows network error

Check:

- backend terminal is still running
- backend is on port `8080`
- browser can open `http://localhost:8080/swagger-ui`

## Problem: demo users not working

Demo data depends on seed behavior and DB state.

If your DB already contains users, demo accounts may not be created automatically.

---

# 11. Tech Stack

## Backend

- **Java 17+**
- **Spring Boot 3.3.5**
- **Spring Security**
- **JWT (jjwt 0.11.5)**
- **Spring Data JPA**
- **MySQL**
- **Hibernate**
- **Bean Validation**
- **Lombok**
- **Swagger / OpenAPI**
- **H2** for tests

## Frontend

- **React 19**
- **Vite 8**
- **React Router**
- **Tailwind CSS**
- **Axios**
- **Framer Motion**
- **Lucide React**
- **React Hook Form**

---

# 12. What CampusConnect Does

CampusConnect is a campus-focused community platform with the following major modules.

## 12.1 Authentication and User Management

Users can:

- register an account
- log in with email and password
- receive a JWT token
- access protected routes using that token

Roles supported:

- `ROLE_STUDENT`
- `ROLE_ADMIN`

## 12.2 Q&A Forum

Students can:

- ask questions
- add descriptions
- attach tags
- search questions
- browse question list
- answer questions
- vote on answers
- accept one answer per question

The Q&A module also includes:

- answer counts
- accepted answer status
- score calculation
- reputation updates based on votes and accepted answers

## 12.3 Lost & Found

Students can:

- create LOST or FOUND posts
- add title, description, location, image URL, and incident date
- browse all posts publicly
- filter by type and status
- open post detail view

## 12.4 Claim System

For FOUND posts:

- another logged-in user can submit a claim request
- post owner can review claims
- owner can approve or reject claims
- approving a claim resolves the post
- other pending claims for the same post are automatically rejected

## 12.5 Admin Portal

Admins can:

- view dashboard statistics
- see all users
- activate or deactivate users
- see all questions
- inspect answers under a question
- delete any question
- delete any answer
- see all lost/found posts
- delete any lost/found post
- see all claims

---

# 13. Major Functional Flows

## 13.1 Authentication Flow

1. user registers
2. backend hashes password with BCrypt
3. backend stores user with role
4. user logs in
5. backend returns JWT
6. frontend stores token in `localStorage`
7. Axios sends `Authorization: Bearer <token>` automatically

## 13.2 Q&A Flow

1. user creates a question
2. tags are normalized and reused
3. another user answers the question
4. users can upvote/downvote answers
5. question owner can accept one answer
6. answer scores and reputation update

## 13.3 Lost & Found Claim Flow

1. user creates a FOUND post
2. another user submits a claim message
3. owner views claim list
4. owner approves or rejects a claim
5. if approved:
   - claim becomes `APPROVED`
   - post becomes `RESOLVED`
   - other pending claims become `REJECTED`

## 13.4 Admin Moderation Flow

1. admin logs in
2. admin opens admin dashboard
3. admin checks users/questions/posts/claims
4. admin deletes content if needed
5. dependent rows are cleaned safely

---

# 14. Backend Architecture

CampusConnect backend follows a layered, modular structure.

## 14.1 Layers

```text
Controller -> Service -> Repository -> Entity
```

### Controller layer

Responsible for:

- receiving HTTP requests
- validating request bodies and params
- returning `ApiResponse`

### Service layer

Responsible for:

- business rules
- permission checks
- workflows
- transactional operations

### Repository layer

Responsible for:

- database access
- JPA queries
- counts, fetches, deletes

### Entity layer

Responsible for:

- table mapping
- relationships
- persistence structure

## 14.2 Shared backend utilities

- [ApiResponse.java](./src/main/java/com/campusconnect/common/ApiResponse.java)
- [BaseEntity.java](./src/main/java/com/campusconnect/common/BaseEntity.java)
- [GlobalExceptionHandler.java](./src/main/java/com/campusconnect/common/GlobalExceptionHandler.java)

---

# 15. Security Model

## 15.1 Authentication

JWT is used for stateless authentication.

Key classes:

- [JwtService.java](./src/main/java/com/campusconnect/config/JwtService.java)
- [JwtAuthenticationFilter.java](./src/main/java/com/campusconnect/config/JwtAuthenticationFilter.java)
- [SecurityConfig.java](./src/main/java/com/campusconnect/config/SecurityConfig.java)

## 15.2 Password handling

- passwords are hashed using **BCrypt**
- password is not exposed in responses
- login only returns token + safe user data

## 15.3 Route access

### Public routes

- login/register
- question listing and question details
- lost/found listing and detail views
- Swagger docs

### Auth-only routes

- ask question
- answer question
- vote
- accept answer
- create lost/found post
- claim found item
- manage own claims on owned posts

### Admin-only routes

- all `/api/admin/**`

---

# 16. Core Data Model

## 16.1 User

Fields include:

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

## 16.2 Role

Supported values:

- `ROLE_STUDENT`
- `ROLE_ADMIN`

## 16.3 Question

Fields include:

- id
- title
- description
- user
- tags
- createdAt
- updatedAt

## 16.4 Answer

Fields include:

- id
- content
- question
- user
- isAccepted
- createdAt
- updatedAt

## 16.5 Tag

Fields include:

- id
- name

## 16.6 Vote

Fields include:

- id
- answer
- user
- voteType
- createdAt
- updatedAt

Rule:

- one user can vote only once per answer

## 16.7 LostFoundPost

Fields include:

- id
- user
- type
- title
- description
- imageUrl
- location
- dateOfIncident
- status
- createdAt
- updatedAt

## 16.8 ClaimRequest

Fields include:

- id
- post
- claimer
- message
- status
- createdAt
- updatedAt

Rule:

- one user can have only one **pending** claim per post

---

# 17. Frontend Architecture

The frontend is modular and page-driven.

## 17.1 Key folders

### `src/api/`

Contains centralized API wrappers:

- auth
- questions
- lost/found
- claims
- admin

### `src/context/`

Contains:

- auth context
- toast context

### `src/components/`

Contains reusable UI pieces:

- layout
- forms
- cards
- badges
- loaders
- modals
- admin layout

### `src/pages/`

Contains page-level screens:

- login/register
- dashboard/home
- Q&A pages
- lost & found pages
- admin pages

## 17.2 API handling strategy

All frontend API calls go through:

- [frontend/src/api/axios.js](./frontend/src/api/axios.js)

That file:

- sets the shared base URL
- injects JWT token automatically
- normalizes API wrapper behavior

---

# 18. Main Pages in the Frontend

## Public pages

- Login
- Register
- Questions list
- Question detail
- Lost & Found list
- Lost & Found detail

## Protected student pages

- Home dashboard
- Ask question
- Create lost/found post
- Claim item
- Vote on answers
- Accept answer (owner only)

## Protected admin pages

- Admin dashboard
- Admin users
- Admin questions
- Admin question detail
- Admin lost/found
- Admin claims

---

# 19. API Overview

This is not a full Swagger replacement, but a quick map.

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

For complete request/response exploration, use Swagger.

---

# 20. Validation and Safety Rules Built Into the App

## Question rules

- title required
- title length limits applied
- tags normalized:
  - trimmed
  - lowercased
  - duplicates removed
  - empty tags ignored

## Answer rules

- content required
- length limits applied
- answer order is consistent

## Voting rules

- cannot vote own answer
- one vote per user per answer
- same vote toggles off
- opposite vote switches type

## Accepted answer rules

- only question owner can accept
- only one accepted answer per question
- switching accepted answer updates state correctly

## Lost & Found rules

- title required
- location required
- date required
- blank description becomes `null`
- filtering and pagination are DB-backed

## Claim rules

- only FOUND posts can be claimed
- post must be OPEN
- owner cannot claim own post
- one pending claim per user per post
- approving a claim resolves the item

## Admin rules

- admin routes protected by role
- delete operations are transactional
- dependent child rows are cleaned before parent deletion where required

---

# 21. Demo Data

The project includes a demo seeder:

- [DemoDataSeeder.java](./src/main/java/com/campusconnect/config/DemoDataSeeder.java)

Current seed flag in [application.yml](./src/main/resources/application.yml):

```yml
app:
  demo:
    seed: true
```

## Seed behavior

- roles are always ensured by [DataSeeder.java](./src/main/java/com/campusconnect/config/DataSeeder.java)
- demo data seeding depends on current database state
- if DB already contains users, bulk demo content may be skipped

## Demo credentials

Password for demo users:

```text
password123
```

Possible demo accounts:

- `admin@campusconnect.com`
- `aarav@demo.com`
- `diya@demo.com`
- `kabir@demo.com`

If demo users do not work, reset DB and restart backend.

---

# 22. Database Reset Options

## Option A: remove container and DB volume

```powershell
docker compose down -v
docker compose up -d
```

## Option B: drop and recreate database manually

Inside MySQL:

```sql
DROP DATABASE campusconnect;
CREATE DATABASE campusconnect;
```

Then restart backend.

---

# 23. Tunnel Sharing

To share the app temporarily with a friend using tunnel URLs, see:

- [docs/SHARE_WITH_FRIEND.md](./docs/SHARE_WITH_FRIEND.md)

Frontend env example:

- [frontend/.env.example](./frontend/.env.example)

Important runtime variables:

- `VITE_API_BASE_URL`
- `APP_CORS_ALLOWED_ORIGINS`

---

# 24. Testing

## Backend tests

Run from project root:

```powershell
./mvnw test
```

These tests cover core flows including:

- auth
- Q&A
- voting and accepted answers
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

# 25. Useful Development Commands

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

---

# 26. Screens to Show When Demonstrating the App

A simple end-to-end app walkthrough can be:

1. Register or login
2. Open Home Dashboard
3. Browse Q&A questions
4. Open one question and show answers, score, accepted answer
5. Ask a new question
6. Open Lost & Found list
7. Open one FOUND post
8. Submit a claim request
9. Login as owner/admin and review moderation or claims
10. Open Admin Portal and show users/questions/posts/claims

---

# 27. Project Highlights

What makes this project complete:

- full authentication with JWT
- layered backend architecture
- DTO-based API design
- reusable frontend component structure
- admin role protection
- claim workflow state handling
- voting and reputation logic
- responsive UI
- tunnel-ready local sharing support

---

# 28. Important Files to Know

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

# 29. If You Want the Smoothest Local Experience

Use this exact sequence:

```powershell
cd path\to\CampusConnect
docker compose up -d
$env:DB_PASSWORD="password"
./mvnw spring-boot:run
```

Open second terminal:

```powershell
cd path\to\CampusConnect\frontend
npm install
npm run dev
```

Then open:

```text
http://localhost:5173
```

---

# 30. Final Notes

CampusConnect is built to be:

- modular
- demo-friendly
- role-aware
- API-first
- frontend-integrated
- locally runnable without cloud deployment

If you keep Docker running and use the commands in this README exactly, the project should be straightforward to start and explore.
