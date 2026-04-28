package com.inventory.gui;

import com.inventory.model.User;
import com.inventory.service.AuthService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class LoginFrame extends JFrame {

    private AuthService authService = new AuthService();
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel statusLabel;

    public LoginFrame() {
        UITheme.applyGlobal();
        setTitle("Smart Inventory — Login");
        setSize(480, 520);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setBackground(UITheme.BG_BASE);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(UITheme.BG_BASE);
        root.setBorder(new EmptyBorder(40, 50, 40, 50));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill      = GridBagConstraints.HORIZONTAL;
        gbc.weightx   = 1;
        gbc.insets    = new Insets(0, 0, 0, 0);

        // Logo area
        JPanel logoPanel = new JPanel(
                new FlowLayout(FlowLayout.CENTER, 8, 0));
        logoPanel.setOpaque(false);
        JLabel logo = new JLabel("⬡");
        logo.setFont(new Font("Arial", Font.PLAIN, 28));
        logo.setForeground(UITheme.BLUE);
        logoPanel.add(logo);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 8, 0);
        root.add(logoPanel, gbc);

        // Title
        JLabel title = new JLabel("Smart Inventory System",
                SwingConstants.CENTER);
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_PRIMARY);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 6, 0);
        root.add(title, gbc);

        // Subtitle
        JLabel sub = new JLabel(
                "Supply Chain Intelligence Platform",
                SwingConstants.CENTER);
        sub.setFont(UITheme.FONT_SMALL);
        sub.setForeground(UITheme.TEXT_MUTED);
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 32, 0);
        root.add(sub, gbc);

        // Card
        JPanel card = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_SURFACE);
                g2.fill(new RoundRectangle2D.Float(
                        0, 0, getWidth(), getHeight(), 16, 16));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(28, 28, 28, 28));

        GridBagConstraints c = new GridBagConstraints();
        c.fill    = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        // Username label
        JLabel uLbl = label("Username");
        c.gridy  = 0;
        c.insets = new Insets(0, 0, 6, 0);
        card.add(uLbl, c);

        // Username field
        usernameField = UITheme.inputField("Enter username");
        c.gridy  = 1;
        c.insets = new Insets(0, 0, 16, 0);
        card.add(usernameField, c);

        // Password label
        JLabel pLbl = label("Password");
        c.gridy  = 2;
        c.insets = new Insets(0, 0, 6, 0);
        card.add(pLbl, c);

        // Password field
        passwordField = new JPasswordField();
        passwordField.setFont(UITheme.FONT_BODY);
        passwordField.setForeground(UITheme.TEXT_PRIMARY);
        passwordField.setBackground(UITheme.BG_INPUT);
        passwordField.setCaretColor(UITheme.BLUE);
        passwordField.setOpaque(true);
        passwordField.setBorder(
                BorderFactory.createCompoundBorder(
                        new UITheme.RoundedBorder(
                                UITheme.BORDER, 8),
                        new EmptyBorder(7, 12, 7, 12)));
        c.gridy  = 3;
        c.insets = new Insets(0, 0, 8, 0);
        card.add(passwordField, c);

        // Status
        statusLabel = new JLabel(" ",
                SwingConstants.CENTER);
        statusLabel.setFont(UITheme.FONT_SMALL);
        statusLabel.setForeground(UITheme.RED);
        c.gridy  = 4;
        c.insets = new Insets(0, 0, 16, 0);
        card.add(statusLabel, c);

        // Login button
        JButton loginBtn = UITheme.primaryBtn(
                "Sign In", UITheme.BLUE);
        loginBtn.setPreferredSize(new Dimension(0, 42));
        loginBtn.addActionListener(e -> handleLogin());
        passwordField.addActionListener(e -> handleLogin());
        c.gridy  = 5;
        c.insets = new Insets(0, 0, 20, 0);
        card.add(loginBtn, c);

        // Hint
        JLabel hint = new JLabel(
                "Demo: admin / admin123  ·  staff1 / staff123",
                SwingConstants.CENTER);
        hint.setFont(UITheme.FONT_SMALL);
        hint.setForeground(UITheme.TEXT_MUTED);
        c.gridy  = 6;
        c.insets = new Insets(0, 0, 0, 0);
        card.add(hint, c);

        gbc.gridy  = 3;
        gbc.insets = new Insets(0, 0, 0, 0);
        root.add(card, gbc);

        add(root);
    }

    private void handleLogin() {
        String user = usernameField.getText().trim();
        String pass = new String(passwordField.getPassword());
        if (user.isEmpty() || pass.isEmpty()) {
            statusLabel.setText("Please fill in all fields.");
            return;
        }
        statusLabel.setText("Authenticating...");
        statusLabel.setForeground(UITheme.TEXT_MUTED);
        User u = authService.login(user, pass);
        if (u != null) {
            dispose();
            new MainFrame(u).setVisible(true);
        } else {
            statusLabel.setText(
                    "Invalid username or password.");
            statusLabel.setForeground(UITheme.RED);
            passwordField.setText("");
        }
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(UITheme.TEXT_SECONDARY);
        return l;
    }
}