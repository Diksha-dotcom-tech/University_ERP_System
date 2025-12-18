# University ERP System (Java + Swing)
A desktop-based University ERP application built using Java Swing.

## Project Overview
This application manages courses, sections, enrollments, and grades for a university.
It supports Students, Instructors, and Admins with role-based access control.

## Features

### Authentication
Secure login with role-based access
Password hashing
Separate Auth and ERP databases

### Student
Browse course catalog
Register and drop sections
View timetable and grades
Download transcript

### Instructor
View assigned sections
Enter and compute grades
Export grades (CSV)

### Admin
Create users, courses, and sections
Assign instructors
Toggle maintenance mode

## Tech Stack
Java
Swing
JDBC (MySQL)
BCrypt

## How to Run
```bash
javac Main.java
java Main

### Wow
