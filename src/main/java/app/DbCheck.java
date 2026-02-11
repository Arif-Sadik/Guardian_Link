package app;

import util.DBUtil;
import java.sql.*;

public class DbCheck {
    public static void main(String[] args) {
        try {
            DBUtil.initialize();
            Connection conn = DBUtil.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id, username, password, role, approved FROM users");
            System.out.println("--- USERS IN DATABASE ---");
            while (rs.next()) {
                System.out.printf("ID: %d, User: %s, Pass: %s, Role: %s, Approved: %d%n",
                    rs.getInt("id"), rs.getString("username"), rs.getString("password"), rs.getString("role"), rs.getInt("approved"));
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
