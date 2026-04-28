package com.inventory.dao;

import com.inventory.model.Supplier;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SupplierDAO {

    public List<Supplier> getAllSuppliers() {
        List<Supplier> suppliers = new ArrayList<>();
        String sql = "SELECT * FROM suppliers ORDER BY performance_score DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Supplier s = new Supplier();
                s.setId(rs.getInt("id"));
                s.setName(rs.getString("name"));
                s.setLeadTimeDays(rs.getInt("lead_time_days"));
                s.setDefectRate(rs.getDouble("defect_rate"));
                s.setReliabilityScore(rs.getDouble("reliability_score"));
                s.setCostPerUnit(rs.getDouble("cost_per_unit"));
                s.setLocation(rs.getString("location"));
                s.setPerformanceScore(rs.getDouble("performance_score"));
                suppliers.add(s);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching suppliers: " + e.getMessage());
        }
        return suppliers;
    }

    public boolean addSupplier(Supplier supplier) {
        String sql = "INSERT INTO suppliers (name, lead_time_days, defect_rate, " +
                     "reliability_score, cost_per_unit, location, performance_score) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, supplier.getName());
            stmt.setInt(2, supplier.getLeadTimeDays());
            stmt.setDouble(3, supplier.getDefectRate());
            stmt.setDouble(4, supplier.getReliabilityScore());
            stmt.setDouble(5, supplier.getCostPerUnit());
            stmt.setString(6, supplier.getLocation());
            stmt.setDouble(7, supplier.getPerformanceScore());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error adding supplier: " + e.getMessage());
        }
        return false;
    }

    public boolean updateSupplierScore(int supplierId, double score) {
        String sql = "UPDATE suppliers SET performance_score = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, score);
            stmt.setInt(2, supplierId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error updating supplier score: " + e.getMessage());
        }
        return false;
    }

    public boolean updateSupplier(Supplier supplier) {
        String sql = "UPDATE suppliers SET name=?, lead_time_days=?, defect_rate=?, " +
                     "reliability_score=?, cost_per_unit=?, location=?, performance_score=? " +
                     "WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, supplier.getName());
            stmt.setInt(2, supplier.getLeadTimeDays());
            stmt.setDouble(3, supplier.getDefectRate());
            stmt.setDouble(4, supplier.getReliabilityScore());
            stmt.setDouble(5, supplier.getCostPerUnit());
            stmt.setString(6, supplier.getLocation());
            stmt.setDouble(7, supplier.getPerformanceScore());
            stmt.setInt(8, supplier.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error updating supplier: " + e.getMessage());
        }
        return false;
    }

    public Supplier getSupplierById(int id) {
        String sql = "SELECT * FROM suppliers WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Supplier s = new Supplier();
                s.setId(rs.getInt("id"));
                s.setName(rs.getString("name"));
                s.setLeadTimeDays(rs.getInt("lead_time_days"));
                s.setDefectRate(rs.getDouble("defect_rate"));
                s.setReliabilityScore(rs.getDouble("reliability_score"));
                s.setCostPerUnit(rs.getDouble("cost_per_unit"));
                s.setLocation(rs.getString("location"));
                s.setPerformanceScore(rs.getDouble("performance_score"));
                return s;
            }
        } catch (SQLException e) {
            System.out.println("Error fetching supplier: " + e.getMessage());
        }
        return null;
    }

    public boolean deleteSupplier(int id) {
        String sql = "DELETE FROM suppliers WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error deleting supplier: " + e.getMessage());
        }
        return false;
    }
}