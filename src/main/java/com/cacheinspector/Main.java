package com.cacheinspector;

import com.cacheinspector.ui.MainFrame;
import com.cacheinspector.ui.theme.UITheme;

import javax.swing.*;

/**
 * Application entry point for Buffer Pool Management System – Cache Inspector.
 */
public class Main {
    public static void main(String[] args) {
        // Configure Look and Feel on start
        UITheme.setupLookAndFeel();

        // Launch UI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                MainFrame frame = new MainFrame();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Failed to launch application: " + e.getMessage(),
                        "Launch Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
