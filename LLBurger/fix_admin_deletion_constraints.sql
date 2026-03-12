-- Migration to fix foreign key constraints for admin deletion
-- Run this SQL script on your Supabase database

-- 1. Fix business_status.last_changed_by - SET NULL on delete
ALTER TABLE business_status
DROP CONSTRAINT IF EXISTS fk_business_status_last_changed_by;

ALTER TABLE business_status
ADD CONSTRAINT fk_business_status_last_changed_by
FOREIGN KEY (last_changed_by)
REFERENCES admins(user_id)
ON DELETE SET NULL;

-- 2. Fix business_status_logs.changed_by - make nullable and SET NULL on delete
ALTER TABLE business_status_logs
ALTER COLUMN changed_by DROP NOT NULL;

ALTER TABLE business_status_logs
DROP CONSTRAINT IF EXISTS fk_business_status_logs_changed_by;

ALTER TABLE business_status_logs
ADD CONSTRAINT fk_business_status_logs_changed_by
FOREIGN KEY (changed_by)
REFERENCES admins(user_id)
ON DELETE SET NULL;

-- 3. Fix password_reset_tokens.user_id - CASCADE delete
ALTER TABLE password_reset_tokens
DROP CONSTRAINT IF EXISTS fk_password_reset_tokens_user_id;

ALTER TABLE password_reset_tokens
ADD CONSTRAINT fk_password_reset_tokens_user_id
FOREIGN KEY (user_id)
REFERENCES users(id)
ON DELETE CASCADE;

-- 4. Fix refresh_tokens.user_id - CASCADE delete
ALTER TABLE refresh_tokens
DROP CONSTRAINT IF EXISTS fk_refresh_tokens_user_id;

ALTER TABLE refresh_tokens
ADD CONSTRAINT fk_refresh_tokens_user_id
FOREIGN KEY (user_id)
REFERENCES users(id)
ON DELETE CASCADE;
