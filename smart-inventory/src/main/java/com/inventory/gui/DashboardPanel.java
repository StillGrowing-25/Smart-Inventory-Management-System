package com.inventory.gui;

import com.inventory.dao.InventoryLogDAO;
import com.inventory.dao.ProductDAO;
import com.inventory.dao.WarehouseDAO;
import com.inventory.model.InventoryLog;
import com.inventory.model.Product;
import com.inventory.model.User;
import com.inventory.model.Warehouse;
import com.inventory.service.AuthService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

public class DashboardPanel extends JPanel {

    private User currentUser;
    private WarehouseDAO warehouseDAO = new WarehouseDAO();
    private ProductDAO productDAO     = new ProductDAO();
    private InventoryLogDAO logDAO    = new InventoryLogDAO();

    private JTable stockTable;
    private DefaultTableModel stockModel;
    private JTable logTable;
    private DefaultTableModel logModel;
    private JComboBox<String> whCombo;
    private JComboBox<String> prodCombo;
    private JTextField qtyField;
    private JComboBox<String> reasonCombo;

    private JLabel totalProdVal;
    private JLabel lowStockVal;
    private JLabel totalStockVal;

    private List<Warehouse> warehouses;
    private List<Product> products;

    public DashboardPanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout(0, 0));
        setBackground(UITheme.BG_BASE);
        buildUI();
        refreshData();
    }

    private void buildUI() {
        // ── Stat Cards ────────────────────────────────────────
        JPanel cards = new JPanel(new GridLayout(1, 3, 14, 0));
        cards.setOpaque(false);
        cards.setBorder(new EmptyBorder(18, 18, 14, 18));

        JPanel c1 = buildStatCard("TOTAL PRODUCTS",   "0", UITheme.BLUE);
        JPanel c2 = buildStatCard("LOW STOCK ALERTS", "0", UITheme.RED);
        JPanel c3 = buildStatCard("TOTAL STOCK UNITS","0", UITheme.GREEN);

        totalProdVal  = getValueLabel(c1);
        lowStockVal   = getValueLabel(c2);
        totalStockVal = getValueLabel(c3);

        cards.add(c1);
        cards.add(c2);
        cards.add(c3);

        // ── Stock Table ───────────────────────────────────────
        JPanel tableSection = new JPanel(new BorderLayout(0, 10));
        tableSection.setOpaque(false);
        tableSection.setBorder(new EmptyBorder(0, 18, 10, 18));

        JPanel tblHeader = new JPanel(new BorderLayout());
        tblHeader.setOpaque(false);
        tblHeader.setBorder(new EmptyBorder(0, 0, 10, 0));

        JLabel tblTitle = new JLabel("Warehouse Stock Levels");
        tblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tblTitle.setForeground(UITheme.TEXT_PRIMARY);

        JButton refreshBtn = UITheme.outlineBtn("Refresh", UITheme.BLUE);
        refreshBtn.setPreferredSize(new Dimension(90, 30));
        refreshBtn.addActionListener(e -> refreshData());

        tblHeader.add(tblTitle,   BorderLayout.WEST);
        tblHeader.add(refreshBtn, BorderLayout.EAST);

        String[] cols = {"Warehouse", "Product", "Category",
                "Stock Qty", "Reorder Level", "Status"};
        stockModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        stockTable = new JTable(stockModel);
        UITheme.styleTable(stockTable);
        stockTable.getColumnModel().getColumn(5)
                .setCellRenderer(new StatusRenderer());
        stockTable.getColumnModel().getColumn(0).setPreferredWidth(130);
        stockTable.getColumnModel().getColumn(1).setPreferredWidth(200);

        tableSection.add(tblHeader,                    BorderLayout.NORTH);
        tableSection.add(UITheme.scrollPane(stockTable), BorderLayout.CENTER);

        // ── Bottom Row ────────────────────────────────────────
        JPanel bottom = new JPanel(new GridLayout(1, 2, 14, 0));
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(0, 18, 18, 18));
        bottom.setPreferredSize(new Dimension(0, 340));

        bottom.add(buildOpsCard());
        bottom.add(buildLedgerCard());

        add(cards,        BorderLayout.NORTH);
        add(tableSection, BorderLayout.CENTER);
        add(bottom,       BorderLayout.SOUTH);
    }

    // ── Stat Card ─────────────────────────────────────────────
    private JPanel buildStatCard(String label, String value, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_SURFACE);
                g2.fill(new RoundRectangle2D.Float(
                        0, 0, getWidth(), getHeight(), 12, 12));
                g2.setColor(accent);
                g2.fill(new RoundRectangle2D.Float(
                        0, 0, 4, getHeight(), 4, 4));
                GradientPaint gp = new GradientPaint(
                        0, 0,
                        new Color(accent.getRed(),
                                accent.getGreen(),
                                accent.getBlue(), 25),
                        0, getHeight() / 2, new Color(0, 0, 0, 0));
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(
                        0, 0, getWidth(), getHeight(), 12, 12));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(UITheme.TEXT_MUTED);

        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.BOLD, 34));
        val.setForeground(accent);
        val.setName("statval");

        card.add(lbl, BorderLayout.NORTH);
        card.add(val, BorderLayout.CENTER);
        return card;
    }

    private JLabel getValueLabel(JPanel card) {
        for (Component c : card.getComponents()) {
            if (c instanceof JLabel &&
                    "statval".equals(((JLabel) c).getName()))
                return (JLabel) c;
        }
        return new JLabel("0");
    }

    // ── Ops Card ──────────────────────────────────────────────
    private JPanel buildOpsCard() {
        JPanel card = new JPanel(new BorderLayout(0, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_SURFACE);
                g2.fill(new RoundRectangle2D.Float(
                        0, 0, getWidth(), getHeight(), 12, 12));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(20, 22, 20, 22));

        // Title
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        titleRow.setBorder(new EmptyBorder(0, 0, 12, 0));
        JLabel title = new JLabel("Stock Operations");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(UITheme.TEXT_PRIMARY);
        JLabel sub = new JLabel("Add or remove inventory");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sub.setForeground(UITheme.TEXT_MUTED);
        titleRow.add(title, BorderLayout.WEST);
        titleRow.add(sub,   BorderLayout.EAST);

        // Init components
        whCombo     = new JComboBox<>();
        prodCombo   = new JComboBox<>();
        qtyField    = UITheme.inputField("Enter quantity");
        reasonCombo = new JComboBox<>(new String[]{
            "RESTOCK","SALE","DAMAGED","RETURNED","ADJUSTMENT"});
        UITheme.styleCombo(whCombo);
        UITheme.styleCombo(prodCombo);
        UITheme.styleCombo(reasonCombo);

        // Form — using simple vertical BoxLayout for reliability
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);

        form.add(buildFieldRow("Warehouse", whCombo));
        form.add(Box.createVerticalStrut(8));
        form.add(buildFieldRow("Product",   prodCombo));
        form.add(Box.createVerticalStrut(8));
        form.add(buildFieldRow("Quantity",  qtyField));
        form.add(Box.createVerticalStrut(8));
        form.add(buildFieldRow("Reason",    reasonCombo));

        // Buttons
        JPanel btns = new JPanel(new GridLayout(1, 2, 10, 0));
        btns.setOpaque(false);
        btns.setBorder(new EmptyBorder(14, 0, 0, 0));
        JButton addBtn = UITheme.primaryBtn("+ Add Stock",    UITheme.GREEN);
        JButton remBtn = UITheme.primaryBtn("- Remove Stock", UITheme.RED);
        addBtn.setPreferredSize(new Dimension(0, 42));
        remBtn.setPreferredSize(new Dimension(0, 42));
        addBtn.addActionListener(e -> handleStock(true));
        remBtn.addActionListener(e -> handleStock(false));
        btns.add(addBtn);
        btns.add(remBtn);

        card.add(titleRow, BorderLayout.NORTH);
        card.add(form,     BorderLayout.CENTER);
        card.add(btns,     BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildFieldRow(String labelText, JComponent field) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(180, 200, 230));
        lbl.setPreferredSize(new Dimension(80, 36));

        field.setPreferredSize(new Dimension(0, 36));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        row.add(lbl,   BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);
        return row;
    }

    // ── Ledger Card ───────────────────────────────────────────
    private JPanel buildLedgerCard() {
        JPanel card = new JPanel(new BorderLayout(0, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_SURFACE);
                g2.fill(new RoundRectangle2D.Float(
                        0, 0, getWidth(), getHeight(), 12, 12));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(20, 22, 20, 22));

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        titleRow.setBorder(new EmptyBorder(0, 0, 12, 0));
        JLabel title = new JLabel("Inventory Ledger Log");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(UITheme.TEXT_PRIMARY);
        JLabel sub = new JLabel("Append-only audit trail");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sub.setForeground(UITheme.TEXT_MUTED);
        titleRow.add(title, BorderLayout.WEST);
        titleRow.add(sub,   BorderLayout.EAST);

        String[] cols = {"Product","Warehouse","Change","Reason","By","Time"};
        logModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        logTable = new JTable(logModel);
        UITheme.styleTable(logTable);
        logTable.getColumnModel().getColumn(2)
                .setCellRenderer(new ChangeRenderer());

        card.add(titleRow,                       BorderLayout.NORTH);
        card.add(UITheme.scrollPane(logTable),   BorderLayout.CENTER);
        return card;
    }

    // ── Stock Handler ─────────────────────────────────────────
    private void handleStock(boolean isAdd) {
        try {
            if (whCombo.getSelectedIndex() < 0 ||
                prodCombo.getSelectedIndex() < 0) {
                warn("Please select a warehouse and product.");
                return;
            }
            int qty = Integer.parseInt(qtyField.getText().trim());
            if (qty <= 0) { warn("Quantity must be > 0."); return; }

            Warehouse wh  = warehouses.get(whCombo.getSelectedIndex());
            Product prod  = products.get(prodCombo.getSelectedIndex());
            String reason = (String) reasonCombo.getSelectedItem();

            int cur = warehouseDAO.getStockForProduct(
                    wh.getId(), prod.getId());
            int nxt = isAdd ? cur + qty : cur - qty;

            if (!isAdd && nxt < 0) {
                warn("Insufficient stock. Current: " + cur);
                return;
            }

            warehouseDAO.updateStock(wh.getId(), prod.getId(), nxt);

            com.inventory.model.InventoryLog log =
                    new com.inventory.model.InventoryLog(
                            prod.getId(), wh.getId(),
                            isAdd ? qty : -qty,
                            reason, currentUser.getId());
            logDAO.addLog(log);
            qtyField.setText("");
            refreshData();

            UITheme.showSuccess(this,
                    (isAdd ? "Added " : "Removed ") + qty
                            + " units of " + prod.getName()
                            + "\nNew stock: " + nxt,
                    "Success");
        } catch (NumberFormatException ex) {
            warn("Please enter a valid number.");
        }
    }

    // ── Refresh ───────────────────────────────────────────────
    public void refreshData() {
        warehouses = warehouseDAO.getAllWarehouses();
        whCombo.removeAllItems();
        for (Warehouse w : warehouses)
            if (AuthService.canAccessWarehouse(w.getId()))
                whCombo.addItem(w.getName());

        products = productDAO.getAllProducts();
        prodCombo.removeAllItems();
        for (Product p : products)
            prodCombo.addItem(p.getName());

        stockModel.setRowCount(0);
        int total = 0, low = 0;
        for (Object[] row : warehouseDAO.getAllWarehouseStock()) {
            int qty     = (int) row[3];
            int reorder = (int) row[4];
            String st   = qty <= reorder ? "LOW" : "OK";
            if (AuthService.canAccessWarehouse(whId((String) row[0]))) {
                stockModel.addRow(new Object[]{
                    row[0], row[1], row[2], qty, reorder, st});
                total += qty;
                if (qty <= reorder) low++;
            }
        }
        totalProdVal.setText(String.valueOf(products.size()));
        lowStockVal.setText(String.valueOf(low));
        totalStockVal.setText(String.valueOf(total));

        logModel.setRowCount(0);
        List<InventoryLog> logs = currentUser.canAccessAllWarehouses()
                ? logDAO.getRecentLogs(25)
                : logDAO.getLogsByWarehouse(
                        currentUser.getWarehouseId(), 25);
        for (InventoryLog lg : logs) {
            logModel.addRow(new Object[]{
                lg.getProductName(),
                lg.getWarehouseName(),
                (lg.getChangeQty() > 0 ? "+" : "") + lg.getChangeQty(),
                lg.getReason(),
                lg.getPerformedByName(),
                lg.getLoggedAt() != null
                    ? lg.getLoggedAt().toString()
                            .replace("T"," ").substring(0,16)
                    : ""
            });
        }
    }

    private int whId(String name) {
        if (warehouses == null) return -1;
        for (Warehouse w : warehouses)
            if (w.getName().equals(name)) return w.getId();
        return -1;
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg,
                "Warning", JOptionPane.WARNING_MESSAGE);
    }

    // ── Renderers ─────────────────────────────────────────────
    static class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel,
                boolean foc, int row, int col) {
            JLabel lbl = new JLabel();
            lbl.setOpaque(true);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lbl.setBorder(new EmptyBorder(0, 14, 0, 14));
            lbl.setBackground(sel ? UITheme.BG_ELEVATED
                    : (row % 2 == 0 ? UITheme.BG_SURFACE
                            : new Color(36, 48, 66)));
            if (v != null && v.toString().equalsIgnoreCase("LOW")) {
                lbl.setText("  LOW");
                lbl.setForeground(UITheme.RED);
            } else {
                lbl.setText("  OK");
                lbl.setForeground(UITheme.GREEN);
            }
            return lbl;
        }
    }

    static class ChangeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel,
                boolean foc, int row, int col) {
            JLabel lbl = new JLabel(v != null ? v.toString() : "");
            lbl.setOpaque(true);
            lbl.setFont(new Font("Consolas", Font.BOLD, 12));
            lbl.setBorder(new EmptyBorder(0, 14, 0, 14));
            lbl.setBackground(sel ? UITheme.BG_ELEVATED
                    : (row % 2 == 0 ? UITheme.BG_SURFACE
                            : new Color(36, 48, 66)));
            String s = v != null ? v.toString() : "";
            lbl.setForeground(s.startsWith("+")
                    ? UITheme.GREEN : UITheme.RED);
            return lbl;
        }
    }
}