package com.inventory.service;

import com.inventory.dao.DatabaseConnection;
import com.opencsv.CSVReader;

import java.io.*;
import java.sql.*;
import java.util.*;

public class CSVDataLoader {

    // ✅ Proper way to load files from resources
    private InputStream getFileFromResources(String fileName) {
        InputStream inputStream = getClass()
                .getClassLoader()
                .getResourceAsStream("data/" + fileName);

        if (inputStream == null) {
            throw new RuntimeException("File not found in resources: " + fileName);
        }
        return inputStream;
    }

    public void loadAllData() {
        System.out.println("Loading CSV data...");
        clearAndReload();
        loadSupplierData();
        System.out.println("All data loaded!");
    }

    // ── Clear + Reload Inventory ──────────────────────────────
    private void clearAndReload() {

        if (productsAlreadyLoaded()) {
            System.out.println("Products already in DB — skipping reload.");
            return;
        }

        // Step 1: Clear old data
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
            stmt.execute("TRUNCATE TABLE demand_forecast");
            stmt.execute("TRUNCATE TABLE dead_stock");
            stmt.execute("TRUNCATE TABLE inventory_log");
            stmt.execute("TRUNCATE TABLE warehouse_stock");
            stmt.execute("TRUNCATE TABLE products");
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");

            System.out.println("Old data cleared.");

        } catch (SQLException e) {
            System.out.println("Clear error: " + e.getMessage());
        }

        Map<String, Integer> storeMap = new HashMap<>();
        storeMap.put("S001", 1);
        storeMap.put("S002", 2);
        storeMap.put("S003", 3);
        storeMap.put("S004", 4);
        storeMap.put("S005", 5);

        Map<String, String[]> products = new LinkedHashMap<>();
        Map<String, Integer> latestStock = new LinkedHashMap<>();
        Map<String, String> latestDate = new LinkedHashMap<>();

        // ✅ FIXED CSV LOAD
        try (CSVReader reader = new CSVReader(
                new InputStreamReader(
                        getFileFromResources("retail_store_inventory.csv")))) {

            reader.readNext();
            String[] line;

            while ((line = reader.readNext()) != null) {
                try {
                    String date = line[0].trim();
                    String storeId = line[1].trim();
                    String productId = line[2].trim();
                    String category = line[3].trim();
                    int inventory = Integer.parseInt(line[5].trim());
                    double price = Double.parseDouble(line[9].trim());

                    products.putIfAbsent(productId,
                            new String[]{category, String.valueOf(price)});

                    Integer wId = storeMap.get(storeId);
                    if (wId == null) continue;

                    String key = productId + "_" + wId;
                    String existing = latestDate.get(key);

                    if (existing == null || date.compareTo(existing) >= 0) {
                        latestDate.put(key, date);
                        latestStock.put(key, inventory);
                    }

                } catch (Exception ignored) {}
            }

        } catch (Exception e) {
            System.out.println("CSV error: " + e.getMessage());
            return;
        }

        // Step 2: Insert products
        String pSql = "INSERT INTO products (name, category, unit_price, reorder_level) VALUES (?,?,?,?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(pSql)) {

            for (Map.Entry<String, String[]> e : products.entrySet()) {
                stmt.setString(1, e.getKey());
                stmt.setString(2, e.getValue()[0]);
                stmt.setDouble(3, Double.parseDouble(e.getValue()[1]));
                stmt.setInt(4, 20);
                stmt.addBatch();
            }

