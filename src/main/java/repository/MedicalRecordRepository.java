package repository;

import model.entity.MedicalRecord;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data-access layer for the medical_records table.
 */
public class MedicalRecordRepository {

    /**
     * Finds all medical records for a given child.
     */
    public List<MedicalRecord> findByChildId(int childId) {
        List<MedicalRecord> records = new ArrayList<>();
        String sql = "SELECT * FROM medical_records WHERE child_id = ?";
        try {
            PreparedStatement ps = DBUtil.getConnection().prepareStatement(sql);
            ps.setInt(1, childId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                MedicalRecord record = new MedicalRecord(
                        rs.getInt("child_id"),
                        rs.getString("blood_group"),
                        rs.getString("medical_condition"),
                        rs.getString("last_checkup"));
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
     * Inserts a new medical record.
     */
    public void save(MedicalRecord record) {
        String sql = "INSERT INTO medical_records (child_id, blood_group, medical_condition, last_checkup) VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement ps = DBUtil.getConnection().prepareStatement(sql);
            ps.setInt(1, record.getChildId());
            ps.setString(2, record.getBloodGroup());
            ps.setString(3, record.getMedicalCondition());
            ps.setString(4, record.getLastCheckup());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
