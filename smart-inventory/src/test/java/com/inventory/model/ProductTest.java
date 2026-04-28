package com.inventory.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Product Model Unit Tests")
public class ProductTest {

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product(1, "P001", "Laptop", "Electronics", 799.99, 10);
    }

    // ── Constructor & Getters ────────────────────────────────

    @Test
    @DisplayName("All-args constructor sets all fields correctly")
    void testAllArgsConstructor() {
        assertEquals(1,            product.getId());
        assertEquals("P001",       product.getProductId());
        assertEquals("Laptop",     product.getName());
        assertEquals("Electronics",product.getCategory());
        assertEquals(799.99,       product.getUnitPrice(), 0.001);
        assertEquals(10,           product.getReorderLevel());
    }

    @Test
    @DisplayName("No-args constructor creates product with default values")
    void testNoArgsConstructor() {
        Product p = new Product();
        assertNull(p.getProductId());
        assertNull(p.getName());
        assertEquals(0,   p.getId());
        assertEquals(0,   p.getReorderLevel());
        assertEquals(0.0, p.getUnitPrice(), 0.001);
    }

    // ── Setters ──────────────────────────────────────────────

    @Test
    @DisplayName("setId updates product id")
    void testSetId() {
        product.setId(99);
        assertEquals(99, product.getId());
    }

    @Test
    @DisplayName("setProductId updates product id string")
    void testSetProductId() {
        product.setProductId("P999");
        assertEquals("P999", product.getProductId());
    }

    @Test
    @DisplayName("setName updates product name")
    void testSetName() {
        product.setName("Desktop");
        assertEquals("Desktop", product.getName());
    }

    @Test
    @DisplayName("setCategory updates category")
    void testSetCategory() {
        product.setCategory("Computers");
        assertEquals("Computers", product.getCategory());
    }

    @Test
    @DisplayName("setUnitPrice updates price")
    void testSetUnitPrice() {
        product.setUnitPrice(999.50);
        assertEquals(999.50, product.getUnitPrice(), 0.001);
    }

    @Test
    @DisplayName("setReorderLevel updates reorder level")
    void testSetReorderLevel() {
        product.setReorderLevel(20);
        assertEquals(20, product.getReorderLevel());
    }

    // ── toString ─────────────────────────────────────────────

    @Test
    @DisplayName("toString returns formatted product string")
    void testToString() {
        String result = product.toString();
        assertEquals("P001 - Laptop (Electronics)", result);
    }

    @Test
    @DisplayName("toString reflects updated values after setter calls")
    void testToStringAfterUpdate() {
        product.setProductId("P002");
        product.setName("Monitor");
        product.setCategory("Peripherals");
        assertEquals("P002 - Monitor (Peripherals)", product.toString());
    }

    // ── Edge Cases ────────────────────────────────────────────

    @Test
    @DisplayName("Product can hold zero unit price")
    void testZeroPrice() {
        product.setUnitPrice(0.0);
        assertEquals(0.0, product.getUnitPrice(), 0.001);
    }

    @Test
    @DisplayName("Product can hold zero reorder level")
    void testZeroReorderLevel() {
        product.setReorderLevel(0);
        assertEquals(0, product.getReorderLevel());
    }

    @Test
    @DisplayName("Product can hold negative reorder level")
    void testNegativeReorderLevel() {
        product.setReorderLevel(-5);
        assertEquals(-5, product.getReorderLevel());
    }
}
