package com.inventory.dao;

import com.inventory.model.Warehouse;
import com.inventory.model.StockTransfer;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WarehouseDAO {

    public List<Warehouse> getAllWarehouses() {
        List<Warehouse> warehouses = new ArrayList<>();
        String sql = "SELECT * FROM warehouses ORDER BY name";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Warehouse w = new Warehouse();
                w.setId(rs.getInt("id"));
                w.setName(rs.getString("name"));
                w.setLocation(rs.getString("location"));
                w.setManagerName(rs.getString("manager_name"));
                warehouses.add(w);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching warehouses: " + e.getMessage());
        }
        return warehouses;
    }

    public Warehouse getWarehouseById(int id) {
        String sql = "SELECT * FROM warehouses WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Warehouse w = new Warehouse();
                w.setId(rs.getInt("id"));
                w.setName(rs.getString("name"));
                w.setLocation(rs.getString("location"));
                w.setManagerName(rs.getString("manager_name"));
                return w;
            }
        } catch (SQLException e) {
            System.out.println("Error fetching warehouse: " + e.getMessage());
        }
        return null;
    }

    public int getStockForProduct(int warehouseId, int productId) {
        String sql = "SELECT stock_qty FROM warehouse_stock WHERE warehouse_id=? AND product_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, warehouseId);
            stmt.setInt(2, productId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("stock_qty");
            }
        } catch (SQLException e) {
            System.out.println("Error fetching stock: " + e.getMessage());
        }
        return 0;
    }

    public boolean updateStock(int warehouseId, int productId, int newQty) {
        String checkSql = "SELECT id FROM warehouse_stock WHERE warehouse_id=? AND product_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setInt(1, warehouseId);
            checkStmt.setInt(2, productId);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                String updateSql = "UPDATE warehouse_stock SET stock_qty=?, last_updated=NOW() WHERE warehouse_id=? AND product_id=?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setInt(1, newQty);
                    updateStmt.setInt(2, warehouseId);
                    updateStmt.setInt(3, productId);
                    return updateStmt.executeUpdate() > 0;
                }
            } else {
                String insertSql = "INSERT INTO warehouse_stock (warehouse_id, product_id, stock_qty) VALUES (?,?,?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setInt(1, warehouseId);
                    insertStmt.setInt(2, productId);
                    insertStmt.setInt(3, newQty);
                    return insertStmt.executeUpdate() > 0;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error updating stock: " + e.getMessage());
        }
        return false;
    }

    public boolean transferStock(StockTransfer transfer) {
    // Step 1: Check source has enough stock
    int fromStock = getStockForProduct(
            transfer.getFromWarehouseId(),
            transfer.getProductId());

    if (fromStock < transfer.getQuantity()) {
        System.out.println("Insufficient stock: "
                + "has " + fromStock
                + " needs " + transfer.getQuantity());
        return false;
    }

    String deductSql =
            "UPDATE warehouse_stock "
            + "SET stock_qty = stock_qty - ? "
            + "WHERE warehouse_id = ? "
            + "AND product_id = ?";

    String addSql =
            "INSERT INTO warehouse_stock "
            + "(warehouse_id, product_id, stock_qty) "
            + "VALUES (?, ?, ?) "
            + "ON DUPLICATE KEY UPDATE "
            + "stock_qty = stock_qty + ?";

    String logSql =
            "INSERT INTO stock_transfers "
            + "(from_warehouse_id, to_warehouse_id, "
            + "product_id, quantity, "
            + "transferred_by, reason) "
            + "VALUES (?,?,?,?,?,?)";

    try (Connection conn =
            DatabaseConnection.getConnection()) {
        conn.setAutoCommit(false);

        // Deduct from source
        try (PreparedStatement stmt =
                conn.prepareStatement(deductSql)) {
            stmt.setInt(1, transfer.getQuantity());
            stmt.setInt(2,
                    transfer.getFromWarehouseId());
            stmt.setInt(3, transfer.getProductId());
            stmt.executeUpdate();
        }

        // Add to destination
        try (PreparedStatement stmt =
                conn.prepareStatement(addSql)) {
            stmt.setInt(1,
                    transfer.getToWarehouseId());
            stmt.setInt(2, transfer.getProductId());
            stmt.setInt(3, transfer.getQuantity());
            stmt.setInt(4, transfer.getQuantity());
            stmt.executeUpdate();
        }

        // Log the transfer
        try (PreparedStatement stmt =
                conn.prepareStatement(logSql)) {
            stmt.setInt(1,
                    transfer.getFromWarehouseId());
            stmt.setInt(2,
                    transfer.getToWarehouseId());
            stmt.setInt(3, transfer.getProductId());
            stmt.setInt(4, transfer.getQuantity());
            stmt.setInt(5,
                    transfer.getTransferredBy());
            stmt.setString(6, transfer.getReason());
            stmt.executeUpdate();
        }

        conn.commit();
        System.out.println("Transfer successful: "
                + transfer.getQuantity()
                + " units moved.");
        return true;

    } catch (SQLException e) {
        System.out.println("Transfer error: "
                + e.getMessage());
        return false;
    }
}

    public List<StockTransfer> getTransferHistory(
        int warehouseId) {
    List<StockTransfer> transfers =
            new ArrayList<>();

    // If warehouseId = 0, get ALL transfers
    String sql = warehouseId <= 0
            ? "SELECT st.*, "
              + "p.name as product_name, "
              + "fw.name as from_name, "
              + "tw.name as to_name, "
              + "u.full_name as by_name "
              + "FROM stock_transfers st "
              + "JOIN products p "
              + "ON st.product_id = p.id "
              + "JOIN warehouses fw "
              + "ON st.from_warehouse_id = fw.id "
              + "JOIN warehouses tw "
              + "ON st.to_warehouse_id = tw.id "
              + "JOIN users u "
              + "ON st.transferred_by = u.id "
              + "ORDER BY st.transferred_at DESC "
              + "LIMIT 100"
            : "SELECT st.*, "
              + "p.name as product_name, "
              + "fw.name as from_name, "
              + "tw.name as to_name, "
              + "u.full_name as by_name "
              + "FROM stock_transfers st "
              + "JOIN products p "
              + "ON st.product_id = p.id "
              + "JOIN warehouses fw "
              + "ON st.from_warehouse_id = fw.id "
              + "JOIN warehouses tw "
              + "ON st.to_warehouse_id = tw.id "
              + "JOIN users u "
              + "ON st.transferred_by = u.id "
              + "WHERE st.from_warehouse_id = ? "
              + "OR st.to_warehouse_id = ? "
              + "ORDER BY st.transferred_at DESC "
              + "LIMIT 100";

    try (Connection conn =
            DatabaseConnection.getConnection();
         PreparedStatement stmt =
                 conn.prepareStatement(sql)) {

        if (warehouseId > 0) {
            stmt.setInt(1, warehouseId);
            stmt.setInt(2, warehouseId);
        }

        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            StockTransfer t = new StockTransfer();
            t.setId(rs.getInt("id"));
            t.setProductId(rs.getInt("product_id"));
            t.setFromWarehouseId(
                    rs.getInt("from_warehouse_id"));
            t.setToWarehouseId(
                    rs.getInt("to_warehouse_id"));
            t.setQuantity(rs.getInt("quantity"));
            t.setTransferredBy(
                    rs.getInt("transferred_by"));
            t.setReason(rs.getString("reason"));
            t.setProductName(
                    rs.getString("product_name"));
            t.setFromWarehouseName(
                    rs.getString("from_name"));
            t.setToWarehouseName(
                    rs.getString("to_name"));
            t.setTransferredByName(
                    rs.getString("by_name"));
            t.setTransferredAt(
                    rs.getTimestamp("transferred_at")
                    != null
                    ? rs.getTimestamp(
                            "transferred_at")
                    .toLocalDateTime()
                    : null);
            transfers.add(t);
        }
    } catch (SQLException e) {
        System.out.println(
                "Error getting transfer history: "
                + e.getMessage());
    }
    return transfers;
}

    public List<Object[]> getAllWarehouseStock() {
        List<Object[]> stockList = new ArrayList<>();
        String sql = "SELECT w.name as warehouse, p.name as product, " +
                "p.category, ws.stock_qty, p.reorder_level " +
                "FROM warehouse_stock ws " +
                "JOIN warehouses w ON ws.warehouse_id = w.id " +
                "JOIN products p ON ws.product_id = p.id " +
                "ORDER BY w.name, p.name";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Object[] row = {
                    rs.getString("warehouse"),
                    rs.getString("product"),
                    rs.getString("category"),
                    rs.getInt("stock_qty"),
                    rs.getInt("reorder_level")
                };
                stockList.add(row);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching warehouse stock: " + e.getMessage());
        }
        return stockList;
    }
}