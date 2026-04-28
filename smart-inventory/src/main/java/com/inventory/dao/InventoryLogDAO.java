package com.inventory.dao;

import com.inventory.model.InventoryLog;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InventoryLogDAO {

    public boolean addLog(InventoryLog log) {
        String sql = "INSERT INTO inventory_log (product_id, warehouse_id, change_qty, reason, performed_by) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, log.getProductId());
            stmt.setInt(2, log.getWarehouseId());
            stmt.setInt(3, log.getChangeQty());
            stmt.setString(4, log.getReason());
            stmt.setInt(5, log.getPerformedBy());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error adding log: " + e.getMessage());
        }
        return false;
    }

    public List<InventoryLog> getRecentLogs(int limit) {
        List<InventoryLog> logs = new ArrayList<>();
        String sql = "SELECT il.*, p.name as product_name, " +
                     "w.name as warehouse_name, u.full_name " +
                     "FROM inventory_log il " +
                     "JOIN products p ON il.product_id = p.id " +
                     "JOIN warehouses w ON il.warehouse_id = w.id " +
                     "JOIN users u ON il.performed_by = u.id " +
                     "ORDER BY il.logged_at DESC LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                InventoryLog log = new InventoryLog();
                log.setId(rs.getInt("id"));
                log.setProductId(rs.getInt("product_id"));
                log.setWarehouseId(rs.getInt("warehouse_id"));
                log.setChangeQty(rs.getInt("change_qty"));
                log.setReason(rs.getString("reason"));
                log.setProductName(rs.getString("product_name"));
                log.setWarehouseName(rs.getString("warehouse_name"));
                log.setPerformedByName(rs.getString("full_name"));
                log.setLoggedAt(rs.getTimestamp("logged_at").toLocalDateTime());
                logs.add(log);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching logs: " + e.getMessage());
        }
        return logs;
    }

    public List<InventoryLog> getLogsByWarehouse(int warehouseId, int limit) {
        List<InventoryLog> logs = new ArrayList<>();
        String sql = "SELECT il.*, p.name as product_name, " +
                     "w.name as warehouse_name, u.full_name " +
                     "FROM inventory_log il " +
                     "JOIN products p ON il.product_id = p.id " +
                     "JOIN warehouses w ON il.warehouse_id = w.id " +
                     "JOIN users u ON il.performed_by = u.id " +
                     "WHERE il.warehouse_id = ? " +
                     "ORDER BY il.logged_at DESC LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, warehouseId);
            stmt.setInt(2, limit);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                InventoryLog log = new InventoryLog();
                log.setId(rs.getInt("id"));
                log.setProductId(rs.getInt("product_id"));
                log.setWarehouseId(rs.getInt("warehouse_id"));
                log.setChangeQty(rs.getInt("change_qty"));
                log.setReason(rs.getString("reason"));
                log.setProductName(rs.getString("product_name"));
                log.setWarehouseName(rs.getString("warehouse_name"));
                log.setPerformedByName(rs.getString("full_name"));
                log.setLoggedAt(rs.getTimestamp("logged_at").toLocalDateTime());
                logs.add(log);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching logs by warehouse: " + e.getMessage());
        }
        return logs;
    }

    public List<InventoryLog> getLogsByProduct(int productId) {
        List<InventoryLog> logs = new ArrayList<>();
        String sql = "SELECT il.*, p.name as product_name, " +
                     "w.name as warehouse_name, u.full_name " +
                     "FROM inventory_log il " +
                     "JOIN products p ON il.product_id = p.id " +
                     "JOIN warehouses w ON il.warehouse_id = w.id " +
                     "JOIN users u ON il.performed_by = u.id " +
                     "WHERE il.product_id = ? " +
                     "ORDER BY il.logged_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                InventoryLog log = new InventoryLog();
                log.setId(rs.getInt("id"));
                log.setProductId(rs.getInt("product_id"));
                log.setWarehouseId(rs.getInt("warehouse_id"));
                log.setChangeQty(rs.getInt("change_qty"));
                log.setReason(rs.getString("reason"));
                log.setProductName(rs.getString("product_name"));
                log.setWarehouseName(rs.getString("warehouse_name"));
                log.setPerformedByName(rs.getString("full_name"));
                log.setLoggedAt(rs.getTimestamp("logged_at").toLocalDateTime());
                logs.add(log);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching logs by product: " + e.getMessage());
        }
        return logs;
    }

    public int getCurrentStock(int productId, int warehouseId) {
        String sql = "SELECT COALESCE(SUM(change_qty), 0) as total " +
                     "FROM inventory_log " +
                     "WHERE product_id = ? AND warehouse_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            stmt.setInt(2, warehouseId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            System.out.println("Error calculating stock: " + e.getMessage());
        }
        return 0;
    }
}