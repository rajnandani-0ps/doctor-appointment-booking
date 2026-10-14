# 🏥 DocCare - Doctor Appointment Booking System

> A full-stack healthcare web application built with **Java 21**, **Spring Boot 3.4**, **Spring Security (JWT)**, **Spring Data JPA**, **H2 / MySQL**, and a modern, responsive frontend.

---

## 🌟 Key Features

### 👤 Patient Features
- **Browse & Search Doctors**: Filter by specialization (Cardiology, Dermatology, Pediatrics, Orthopedics, General Medicine), doctor name, or clinic location.
- **Doctor Profiles & Reviews**: View qualifications, consultation fees, experience, ratings, and clinics.
- **Interactive Time-Slot Booking**: Select available appointment slots for any chosen date in real-time.
- **Appointment Management**:
  - Track appointment statuses (`CONFIRMED`, `PENDING`, `COMPLETED`, `CANCELLED`).
  - Cancel upcoming appointments.
- **Digital Prescriptions**: View and print doctor's prescriptions and clinical advice after consultation.
- **Health Profile**: Store date of birth, blood group, emergency contact, and medical history / known allergies.

### 🩺 Doctor Features
- **Doctor Dashboard & Analytics**:
  - Live metrics: Total appointments, pending requests, confirmed visits, and completed consultations.
- **Appointment Approval & Status Updates**:
  - Accept or reject incoming patient appointment requests.
  - Complete consultations and record clinical observations.
  - Issue structured digital prescriptions with dosage and instructions.
- **Custom Availability Slots**:
  - Generate and configure custom availability slots (date, start time, end time).
  - Automatically prevents overlapping bookings.
- **Clinic Profile Settings**:
  - Update consultation fee, bio, clinic address, phone number, and avatar.

### 🔐 Authentication & Security
- **Role-Based Access Control (RBAC)**: Secure separation between `ROLE_PATIENT` and `ROLE_DOCTOR`.
- **JWT Authentication**: Stateless token-based security via JJWT.
- **Password Encryption**: Industry-standard BCrypt hashing.
- **CORS Configured**: Ready for separate frontend hosting or unified serving.

---

## 📁 Repository Structure

```text
doctor-appointment-booking/
├── .gitignore                      # Git ignore rules for Java, Maven, IDEs
├── README.md                       # Complete setup and API documentation
│
├── backend/                        # Spring Boot 3.4 REST API
│   ├── pom.xml                     # Maven project descriptor
│   ├── mvnw / mvnw.cmd             # Maven Wrapper (no local Maven install needed!)
│   └── src/
│       ├── main/
│       │   ├── java/com/doctorapp/
│       │   │   ├── DoctorAppointmentBackendApplication.java
│       │   │   ├── config/         # Security, CORS, and DataInitializer
│       │   │   ├── controller/     # Auth, Doctor, Appointment, Patient REST controllers
│       │   │   ├── dto/            # Data Transfer Objects & API Responses
│       │   │   ├── entity/         # User, DoctorProfile, PatientProfile, Appointment, AvailabilitySlot
│       │   │   ├── repository/     # Spring Data JPA repositories
│       │   │   ├── security/       # JWT Provider, Auth Filter, UserDetailsService
│       │   │   └── service/        # Business logic services
│       │   └── resources/
│       │       ├── application.properties # H2 / MySQL configuration
│       │       └── static/         # Frontend files served automatically by Spring Boot
│       └── test/
│
└── frontend/                       # Standalone Frontend (works with Live Server or any browser)
    ├── index.html                  # Landing page & doctor directory
    ├── login.html                  # Unified login with 1-click demo accounts
    ├── register.html               # Registration with Patient / Doctor role switch
    ├── patient-dashboard.html      # Patient portal (booking, my appointments, prescription viewer)
    ├── doctor-dashboard.html       # Doctor portal (stats, appointments, prescriptions, slot manager)
    ├── css/
    │   └── styles.css              # Custom healthcare UI styling & responsive design
    └── js/
        ├── api.js                  # Central API client & JWT token management
        ├── auth.js                 # Login & Registration logic
        ├── patient.js              # Patient booking & appointments workflows
        └── doctor.js               # Doctor appointments, prescriptions, & slot management
```

---

## 🚀 Quick Start Guide

### 1. Prerequisites
- **Java 21** or later installed (`java -version`)
- **Git** installed (`git --version`)

### 2. Run the Backend
Navigate to the `backend` folder:
```bash
cd backend
```

Using the included **Maven Wrapper** (works without installing Maven):
- **On Windows (PowerShell / Command Prompt)**:
  ```powershell
  .\mvnw.cmd spring-boot:run
  ```
