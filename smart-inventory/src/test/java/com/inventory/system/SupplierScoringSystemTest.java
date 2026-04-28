package com.inventory.system;

import com.inventory.dao.SupplierDAO;
import com.inventory.model.Supplier;
import com.inventory.service.SupplierScoringService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * System-level tests for the Supplier Scoring pipeline.
 *
 * <p>Validates the complete workflow from raw supplier data
 * through scoring, ranking, grade assignment, and alerting —
 * end-to-end as a business scenario.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Supplier Scoring System Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SupplierScoringSystemTest {

    @Mock
    private SupplierDAO supplierDAO;

    @InjectMocks
    private SupplierScoringService service;

    // A realistic four-supplier pool
    private Supplier s1; // Best: high reliability, low lead-time, low defect, low cost
    private Supplier s2; // Good
    private Supplier s3; // Average
    private Supplier s4; // Worst: low reliability, high lead-time, high defect, high cost

    @BeforeEach
    void setUp() {
        s1 = new Supplier(1, "EliteCorp",    3,  0.2, 97.0,  80.0, "Mumbai",  0.0);
        s2 = new Supplier(2, "ReliableCo",   8,  1.5, 82.0, 150.0, "Delhi",   0.0);
        s3 = new Supplier(3, "MidSupplies",  15, 4.0, 68.0, 200.0, "Pune",    0.0);
        s4 = new Supplier(4, "WeakLink",     27, 8.5, 38.0, 320.0, "Chennai", 0.0);
    }

    // ── Scenario 1: Full Ranking Pipeline ─────────────────────

    @Test
    @Order(1)
    @DisplayName("SYSTEM: Full supplier pool is scored, ranked, and persisted correctly")
    void scenario_fullRankingPipeline() {
        List<Supplier> pool = Arrays.asList(s1, s2, s3, s4);
        when(supplierDAO.getAllSuppliers()).thenReturn(pool);
        when(supplierDAO.updateSupplierScore(anyInt(), anyDouble())).thenReturn(true);

        // Execute the scoring pipeline
        List<Supplier> ranked = service.scoreAllSuppliers();

        // Verify count
        assertEquals(4, ranked.size(), "All 4 suppliers should be ranked");

        // Verify scores were assigned
        for (Supplier s : ranked) {
            assertTrue(s.getPerformanceScore() >= 0.0,
                    s.getName() + " must have a non-negative score");
        }

        // Verify descending sort order
        for (int i = 0; i < ranked.size() - 1; i++) {
            assertTrue(ranked.get(i).getPerformanceScore() >=
                            ranked.get(i + 1).getPerformanceScore(),
                    "Suppliers must be in descending score order");
        }

        // Best supplier should have a higher score than the worst
        double topScore  = ranked.get(0).getPerformanceScore();
        double lastScore = ranked.get(ranked.size() - 1).getPerformanceScore();
        assertTrue(topScore > lastScore,
                "Top-scored supplier should outrank the bottom supplier");

        // Verify updateSupplierScore was called for each supplier
        verify(supplierDAO, times(4)).updateSupplierScore(anyInt(), anyDouble());
    }

    // ── Scenario 2: Grade & Status Consistency ────────────────

    @Test
    @Order(2)
    @DisplayName("SYSTEM: Grades and statuses are consistently assigned across the full ranking")
    void scenario_gradeStatusConsistency() {
        List<Supplier> pool = Arrays.asList(s1, s2, s3, s4);
        when(supplierDAO.getAllSuppliers()).thenReturn(pool);
        when(supplierDAO.updateSupplierScore(anyInt(), anyDouble())).thenReturn(true);

        List<Map<String, Object>> rankings = service.getSupplierRankings();
        assertEquals(4, rankings.size());

        for (Map<String, Object> entry : rankings) {
            double score  = (double) entry.get("score");
            String grade  = (String) entry.get("grade");
            String status = (String) entry.get("status");

            // Grade must match expected band
            String expectedGrade = service.getGrade(score);
            assertEquals(expectedGrade, grade,
                    "Grade mismatch for score " + score);

            // Status must match expected band
            String expectedStatus = service.getStatus(score);
            assertEquals(expectedStatus, status,
                    "Status mismatch for score " + score);
        }
    }

    // ── Scenario 3: Critical Supplier Alert ───────────────────

    @Test
    @Order(3)
    @DisplayName("SYSTEM: Critical supplier detection correctly flags underperformers")
    void scenario_criticalSupplierAlert() {
        List<Supplier> pool = Arrays.asList(s1, s2, s3, s4);
        when(supplierDAO.getAllSuppliers()).thenReturn(pool);
        when(supplierDAO.updateSupplierScore(anyInt(), anyDouble())).thenReturn(true);

        // Score all first
        service.scoreAllSuppliers();

        // Low threshold — only very poor suppliers flagged
        List<Supplier> critical = service.getCriticalSuppliers(50.0);
        for (Supplier s : critical) {
            assertTrue(s.getPerformanceScore() < 50.0,
                    s.getName() + " should be below threshold 50");
        }

        // High threshold — most suppliers flagged
        List<Supplier> mostCritical = service.getCriticalSuppliers(95.0);
        for (Supplier s : mostCritical) {
            assertTrue(s.getPerformanceScore() < 95.0);
        }
    }

    // ── Scenario 4: Best Supplier Selection ──────────────────

    @Test
    @Order(4)
    @DisplayName("SYSTEM: getBestSupplier always returns the highest-scoring supplier")
    void scenario_bestSupplierSelection() {
        List<Supplier> pool = Arrays.asList(s4, s3, s2, s1); // deliberately reversed
        when(supplierDAO.getAllSuppliers()).thenReturn(pool);
        when(supplierDAO.updateSupplierScore(anyInt(), anyDouble())).thenReturn(true);

        Supplier best = service.getBestSupplier();

        assertNotNull(best);
        // Best should be s1 (EliteCorp — best attributes)
        assertEquals("EliteCorp", best.getName(),
                "EliteCorp should be ranked #1 in this pool");
    }

    // ── Scenario 5: Score Breakdown Traceability ──────────────

    @Test
    @Order(5)
    @DisplayName("SYSTEM: Score breakdown is traceable and components sum to total score")
    void scenario_scoreBreakdownTraceability() {
        List<Supplier> pool = Arrays.asList(s1, s4);
        when(supplierDAO.getAllSuppliers()).thenReturn(pool);

        double totalScore = service.calculateScore(s1);
        Map<String, Double> breakdown = service.getScoreBreakdown(s1);

        // All four components must be present
        assertEquals(4, breakdown.size());
        assertTrue(breakdown.containsKey("Reliability (40%)"));
        assertTrue(breakdown.containsKey("Lead Time (30%)"));
        assertTrue(breakdown.containsKey("Defect Rate (20%)"));
        assertTrue(breakdown.containsKey("Cost Efficiency (10%)"));

        // All components must be non-negative
        breakdown.values().forEach(v ->
                assertTrue(v >= 0.0, "Breakdown component should be >= 0"));

        // Sum of components should match total score (within rounding tolerance)
        double componentSum = breakdown.values().stream()
                .mapToDouble(Double::doubleValue).sum();
        assertEquals(totalScore, componentSum, 1.0,
                "Component sum should match total score");
    }

    // ── Scenario 6: Edge Case — Single Supplier Pool ──────────

    @Test
    @Order(6)
    @DisplayName("SYSTEM: Single-supplier pool scores and ranks without errors")
    void scenario_singleSupplierPool() {
        when(supplierDAO.getAllSuppliers())
                .thenReturn(Arrays.asList(s2));
        when(supplierDAO.updateSupplierScore(anyInt(), anyDouble())).thenReturn(true);

        List<Supplier> ranked = service.scoreAllSuppliers();
        assertEquals(1, ranked.size());

        List<Map<String, Object>> rankings = service.getSupplierRankings();
        assertEquals(1, rankings.size());
        assertEquals(1, rankings.get(0).get("rank"), "Single supplier should be rank 1");

        // getBestSupplier returns the only supplier
        Supplier best = service.getBestSupplier();
        assertEquals("ReliableCo", best.getName());
    }

    // ── Scenario 7: Ranking Data Completeness ─────────────────

    @Test
    @Order(7)
    @DisplayName("SYSTEM: Every ranking entry contains complete business-readable data")
    void scenario_rankingDataCompleteness() {
        List<Supplier> pool = Arrays.asList(s1, s2, s3);
        when(supplierDAO.getAllSuppliers()).thenReturn(pool);
        when(supplierDAO.updateSupplierScore(anyInt(), anyDouble())).thenReturn(true);

        List<Map<String, Object>> rankings = service.getSupplierRankings();
        assertEquals(3, rankings.size());

        int expectedRank = 1;
        for (Map<String, Object> entry : rankings) {
            assertEquals(expectedRank++, entry.get("rank"),
                    "Ranks should be sequential from 1");

            // Non-null name
            assertNotNull(entry.get("name"), "Supplier name must not be null");

            // Score in valid range
            double score = (double) entry.get("score");
            assertTrue(score >= 0 && score <= 100,
                    "Score out of range: " + score);

            // Grade is a valid letter
            String grade = (String) entry.get("grade");
            assertTrue(grade.matches("A\\+|A|B|C|D|F"),
                    "Invalid grade letter: " + grade);

            // Status is a known label
            String status = (String) entry.get("status");
            assertTrue(
                    status.equals("Excellent") || status.equals("Good") ||
                    status.equals("Average")   || status.equals("Poor") ||
                    status.equals("Critical"),
                    "Unknown status label: " + status);
        }
    }
}
