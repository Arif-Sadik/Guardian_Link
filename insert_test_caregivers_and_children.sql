-- ====================================
-- Test Data: 3 New Caregivers + Children Assignment
-- Guardian Link Database
-- ====================================

-- INSERT 3 NEW CAREGIVERS
-- Note: Ensure these users are APPROVED (approved = 1) so they appear in dropdowns

INSERT INTO users (username, password, email, phone_number, role, approved) VALUES
('caregiver_jalal', 'care123', 'jalal.caregiver@guardianlink.com', '1234567890', 'CAREGIVER', 1),
('caregiver_karim', 'care123', 'karim.caregiver@guardianlink.com', '9876543210', 'CAREGIVER', 1),
('caregiver_nasrin', 'care123', 'nasrin.caregiver@guardianlink.com', '5551234567', 'CAREGIVER', 1);

-- Note: If you already have caregiver_john or similar, adjust usernames accordingly
-- The passwords should match the existing format (plain text stored in DB)

-- ====================================
-- INSERT CHILDREN FOR EACH CAREGIVER
-- ====================================

-- Get caregiver IDs (adjust these based on INSERT order - they should be sequential)
-- Assuming the 3 new caregivers get IDs starting from the next available ID after existing users
-- For this demo, we'll use variables or direct IDs

-- CHILDREN FOR CAREGIVER_JALAL
INSERT INTO children (name, age, gender, organization, date_of_birth, status, assigned_caregiver_id) VALUES
('Jarina Khan', 8, 'Female', 'Bright Future Foundation', '2017-05-15', 'Active', (SELECT id FROM users WHERE username = 'caregiver_jalal')),
('Tahsin Ahmed', 10, 'Male', 'Bright Future Foundation', '2015-03-20', 'Active', (SELECT id FROM users WHERE username = 'caregiver_jalal')),
('Nasrin Begum', 7, 'Female', 'Children Care Initiative', '2018-07-10', 'Active', (SELECT id FROM users WHERE username = 'caregiver_jalal')),
('Rafi Hassan', 9, 'Male', 'Children Care Initiative', '2016-11-25', 'Active', (SELECT id FROM users WHERE username = 'caregiver_jalal')),
('Salma Akhter', 6, 'Female', 'Community Support Network', '2019-02-14', 'Active', (SELECT id FROM users WHERE username = 'caregiver_jalal')),
('Imran Khan', 11, 'Male', 'Community Support Network', '2014-08-30', 'Active', (SELECT id FROM users WHERE username = 'caregiver_jalal')),
('Tania Rahman', 8, 'Female', 'Hope for Tomorrow', '2017-12-05', 'Active', (SELECT id FROM users WHERE username = 'caregiver_jalal')),
('Anwar Ali', 10, 'Male', 'Hope for Tomorrow', '2015-09-18', 'Active', (SELECT id FROM users WHERE username = 'caregiver_jalal')),
('Mina Saha', 7, 'Female', 'Children''s Welfare Association', '2018-04-22', 'Active', (SELECT id FROM users WHERE username = 'caregiver_jalal')),
('Sabbir Hossain', 9, 'Male', 'Children''s Welfare Association', '2016-06-13', 'Active', (SELECT id FROM users WHERE username = 'caregiver_jalal')),
('Nila Das', 6, 'Female', 'Youth Development Program', '2019-10-30', 'Active', (SELECT id FROM users WHERE username = 'caregiver_jalal')),
('Ripon Saha', 11, 'Male', 'Youth Development Program', '2014-01-07', 'Active', (SELECT id FROM users WHERE username = 'caregiver_jalal'));

