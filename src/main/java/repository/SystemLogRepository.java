package repository;

import model.entity.SystemLog;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data-access layer for the system_logs table.
 */
public class SystemLogRepository {

    /**
     * Returns all system logs, most recent first.
     */
    public List<SystemLog> findAll() {
        List<SystemLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM system_logs ORDER BY timestamp DESC";
        try {
            Statement stmt = DBUtil.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                logs.add(mapRow(rs));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }

    /**
     * Returns the most recent N log entries.
     */
    public List<SystemLog> findRecent(int limit) {
        List<SystemLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM system_logs ORDER BY timestamp DESC LIMIT ?";
        try {
            PreparedStatement ps = DBUtil.getConnection().prepareStatement(sql);
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                logs.add(mapRow(rs));
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }

    /**
     * Returns the total count of system logs.
     */
    public int count() {
        String sql = "SELECT COUNT(*) FROM system_logs";
        try {
            Statement stmt = DBUtil.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                int c = rs.getInt(1);
                rs.close();
                stmt.close();
                return c;
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Inserts a new system log entry.
     */
    public void save(SystemLog log) {
        String sql = "INSERT INTO system_logs (event_type, description, actor, timestamp) VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement ps = DBUtil.getConnection().prepareStatement(sql);
            ps.setString(1, log.getEventType());
            ps.setString(2, log.getDescription());
            ps.setString(3, log.getActor());
            ps.setString(4, log.getTimestamp());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private SystemLog mapRow(ResultSet rs) throws SQLException {
        SystemLog log = new SystemLog();
        log.setId(rs.getInt("id"));
        log.setEventType(rs.getString("event_type"));
        log.setDescription(rs.getString("description"));
        log.setActor(rs.getString("actor"));
        log.setTimestamp(rs.getString("timestamp"));
        return log;
    }
}
