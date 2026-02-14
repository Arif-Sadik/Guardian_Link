package repository;

import model.entity.Expense;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data-access layer for the expenses table.
 */
public class ExpenseRepository {

    /**
     * Returns all expenses for a specific child.
     */
    public List<Expense> findByChildId(int childId) {
        List<Expense> expenses = new ArrayList<>();
        String sql = "SELECT * FROM expenses WHERE child_id = ? ORDER BY date DESC";
        try {
            PreparedStatement ps = DBUtil.getConnection().prepareStatement(sql);
            ps.setInt(1, childId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                expenses.add(mapRow(rs));
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return expenses;
    }

    /**
     * Returns total expenses for a specific child.
     */
    public double getTotalByChildId(int childId) {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE child_id = ?";
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
     * Inserts a new expense.
     */
    public boolean save(Expense expense) {
        String sql = "INSERT INTO expenses (child_id, category, amount, description, date) VALUES (?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = DBUtil.getConnection().prepareStatement(sql);
            ps.setInt(1, expense.getChildId());
            ps.setString(2, expense.getCategory());
            ps.setDouble(3, expense.getAmount());
            ps.setString(4, expense.getDescription());
            ps.setString(5, expense.getDate());
            ps.executeUpdate();
            ps.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Expense mapRow(ResultSet rs) throws SQLException {
        Expense e = new Expense();
        e.setId(rs.getInt("id"));
        e.setChildId(rs.getInt("child_id"));
        e.setCategory(rs.getString("category"));
        e.setAmount(rs.getDouble("amount"));
        e.setDescription(rs.getString("description"));
        e.setDate(rs.getString("date"));
        return e;
    }
}
