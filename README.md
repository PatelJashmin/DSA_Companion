# DSA Practice Companion

## Overview
This repository contains the **DSA Practice Companion**, an Advanced Java and DBMS combined mini-project developed for our Advanced Java and DBMS course. It is designed to help students track their competitive programming journey, manage personalized problem banks, and optimize learning through an automated Spaced Repetition engine.

## Features
* **Personalized Question Bank:** Strictly isolated problem sets ensuring users only see their own tailored data.
* **Spaced Repetition Engine:** A dynamic 5-tier interval algorithm (1, 3, 7, 21, and 30 days) that calculates and schedules future problem reviews based on user mastery scores (1-10).
* **Live Activity Dashboard:** A real-time activity log demonstrating full CRUD operations (Create practice attempts, Read historical logs, Update revision states, Delete mistaken entries).
* **Relational Topic Mapping:** Robust many-to-many topic tagging using junction tables to accurately categorize algorithms.

## Technology Stack
* **Frontend:** Java (Swing & AWT)
* **Backend:** MySQL
* **Connectivity:** JDBC Prepared Statements for secure, transaction-safe execution

## Database Architecture
The system is built on a normalized relational schema:
* `STUDENT`: Manages multi-user authentication and data isolation.
* `TOPIC` & `CATEGORY_MAP`: A master dictionary and junction table for many-to-many algorithmic tagging.
* `QUESTION`: Stores problem metadata (Title, Level, URL, Source).
* `ATTEMPT`: Logs individual practice sessions, execution times, and logic notes.
* `REVISION`: The operational engine table driving the spaced repetition scheduling.

## Setup Instructions
1. Clone the repository and open the project in your preferred Java IDE (e.g., VS Code or IntelliJ).
2. Execute the provided SQL script within MySQL Workbench to instantiate the database schema and tables.
3. Update the `DatabaseConnection.java` file with your local MySQL username and password.
4. Compile the source files and run `Login.java` to launch the application interface.

## Authors
* **Patel Jashmin**
* **Prajwal Deepak Govekar**
