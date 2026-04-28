package com.inventory;

import com.inventory.gui.LoginFrame;
import com.inventory.service.CSVDataLoader;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        System.out.println("Initializing data...");
        CSVDataLoader loader = new CSVDataLoader();
        loader.loadAllData();
        System.out.println("Data ready!");

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.out.println("Look and feel error: " + e.getMessage());
            }
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}