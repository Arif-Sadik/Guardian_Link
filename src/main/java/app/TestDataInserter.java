package app;

import model.entity.Child;
import model.user.Caregiver;
import model.user.User;
import repository.ChildRepository;
import repository.UserRepository;
import service.SystemLogService;
import util.DBUtil;

import java.sql.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility to insert test data: 3 new caregivers and 12 children per caregiver
 */
public class TestDataInserter {

    private static final UserRepository userRepo = new UserRepository();
    private static final ChildRepository childRepo = new ChildRepository();
    private static final SystemLogService sysLogService = new SystemLogService();

    public static void main(String[] args) {
        try {
            DBUtil.initialize();
            
            System.out.println("=== GUARDIAN LINK TEST DATA INSERTION ===\n");
            
            // Check existing users
            System.out.println("Checking existing caregivers...");
            
            // Delete old test caregivers if they exist
            System.out.println("\n--- Cleaning up old test caregivers ---");
            deleteOldTestCaregivers();
            
            listExistingCaregivers();
            
            // Create 3 new caregivers with Bangladeshi names
            System.out.println("\n--- Creating 3 New Caregivers ---");
            int caregiverId1 = createCaregiver("caregiver_jalal", "care123", "jalal.caregiver@guardianlink.com", "1234567890");
            int caregiverId2 = createCaregiver("caregiver_karim", "care123", "karim.caregiver@guardianlink.com", "9876543210");
            int caregiverId3 = createCaregiver("caregiver_nasrin", "care123", "nasrin.caregiver@guardianlink.com", "5551234567");
            
            // Create and assign children to each caregiver
            System.out.println("\n--- Creating Children for caregiver_jalal ---");
            createChildrenForCaregiver(caregiverId1, "caregiver_jalal", new String[][] {
                {"Emma Johnson", "8", "Female", "Bright Future Foundation", "2017-05-15"},
                {"Ethan Johnson", "10", "Male", "Bright Future Foundation", "2015-03-20"},
                {"Sophia Davis", "7", "Female", "Children Care Initiative", "2018-07-10"},
                {"Jackson Davis", "9", "Male", "Children Care Initiative", "2016-11-25"},
                {"Olivia Wilson", "6", "Female", "Community Support Network", "2019-02-14"},
                {"Noah Wilson", "11", "Male", "Community Support Network", "2014-08-30"},
                {"Ava Martinez", "8", "Female", "Hope for Tomorrow", "2017-12-05"},
                {"Liam Martinez", "10", "Male", "Hope for Tomorrow", "2015-09-18"},
                {"Isabella Garcia", "7", "Female", "Children's Welfare Association", "2018-04-22"},
                {"Mason Garcia", "9", "Male", "Children's Welfare Association", "2016-06-13"},
                {"Mia Rodriguez", "6", "Female", "Youth Development Program", "2019-10-30"},
                {"Lucas Rodriguez", "11", "Male", "Youth Development Program", "2014-01-07"}
            });
            
            System.out.println("\n--- Creating Children for caregiver_karim ---");
            createChildrenForCaregiver(caregiverId2, "caregiver_karim", new String[][] {
                {"Charlotte Brown", "8", "Female", "Bright Future Foundation", "2017-01-28"},
                {"Benjamin Brown", "10", "Male", "Bright Future Foundation", "2015-04-12"},
                {"Amelia Taylor", "7", "Female", "Children Care Initiative", "2018-09-09"},
                {"Henry Taylor", "9", "Male", "Children Care Initiative", "2016-02-20"},
                {"Harper Anderson", "6", "Female", "Community Support Network", "2019-05-16"},
                {"Michael Anderson", "11", "Male", "Community Support Network", "2014-12-03"},
                {"Evelyn Thomas", "8", "Female", "Hope for Tomorrow", "2017-08-11"},
                {"Alexander Thomas", "10", "Male", "Hope for Tomorrow", "2015-11-24"},
                {"Abigail Jackson", "7", "Female", "Children's Welfare Association", "2018-03-08"},
                {"Daniel Jackson", "9", "Male", "Children's Welfare Association", "2016-07-17"},
                {"Elizabeth White", "6", "Female", "Youth Development Program", "2019-09-02"},
                {"James White", "11", "Male", "Youth Development Program", "2014-11-19"}
            });
            
            System.out.println("\n--- Creating Children for caregiver_nasrin ---");
            createChildrenForCaregiver(caregiverId3, "caregiver_nasrin", new String[][] {
                {"Grace Harris", "8", "Female", "Bright Future Foundation", "2017-06-07"},
                {"Jacob Harris", "10", "Male", "Bright Future Foundation", "2015-02-14"},
                {"Scarlett Martin", "7", "Female", "Children Care Initiative", "2018-10-31"},
                {"Logan Martin", "9", "Male", "Children Care Initiative", "2016-05-22"},
                {"Victoria Lee", "6", "Female", "Community Support Network", "2019-01-10"},
                {"Samuel Lee", "11", "Male", "Community Support Network", "2014-04-26"},
                {"Eleanor Clark", "8", "Female", "Hope for Tomorrow", "2017-07-19"},
                {"Sebastian Clark", "10", "Male", "Hope for Tomorrow", "2015-12-01"},
                {"Lillian Lewis", "7", "Female", "Children's Welfare Association", "2018-11-13"},
                {"Oliver Lewis", "9", "Male", "Children's Welfare Association", "2016-09-04"},
                {"Stella Walker", "6", "Female", "Youth Development Program", "2019-03-27"},
                {"Benjamin Walker", "11", "Male", "Youth Development Program", "2014-10-15"}
            });
            
            // Display summary
            System.out.println("\n=== SUMMARY ===");
            listExistingCaregivers();
            
            System.out.println("\n=== LOGIN CREDENTIALS FOR NEW CAREGIVERS ===");
            System.out.println("Caregiver 1: caregiver_jalal / care123");
            System.out.println("Caregiver 2: caregiver_karim / care123");
            System.out.println("Caregiver 3: caregiver_nasrin / care123");
            
        } catch (Exception e) {
            System.err.println("Error during test data insertion: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void deleteOldTestCaregivers() {
        try {
            String[] oldUsernames = {"caregiver_sarah", "caregiver_james", "caregiver_maria"};
            Connection conn = DBUtil.getConnection();
            for (String oldUsername : oldUsernames) {
                String sql = "DELETE FROM users WHERE username = ? AND role = 'CAREGIVER'";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, oldUsername);
                int deleted = ps.executeUpdate();
                if (deleted > 0) {
                    System.out.println("✓ Deleted old caregiver: " + oldUsername);
                }
                ps.close();
            }
        } catch (SQLException e) {
            System.out.println("✓ No old caregivers to delete or already deleted");
        }
    }

    private static int createCaregiver(String username, String password, String email, String phone) {
        try {
            // Check if user already exists
            User existing = userRepo.findByUsername(username);
            if (existing != null) {
                System.out.println("✓ Caregiver '" + username + "' already exists (ID: " + existing.getId() + ")");
                return existing.getId();
            }
            
            // Create new caregiver
            String sql = "INSERT INTO users (username, password, email, phone_number, role, approved) VALUES (?, ?, ?, ?, ?, ?)";
            Connection conn = DBUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, email);
            ps.setString(4, phone);
            ps.setString(5, "CAREGIVER");
            ps.setInt(6, 1); // approved
            
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            int caregiverId = 0;
            if (keys.next()) {
                caregiverId = keys.getInt(1);
            }
            ps.close();
            
            System.out.println("✓ Created caregiver '" + username + "' (ID: " + caregiverId + ")");
            return caregiverId;
        } catch (SQLException e) {
            System.err.println("✗ Failed to create caregiver '" + username + "': " + e.getMessage());
            return -1;
        }
    }

