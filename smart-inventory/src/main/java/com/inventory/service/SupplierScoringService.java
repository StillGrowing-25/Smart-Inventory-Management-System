package com.inventory.service;

import com.inventory.dao.SupplierDAO;
import com.inventory.model.Supplier;
import java.util.*;

public class SupplierScoringService {

    private SupplierDAO supplierDAO = new SupplierDAO();

    // Weights for scoring algorithm
    private static final double WEIGHT_RELIABILITY  = 0.40;
    private static final double WEIGHT_LEAD_TIME    = 0.30;
    private static final double WEIGHT_DEFECT_RATE  = 0.20;
    private static final double WEIGHT_COST         = 0.10;

    // Calculate score for a single supplier (0-100)
    public double calculateScore(Supplier supplier) {
        // Reliability score (already 0-100)
        double reliabilityScore = supplier.getReliabilityScore();

        // Lead time score — lower is better
        // Max lead time assumed 30 days, min 1 day
        double leadTimeScore = Math.max(0,
                100 - ((supplier.getLeadTimeDays() / 30.0) * 100));

        // Defect rate score — lower is better
        // Max defect rate assumed 10%, min 0%
        double defectScore = Math.max(0,
                100 - (supplier.getDefectRate() * 10));

        // Cost score — lower cost = higher score
        // Normalize against all suppliers
        double costScore = calculateCostScore(supplier);

        // Weighted final score
        double finalScore =
                (reliabilityScore * WEIGHT_RELIABILITY) +
                (leadTimeScore    * WEIGHT_LEAD_TIME)   +
                (defectScore      * WEIGHT_DEFECT_RATE) +
                (costScore        * WEIGHT_COST);

        return Math.round(finalScore * 100.0) / 100.0;
    }

    // Score all suppliers and save to DB
    public List<Supplier> scoreAllSuppliers() {
        List<Supplier> suppliers = supplierDAO.getAllSuppliers();

        for (Supplier supplier : suppliers) {
            double score = calculateScore(supplier);
            supplier.setPerformanceScore(score);
            supplierDAO.updateSupplierScore(supplier.getId(), score);
        }

        // Sort by score descending
        suppliers.sort((a, b) ->
                Double.compare(b.getPerformanceScore(), a.getPerformanceScore()));

        return suppliers;
    }

    // Get supplier ranking with grades
    public List<Map<String, Object>> getSupplierRankings() {
        List<Supplier> suppliers = scoreAllSuppliers();
        List<Map<String, Object>> rankings = new ArrayList<>();

        int rank = 1;
        for (Supplier supplier : suppliers) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("rank",          rank++);
            entry.put("name",          supplier.getName());
            entry.put("score",         supplier.getPerformanceScore());
            entry.put("grade",         getGrade(supplier.getPerformanceScore()));
            entry.put("reliability",   supplier.getReliabilityScore());
            entry.put("leadTime",      supplier.getLeadTimeDays());
            entry.put("defectRate",    supplier.getDefectRate());
            entry.put("costPerUnit",   supplier.getCostPerUnit());
            entry.put("status",        getStatus(supplier.getPerformanceScore()));
            rankings.add(entry);
        }
        return rankings;
    }

    // Get grade letter based on score
    public String getGrade(double score) {
        if (score >= 90) return "A+";
        if (score >= 80) return "A";
        if (score >= 70) return "B";
        if (score >= 60) return "C";
        if (score >= 50) return "D";
        return "F";
    }

    // Get status label
    public String getStatus(double score) {
        if (score >= 85) return "Excellent";
        if (score >= 70) return "Good";
        if (score >= 55) return "Average";
        if (score >= 40) return "Poor";
        return "Critical";
    }

    // Normalize cost score against all suppliers
    private double calculateCostScore(Supplier supplier) {
        List<Supplier> allSuppliers = supplierDAO.getAllSuppliers();
        if (allSuppliers.isEmpty()) return 50.0;

        double maxCost = allSuppliers.stream()
                .mapToDouble(Supplier::getCostPerUnit)
                .max().orElse(1.0);
        double minCost = allSuppliers.stream()
                .mapToDouble(Supplier::getCostPerUnit)
                .min().orElse(0.0);

        if (maxCost == minCost) return 50.0;

        // Lower cost = higher score
        return ((maxCost - supplier.getCostPerUnit()) /
                (maxCost - minCost)) * 100;
    }

    // Get best supplier recommendation
    public Supplier getBestSupplier() {
        List<Supplier> suppliers = scoreAllSuppliers();
        return suppliers.isEmpty() ? null : suppliers.get(0);
    }

    // Get suppliers below threshold — need attention
    public List<Supplier> getCriticalSuppliers(double threshold) {
        List<Supplier> suppliers = scoreAllSuppliers();
        List<Supplier> critical = new ArrayList<>();
        for (Supplier s : suppliers) {
            if (s.getPerformanceScore() < threshold) {
                critical.add(s);
            }
        }
        return critical;
    }

    // Get score breakdown for display
    public Map<String, Double> getScoreBreakdown(Supplier supplier) {
        Map<String, Double> breakdown = new LinkedHashMap<>();

        double reliabilityScore = supplier.getReliabilityScore();
        double leadTimeScore    = Math.max(0,
                100 - ((supplier.getLeadTimeDays() / 30.0) * 100));
        double defectScore      = Math.max(0,
                100 - (supplier.getDefectRate() * 10));
        double costScore        = calculateCostScore(supplier);

        breakdown.put("Reliability (40%)",
                Math.round(reliabilityScore * WEIGHT_RELIABILITY * 100.0) / 100.0);
        breakdown.put("Lead Time (30%)",
                Math.round(leadTimeScore * WEIGHT_LEAD_TIME * 100.0) / 100.0);
        breakdown.put("Defect Rate (20%)",
                Math.round(defectScore * WEIGHT_DEFECT_RATE * 100.0) / 100.0);
        breakdown.put("Cost Efficiency (10%)",
                Math.round(costScore * WEIGHT_COST * 100.0) / 100.0);

        return breakdown;
    }
}