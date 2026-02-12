package repository;

import util.DBUtil;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Data-access layer for the role_permissions table.
 */
public class RolePermissionsRepository {

    /**
     * Returns all role permissions as a Map.
     */
    public Map<String, String> findAll() {
        Map<String, String> permissions = new HashMap<>();
        String sql = "SELECT role_name, permissions FROM role_permissions";
        try {
            Statement stmt = DBUtil.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                permissions.put(rs.getString("role_name"), rs.getString("permissions"));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return permissions;
    }

    /**
     * Updates permissions for a specific role.
     * 
     * @return true if updated successfully, false otherwise
     */
    public boolean updatePermissions(String roleName, String permissions) {
        String sql = "UPDATE role_permissions SET permissions = ? WHERE role_name = ?";
        try {
            PreparedStatement ps = DBUtil.getConnection().prepareStatement(sql);
            ps.setString(1, permissions);
            ps.setString(2, roleName);
            int rowsAffected = ps.executeUpdate();
            ps.close();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Failed to update role permissions: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Gets permissions for a specific role.
     */
    public String getPermissions(String roleName) {
        String sql = "SELECT permissions FROM role_permissions WHERE role_name = ?";
        try {
            PreparedStatement ps = DBUtil.getConnection().prepareStatement(sql);
            ps.setString(1, roleName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String permissions = rs.getString("permissions");
                rs.close();
                ps.close();
                return permissions;
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }
}
