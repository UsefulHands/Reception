CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    role VARCHAR(50) NOT NULL, -- ROLE_ADMIN, ROLE_RECEPTIONIST, ROLE_GUEST
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255)
);

CREATE TABLE admins (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    corporate_email VARCHAR(255) NOT NULL,
    admin_title VARCHAR(50),
    user_id BIGINT NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    CONSTRAINT fk_admin_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE receptionists (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    employee_id VARCHAR(50) UNIQUE,
    shift_type VARCHAR(50),
    user_id BIGINT NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    CONSTRAINT fk_receptionist_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE guests (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(50) UNIQUE,
    identity_number VARCHAR(20) UNIQUE,
    user_id BIGINT UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    CONSTRAINT fk_guest_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    action VARCHAR(255),
    performed_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    details VARCHAR(255)
);

CREATE TABLE rooms (
                       id BIGSERIAL PRIMARY KEY,
                       room_number VARCHAR(20) NOT NULL UNIQUE,
                       type VARCHAR(50) NOT NULL,
                       view VARCHAR(50),
                       beds INT NOT NULL,
                       max_guests INT NOT NULL,
                       area_sqm DOUBLE PRECISION,
                       description TEXT NOT NULL,
                       price DECIMAL(19, 2) NOT NULL,
                       available BOOLEAN NOT NULL DEFAULT TRUE,
                       smoking_allowed BOOLEAN NOT NULL DEFAULT FALSE,
                       floor INT NOT NULL,
                       status VARCHAR(20) NOT NULL DEFAULT 'CLEAN',
                       current_reservation_id BIGINT,
                       is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP,
                       created_by VARCHAR(100),
                       updated_by VARCHAR(100)
);

CREATE TABLE reservations (
                              id BIGSERIAL PRIMARY KEY,
                              room_id BIGINT NOT NULL,
                              guest_id BIGINT NOT NULL,

                              check_in_date DATE NOT NULL,
                              check_out_date DATE NOT NULL,

                              status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                              is_deleted BOOLEAN NOT NULL DEFAULT FALSE,

                              total_price DECIMAL(19, 2) NOT NULL,
                              amount_paid DECIMAL(19, 2) DEFAULT 0,

                              notes TEXT,

                              created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              created_by VARCHAR(255),
                              updated_at TIMESTAMP,
                              updated_by VARCHAR(255),
                              CONSTRAINT fk_room FOREIGN KEY (room_id) REFERENCES rooms(id),
                              CONSTRAINT fk_guest FOREIGN KEY (guest_id) REFERENCES guests(id)
);

CREATE TABLE room_bed_types (
                                room_id BIGINT NOT NULL,
                                bed_type VARCHAR(50) NOT NULL, -- DOUBLE, QUEEN, KING
                                CONSTRAINT fk_room_bed FOREIGN KEY (room_id) REFERENCES rooms (id) ON DELETE CASCADE
);

CREATE TABLE room_amenities (
                                room_id BIGINT NOT NULL,
                                amenity VARCHAR(100) NOT NULL, -- WIFI, TV, MINIBAR, vs.
                                CONSTRAINT fk_room_amenity FOREIGN KEY (room_id) REFERENCES rooms (id) ON DELETE CASCADE
);

CREATE TABLE room_images (
                             room_id BIGINT NOT NULL,
                             image_url VARCHAR(255) NOT NULL,
                             CONSTRAINT fk_room_image FOREIGN KEY (room_id) REFERENCES rooms (id) ON DELETE CASCADE
);

INSERT INTO rooms (room_number, type, beds, max_guests, area_sqm, view, description, price, available, smoking_allowed, floor, created_by)
VALUES ('101', 'SINGLE', 1, 1, 18.0, 'GARDEN', 'Cozy single room with garden view', 85.00, TRUE, FALSE, 1, 'SYSTEM');

INSERT INTO room_bed_types (room_id, bed_type) VALUES (1, 'QUEEN');
INSERT INTO room_amenities (room_id, amenity) VALUES (1, 'WIFI'), (1, 'TV'), (1, 'SAFE');
INSERT INTO room_images (room_id, image_url) VALUES (1, 'https://plus.unsplash.com/premium_photo-1661964402307-02267d1423f5?q=80&w=1546&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D'),
                                                    (1, 'https://images.unsplash.com/photo-1568495248636-6432b97bd949?q=80&w=2574&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D');


INSERT INTO rooms (room_number, type, beds, max_guests, area_sqm, view, description, price, available, smoking_allowed, floor, created_by)
VALUES ('304', 'DELUXE', 2, 4, 45.0, 'CITY', 'Large deluxe room for families', 250.00, TRUE, TRUE, 3, 'SYSTEM');

INSERT INTO room_bed_types (room_id, bed_type) VALUES (2, 'KING'), (2, 'KING');
INSERT INTO room_amenities (room_id, amenity) VALUES (2, 'WIFI'), (2, 'COFFEE_MACHINE'), (2, 'MINIBAR'), (2, 'BALCONY');
INSERT INTO room_images (room_id, image_url) VALUES (2, 'https://plus.unsplash.com/premium_photo-1661964402307-02267d1423f5?q=80&w=1546&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D'),
                                                    (2, 'https://images.unsplash.com/photo-1568495248636-6432b97bd949?q=80&w=2574&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D');

-- pass: 'admin123'
INSERT INTO users (username, password, role, created_at, created_by)
VALUES ('admin', '$2a$10$hkgemn3l2/eMQIVynLaghuSGszNDwkeGX2FLzN23QMCTUMO1Uiw7a', 'ROLE_ADMIN', CURRENT_TIMESTAMP, 'SYSTEM');

INSERT INTO admins (first_name, last_name, corporate_email, admin_title, user_id, created_at, created_by)
VALUES ('Cemaleddin', 'Seyhan', 'admin@otel.com', 'SUPER_ADMIN', 1, CURRENT_TIMESTAMP, 'SYSTEM');