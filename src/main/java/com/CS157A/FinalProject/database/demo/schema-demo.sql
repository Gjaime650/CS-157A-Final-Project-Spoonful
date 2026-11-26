-- Spoonful sample schema, for testing the current login and sign-up code.
-- NOT FINAL SCHEMA just for demo purposes
--
-- Load it with:
--   mysql -u <your_user> -p < src/main/java/com/CS157A/FinalProject/database/demo/schema-demo.sql
--
-- WARNING: this drops and recreates the tables, so any data in them is lost.
-- The database name must match db.url in src/main/resources/db.properties.

CREATE DATABASE IF NOT EXISTS SpoonfulDB
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;   -- case-insensitive, so email lookups ignore case
USE SpoonfulDB;

DROP TABLE IF EXISTS restaurant_phone;
DROP TABLE IF EXISTS restaurant;
DROP TABLE IF EXISTS zip_code;
DROP TABLE IF EXISTS restaurant_staff;
DROP TABLE IF EXISTS customer;
DROP TABLE IF EXISTS users;

-- Every account. user_type values must match the UserType enum exactly.
CREATE TABLE users (
    user_id       INT          NOT NULL AUTO_INCREMENT,
    user_type     ENUM('CUSTOMER', 'STAFF') NOT NULL,
    name          VARCHAR(100) NOT NULL,
    email         VARCHAR(255) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,   -- BCrypt hashes are 60 characters
    phone_number  VARCHAR(20)  NULL,       -- optional for staff
    PRIMARY KEY (user_id),
    UNIQUE KEY uq_users_email (email)
);

-- Customer subtype
CREATE TABLE customer (
    user_id                 INT          NOT NULL,
    profile_special_request VARCHAR(500) NULL,
    PRIMARY KEY (user_id),
    CONSTRAINT fk_customer_user FOREIGN KEY (user_id)
        REFERENCES users (user_id) ON DELETE CASCADE
);

-- Staff subtype
CREATE TABLE restaurant_staff (
    user_id INT NOT NULL,
    PRIMARY KEY (user_id),
    CONSTRAINT fk_staff_user FOREIGN KEY (user_id)
        REFERENCES users (user_id) ON DELETE CASCADE
);

-- City and state are stored once per ZIP code.
CREATE TABLE zip_code (
    zip_code VARCHAR(10)  NOT NULL,
    city     VARCHAR(100) NOT NULL,
    state    VARCHAR(50)  NOT NULL,
    PRIMARY KEY (zip_code)
);

CREATE TABLE restaurant (
    restaurant_id    INT          NOT NULL AUTO_INCREMENT,
    name             VARCHAR(150) NOT NULL,
    description      TEXT         NULL,
    dress_code       VARCHAR(50)  NULL,
    dining_style     VARCHAR(50)  NULL,
    street           VARCHAR(150) NOT NULL,
    zip_code         VARCHAR(10)  NOT NULL,
    manager_staff_id INT          NOT NULL,
    PRIMARY KEY (restaurant_id),
    -- One restaurant per staff member, so the login query returns at most one row.
    UNIQUE KEY uq_restaurant_manager (manager_staff_id),
    CONSTRAINT fk_restaurant_zip FOREIGN KEY (zip_code)
        REFERENCES zip_code (zip_code),
    CONSTRAINT fk_restaurant_manager FOREIGN KEY (manager_staff_id)
        REFERENCES restaurant_staff (user_id)
);

-- A restaurant can list several phone numbers.
CREATE TABLE restaurant_phone (
    restaurant_id INT         NOT NULL,
    phone_number  VARCHAR(20) NOT NULL,
    PRIMARY KEY (restaurant_id, phone_number),
    CONSTRAINT fk_phone_restaurant FOREIGN KEY (restaurant_id)
        REFERENCES restaurant (restaurant_id) ON DELETE CASCADE
);
