package repository;

import model.entity.Notification;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data-access layer for the notifications table.
 */
public class NotificationRepository {

    /**
     * Saves a new notification to the database.
     */
    public boolean save(Notification notification) {
        String sql = "INSERT INTO notifications (caregiver_id, message, notification_type, child_name, child_id, timestamp, is_read) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = DBUtil.getConnection().prepareStatement(sql);
            ps.setInt(1, notification.getCaregiverId());
            ps.setString(2, notification.getMessage());
            ps.setString(3, notification.getNotificationType());
            ps.setString(4, notification.getChildName());
            ps.setInt(5, notification.getChildId());
            ps.setString(6, notification.getTimestamp());
            ps.setBoolean(7, notification.isRead());
            int rows = ps.executeUpdate();
            ps.close();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves all notifications for a specific caregiver.
     */
    public List<Notification> findByCaregiver(int caregiverId) {
        List<Notification> notifications = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE caregiver_id = ? ORDER BY timestamp DESC";
        try {
            PreparedStatement ps = DBUtil.getConnection().prepareStatement(sql);
            ps.setInt(1, caregiverId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                notifications.add(mapRow(rs));
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notifications;
    }

    /**
     * Retrieves unread notifications for a caregiver.
     */
    public List<Notification> findUnreadByCaregiver(int caregiverId) {
        List<Notification> notifications = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE caregiver_id = ? AND is_read = false ORDER BY timestamp DESC";
        try {
            PreparedStatement ps = DBUtil.getConnection().prepareStatement(sql);
            ps.setInt(1, caregiverId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                notifications.add(mapRow(rs));
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notifications;
    }

    /**
     * Marks a notification as read.
     */
    public boolean markAsRead(int notificationId) {
        String sql = "UPDATE notifications SET is_read = true WHERE id = ?";
        try {
            PreparedStatement ps = DBUtil.getConnection().prepareStatement(sql);
            ps.setInt(1, notificationId);
            int rows = ps.executeUpdate();
            ps.close();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Marks all notifications for a caregiver as read.
     */
    public boolean markAllAsRead(int caregiverId) {
        String sql = "UPDATE notifications SET is_read = true WHERE caregiver_id = ?";
        try {
            PreparedStatement ps = DBUtil.getConnection().prepareStatement(sql);
            ps.setInt(1, caregiverId);
            int rows = ps.executeUpdate();
            ps.close();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deletes a notification by ID.
     */
    public boolean deleteById(int notificationId) {
        String sql = "DELETE FROM notifications WHERE id = ?";
        try {
            PreparedStatement ps = DBUtil.getConnection().prepareStatement(sql);
            ps.setInt(1, notificationId);
            int rows = ps.executeUpdate();
            ps.close();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Counts unread notifications for a caregiver.
     */
    public int countUnread(int caregiverId) {
        String sql = "SELECT COUNT(*) FROM notifications WHERE caregiver_id = ? AND is_read = false";
        try {
            PreparedStatement ps = DBUtil.getConnection().prepareStatement(sql);
            ps.setInt(1, caregiverId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Maps a ResultSet row to a Notification object.
     */
    private Notification mapRow(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setId(rs.getInt("id"));
        n.setCaregiverId(rs.getInt("caregiver_id"));
        n.setMessage(rs.getString("message"));
        n.setNotificationType(rs.getString("notification_type"));
        n.setChildName(rs.getString("child_name"));
        n.setChildId(rs.getInt("child_id"));
        n.setTimestamp(rs.getString("timestamp"));
        n.setRead(rs.getBoolean("is_read"));
        return n;
    }
}
