CREATE DATABASE IF NOT EXISTS dsa_companion;
USE dsa_companion;

-- 2. Drop existing tables to ensure a clean slate (in reverse order of dependencies)
DROP TABLE IF EXISTS CATEGORY_MAP;
DROP TABLE IF EXISTS ATTEMPT;
DROP TABLE IF EXISTS REVISION;
DROP TABLE IF EXISTS QUESTION;
DROP TABLE IF EXISTS TOPIC;
DROP TABLE IF EXISTS STUDENT;

-- STUDENT Table
CREATE TABLE STUDENT (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    joined_on DATE DEFAULT (CURRENT_DATE),
    password VARCHAR(50) DEFAULT 'pass123'
);

-- TOPIC Table 
CREATE TABLE TOPIC (
    name VARCHAR(100) PRIMARY KEY,
    parent_name VARCHAR(100),
    FOREIGN KEY (parent_name) REFERENCES TOPIC(name) ON DELETE SET NULL
);

-- QUESTION Table 
CREATE TABLE QUESTION (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    level ENUM('Easy', 'Medium', 'Hard') NOT NULL,
    source ENUM('LeetCode', 'GeeksForGeeks', 'Codeforces', 'HackerRank', 'Other') NOT NULL,
    link VARCHAR(500),
    student_id INT,
    FOREIGN KEY (student_id) REFERENCES STUDENT(id) ON DELETE CASCADE
);

-- CATEGORY_MAP Table 
CREATE TABLE CATEGORY_MAP (
    topic_name VARCHAR(100) NOT NULL,
    question_id INT NOT NULL,
    PRIMARY KEY (topic_name, question_id),
    FOREIGN KEY (topic_name) REFERENCES TOPIC(name) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES QUESTION(id) ON DELETE CASCADE
);

-- ATTEMPT Table 
CREATE TABLE ATTEMPT (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    question_id INT NOT NULL,
    duration INT,
    attempted_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    logic VARCHAR(500),
    result VARCHAR(50),
    FOREIGN KEY (student_id) REFERENCES STUDENT(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES QUESTION(id) ON DELETE CASCADE
);

-- REVISION Table 
CREATE TABLE REVISION (
    student_id INT NOT NULL,
    question_id INT NOT NULL,
    rev_no INT NOT NULL,
    score INT,
    due_on DATE,
    done_on DATE,
    is_late TINYINT(1) DEFAULT 0,
    PRIMARY KEY (student_id, question_id, rev_no),
    FOREIGN KEY (student_id) REFERENCES STUDENT(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES QUESTION(id) ON DELETE CASCADE
);
-- Insert standard topics (including parent-child relationships where applicable)
INSERT INTO TOPIC (name, parent_name) VALUES 
('Arrays & Hashing', NULL), 
('Stack', NULL), 
('Sliding Window', NULL),
('Dynamic Programming', NULL),
('Graphs', NULL),
('Trees', NULL),
('Linked List', NULL),
('Binary Search', NULL),
('Heap / Priority Queue', NULL),
('Two Pointers', NULL),
('Backtracking', NULL),
('Greedy', NULL);
