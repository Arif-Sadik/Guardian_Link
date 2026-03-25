-- ====================================
-- Test Data: 3 New Caregivers + Children Assignment
-- Guardian Link Database
-- ====================================

-- INSERT 3 NEW CAREGIVERS
-- Note: Ensure these users are APPROVED (approved = 1) so they appear in dropdowns

INSERT INTO users (username, password, email, phone_number, role, approved) VALUES
('caregiver_sarah', 'pass123', 'sarah.caregiver@guardianlink.com', '1234567890', 'CAREGIVER', 1),
('caregiver_james', 'pass123', 'james.caregiver@guardianlink.com', '9876543210', 'CAREGIVER', 1),
('caregiver_maria', 'pass123', 'maria.caregiver@guardianlink.com', '5551234567', 'CAREGIVER', 1);

-- Note: If you already have caregiver_john or similar, adjust usernames accordingly
-- The passwords should match the existing format (plain text stored in DB)

-- ====================================
-- INSERT CHILDREN FOR EACH CAREGIVER
-- ====================================

-- Get caregiver IDs (adjust these based on INSERT order - they should be sequential)
-- Assuming the 3 new caregivers get IDs starting from the next available ID after existing users
-- For this demo, we'll use variables or direct IDs

-- CHILDREN FOR CAREGIVER_SARAH (assuming caregiver_sarah has ID = next ID after existing users)
INSERT INTO children (name, age, gender, organization, date_of_birth, status, assigned_caregiver_id) VALUES
('Emma Johnson', 8, 'Female', 'Bright Future Foundation', '2017-05-15', 'Active', (SELECT id FROM users WHERE username = 'caregiver_sarah')),
('Ethan Johnson', 10, 'Male', 'Bright Future Foundation', '2015-03-20', 'Active', (SELECT id FROM users WHERE username = 'caregiver_sarah')),
('Sophia Davis', 7, 'Female', 'Children Care Initiative', '2018-07-10', 'Active', (SELECT id FROM users WHERE username = 'caregiver_sarah')),
('Jackson Davis', 9, 'Male', 'Children Care Initiative', '2016-11-25', 'Active', (SELECT id FROM users WHERE username = 'caregiver_sarah')),
('Olivia Wilson', 6, 'Female', 'Community Support Network', '2019-02-14', 'Active', (SELECT id FROM users WHERE username = 'caregiver_sarah')),
('Noah Wilson', 11, 'Male', 'Community Support Network', '2014-08-30', 'Active', (SELECT id FROM users WHERE username = 'caregiver_sarah')),
('Ava Martinez', 8, 'Female', 'Hope for Tomorrow', '2017-12-05', 'Active', (SELECT id FROM users WHERE username = 'caregiver_sarah')),
('Liam Martinez', 10, 'Male', 'Hope for Tomorrow', '2015-09-18', 'Active', (SELECT id FROM users WHERE username = 'caregiver_sarah')),
('Isabella Garcia', 7, 'Female', 'Children''s Welfare Association', '2018-04-22', 'Active', (SELECT id FROM users WHERE username = 'caregiver_sarah')),
('Mason Garcia', 9, 'Male', 'Children''s Welfare Association', '2016-06-13', 'Active', (SELECT id FROM users WHERE username = 'caregiver_sarah')),
('Mia Rodriguez', 6, 'Female', 'Youth Development Program', '2019-10-30', 'Active', (SELECT id FROM users WHERE username = 'caregiver_sarah')),
('Lucas Rodriguez', 11, 'Male', 'Youth Development Program', '2014-01-07', 'Active', (SELECT id FROM users WHERE username = 'caregiver_sarah'));

