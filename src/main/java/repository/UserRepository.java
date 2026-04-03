package repository;

import model.user.*;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data-access layer for the users table.
 * All database operations use prepared statements.
 */
public class UserRepository {

    /**
     * Finds a user by username, or returns null if not found.
     */
    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try {
            PreparedStatement ps = DBUtil.getConnection().prepareStatement(sql);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns all users in the database.
     */
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try {
            Statement stmt = DBUtil.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                users.add(mapRow(rs));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    /**
     * Inserts a new user into the database.
     * 
     * @return true if saved successfully, false otherwise
     */
    public boolean save(User user) {
        String sql = "INSERT INTO users (username, password, email, phone_number, role, approved, organization) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = DBUtil.getConnection().prepareStatement(sql);
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPhoneNumber());
            ps.setString(5, user.getRole().name());
            ps.setInt(6, user.isApproved() ? 1 : 0);
            ps.setString(7, user.getOrganization());
            int rowsAffected = ps.executeUpdate();
            ps.close();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Failed to save user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Checks if a username already exists.
     */
    public boolean usernameExists(String username) {
        return findByUsername(username) != null;
    }

    /**
     * Approves a user by setting approved = 1.
     */
    public void approveUser(int userId) {
        String sql = "UPDATE users SET approved = 1 WHERE id = ?";
        try {
            PreparedStatement ps = DBUtil.getConnection().prepareStatement(sql);
            ps.setInt(1, userId);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes a user by their database ID.
     * 
     * @return true if deleted successfully, false otherwise
     */
    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE id = ?";
        try {
            PreparedStatement ps = DBUtil.getConnection().prepareStatement(sql);
            ps.setInt(1, userId);
            int rowsAffected = ps.executeUpdate();
            ps.close();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Failed to delete user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Updates an existing user's information (including password and email if
     * changed).
     * 
     * @return true if updated successfully, false otherwise
     */
    public boolean updateUser(User user) {
        String sql = "UPDATE users SET username = ?, password = ?, email = ?, phone_number = ?, organization = ?, profile_photo = ?, approved = ? WHERE id = ?";
        try {
            PreparedStatement ps = DBUtil.getConnection().prepareStatement(sql);
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPhoneNumber());
            ps.setString(5, user.getOrganization());
            ps.setString(6, user.getProfilePhoto());
            ps.setInt(7, user.isApproved() ? 1 : 0);
            ps.setInt(8, user.getId());
            int rowsAffected = ps.executeUpdate();
            ps.close();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Failed to update user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Updates an existing user's profile information.
     */
    public boolean updateProfile(User user) {
        String sql = "UPDATE users SET email = ?, phone_number = ?, organization = ?, profile_photo = ?, password = ? WHERE id = ?";
        try {
            PreparedStatement ps = DBUtil.getConnection().prepareStatement(sql);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getPhoneNumber());
            ps.setString(3, user.getOrganization());
            ps.setString(4, user.getProfilePhoto());
            ps.setString(5, user.getPassword());
            ps.setInt(6, user.getId());
            int rowsAffected = ps.executeUpdate();
            ps.close();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Failed to update profile: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Finds a user by their database ID.
     */
    public User findById(int userId) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try {
            PreparedStatement ps = DBUtil.getConnection().prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                User user = mapRow(rs);
                rs.close();
                ps.close();
                return user;
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Finds all caregivers in the system (approved).
     */
    public List<User> findAllCaregivers() {
        List<User> caregivers = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = ? AND approved = 1";
        try {
            PreparedStatement ps = DBUtil.getConnection().prepareStatement(sql);
            ps.setString(1, UserRole.CAREGIVER.name());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                caregivers.add(mapRow(rs));
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return caregivers;
    }

    // ── Helper ─────────────────────────────────────────────────

    /**
     * Maps a ResultSet row to the correct User subclass.
     */
    private User mapRow(ResultSet rs) throws SQLException {
        UserRole role = UserRole.valueOf(rs.getString("role"));
        User user;
        switch (role) {
            case SYSTEM_ADMIN -> user = new SystemAdmin();
            case ORGANIZATION_ADMIN -> user = new OrganizationAdmin();
            case DONOR -> user = new Donor();
            case CAREGIVER -> user = new Caregiver();
            case SUPPORT -> user = new Support();
            default -> throw new IllegalStateException("Unknown role: " + role);
        }
        ;
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setEmail(rs.getString("email"));
        user.setPhoneNumber(rs.getString("phone_number"));
        user.setRole(role);
        user.setApproved(rs.getInt("approved") == 1);
        user.setOrganization(rs.getString("organization"));
        user.setProfilePhoto(rs.getString("profile_photo"));
        return user;
    }
}
