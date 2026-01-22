-- V9: Add room_number to users table for auto room assignment
ALTER TABLE users ADD COLUMN room_number VARCHAR(10) NULL;
CREATE INDEX idx_users_room_number ON users(room_number);
