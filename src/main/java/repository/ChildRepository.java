package repository;

import model.entity.Child;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data-access layer for the children table.
 * Updated to support extended child fields.
 */
public class ChildRepository {

    /**
     * Returns all children from the database.
     */
    public List<Child> findAll() {
        List<Child> children = new ArrayList<>();
        String sql = "SELECT * FROM children";
        try {
            Statement stmt = DBUtil.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                children.add(mapRow(rs));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return children;
    }

    /**
     * Finds a single child by ID, or returns null.
     */
    public Child findById(int id) {
        String sql = "SELECT * FROM children WHERE id = ?";
        try {
            PreparedStatement ps = DBUtil.getConnection().prepareStatement(sql);
            ps.setInt(1, id);
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
     * Inserts a new child into the database and returns the generated ID.
     */
    public int save(Child child) {
        // Try with the new schema first (with assigned_caregiver_id)
        String sql = "INSERT INTO children (name, age, organization, gender, date_of_birth, status, assigned_caregiver_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = DBUtil.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, child.getName());
            ps.setInt(2, child.getAge());
            ps.setString(3, child.getOrganization());
            ps.setString(4, child.getGender());
            ps.setString(5, child.getDateOfBirth());
            ps.setString(6, child.getStatus() != null ? child.getStatus() : "Active");
            if (child.getAssignedCaregiverId() != null) {
                ps.setInt(7, child.getAssignedCaregiverId());
            } else {
                ps.setNull(7, java.sql.Types.INTEGER);
            }
            ps.executeUpdate();
            ResultSet generatedKeys = ps.getGeneratedKeys();
            if (generatedKeys.next()) {
                int id = generatedKeys.getInt(1);
                ps.close();
                return id;
            }
            ps.close();
            return -1;
        } catch (SQLException e) {
            // If the column doesn't exist, try without it
            String fallbackSql = "INSERT INTO children (name, age, organization, gender, date_of_birth, status) VALUES (?, ?, ?, ?, ?, ?)";
            try {
                PreparedStatement ps = DBUtil.getConnection().prepareStatement(fallbackSql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, child.getName());
                ps.setInt(2, child.getAge());
                ps.setString(3, child.getOrganization());
                ps.setString(4, child.getGender());
                ps.setString(5, child.getDateOfBirth());
                ps.setString(6, child.getStatus() != null ? child.getStatus() : "Active");
                ps.executeUpdate();
                ResultSet generatedKeys = ps.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    ps.close();
                    return id;
                }
                ps.close();
                return -1;
            } catch (SQLException fallbackError) {
                fallbackError.printStackTrace();
                return -1;
            }
        }
    }

    /**
     * Updates an existing child record.
     */
    public boolean updateChild(Child child) {
        String sql = "UPDATE children SET name = ?, age = ?, organization = ?, gender = ?, date_of_birth = ?, status = ?, assigned_caregiver_id = ? WHERE id = ?";
        try {
            PreparedStatement ps = DBUtil.getConnection().prepareStatement(sql);
            ps.setString(1, child.getName());
            ps.setInt(2, child.getAge());
            ps.setString(3, child.getOrganization());
            ps.setString(4, child.getGender());
            ps.setString(5, child.getDateOfBirth());
            ps.setString(6, child.getStatus());
            if (child.getAssignedCaregiverId() != null) {
                ps.setInt(7, child.getAssignedCaregiverId());
            } else {
                ps.setNull(7, java.sql.Types.INTEGER);
            }
            ps.setInt(8, child.getId());
            int rows = ps.executeUpdate();
            ps.close();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deletes a child by ID.
     */
    public boolean deleteChild(int id) {
        String sql = "DELETE FROM children WHERE id = ?";
        try {
            PreparedStatement ps = DBUtil.getConnection().prepareStatement(sql);
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            ps.close();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Finds all children assigned to a specific caregiver.
     */
    public List<Child> findByCaregiver(int caregiverId) {
        List<Child> children = new ArrayList<>();
        String sql = "SELECT * FROM children WHERE assigned_caregiver_id = ?";
        try {
            PreparedStatement ps = DBUtil.getConnection().prepareStatement(sql);
            ps.setInt(1, caregiverId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                children.add(mapRow(rs));
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return children;
    }

    /**
     * Removes caregiver assignment from a child (sets to null).
     */
    public boolean removeCaregiverAssignment(int childId) {
        String sql = "UPDATE children SET assigned_caregiver_id = NULL WHERE id = ?";
        try {
            PreparedStatement ps = DBUtil.getConnection().prepareStatement(sql);
            ps.setInt(1, childId);
            int rows = ps.executeUpdate();
            ps.close();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Maps a ResultSet row to a Child object.
     */
    private Child mapRow(ResultSet rs) throws SQLException {
        Child child = new Child();
        child.setId(rs.getInt("id"));
        child.setName(rs.getString("name"));
        child.setAge(rs.getInt("age"));
        child.setOrganization(rs.getString("organization"));
        child.setGender(rs.getString("gender"));
        child.setDateOfBirth(rs.getString("date_of_birth"));
        child.setStatus(rs.getString("status"));
        
        // Handle assigned caregiver ID (nullable and may not exist in all systems)
        try {
            Object caregiverId = rs.getObject("assigned_caregiver_id");
            if (caregiverId != null) {
                child.setAssignedCaregiverId((Integer) caregiverId);
            }
        } catch (SQLException e) {
            // Column doesn't exist, skip it
        }
        
        return child;
    }
}
