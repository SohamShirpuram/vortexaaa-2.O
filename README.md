# GovFix — Smart & Accountable Civic Services for PCMC

> **Hackathon Prototype Note:** GovFix is an independent prototype inspired by municipal civic services in Pimpri Chinchwad (<https://www.pcmcindia.gov.in/>). It uses fictional demo datasets, simulated SLA deadlines, and simulated vehicle GPS coordinates for judging and demonstration purposes.

---

## 1. Project Overview & Architecture

**GovFix** is a configurable civic grievance redressal and workflow management platform built for municipal corporations. It replaces hardcoded complaint lifecycles with a database-backed state-machine engine, enforces strict role-based access control, mandates photographic/inspection evidence for resolution, automatically escalates breached SLAs to supervisors, maintains an append-only audit trail, and features live GPS tracking for municipal garbage collection vehicles.

### Architecture

```text
       Citizen Portal / Web Frontend (Vanilla JS + HTML5 + CSS3 + Leaflet Maps)
                                    |
                            HTTP / REST APIs
                     (with X-GovFix-Role Auth Headers)
                                    v
     ==================== Spring Boot 3 / Java 21 ====================
     |  • Spring Security (Role Authority: CITIZEN, OFFICER, etc.)   |
     |  • WorkflowEngineService (State-Machine Transitions)          |
     |  • EscalationService (SLA Deadlines & Time Advance Sim)       |
     |  • AIRecommendationService (Rule-Based NLP + Human Approval)  |
     |  • GarbageVanTrackingService (GPS Route & ETA Engine)         |
     |  • AuditService (@Transactional(REQUIRES_NEW) Trail)         |
     ================================================================
                                    |
                        Spring Data JPA / Hibernate
                                    v
            MySQL 8 Database (Persistent Storage) / H2 Fallback
```

---

## 2. Technology Stack

- **Backend:** Java 21, Spring Boot 3.3.4, Spring Web, Spring Data JPA / Hibernate, Spring Security, Maven
- **Frontend:** HTML5, CSS3, Vanilla JavaScript (ES6+ Fetch API), Leaflet + OpenStreetMap (Live Vehicle GPS), Lucide/Inline SVG Icons. Directly served from Spring Boot's `src/main/resources/static/`.
- **Database:** MySQL 8 with persistent storage, proper foreign keys, constraints, and indexes. *(Includes persistent file fallback for immediate zero-config demo runs)*.

---

## 3. Demo User Accounts & Roles

GovFix includes a **Simulated Role Selector** in the top navigation bar for evaluating role permissions:

| Username | Role | Full Name & Department | Permitted Capabilities |
|---|---|---|---|
| `citizen_rahul` | `CITIZEN` | Rahul Deshmukh | Register grievances, track ticket timelines, submit 1-5 star feedback, request reopening |
| `officer_sharma` | `OFFICER` | Amit Sharma (JE Road Dept) | Manage assigned cases, record site inspections, upload evidence photos, progress workflow stages |
| `officer_patil` | `OFFICER` | Sunita Patil (Health Inspector) | Handle sanitation and waste collection grievances |
| `supervisor_kulkarni`| `SUPERVISOR` | Dr. Anand Kulkarni (Exec. Eng) | Monitor overdue & escalated cases, reassign field officers, approve resolution quality |
| `admin_govfix` | `ADMIN` | System Administrator | Configure workflows, states, transitions, publish new municipal services |
| `auditor_joshi` | `AUDITOR` | Vikas Joshi (Chief Auditor) | View append-only audit history of successful and rejected actions |

---

## 4. Setup & Running Instructions

### Prerequisites
- **Java 21 JDK** installed
- **Apache Maven 3.9+** (or included Maven binary)
- **MySQL 8** (Optional: GovFix runs out of the box with file persistence if MySQL is not running)

### Option A: Running with Local MySQL 8
1. Create the MySQL database and run the schema and seed scripts:
   ```bash
   mysql -u root -p < database/schema.sql
   mysql -u root -p < database/seed.sql
   ```
2. Configure environment variables (or copy `.env.example` to `.env`):
   ```bash
   export DB_URL="jdbc:mysql://localhost:3306/govfix?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
   export DB_USERNAME="root"
   export DB_PASSWORD="your_password"
   ```
3. Build and launch:
   ```bash
   mvn clean package -DskipTests
   java -jar target/govfix-1.0.0.jar
   ```

### Option B: Immediate Zero-Config Launch (Out of the Box)
GovFix includes automatic persistent storage in `./data/govfix` in MySQL mode. Simply run:
```bash
mvn package -DskipTests
java -jar target/govfix-1.0.0.jar
```
Open your browser at: **`http://localhost:8080/`**

---

## 5. Five-Minute Judging Demonstration Script

Follow this step-by-step sequence to verify all prompt requirements:

1. **Dashboard & KPIs:**
   - Open `http://localhost:8080/` and click the **Dashboard & KPIs** tab.
   - Observe real-time KPI counters: Total, Pending, Assigned, In-Progress, Resolved, Overdue, and Escalated cases with category volume distribution.

2. **Register a Fictional Pothole Grievance:**
   - Switch active role to `CITIZEN` in the top bar.
   - Go to **Citizen Portal**. Fill the form: Title: *"Deep pothole near Akurdi Station gate"*, Locality: *Akurdi*.
   - Click **"Auto-Categorize with AI"** — notice the instant recommendation for *"Roads and potholes"*, *"HIGH"* priority, and explanation.
   - Submit the grievance. Note the generated ticket ID (e.g., `GV-1019`).
   - Paste the ticket ID into **Track Existing Grievance** to see the interactive lifecycle timeline.

3. **Officer Inspection & Evidence:**
   - Switch role to `OFFICER (Amit Sharma)` in the top role selector.
   - Go to **Officer Workspace** or **Dashboard**, open the complaint details for `GV-1019`.
   - Transition ticket: **Initiate Review → Under Review**.
   - Next, transition to **Assigned** (assigned by Supervisor) and **Inspection Required**.

4. **Demonstrate State Machine Rejection & Audit Trail Persistence:**
   - While in `Inspection Required`, click **"Demo: Attempt Invalid Transition"** (or attempt to skip directly to `Closed`).
   - Notice the prompt alert: **`Workflow Rejection: Illegal transition...`**.
   - Switch role to `AUDITOR` and open the **Audit Logs** tab.
   - Notice the top entry in **RED**: Outcome: `REJECTED`, Actor: `officer_sharma`, with the full diagnostic failure reason.
   - *Technical highlight:* The audit event survived the failed transaction via Spring's `@Transactional(propagation = Propagation.REQUIRES_NEW)`.

5. **Evidence-Backed Resolution:**
   - Switch back to `OFFICER`. Attach site evidence (photo or inspection notes).
   - Advance stage to **In Progress**, then **Resolution Submitted**.
   - Switch role to `SUPERVISOR` and click **Approve Quality & Mark Resolved**.
   - Switch to `CITIZEN` to see the resolved ticket and submit a 5-star citizen satisfaction rating.

6. **Smart Garbage Collection Van Live Tracking (Featured PCMC Module):**
   - Click the **Garbage Van Live Tracking** tab.
   - View garbage trucks moving along real PCMC routes (Nigdi-Pradhikaran, Wakad-Hinjawadi, Bhosari-MIDC).
   - Observe live truck icons, route polylines, numbered stops, and dynamic ETA countdowns.
   - Click **"Step Movement (Tick)"** or **"Start Auto-Tracking"** to see coordinates live update.
   - Click **"Report Missed Pickup"** — submit a report to generate an immediate Waste Management grievance.

7. **Deadline Simulation & Automatic Escalation:**
   - Click the header button: **"Simulate Time Advance (+24h)"**.
   - Open **Supervisor & Escalations** tab.
   - Notice open complaints breach SLA and are automatically flagged as **🚨 Escalated**.
   - Observe duplicate escalations are prevented on subsequent runs.

8. **Workflow Builder & Multi-Workflow Support:**
   - Open **Workflow Builder** to demonstrate the second configured workflow for **Water Supply & Contamination**.

9. **Reset Demo State:**
   - Click **"Reset Demo"** in the top bar to restore all data to the pristine seed state for repeated judging runs.

---

## 6. Automated Test Results

GovFix has a comprehensive JUnit 5 test suite covering all core business logic:

```text
[INFO] Running com.govfix.EscalationAndAiTests
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0 -- in com.govfix.EscalationAndAiTests
[INFO] Running com.govfix.WorkflowEngineTests
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0 -- in com.govfix.WorkflowEngineTests
[INFO] 
[INFO] Results:
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

## 7. Known Limitations & Production Roadmap

- **GPS Source:** Live vehicle tracking in this hackathon prototype uses high-fidelity mathematical route interpolation across Pimpri Chinchwad coordinates. A production deployment would bind to real AIS-140 PCMC telematics GPS feeds.
- **AI Integration:** The AI Assistant includes an accurate built-in rule-based NLP classifier that operates out of the box with zero external dependencies. When `GEMINI_API_KEY` is provided in `.env`, it connects to Google Gemini for generative explanations.
- **SMS / Push Alerts:** Citizen notifications are rendered via interactive in-app timelines; production integration would plug into PCMC's SMS gateway.

