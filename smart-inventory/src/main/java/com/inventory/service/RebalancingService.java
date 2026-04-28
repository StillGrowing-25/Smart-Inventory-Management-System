package com.inventory.service;

import com.inventory.dao.DatabaseConnection;
import com.inventory.dao.WarehouseDAO;
import com.inventory.model.StockTransfer;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import java.io.*;
import java.sql.*;
import java.util.*;

public class RebalancingService {

    private static final String CSV_FILE = CsvPathResolver.resolveDataFilePath("retail_store_inventory.csv");
    private WarehouseDAO warehouseDAO = new WarehouseDAO();

    // Main method — finds all rebalancing opportunities
    public List<Map<String, Object>> findRebalancingOpportunities() {
        List<Map<String, Object>> opportunities = new ArrayList<>();

        // Get stock levels per product per warehouse from DB
        Map<String, Map<Integer, Integer>> productWarehouseStock =
                getProductStockAcrossWarehouses();

        for (Map.Entry<String, Map<Integer, Integer>> entry :
                productWarehouseStock.entrySet()) {
            String productName = entry.getKey();
            Map<Integer, Integer> warehouseStocks = entry.getValue();

            if (warehouseStocks.size() < 2) continue;

            // Find overstocked and understocked warehouses
            int totalStock = warehouseStocks.values().stream()
                    .mapToInt(Integer::intValue).sum();
            int avgStock = totalStock / warehouseStocks.size();

            int maxStock = Collections.max(warehouseStocks.values());
            int minStock = Collections.min(warehouseStocks.values());

            // Only suggest if difference is significant (>30% of average)
            if (maxStock - minStock < avgStock * 0.3) continue;
            if (avgStock == 0) continue;

            int fromWarehouseId = -1;
            int toWarehouseId   = -1;

            for (Map.Entry<Integer, Integer> ws : warehouseStocks.entrySet()) {
                if (ws.getValue() == maxStock) fromWarehouseId = ws.getKey();
                if (ws.getValue() == minStock) toWarehouseId   = ws.getKey();
            }

            if (fromWarehouseId == -1 || toWarehouseId == -1) continue;

            int transferQty = (maxStock - avgStock);
            if (transferQty <= 0) continue;

            String fromName = getWarehouseName(fromWarehouseId);
            String toName   = getWarehouseName(toWarehouseId);

            Map<String, Object> opportunity = new LinkedHashMap<>();
            opportunity.put("productName",      productName);
            opportunity.put("fromWarehouseId",  fromWarehouseId);
            opportunity.put("toWarehouseId",    toWarehouseId);
            opportunity.put("fromWarehouseName", fromName);
            opportunity.put("toWarehouseName",  toName);
            opportunity.put("fromStock",        maxStock);
            opportunity.put("toStock",          minStock);
            opportunity.put("suggestedQty",     transferQty);
            opportunity.put("avgStock",         avgStock);
            opportunity.put("balanceScore",     calculateBalanceScore(
                    maxStock, minStock, avgStock));

            opportunities.add(opportunity);
        }

        // Sort by balance score descending (most urgent first)
        opportunities.sort((a, b) ->
                Double.compare(
                        (double) b.get("balanceScore"),
                        (double) a.get("balanceScore")));

        return opportunities;
    }

    // Get stock levels from database
    private Map<String, Map<Integer, Integer>> getProductStockAcrossWarehouses() {
        Map<String, Map<Integer, Integer>> result = new LinkedHashMap<>();
        String sql = "SELECT p.name, ws.warehouse_id, ws.stock_qty " +
                     "FROM warehouse_stock ws " +
                     "JOIN products p ON ws.product_id = p.id " +
                     "ORDER BY p.name";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String product    = rs.getString("name");
                int warehouseId   = rs.getInt("warehouse_id");
                int stockQty      = rs.getInt("stock_qty");

                result.computeIfAbsent(product, k -> new HashMap<>())
                      .put(warehouseId, stockQty);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching warehouse stock: " + e.getMessage());
        }
        return result;
    }

    // Apply a rebalancing transfer
    public boolean applyRebalancing(Map<String, Object> opportunity, int userId) {
        String productName    = opportunity.get("productName").toString();
        int fromWarehouseId   = (int) opportunity.get("fromWarehouseId");
        int toWarehouseId     = (int) opportunity.get("toWarehouseId");
        int qty               = (int) opportunity.get("suggestedQty");

        int productId = getProductIdByName(productName);
        if (productId == -1) return false;

        StockTransfer transfer = new StockTransfer(
                fromWarehouseId, toWarehouseId,
                productId, qty, userId,
                "Auto-rebalancing by system");

        return warehouseDAO.transferStock(transfer);
    }

    // Calculate how urgently rebalancing is needed (0-100)
    private double calculateBalanceScore(int maxStock,
            int minStock, int avgStock) {
        if (avgStock == 0) return 0;
        double imbalance = (double)(maxStock - minStock) / avgStock;
        return Math.min(100, Math.round(imbalance * 50 * 100.0) / 100.0);
    }

    // Get warehouse name by ID
    private String getWarehouseName(int warehouseId) {
        String sql = "SELECT name FROM warehouses WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, warehouseId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("name");
        } catch (SQLException e) {
            System.out.println("Error getting warehouse name: " + e.getMessage());
        }
        return "Unknown";
    }

    // Get product ID by name
    private int getProductIdByName(String name) {
        String sql = "SELECT id FROM products WHERE name = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("id");
        } catch (SQLException e) {
            System.out.println("Error getting product id: " + e.getMessage());
        }
        return -1;
    }

    // Get rebalancing summary
    public Map<String, Object> getRebalancingSummary() {
        List<Map<String, Object>> opportunities = findRebalancingOpportunities();
        Map<String, Object> summary = new LinkedHashMap<>();

        int totalOpportunities = opportunities.size();
        int totalUnitsToMove   = opportunities.stream()
                .mapToInt(o -> (int) o.get("suggestedQty"))
                .sum();
        double avgBalanceScore = opportunities.stream()
                .mapToDouble(o -> (double) o.get("balanceScore"))
                .average().orElse(0.0);

        summary.put("totalOpportunities", totalOpportunities);
        summary.put("totalUnitsToMove",   totalUnitsToMove);
        summary.put("avgBalanceScore",
                Math.round(avgBalanceScore * 100.0) / 100.0);
        summary.put("topOpportunity",
                totalOpportunities > 0 ?
                opportunities.get(0).get("productName") : "None");

        return summary;
    }

    // Load CSV stock data for analysis
    public Map<String, Object> getCSVStockAnalysis() {
        Map<String, Integer> categoryStock = new LinkedHashMap<>();
        Map<String, Integer> storeStock    = new LinkedHashMap<>();
        int totalRows = 0;

        try (CSVReader reader = new CSVReader(new FileReader(CSV_FILE))) {
            reader.readNext();
            String[] line;
            while ((line = reader.readNext()) != null) {
                try {
                    String storeId   = line[1].trim();
                    String category  = line[3].trim();
                    int inventory    = Integer.parseInt(line[5].trim());

                    categoryStock.merge(category, inventory, Integer::sum);
                    storeStock.merge(storeId, inventory, Integer::sum);
                    totalRows++;
                } catch (Exception ignored) {}
            }
        } catch (IOException | CsvValidationException e) {
            System.out.println("Error reading CSV: " + e.getMessage());
        }

        Map<String, Object> analysis = new LinkedHashMap<>();
        analysis.put("categoryStock", categoryStock);
        analysis.put("storeStock",    storeStock);
        analysis.put("totalRows",     totalRows);
        return analysis;
    }
}
