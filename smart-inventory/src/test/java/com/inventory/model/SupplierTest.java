package com.inventory.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Supplier Model Unit Tests")
public class SupplierTest {

    private Supplier supplier;

    @BeforeEach
    void setUp() {
        supplier = new Supplier(1, "TechCorp", 7, 2.5, 88.0, 150.0, "Mumbai", 82.5);
    }

    // ── Constructor & Getters ────────────────────────────────

    @Test
    @DisplayName("All-args constructor sets all fields correctly")
    void testAllArgsConstructor() {
        assertEquals(1,        supplier.getId());
        assertEquals("TechCorp", supplier.getName());
        assertEquals(7,        supplier.getLeadTimeDays());
        assertEquals(2.5,      supplier.getDefectRate(), 0.001);
        assertEquals(88.0,     supplier.getReliabilityScore(), 0.001);
        assertEquals(150.0,    supplier.getCostPerUnit(), 0.001);
        assertEquals("Mumbai", supplier.getLocation());
        assertEquals(82.5,     supplier.getPerformanceScore(), 0.001);
    }

    @Test
    @DisplayName("No-args constructor creates supplier with default values")
    void testNoArgsConstructor() {
        Supplier s = new Supplier();
        assertNull(s.getName());
        assertEquals(0,   s.getId());
        assertEquals(0,   s.getLeadTimeDays());
        assertEquals(0.0, s.getDefectRate(), 0.001);
    }

    // ── Setters ──────────────────────────────────────────────

    @Test
    @DisplayName("setId updates supplier id")
    void testSetId() {
        supplier.setId(42);
        assertEquals(42, supplier.getId());
    }

    @Test
    @DisplayName("setName updates supplier name")
    void testSetName() {
        supplier.setName("GlobalSupply");
        assertEquals("GlobalSupply", supplier.getName());
    }

    @Test
    @DisplayName("setLeadTimeDays updates lead time")
    void testSetLeadTimeDays() {
        supplier.setLeadTimeDays(14);
        assertEquals(14, supplier.getLeadTimeDays());
    }

    @Test
    @DisplayName("setDefectRate updates defect rate")
    void testSetDefectRate() {
        supplier.setDefectRate(0.5);
        assertEquals(0.5, supplier.getDefectRate(), 0.001);
    }

    @Test
    @DisplayName("setReliabilityScore updates reliability score")
    void testSetReliabilityScore() {
        supplier.setReliabilityScore(95.0);
        assertEquals(95.0, supplier.getReliabilityScore(), 0.001);
    }

    @Test
    @DisplayName("setCostPerUnit updates cost")
    void testSetCostPerUnit() {
        supplier.setCostPerUnit(200.0);
        assertEquals(200.0, supplier.getCostPerUnit(), 0.001);
    }

    @Test
    @DisplayName("setLocation updates location")
    void testSetLocation() {
        supplier.setLocation("Delhi");
        assertEquals("Delhi", supplier.getLocation());
    }

    @Test
    @DisplayName("setPerformanceScore updates score")
    void testSetPerformanceScore() {
        supplier.setPerformanceScore(91.3);
        assertEquals(91.3, supplier.getPerformanceScore(), 0.001);
    }

    // ── toString ─────────────────────────────────────────────

    @Test
    @DisplayName("toString returns formatted string with score")
    void testToString() {
        String result = supplier.toString();
        assertEquals("TechCorp (Score: 82.5)", result);
    }

    @Test
    @DisplayName("toString rounds score to one decimal place")
    void testToStringRounding() {
        supplier.setPerformanceScore(75.678);
        String result = supplier.toString();
        assertEquals("TechCorp (Score: 75.7)", result);
    }

    // ── Edge Cases ────────────────────────────────────────────

    @Test
    @DisplayName("Supplier handles zero defect rate")
    void testZeroDefectRate() {
        supplier.setDefectRate(0.0);
        assertEquals(0.0, supplier.getDefectRate(), 0.001);
    }

    @Test
    @DisplayName("Supplier handles maximum reliability score of 100")
    void testMaxReliabilityScore() {
        supplier.setReliabilityScore(100.0);
        assertEquals(100.0, supplier.getReliabilityScore(), 0.001);
    }
}
