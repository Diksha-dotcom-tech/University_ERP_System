#University ERP System (Java + Swing)

A desktop-based University ERP application built using Java Swing that manages academic workflows for Students, Instructors, and Admins with secure authentication and strict role-based access control.

##Project Overview
This system enables a university to manage courses, sections, enrollments, grading, and transcripts.
It uses two separate databases:

Auth DB — stores usernames, roles, and password hashes
ERP DB — stores academic data (students, courses, enrollments, grades)

The application enforces access rules and supports a global Maintenance Mode.

##Features
Authentication & Security
Role-based login (Student / Instructor / Admin)
Passwords stored as secure hashes (no plaintext)
Separate Auth DB and ERP DB
Session-based access control

###Student
Browse course catalog
Register/drop sections with capacity and duplicate checks
View timetable and grades
Download transcript (CSV/PDF)

###Instructor
View assigned sections only
Enter assessment scores
Compute final grades using custom weightage
View basic class statistics
Export grades (CSV)

###Admin
Add users (students/instructors)
Create courses and sections
Assign instructors
Toggle Maintenance Mode

###Maintenance Mode
Students and instructors can view data only
All write operations are blocked
Visible banner shown in UI

##Architecture
edu.univ.erp
├── ui          # Swing UI components
├── domain      # Data models
├── service     # Business logic
├── data        # Database access (JDBC)
├── auth        # Login & password hashing
├── access      # Role & maintenance checks
├── util        # CSV/PDF export, helpers


UI never directly accesses the database; all actions go through the service layer.

##Tech Stack
Java
Swing (GUI)
JDBC (MySQL)
BCrypt for password hashing
CSV / PDF export utilities

##How to Run
Create Auth DB and ERP DB using provided SQL scripts
Update database credentials in configuration
Compile and run the application

javac Main.java
java Main

##Sample users:
admin1 — Admin
inst1 — Instructor
stu1, stu2 — Students
