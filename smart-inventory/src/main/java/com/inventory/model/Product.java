package com.inventory.model;

public class Product {
    private int id;
    private String productId;
    private String name;
    private String category;
    private double unitPrice;
    private int reorderLevel;

    public Product() {}

    public Product(int id, String productId, String name, String category,
                   double unitPrice, int reorderLevel) {
        this.id = id;
        this.productId = productId;
        this.name = name;
        this.category = category;
        this.unitPrice = unitPrice;
        this.reorderLevel = reorderLevel;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    public int getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(int reorderLevel) { this.reorderLevel = reorderLevel; }

    @Override
    public String toString() {
        return productId + " - " + name + " (" + category + ")";
    }
}