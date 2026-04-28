package com.inventory.gui;

import com.inventory.dao.WarehouseDAO;
import com.inventory.dao.ProductDAO;
import com.inventory.model.User;
import com.inventory.model.Warehouse;
import com.inventory.model.Product;
import com.inventory.model.StockTransfer;
import com.inventory.service.AuthService;
import com.inventory.service.RebalancingService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import java.util.Map;

public class WarehousePanel extends JPanel {

    private User currentUser;
    private WarehouseDAO warehouseDAO =
            new WarehouseDAO();
    private ProductDAO productDAO =
            new ProductDAO();
    private RebalancingService rebalancingService =
            new RebalancingService();

    private JTable stockTable;
    private DefaultTableModel stockTableModel;
    private JTable transferTable;
    private DefaultTableModel transferTableModel;
    private JTable rebalanceTable;
    private DefaultTableModel rebalanceTableModel;

    private JComboBox<String> fromWarehouseCombo;
    private JComboBox<String> toWarehouseCombo;
    private JComboBox<String> productCombo;
    private JTextField transferQtyField;

    private List<Warehouse> warehouses;
    private List<Product> products;

    public WarehousePanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_BASE);
        buildUI();
        refreshData();
    }

    private void buildUI() {
        // ── Top Bar ───────────────────────────────────────────
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(UITheme.BG_SURFACE);
        topBar.setBorder(new EmptyBorder(12, 18, 12, 18));

        JLabel title = new JLabel(
                "Warehouse Management & Stock Transfers");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(UITheme.BLUE);

        JButton refreshBtn = UITheme.outlineBtn(
                "Refresh", UITheme.BLUE);
        refreshBtn.setPreferredSize(new Dimension(100, 34));
        refreshBtn.addActionListener(e -> refreshData());

        topBar.add(title,      BorderLayout.WEST);
        topBar.add(refreshBtn, BorderLayout.EAST);

        // ── Tabs ──────────────────────────────────────────────
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(UITheme.BG_BASE);
        tabs.setForeground(UITheme.TEXT_SECONDARY);
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 12));

        tabs.addTab("  Stock Overview  ",
                buildStockOverviewTab());
        tabs.addTab("  Transfer Stock  ",
                buildTransferTab());
        tabs.addTab("  Auto Rebalancing  ",
                buildRebalancingTab());
        tabs.addTab("  Transfer History  ",
                buildHistoryTab());

        add(topBar, BorderLayout.NORTH);
        add(tabs,   BorderLayout.CENTER);
    }

    // ── Stock Overview Tab ────────────────────────────────────
    private JPanel buildStockOverviewTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(UITheme.BG_BASE);
        panel.setBorder(new EmptyBorder(14, 18, 18, 18));

        JLabel sub = new JLabel(
                "Stock levels across all warehouses");
        sub.setForeground(UITheme.TEXT_MUTED);
        sub.setFont(new Font("Segoe UI",
                Font.ITALIC, 12));

        String[] cols = {
            "Warehouse", "Product", "Category",
            "Stock Qty", "Reorder Level", "Status"
        };
        stockTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(
                    int r, int c) { return false; }
        };
        stockTable = new JTable(stockTableModel);
        UITheme.styleTable(stockTable);
        stockTable.getColumnModel().getColumn(5)
                .setCellRenderer(new StatusRenderer());
        stockTable.getColumnModel().getColumn(0)
                .setPreferredWidth(140);
        stockTable.getColumnModel().getColumn(1)
                .setPreferredWidth(160);

        panel.add(sub, BorderLayout.NORTH);
        panel.add(UITheme.scrollPane(stockTable),
                BorderLayout.CENTER);
        return panel;
    }

    // ── Transfer Tab ──────────────────────────────────────────
    private JPanel buildTransferTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.BG_BASE);
        panel.setBorder(new EmptyBorder(18, 18, 18, 18));

        JPanel card = new JPanel(
                new GridBagLayout()) {
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
        card.setBorder(new EmptyBorder(24, 32, 24, 32));

        GridBagConstraints gbc =
                new GridBagConstraints();
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets  = new Insets(6, 8, 6, 8);

        // Title
        JLabel cardTitle = new JLabel(
                "Transfer Stock Between Warehouses");
        cardTitle.setFont(new Font("Segoe UI",
                Font.BOLD, 14));
        cardTitle.setForeground(UITheme.TEXT_PRIMARY);
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        card.add(cardTitle, gbc);
        gbc.gridwidth = 1;

        // From warehouse
        fromWarehouseCombo = new JComboBox<>();
        UITheme.styleCombo(fromWarehouseCombo);
        addTransferRow(card, gbc,
                "From Warehouse:", fromWarehouseCombo, 1);

        // To warehouse
        toWarehouseCombo = new JComboBox<>();
        UITheme.styleCombo(toWarehouseCombo);
        addTransferRow(card, gbc,
                "To Warehouse:", toWarehouseCombo, 2);

        // Product
        productCombo = new JComboBox<>();
        UITheme.styleCombo(productCombo);
        addTransferRow(card, gbc,
                "Product:", productCombo, 3);

        // Quantity
        transferQtyField = UITheme.inputField("");
        addTransferRow(card, gbc,
                "Quantity:", transferQtyField, 4);

        // Stock info
        JLabel stockInfoLabel = new JLabel(
                "Select warehouse and product "
                + "to see stock level");
        stockInfoLabel.setForeground(UITheme.TEXT_MUTED);
        stockInfoLabel.setFont(new Font("Segoe UI",
                Font.ITALIC, 11));
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.gridwidth = 2;
        card.add(stockInfoLabel, gbc);

        // Transfer button
        JButton transferBtn = UITheme.primaryBtn(
                "Execute Transfer", UITheme.BLUE);
        transferBtn.setPreferredSize(
                new Dimension(0, 42));
        gbc.gridy = 6;
        card.add(transferBtn, gbc);

        fromWarehouseCombo.addActionListener(
                e -> updateStockInfo(stockInfoLabel));
        productCombo.addActionListener(
                e -> updateStockInfo(stockInfoLabel));
        transferBtn.addActionListener(
                e -> executeTransfer());

        // Center the card
        JPanel wrapper = new JPanel(
                new GridBagLayout());
        wrapper.setBackground(UITheme.BG_BASE);
        GridBagConstraints wgbc =
                new GridBagConstraints();
        wgbc.fill    = GridBagConstraints.BOTH;
        wgbc.weightx = 1;
        wgbc.weighty = 1;
        wrapper.add(card, wgbc);

        panel.add(wrapper, BorderLayout.CENTER);
        return panel;
    }

    private void addTransferRow(JPanel panel,
            GridBagConstraints gbc, String labelText,
            JComponent field, int row) {
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI",
                Font.BOLD, 12));
        lbl.setForeground(UITheme.TEXT_SECONDARY);
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 1; gbc.weightx = 0.3;
        panel.add(lbl, gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        field.setPreferredSize(
                new Dimension(0, 36));
        panel.add(field, gbc);
    }

    // ── Rebalancing Tab ───────────────────────────────────────
    private JPanel buildRebalancingTab() {
        JPanel panel = new JPanel(
                new BorderLayout(0, 10));
        panel.setBackground(UITheme.BG_BASE);
        panel.setBorder(new EmptyBorder(14, 18, 18, 18));

        JPanel btnRow = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnRow.setOpaque(false);

        JButton findBtn = UITheme.primaryBtn(
                "Find Rebalancing Opportunities",
                UITheme.GREEN);
        findBtn.setPreferredSize(
                new Dimension(260, 36));
        findBtn.addActionListener(
                e -> findRebalancingOpportunities());

        JButton applyBtn = UITheme.outlineBtn(
                "Apply Selected Transfer", UITheme.BLUE);
        applyBtn.setPreferredSize(
                new Dimension(200, 36));
        applyBtn.addActionListener(
                e -> applySelectedRebalancing());

        btnRow.add(findBtn);
        btnRow.add(applyBtn);

        JLabel info = new JLabel(
                "Detects imbalanced stock across "
                + "warehouses and suggests transfers");
        info.setForeground(UITheme.TEXT_MUTED);
        info.setFont(new Font("Segoe UI",
                Font.ITALIC, 11));

        String[] cols = {
            "Product", "From Warehouse", "From Stock",
            "To Warehouse", "To Stock",
            "Transfer Qty", "Balance Score"
        };
        rebalanceTableModel =
                new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(
                    int r, int c) { return false; }
        };
        rebalanceTable = new JTable(rebalanceTableModel);
        UITheme.styleTable(rebalanceTable);

        JPanel top = new JPanel(new BorderLayout(0, 6));
        top.setOpaque(false);
        top.add(btnRow, BorderLayout.NORTH);
        top.add(info,   BorderLayout.SOUTH);

        panel.add(top, BorderLayout.NORTH);
        panel.add(UITheme.scrollPane(rebalanceTable),
                BorderLayout.CENTER);
        return panel;
    }

    // ── History Tab ───────────────────────────────────────────
    private JPanel buildHistoryTab() {
        JPanel panel = new JPanel(
                new BorderLayout(0, 10));
        panel.setBackground(UITheme.BG_BASE);
        panel.setBorder(new EmptyBorder(14, 18, 18, 18));

        JLabel sub = new JLabel(
                "Complete history of all stock transfers");
        sub.setForeground(UITheme.TEXT_MUTED);
        sub.setFont(new Font("Segoe UI",
                Font.ITALIC, 12));

        String[] cols = {
            "Product", "From", "To",
            "Quantity", "Reason", "By", "Date"
        };
        transferTableModel =
                new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(
                    int r, int c) { return false; }
        };
        JTable historyTable =
                new JTable(transferTableModel);
        UITheme.styleTable(historyTable);

        panel.add(sub, BorderLayout.NORTH);
        panel.add(UITheme.scrollPane(historyTable),
                BorderLayout.CENTER);
        return panel;
    }

    // ── Execute Transfer ──────────────────────────────────────
    private void executeTransfer() {
        try {
            if (fromWarehouseCombo.getSelectedIndex() < 0
                    || toWarehouseCombo
                    .getSelectedIndex() < 0
                    || productCombo
                    .getSelectedIndex() < 0) {
                warn("Please select all fields!");
                return;
            }

            int fromIdx =
                    fromWarehouseCombo.getSelectedIndex();
            int toIdx =
                    toWarehouseCombo.getSelectedIndex();

            if (fromIdx == toIdx) {
                warn("From and To cannot be the same!");
                return;
            }

            int qty = Integer.parseInt(
                    transferQtyField.getText().trim());
            if (qty <= 0) {
                warn("Quantity must be positive!");
                return;
            }

            Warehouse from = warehouses.get(fromIdx);
            Warehouse to   = warehouses.get(toIdx);
            Product prod   = products.get(
                    productCombo.getSelectedIndex());

            StockTransfer transfer = new StockTransfer(
                    from.getId(), to.getId(),
                    prod.getId(), qty,
                    currentUser.getId(),
                    "Manual transfer by "
                    + currentUser.getFullName());

            boolean ok =
                    warehouseDAO.transferStock(transfer);
            if (ok) {
                transferQtyField.setText("");
                refreshData();
                UITheme.showSuccess(this,
                        "Transfer successful!\n" + qty + " units of "
                                + prod.getName() + "\nFrom: " + from.getName()
                                + "\nTo: " + to.getName(), "Success");
            } else {
                warn("Transfer failed! "
                        + "Check stock levels.");
            }
        } catch (NumberFormatException ex) {
            warn("Please enter a valid quantity!");
        }
    }

    // ── Find Rebalancing ──────────────────────────────────────
    private void findRebalancingOpportunities() {
        rebalanceTableModel.setRowCount(0);
        SwingWorker<List<Map<String, Object>>, Void>
                worker = new SwingWorker<>() {
            protected List<Map<String, Object>>
                    doInBackground() {
                return rebalancingService
                        .findRebalancingOpportunities();
            }
            protected void done() {
                try {
                    List<Map<String, Object>> opps =
                            get();
                    for (Map<String, Object> opp :
                            opps) {
                        rebalanceTableModel.addRow(
                                new Object[]{
                            opp.get("productName"),
                            opp.get("fromWarehouseName"),
                            opp.get("fromStock"),
                            opp.get("toWarehouseName"),
                            opp.get("toStock"),
                            opp.get("suggestedQty"),
                            String.format("%.1f",
                                opp.get("balanceScore"))
                        });
                    }
                    if (opps.isEmpty()) {
                        UITheme.showInfo(WarehousePanel.this,
                                "Stock is balanced — no transfers needed!");
                    }
                } catch (Exception e) {
                    warn("Error: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    // ── Apply Rebalancing ─────────────────────────────────────
    private void applySelectedRebalancing() {
        int row = rebalanceTable.getSelectedRow();
        if (row < 0) {
            warn("Please select a row first!");
            return;
        }
        String product =
                (String) rebalanceTableModel
                .getValueAt(row, 0);
        String from    =
                (String) rebalanceTableModel
                .getValueAt(row, 1);
        String to      =
                (String) rebalanceTableModel
                .getValueAt(row, 3);
        int qty        =
                (int) rebalanceTableModel
                .getValueAt(row, 5);

        int confirm = UITheme.showConfirm(this,
                "Transfer " + qty + " units of " + product
                        + "\nFrom: " + from + "  →  To: " + to + "?",
                "Confirm Transfer");

        if (confirm == JOptionPane.YES_OPTION) {
            List<Map<String, Object>> opps =
                    rebalancingService
                    .findRebalancingOpportunities();
            if (row < opps.size()) {
                boolean ok =
                        rebalancingService
                        .applyRebalancing(
                                opps.get(row),
                                currentUser.getId());
                if (ok) {
                    UITheme.showSuccess(this, "Rebalancing applied!", "Success");
                    refreshData();
                    findRebalancingOpportunities();
                } else {
                    warn("Transfer failed!");
                }
            }
        }
    }

    // ── Update Stock Info ─────────────────────────────────────
    private void updateStockInfo(JLabel label) {
        try {
            int fromIdx =
                    fromWarehouseCombo.getSelectedIndex();
            int prodIdx =
                    productCombo.getSelectedIndex();
            if (fromIdx >= 0 && prodIdx >= 0
                    && fromIdx < warehouses.size()
                    && prodIdx < products.size()) {
                int stock =
                        warehouseDAO.getStockForProduct(
                        warehouses.get(fromIdx).getId(),
                        products.get(prodIdx).getId());
                label.setText(
                        "Current stock in "
                        + warehouses.get(fromIdx)
                        .getName()
                        + ": " + stock + " units");
                label.setForeground(stock > 0
                        ? UITheme.GREEN
                        : UITheme.RED);
            }
        } catch (Exception ignored) {}
    }

    // ── Refresh ───────────────────────────────────────────────
    public void refreshData() {
        warehouses = warehouseDAO.getAllWarehouses();
        if (fromWarehouseCombo != null) {
            fromWarehouseCombo.removeAllItems();
            toWarehouseCombo.removeAllItems();
            for (Warehouse w : warehouses) {
                fromWarehouseCombo.addItem(w.getName());
                toWarehouseCombo.addItem(w.getName());
            }
        }

        products = productDAO.getAllProducts();
        if (productCombo != null) {
            productCombo.removeAllItems();
            for (Product p : products)
                productCombo.addItem(p.getName());
        }

        // Stock overview — no auth filter
        if (stockTableModel != null) {
            stockTableModel.setRowCount(0);
            for (Object[] row :
                    warehouseDAO.getAllWarehouseStock()) {
                int qty     = (int) row[3];
                int reorder = (int) row[4];
                String status = qty <= reorder
                        ? "LOW" : "OK";
                stockTableModel.addRow(new Object[]{
                    row[0], row[1], row[2],
                    qty, reorder, status
                });
            }
        }

        // Transfer history
        if (transferTableModel != null) {
            transferTableModel.setRowCount(0);
            List<StockTransfer> transfers =
                    warehouseDAO.getTransferHistory(
                    currentUser.canAccessAllWarehouses()
                    ? 0
                    : currentUser.getWarehouseId());
            for (StockTransfer t : transfers) {
                transferTableModel.addRow(
                        new Object[]{
                    t.getProductName(),
                    t.getFromWarehouseName(),
                    t.getToWarehouseName(),
                    t.getQuantity(),
                    t.getReason(),
                    t.getTransferredByName(),
                    t.getTransferredAt() != null
                        ? t.getTransferredAt()
                        .toString()
                        .replace("T", " ")
                        : ""
                });
            }
        }
    }

    private void warn(String msg) {
        UITheme.showWarning(this, msg, "Warning");
    }

    // ── Renderers ─────────────────────────────────────────────
    class StatusRenderer
            extends DefaultTableCellRenderer {
        @Override
        public Component
                getTableCellRendererComponent(
                JTable t, Object v, boolean sel,
                boolean foc, int row, int col) {
            JLabel lbl = new JLabel();
            lbl.setOpaque(true);
            lbl.setFont(new Font("Segoe UI",
                    Font.BOLD, 12));
            lbl.setBorder(new EmptyBorder(
                    0, 14, 0, 14));
            lbl.setBackground(sel
                    ? UITheme.BG_ELEVATED
                    : (row % 2 == 0
                            ? UITheme.BG_SURFACE
                            : new Color(36, 48, 66)));
            if (v != null && v.toString()
                    .equalsIgnoreCase("LOW")) {
                lbl.setText("  LOW");
                lbl.setForeground(UITheme.RED);
            } else {
                lbl.setText("  OK");
                lbl.setForeground(UITheme.GREEN);
            }
            return lbl;
        }
    }
}