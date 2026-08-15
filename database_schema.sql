-- Sunrise Dental Clinic System Database

DROP DATABASE IF EXISTS sunrise_dental_clinic_system_db;

CREATE DATABASE sunrise_dental_clinic_system_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE sunrise_dental_clinic_system_db;


-- Users

CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role ENUM('Receptionist', 'Dentist', 'Administrator') NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_role (role)
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci;


-- Patients

CREATE TABLE patients (
    patient_id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    contact_number VARCHAR(15) NOT NULL,
    address VARCHAR(255),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_contact_number (contact_number),
    INDEX idx_first_name (first_name),
    INDEX idx_last_name (last_name)
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci;


-- Dentists

CREATE TABLE dentists (
    dentist_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    specialization VARCHAR(100),

    FOREIGN KEY (user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci;


-- Treatments

CREATE TABLE treatments (
    treatment_id INT AUTO_INCREMENT PRIMARY KEY,
    treatment_name VARCHAR(100) NOT NULL UNIQUE,
    cost DECIMAL(10, 2) NOT NULL,

    CONSTRAINT chk_cost CHECK (cost > 0)
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci;


-- Appointments

CREATE TABLE appointments (
    appointment_id INT AUTO_INCREMENT PRIMARY KEY,
    appointment_number VARCHAR(20) NOT NULL UNIQUE,
    patient_id INT NOT NULL,
    dentist_id INT NOT NULL,
    appointment_date DATE NOT NULL,
    appointment_time TIME NOT NULL,
    treatment_id INT,
    status ENUM('Scheduled', 'Completed', 'Cancelled', 'No-Show')
        DEFAULT 'Scheduled',
    notes TEXT,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (patient_id)
        REFERENCES patients(patient_id)
        ON DELETE RESTRICT,

    FOREIGN KEY (dentist_id)
        REFERENCES dentists(dentist_id)
        ON DELETE RESTRICT,

    FOREIGN KEY (treatment_id)
        REFERENCES treatments(treatment_id)
        ON DELETE SET NULL,

    UNIQUE KEY unique_dentist_slot (
        dentist_id,
        appointment_date,
        appointment_time
    ),

    INDEX idx_patient_id (patient_id),
    INDEX idx_dentist_id (dentist_id),
    INDEX idx_appointment_date (appointment_date),
    INDEX idx_status (status)
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci;


-- Bills

CREATE TABLE bills (
    bill_id INT AUTO_INCREMENT PRIMARY KEY,
    bill_number VARCHAR(20) NOT NULL UNIQUE,
    appointment_id INT NOT NULL,
    patient_id INT NOT NULL,
    treatment_id INT NOT NULL,
    total DECIMAL(10, 2) NOT NULL,
    payment_status ENUM('Pending', 'Paid', 'Cancelled')
        NOT NULL DEFAULT 'Pending',
    payment_date DATE NULL,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (appointment_id)
        REFERENCES appointments(appointment_id)
        ON DELETE RESTRICT,

    FOREIGN KEY (patient_id)
        REFERENCES patients(patient_id)
        ON DELETE RESTRICT,

    FOREIGN KEY (treatment_id)
        REFERENCES treatments(treatment_id)
        ON DELETE RESTRICT,

    INDEX idx_patient_id (patient_id),
    INDEX idx_bill_date (created_date),

    CONSTRAINT chk_total CHECK (total > 0)
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci;


-- Initial administrator account

INSERT INTO users (
    username,
    password,
    full_name,
    role,
    is_active
) VALUES (
    'admin01',
    '9b8769a4a742959a2d0298c36fb70623f2dfacda8436237df08d8dfd5b37374c',
    'Robert Brown',
    'Administrator',
    TRUE
);

COMMIT;