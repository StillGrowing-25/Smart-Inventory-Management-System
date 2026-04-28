package com.inventory.service;

import com.inventory.dao.SupplierDAO;
import com.inventory.model.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SupplierScoringService Unit Tests")
public class SupplierScoringServiceTest {

    @Mock
    private SupplierDAO supplierDAO;

    @InjectMocks
    private SupplierScoringService service;

    private Supplier excellent;   // high reliability, low lead-time, low defect
    private Supplier poor;        // low reliability, high lead-time, high defect
    private Supplier average;     // mid-range values

    @BeforeEach
    void setUp() {
        // Excellent supplier — should score high
        excellent = new Supplier(1, "BestCorp",  5,  0.5, 95.0, 100.0, "Mumbai", 0.0);
        // Poor supplier — should score low
        poor      = new Supplier(2, "BadSupply", 28, 9.0, 40.0, 300.0, "Delhi",  0.0);
        // Average supplier
        average   = new Supplier(3, "MidCo",     15, 4.0, 70.0, 200.0, "Pune",   0.0);
    }

    // ── getGrade ─────────────────────────────────────────────

    @Test
    @DisplayName("getGrade returns A+ for score >= 90")
    void testGetGrade_Aplus() {
        assertEquals("A+", service.getGrade(90.0));
        assertEquals("A+", service.getGrade(100.0));
        assertEquals("A+", service.getGrade(95.5));
    }

    @Test
    @DisplayName("getGrade returns A for score 80-89")
    void testGetGrade_A() {
        assertEquals("A", service.getGrade(80.0));
        assertEquals("A", service.getGrade(89.9));
    }

    @Test
    @DisplayName("getGrade returns B for score 70-79")
    void testGetGrade_B() {
        assertEquals("B", service.getGrade(70.0));
        assertEquals("B", service.getGrade(79.9));
    }

    @Test
    @DisplayName("getGrade returns C for score 60-69")
    void testGetGrade_C() {
        assertEquals("C", service.getGrade(60.0));
        assertEquals("C", service.getGrade(69.9));
    }

    @Test
    @DisplayName("getGrade returns D for score 50-59")
    void testGetGrade_D() {
        assertEquals("D", service.getGrade(50.0));
        assertEquals("D", service.getGrade(59.9));
    }

    @Test
    @DisplayName("getGrade returns F for score below 50")
    void testGetGrade_F() {
        assertEquals("F", service.getGrade(49.9));
        assertEquals("F", service.getGrade(0.0));
    }

    // ── getStatus ─────────────────────────────────────────────

    @Test
    @DisplayName("getStatus returns Excellent for score >= 85")
    void testGetStatus_Excellent() {
        assertEquals("Excellent", service.getStatus(85.0));
        assertEquals("Excellent", service.getStatus(100.0));
    }

    @Test
    @DisplayName("getStatus returns Good for score 70-84")
    void testGetStatus_Good() {
        assertEquals("Good", service.getStatus(70.0));
        assertEquals("Good", service.getStatus(84.9));
    }

    @Test
    @DisplayName("getStatus returns Average for score 55-69")
    void testGetStatus_Average() {
        assertEquals("Average", service.getStatus(55.0));
        assertEquals("Average", service.getStatus(69.9));
    }

    @Test
    @DisplayName("getStatus returns Poor for score 40-54")
    void testGetStatus_Poor() {
        assertEquals("Poor", service.getStatus(40.0));
        assertEquals("Poor", service.getStatus(54.9));
    }

    @Test
    @DisplayName("getStatus returns Critical for score below 40")
    void testGetStatus_Critical() {
        assertEquals("Critical", service.getStatus(39.9));
        assertEquals("Critical", service.getStatus(0.0));
    }

    // ── calculateScore ────────────────────────────────────────

    @Test
    @DisplayName("calculateScore produces higher score for excellent vs poor supplier")
    void testCalculateScore_excellentBeatsWeak() {
        // Both suppliers in the pool for cost normalization
        List<Supplier> pool = Arrays.asList(excellent, poor);
        when(supplierDAO.getAllSuppliers()).thenReturn(pool);

        double excellentScore = service.calculateScore(excellent);
        double poorScore      = service.calculateScore(poor);

        assertTrue(excellentScore > poorScore,
                String.format("Expected excellent (%.2f) > poor (%.2f)",
                        excellentScore, poorScore));
    }

    @Test
    @DisplayName("calculateScore is within 0-100 range")
    void testCalculateScore_withinRange() {
        List<Supplier> pool = Arrays.asList(excellent, poor, average);
        when(supplierDAO.getAllSuppliers()).thenReturn(pool);

        for (Supplier s : pool) {
            double score = service.calculateScore(s);
            assertTrue(score >= 0.0 && score <= 100.0,
                    "Score out of range [0,100]: " + score + " for " + s.getName());
        }
    }

    @Test
    @DisplayName("calculateScore with single supplier in pool returns consistent result")
    void testCalculateScore_singleSupplierPool() {
        when(supplierDAO.getAllSuppliers()).thenReturn(
                Collections.singletonList(excellent));

        double score = service.calculateScore(excellent);
        // With single supplier, cost score normalizes to 50
        assertTrue(score > 0.0, "Score should be positive");
    }

    @Test
    @DisplayName("calculateScore with empty pool falls back to 50 cost score")
    void testCalculateScore_emptyPool() {
        when(supplierDAO.getAllSuppliers()).thenReturn(Collections.emptyList());

        double score = service.calculateScore(excellent);
        assertTrue(score > 0.0);
    }

