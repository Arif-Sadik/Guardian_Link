-- ====================================
-- Guardian Link Database Schema
-- Caregiver Assignment & Notification Support
-- ====================================

-- Add assigned_caregiver_id column to children table (if not exists)
ALTER TABLE children 
ADD COLUMN assigned_caregiver_id INT NULL REFERENCES users(id) ON DELETE SET NULL;

-- Create notifications table for caregiver assignments and removals
CREATE TABLE IF NOT EXISTS notifications (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    caregiver_id INTEGER NOT NULL,
    message VARCHAR(500) NOT NULL,
    notification_type VARCHAR(50) NOT NULL,
    child_name VARCHAR(255) NOT NULL,
    child_id INTEGER NOT NULL,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    is_read INTEGER DEFAULT 0,
    FOREIGN KEY (caregiver_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (child_id) REFERENCES children(id) ON DELETE CASCADE
);

-- Add index to children table for assigned_caregiver_id for faster queries
CREATE INDEX IF NOT EXISTS idx_assigned_caregiver_id ON children(assigned_caregiver_id);