            stmt.executeBatch();
            System.out.println("Products inserted: " + products.size());

        } catch (SQLException e) {
            System.out.println("Product error: " + e.getMessage());
        }

        // Step 3: Build product ID map
        Map<String, Integer> productIdMap = new HashMap<>();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, name FROM products")) {

            while (rs.next()) {
                productIdMap.put(rs.getString("name"), rs.getInt("id"));
            }

        } catch (SQLException e) {
            System.out.println("Product map error: " + e.getMessage());
        }

        // Step 4: Insert warehouse stock
        String wSql = "INSERT INTO warehouse_stock (product_id, warehouse_id, stock_qty) VALUES (?,?,?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(wSql)) {

            for (Map.Entry<String, Integer> e : latestStock.entrySet()) {

                String[] parts = e.getKey().split("_");
                String productName = parts[0];
                int warehouseId = Integer.parseInt(parts[1]);

                Integer productId = productIdMap.get(productName);
                if (productId == null) continue;

                stmt.setInt(1, productId);
                stmt.setInt(2, warehouseId);
                stmt.setInt(3, e.getValue());
                stmt.addBatch();
            }

            stmt.executeBatch();
            System.out.println("Warehouse stock inserted.");

        } catch (SQLException e) {
            System.out.println("Stock error: " + e.getMessage());
        }
    }

    // ── Load Suppliers ────────────────────────────────────────
    public void loadSupplierData() {

        if (suppliersAlreadyLoaded()) {
            System.out.println("Suppliers already in DB — skipping reload.");
            return;
        }

        Map<String, Integer> totalOrders = new LinkedHashMap<>();
        Map<String, Integer> deliveredOrders = new LinkedHashMap<>();
        Map<String, Double> totalDefects = new LinkedHashMap<>();
        Map<String, List<Long>> leadTimes = new LinkedHashMap<>();
        Map<String, Double> unitPrices = new LinkedHashMap<>();

        // ✅ FIXED CSV LOAD
        try (CSVReader reader = new CSVReader(
                new InputStreamReader(
                        getFileFromResources("Procurement KPI Analysis Dataset.csv")))) {

            reader.readNext();
            String[] line;

            while ((line = reader.readNext()) != null) {
                try {
                    String supplier = line[1].trim();
                    String orderDate = line[2].trim();
                    String delivDate = line[3].trim();
                    String status = line[5].trim();
                    double price = Double.parseDouble(line[7].trim());
                    String defectStr = line[9].trim();

                    totalOrders.merge(supplier, 1, Integer::sum);
                    unitPrices.put(supplier, price);

                    if (status.equalsIgnoreCase("Delivered")) {
                        deliveredOrders.merge(supplier, 1, Integer::sum);
                    }

                    if (!defectStr.isEmpty()) {
                        totalDefects.merge(supplier,
                                Double.parseDouble(defectStr),
                                Double::sum);
                    }

                    if (!orderDate.isEmpty() && !delivDate.isEmpty()) {
                        java.time.LocalDate od = parseDate(orderDate);
                        java.time.LocalDate dd = parseDate(delivDate);

                        if (od != null && dd != null && !dd.isBefore(od)) {
                            long days = java.time.temporal.ChronoUnit.DAYS.between(od, dd);
                            leadTimes.computeIfAbsent(supplier, k -> new ArrayList<>()).add(days);
                        }
                    }

                } catch (Exception ignored) {}
            }

        } catch (Exception e) {
            System.out.println("Supplier CSV error: " + e.getMessage());
            return;
        }

        String sql = "INSERT INTO suppliers (name, lead_time_days, defect_rate, reliability_score, cost_per_unit, location, performance_score) VALUES (?,?,?,?,?,?,0)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (String name : totalOrders.keySet()) {

                int total = totalOrders.get(name);
                int delivered = deliveredOrders.getOrDefault(name, 0);
                double defects = totalDefects.getOrDefault(name, 0.0);
                List<Long> lt = leadTimes.getOrDefault(name, new ArrayList<>());

                double reliability = total > 0 ? (delivered * 100.0 / total) : 0;
                double defectRate = total > 0 ? (defects / total) : 0;
                double avgLead = lt.isEmpty() ? 7 :
                        lt.stream().mapToLong(Long::longValue).average().orElse(7);

                stmt.setString(1, name);
                stmt.setInt(2, (int) avgLead);
                stmt.setDouble(3, defectRate);
                stmt.setDouble(4, reliability);
                stmt.setDouble(5, unitPrices.getOrDefault(name, 0.0));
                stmt.setString(6, "Unknown");

                stmt.addBatch();
            }

            stmt.executeBatch();
            System.out.println("Suppliers loaded.");

        } catch (SQLException e) {
            System.out.println("Supplier insert error: " + e.getMessage());
        }
    }

    // ── Checks ────────────────────────────────────────────────
    private boolean productsAlreadyLoaded() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM products")) {

            return rs.next() && rs.getInt(1) > 0;

        } catch (SQLException e) {
            return false;
        }
    }

    private boolean suppliersAlreadyLoaded() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM suppliers")) {

            return rs.next() && rs.getInt(1) > 0;

        } catch (SQLException e) {
            return false;
        }
    }

    // ── Date Parser ───────────────────────────────────────────
    private java.time.LocalDate parseDate(String s) {
        String[] fmts = {"yyyy-MM-dd", "MM/dd/yyyy", "dd-MM-yyyy", "dd/MM/yyyy"};

        for (String fmt : fmts) {
            try {
                return java.time.LocalDate.parse(s,
                        java.time.format.DateTimeFormatter.ofPattern(fmt));
            } catch (Exception ignored) {}
        }
        return null;
    }
}