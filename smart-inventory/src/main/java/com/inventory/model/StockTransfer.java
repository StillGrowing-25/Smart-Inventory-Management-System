package com.inventory.model;
import java.time.LocalDateTime;

public class StockTransfer {
    private int id;
    private int fromWarehouseId;
    private int toWarehouseId;
    private int productId;
    private int quantity;
    private int transferredBy;
    private String reason;
    private LocalDateTime transferredAt;

    // Extra fields for display
    private String fromWarehouseName;
    private String toWarehouseName;
    private String productName;
    private String transferredByName;

    public StockTransfer() {}

    public StockTransfer(int fromWarehouseId, int toWarehouseId,
                         int productId, int quantity,
                         int transferredBy, String reason) {
        this.fromWarehouseId = fromWarehouseId;
        this.toWarehouseId = toWarehouseId;
        this.productId = productId;
        this.quantity = quantity;
        this.transferredBy = transferredBy;
        this.reason = reason;
        this.transferredAt = LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getFromWarehouseId() { return fromWarehouseId; }
    public void setFromWarehouseId(int fromWarehouseId) { this.fromWarehouseId = fromWarehouseId; }

    public int getToWarehouseId() { return toWarehouseId; }
    public void setToWarehouseId(int toWarehouseId) { this.toWarehouseId = toWarehouseId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public int getTransferredBy() { return transferredBy; }
    public void setTransferredBy(int transferredBy) { this.transferredBy = transferredBy; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public LocalDateTime getTransferredAt() { return transferredAt; }
    public void setTransferredAt(LocalDateTime transferredAt) { this.transferredAt = transferredAt; }

    public String getFromWarehouseName() { return fromWarehouseName; }
    public void setFromWarehouseName(String fromWarehouseName) { this.fromWarehouseName = fromWarehouseName; }

    public String getToWarehouseName() { return toWarehouseName; }
    public void setToWarehouseName(String toWarehouseName) { this.toWarehouseName = toWarehouseName; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getTransferredByName() { return transferredByName; }
    public void setTransferredByName(String transferredByName) { this.transferredByName = transferredByName; }

    @Override
    public String toString() {
        return "Transfer " + quantity + " units of " + productName +
               " from " + fromWarehouseName +
               " to " + toWarehouseName +
               " | " + transferredAt;
    }
}