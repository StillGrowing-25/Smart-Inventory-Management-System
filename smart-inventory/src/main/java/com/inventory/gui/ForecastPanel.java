package com.inventory.gui;

import com.inventory.model.User;
import com.inventory.service.DemandForecastService;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import java.util.Map;

public class ForecastPanel extends JPanel {

    private User currentUser;
    private DemandForecastService forecastService =
            new DemandForecastService();

    private JComboBox<String> productCombo;
    private JComboBox<Integer> monthsCombo;
    private JPanel chartPanel;
    private JLabel recommendationLabel;
    private JLabel forecastValueLabel;
    private JTextArea statsArea;

    public ForecastPanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_BASE);
        buildUI();
        loadProducts();
    }

    private void buildUI() {
        // ── Top Control Bar ───────────────────────────────────
        JPanel controlBar = new JPanel(new FlowLayout(
                FlowLayout.LEFT, 14, 12));
        controlBar.setBackground(UITheme.BG_SURFACE);
        controlBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(
                        0, 0, 1, 0, UITheme.BORDER),
                new EmptyBorder(0, 8, 0, 8)));

        JLabel titleLabel = new JLabel("AI Demand Forecasting");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);

        JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
        sep.setForeground(UITheme.BORDER);
        sep.setPreferredSize(new Dimension(1, 28));

        JLabel productLabel = new JLabel("Product:");
        productLabel.setFont(UITheme.FONT_SMALL);
        productLabel.setForeground(UITheme.TEXT_SECONDARY);

        productCombo = new JComboBox<>();
        productCombo.setPreferredSize(new Dimension(160, 34));
        UITheme.styleCombo(productCombo);

        JLabel monthsLabel = new JLabel("Forecast Months:");
        monthsLabel.setFont(UITheme.FONT_SMALL);
        monthsLabel.setForeground(UITheme.TEXT_SECONDARY);

        monthsCombo = new JComboBox<>(
                new Integer[]{1, 2, 3, 6});
        monthsCombo.setSelectedItem(3);
        monthsCombo.setPreferredSize(new Dimension(80, 34));
        UITheme.styleCombo(monthsCombo);

        JButton runBtn = UITheme.primaryBtn(
                "Run Forecast", UITheme.BLUE);
        runBtn.setPreferredSize(new Dimension(140, 34));
        runBtn.addActionListener(e -> runForecast());

        controlBar.add(titleLabel);
        controlBar.add(sep);
        controlBar.add(productLabel);
        controlBar.add(productCombo);
        controlBar.add(monthsLabel);
        controlBar.add(monthsCombo);
        controlBar.add(runBtn);

        // ── Chart Area ────────────────────────────────────────
        chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBackground(UITheme.BG_BASE);
        chartPanel.setBorder(new EmptyBorder(14, 18, 8, 18));

        JLabel placeholder = new JLabel(
                "Select a product and click 'Run Forecast' "
                + "to see AI predictions",
                SwingConstants.CENTER);
        placeholder.setForeground(UITheme.TEXT_MUTED);
        placeholder.setFont(new Font("Segoe UI",
                Font.ITALIC, 14));
        chartPanel.add(placeholder, BorderLayout.CENTER);

        // ── Bottom Cards ──────────────────────────────────────
        JPanel bottom = new JPanel(new GridLayout(1, 2, 14, 0));
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(0, 18, 18, 18));
        bottom.setPreferredSize(new Dimension(0, 150));

        bottom.add(buildRecommendationCard());
        bottom.add(buildStatsCard());

        add(controlBar,  BorderLayout.NORTH);
        add(chartPanel,  BorderLayout.CENTER);
        add(bottom,      BorderLayout.SOUTH);
    }

    private JPanel buildRecommendationCard() {
        JPanel card = new JPanel(new BorderLayout(0, 8)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_SURFACE);
                g2.fill(new RoundRectangle2D.Float(
                        0, 0, getWidth(), getHeight(),
                        12, 12));
                // Blue accent top bar
                g2.setColor(UITheme.BLUE);
                g2.fill(new RoundRectangle2D.Float(
                        0, 0, getWidth(), 4, 4, 4));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(14, 16, 14, 16));

        JLabel title = new JLabel("AI Recommendation");
        title.setFont(new Font("Segoe UI", Font.BOLD, 12));
        title.setForeground(UITheme.BLUE);

        forecastValueLabel = new JLabel("—");
        forecastValueLabel.setFont(
                new Font("Segoe UI", Font.BOLD, 32));
        forecastValueLabel.setForeground(UITheme.GREEN);

        recommendationLabel = new JLabel(
                "Run forecast to get recommendation");
        recommendationLabel.setFont(UITheme.FONT_SMALL);
        recommendationLabel.setForeground(UITheme.TEXT_SECONDARY);

        card.add(title,               BorderLayout.NORTH);
        card.add(forecastValueLabel,  BorderLayout.CENTER);
        card.add(recommendationLabel, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildStatsCard() {
        JPanel card = new JPanel(new BorderLayout(0, 8)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_SURFACE);
                g2.fill(new RoundRectangle2D.Float(
                        0, 0, getWidth(), getHeight(),
                        12, 12));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(14, 16, 14, 16));

        JLabel title = new JLabel("Forecast Details");
        title.setFont(new Font("Segoe UI", Font.BOLD, 12));
        title.setForeground(UITheme.BLUE);

        statsArea = new JTextArea("No forecast data yet...");
        statsArea.setEditable(false);
        statsArea.setOpaque(false);
        statsArea.setBackground(UITheme.BG_SURFACE);
        statsArea.setForeground(UITheme.TEXT_SECONDARY);
        statsArea.setFont(new Font("Consolas", Font.PLAIN, 11));

        JScrollPane sp = new JScrollPane(statsArea);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setBorder(BorderFactory.createEmptyBorder());

        card.add(title, BorderLayout.NORTH);
        card.add(sp,    BorderLayout.CENTER);
        return card;
    }

    private void loadProducts() {
        List<String> ids = forecastService.getAllProductIds();
        productCombo.removeAllItems();
        int limit = Math.min(ids.size(), 50);
        for (int i = 0; i < limit; i++) {
            productCombo.addItem(ids.get(i));
        }
    }

    private void runForecast() {if (productCombo.getSelectedItem() == null) {
        UITheme.showWarning(this,
                "Please select a product!", "Warning");
        return;
        }

        String productId = (String) productCombo
                .getSelectedItem();
        int months = (Integer) monthsCombo.getSelectedItem();

        recommendationLabel.setText("Running forecast...");
        forecastValueLabel.setText("...");

        SwingWorker<Map<String, Object>, Void> worker =
                new SwingWorker<>() {
            protected Map<String, Object> doInBackground() {
                return forecastService.getForecastChartData(
                        productId, months);
            }
            protected void done() {
                try {
                    updateChart(productId, get(), months);
                }  catch (Exception e) {
                        UITheme.showError(
                                ForecastPanel.this,
                                "Forecast error: " + e.getMessage(),
                                "Error");
                    }
            }
        };
        worker.execute();
    }

    @SuppressWarnings("unchecked")
    private void updateChart(String productId,
            Map<String, Object> data, int months) {

        List<String> labels =
                (List<String>) data.get("labels");
        List<Double> historical =
                (List<Double>) data.get("historical");
        List<Double> forecast =
                (List<Double>) data.get("forecast");
        double nextMonth =
                (double) data.get("nextMonthForecast");

        // ── Build Series ──────────────────────────────────────
        XYSeries histSeries  = new XYSeries("Historical Sales");
        XYSeries fcstSeries  = new XYSeries("AI Forecast");

        // Add historical points
        for (int i = 0; i < historical.size(); i++) {
            if (historical.get(i) != null) {
                histSeries.add(i, historical.get(i));
            }
        }

        // Find last historical index to connect lines
        int lastHistIdx = -1;
        for (int i = historical.size() - 1; i >= 0; i--) {
            if (historical.get(i) != null) {
                lastHistIdx = i;
                break;
            }
        }

        // Connect forecast line to last historical point
        if (lastHistIdx >= 0) {
            fcstSeries.add(lastHistIdx,
                    historical.get(lastHistIdx));
        }

        // Add forecast points
        int fcStart = lastHistIdx + 1;
        for (int i = 0; i < forecast.size(); i++) {
            if (forecast.get(i) != null) {
                fcstSeries.add(fcStart + i, forecast.get(i));
            }
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(histSeries);
        dataset.addSeries(fcstSeries);

        // ── Create Chart ──────────────────────────────────────
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Demand Forecast  —  " + productId,
                "", "Units Sold",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);

        // Chart background
        chart.setBackgroundPaint(UITheme.BG_SURFACE);
        chart.getTitle().setPaint(UITheme.TEXT_PRIMARY);
        chart.getTitle().setFont(
                new Font("Segoe UI", Font.BOLD, 13));
        chart.getLegend().setBackgroundPaint(UITheme.BG_SURFACE);
        chart.getLegend().setItemPaint(UITheme.TEXT_SECONDARY);
        chart.getLegend().setItemFont(UITheme.FONT_SMALL);

        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(UITheme.BG_BASE);
        plot.setDomainGridlinePaint(UITheme.BORDER);
        plot.setRangeGridlinePaint(UITheme.BORDER);
        plot.setOutlinePaint(UITheme.BORDER);

        // ── X Axis with readable labels ───────────────────────
        SymbolAxis xAxis = new SymbolAxis("",
                labels.toArray(new String[0]));
        xAxis.setTickLabelFont(
                new Font("Segoe UI", Font.PLAIN, 10));
        xAxis.setTickLabelPaint(UITheme.TEXT_SECONDARY);
        xAxis.setLabelPaint(UITheme.TEXT_MUTED);
        xAxis.setAxisLinePaint(UITheme.BORDER);
        plot.setDomainAxis(xAxis);

        // ── Y Axis ────────────────────────────────────────────
        NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
        yAxis.setTickLabelPaint(UITheme.TEXT_SECONDARY);
        yAxis.setTickLabelFont(
                new Font("Segoe UI", Font.PLAIN, 11));
        yAxis.setLabelPaint(UITheme.TEXT_MUTED);
        yAxis.setAxisLinePaint(UITheme.BORDER);
        yAxis.setAutoRangeIncludesZero(false);

        // ── Renderer ──────────────────────────────────────────
        XYLineAndShapeRenderer renderer =
                new XYLineAndShapeRenderer();
        // Historical — blue
        renderer.setSeriesPaint(0, UITheme.BLUE);
        renderer.setSeriesStroke(0, new BasicStroke(2.5f));
        renderer.setSeriesShapesVisible(0, true);
        renderer.setSeriesShape(0,
                new java.awt.geom.Ellipse2D.Double(
                        -4, -4, 8, 8));
        // Forecast — green dashed
        renderer.setSeriesPaint(1, UITheme.GREEN);
        renderer.setSeriesStroke(1, new BasicStroke(
                2.5f,
                BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND,
                1.0f,
                new float[]{8f, 4f},
                0f));
        renderer.setSeriesShapesVisible(1, true);
        renderer.setSeriesShape(1,
                new java.awt.geom.Ellipse2D.Double(
                        -4, -4, 8, 8));
        plot.setRenderer(renderer);

        // ── Update Panel ──────────────────────────────────────
        chartPanel.removeAll();
        ChartPanel cp = new ChartPanel(chart);
        cp.setBackground(UITheme.BG_SURFACE);
        cp.setBorder(new EmptyBorder(0, 0, 0, 0));
        chartPanel.add(cp, BorderLayout.CENTER);
        chartPanel.revalidate();
        chartPanel.repaint();

        // ── Update Cards ──────────────────────────────────────
        forecastValueLabel.setText(
                String.format("%,d", (int) nextMonth)
                + " units");
        recommendationLabel.setText(
                forecastService.getReorderRecommendation(
                        productId, 0));

        String[] fcLabels = {
            "Next Month", "+2 months", "+3 months",
            "+4 months", "+5 months", "+6 months"
        };
        StringBuilder sb = new StringBuilder();
        sb.append("Product    : ").append(productId)
          .append("\n");
        sb.append("Algorithm  : Moving Average")
          .append(" + Seasonal\n");
        sb.append("History    : Last 6 months\n\n");
        sb.append("Predictions:\n");
        for (int i = 0; i < forecast.size(); i++) {
            String lbl = i < fcLabels.length
                    ? fcLabels[i] : "+?" + " months";
            sb.append(String.format("  %-12s: %s units\n",
                    lbl,
                    forecast.get(i) != null
                            ? String.format("%,.0f",
                                    forecast.get(i))
                            : "N/A"));
        }
        statsArea.setText(sb.toString());
    }
}