-- CHILDREN FOR CAREGIVER_KARIM
INSERT INTO children (name, age, gender, organization, date_of_birth, status, assigned_caregiver_id) VALUES
('Fatima Begum', 8, 'Female', 'Bright Future Foundation', '2017-01-28', 'Active', (SELECT id FROM users WHERE username = 'caregiver_karim')),
('Azim Khan', 10, 'Male', 'Bright Future Foundation', '2015-04-12', 'Active', (SELECT id FROM users WHERE username = 'caregiver_karim')),
('Saima Akhter', 7, 'Female', 'Children Care Initiative', '2018-09-09', 'Active', (SELECT id FROM users WHERE username = 'caregiver_karim')),
('Tanvir Hassan', 9, 'Male', 'Children Care Initiative', '2016-02-20', 'Active', (SELECT id FROM users WHERE username = 'caregiver_karim')),
('Rifat Jahan', 6, 'Female', 'Community Support Network', '2019-05-16', 'Active', (SELECT id FROM users WHERE username = 'caregiver_karim')),
('Habib Rahman', 11, 'Male', 'Community Support Network', '2014-12-03', 'Active', (SELECT id FROM users WHERE username = 'caregiver_karim')),
('Hena Roy', 8, 'Female', 'Hope for Tomorrow', '2017-08-11', 'Active', (SELECT id FROM users WHERE username = 'caregiver_karim')),
('Kamal Ahmed', 10, 'Male', 'Hope for Tomorrow', '2015-11-24', 'Active', (SELECT id FROM users WHERE username = 'caregiver_karim')),
('Shazma Islam', 7, 'Female', 'Children''s Welfare Association', '2018-03-08', 'Active', (SELECT id FROM users WHERE username = 'caregiver_karim')),
('Rafiq Ali', 9, 'Male', 'Children''s Welfare Association', '2016-07-17', 'Active', (SELECT id FROM users WHERE username = 'caregiver_karim')),
('Durga Saha', 6, 'Female', 'Youth Development Program', '2019-09-02', 'Active', (SELECT id FROM users WHERE username = 'caregiver_karim')),
('Sameer Khan', 11, 'Male', 'Youth Development Program', '2014-11-19', 'Active', (SELECT id FROM users WHERE username = 'caregiver_karim'));

-- CHILDREN FOR CAREGIVER_NASRIN
INSERT INTO children (name, age, gender, organization, date_of_birth, status, assigned_caregiver_id) VALUES
('Sultana Begum', 8, 'Female', 'Bright Future Foundation', '2017-06-07', 'Active', (SELECT id FROM users WHERE username = 'caregiver_nasrin')),
('Akbar Khan', 10, 'Male', 'Bright Future Foundation', '2015-02-14', 'Active', (SELECT id FROM users WHERE username = 'caregiver_nasrin')),
('Yasmin Ahmed', 7, 'Female', 'Children Care Initiative', '2018-10-31', 'Active', (SELECT id FROM users WHERE username = 'caregiver_nasrin')),
('Hasan Ali', 9, 'Male', 'Children Care Initiative', '2016-05-22', 'Active', (SELECT id FROM users WHERE username = 'caregiver_nasrin')),
('Anjali Roy', 6, 'Female', 'Community Support Network', '2019-01-10', 'Active', (SELECT id FROM users WHERE username = 'caregiver_nasrin')),
('Ashraf Uddin', 11, 'Male', 'Community Support Network', '2014-04-26', 'Active', (SELECT id FROM users WHERE username = 'caregiver_nasrin')),
('Mina Chakraborty', 8, 'Female', 'Hope for Tomorrow', '2017-07-19', 'Active', (SELECT id FROM users WHERE username = 'caregiver_nasrin')),
('Rajesh Saha', 10, 'Male', 'Hope for Tomorrow', '2015-12-01', 'Active', (SELECT id FROM users WHERE username = 'caregiver_nasrin')),
('Priya Dey', 7, 'Female', 'Children''s Welfare Association', '2018-11-13', 'Active', (SELECT id FROM users WHERE username = 'caregiver_nasrin')),
('Sumon Islam', 9, 'Male', 'Children''s Welfare Association', '2016-09-04', 'Active', (SELECT id FROM users WHERE username = 'caregiver_nasrin')),
('Nora Hassan', 6, 'Female', 'Youth Development Program', '2019-03-27', 'Active', (SELECT id FROM users WHERE username = 'caregiver_nasrin')),
('Faraz Khan', 11, 'Male', 'Youth Development Program', '2014-10-15', 'Active', (SELECT id FROM users WHERE username = 'caregiver_nasrin'));

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
