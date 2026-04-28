package com.inventory.model;

public class Supplier {
    private int id;
    private String name;
    private int leadTimeDays;
    private double defectRate;
    private double reliabilityScore;
    private double costPerUnit;
    private String location;
    private double performanceScore;

    public Supplier() {}

    public Supplier(int id, String name, int leadTimeDays, double defectRate,
                    double reliabilityScore, double costPerUnit, 
                    String location, double performanceScore) {
        this.id = id;
        this.name = name;
        this.leadTimeDays = leadTimeDays;
        this.defectRate = defectRate;
        this.reliabilityScore = reliabilityScore;
        this.costPerUnit = costPerUnit;
        this.location = location;
        this.performanceScore = performanceScore;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getLeadTimeDays() { return leadTimeDays; }
    public void setLeadTimeDays(int leadTimeDays) { this.leadTimeDays = leadTimeDays; }

    public double getDefectRate() { return defectRate; }
    public void setDefectRate(double defectRate) { this.defectRate = defectRate; }

    public double getReliabilityScore() { return reliabilityScore; }
    public void setReliabilityScore(double reliabilityScore) { this.reliabilityScore = reliabilityScore; }

    public double getCostPerUnit() { return costPerUnit; }
    public void setCostPerUnit(double costPerUnit) { this.costPerUnit = costPerUnit; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public double getPerformanceScore() { return performanceScore; }
    public void setPerformanceScore(double performanceScore) { this.performanceScore = performanceScore; }

    @Override
    public String toString() {
        return name + " (Score: " + String.format("%.1f", performanceScore) + ")";
    }
}