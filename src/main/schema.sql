CREATE DATABASE IF NOT EXISTS productivity_coach;
USE productivity_coach;

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    goals TEXT,
    work_rhythm VARCHAR(255),
    preferences TEXT,
    role VARCHAR(20) DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tasks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(50),
    priority INT DEFAULT 2, -- 1: High, 2: Medium, 3: Low
    deadline DATE,
    status VARCHAR(20) DEFAULT 'TODO', -- TODO, IN_PROGRESS, DONE
    time_spent_minutes INT DEFAULT 0,
    estimated_time_minutes INT DEFAULT 30,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
