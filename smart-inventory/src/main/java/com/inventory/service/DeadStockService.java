package com.inventory.service;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import com.inventory.dao.DatabaseConnection;
import java.io.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DeadStockService {

    private static final String CSV_FILE = CsvPathResolver.resolveDataFilePath("retail_store_inventory.csv");
    private static final int DEAD_STOCK_DAYS = 60;

    // ── Detect Dead Stock ─────────────────────────────────────
    public List<Map<String, Object>> detectDeadStock() {
        List<Map<String, Object>> deadStockList =
                new ArrayList<>();

        Map<String, LocalDate> lastSaleDates =
                getLastSaleDates();
        Map<String, Integer> currentStock =
                getCurrentStockLevels();
        Map<String, Double> avgSales =
                getAverageMonthlySales();

        LocalDate today = LocalDate.now();

        for (Map.Entry<String, LocalDate> entry :
                lastSaleDates.entrySet()) {
            String key       = entry.getKey();
            LocalDate lastSale = entry.getValue();

            long daysSinceLastSale =
                    java.time.temporal.ChronoUnit.DAYS
                    .between(lastSale, today);

            if (daysSinceLastSale < DEAD_STOCK_DAYS) continue;

            int stock = currentStock.getOrDefault(key, 0);
            if (stock <= 0) continue;

            double avgMonthly =
                    avgSales.getOrDefault(key, 0.0);
            double discount =
                    calculateSuggestedDiscount(
                            (int) daysSinceLastSale);

            String[] parts   = key.split("_");
            String productId = parts[0];
            String storeId   = parts.length > 1
                    ? parts[1] : "Unknown";

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("productId",         productId);
            item.put("storeId",           storeId);
            item.put("currentStock",      stock);
            item.put("daysSinceLastSale", (int) daysSinceLastSale);
            item.put("lastSaleDate",      lastSale.toString());
            item.put("avgMonthlySales",
                    Math.round(avgMonthly * 10.0) / 10.0);
            item.put("suggestedDiscount", discount);
            item.put("urgency",
                    getUrgencyLevel((int) daysSinceLastSale));
            item.put("estimatedLoss",
                    Math.round(stock * avgMonthly
                            * 0.1 * 100.0) / 100.0);

            deadStockList.add(item);
        }

        deadStockList.sort((a, b) ->
                Integer.compare(
                        (int) b.get("daysSinceLastSale"),
                        (int) a.get("daysSinceLastSale")));

        return deadStockList;
    }

    // ── Last Sale Dates ───────────────────────────────────────
    private Map<String, LocalDate> getLastSaleDates() {
        Map<String, LocalDate> lastSales = new HashMap<>();
        try (CSVReader reader =
                new CSVReader(new FileReader(CSV_FILE))) {
            reader.readNext();
            String[] line;
            while ((line = reader.readNext()) != null) {
                try {
                    String dateStr   = line[0].trim();
                    String storeId   = line[1].trim();
                    String productId = line[2].trim();
                    double unitsSold =
                            Double.parseDouble(line[6].trim());
                    if (unitsSold <= 0) continue;
                    LocalDate date = parseDate(dateStr);
                    if (date == null) continue;
                    String key = productId + "_" + storeId;
                    lastSales.merge(key, date,
                            (existing, newDate) ->
                            newDate.isAfter(existing)
                                    ? newDate : existing);
                } catch (Exception ignored) {}
            }
        } catch (IOException | CsvValidationException e) {
            System.out.println("Error reading CSV: "
                    + e.getMessage());
        }
        return lastSales;
    }

    // ── Current Stock Levels ──────────────────────────────────
    private Map<String, Integer> getCurrentStockLevels() {
        Map<String, Integer> stockLevels = new HashMap<>();
        Map<String, LocalDate> latestDates = new HashMap<>();
        try (CSVReader reader =
                new CSVReader(new FileReader(CSV_FILE))) {
            reader.readNext();
            String[] line;
            while ((line = reader.readNext()) != null) {
                try {
                    String dateStr   = line[0].trim();
                    String storeId   = line[1].trim();
                    String productId = line[2].trim();
                    int inventory =
                            Integer.parseInt(line[5].trim());
                    LocalDate date = parseDate(dateStr);
                    if (date == null) continue;
                    String key = productId + "_" + storeId;
                    LocalDate existing = latestDates.get(key);
                    if (existing == null
                            || date.isAfter(existing)) {
                        latestDates.put(key, date);
                        stockLevels.put(key, inventory);
                    }
                } catch (Exception ignored) {}
            }
        } catch (IOException | CsvValidationException e) {
            System.out.println("Error reading stock: "
                    + e.getMessage());
        }
        return stockLevels;
    }

    // ── Average Monthly Sales ─────────────────────────────────
    private Map<String, Double> getAverageMonthlySales() {
        Map<String, List<Double>> salesData = new HashMap<>();
        try (CSVReader reader =
                new CSVReader(new FileReader(CSV_FILE))) {
            reader.readNext();
            String[] line;
            while ((line = reader.readNext()) != null) {
                try {
                    String storeId   = line[1].trim();
                    String productId = line[2].trim();
                    double unitsSold =
                            Double.parseDouble(line[6].trim());
                    String key = productId + "_" + storeId;
                    salesData.computeIfAbsent(
                            key, k -> new ArrayList<>())
                            .add(unitsSold);
                } catch (Exception ignored) {}
            }
        } catch (IOException | CsvValidationException e) {
            System.out.println("Error reading avg sales: "
                    + e.getMessage());
        }
        Map<String, Double> averages = new HashMap<>();
        for (Map.Entry<String, List<Double>> entry :
                salesData.entrySet()) {
            double avg = entry.getValue().stream()
                    .mapToDouble(Double::doubleValue)
                    .average().orElse(0.0);
            averages.put(entry.getKey(), avg);
        }
        return averages;
    }

    // ── Discount Calculation ──────────────────────────────────
    public double calculateSuggestedDiscount(int daysIdle) {
        if (daysIdle >= 180) return 50.0;
        if (daysIdle >= 120) return 40.0;
        if (daysIdle >= 90)  return 30.0;
        if (daysIdle >= 60)  return 20.0;
        if (daysIdle >= 30)  return 10.0;
        return 5.0;
    }

    // ── Urgency Level ─────────────────────────────────────────
    public String getUrgencyLevel(int daysIdle) {
        if (daysIdle >= 180) return "CRITICAL";
        if (daysIdle >= 90)  return "HIGH";
        if (daysIdle >= 60)  return "MEDIUM";
        return "LOW";
    }

    // ── Save to Database ──────────────────────────────────────
    public void saveDeadStockToDatabase(
            List<Map<String, Object>> deadStockList) {

        String deleteSql = "DELETE FROM dead_stock";
        String insertSql =
                "INSERT INTO dead_stock " +
                "(product_id, warehouse_id, " +
                "days_no_movement, suggested_discount) " +
                "VALUES (?,?,?,?)";

        // Single connection for entire operation
        try (Connection conn =
                DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            // Step 1: Delete old records
            try (Statement delStmt =
                    conn.createStatement()) {
                delStmt.executeUpdate(deleteSql);
            }

            // Step 2: Insert new records
            try (PreparedStatement insertStmt =
                    conn.prepareStatement(insertSql)) {

                int count = 0;
                for (Map<String, Object> item :
                        deadStockList) {
                    String productName =
                            item.get("productId").toString();
                    String storeId =
                            item.get("storeId").toString();

                    int productDbId =
                            getProductDbId(conn, productName);
                    int warehouseId =
                            getWarehouseIdByStore(storeId);

                    if (productDbId == -1 ||
                            warehouseId == -1) {
                        System.out.println(
                                "Skipping: " + productName
                                + " / " + storeId);
                        continue;
                    }

                    insertStmt.setInt(1, productDbId);
                    insertStmt.setInt(2, warehouseId);
                    insertStmt.setInt(3,
                            (int) item.get(
                                    "daysSinceLastSale"));
                    insertStmt.setDouble(4,
                            (double) item.get(
                                    "suggestedDiscount"));
                    insertStmt.addBatch();
                    count++;
                }

                insertStmt.executeBatch();
                conn.commit();
                System.out.println(
                        "Dead stock saved: "
                        + count + " records.");
            }

        } catch (SQLException e) {
            System.out.println(
                    "Error saving dead stock: "
                    + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── Summary ───────────────────────────────────────────────
    public Map<String, Object> getDeadStockSummary() {
        List<Map<String, Object>> deadStock =
                detectDeadStock();
        Map<String, Object> summary = new LinkedHashMap<>();

        int totalItems = deadStock.size();
        int criticalItems = (int) deadStock.stream()
                .filter(d -> "CRITICAL"
                        .equals(d.get("urgency")))
                .count();
        int highItems = (int) deadStock.stream()
                .filter(d -> "HIGH"
                        .equals(d.get("urgency")))
                .count();
        double totalLoss = deadStock.stream()
                .mapToDouble(d ->
                        (double) d.get("estimatedLoss"))
                .sum();

        summary.put("totalDeadStockItems", totalItems);
        summary.put("criticalItems",       criticalItems);
        summary.put("highUrgencyItems",    highItems);
        summary.put("estimatedTotalLoss",
                Math.round(totalLoss * 100.0) / 100.0);

        return summary;
    }

    // ── Helpers ───────────────────────────────────────────────

    // Uses existing connection to avoid "statement closed" error
    private int getProductDbId(Connection conn,
            String productName) {
        String sql = "SELECT id FROM products WHERE name = ?";
        try (PreparedStatement stmt =
                conn.prepareStatement(sql)) {
            stmt.setString(1, productName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("id");
        } catch (SQLException e) {
            System.out.println("Error getting product id: "
                    + e.getMessage());
        }
        return -1;
    }

    // Standalone version for other callers
    private int getProductDbId(String productName) {
        String sql = "SELECT id FROM products WHERE name = ?";
        try (Connection conn =
                DatabaseConnection.getConnection();
             PreparedStatement stmt =
                     conn.prepareStatement(sql)) {
            stmt.setString(1, productName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("id");
        } catch (SQLException e) {
            System.out.println("Error getting product id: "
                    + e.getMessage());
        }
        return -1;
    }

    private int getWarehouseIdByStore(String storeId) {
        Map<String, Integer> storeMap = new HashMap<>();
        storeMap.put("S001", 1);
        storeMap.put("S002", 2);
        storeMap.put("S003", 3);
        storeMap.put("S004", 4);
        storeMap.put("S005", 5);
        return storeMap.getOrDefault(storeId, -1);
    }

    private LocalDate parseDate(String dateStr) {
        String[] formats = {
            "yyyy-MM-dd", "MM/dd/yyyy",
            "dd-MM-yyyy", "dd/MM/yyyy",
            "M/d/yyyy",   "d/M/yyyy"
        };
        for (String fmt : formats) {
            try {
                return LocalDate.parse(dateStr,
                        DateTimeFormatter.ofPattern(fmt));
            } catch (Exception ignored) {}
        }
        return null;
    }
}