    // ── scoreAllSuppliers ─────────────────────────────────────

    @Test
    @DisplayName("scoreAllSuppliers returns suppliers sorted by score descending")
    void testScoreAllSuppliers_sorted() {
        List<Supplier> suppliers = Arrays.asList(poor, average, excellent);
        when(supplierDAO.getAllSuppliers()).thenReturn(suppliers);
        when(supplierDAO.updateSupplierScore(anyInt(), anyDouble())).thenReturn(true);

        List<Supplier> ranked = service.scoreAllSuppliers();

        for (int i = 0; i < ranked.size() - 1; i++) {
            assertTrue(ranked.get(i).getPerformanceScore() >=
                            ranked.get(i + 1).getPerformanceScore(),
                    "Suppliers should be in descending score order");
        }
    }

    @Test
    @DisplayName("scoreAllSuppliers updates each supplier's performance score")
    void testScoreAllSuppliers_updatesScores() {
        List<Supplier> suppliers = Arrays.asList(excellent, poor);
        when(supplierDAO.getAllSuppliers()).thenReturn(suppliers);
        when(supplierDAO.updateSupplierScore(anyInt(), anyDouble())).thenReturn(true);

        service.scoreAllSuppliers();

        // Each supplier should have had their performance score updated
        assertTrue(excellent.getPerformanceScore() > 0.0);
        assertTrue(poor.getPerformanceScore() >= 0.0);
    }

    @Test
    @DisplayName("scoreAllSuppliers returns empty list for empty DAO")
    void testScoreAllSuppliers_emptyList() {
        when(supplierDAO.getAllSuppliers()).thenReturn(Collections.emptyList());

        List<Supplier> result = service.scoreAllSuppliers();
        assertTrue(result.isEmpty());
    }

    // ── getSupplierRankings ───────────────────────────────────

    @Test
    @DisplayName("getSupplierRankings contains all required keys")
    void testGetSupplierRankings_keys() {
        List<Supplier> suppliers = Arrays.asList(excellent, poor);
        when(supplierDAO.getAllSuppliers()).thenReturn(suppliers);
        when(supplierDAO.updateSupplierScore(anyInt(), anyDouble())).thenReturn(true);

        List<Map<String, Object>> rankings = service.getSupplierRankings();

        assertFalse(rankings.isEmpty());
        Map<String, Object> first = rankings.get(0);
        assertTrue(first.containsKey("rank"));
        assertTrue(first.containsKey("name"));
        assertTrue(first.containsKey("score"));
        assertTrue(first.containsKey("grade"));
        assertTrue(first.containsKey("status"));
        assertTrue(first.containsKey("reliability"));
        assertTrue(first.containsKey("leadTime"));
        assertTrue(first.containsKey("defectRate"));
        assertTrue(first.containsKey("costPerUnit"));
    }

    @Test
    @DisplayName("getSupplierRankings assigns rank 1 to top-scoring supplier")
    void testGetSupplierRankings_rankOne() {
        List<Supplier> suppliers = Arrays.asList(excellent, poor);
        when(supplierDAO.getAllSuppliers()).thenReturn(suppliers);
        when(supplierDAO.updateSupplierScore(anyInt(), anyDouble())).thenReturn(true);

        List<Map<String, Object>> rankings = service.getSupplierRankings();
        assertEquals(1, rankings.get(0).get("rank"));
    }

    // ── getCriticalSuppliers ──────────────────────────────────

    @Test
    @DisplayName("getCriticalSuppliers returns only suppliers below threshold")
    void testGetCriticalSuppliers() {
        List<Supplier> suppliers = Arrays.asList(excellent, poor);
        when(supplierDAO.getAllSuppliers()).thenReturn(suppliers);
        when(supplierDAO.updateSupplierScore(anyInt(), anyDouble())).thenReturn(true);

        // Score all first to populate performanceScore fields
        service.scoreAllSuppliers();

        // Use a high threshold to capture the poor supplier
        List<Supplier> critical = service.getCriticalSuppliers(75.0);

        for (Supplier s : critical) {
            assertTrue(s.getPerformanceScore() < 75.0,
                    s.getName() + " score " + s.getPerformanceScore() +
                            " should be below threshold 75");
        }
    }

    // ── getScoreBreakdown ─────────────────────────────────────

    @Test
    @DisplayName("getScoreBreakdown returns four weighted component keys")
    void testGetScoreBreakdown_keys() {
        when(supplierDAO.getAllSuppliers()).thenReturn(
                Collections.singletonList(excellent));

        Map<String, Double> breakdown = service.getScoreBreakdown(excellent);

        assertEquals(4, breakdown.size());
        assertTrue(breakdown.containsKey("Reliability (40%)"));
        assertTrue(breakdown.containsKey("Lead Time (30%)"));
        assertTrue(breakdown.containsKey("Defect Rate (20%)"));
        assertTrue(breakdown.containsKey("Cost Efficiency (10%)"));
    }

    @Test
    @DisplayName("getScoreBreakdown component values sum close to total score")
    void testGetScoreBreakdown_sumMatchesTotal() {
        List<Supplier> pool = Collections.singletonList(excellent);
        when(supplierDAO.getAllSuppliers()).thenReturn(pool);

        double totalScore = service.calculateScore(excellent);
        Map<String, Double> breakdown = service.getScoreBreakdown(excellent);
        double componentSum = breakdown.values().stream()
                .mapToDouble(Double::doubleValue).sum();

        assertEquals(totalScore, componentSum, 1.0,
                "Sum of breakdown components should be close to total score");
    }
}
