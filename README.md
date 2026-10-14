# Campus Fix - Backend

REST API for **Campus Fix: A Web-Based Campus Complaint Tracking System**.
Students report campus problems; admins assign and resolve them.

**Stack:** Node.js, Express, MongoDB (Mongoose), JWT auth, Multer (image upload)

## Setup

```bash
npm install
cp .env.example .env     # then edit values (MongoDB URI, JWT secret)
npm run dev              # http://localhost:5000
```

MongoDB: use local MongoDB or a free MongoDB Atlas cluster and paste its URI in `.env`.

## Creating an admin
Register with an extra `adminCode` field equal to `ADMIN_CODE` in `.env`
(the frontend has an optional "Admin code" box on the register page).

## API

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| POST | /api/auth/register | public | Create account |
| POST | /api/auth/login | public | Login, returns JWT |
| GET | /api/auth/me | user | Current user |
| GET | /api/complaints/categories | user | Category list |
| POST | /api/complaints | student | Submit complaint (multipart, optional `image`) |
| GET | /api/complaints/mine | student | My complaints |
| GET | /api/complaints | admin | All complaints (`?status=&category=`) |
| GET | /api/complaints/stats | admin | Counts by status |
| PATCH | /api/complaints/:id | admin | Update status / assignedTo / adminNote |
| DELETE | /api/complaints/:id | owner (Pending) / admin | Delete |

Send the token as `Authorization: Bearer <token>`.

## Folder structure

```
server.js          app entry, DB connection
models/            User, Complaint schemas
routes/            auth.js, complaints.js
middleware/        auth.js (JWT, admin check), upload.js (Multer)
uploads/           stored images
```

## Deploy (free)
Render / Railway for the API + MongoDB Atlas for the database.
Set the same variables from `.env.example` in the host's dashboard.
