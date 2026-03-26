-- Update caregiver database to match test data
UPDATE users SET username = 'caregiver_karim', email = 'karim@guardianlink.org', password = 'c2e9d9947fd5745e16d6f2ec65cf3449f63a1a228a984c917eee284552ca767a' WHERE id = 6;
UPDATE users SET username = 'caregiver_nasrin', email = 'nasrin@guardianlink.org', password = 'c2e9d9947fd5745e16d6f2ec65cf3449f63a1a228a984c917eee284552ca767a' WHERE id = 7;
DELETE FROM users WHERE id = 8;
