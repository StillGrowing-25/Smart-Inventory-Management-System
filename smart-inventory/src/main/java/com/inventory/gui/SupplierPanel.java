package com.inventory.gui;

import com.inventory.dao.SupplierDAO;
import com.inventory.model.Supplier;
import com.inventory.model.User;
import com.inventory.service.SupplierScoringService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import java.util.Map;

public class SupplierPanel extends JPanel {

    private User currentUser;
    private SupplierDAO supplierDAO =
            new SupplierDAO();
    private SupplierScoringService scoringService =
            new SupplierScoringService();

    private JTable supplierTable;
    private DefaultTableModel tableModel;
    private JLabel bestSupplierLabel;
    private JLabel avgScoreLabel;
    private JLabel criticalLabel;
    private JTextArea breakdownArea;

    public SupplierPanel(User user) {
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
                "Supplier Performance Scoring");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(UITheme.BLUE);

        JPanel btnPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);

        JButton addBtn = UITheme.primaryBtn(
                "+ Add Supplier", UITheme.GREEN);
        addBtn.setPreferredSize(new Dimension(130, 34));
        addBtn.addActionListener(
                e -> showAddSupplierDialog());

        JButton scoreBtn = UITheme.outlineBtn(
                "Recalculate Scores", UITheme.BLUE);
        scoreBtn.setPreferredSize(new Dimension(160, 34));
        scoreBtn.addActionListener(
                e -> recalculateScores());

        btnPanel.add(addBtn);
        btnPanel.add(scoreBtn);
        topBar.add(title,    BorderLayout.WEST);
        topBar.add(btnPanel, BorderLayout.EAST);

        // ── Summary Cards ─────────────────────────────────────
        JPanel summaryPanel = new JPanel(
                new GridLayout(1, 3, 14, 0));
        summaryPanel.setOpaque(false);
        summaryPanel.setBorder(
                new EmptyBorder(14, 18, 10, 18));

        JPanel bestCard     = buildStatCard(
                "Best Supplier",    "—", UITheme.GREEN);
        JPanel avgCard      = buildStatCard(
                "Average Score",    "0", UITheme.BLUE);
        JPanel criticalCard = buildStatCard(
                "Critical Suppliers","0", UITheme.RED);

        bestSupplierLabel = getCardValue(bestCard);
        avgScoreLabel     = getCardValue(avgCard);
        criticalLabel     = getCardValue(criticalCard);

        summaryPanel.add(bestCard);
        summaryPanel.add(avgCard);
        summaryPanel.add(criticalCard);

        JPanel headerPanel = new JPanel(
                new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.add(topBar,       BorderLayout.NORTH);
        headerPanel.add(summaryPanel, BorderLayout.SOUTH);

        // ── Table ─────────────────────────────────────────────
        JPanel centerPanel = new JPanel(
                new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.setBorder(
                new EmptyBorder(0, 18, 10, 18));

        String[] cols = {
            "Rank", "Supplier Name", "Score", "Grade",
            "Reliability%", "Lead Time(days)",
            "Defect Rate%", "Cost/Unit", "Status"
        };
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(
                    int r, int c) { return false; }
        };
        supplierTable = new JTable(tableModel);
        UITheme.styleTable(supplierTable);
        supplierTable.getColumnModel().getColumn(2)
                .setCellRenderer(new ScoreRenderer());
        supplierTable.getColumnModel().getColumn(8)
                .setCellRenderer(new StatusRenderer());
        supplierTable.getSelectionModel()
                .addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting())
                showBreakdown();
        });

        centerPanel.add(
                UITheme.scrollPane(supplierTable),
                BorderLayout.CENTER);

        // ── Breakdown ─────────────────────────────────────────
        JPanel bottomPanel = new JPanel(
                new BorderLayout(0, 8)) {
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
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(
                new EmptyBorder(14, 18, 14, 18));
        bottomPanel.setPreferredSize(
                new Dimension(0, 130));

        JLabel breakdownTitle = new JLabel(
                "Score Breakdown (click a supplier)");
        breakdownTitle.setFont(UITheme.FONT_SUBHEAD);
        breakdownTitle.setForeground(UITheme.BLUE);

        breakdownArea = new JTextArea(
                "Click on a supplier row to see "
                + "detailed score breakdown...");
        breakdownArea.setEditable(false);
        breakdownArea.setOpaque(false);
        breakdownArea.setBackground(UITheme.BG_SURFACE);
        breakdownArea.setForeground(
                UITheme.TEXT_SECONDARY);
        breakdownArea.setFont(
                new Font("Consolas", Font.PLAIN, 12));

        JScrollPane bsp = new JScrollPane(breakdownArea);
        bsp.setOpaque(false);
        bsp.getViewport().setOpaque(false);
        bsp.setBorder(
                BorderFactory.createEmptyBorder());

        JPanel outerBottom = new JPanel(
                new BorderLayout());
        outerBottom.setOpaque(false);
        outerBottom.setBorder(
                new EmptyBorder(0, 18, 18, 18));

        bottomPanel.add(breakdownTitle,
                BorderLayout.NORTH);
        bottomPanel.add(bsp, BorderLayout.CENTER);
        outerBottom.add(bottomPanel);

        add(headerPanel,  BorderLayout.NORTH);
        add(centerPanel,  BorderLayout.CENTER);
        add(outerBottom,  BorderLayout.SOUTH);
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

    // ── Recalculate ───────────────────────────────────────────
    private void recalculateScores() {
        SwingWorker<List<Supplier>, Void> worker =
                new SwingWorker<>() {
            protected List<Supplier> doInBackground() {
                return scoringService.scoreAllSuppliers();
            }
            protected void done() {
                refreshData();
                UITheme.showSuccess(SupplierPanel.this, "Scores recalculated!", "Success");

            }
        };
        worker.execute();
    }

    // ── Breakdown ─────────────────────────────────────────────
    private void showBreakdown() {
        int row = supplierTable.getSelectedRow();
        if (row < 0) return;
        String name = (String) tableModel
                .getValueAt(row, 1);
        for (Supplier s :
                supplierDAO.getAllSuppliers()) {
            if (s.getName().equals(name)) {
                Map<String, Double> bd =
                        scoringService.getScoreBreakdown(s);
                StringBuilder sb = new StringBuilder();
                sb.append("Supplier : ")
                  .append(s.getName()).append("\n");
                sb.append("─".repeat(32)).append("\n");
                for (Map.Entry<String, Double> e :
                        bd.entrySet()) {
                    sb.append(String.format(
                            "%-25s: %.2f points\n",
                            e.getKey(), e.getValue()));
                }
                sb.append("─".repeat(32)).append("\n");
                sb.append(String.format(
                        "%-25s: %.2f / 100\n",
                        "TOTAL SCORE",
                        s.getPerformanceScore()));
                breakdownArea.setText(sb.toString());
                break;
            }
        }
    }

    // ── Add Supplier Dialog ───────────────────────────────────
    private void showAddSupplierDialog() {
        JDialog dialog = new JDialog(
                (Frame) SwingUtilities
                .getWindowAncestor(this),
                "Add New Supplier", true);
        dialog.setSize(460, 520);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel root = new JPanel(
                new BorderLayout(0, 16));
        root.setBackground(UITheme.BG_BASE);
        root.setBorder(
                new EmptyBorder(24, 28, 24, 28));

        JLabel dlgTitle = new JLabel(
                "Add New Supplier");
        dlgTitle.setFont(new Font("Segoe UI",
                Font.BOLD, 15));
        dlgTitle.setForeground(UITheme.BLUE);
        dlgTitle.setBorder(
                new EmptyBorder(0, 0, 8, 0));

        // Form — label above each field
        JPanel form = new JPanel(
                new GridLayout(6, 1, 0, 8));
        form.setBackground(UITheme.BG_BASE);

        JTextField nameField     =
                makeDialogField();
        JTextField leadField     =
                makeDialogField();
        JTextField defectField   =
                makeDialogField();
        JTextField reliableField =
                makeDialogField();
        JTextField costField     =
                makeDialogField();
        JTextField locationField =
                makeDialogField();

        form.add(labeledField(
                "Supplier Name:",    nameField));
        form.add(labeledField(
                "Lead Time (days):", leadField));
        form.add(labeledField(
                "Defect Rate (%):",  defectField));
        form.add(labeledField(
                "Reliability (%):",  reliableField));
        form.add(labeledField(
                "Cost Per Unit:",    costField));
        form.add(labeledField(
                "Location:",         locationField));

        JButton saveBtn = UITheme.primaryBtn(
                "Save Supplier", UITheme.BLUE);
        saveBtn.setPreferredSize(
                new Dimension(0, 44));

        saveBtn.addActionListener(e -> {
            try {
                String nm =
                        nameField.getText().trim();
                if (nm.isEmpty()) {
                    UITheme.showWarning(dialog, "Supplier name required!", "Warning");

                    return;
                }
                Supplier s = new Supplier();
                s.setName(nm);
                s.setLeadTimeDays(Integer.parseInt(
                        leadField.getText().trim()));
                s.setDefectRate(Double.parseDouble(
                        defectField.getText().trim()));
                s.setReliabilityScore(
                        Double.parseDouble(
                        reliableField.getText()
                        .trim()));
                s.setCostPerUnit(Double.parseDouble(
                        costField.getText().trim()));
                s.setLocation(locationField
                        .getText().trim());
                s.setPerformanceScore(
                        scoringService
                        .calculateScore(s));

                if (supplierDAO.addSupplier(s)) {
                    UITheme.showSuccess(dialog, "Supplier added!", "Success");

                    dialog.dispose();
                    refreshData();
                }
            } catch (NumberFormatException ex) {
                UITheme.showWarning(dialog, "Please enter valid numbers!", "Warning");

            }
        });

        root.add(dlgTitle, BorderLayout.NORTH);
        root.add(form,     BorderLayout.CENTER);
        root.add(saveBtn,  BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.setVisible(true);
    }

    // Plain white-on-dark text field for dialogs
    private JTextField makeDialogField() {
        JTextField f = new JTextField();
        f.setFont(new Font("Segoe UI",
                Font.PLAIN, 13));
        f.setForeground(new Color(241, 245, 249));
        f.setBackground(new Color(30, 41, 59));
        f.setCaretColor(new Color(59, 130, 246));
        f.setOpaque(true);
        f.setEditable(true);
        f.setFocusable(true);
        f.setEnabled(true);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        new Color(51, 65, 85)),
                new EmptyBorder(8, 12, 8, 12)));
        f.setPreferredSize(new Dimension(0, 38));
        return f;
    }

    // Label above field in a small panel
    private JPanel labeledField(String labelText,
            JTextField field) {
        JPanel row = new JPanel(
                new BorderLayout(0, 4));
        row.setBackground(UITheme.BG_BASE);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI",
                Font.BOLD, 12));
        lbl.setForeground(UITheme.TEXT_SECONDARY);
        row.add(lbl,   BorderLayout.NORTH);
        row.add(field, BorderLayout.CENTER);
        return row;
    }

    // ── Refresh ───────────────────────────────────────────────
    public void refreshData() {
        List<Map<String, Object>> rankings =
                scoringService.getSupplierRankings();
        tableModel.setRowCount(0);

        double totalScore = 0;
        int critical = 0;
        String best = "—";

        for (Map<String, Object> r : rankings) {
            double score = (double) r.get("score");
            tableModel.addRow(new Object[]{
                r.get("rank"),
                r.get("name"),
                String.format("%.1f", score),
                r.get("grade"),
                String.format("%.1f",
                        r.get("reliability")),
                r.get("leadTime"),
                String.format("%.2f",
                        r.get("defectRate")),
                String.format("%.2f",
                        r.get("costPerUnit")),
                r.get("status")
            });
            totalScore += score;
            if (score < 50) critical++;
        }

        if (!rankings.isEmpty()) {
            best = rankings.get(0)
                    .get("name").toString();
            avgScoreLabel.setText(String.format(
                    "%.1f",
                    totalScore / rankings.size()));
        }

        bestSupplierLabel.setText(best);
        criticalLabel.setText(
                String.valueOf(critical));
    }

    // ── Renderers ─────────────────────────────────────────────
    class ScoreRenderer
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
            try {
                double score = Double.parseDouble(
                        value.toString());
                if (score >= 85)
                    setForeground(UITheme.GREEN);
                else if (score >= 60)
                    setForeground(UITheme.AMBER);
                else
                    setForeground(UITheme.RED);
                setFont(getFont()
                        .deriveFont(Font.BOLD));
            } catch (Exception ignored) {}
            return this;
        }
    }

    class StatusRenderer
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
            switch (value.toString()) {
                case "Excellent":
                    setForeground(UITheme.GREEN); break;
                case "Good":
                    setForeground(UITheme.TEAL);  break;
                case "Average":
                    setForeground(UITheme.AMBER); break;
                default:
                    setForeground(UITheme.RED);   break;
            }
            return this;
        }
    }
}