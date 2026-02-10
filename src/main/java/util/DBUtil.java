package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Central database utility — manages the SQLite connection,
 * auto-creates tables on first run, and seeds dummy data.
 */
public class DBUtil {

    private static final String DB_URL = "jdbc:sqlite:guardianlink.db";
    private static Connection connection;

    /**
     * Returns a singleton JDBC connection to the SQLite database.
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
        }
        return connection;
    }

    /**
     * Creates the required tables if they do not already exist,
     * and seeds dummy data on first run.
     */
    public static void initialize() {
        try {
            Connection conn = getConnection();
            Statement stmt = conn.createStatement();

            // ── Create tables ────────────────────────────────

            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS users (
                            id       INTEGER PRIMARY KEY AUTOINCREMENT,
                            username TEXT    UNIQUE,
                            password TEXT,
                            role     TEXT,
                            approved INTEGER
                        )
                    """);

            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS children (
                            id             INTEGER PRIMARY KEY AUTOINCREMENT,
                            name           TEXT,
                            age            INTEGER,
                            organization   TEXT,
                            gender         TEXT,
                            date_of_birth  TEXT,
                            status         TEXT
                        )
                    """);

            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS medical_records (
                            id                INTEGER PRIMARY KEY AUTOINCREMENT,
                            child_id          INTEGER,
                            blood_group       TEXT,
                            medical_condition TEXT,
                            last_checkup      TEXT
                        )
                    """);

            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS education_records (
                            id                     INTEGER PRIMARY KEY AUTOINCREMENT,
                            child_id               INTEGER,
                            school_name            TEXT,
                            grade                  TEXT,
                            attendance_percentage  REAL
                        )
                    """);

            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS donations (
                            id          INTEGER PRIMARY KEY AUTOINCREMENT,
                            donor_id    INTEGER,
                            child_id    INTEGER,
                            amount      REAL,
                            purpose     TEXT,
                            date        TEXT,
                            status      TEXT DEFAULT 'Completed'
                        )
                    """);

            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS system_logs (
                            id          INTEGER PRIMARY KEY AUTOINCREMENT,
                            event_type  TEXT,
                            description TEXT,
                            actor       TEXT,
                            timestamp   TEXT
                        )
                    """);

            // ── Seed data (only on first run) ────────────────

            var rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
            if (rs.next() && rs.getInt(1) == 0) {
                seedUsers(conn);
                seedChildren(conn);
                seedMedicalRecords(conn);
                seedEducationRecords(conn);
                seedDonations(conn);
                seedSystemLogs(conn);
            }
            rs.close();
            stmt.close();

        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── Seed Users ───────────────────────────────────────────

    private static void seedUsers(Connection conn) throws SQLException {
        String sql = "INSERT INTO users (username, password, role, approved) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);

        // System Admin (approved)
        ps.setString(1, "admin");
        ps.setString(2, PasswordUtil.hash("admin123"));
        ps.setString(3, "SYSTEM_ADMIN");
        ps.setInt(4, 1);
        ps.executeUpdate();

        // Organization Admin (approved)
        ps.setString(1, "orgadmin");
        ps.setString(2, PasswordUtil.hash("org123"));
        ps.setString(3, "ORGANIZATION_ADMIN");
        ps.setInt(4, 1);
        ps.executeUpdate();

        // Donor (approved)
        ps.setString(1, "donor");
        ps.setString(2, PasswordUtil.hash("donor123"));
        ps.setString(3, "DONOR");
        ps.setInt(4, 1);
        ps.executeUpdate();

        ps.close();
    }

    // ── Seed Children ────────────────────────────────────────

    private static void seedChildren(Connection conn) throws SQLException {
        String sql = "INSERT INTO children (name, age, organization, gender, date_of_birth, status) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);

        insertChild(ps, "Arif Rahman", 10, "Hope Foundation", "Male", "2016-03-15", "Active");
        insertChild(ps, "Fatima Begum", 8, "Hope Foundation", "Female", "2018-07-22", "Active");
        insertChild(ps, "Kamal Hossain", 12, "Bright Future NGO", "Male", "2014-01-10", "Active");
        insertChild(ps, "Ayesha Akter", 14, "Bright Future NGO", "Female", "2012-11-05", "Graduated");
        insertChild(ps, "Rafi Islam", 6, "Hope Foundation", "Male", "2020-05-30", "Active");

        ps.close();
    }

    private static void insertChild(PreparedStatement ps, String name, int age,
            String org, String gender, String dob, String status) throws SQLException {
        ps.setString(1, name);
        ps.setInt(2, age);
        ps.setString(3, org);
        ps.setString(4, gender);
        ps.setString(5, dob);
        ps.setString(6, status);
        ps.executeUpdate();
    }

    // ── Seed Medical Records ─────────────────────────────────

    private static void seedMedicalRecords(Connection conn) throws SQLException {
        String sql = "INSERT INTO medical_records (child_id, blood_group, medical_condition, last_checkup) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);

        insertMedical(ps, 1, "B+", "None", "2025-12-10");
        insertMedical(ps, 2, "A-", "Mild asthma", "2025-11-20");
        insertMedical(ps, 3, "O+", "None", "2025-10-05");
        insertMedical(ps, 4, "AB+", "Iron deficiency anemia", "2025-09-18");
        insertMedical(ps, 5, "B+", "None", "2026-01-15");

        ps.close();
    }

    private static void insertMedical(PreparedStatement ps, int childId, String bloodGroup,
            String condition, String lastCheckup) throws SQLException {
        ps.setInt(1, childId);
        ps.setString(2, bloodGroup);
        ps.setString(3, condition);
        ps.setString(4, lastCheckup);
        ps.executeUpdate();
    }

    // ── Seed Education Records ───────────────────────────────

    private static void seedEducationRecords(Connection conn) throws SQLException {
        String sql = "INSERT INTO education_records (child_id, school_name, grade, attendance_percentage) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);

        insertEducation(ps, 1, "Green Valley Primary School", "Grade 4", 92.5);
        insertEducation(ps, 2, "Green Valley Primary School", "Grade 2", 88.0);
        insertEducation(ps, 3, "Sunrise Academy", "Grade 6", 95.3);
        insertEducation(ps, 4, "Sunrise Academy", "Grade 9", 91.0);
        insertEducation(ps, 5, "Green Valley Primary School", "Grade 1", 85.7);

        ps.close();
    }

    private static void insertEducation(PreparedStatement ps, int childId, String school,
            String grade, double attendance) throws SQLException {
        ps.setInt(1, childId);
        ps.setString(2, school);
        ps.setString(3, grade);
        ps.setDouble(4, attendance);
        ps.executeUpdate();
    }

    // ── Seed Donations ───────────────────────────────────────

    private static void seedDonations(Connection conn) throws SQLException {
        String sql = "INSERT INTO donations (donor_id, child_id, amount, purpose, date, status) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);

        insertDonation(ps, 3, 1, 150.0, "Education Support", "2026-01-20", "Completed");
        insertDonation(ps, 3, 2, 200.0, "Medical Care", "2026-01-20", "Completed");
        insertDonation(ps, 3, 3, 180.0, "General Welfare", "2026-01-15", "Completed");
        insertDonation(ps, 3, 1, 150.0, "Food & Nutrition", "2026-01-05", "Completed");
        insertDonation(ps, 3, 2, 150.0, "Education Support", "2025-12-20", "Completed");

        ps.close();
    }

    private static void insertDonation(PreparedStatement ps, int donorId, int childId, double amount,
            String purpose, String date, String status) throws SQLException {
        ps.setInt(1, donorId);
        ps.setInt(2, childId);
        ps.setDouble(3, amount);
        ps.setString(4, purpose);
        ps.setString(5, date);
        ps.setString(6, status);
        ps.executeUpdate();
    }

    // ── Seed System Logs ─────────────────────────────────────

    private static void seedSystemLogs(Connection conn) throws SQLException {
        String sql = "INSERT INTO system_logs (event_type, description, actor, timestamp) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);

        insertLog(ps, "User Login", "System Admin logged in", "admin", "2026-02-10 09:00:00");
        insertLog(ps, "Data Update", "Updated Child Profile CH-1024", "orgadmin", "2026-02-10 10:15:00");
        insertLog(ps, "Security Alert", "Failed login attempt detected", "unknown", "2026-02-09 23:45:00");
        insertLog(ps, "System", "Database backup completed", "system", "2026-02-10 02:00:00");
        insertLog(ps, "User Management", "New donor account created", "admin", "2026-02-08 14:30:00");

        ps.close();
    }

    private static void insertLog(PreparedStatement ps, String type, String desc, String actor, String time)
            throws SQLException {
        ps.setString(1, type);
        ps.setString(2, desc);
        ps.setString(3, actor);
        ps.setString(4, time);
        ps.executeUpdate();
    }
}
