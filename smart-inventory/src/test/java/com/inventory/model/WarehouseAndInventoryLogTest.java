package com.inventory.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Warehouse & InventoryLog Model Unit Tests")
public class WarehouseAndInventoryLogTest {

    // ══════════════════════════════════════════════════════════
    //  Warehouse
    // ══════════════════════════════════════════════════════════

    @Test
    @DisplayName("All-args constructor sets all Warehouse fields")
    void testWarehouseConstructor() {
        Warehouse w = new Warehouse(1, "North Hub", "Delhi", "Raj Sharma");
        assertEquals(1,           w.getId());
        assertEquals("North Hub", w.getName());
        assertEquals("Delhi",     w.getLocation());
        assertEquals("Raj Sharma",w.getManagerName());
    }

    @Test
    @DisplayName("No-args Warehouse constructor creates default object")
    void testWarehouseNoArgsConstructor() {
        Warehouse w = new Warehouse();
        assertEquals(0,   w.getId());
        assertNull(w.getName());
        assertNull(w.getLocation());
        assertNull(w.getManagerName());
    }

    @Test
    @DisplayName("Warehouse setters update fields correctly")
    void testWarehouseSetters() {
        Warehouse w = new Warehouse();
        w.setId(5);
        w.setName("South Depot");
        w.setLocation("Bangalore");
        w.setManagerName("Priya Nair");

        assertEquals(5,            w.getId());
        assertEquals("South Depot",w.getName());
        assertEquals("Bangalore",  w.getLocation());
        assertEquals("Priya Nair", w.getManagerName());
    }

    @Test
    @DisplayName("Warehouse toString returns 'Name - Location'")
    void testWarehouseToString() {
        Warehouse w = new Warehouse(2, "East Hub", "Kolkata", "Anand Das");
        assertEquals("East Hub - Kolkata", w.toString());
    }

    // ══════════════════════════════════════════════════════════
    //  InventoryLog
    // ══════════════════════════════════════════════════════════

    @Test
    @DisplayName("Main InventoryLog constructor sets all core fields")
    void testInventoryLogConstructor() {
        InventoryLog log = new InventoryLog(10, 2, 50, "Restock", 3);
        assertEquals(10,         log.getProductId());
        assertEquals(2,          log.getWarehouseId());
        assertEquals(50,         log.getChangeQty());
        assertEquals("Restock",  log.getReason());
        assertEquals(3,          log.getPerformedBy());
        assertNotNull(log.getLoggedAt());
    }

    @Test
    @DisplayName("isStockIn returns true for positive changeQty")
    void testIsStockIn_positive() {
        InventoryLog log = new InventoryLog(1, 1, 100, "Purchase", 1);
        assertTrue(log.isStockIn());
        assertFalse(log.isStockOut());
    }

    @Test
    @DisplayName("isStockOut returns true for negative changeQty")
    void testIsStockOut_negative() {
        InventoryLog log = new InventoryLog(1, 1, -30, "Sale", 1);
        assertTrue(log.isStockOut());
        assertFalse(log.isStockIn());
    }

    @Test
    @DisplayName("Zero changeQty is neither stock-in nor stock-out")
    void testZeroChangeQty() {
        InventoryLog log = new InventoryLog(1, 1, 0, "Adjustment", 1);
        assertFalse(log.isStockIn());
        assertFalse(log.isStockOut());
    }

    @Test
    @DisplayName("InventoryLog setters update display fields correctly")
    void testInventoryLogDisplayFields() {
        InventoryLog log = new InventoryLog();
        log.setProductName("Laptop");
        log.setWarehouseName("North Hub");
        log.setPerformedByName("Alice");

        assertEquals("Laptop",    log.getProductName());
        assertEquals("North Hub", log.getWarehouseName());
        assertEquals("Alice",     log.getPerformedByName());
    }

    @Test
    @DisplayName("InventoryLog setLoggedAt and getLoggedAt work correctly")
    void testSetLoggedAt() {
        InventoryLog log = new InventoryLog();
        LocalDateTime now = LocalDateTime.of(2025, 6, 15, 10, 30);
        log.setLoggedAt(now);
        assertEquals(now, log.getLoggedAt());
    }

    @Test
    @DisplayName("InventoryLog toString starts with '+' for stock-in")
    void testToString_stockIn() {
        InventoryLog log = new InventoryLog(1, 1, 50, "Restock", 1);
        log.setProductName("Laptop");
        log.setWarehouseName("Hub");
        String str = log.toString();
        assertTrue(str.startsWith("+50"));
    }

    @Test
    @DisplayName("InventoryLog toString does not prefix '+' for stock-out")
    void testToString_stockOut() {
        InventoryLog log = new InventoryLog(1, 1, -20, "Dispatch", 1);
        log.setProductName("Laptop");
        log.setWarehouseName("Hub");
        String str = log.toString();
        assertTrue(str.startsWith("-20"));
    }
}
