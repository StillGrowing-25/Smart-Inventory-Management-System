package com.inventory.gui;

import com.inventory.model.User;
import com.inventory.service.AuthService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class MainFrame extends JFrame {

    private User currentUser;

    public static final Color BG_BASE       = UITheme.BG_BASE;
    public static final Color BG_CARD       = UITheme.BG_SURFACE;
    public static final Color BG_HOVER      = UITheme.BG_ELEVATED;
    public static final Color ACCENT_BLUE   = UITheme.BLUE;
    public static final Color ACCENT_GREEN  = UITheme.GREEN;
    public static final Color ACCENT_RED    = UITheme.RED;
    public static final Color ACCENT_GOLD   = UITheme.AMBER;
    public static final Color ACCENT_TEAL   = UITheme.TEAL;
    public static final Color TEXT_PRIMARY  = UITheme.TEXT_PRIMARY;
    public static final Color TEXT_SECONDARY= UITheme.TEXT_SECONDARY;
    public static final Color BORDER_COLOR  = UITheme.BORDER;

    public MainFrame(User user) {
        this.currentUser = user;
        UITheme.applyGlobal();
        setTitle("Smart Inventory System");
        setSize(1350, 850);
        setMinimumSize(new Dimension(1100, 700));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setBackground(UITheme.BG_BASE);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG_BASE);

        // ── Top Bar ──────────────────────────────────────────
        JPanel topBar = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(UITheme.BORDER);
                g2.drawLine(0, getHeight() - 1,
                        getWidth(), getHeight() - 1);
                g2.dispose();
            }
        };
        topBar.setBackground(UITheme.BG_SURFACE);
        topBar.setPreferredSize(new Dimension(0, 58));
        topBar.setBorder(new EmptyBorder(0, 24, 0, 24));

        // Left side
        JPanel left = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        JLabel logo = new JLabel("*");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        logo.setForeground(UITheme.BLUE);

        JLabel appName = new JLabel(
                "Smart Inventory & Supply Chain Intelligence");
        appName.setFont(new Font("Segoe UI", Font.BOLD, 15));
        appName.setForeground(UITheme.TEXT_PRIMARY);

        left.add(logo);
        left.add(appName);

        // Right side
        JPanel right = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        // User chip
        JPanel chip = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 8, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_ELEVATED);
                g2.fill(new RoundRectangle2D.Float(
                        0, 0, getWidth(), getHeight(),
                        getHeight(), getHeight()));
                g2.dispose();
            }
        };
        chip.setOpaque(false);
        chip.setBorder(new EmptyBorder(5, 14, 5, 14));

        JLabel dot = new JLabel("●");
        dot.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        dot.setForeground(UITheme.GREEN);

        JLabel uName = new JLabel(currentUser.getFullName());
        uName.setFont(new Font("Segoe UI", Font.BOLD, 13));
        uName.setForeground(UITheme.TEXT_PRIMARY);

        JLabel roleLbl = UITheme.badge(
                currentUser.getRole(), UITheme.BLUE, Color.WHITE);

        chip.add(dot);
        chip.add(uName);
        chip.add(roleLbl);

        JButton logoutBtn = UITheme.outlineBtn(
                "Sign Out", UITheme.RED);
        logoutBtn.setPreferredSize(new Dimension(100, 34));
        logoutBtn.addActionListener(e -> handleLogout());

        right.add(chip);
        right.add(logoutBtn);

        topBar.add(left,  BorderLayout.WEST);
        topBar.add(right, BorderLayout.EAST);

        // ── Tabs ─────────────────────────────────────────────
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(UITheme.BG_BASE);
        tabs.setForeground(new Color(200, 215, 245));
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 13));

        tabs.addTab("  Dashboard  ",
                new DashboardPanel(currentUser));
        tabs.addTab("  Forecast  ",
                new ForecastPanel(currentUser));
        tabs.addTab("  Suppliers  ",
                new SupplierPanel(currentUser));
        tabs.addTab("  Dead Stock  ",
                new DeadStockPanel(currentUser));
        tabs.addTab("  Warehouses  ",
                new WarehousePanel(currentUser));
        tabs.addTab("  Reports  ",
                new ReportPanel(currentUser));

        root.add(topBar, BorderLayout.NORTH);
        root.add(tabs,   BorderLayout.CENTER);
        add(root);
    }

    private void handleLogout() {
        int c = JOptionPane.showConfirmDialog(this,
                "Sign out of Smart Inventory System?",
                "Sign Out",
                JOptionPane.YES_NO_OPTION);
        if (c == JOptionPane.YES_OPTION) {
            new AuthService().logout();
            dispose();
            new LoginFrame().setVisible(true);
        }
    }
}