    private static void createChildrenForCaregiver(int caregiverId, String caregiverUsername, String[][] childrenData) {
        int count = 0;
        for (String[] data : childrenData) {
            try {
                String name = data[0];
                int age = Integer.parseInt(data[1]);
                String gender = data[2];
                String organization = data[3];
                String dob = data[4];
                
                String sql = "INSERT INTO children (name, age, gender, organization, date_of_birth, status, assigned_caregiver_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
                Connection conn = DBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, name);
                ps.setInt(2, age);
                ps.setString(3, gender);
                ps.setString(4, organization);
                ps.setString(5, dob);
                ps.setString(6, "Active");
                ps.setInt(7, caregiverId);
                
                ps.executeUpdate();
                ps.close();
                count++;
                System.out.println("  ✓ Created child: " + name + " (Age: " + age + ", Assigned to: " + caregiverUsername + ")");
            } catch (Exception e) {
                System.err.println("  ✗ Failed to create child: " + e.getMessage());
            }
        }
        System.out.println("  Total children created for " + caregiverUsername + ": " + count);
    }

    private static void listExistingCaregivers() {
        try {
            String sql = "SELECT id, username, password, email, phone_number FROM users WHERE role = 'CAREGIVER' AND approved = 1 ORDER BY id";
            Connection conn = DBUtil.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            System.out.println("\nCaregivers in Database:");
            System.out.println("ID | Username | Password | Email | Phone");
            System.out.println("---+----------+----------+-------+-------");
            
            while (rs.next()) {
                int id = rs.getInt("id");
                String username = rs.getString("username");
                String password = rs.getString("password");
                String email = rs.getString("email");
                String phone = rs.getString("phone_number");
                System.out.printf("%d | %s | %s | %s | %s%n", id, username, password, email, phone);
                
                // Count children for this caregiver
                String countSql = "SELECT COUNT(*) as child_count FROM children WHERE assigned_caregiver_id = ?";
                PreparedStatement countPs = conn.prepareStatement(countSql);
                countPs.setInt(1, id);
                ResultSet countRs = countPs.executeQuery();
                if (countRs.next()) {
                    int childCount = countRs.getInt("child_count");
                    System.out.println("    → Assigned children: " + childCount);
                }
                countRs.close();
                countPs.close();
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Error listing caregivers: " + e.getMessage());
        }
    }
}
