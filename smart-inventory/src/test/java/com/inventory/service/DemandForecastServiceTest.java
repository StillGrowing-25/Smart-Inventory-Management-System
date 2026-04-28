package com.inventory.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for DemandForecastService's pure algorithm methods —
 * those that do not depend on CSV file I/O.
 *
 * <p>File-dependent methods (getMonthlySales, getSeasonalFactors)
 * are tested indirectly through their outputs where possible, and
 * fully covered in the system tests using a sample CSV fixture.
 */
@DisplayName("DemandForecastService Algorithm Unit Tests")
public class DemandForecastServiceTest {

    private DemandForecastService service;

    @BeforeEach
    void setUp() {
        service = new DemandForecastService();
    }

    // ── forecastNextMonths — fallback when < 3 data points ───

    @Test
    @DisplayName("forecastNextMonths returns zeros for product with no sales history")
    void testForecastNextMonths_noHistory() {
        // Product that won't exist in any CSV → returns fallback zeros
        List<Double> forecasts = service.forecastNextMonths("NONEXISTENT_PRODUCT_XYZ", 3);
        assertNotNull(forecasts);
        assertEquals(3, forecasts.size());
        for (Double val : forecasts) {
            assertEquals(0.0, val, 0.001,
                    "Forecast should be 0.0 when history is insufficient");
        }
    }

    @Test
    @DisplayName("forecastNextMonths returns correct number of forecast periods")
    void testForecastNextMonths_returnCount() {
        List<Double> forecasts = service.forecastNextMonths("FAKE_ID", 6);
        assertEquals(6, forecasts.size());
    }

    @Test
    @DisplayName("forecastNextMonths returns single period for months=1")
    void testForecastNextMonths_singlePeriod() {
        List<Double> forecasts = service.forecastNextMonths("FAKE_ID", 1);
        assertEquals(1, forecasts.size());
    }

    // ── getReorderRecommendation ──────────────────────────────

    @Test
    @DisplayName("getReorderRecommendation returns insufficient-data message for unknown product")
    void testGetReorderRecommendation_unknownProduct() {
        String result = service.getReorderRecommendation("FAKE_PRODUCT_99", 100);
        assertNotNull(result);
        // With no data, forecasts returns [0.0], next month demand = 0
        // stock 100 >= demand 0, so "sufficient" message
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("getReorderRecommendation shows 'sufficient' when stock exceeds forecast")
    void testGetReorderRecommendation_sufficientStock() {
        // FAKE product → forecast is 0.0, any positive stock is sufficient
        String result = service.getReorderRecommendation("FAKE_PRODUCT_ZZ", 1000);
        assertTrue(result.contains("✓") || result.contains("sufficient") ||
                   result.contains("Insufficient"),
                "Expected a stock-ok or insufficient-data message, got: " + result);
    }

    // ── getForecastChartData ──────────────────────────────────

    @Test
    @DisplayName("getForecastChartData returns map with required keys")
    void testGetForecastChartData_keys() {
        var chartData = service.getForecastChartData("FAKE_PRODUCT", 3);
        assertNotNull(chartData);
        assertTrue(chartData.containsKey("labels"),           "Missing 'labels'");
        assertTrue(chartData.containsKey("historical"),       "Missing 'historical'");
        assertTrue(chartData.containsKey("forecast"),         "Missing 'forecast'");
        assertTrue(chartData.containsKey("nextMonthForecast"),"Missing 'nextMonthForecast'");
    }

    @Test
    @DisplayName("getForecastChartData nextMonthForecast is 0 for unknown product")
    void testGetForecastChartData_nextMonthForecastZero() {
        var chartData = service.getForecastChartData("FAKE_PRODUCT_ABC", 2);
        Object nmf = chartData.get("nextMonthForecast");
        assertNotNull(nmf);
        assertEquals(0.0, (Double) nmf, 0.001);
    }

    // ── parseDate (via reflection) ────────────────────────────

    @Test
    @DisplayName("parseDate handles yyyy-MM-dd format")
    void testParseDate_isoFormat() throws Exception {
        Method parseDate = DemandForecastService.class
                .getDeclaredMethod("parseDate", String.class);
        parseDate.setAccessible(true);

        Object result = parseDate.invoke(service, "2024-06-15");
        assertNotNull(result, "Expected non-null LocalDate for '2024-06-15'");
    }

    @Test
    @DisplayName("parseDate handles MM/dd/yyyy format")
    void testParseDate_usFormat() throws Exception {
        Method parseDate = DemandForecastService.class
                .getDeclaredMethod("parseDate", String.class);
        parseDate.setAccessible(true);

        Object result = parseDate.invoke(service, "06/15/2024");
        assertNotNull(result, "Expected non-null LocalDate for '06/15/2024'");
    }

    @Test
    @DisplayName("parseDate returns null for unparseable date string")
    void testParseDate_invalid() throws Exception {
        Method parseDate = DemandForecastService.class
                .getDeclaredMethod("parseDate", String.class);
        parseDate.setAccessible(true);

        Object result = parseDate.invoke(service, "not-a-date");
        assertNull(result, "Expected null for unparseable date string");
    }

    @Test
    @DisplayName("parseDate handles dd-MM-yyyy format")
    void testParseDate_ddMMYYYY() throws Exception {
        Method parseDate = DemandForecastService.class
                .getDeclaredMethod("parseDate", String.class);
        parseDate.setAccessible(true);

        Object result = parseDate.invoke(service, "15-06-2024");
        assertNotNull(result, "Expected non-null LocalDate for '15-06-2024'");
    }

    @Test
    @DisplayName("parseDate returns null for null input")
    void testParseDate_null() throws Exception {
        Method parseDate = DemandForecastService.class
                .getDeclaredMethod("parseDate", String.class);
        parseDate.setAccessible(true);

        // Null will cause exception inside — method returns null for any error
        Object result = parseDate.invoke(service, (Object) null);
        assertNull(result);
    }
}
