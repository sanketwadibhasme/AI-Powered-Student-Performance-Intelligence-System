# Running the V0 Stack

Three processes are needed: MySQL (service), the Python AI service, and the Spring Boot backend. The frontend is a static SPA (`frontend/index.html`) opened in a browser.

**Start in this order** — MySQL first (backend needs it), then Flask (so the first AI call succeeds), then Spring Boot:

```
1. MySQL (service)          → port 3306
2. Flask AI service         → port 5001
3. Spring Boot backend      → port 8080
4. Frontend (static files)
```

## 1. MySQL
Must be running with database `student_performance_db` (config in `src/main/resources/application.properties`).

## 2. Python AI service (first time: train models)

```bash
# one-time setup
python -m venv ml/.venv
ml/.venv/Scripts/python.exe -m pip install pandas scikit-learn joblib flask

# train the two models (creates ml/models/*.joblib)
ml/.venv/Scripts/python.exe ml/train_models.py

# start the inference service (port 5001)
ml/.venv/Scripts/python.exe ml/app.py
```

Verify: `curl http://127.0.0.1:5001/health` → `{"status": "UP", "models": {"performance": true, "risk": true}}`

**If the AI service is down, everything else still works.** Predictions return a clear
"AI Service Offline" panel (HTTP 503) and the UI health pill shows `○ AI Offline`.
The backend proxies service health at `GET /api/ai/health`.

## 3. Spring Boot backend (port 8080)

```bash
mvn spring-boot:run
```

Swagger: http://localhost:8080/swagger-ui.html

## 4. Frontend

Open `frontend/login.html` in a browser (or serve the `frontend/` folder with any static server).
After login every role lands on `frontend/index.html` — a single-page app shell with a role-based sidebar.

> If you open `index.html` directly from the filesystem (file://), modern browsers may block module-style
> script loading; using any static server (e.g. `npx serve frontend` or the VS Code Live Server extension)
> is the reliable option.

## Demo accounts

| username  | password    | role    |
|-----------|-------------|---------|
| admin     | admin123    | ADMIN   |
| faculty1  | faculty123  | FACULTY |
| student1  | student123  | STUDENT (linked to studentId 2) |

`student1` is linked to student row 2 via `app_users.student_id`. To link other student accounts, use
**Users** in the admin UI (Edit → Linked Student Record) or set `app_users.student_id` directly in SQL.

A FACULTY account is matched to its faculty record **by email** (e.g. `faculty1` must use the same email
as a `faculties` row) — this powers the faculty "My subjects" workflow.

## Demo data

`ml/seed_demo.sql` idempotently seeds demo users, extra students, marks and attendance:

```bash
mysql -uroot -psanket < ml/seed_demo.sql
```

## Daily workflows by role

- **FACULTY:** Dashboard shows *my subjects* (from `GET /api/faculties/me/subjects`) with one-click
  Attendance / Marks / Class shortcuts → Take Attendance records the **enrolled** class for a subject+date
  in one bulk save (duplicates skipped, history editable for corrections) → Class Marks Entry prefills
  existing records for subject+exam (edits instead of duplicating) → flagged students are listed on the
  dashboard with direct Profile / Analyze actions.
- **STUDENT:** Dashboard → marks / attendance / upcoming exams / trend → My AI Insights runs automatically
  on real data, with data-quality info and an optional what-if simulation.
- **ADMIN:** Structure management (departments, faculty, users) plus the same analytics and AI as faculty.

## Key API surface for V0

- `POST /api/auth/login` → `{token, role, username}`
- `GET /api/auth/me` → `{username, role, studentId}`
- `GET /api/students/page?page=0&size=20&sort=...` (also `/api/marks/page`, `/api/attendance/page`, `/api/users/page`)
- `GET /api/students/me/profile|marks|attendance|average-marks|attendance-percentage|enrollments` (STUDENT, JWT-resolved)
- `GET /api/faculties/me/subjects` (FACULTY, JWT-resolved)
- `POST /api/attendance` + `POST /api/attendance/bulk` (bulk session save with duplicate skip)
- AI (JWT-protected, see `docs/ML_FEATURE_MAPPING.md`):
  - `GET /api/ai/health` → ML service status for the UI pill
  - `POST /api/ai/students/{id}/insights` (ADMIN/FACULTY) — empty body = real data; feature values = what-if
  - `POST /api/ai/students/{id}/simulate` (ADMIN/FACULTY) — explicit what-if
  - `POST /api/ai/me/insights` (STUDENT, JWT-resolved)

## Backend changes made during the V0 restructuring (kept minimal)

1. `AppUser` — transient `studentId` field so the admin Users UI can link/unlink student accounts through
   the existing `PUT /api/users/{id}` (no schema change; `0`/`null` = unlink).
2. `AppUserServiceImpl.updateUser` — password re-encoded only when a new one is supplied (previously
   `encode(null)` broke every edit that didn't resend a password).
3. `StudentSelfController` — added `GET /api/students/me/enrollments` so students can see their subjects
   (already covered by the `hasRole("STUDENT")` rule for `/api/students/me/**`).
4. `AuthenticationController./api/auth/me` — `Map.of` NPE fix for staff accounts (null studentId).
5. `AppUser` — transient `facultyId` resolved at login by email match (no schema change), enabling
   `GET /api/faculties/me/subjects`.
6. AI pipeline — chronological G1/G2 from real marks, targeted queries instead of `findAll()`,
   data-sufficiency metadata, `/api/ai/health` proxy, distinct 502/503/422 error semantics (see
   `docs/ML_FEATURE_MAPPING.md`).
7. Duplicate guards — attendance (student+subject+date, 409) and marks (student+subject+exam, 409),
   plus `POST /api/attendance/bulk` transactional session save.
