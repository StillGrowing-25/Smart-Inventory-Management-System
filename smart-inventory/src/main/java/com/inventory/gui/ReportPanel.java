package com.inventory.gui;

import com.inventory.dao.SupplierDAO;
import com.inventory.dao.WarehouseDAO;
import com.inventory.model.User;
import com.inventory.service.DeadStockService;
import com.inventory.service.RebalancingService;
import com.inventory.service.SupplierScoringService;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.DrawInterface;
import javax.swing.*;
import java.awt.*;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class ReportPanel extends JPanel {

    private User currentUser;
    private WarehouseDAO warehouseDAO         = new WarehouseDAO();
    private SupplierDAO supplierDAO           = new SupplierDAO();
    private SupplierScoringService scoring    = new SupplierScoringService();
    private DeadStockService deadStockService = new DeadStockService();
    private RebalancingService rebalancing    = new RebalancingService();

    private JTextArea previewArea;
    private JLabel statusLabel;

    // iText font aliases to avoid ambiguity with java.awt.Font
    private static com.itextpdf.text.Font pdfTitleFont() {
        return new com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA,
                18, com.itextpdf.text.Font.BOLD,
                new BaseColor(137, 180, 250));
    }

    private static com.itextpdf.text.Font pdfSectionFont() {
        return new com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA,
                13, com.itextpdf.text.Font.BOLD,
                new BaseColor(50, 150, 50));
    }

    private static com.itextpdf.text.Font pdfBodyFont() {
        return new com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA,
                10, com.itextpdf.text.Font.NORMAL,
                BaseColor.DARK_GRAY);
    }

    private static com.itextpdf.text.Font pdfHeaderFont() {
        return new com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA,
                10, com.itextpdf.text.Font.BOLD,
                BaseColor.WHITE);
    }

    public ReportPanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout());
        setBackground(new Color(30, 30, 46));
        buildUI();
    }

    private void buildUI() {
        // Top panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(49, 50, 68));
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("Report Generation");
        titleLabel.setFont(new java.awt.Font("Arial",
                java.awt.Font.BOLD, 16));
        titleLabel.setForeground(new Color(137, 180, 250));
        topPanel.add(titleLabel, BorderLayout.WEST);

        // Center panel
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(new Color(30, 30, 46));
        centerPanel.setBorder(
                BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Report options grid
        JPanel optionsPanel = new JPanel(new GridLayout(2, 3, 15, 15));
        optionsPanel.setBackground(new Color(30, 30, 46));
        optionsPanel.setPreferredSize(new Dimension(0, 200));

        optionsPanel.add(createReportCard(
                "📊 Full Inventory Report",
                "Complete stock levels across all warehouses",
                new Color(137, 180, 250),
                e -> generateFullReport()));

        optionsPanel.add(createReportCard(
                "🏭 Supplier Performance",
                "Rankings and scores for all suppliers",
                new Color(166, 227, 161),
                e -> generateSupplierReport()));

        optionsPanel.add(createReportCard(
                "⚠ Dead Stock Report",
                "Items with no movement and suggested discounts",
                new Color(243, 139, 168),
                e -> generateDeadStockReport()));

        optionsPanel.add(createReportCard(
                "⚖ Rebalancing Report",
                "Stock transfer recommendations",
                new Color(249, 226, 175),
                e -> generateRebalancingReport()));

        optionsPanel.add(createReportCard(
                "📈 Forecast Summary",
                "AI predictions for top products",
                new Color(148, 226, 213),
                e -> generateForecastSummary()));

        optionsPanel.add(createReportCard(
                "📄 Complete PDF Report",
                "All sections combined in one PDF file",
                new Color(203, 166, 247),
                e -> generateCompletePDF()));

        // Preview area
        JPanel previewPanel = new JPanel(new BorderLayout());
        previewPanel.setBackground(new Color(49, 50, 68));
        previewPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(15, 0, 0, 0),
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(
                                new Color(88, 91, 112)),
                        "Report Preview",
                        0, 0,
                        new java.awt.Font("Arial",
                                java.awt.Font.BOLD, 12),
                        new Color(137, 180, 250))));

        previewArea = new JTextArea();
        previewArea.setEditable(false);
        previewArea.setBackground(new Color(30, 30, 46));
        previewArea.setForeground(new Color(205, 214, 244));
        previewArea.setFont(new java.awt.Font("Monospaced",
                java.awt.Font.PLAIN, 12));
        previewArea.setText(
                "Select a report type above to preview...");
        previewArea.setBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane previewScroll = new JScrollPane(previewArea);
        previewScroll.getViewport().setBackground(
                new Color(30, 30, 46));
        previewScroll.setBorder(BorderFactory.createEmptyBorder());
        previewPanel.add(previewScroll, BorderLayout.CENTER);

        // Status label
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(new Color(166, 227, 161));
        statusLabel.setFont(new java.awt.Font("Arial",
                java.awt.Font.BOLD, 12));
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setBorder(
                BorderFactory.createEmptyBorder(5, 0, 5, 0));

        centerPanel.add(optionsPanel, BorderLayout.NORTH);
        centerPanel.add(previewPanel, BorderLayout.CENTER);
        centerPanel.add(statusLabel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    private void generateFullReport() {
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            protected String doInBackground() {
                StringBuilder sb = new StringBuilder();
                sb.append("INVENTORY REPORT\n");
                sb.append("Generated: ").append(LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern(
                                "yyyy-MM-dd HH:mm"))).append("\n");
                sb.append("Generated By: ").append(
                        currentUser.getFullName()).append("\n");
                sb.append("=".repeat(60)).append("\n\n");
                sb.append("WAREHOUSE STOCK OVERVIEW\n");
                sb.append("-".repeat(60)).append("\n");
                sb.append(String.format("%-20s %-20s %-10s %-10s\n",
                        "Warehouse", "Product", "Stock", "Status"));
                sb.append("-".repeat(60)).append("\n");

                List<Object[]> stockData =
                        warehouseDAO.getAllWarehouseStock();
                int totalStock = 0;
                int lowStock   = 0;

                for (Object[] row : stockData) {
                    int qty     = (int) row[3];
                    int reorder = (int) row[4];
                    String status = qty <= reorder ? "LOW" : "OK";
                    sb.append(String.format(
                            "%-20s %-20s %-10d %-10s\n",
                            row[0], row[1], qty, status));
                    totalStock += qty;
                    if (qty <= reorder) lowStock++;
                }
                sb.append("-".repeat(60)).append("\n");
                sb.append("Total Stock Units: ")
                        .append(totalStock).append("\n");
                sb.append("Low Stock Items: ")
                        .append(lowStock).append("\n");
                return sb.toString();
            }

            protected void done() {
                try {
                    previewArea.setText(get());
                    statusLabel.setText(
                            "✓ Inventory report generated!");
                } catch (Exception e) {
                    statusLabel.setText("Error: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void generateSupplierReport() {
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            protected String doInBackground() {
                StringBuilder sb = new StringBuilder();
                sb.append("SUPPLIER PERFORMANCE REPORT\n");
                sb.append("Generated: ").append(LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern(
                                "yyyy-MM-dd HH:mm"))).append("\n");
                sb.append("=".repeat(60)).append("\n\n");

                List<Map<String, Object>> rankings =
                        scoring.getSupplierRankings();
                sb.append(String.format(
                        "%-5s %-20s %-8s %-6s %-10s\n",
                        "Rank", "Supplier",
                        "Score", "Grade", "Status"));
                sb.append("-".repeat(60)).append("\n");

                for (Map<String, Object> r : rankings) {
                    sb.append(String.format(
                            "%-5s %-20s %-8s %-6s %-10s\n",
                            r.get("rank"), r.get("name"),
                            String.format("%.1f", r.get("score")),
                            r.get("grade"), r.get("status")));
                }
                sb.append("\nAlgorithm: Weighted KPI Score\n");
                sb.append("Weights: Reliability 40% | ");
                sb.append("Lead Time 30% | ");
                sb.append("Defect Rate 20% | Cost 10%\n");
                return sb.toString();
            }

            protected void done() {
                try {
                    previewArea.setText(get());
                    statusLabel.setText(
                            "✓ Supplier report generated!");
                } catch (Exception e) {
                    statusLabel.setText("Error: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void generateDeadStockReport() {
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            protected String doInBackground() {
                StringBuilder sb = new StringBuilder();
                sb.append("DEAD STOCK REPORT\n");
                sb.append("Generated: ").append(LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern(
                                "yyyy-MM-dd HH:mm"))).append("\n");
                sb.append("=".repeat(60)).append("\n\n");

                List<Map<String, Object>> deadStock =
                        deadStockService.detectDeadStock();
                Map<String, Object> summary =
                        deadStockService.getDeadStockSummary();

                sb.append("SUMMARY\n");
                sb.append("Total Dead Stock Items: ")
                        .append(summary.get("totalDeadStockItems"))
                        .append("\n");
                sb.append("Critical Items: ")
                        .append(summary.get("criticalItems")).append("\n");
                sb.append("Estimated Loss: $")
                        .append(summary.get("estimatedTotalLoss"))
                        .append("\n\n");

                sb.append("DETAILS\n");
                sb.append("-".repeat(60)).append("\n");
                sb.append(String.format(
                        "%-12s %-8s %-10s %-10s %-10s\n",
                        "Product", "Store", "Stock",
                        "Days Idle", "Discount%"));
                sb.append("-".repeat(60)).append("\n");

                for (Map<String, Object> item : deadStock) {
                    sb.append(String.format(
                            "%-12s %-8s %-10s %-10s %-10s\n",
                            item.get("productId"),
                            item.get("storeId"),
                            item.get("currentStock"),
                            item.get("daysSinceLastSale"),
                            item.get("suggestedDiscount") + "%"));
                }
                return sb.toString();
            }

            protected void done() {
                try {
                    previewArea.setText(get());
                    statusLabel.setText(
                            "✓ Dead stock report generated!");
                } catch (Exception e) {
                    statusLabel.setText("Error: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void generateRebalancingReport() {
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            protected String doInBackground() {
                StringBuilder sb = new StringBuilder();
                sb.append("REBALANCING RECOMMENDATIONS\n");
                sb.append("Generated: ").append(LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern(
                                "yyyy-MM-dd HH:mm"))).append("\n");
                sb.append("=".repeat(60)).append("\n\n");

                List<Map<String, Object>> opps =
                        rebalancing.findRebalancingOpportunities();
                Map<String, Object> summary =
                        rebalancing.getRebalancingSummary();

                sb.append("Total Opportunities: ")
                        .append(summary.get("totalOpportunities"))
                        .append("\n");
                sb.append("Total Units to Move: ")
                        .append(summary.get("totalUnitsToMove"))
                        .append("\n\n");

                sb.append("RECOMMENDATIONS\n");
                sb.append("-".repeat(60)).append("\n");

                for (Map<String, Object> opp : opps) {
                    sb.append("Product: ")
                            .append(opp.get("productName")).append("\n");
                    sb.append("  Transfer ")
                            .append(opp.get("suggestedQty"))
                            .append(" units from ")
                            .append(opp.get("fromWarehouseName"))
                            .append(" to ")
                            .append(opp.get("toWarehouseName"))
                            .append("\n\n");
                }
                return sb.toString();
            }

            protected void done() {
                try {
                    previewArea.setText(get());
                    statusLabel.setText(
                            "✓ Rebalancing report generated!");
                } catch (Exception e) {
                    statusLabel.setText("Error: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void generateForecastSummary() {
        previewArea.setText(
                "Forecast Summary\n\n" +
                        "Use the Forecast tab to run predictions\n" +
                        "for individual products.\n\n" +
                        "Then generate the Complete PDF for a full summary.");
        statusLabel.setText(
                "Use the Forecast tab for predictions.");
    }

    private void generateCompletePDF() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new java.io.File(
                "InventoryReport_" +
                        LocalDateTime.now().format(
                                DateTimeFormatter.ofPattern(
                                        "yyyyMMdd_HHmm")) + ".pdf"));

        int result = UITheme.showStyledFileChooser(fileChooser, this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        String filePath =
                fileChooser.getSelectedFile().getAbsolutePath();
        if (!filePath.endsWith(".pdf")) filePath += ".pdf";
        final String finalPath = filePath; // must stay here — used inside SwingWorker

        statusLabel.setText("Generating PDF...");

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            protected Void doInBackground() throws Exception {
                Document document = new Document(PageSize.A4);
                PdfWriter.getInstance(document,
                        new FileOutputStream(finalPath));
                document.open();

                // Title
                Paragraph title = new Paragraph(
                        "Smart Inventory & Supply Chain Report",
                        pdfTitleFont());
                title.setAlignment(Element.ALIGN_CENTER);
                title.setSpacingAfter(5);
                document.add(title);

                Paragraph subtitle = new Paragraph(
                        "Generated: " +
                                LocalDateTime.now().format(
                                        DateTimeFormatter.ofPattern(
                                                "yyyy-MM-dd HH:mm")) +
                                "  |  By: " +
                                currentUser.getFullName(),
                        pdfBodyFont());
                subtitle.setAlignment(Element.ALIGN_CENTER);
                subtitle.setSpacingAfter(15);
                document.add(subtitle);

                document.add(new Paragraph(
                        "─────────────────────────────────────────",
                        pdfBodyFont()));
                document.add(Chunk.NEWLINE);

                // Section 1: Stock
                document.add(new Paragraph(
                        "1. WAREHOUSE STOCK OVERVIEW",
                        pdfSectionFont()));
                document.add(Chunk.NEWLINE);

                PdfPTable stockTable = new PdfPTable(5);
                stockTable.setWidthPercentage(100);
                stockTable.setSpacingAfter(15);

                for (String h : new String[]{
                        "Warehouse", "Product",
                        "Category", "Stock", "Status"}) {
                    PdfPCell cell = new PdfPCell(
                            new Phrase(h, pdfHeaderFont()));
                    cell.setBackgroundColor(
                            new BaseColor(30, 30, 46));
                    cell.setPadding(6);
                    stockTable.addCell(cell);
                }

                for (Object[] row :
                        warehouseDAO.getAllWarehouseStock()) {
                    int qty     = (int) row[3];
                    int reorder = (int) row[4];
                    String status = qty <= reorder ? "LOW" : "OK";
                    stockTable.addCell(new Phrase(
                            row[0].toString(), pdfBodyFont()));
                    stockTable.addCell(new Phrase(
                            row[1].toString(), pdfBodyFont()));
                    stockTable.addCell(new Phrase(
                            row[2].toString(), pdfBodyFont()));
                    stockTable.addCell(new Phrase(
                            String.valueOf(qty), pdfBodyFont()));
                    stockTable.addCell(new Phrase(
                            status, pdfBodyFont()));
                }
                document.add(stockTable);

                // Section 2: Suppliers
                document.add(new Paragraph(
                        "2. SUPPLIER PERFORMANCE RANKINGS",
                        pdfSectionFont()));
                document.add(Chunk.NEWLINE);

                PdfPTable supTable = new PdfPTable(5);
                supTable.setWidthPercentage(100);
                supTable.setSpacingAfter(15);

                for (String h : new String[]{
                        "Rank", "Supplier",
                        "Score", "Grade", "Status"}) {
                    PdfPCell cell = new PdfPCell(
                            new Phrase(h, pdfHeaderFont()));
                    cell.setBackgroundColor(
                            new BaseColor(30, 30, 46));
                    cell.setPadding(6);
                    supTable.addCell(cell);
                }

                for (Map<String, Object> r :
                        scoring.getSupplierRankings()) {
                    supTable.addCell(new Phrase(
                            r.get("rank").toString(),
                            pdfBodyFont()));
                    supTable.addCell(new Phrase(
                            r.get("name").toString(),
                            pdfBodyFont()));
                    supTable.addCell(new Phrase(
                            String.format("%.1f", r.get("score")),
                            pdfBodyFont()));
                    supTable.addCell(new Phrase(
                            r.get("grade").toString(),
                            pdfBodyFont()));
                    supTable.addCell(new Phrase(
                            r.get("status").toString(),
                            pdfBodyFont()));
                }
                document.add(supTable);

                // Section 3: Dead Stock
                document.add(new Paragraph(
                        "3. DEAD STOCK ANALYSIS",
                        pdfSectionFont()));
                document.add(Chunk.NEWLINE);

                Map<String, Object> dsSummary =
                        deadStockService.getDeadStockSummary();
                document.add(new Paragraph(
                        "Total Dead Stock Items: " +
                                dsSummary.get("totalDeadStockItems"),
                        pdfBodyFont()));
                document.add(new Paragraph(
                        "Critical Items: " +
                                dsSummary.get("criticalItems"),
                        pdfBodyFont()));
                document.add(new Paragraph(
                        "Estimated Loss: $" +
                                dsSummary.get("estimatedTotalLoss"),
                        pdfBodyFont()));
                document.add(Chunk.NEWLINE);

                // Section 4: Rebalancing
                document.add(new Paragraph(
                        "4. REBALANCING RECOMMENDATIONS",
                        pdfSectionFont()));
                document.add(Chunk.NEWLINE);

                Map<String, Object> rebSummary =
                        rebalancing.getRebalancingSummary();
                document.add(new Paragraph(
                        "Total Opportunities: " +
                                rebSummary.get("totalOpportunities"),
                        pdfBodyFont()));
                document.add(new Paragraph(
                        "Total Units to Move: " +
                                rebSummary.get("totalUnitsToMove"),
                        pdfBodyFont()));

                document.close();
                return null;
            }

            // finalPath is captured from the enclosing method scope above
            protected void done() {
                try {
                    get();
                    statusLabel.setText(
                            "✓ PDF saved: " + finalPath);
                    UITheme.showSuccess(
                            ReportPanel.this,
                            "PDF generated!\n" + finalPath,
                            "Success");
                } catch (Exception e) {
                    statusLabel.setText(
                            "PDF Error: " + e.getMessage());
                    UITheme.showError(
                            ReportPanel.this,
                            "Error: " + e.getMessage(),
                            "Error");
                }
            }
        };
        worker.execute();
    }

    private JPanel createReportCard(String title,
                                    String description, Color color,
                                    java.awt.event.ActionListener action) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(49, 50, 68));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 2),
                BorderFactory.createEmptyBorder(
                        12, 15, 12, 15)));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new java.awt.Font("Arial",
                java.awt.Font.BOLD, 12));
        titleLbl.setForeground(color);

        JLabel descLbl = new JLabel(
                "<html>" + description + "</html>");
        descLbl.setFont(new java.awt.Font("Arial",
                java.awt.Font.PLAIN, 11));
        descLbl.setForeground(new Color(166, 173, 200));

        JButton btn = new JButton("Generate");
        btn.setBackground(color);
        btn.setForeground(new Color(30, 30, 46));
        btn.setFont(new java.awt.Font("Arial",
                java.awt.Font.BOLD, 11));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(action);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(descLbl, BorderLayout.CENTER);
        card.add(btn, BorderLayout.SOUTH);
        return card;
    }
}