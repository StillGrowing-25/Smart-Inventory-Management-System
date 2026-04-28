package com.inventory.gui;

import com.inventory.model.User;
import com.inventory.service.AuthService;
import com.inventory.service.DeadStockService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DeadStockPanel extends JPanel {

    private User currentUser;
    private DeadStockService deadStockService =
            new DeadStockService();

    private JTable deadStockTable;
    private DefaultTableModel tableModel;
    private JLabel totalItemsLabel;
    private JLabel criticalLabel;
    private JLabel highUrgencyLabel;
    private JLabel estimatedLossLabel;

    private List<Map<String, Object>> deadStockItems =
            new ArrayList<>();

    public DeadStockPanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_BASE);
        buildUI();
    }

    private void buildUI() {
        // ── Top Bar ───────────────────────────────────────────
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(UITheme.BG_SURFACE);
        topBar.setBorder(new EmptyBorder(12, 18, 12, 18));

        JLabel title = new JLabel(
                "Dead Stock Detection & Analysis");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(UITheme.RED);

        JPanel btnPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);

        JButton scanBtn = UITheme.primaryBtn(
                "Scan Dead Stock", UITheme.RED);
        scanBtn.setPreferredSize(new Dimension(150, 34));
        scanBtn.addActionListener(e -> scanDeadStock());

        JButton saveBtn = UITheme.outlineBtn(
                "Save to Database", UITheme.BLUE);
        saveBtn.setPreferredSize(new Dimension(150, 34));
        saveBtn.addActionListener(e -> saveToDatabase());

        btnPanel.add(scanBtn);
        btnPanel.add(saveBtn);

        topBar.add(title,    BorderLayout.WEST);
        topBar.add(btnPanel, BorderLayout.EAST);

        // ── Summary Cards ─────────────────────────────────────
        JPanel summaryPanel = new JPanel(
                new GridLayout(1, 4, 14, 0));
        summaryPanel.setOpaque(false);
        summaryPanel.setBorder(
                new EmptyBorder(14, 18, 10, 18));

        JPanel c1 = buildStatCard(
                "Dead Stock Items",    "0", UITheme.RED);
        JPanel c2 = buildStatCard(
                "Critical Items",      "0", UITheme.RED);
        JPanel c3 = buildStatCard(
                "High Urgency",        "0", UITheme.AMBER);
        JPanel c4 = buildStatCard(
                "Estimated Loss ($)",  "0", UITheme.AMBER);

        totalItemsLabel    = getCardValue(c1);
        criticalLabel      = getCardValue(c2);
        highUrgencyLabel   = getCardValue(c3);
        estimatedLossLabel = getCardValue(c4);

        summaryPanel.add(c1);
        summaryPanel.add(c2);
        summaryPanel.add(c3);
        summaryPanel.add(c4);

        JPanel headerPanel = new JPanel(
                new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.add(topBar,       BorderLayout.NORTH);
        headerPanel.add(summaryPanel, BorderLayout.SOUTH);

        // ── Table ─────────────────────────────────────────────
        JPanel centerPanel = new JPanel(
                new BorderLayout(0, 8));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(
                new EmptyBorder(0, 18, 18, 18));

        JLabel infoLabel = new JLabel(
                "Click 'Scan Dead Stock' to analyze "
                        + "inventory movement from CSV data");
        infoLabel.setForeground(UITheme.TEXT_MUTED);
        infoLabel.setFont(new Font("Segoe UI",
                Font.ITALIC, 11));

        String[] cols = {
                "Product ID", "Store", "Current Stock",
                "Days Idle", "Last Sale Date",
                "Avg Monthly Sales",
                "Suggested Discount%", "Urgency"
        };
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(
                    int r, int c) { return false; }
        };
        deadStockTable = new JTable(tableModel);
        UITheme.styleTable(deadStockTable);
        deadStockTable.getColumnModel().getColumn(7)
                .setCellRenderer(new UrgencyRenderer());
        deadStockTable.getColumnModel().getColumn(6)
                .setCellRenderer(new DiscountRenderer());

        centerPanel.add(infoLabel,
                BorderLayout.NORTH);
        centerPanel.add(
                UITheme.scrollPane(deadStockTable),
                BorderLayout.CENTER);

        add(headerPanel,  BorderLayout.NORTH);
        add(centerPanel,  BorderLayout.CENTER);
    }

    // ── Stat Card ─────────────────────────────────────────────
    private JPanel buildStatCard(String label,
                                 String value, Color accent) {
        JPanel card = new JPanel(
                new BorderLayout(0, 6)) {
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
                g2.setColor(accent);
                g2.fill(new RoundRectangle2D.Float(
                        0, 0, 4, getHeight(), 4, 4));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(UITheme.TEXT_MUTED);

        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.BOLD, 26));
        val.setForeground(accent);
        val.setName("cardval");

        card.add(lbl, BorderLayout.NORTH);
        card.add(val, BorderLayout.CENTER);
        return card;
    }

    private JLabel getCardValue(JPanel card) {
        for (Component c : card.getComponents()) {
            if (c instanceof JLabel && "cardval".equals(
                    ((JLabel) c).getName()))
                return (JLabel) c;
        }
        return new JLabel();
    }

    // ── Scan ──────────────────────────────────────────────────
    private void scanDeadStock() {
        tableModel.setRowCount(0);
        totalItemsLabel.setText("...");

        SwingWorker<List<Map<String, Object>>, Void>
                worker = new SwingWorker<>() {
            protected List<Map<String, Object>>
            doInBackground() {
                return deadStockService.detectDeadStock();
            }
            protected void done() {
                try {
                    List<Map<String, Object>> all = get();
                    // Filter by warehouse for
                    // non-admin users
                    deadStockItems = new ArrayList<>();
                    for (Map<String, Object> item : all) {
                        String storeId = item.get(
                                "storeId").toString();
                        int wid =
                                storeToWarehouse(storeId);
                        if (AuthService
                                .canAccessWarehouse(wid)) {
                            deadStockItems.add(item);
                        }
                    }
                    populateTable(deadStockItems);
                    updateSummary(deadStockItems);
                } catch (Exception e) {
                    // CHANGED: was JOptionPane.showMessageDialog
                    UITheme.showError(
                            DeadStockPanel.this,
                            "Error: " + e.getMessage(),
                            "Error");
                }
            }
        };
        worker.execute();
    }

    // ── Populate Table ────────────────────────────────────────
    private void populateTable(
            List<Map<String, Object>> deadStock) {
        tableModel.setRowCount(0);
        for (Map<String, Object> item : deadStock) {
            tableModel.addRow(new Object[]{
                    item.get("productId"),
                    item.get("storeId"),
                    item.get("currentStock"),
                    item.get("daysSinceLastSale"),
                    item.get("lastSaleDate"),
                    item.get("avgMonthlySales"),
                    item.get("suggestedDiscount") + "%",
                    item.get("urgency")
            });
        }
    }

    // ── Update Summary Cards ──────────────────────────────────
    private void updateSummary(
            List<Map<String, Object>> deadStock) {
        int total    = deadStock.size();
        int critical = (int) deadStock.stream()
                .filter(d -> "CRITICAL".equals(
                        d.get("urgency")))
                .count();
        int high     = (int) deadStock.stream()
                .filter(d -> "HIGH".equals(
                        d.get("urgency")))
                .count();
        double loss  = deadStock.stream()
                .mapToDouble(d -> (double)
                        d.get("estimatedLoss"))
                .sum();

        totalItemsLabel.setText(
                String.valueOf(total));
        criticalLabel.setText(
                String.valueOf(critical));
        highUrgencyLabel.setText(
                String.valueOf(high));
        estimatedLossLabel.setText(
                "$" + String.format("%.2f", loss));
    }

    // ── Save to Database ──────────────────────────────────────
    private void saveToDatabase() {
        if (deadStockItems == null
                || deadStockItems.isEmpty()) {
            // CHANGED: was JOptionPane.showMessageDialog with WARNING_MESSAGE
            UITheme.showWarning(this,
                    "Please scan for dead stock first!",
                    "Warning");
            return;
        }

        SwingWorker<Integer, Void> worker =
                new SwingWorker<>() {
                    protected Integer doInBackground() {
                        deadStockService
                                .saveDeadStockToDatabase(
                                        deadStockItems);
                        return deadStockItems.size();
                    }
                    protected void done() {
                        try {
                            int saved = get();
                            // CHANGED: was JOptionPane.showMessageDialog with INFORMATION_MESSAGE
                            UITheme.showSuccess(
                                    DeadStockPanel.this,
                                    "Saved " + saved
                                            + " dead stock records!",
                                    "Success");
                        } catch (Exception e) {
                            // CHANGED: was JOptionPane.showMessageDialog with ERROR_MESSAGE
                            UITheme.showError(
                                    DeadStockPanel.this,
                                    "Error: " + e.getMessage(),
                                    "Error");
                        }
                    }
                };
        worker.execute();
    }

    // ── Store → Warehouse mapping ─────────────────────────────
    private int storeToWarehouse(String storeId) {
        switch (storeId) {
            case "S001": return 1;
            case "S002": return 2;
            case "S003": return 3;
            case "S004": return 4;
            case "S005": return 5;
            default:     return -1;
        }
    }

    // ── Renderers ─────────────────────────────────────────────
    class UrgencyRenderer
            extends DefaultTableCellRenderer {
        @Override
        public Component
        getTableCellRendererComponent(
                JTable table, Object value,
                boolean isSelected, boolean hasFocus,
                int row, int col) {
            super.getTableCellRendererComponent(
                    table, value, isSelected,
                    hasFocus, row, col);
            if (value == null) return this;
            switch (value.toString()) {
                case "CRITICAL":
                    setForeground(UITheme.RED);
                    setFont(getFont().deriveFont(
                            Font.BOLD));
                    break;
                case "HIGH":
                    setForeground(UITheme.AMBER);
                    setFont(getFont().deriveFont(
                            Font.BOLD));
                    break;
                case "MEDIUM":
                    setForeground(new Color(
                            250, 200, 100));
                    setFont(getFont().deriveFont(
                            Font.PLAIN));
                    break;
                default:
                    setForeground(UITheme.GREEN);
                    setFont(getFont().deriveFont(
                            Font.PLAIN));
                    break;
            }
            return this;
        }
    }

    class DiscountRenderer
            extends DefaultTableCellRenderer {
        @Override
        public Component
        getTableCellRendererComponent(
                JTable table, Object value,
                boolean isSelected, boolean hasFocus,
                int row, int col) {
            super.getTableCellRendererComponent(
                    table, value, isSelected,
                    hasFocus, row, col);
            setForeground(UITheme.AMBER);
            setFont(getFont().deriveFont(Font.BOLD));
            return this;
        }
    }
}