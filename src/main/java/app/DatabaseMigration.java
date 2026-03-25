package app;

import util.DBUtil;
import java.sql.*;

/**
 * Utility to migrate database schema - adds new columns and tables for caregiver assignment feature
 */
public class DatabaseMigration {

    public static void main(String[] args) {
        try {
            DBUtil.initialize();
            Connection conn = DBUtil.getConnection();
            
            System.out.println("=== GUARDIAN LINK DATABASE MIGRATION ===\n");
            
            // Migrate users table - add phone_number column if not exists
            System.out.println("1. Checking users table schema...");
            migrateUsersTable(conn);
            
            // Migrate children table - add assigned_caregiver_id column if not exists
            System.out.println("\n2. Checking children table schema...");
            migrateChildrenTable(conn);
            
            // Create notifications table if not exists
            System.out.println("\n3. Checking notifications table...");
            createNotificationsTable(conn);
            
            // Create index on assigned_caregiver_id
            System.out.println("\n4. Creating indexes...");
            createIndexes(conn);
            
            System.out.println("\n=== MIGRATION COMPLETE ===");
            conn.close();
            
        } catch (Exception e) {
            System.err.println("Migration failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void migrateUsersTable(Connection conn) {
        try {
            DatabaseMetaData metadata = conn.getMetaData();
            Connection connRef = conn;
            
            // Check if phone_number column exists
            ResultSet columns = metadata.getColumns(null, null, "users", "phone_number");
            if (!columns.next()) {
                String sql = "ALTER TABLE users ADD COLUMN phone_number VARCHAR(20) DEFAULT NULL";
                Statement stmt = connRef.createStatement();
                stmt.execute(sql);
                stmt.close();
                System.out.println("✓ Added phone_number column to users table");
            } else {
                System.out.println("✓ phone_number column already exists in users table");
            }
            columns.close();
            
            // Check if organization column exists
            ResultSet orgColumns = metadata.getColumns(null, null, "users", "organization");
            if (!orgColumns.next()) {
                String sql = "ALTER TABLE users ADD COLUMN organization VARCHAR(255) DEFAULT NULL";
                Statement stmt = connRef.createStatement();
                stmt.execute(sql);
                stmt.close();
                System.out.println("✓ Added organization column to users table");
            } else {
                System.out.println("✓ organization column already exists in users table");
            }
            orgColumns.close();
        } catch (SQLException e) {
            if (e.getMessage().contains("already")) {
                System.out.println("✓ phone_number column already exists");
            } else {
                System.err.println("✗ Error migrating users table: " + e.getMessage());
            }
        }
    }

    private static void migrateChildrenTable(Connection conn) {
        try {
            // Check if assigned_caregiver_id column exists
            DatabaseMetaData metadata = conn.getMetaData();
            ResultSet columns = metadata.getColumns(null, null, "children", "assigned_caregiver_id");
            
            if (!columns.next()) {
                // Column doesn't exist, add it
                String sql = "ALTER TABLE children ADD COLUMN assigned_caregiver_id INTEGER DEFAULT NULL";
                Statement stmt = conn.createStatement();
                stmt.execute(sql);
                stmt.close();
                System.out.println("✓ Added assigned_caregiver_id column to children table");
            } else {
                System.out.println("✓ assigned_caregiver_id column already exists in children table");
            }
            columns.close();
        } catch (SQLException e) {
            if (e.getMessage().contains("already")) {
                System.out.println("✓ assigned_caregiver_id column already exists");
            } else {
                System.err.println("✗ Error migrating children table: " + e.getMessage());
            }
        }
    }

    private static void createNotificationsTable(Connection conn) {
        try {
            // Check if notifications table exists
            DatabaseMetaData metadata = conn.getMetaData();
            ResultSet tables = metadata.getTables(null, null, "notifications", null);
            
            if (!tables.next()) {
                // Table doesn't exist, create it
                String sql = "CREATE TABLE notifications (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "caregiver_id INTEGER NOT NULL, " +
                        "message VARCHAR(500) NOT NULL, " +
                        "notification_type VARCHAR(50) NOT NULL, " +
                        "child_name VARCHAR(255) NOT NULL, " +
                        "child_id INTEGER NOT NULL, " +
                        "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                        "is_read TINYINT DEFAULT 0, " +
                        "FOREIGN KEY (caregiver_id) REFERENCES users(id) ON DELETE CASCADE, " +
                        "FOREIGN KEY (child_id) REFERENCES children(id) ON DELETE CASCADE" +
                        ")";
                Statement stmt = conn.createStatement();
                stmt.execute(sql);
                stmt.close();
                System.out.println("✓ Created notifications table");
            } else {
                System.out.println("✓ notifications table already exists");
            }
            tables.close();
        } catch (SQLException e) {
            if (e.getMessage().contains("already")) {
                System.out.println("✓ notifications table already exists");
            } else {
                System.err.println("✗ Error creating notifications table: " + e.getMessage());
            }
        }
    }

    private static void createIndexes(Connection conn) {
        try {
            // Create index on assigned_caregiver_id in children table
            String indexSql1 = "CREATE INDEX IF NOT EXISTS idx_assigned_caregiver_id ON children(assigned_caregiver_id)";
            Statement stmt1 = conn.createStatement();
            stmt1.execute(indexSql1);
            stmt1.close();
            System.out.println("✓ Created index on children.assigned_caregiver_id");
            
            // Create index on caregiver_id in notifications table
            String indexSql2 = "CREATE INDEX IF NOT EXISTS idx_notification_caregiver_id ON notifications(caregiver_id)";
            Statement stmt2 = conn.createStatement();
            stmt2.execute(indexSql2);
            stmt2.close();
            System.out.println("✓ Created index on notifications.caregiver_id");
            
            // Create index on is_read in notifications table
            String indexSql3 = "CREATE INDEX IF NOT EXISTS idx_notification_is_read ON notifications(is_read)";
            Statement stmt3 = conn.createStatement();
            stmt3.execute(indexSql3);
            stmt3.close();
            System.out.println("✓ Created index on notifications.is_read");
            
        } catch (SQLException e) {
            if (e.getMessage().contains("already")) {
                System.out.println("✓ Indexes already exist");
            } else {
                System.err.println("✗ Error creating indexes: " + e.getMessage());
            }
        }
    }
}
