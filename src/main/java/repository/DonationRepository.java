package repository;

import model.entity.Donation;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data-access layer for the donations table.
 */
public class DonationRepository {

    /**
     * Returns all donations.
     */
    public List<Donation> findAll() {
        List<Donation> donations = new ArrayList<>();
        String sql = "SELECT * FROM donations ORDER BY date DESC";
        try {
            Statement stmt = DBUtil.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                donations.add(mapRow(rs));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return donations;
    }

    /**
     * Returns all donations by a specific donor.
     */
    public List<Donation> findByDonorId(int donorId) {
        List<Donation> donations = new ArrayList<>();
        String sql = "SELECT * FROM donations WHERE donor_id = ? ORDER BY date DESC";
        try {
            PreparedStatement ps = DBUtil.getConnection().prepareStatement(sql);
            ps.setInt(1, donorId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                donations.add(mapRow(rs));
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return donations;
    }

    /**
     * Returns all donations for a specific child.
     */
    public List<Donation> findByChildId(int childId) {
        List<Donation> donations = new ArrayList<>();
        String sql = "SELECT * FROM donations WHERE child_id = ? ORDER BY date DESC";
        try {
            PreparedStatement ps = DBUtil.getConnection().prepareStatement(sql);
            ps.setInt(1, childId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                donations.add(mapRow(rs));
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return donations;
    }

    /**
     * Returns the total donation amount by a specific donor.
     */
    public double getTotalByDonorId(int donorId) {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM donations WHERE donor_id = ?";
        try {
            PreparedStatement ps = DBUtil.getConnection().prepareStatement(sql);
            ps.setInt(1, donorId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                double total = rs.getDouble(1);
                rs.close();
                ps.close();
                return total;
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Returns the total donation amount for a specific child.
     */
    public double getTotalByChildId(int childId) {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM donations WHERE child_id = ?";
        try {
            PreparedStatement ps = DBUtil.getConnection().prepareStatement(sql);
            ps.setInt(1, childId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                double total = rs.getDouble(1);
                rs.close();
                ps.close();
                return total;
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Returns the count of distinct children sponsored by a donor.
     */
    public int countChildrenByDonorId(int donorId) {
        String sql = "SELECT COUNT(DISTINCT child_id) FROM donations WHERE donor_id = ?";
        try {
            PreparedStatement ps = DBUtil.getConnection().prepareStatement(sql);
            ps.setInt(1, donorId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                rs.close();
                ps.close();
                return count;
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Inserts a new donation.
     */
    public boolean save(Donation donation) {
        String sql = "INSERT INTO donations (donor_id, child_id, amount, purpose, date, status) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = DBUtil.getConnection().prepareStatement(sql);
            ps.setInt(1, donation.getDonorId());
            ps.setInt(2, donation.getChildId());
            ps.setDouble(3, donation.getAmount());
            ps.setString(4, donation.getPurpose());
            ps.setString(5, donation.getDate());
            ps.setString(6, donation.getStatus());
            ps.executeUpdate();
            ps.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Donation mapRow(ResultSet rs) throws SQLException {
        Donation d = new Donation();
        d.setId(rs.getInt("id"));
        d.setDonorId(rs.getInt("donor_id"));
        d.setChildId(rs.getInt("child_id"));
        d.setAmount(rs.getDouble("amount"));
        d.setPurpose(rs.getString("purpose"));
        d.setDate(rs.getString("date"));
        d.setStatus(rs.getString("status"));
        return d;
    }
}
