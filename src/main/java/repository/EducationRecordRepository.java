package repository;

import model.entity.EducationRecord;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data-access layer for the education_records table.
 */
public class EducationRecordRepository {

    /**
     * Finds all education records for a given child.
     */
    public List<EducationRecord> findByChildId(int childId) {
        List<EducationRecord> records = new ArrayList<>();
        String sql = "SELECT * FROM education_records WHERE child_id = ?";
        try {
            PreparedStatement ps = DBUtil.getConnection().prepareStatement(sql);
            ps.setInt(1, childId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                EducationRecord record = new EducationRecord(
                        rs.getInt("child_id"),
                        rs.getString("school_name"),
                        rs.getString("grade"),
                        rs.getDouble("attendance_percentage"));
                record.setId(rs.getInt("id"));
                records.add(record);
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }

    /**
     * Inserts a new education record.
     */
    public void save(EducationRecord record) {
        String sql = "INSERT INTO education_records (child_id, school_name, grade, attendance_percentage) VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement ps = DBUtil.getConnection().prepareStatement(sql);
            ps.setInt(1, record.getChildId());
            ps.setString(2, record.getSchoolName());
            ps.setString(3, record.getGrade());
            ps.setDouble(4, record.getAttendancePercentage());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
