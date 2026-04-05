CREATE DATABASE IF NOT EXISTS gate_system;
USE gate_system;

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    user_type ENUM('GUARD', 'STUDENT', 'ADMIN') NOT NULL,
    date_of_birth DATE NOT NULL,
    department VARCHAR(100),
    profile_photo VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS entry_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    officer_id INT NOT NULL,
    student_id INT NOT NULL,
    entry_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_entry_officer FOREIGN KEY (officer_id) REFERENCES users(id),
    CONSTRAINT fk_entry_student FOREIGN KEY (student_id) REFERENCES users(id)
);

-- Seed one guard account. Password is SHA-256 hash for plain text: guard123
INSERT INTO users (first_name, last_name, email, password, phone, user_type, date_of_birth, department, profile_photo, is_active)
SELECT 'Default', 'Guard', 'guard@gate.local',
       '0c29b824ba1b408a3469d1581b79cb7b0a363155bf16f0e642cc9ba75d37b07d',
       '0000000000', 'GUARD', '1995-01-01', 'Security', '', TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'guard@gate.local'
);