- **On macOS / Linux**:
  ```bash
  ./mvnw spring-boot:run
  ```

Once started, the backend runs at:
👉 **`http://localhost:8080`**

### 3. Open the Application
You have two easy ways to access the frontend:

#### Option A: Directly through Spring Boot (Recommended)
Spring Boot automatically serves the web frontend! Open your browser to:
👉 **[http://localhost:8080](http://localhost:8080)**

#### Option B: Standalone Frontend
You can open `frontend/index.html` directly in your browser, or serve it using VS Code Live Server or any HTTP server:
```bash
cd frontend
# e.g., with npx:
npx serve .
# or Python:
python -m http.server 3000
```
*(CORS is pre-configured on the backend to accept requests from any origin)*.

---

## 🔑 Pre-Configured Demo Accounts

For testing, sample accounts and verified doctors are automatically seeded at startup:

| Account Type | Email | Password | Role / Specialization |
| :--- | :--- | :--- | :--- |
| **Doctor** | `doctor@demo.com` | `password123` | Dr. Sarah Jenkins (Cardiologist) |
| **Doctor** | `drchen@demo.com` | `password123` | Dr. David Chen (Dermatologist) |
| **Patient** | `patient@demo.com` | `password123` | John Doe (Has booked appointments) |
| **Patient** | `emma@demo.com` | `password123` | Emma Watson (New patient) |

> 💡 **Tip**: On `login.html`, click **"👤 Patient Demo"** or **"🩺 Doctor Demo"** for instant 1-click test login!

---

## 🗄️ Database Options

### In-Memory H2 Database (Default)
Runs out of the box with zero setup required.
- **H2 Console URL**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
- **JDBC URL**: `jdbc:h2:mem:doctorappdb`
- **Username**: `sa`
- **Password**: *(leave blank)*

### MySQL Database (Optional)
To switch to MySQL, open `backend/src/main/resources/application.properties` and uncomment:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/doctor_booking_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
```

---

## 📡 REST API Reference

### 🔐 Authentication (`/api/auth`)
| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/register` | Register new Patient or Doctor account | Public |
| `POST` | `/api/auth/login` | Authenticate and get JWT token | Public |
| `GET` | `/api/auth/me` | Get current logged-in user details | Authenticated |

### 🩺 Doctors (`/api/doctors`)
| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/doctors` | List doctors (supports `?search=` and `?specialization=`) | Public |
| `GET` | `/api/doctors/{id}` | Get specific doctor details | Public |
| `GET` | `/api/doctors/specializations` | Get list of distinct specializations | Public |
| `GET` | `/api/doctors/{id}/slots` | Get available slots for doctor by date (`?date=YYYY-MM-DD`) | Public |
| `GET` | `/api/doctors/profile/me` | Get current doctor's profile | Doctor |
| `PUT` | `/api/doctors/profile/me` | Update current doctor's profile | Doctor |
| `GET` | `/api/doctors/slots/my-slots` | Get doctor's upcoming configured slots | Doctor |
| `POST` | `/api/doctors/slots` | Add new availability slot | Doctor |
| `DELETE` | `/api/doctors/slots/{id}` | Remove availability slot | Doctor |

### 📅 Appointments (`/api/appointments`)
| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/appointments/book` | Book a new appointment slot | Patient |
| `GET` | `/api/appointments/my-appointments` | Get appointments for logged-in user | Authenticated |
| `PATCH` | `/api/appointments/{id}/status` | Update status (`CONFIRMED`, `COMPLETED`, `CANCELLED`) & write Rx | Doctor / Patient |
| `POST` | `/api/appointments/{id}/cancel` | Cancel an appointment | Doctor / Patient |

### 👤 Patients (`/api/patients`)
| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/patients/profile/me` | Get current patient profile | Patient |
| `PUT` | `/api/patients/profile/me` | Update health details / history | Patient |

---

## 📤 Pushing to Git & GitHub

Follow these steps to initialize and push your project to your GitHub repository:

### 1. Initialize Git in the Project Root
Open PowerShell or your terminal in the `doctor-appointment-booking` folder:
```bash
git init
```

### 2. Stage all Files
```bash
git add .
```

### 3. Commit the Changes
```bash
git commit -m "feat: initial commit for full stack doctor appointment booking system"
```

### 4. Create a New Repository on GitHub
1. Go to [GitHub](https://github.com/new).
2. Create a new repository named `doctor-appointment-booking`.
3. Do **NOT** initialize with README or .gitignore (we already provided them).

### 5. Link and Push to GitHub
```bash
git branch -M main
git remote add origin https://github.com/<your-username>/doctor-appointment-booking.git
git push -u origin main
```

---

## 📄 License
This project is open-source and available under the [MIT License](LICENSE).