-- CHILDREN FOR CAREGIVER_JAMES
INSERT INTO children (name, age, gender, organization, date_of_birth, status, assigned_caregiver_id) VALUES
('Charlotte Brown', 8, 'Female', 'Bright Future Foundation', '2017-01-28', 'Active', (SELECT id FROM users WHERE username = 'caregiver_james')),
('Benjamin Brown', 10, 'Male', 'Bright Future Foundation', '2015-04-12', 'Active', (SELECT id FROM users WHERE username = 'caregiver_james')),
('Amelia Taylor', 7, 'Female', 'Children Care Initiative', '2018-09-09', 'Active', (SELECT id FROM users WHERE username = 'caregiver_james')),
('Henry Taylor', 9, 'Male', 'Children Care Initiative', '2016-02-20', 'Active', (SELECT id FROM users WHERE username = 'caregiver_james')),
('Harper Anderson', 6, 'Female', 'Community Support Network', '2019-05-16', 'Active', (SELECT id FROM users WHERE username = 'caregiver_james')),
('Michael Anderson', 11, 'Male', 'Community Support Network', '2014-12-03', 'Active', (SELECT id FROM users WHERE username = 'caregiver_james')),
('Evelyn Thomas', 8, 'Female', 'Hope for Tomorrow', '2017-08-11', 'Active', (SELECT id FROM users WHERE username = 'caregiver_james')),
('Alexander Thomas', 10, 'Male', 'Hope for Tomorrow', '2015-11-24', 'Active', (SELECT id FROM users WHERE username = 'caregiver_james')),
('Abigail Jackson', 7, 'Female', 'Children''s Welfare Association', '2018-03-08', 'Active', (SELECT id FROM users WHERE username = 'caregiver_james')),
('Daniel Jackson', 9, 'Male', 'Children''s Welfare Association', '2016-07-17', 'Active', (SELECT id FROM users WHERE username = 'caregiver_james')),
('Elizabeth White', 6, 'Female', 'Youth Development Program', '2019-09-02', 'Active', (SELECT id FROM users WHERE username = 'caregiver_james')),
('James White', 11, 'Male', 'Youth Development Program', '2014-11-19', 'Active', (SELECT id FROM users WHERE username = 'caregiver_james'));

-- CHILDREN FOR CAREGIVER_MARIA
INSERT INTO children (name, age, gender, organization, date_of_birth, status, assigned_caregiver_id) VALUES
('Grace Harris', 8, 'Female', 'Bright Future Foundation', '2017-06-07', 'Active', (SELECT id FROM users WHERE username = 'caregiver_maria')),
('Jacob Harris', 10, 'Male', 'Bright Future Foundation', '2015-02-14', 'Active', (SELECT id FROM users WHERE username = 'caregiver_maria')),
('Scarlett Martin', 7, 'Female', 'Children Care Initiative', '2018-10-31', 'Active', (SELECT id FROM users WHERE username = 'caregiver_maria')),
('Logan Martin', 9, 'Male', 'Children Care Initiative', '2016-05-22', 'Active', (SELECT id FROM users WHERE username = 'caregiver_maria')),
('Victoria Lee', 6, 'Female', 'Community Support Network', '2019-01-10', 'Active', (SELECT id FROM users WHERE username = 'caregiver_maria')),
('Samuel Lee', 11, 'Male', 'Community Support Network', '2014-04-26', 'Active', (SELECT id FROM users WHERE username = 'caregiver_maria')),
('Eleanor Clark', 8, 'Female', 'Hope for Tomorrow', '2017-07-19', 'Active', (SELECT id FROM users WHERE username = 'caregiver_maria')),
('Sebastian Clark', 10, 'Male', 'Hope for Tomorrow', '2015-12-01', 'Active', (SELECT id FROM users WHERE username = 'caregiver_maria')),
('Lillian Lewis', 7, 'Female', 'Children''s Welfare Association', '2018-11-13', 'Active', (SELECT id FROM users WHERE username = 'caregiver_maria')),
('Oliver Lewis', 9, 'Male', 'Children''s Welfare Association', '2016-09-04', 'Active', (SELECT id FROM users WHERE username = 'caregiver_maria')),
('Stella Walker', 6, 'Female', 'Youth Development Program', '2019-03-27', 'Active', (SELECT id FROM users WHERE username = 'caregiver_maria')),
('Benjamin Walker', 11, 'Male', 'Youth Development Program', '2014-10-15', 'Active', (SELECT id FROM users WHERE username = 'caregiver_maria'));

-- ====================================
-- VERIFY INSERTIONS
-- ====================================

-- Count caregivers
SELECT 'Total Caregivers:' AS Metric, COUNT(*) AS Count FROM users WHERE role = 'CAREGIVER' AND approved = 1;

-- Count children
SELECT 'Total Children:' AS Metric, COUNT(*) AS Count FROM children;

-- Children per caregiver
SELECT DISTINCT 
    u.username AS Caregiver,
    COUNT(c.id) AS ChildrenAssigned
FROM users u
LEFT JOIN children c ON u.id = c.assigned_caregiver_id
WHERE u.role = 'CAREGIVER' AND u.approved = 1
GROUP BY u.id, u.username
ORDER BY u.username;
