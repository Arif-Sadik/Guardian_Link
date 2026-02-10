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
     */
    public void save(User user) {
        String sql = "INSERT INTO users (username, password, role, approved) VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement ps = DBUtil.getConnection().prepareStatement(sql);
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getRole().name());
            ps.setInt(4, user.isApproved() ? 1 : 0);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
            default -> throw new IllegalStateException("Unknown role: " + role);
        }
        ;
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setRole(role);
        user.setApproved(rs.getInt("approved") == 1);
        return user;
    }
}
