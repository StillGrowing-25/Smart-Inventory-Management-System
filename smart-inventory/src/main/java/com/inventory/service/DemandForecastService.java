package com.inventory.service;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DemandForecastService {

    private static final String CSV_FILE = CsvPathResolver.resolveDataFilePath("retail_store_inventory.csv");

    // Returns monthly sales data for a specific product
    public Map<String, Double> getMonthlySales(String productId) {
        Map<String, Double> monthlySales = new TreeMap<>();

        try (CSVReader reader = new CSVReader(new FileReader(CSV_FILE))) {
            reader.readNext(); // skip header
            String[] line;
            while ((line = reader.readNext()) != null) {
                try {
                    String pid      = line[2].trim(); // Product ID
                    String dateStr  = line[0].trim(); // Date
                    double unitsSold = Double.parseDouble(line[6].trim()); // Units Sold

                    if (!pid.equals(productId)) continue;

                    LocalDate date = parseDate(dateStr);
                    if (date == null) continue;

                    String monthKey = date.getYear() + "-" +
                            String.format("%02d", date.getMonthValue());

                    monthlySales.merge(monthKey, unitsSold, Double::sum);

                } catch (Exception e) {
                    // skip bad rows
                }
            }
        } catch (IOException | CsvValidationException e) {
            System.out.println("Error reading CSV: " + e.getMessage());
        }
        return monthlySales;
    }

    // Moving Average Forecast — predicts next N months
    public List<Double> forecastNextMonths(String productId, int months) {
        if (months <= 0) {
            return new ArrayList<>();
        }

        Map<String, Double> monthlySales = getMonthlySales(productId);
        List<Double> salesValues = new ArrayList<>(monthlySales.values());

        if (salesValues.size() < 3) {
            List<Double> fallback = new ArrayList<>();
            for (int i = 0; i < months; i++) fallback.add(0.0);
            return fallback;
        }

        List<Double> forecasts = new ArrayList<>();
        int window = 3; // 3-month moving average

        for (int i = 0; i < months; i++) {
            int size = salesValues.size();
            double sum = 0;
            for (int j = size - window; j < size; j++) {
                sum += salesValues.get(j);
            }
            double forecast = sum / window;

            // Apply seasonal adjustment
            forecast = applySeasonalAdjustment(forecast, productId,
                    LocalDate.now().plusMonths(i + 1).getMonthValue());

            forecasts.add(Math.round(forecast * 100.0) / 100.0);
            salesValues.add(forecast); // use forecast for next prediction
        }
        return forecasts;
    }

    // Seasonal adjustment based on CSV seasonality column
    private double applySeasonalAdjustment(double baseForecast,
            String productId, int month) {
        Map<Integer, Double> seasonalFactors = getSeasonalFactors(productId);
        double factor = seasonalFactors.getOrDefault(month, 1.0);
        return baseForecast * factor;
    }

    // Calculate seasonal factors from historical data
    public Map<Integer, Double> getSeasonalFactors(String productId) {
        Map<Integer, List<Double>> monthlyData = new HashMap<>();

        try (CSVReader reader = new CSVReader(new FileReader(CSV_FILE))) {
            reader.readNext();
            String[] line;
            while ((line = reader.readNext()) != null) {
                try {
                    String pid       = line[2].trim();
                    String dateStr   = line[0].trim();
                    double unitsSold = Double.parseDouble(line[6].trim());

                    if (!pid.equals(productId)) continue;

                    LocalDate date = parseDate(dateStr);
                    if (date == null) continue;

                    monthlyData
                        .computeIfAbsent(date.getMonthValue(), k -> new ArrayList<>())
                        .add(unitsSold);

                } catch (Exception ignored) {}
            }
        } catch (IOException | CsvValidationException e) {
            System.out.println("Error calculating seasonal factors: " + e.getMessage());
        }

        // Calculate average per month then normalize
        Map<Integer, Double> factors = new HashMap<>();
        double totalAvg = monthlyData.values().stream()
                .flatMap(List::stream)
                .mapToDouble(Double::doubleValue)
                .average().orElse(1.0);

        for (Map.Entry<Integer, List<Double>> entry : monthlyData.entrySet()) {
            double monthAvg = entry.getValue().stream()
                    .mapToDouble(Double::doubleValue)
                    .average().orElse(totalAvg);
            factors.put(entry.getKey(), totalAvg > 0 ? monthAvg / totalAvg : 1.0);
        }
        return factors;
    }

    // Get all unique product IDs from CSV
    public List<String> getAllProductIds() {
        Set<String> productIds = new LinkedHashSet<>();
        try (CSVReader reader = new CSVReader(new FileReader(CSV_FILE))) {
            reader.readNext();
            String[] line;
            while ((line = reader.readNext()) != null) {
                try {
                    productIds.add(line[2].trim());
                } catch (Exception ignored) {}
            }
        } catch (IOException | CsvValidationException e) {
            System.out.println("Error reading product IDs: " + e.getMessage());
        }
        return new ArrayList<>(productIds);
    }

    // Get actual vs forecast comparison data for chart
    public Map<String, Object> getForecastChartData(
        String productId, int forecastMonths) {
        Map<String, Object> chartData = new LinkedHashMap<>();
        int normalizedForecastMonths = Math.max(0, forecastMonths);

        Map<String, Double> historical = getMonthlySales(productId);
        List<Double> forecasts = forecastNextMonths(
                productId, normalizedForecastMonths);

        List<String> labels = new ArrayList<>();
        List<Double> historicalValues = new ArrayList<>();
        List<Double> forecastValues = new ArrayList<>();

        List<String> histKeys = new ArrayList<>(historical.keySet());
        int start = Math.max(0, histKeys.size() - 6);

        for (int i = start; i < histKeys.size(); i++) {
            int monthsAgo = histKeys.size() - 1 - i;
            if (monthsAgo == 0) {
                labels.add("Last Month");
            } else {
                labels.add(monthsAgo + "m ago");
            }
            historicalValues.add(historical.get(histKeys.get(i)));
            forecastValues.add(null);
        }

        Double lastHistorical = historicalValues.isEmpty()
                ? null
                : historicalValues.get(historicalValues.size() - 1);

        for (int i = 0; i < normalizedForecastMonths; i++) {
            if (i == 0) {
                labels.add("Next Month");
            } else {
                labels.add("+" + (i + 1) + " months");
            }
            historicalValues.add(i == 0 ? lastHistorical : null);
            forecastValues.add(i < forecasts.size() ? forecasts.get(i) : null);
        }

        chartData.put("labels", labels);
        chartData.put("historical", historicalValues);
        chartData.put("forecast", forecasts);
        chartData.put("nextMonthForecast",
                forecasts.isEmpty() ? 0.0 : forecasts.get(0));

        return chartData;
    }

    // Get reorder recommendation
    public String getReorderRecommendation(String productId, int currentStock) {
        List<Double> forecasts = forecastNextMonths(productId, 1);
        if (forecasts.isEmpty()) return "Insufficient data for recommendation.";

        double nextMonthDemand = forecasts.get(0);
        if (currentStock < nextMonthDemand) {
            int orderQty = (int) Math.ceil(nextMonthDemand - currentStock);
            return "⚠ Order " + orderQty + " units — predicted demand: " +
                    (int) nextMonthDemand + " units next month.";
        } else {
            return "✓ Stock sufficient — predicted demand: " +
                    (int) nextMonthDemand + " units next month.";
        }
    }

    private LocalDate parseDate(String dateStr) {
        String[] formats = {
            "yyyy-MM-dd", "MM/dd/yyyy", "dd-MM-yyyy",
            "dd/MM/yyyy", "M/d/yyyy",   "d/M/yyyy"
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
