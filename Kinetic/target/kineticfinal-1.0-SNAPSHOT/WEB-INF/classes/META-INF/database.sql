-- STEP 1: Create database safely
CREATE DATABASE IF NOT EXISTS gatesystem;

-- STEP 2: Use the database
USE gatesystem;

-- =========================
-- TABLES
-- =========================

-- USERS
CREATE TABLE IF NOT EXISTS Users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    person_type VARCHAR(50),
    email VARCHAR(190),
    password_hash VARCHAR(255),
    phone VARCHAR(30),
    age INT,
    sex VARCHAR(10),
    is_active BOOLEAN
);

ALTER TABLE Users
    ADD COLUMN IF NOT EXISTS email VARCHAR(190),
    ADD COLUMN IF NOT EXISTS password_hash VARCHAR(255),
    ADD COLUMN IF NOT EXISTS phone VARCHAR(30),
    ADD COLUMN IF NOT EXISTS created_at DATETIME DEFAULT CURRENT_TIMESTAMP;

CREATE UNIQUE INDEX IF NOT EXISTS idx_users_email ON Users(email);

-- CHECKPOINTS
CREATE TABLE IF NOT EXISTS Checkpoints (
    gate_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    gate_name VARCHAR(100),
    is_active BOOLEAN
);

-- STAFF
CREATE TABLE IF NOT EXISTS Staff (
    staff_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    department VARCHAR(100),
    job_title VARCHAR(100),
    FOREIGN KEY (user_id) REFERENCES Users(user_id)
);


































































-- STUDENT
CREATE TABLE IF NOT EXISTS Student (
    reg_no VARCHAR(30) PRIMARY KEY,
    user_id BIGINT,
    year_of_study INT,
    course_name VARCHAR(100),
    faculty VARCHAR(100),
    department VARCHAR(100),
    course VARCHAR(100),
    FOREIGN KEY (user_id) REFERENCES Users(user_id)
);

-- VISITOR
CREATE TABLE IF NOT EXISTS Visitor (
    visitor_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    visit_details JSON,
    FOREIGN KEY (user_id) REFERENCES Users(user_id)
);

-- SECURITY PERSONNEL
CREATE TABLE IF NOT EXISTS Security_personnel (
    security_no BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    badge_no BIGINT,
    FOREIGN KEY (user_id) REFERENCES Users(user_id)
);

-- SESSION
CREATE TABLE IF NOT EXISTS Session (
    session_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    gate_id BIGINT,
    exit_gate_id BIGINT,
    guard_id BIGINT,
    session_time DATETIME,
    exit_time DATETIME,
    session_name VARCHAR(50),
    expected_duration INT,
    office_visiting VARCHAR(120),
    visit_purpose VARCHAR(255),
    reason_for_leaving VARCHAR(255),
    phone VARCHAR(30),
    FOREIGN KEY (user_id) REFERENCES Users(user_id),
    FOREIGN KEY (gate_id) REFERENCES Checkpoints(gate_id),
    FOREIGN KEY (exit_gate_id) REFERENCES Checkpoints(gate_id),
    FOREIGN KEY (guard_id) REFERENCES Security_personnel(security_no)
);

ALTER TABLE Session
    ADD COLUMN IF NOT EXISTS exit_gate_id BIGINT,
    ADD COLUMN IF NOT EXISTS exit_time DATETIME,
    ADD COLUMN IF NOT EXISTS expected_duration INT,
    ADD COLUMN IF NOT EXISTS office_visiting VARCHAR(120),
    ADD COLUMN IF NOT EXISTS visit_purpose VARCHAR(255),
    ADD COLUMN IF NOT EXISTS reason_for_leaving VARCHAR(255),
    ADD COLUMN IF NOT EXISTS phone VARCHAR(30);

-- Bootstrap gates
INSERT INTO Checkpoints (gate_name, is_active)
SELECT 'Main Gate', TRUE
WHERE NOT EXISTS (SELECT 1 FROM Checkpoints WHERE gate_name = 'Main Gate');

INSERT INTO Checkpoints (gate_name, is_active)
SELECT 'Njokerio Gate', TRUE
WHERE NOT EXISTS (SELECT 1 FROM Checkpoints WHERE gate_name = 'Njokerio Gate');

-- REPORTS
CREATE TABLE IF NOT EXISTS Reports (
    report_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_type VARCHAR(50),
    generated_by BIGINT,
    created_at DATETIME,
    report_data JSON,
    FOREIGN KEY (generated_by) REFERENCES Users(user_id)
);