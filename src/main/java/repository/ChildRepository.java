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
     * Inserts a new child into the database.
     */
    public void save(Child child) {
        String sql = "INSERT INTO children (name, age, organization, gender, date_of_birth, status) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = DBUtil.getConnection().prepareStatement(sql);
            ps.setString(1, child.getName());
            ps.setInt(2, child.getAge());
            ps.setString(3, child.getOrganization());
            ps.setString(4, child.getGender());
            ps.setString(5, child.getDateOfBirth());
            ps.setString(6, child.getStatus() != null ? child.getStatus() : "Active");
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
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
        return child;
    }
}
