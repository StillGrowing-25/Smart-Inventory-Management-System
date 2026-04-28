package com.inventory.gui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class UITheme {

    // ── Color Palette ─────────────────────────────────────────
    public static final Color BG_BASE     = new Color(15, 23, 42);
    public static final Color BG_SURFACE  = new Color(30, 41, 59);
    public static final Color BG_ELEVATED = new Color(51, 65, 85);
    public static final Color BG_INPUT    = new Color(22, 30, 50);

    public static final Color BLUE        = new Color(59, 130, 246);
    public static final Color BLUE_DARK   = new Color(37, 99, 235);
    public static final Color BLUE_GLOW   = new Color(59, 130, 246, 40);
    public static final Color GREEN       = new Color(34, 197, 94);
    public static final Color GREEN_DARK  = new Color(22, 163, 74);
    public static final Color RED         = new Color(239, 68, 68);
    public static final Color RED_DARK    = new Color(220, 38, 38);
    public static final Color AMBER       = new Color(245, 158, 11);
    public static final Color PURPLE      = new Color(168, 85, 247);
    public static final Color TEAL        = new Color(20, 184, 166);

    public static final Color TEXT_PRIMARY   = new Color(241, 245, 249);
    public static final Color TEXT_SECONDARY = new Color(180, 200, 230);
    public static final Color TEXT_MUTED     = new Color(120, 140, 170);
    public static final Color BORDER         = new Color(51, 65, 85);
    public static final Color BORDER_FOCUS   = new Color(59, 130, 246);

    // ── Fonts ─────────────────────────────────────────────────
    public static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_SUBHEAD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_BODY    = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MONO    = new Font("Consolas", Font.PLAIN, 12);
    public static final Font FONT_NUMBER  = new Font("Segoe UI", Font.BOLD, 30);

    // ── Apply Global ──────────────────────────────────────────
    public static void applyGlobal() {
        UIManager.put("Panel.background",              BG_BASE);
        UIManager.put("OptionPane.background",              BG_SURFACE);
        UIManager.put("OptionPane.messageForeground",       TEXT_PRIMARY);
        UIManager.put("OptionPane.buttonBackground",        BG_ELEVATED);   // ADD THIS
        UIManager.put("OptionPane.buttonForeground",        TEXT_PRIMARY);  // ADD THIS
        UIManager.put("OptionPane.buttonPadding",           new Insets(6, 16, 6, 16)); // ADD THIS (optional, better look)
        UIManager.put("Button.background",                  BG_SURFACE);
        UIManager.put("Button.foreground",                  TEXT_PRIMARY);
        UIManager.put("OptionPane.buttonPadding",           new Insets(6, 16, 6, 16)); // ADD THIS (optional, better look)
        UIManager.put("Button.background",                  BG_SURFACE);
        UIManager.put("Button.foreground",                  TEXT_PRIMARY);
        UIManager.put("Label.foreground",              TEXT_PRIMARY);
        UIManager.put("TextField.background",          BG_INPUT);
        UIManager.put("TextField.foreground",          TEXT_PRIMARY);
        UIManager.put("TextField.caretForeground",     BLUE);
        UIManager.put("TextField.border",
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER),
                        new EmptyBorder(6, 10, 6, 10)));
        UIManager.put("PasswordField.background",      BG_INPUT);
        UIManager.put("PasswordField.foreground",      TEXT_PRIMARY);
        UIManager.put("PasswordField.caretForeground", BLUE);

        // ComboBox global defaults
        UIManager.put("ComboBox.background",           BG_INPUT);
        UIManager.put("ComboBox.foreground",           TEXT_PRIMARY);
        UIManager.put("ComboBox.selectionBackground",  BG_ELEVATED);
        UIManager.put("ComboBox.selectionForeground",  TEXT_PRIMARY);
        UIManager.put("ComboBox.buttonBackground",     BG_INPUT);
        UIManager.put("ComboBox.disabledBackground",   BG_INPUT);
        UIManager.put("ComboBox.disabledForeground",   TEXT_MUTED);

        // List popup
        UIManager.put("List.background",               BG_SURFACE);
        UIManager.put("List.foreground",               TEXT_PRIMARY);
        UIManager.put("List.selectionBackground",      BG_ELEVATED);
        UIManager.put("List.selectionForeground",      TEXT_PRIMARY);

        // ScrollPane
        UIManager.put("ScrollPane.background",         BG_BASE);
        UIManager.put("Viewport.background",           BG_SURFACE);

        // TabbedPane
        UIManager.put("TabbedPane.font",
                new Font("Segoe UI", Font.BOLD, 13));
        UIManager.put("TabbedPane.background",
                new Color(13, 17, 23));
        UIManager.put("TabbedPane.foreground",
                new Color(200, 215, 245));
        UIManager.put("TabbedPane.selectedForeground",
                new Color(88, 166, 255));
        UIManager.put("TabbedPane.selected",
                new Color(30, 41, 59));
        UIManager.put("TabbedPane.tabAreaBackground",
                new Color(13, 17, 23));
        UIManager.put("TabbedPane.unselectedBackground",
                new Color(13, 17, 23));
        UIManager.put("TabbedPane.contentAreaColor",
                new Color(15, 23, 42));
        UIManager.put("TabbedPane.borderHightlightColor",
                new Color(48, 54, 61));
        UIManager.put("TabbedPane.darkShadow",
                new Color(13, 17, 23));
        UIManager.put("TabbedPane.shadow",
                new Color(13, 17, 23));
        UIManager.put("TabbedPane.light",
                new Color(30, 41, 59));
        UIManager.put("TabbedPane.focus",
                new Color(59, 130, 246));
        UIManager.put("TabbedPane.tabInsets",
                new Insets(10, 20, 10, 20));
        UIManager.put("TabbedPane.selectedTabPadInsets",
                new Insets(10, 20, 10, 20));

        // Table
        UIManager.put("Table.background",              BG_SURFACE);
        UIManager.put("Table.foreground",              TEXT_PRIMARY);
        UIManager.put("Table.gridColor",               BORDER);
        UIManager.put("Table.selectionBackground",     BG_ELEVATED);
        UIManager.put("Table.selectionForeground",     TEXT_PRIMARY);
        UIManager.put("Table.focusCellBackground",     BG_ELEVATED);
        UIManager.put("Table.focusCellForeground",     TEXT_PRIMARY);
        UIManager.put("TableHeader.font",
                new Font("Segoe UI", Font.BOLD, 13));
        UIManager.put("TableHeader.background",
                new Color(13, 17, 23));
        UIManager.put("TableHeader.foreground",
                new Color(180, 200, 255));
        UIManager.put("TableHeader.cellBorder",
                BorderFactory.createMatteBorder(
                        0, 0, 1, 1, new Color(48, 54, 61)));

        // ScrollBar
        UIManager.put("ScrollBar.background",          BG_SURFACE);
        UIManager.put("ScrollBar.thumb",               BG_ELEVATED);
        UIManager.put("ScrollBar.track",               BG_SURFACE);
        UIManager.put("ScrollBar.thumbHighlight",      BG_ELEVATED);
        UIManager.put("ScrollBar.thumbShadow",         BG_ELEVATED);

        // FileChooser
        UIManager.put("FileChooser.background",        BG_SURFACE);
        UIManager.put("FileChooser.foreground",        TEXT_PRIMARY);
    }

    // ── Styled Dialog Helper ───────────────────────────────────
    public static void showSuccess(Component parent, String message, String title) {
        showStyled(parent, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showWarning(Component parent, String message, String title) {
        showStyled(parent, message, title, JOptionPane.WARNING_MESSAGE);
    }
    public static void showInfo(Component parent, String message) {
        showStyled(parent, message, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showError(Component parent, String message, String title) {
        showStyled(parent, message, title, JOptionPane.ERROR_MESSAGE);
    }

    public static int showConfirm(Component parent, String message, String title) {
        JOptionPane pane = new JOptionPane(message,
                JOptionPane.QUESTION_MESSAGE,
                JOptionPane.YES_NO_OPTION);
        JDialog dialog = pane.createDialog(parent, title);
        applyDialogStyle(pane);
        dialog.setVisible(true);
        Object val = pane.getValue();
        return (val instanceof Integer) ? (Integer) val : JOptionPane.CLOSED_OPTION;
    }

    private static void showStyled(Component parent, String message,
                                   String title, int type) {
        JOptionPane pane = new JOptionPane(message, type);
        JDialog dialog = pane.createDialog(parent, title);
        applyDialogStyle(pane);
        dialog.setVisible(true);
    }
    private static void applyDialogStyle(Container container) {
        container.setBackground(BG_SURFACE);
        for (Component c : container.getComponents()) {
            if (c instanceof JButton) {
                JButton btn = (JButton) c;
                btn.setBackground(BG_ELEVATED);
                btn.setForeground(TEXT_PRIMARY);
                btn.setOpaque(true);
                btn.setBorderPainted(false);
                btn.setFocusPainted(false);
            } else if (c instanceof JLabel) {
                ((JLabel) c).setForeground(TEXT_PRIMARY);
            } else if (c instanceof Container) {
                applyDialogStyle((Container) c); // recurse into child panels
            }
        }
    }
    // ── Card ──────────────────────────────────────────────────
    public static JPanel card(LayoutManager layout) {
        JPanel p = new JPanel(layout) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_SURFACE);
                g2.fill(new RoundRectangle2D.Float(
                        0, 0, getWidth(), getHeight(), 12, 12));
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(16, 18, 16, 18));
        return p;
    }

    // ── Stat Card ─────────────────────────────────────────────
    public static JPanel statCard(String icon, String label,
            String value, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 8)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_SURFACE);
                g2.fill(new RoundRectangle2D.Float(
                        0, 0, getWidth(), getHeight(), 12, 12));
                g2.setColor(accent);
                g2.fill(new RoundRectangle2D.Float(
                        0, 0, 4, getHeight(), 4, 4));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(18, 22, 18, 22));

        JLabel labelLbl = new JLabel(label.toUpperCase());
        labelLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        labelLbl.setForeground(TEXT_MUTED);

        JLabel valueLbl = new JLabel(value);
        valueLbl.setFont(FONT_NUMBER);
        valueLbl.setForeground(accent);
        valueLbl.setName("value");

        card.add(labelLbl, BorderLayout.NORTH);
        card.add(valueLbl, BorderLayout.CENTER);
        return card;
    }

    // ── Section Header ────────────────────────────────────────
    public static JPanel sectionHeader(String title) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                new EmptyBorder(0, 0, 10, 0)));
        JLabel lbl = new JLabel(title);
        lbl.setFont(FONT_SUBHEAD);
        lbl.setForeground(TEXT_PRIMARY);
        p.add(lbl, BorderLayout.WEST);
        return p;
    }

    // ── Primary Button ────────────────────────────────────────
    public static JButton primaryBtn(String text, Color bg) {
        JButton btn = new JButton(text) {
            private boolean hover = false;
            {
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) {
                        hover = true; repaint();
                    }
                    public void mouseExited(MouseEvent e) {
                        hover = false; repaint();
                    }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hover ? bg.darker() : bg);
                g2.fill(new RoundRectangle2D.Float(
                        0, 0, getWidth(), getHeight(), 8, 8));
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent()
                        - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(0, 36));
        return btn;
    }

    // ── Outline Button ────────────────────────────────────────
    public static JButton outlineBtn(String text, Color color) {
        JButton btn = new JButton(text) {
            private boolean hover = false;
            {
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) {
                        hover = true; repaint();
                    }
                    public void mouseExited(MouseEvent e) {
                        hover = false; repaint();
                    }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                if (hover) {
                    g2.setColor(new Color(color.getRed(),
                            color.getGreen(), color.getBlue(), 30));
                    g2.fill(new RoundRectangle2D.Float(
                            0, 0, getWidth(), getHeight(), 8, 8));
                }
                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(
                        1, 1, getWidth() - 2,
                        getHeight() - 2, 8, 8));
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent()
                        - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(color);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(0, 36));
        return btn;
    }

    // ── Input Field ───────────────────────────────────────────
    public static JTextField inputField(String placeholder) {
        JTextField f = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_INPUT);
                g2.fill(new RoundRectangle2D.Float(
                        0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        f.setFont(FONT_BODY);
        f.setForeground(TEXT_PRIMARY);
        f.setBackground(BG_INPUT);
        f.setCaretColor(BLUE);
        f.setOpaque(false);
        f.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(BORDER, 8),
                new EmptyBorder(7, 12, 7, 12)));
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                        new RoundedBorder(BORDER_FOCUS, 8),
                        new EmptyBorder(7, 12, 7, 12)));
            }
            public void focusLost(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                        new RoundedBorder(BORDER, 8),
                        new EmptyBorder(7, 12, 7, 12)));
            }
        });
        return f;
    }

    // ── ComboBox ──────────────────────────────────────────────
    public static <T> JComboBox<T> styledCombo(T[] items) {
        JComboBox<T> combo = new JComboBox<>(items);
        styleCombo(combo);
        return combo;
    }

    public static void styleCombo(JComboBox<?> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        combo.setBackground(new Color(22, 30, 50));
        combo.setForeground(new Color(241, 245, 249));
        combo.setOpaque(true);
        combo.setMaximumRowCount(8);

        // Fix editor
        Component edComp = combo.getEditor().getEditorComponent();
        if (edComp instanceof JTextField) {
            JTextField ed = (JTextField) edComp;
            ed.setBackground(new Color(22, 30, 50));
            ed.setForeground(new Color(241, 245, 249));
            ed.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            ed.setOpaque(true);
            ed.setBorder(new EmptyBorder(0, 0, 0, 0));
        }

        // Renderer
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value,
                    int index, boolean isSelected,
                    boolean cellHasFocus) {
                JLabel lbl = new JLabel();
                lbl.setOpaque(true);
                lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                lbl.setText(value != null ? value.toString() : "");
                lbl.setBackground(isSelected
                        ? new Color(51, 65, 85)
                        : new Color(30, 41, 59));
                lbl.setForeground(new Color(241, 245, 249));
                lbl.setBorder(new EmptyBorder(8, 12, 8, 12));
                return lbl;
            }
        });

        // Custom UI — paints background + text correctly
        combo.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton btn = new JButton("v");
                btn.setBackground(new Color(22, 30, 50));
                btn.setForeground(new Color(148, 163, 184));
                btn.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                btn.setBorder(new EmptyBorder(0, 4, 0, 8));
                btn.setFocusPainted(false);
                btn.setBorderPainted(false);
                btn.setContentAreaFilled(false);
                return btn;
            }

            @Override
            public void paintCurrentValueBackground(
                    Graphics g, Rectangle bounds,
                    boolean hasFocus) {
                g.setColor(new Color(22, 30, 50));
                g.fillRect(bounds.x, bounds.y,
                        bounds.width, bounds.height);
            }

            @Override
            public void paintCurrentValue(Graphics g,
                    Rectangle bounds, boolean hasFocus) {
                Object item = comboBox.getSelectedItem();
                if (item != null) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(
                            RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(241, 245, 249));
                    g2.setFont(new Font("Segoe UI",
                            Font.PLAIN, 13));
                    FontMetrics fm = g2.getFontMetrics();
                    int y = bounds.y + (bounds.height
                            + fm.getAscent()
                            - fm.getDescent()) / 2;
                    g2.drawString(item.toString(),
                            bounds.x + 10, y);
                    g2.dispose();
                }
            }
        });

        combo.setBorder(BorderFactory.createLineBorder(
                new Color(51, 65, 85)));
    }

    // ── Table ─────────────────────────────────────────────────
    public static void styleTable(JTable t) {
        t.setBackground(BG_SURFACE);
        t.setForeground(TEXT_PRIMARY);
        t.setFont(FONT_BODY);
        t.setRowHeight(36);
        t.setGridColor(BORDER);
        t.setShowVerticalLines(false);
        t.setShowHorizontalLines(true);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setSelectionBackground(BG_ELEVATED);
        t.setSelectionForeground(TEXT_PRIMARY);
        t.setFillsViewportHeight(true);

        t.getTableHeader().setOpaque(true);
        t.getTableHeader().setBackground(new Color(13, 17, 23));
        t.getTableHeader().setForeground(new Color(180, 200, 255));
        t.getTableHeader().setFont(
                new Font("Segoe UI", Font.BOLD, 13));
        t.getTableHeader().setPreferredSize(new Dimension(0, 40));
        t.getTableHeader().setBorder(
                BorderFactory.createMatteBorder(
                        0, 0, 2, 0, BLUE));

        t.setDefaultRenderer(Object.class,
                new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value,
                    boolean isSelected, boolean hasFocus,
                    int row, int column) {
                JLabel lbl = (JLabel) super
                        .getTableCellRendererComponent(
                                table, value, isSelected,
                                hasFocus, row, column);
                lbl.setBorder(new EmptyBorder(0, 14, 0, 14));
                lbl.setFont(FONT_BODY);
                if (isSelected) {
                    lbl.setBackground(BG_ELEVATED);
                    lbl.setForeground(TEXT_PRIMARY);
                } else {
                    lbl.setBackground(row % 2 == 0
                            ? BG_SURFACE
                            : new Color(36, 48, 66));
                    lbl.setForeground(TEXT_PRIMARY);
                }
                return lbl;
            }
        });
    }

    // ── ScrollPane ────────────────────────────────────────────
    public static JScrollPane scrollPane(JComponent c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBackground(BG_SURFACE);
        sp.getViewport().setBackground(BG_SURFACE);
        sp.setBorder(BorderFactory.createLineBorder(BORDER));
        sp.getVerticalScrollBar().setUI(new SlimScrollBarUI());
        sp.getVerticalScrollBar().setPreferredSize(
                new Dimension(6, 0));
        sp.getHorizontalScrollBar().setUI(new SlimScrollBarUI());
        sp.getHorizontalScrollBar().setPreferredSize(
                new Dimension(0, 6));
        return sp;
    }

    // ── Form Row ──────────────────────────────────────────────
    public static void addFormRow(JPanel panel,
            GridBagConstraints gbc, String labelText,
            JComponent field, int row) {
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(TEXT_SECONDARY);
        gbc.gridx   = 0;
        gbc.gridy   = row;
        gbc.weightx = 0.35;
        gbc.ipady   = 4;
        gbc.insets  = new Insets(4, 0, 4, 12);
        panel.add(lbl, gbc);
        gbc.gridx   = 1;
        gbc.weightx = 0.65;
        gbc.ipady   = 0;
        gbc.insets  = new Insets(4, 0, 4, 0);
        field.setPreferredSize(new Dimension(0, 36));
        panel.add(field, gbc);
    }

    // ── Badge ─────────────────────────────────────────────────
    public static JLabel badge(String text, Color bg, Color fg) {
        JLabel lbl = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(bg.getRed(),
                        bg.getGreen(), bg.getBlue(), 40));
                g2.fill(new RoundRectangle2D.Float(
                        0, 0, getWidth(), getHeight(),
                        getHeight(), getHeight()));
                g2.setColor(bg);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(
                        0.5f, 0.5f, getWidth() - 1,
                        getHeight() - 1,
                        getHeight(), getHeight()));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(bg);
        lbl.setOpaque(false);
        lbl.setBorder(new EmptyBorder(2, 8, 2, 8));
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        return lbl;
    }

    // ── Divider ───────────────────────────────────────────────
    public static JSeparator divider() {
        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER);
        sep.setBackground(BORDER);
        return sep;
    }

    // ── Rounded Border ────────────────────────────────────────
    public static class RoundedBorder implements Border {
        private final Color color;
        private final int radius;

        public RoundedBorder(Color c, int r) {
            color = c;
            radius = r;
        }

        public void paintBorder(Component c, Graphics g,
                int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1f));
            g2.draw(new RoundRectangle2D.Float(
                    x, y, w - 1, h - 1, radius, radius));
            g2.dispose();
        }

        public Insets getBorderInsets(Component c) {
            return new Insets(4, 8, 4, 8);
        }

        public boolean isBorderOpaque() {
            return false;
        }
    }

    // ── Slim ScrollBar ────────────────────────────────────────
    public static class SlimScrollBarUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            thumbColor          = BG_ELEVATED;
            trackColor          = BG_SURFACE;
            thumbHighlightColor = BG_ELEVATED;
        }

        @Override
        protected JButton createDecreaseButton(int o) {
            return zeroBtn();
        }

        @Override
        protected JButton createIncreaseButton(int o) {
            return zeroBtn();
        }

        private JButton zeroBtn() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            return b;
        }

        @Override
        protected void paintThumb(Graphics g,
                JComponent c, Rectangle r) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(thumbColor);
            g2.fill(new RoundRectangle2D.Float(
                    r.x + 1, r.y + 1,
                    r.width - 2, r.height - 2, 6, 6));
            g2.dispose();
        }

        @Override
        protected void paintTrack(Graphics g,
                                  JComponent c, Rectangle r) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(trackColor);
            g2.fillRect(r.x, r.y, r.width, r.height);
            g2.dispose();
        }
    }  // ← closes SlimScrollBarUI

    // ── Styled File Chooser ───────────────────────────────────
    public static int showStyledFileChooser(JFileChooser chooser,
                                            Component parent) {
        applyFileChooserStyle(chooser);
        return chooser.showSaveDialog(parent);
    }

    private static void applyFileChooserStyle(Container c) {
        c.setBackground(BG_SURFACE);
        if (c instanceof JButton) {
            JButton btn = (JButton) c;
            btn.setBackground(BG_ELEVATED);
            btn.setForeground(TEXT_PRIMARY);
            btn.setOpaque(true);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
        } else if (c instanceof JLabel) {
            ((JLabel) c).setForeground(TEXT_PRIMARY);
        } else if (c instanceof JTextField) {
            JTextField tf = (JTextField) c;
            tf.setBackground(BG_INPUT);
            tf.setForeground(TEXT_PRIMARY);
            tf.setCaretColor(BLUE);
        } else if (c instanceof JComboBox) {
            styleCombo((JComboBox<?>) c);
        } else if (c instanceof JList) {
            JList<?> list = (JList<?>) c;
            list.setBackground(BG_BASE);
            list.setForeground(TEXT_PRIMARY);
            list.setSelectionBackground(BG_ELEVATED);
            list.setSelectionForeground(TEXT_PRIMARY);
        } else if (c instanceof JScrollPane) {
            c.setBackground(BG_BASE);
            ((JScrollPane) c).getViewport().setBackground(BG_BASE);
        }
        for (Component child : c.getComponents()) {
            if (child instanceof Container) {
                applyFileChooserStyle((Container) child);
            }
        }
    }
}  // ← closes UITheme
