CREATE TABLE users (
                       id SERIAL PRIMARY KEY,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       role VARCHAR(20) NOT NULL
);

CREATE TABLE audit_logs (
                            id SERIAL PRIMARY KEY,
                            action VARCHAR(255),
                            performed_by VARCHAR(100),
                            timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);