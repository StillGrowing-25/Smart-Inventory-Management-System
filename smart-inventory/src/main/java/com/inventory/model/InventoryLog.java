package com.inventory.model;

import java.time.LocalDateTime;

public class InventoryLog {
    private int id;
    private int productId;
    private int warehouseId;
    private int changeQty;
    private String reason;
    private int performedBy;
    private LocalDateTime loggedAt;

    // Extra fields for display purposes
    private String productName;
    private String warehouseName;
    private String performedByName;

    public InventoryLog() {}

    public InventoryLog(int productId, int warehouseId, int changeQty,
                        String reason, int performedBy) {
        this.productId = productId;
        this.warehouseId = warehouseId;
        this.changeQty = changeQty;
        this.reason = reason;
        this.performedBy = performedBy;
        this.loggedAt = LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public int getWarehouseId() { return warehouseId; }
    public void setWarehouseId(int warehouseId) { this.warehouseId = warehouseId; }

    public int getChangeQty() { return changeQty; }
    public void setChangeQty(int changeQty) { this.changeQty = changeQty; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public int getPerformedBy() { return performedBy; }
    public void setPerformedBy(int performedBy) { this.performedBy = performedBy; }

    public LocalDateTime getLoggedAt() { return loggedAt; }
    public void setLoggedAt(LocalDateTime loggedAt) { this.loggedAt = loggedAt; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }

    public String getPerformedByName() { return performedByName; }
    public void setPerformedByName(String performedByName) { this.performedByName = performedByName; }

    public boolean isStockIn() { return changeQty > 0; }
    public boolean isStockOut() { return changeQty < 0; }

    @Override
    public String toString() {
        return (changeQty > 0 ? "+" : "") + changeQty +
               " | " + productName +
               " | " + warehouseName +
               " | " + reason +
               " | " + loggedAt;
    }
}