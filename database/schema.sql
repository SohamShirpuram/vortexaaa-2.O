-- ==============================================================
-- GovFix: MySQL 8 Schema
-- Database: govfix
-- ==============================================================

CREATE DATABASE IF NOT EXISTS govfix CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE govfix;

-- Drop tables in reverse foreign key order if needed
DROP TABLE IF EXISTS audit_events;
DROP TABLE IF EXISTS escalations;
DROP TABLE IF EXISTS evidence_records;
DROP TABLE IF EXISTS ai_recommendations;
DROP TABLE IF EXISTS garbage_van_stops;
DROP TABLE IF EXISTS garbage_vans;
DROP TABLE IF EXISTS complaints;
DROP TABLE IF EXISTS workflow_transitions;
DROP TABLE IF EXISTS workflow_states;
DROP TABLE IF EXISTS workflow_definitions;
DROP TABLE IF EXISTS users;

-- 1. Users Table
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    role VARCHAR(30) NOT NULL,
    department VARCHAR(50),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_role (role)
) ENGINE=InnoDB;

-- 2. Workflow Definitions Table
CREATE TABLE workflow_definitions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    workflow_key VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    initial_state_key VARCHAR(50) NOT NULL,
    category_pattern VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 3. Workflow States Table
CREATE TABLE workflow_states (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    workflow_id BIGINT NOT NULL,
    state_key VARCHAR(50) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    stage_order INT DEFAULT 0,
    deadline_hours INT DEFAULT 48,
    is_terminal BOOLEAN DEFAULT FALSE,
    requires_evidence BOOLEAN DEFAULT FALSE,
    color_code VARCHAR(20) DEFAULT '#0d9488',
    CONSTRAINT fk_state_workflow FOREIGN KEY (workflow_id) REFERENCES workflow_definitions(id) ON DELETE CASCADE,
    UNIQUE KEY uk_workflow_state (workflow_id, state_key)
) ENGINE=InnoDB;

-- 4. Workflow Transitions Table
CREATE TABLE workflow_transitions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    workflow_id BIGINT NOT NULL,
    from_state_key VARCHAR(50) NOT NULL,
    to_state_key VARCHAR(50) NOT NULL,
    action_name VARCHAR(100) NOT NULL,
    allowed_roles VARCHAR(150) NOT NULL,
    requires_notes BOOLEAN DEFAULT FALSE,
    requires_evidence BOOLEAN DEFAULT FALSE,
    requires_approval BOOLEAN DEFAULT FALSE,
    CONSTRAINT fk_transition_workflow FOREIGN KEY (workflow_id) REFERENCES workflow_definitions(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 5. Complaints Table
CREATE TABLE complaints (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id VARCHAR(30) NOT NULL UNIQUE,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(60) NOT NULL,
    department VARCHAR(60) NOT NULL,
    locality VARCHAR(100) NOT NULL,
    ward_or_zone VARCHAR(50),
    landmark VARCHAR(150),
    priority VARCHAR(20) DEFAULT 'MEDIUM',
    status VARCHAR(50) NOT NULL,
    citizen_name VARCHAR(100) NOT NULL,
    citizen_contact VARCHAR(50),
    assigned_officer_id BIGINT,
    photo_url VARCHAR(255),
    deadline_at TIMESTAMP NULL,
    resolved_at TIMESTAMP NULL,
    closed_at TIMESTAMP NULL,
    is_escalated BOOLEAN DEFAULT FALSE,
    citizen_feedback_rating INT NULL,
    citizen_feedback_comments TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_complaint_officer FOREIGN KEY (assigned_officer_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_complaint_status (status),
    INDEX idx_complaint_category (category),
    INDEX idx_complaint_ticket (ticket_id)
) ENGINE=InnoDB;

-- 6. Evidence Records Table
CREATE TABLE evidence_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    complaint_id BIGINT NOT NULL,
    officer_id BIGINT NOT NULL,
    evidence_type VARCHAR(50) NOT NULL, -- BEFORE_PHOTO, AFTER_PHOTO, INSPECTION_REPORT
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    file_size BIGINT,
    mime_type VARCHAR(100),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_evidence_complaint FOREIGN KEY (complaint_id) REFERENCES complaints(id) ON DELETE CASCADE,
    CONSTRAINT fk_evidence_officer FOREIGN KEY (officer_id) REFERENCES users(id)
) ENGINE=InnoDB;

-- 7. Escalations Table
CREATE TABLE escalations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    complaint_id BIGINT NOT NULL,
    escalation_level INT DEFAULT 1,
    reason TEXT NOT NULL,
    triggered_by VARCHAR(50) NOT NULL, -- SYSTEM_DEADLINE, SUPERVISOR_MANUAL
    target_role VARCHAR(50) NOT NULL,
    is_resolved BOOLEAN DEFAULT FALSE,
    resolution_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP NULL,
    CONSTRAINT fk_escalation_complaint FOREIGN KEY (complaint_id) REFERENCES complaints(id) ON DELETE CASCADE,
    INDEX idx_escalation_complaint (complaint_id)
) ENGINE=InnoDB;

-- 8. Audit Events Table
CREATE TABLE audit_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    actor_username VARCHAR(50) NOT NULL,
    actor_role VARCHAR(30) NOT NULL,
    complaint_id BIGINT,
    ticket_id VARCHAR(30),
    action VARCHAR(100) NOT NULL,
    previous_state VARCHAR(50),
    requested_state VARCHAR(50),
    outcome VARCHAR(20) NOT NULL, -- SUCCESS, REJECTED
    reason TEXT,
    client_ip VARCHAR(50),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_audit_ticket (ticket_id),
    INDEX idx_audit_outcome (outcome)
) ENGINE=InnoDB;

-- 9. AI Recommendations Table
CREATE TABLE ai_recommendations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    complaint_id BIGINT NOT NULL,
    suggested_category VARCHAR(60) NOT NULL,
    suggested_department VARCHAR(60) NOT NULL,
    suggested_priority VARCHAR(20) NOT NULL,
    confidence_score DOUBLE DEFAULT 0.85,
    explanation TEXT,
    status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED, EDITED
    decided_by_username VARCHAR(50),
    decided_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ai_complaint FOREIGN KEY (complaint_id) REFERENCES complaints(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 10. Garbage Collection Vans Table (Featured PCMC Tracking Module)
CREATE TABLE garbage_vans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vehicle_number VARCHAR(30) NOT NULL UNIQUE,
    driver_name VARCHAR(100) NOT NULL,
    contact_phone VARCHAR(20),
    assigned_zone VARCHAR(50) NOT NULL, -- E.g. Nigdi-Pradhikaran, Wakad-Hinjawadi, Bhosari
    current_latitude DOUBLE NOT NULL,
    current_longitude DOUBLE NOT NULL,
    heading DOUBLE DEFAULT 0.0,
    speed_kmph DOUBLE DEFAULT 18.5,
    status VARCHAR(30) DEFAULT 'ON_DUTY', -- ON_DUTY, IDLE, COMPLETED
    last_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 11. Garbage Van Stops & Waypoints Table
CREATE TABLE garbage_van_stops (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    van_id BIGINT NOT NULL,
    locality_name VARCHAR(100) NOT NULL,
    stop_order INT NOT NULL,
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    scheduled_arrival_time VARCHAR(20),
    is_completed BOOLEAN DEFAULT FALSE,
    estimated_arrival_minutes INT DEFAULT 15,
    CONSTRAINT fk_stop_van FOREIGN KEY (van_id) REFERENCES garbage_vans(id) ON DELETE CASCADE
) ENGINE=InnoDB;
