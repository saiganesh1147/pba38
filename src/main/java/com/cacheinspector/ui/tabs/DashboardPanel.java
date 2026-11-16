package com.cacheinspector.ui.tabs;

import com.cacheinspector.ui.components.ModernButton;
import com.cacheinspector.ui.theme.UITheme;
import com.cacheinspector.util.InputParser;

import javax.swing.*;
import java.awt.*;
import java.util.function.BiConsumer;

/**
 * Top Dashboard panel featuring buffer capacity adjustment,
 * reference string input, preset loaders, and simulation execution triggers.
 */
public class DashboardPanel extends JPanel {

    private final JSpinner spinnerCapacity;
    private final JTextField txtSequence;
    private final JComboBox<PresetOption> comboPresets;
    private final ModernButton btnRun;
    private final ModernButton btnReset;
    private final ModernButton btnRandom;
    private final JLabel lblValidationMsg;

    private record PresetOption(String name, int capacity, String sequence) {
        @Override
        public String toString() {
            return name;
        }
    }

    public DashboardPanel(BiConsumer<Integer, String> onRunSimulation, Runnable onReset) {
        setLayout(new BorderLayout(0, 10));
        setOpaque(false);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_DEFAULT, 1, true),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        // 1. Header Row
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Buffer Pool Configuration & Workload Input");
        lblTitle.setFont(UITheme.FONT_SECTION);
        lblTitle.setForeground(UITheme.TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Simulate and compare database memory buffer pool frame eviction policies.");
        lblSub.setFont(UITheme.FONT_SUBTITLE);
        lblSub.setForeground(UITheme.TEXT_SECONDARY);

        headerPanel.add(lblTitle, BorderLayout.NORTH);
        headerPanel.add(lblSub, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // 2. Control Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Initialize form fields first
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(3, 1, 10, 1);
        spinnerCapacity = new JSpinner(spinnerModel);
        spinnerCapacity.setFont(UITheme.FONT_BODY_BOLD);
        spinnerCapacity.setPreferredSize(new Dimension(80, 32));

        txtSequence = new JTextField("1, 2, 3, 1, 4, 5, 2, 1, 2, 3, 4, 5");
        txtSequence.setFont(UITheme.FONT_MONO);
        txtSequence.setBackground(UITheme.BG_INPUT);
        txtSequence.setForeground(UITheme.TEXT_PRIMARY);
        txtSequence.setCaretColor(Color.WHITE);
        txtSequence.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_DEFAULT, 1),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));

        // Row 0: Presets
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        JLabel lblPresets = new JLabel("Workload Preset:");
        lblPresets.setFont(UITheme.FONT_BODY_BOLD);
        lblPresets.setForeground(UITheme.TEXT_SECONDARY);
        formPanel.add(lblPresets, gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0; gbc.gridwidth = 3;
        PresetOption[] presets = new PresetOption[]{
                new PresetOption("Sample Benchmark (3 frames: 1, 2, 3, 1, 4, 5, 2, 1, 2, 3, 4, 5)", 3, "1, 2, 3, 1, 4, 5, 2, 1, 2, 3, 4, 5"),
                new PresetOption("High Locality (4 frames: 1, 2, 1, 3, 1, 2, 4, 1, 2, 1, 3, 4)", 4, "1, 2, 1, 3, 1, 2, 4, 1, 2, 1, 3, 4"),
                new PresetOption("Sequential Scan (3 frames: 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)", 3, "1, 2, 3, 4, 5, 6, 7, 8, 9, 10"),
                new PresetOption("Cyclic Working Set (3 frames: 1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3, 4)", 3, "1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3, 4"),
                new PresetOption("Belady's Anomaly Test (3 frames: 1, 2, 3, 4, 1, 2, 5, 1, 2, 3, 4, 5)", 3, "1, 2, 3, 4, 1, 2, 5, 1, 2, 3, 4, 5")
        };
        comboPresets = new JComboBox<>(presets);
        comboPresets.setFont(UITheme.FONT_BODY);
        comboPresets.setBackground(UITheme.BG_CARD);
        comboPresets.setForeground(UITheme.TEXT_PRIMARY);
        comboPresets.addActionListener(e -> {
            PresetOption sel = (PresetOption) comboPresets.getSelectedItem();
            if (sel != null) {
                spinnerCapacity.setValue(sel.capacity());
                txtSequence.setText(sel.sequence());
                clearValidation();
            }
        });
        formPanel.add(comboPresets, gbc);

        // Row 1: Buffer Frames Capacity
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        JLabel lblCap = new JLabel("Buffer Pool Capacity:");
        lblCap.setFont(UITheme.FONT_BODY_BOLD);
        lblCap.setForeground(UITheme.TEXT_SECONDARY);
        formPanel.add(lblCap, gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0.2;
        formPanel.add(spinnerCapacity, gbc);

        // Page Reference String
        gbc.gridx = 2; gbc.gridy = 1; gbc.weightx = 0;
        JLabel lblSeq = new JLabel("Page Reference String:");
        lblSeq.setFont(UITheme.FONT_BODY_BOLD);
        lblSeq.setForeground(UITheme.TEXT_SECONDARY);
        formPanel.add(lblSeq, gbc);

        gbc.gridx = 3; gbc.gridy = 1; gbc.weightx = 1.0;
        formPanel.add(txtSequence, gbc);

        // Row 2: Action Buttons
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 4; gbc.weightx = 1.0;
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setOpaque(false);

        btnRandom = new ModernButton("🎲 Randomize", ModernButton.ButtonStyle.SECONDARY);
        btnRandom.addActionListener(e -> {
            txtSequence.setText(InputParser.generateRandomSequence(12, 6));
            clearValidation();
        });

        btnReset = new ModernButton("↺ Reset", ModernButton.ButtonStyle.DANGER);
        btnReset.addActionListener(e -> {
            comboPresets.setSelectedIndex(0);
            clearValidation();
            if (onReset != null) onReset.run();
        });

        btnRun = new ModernButton("▶ Run Simulation", ModernButton.ButtonStyle.PRIMARY);
        btnRun.addActionListener(e -> {
            try {
                int capacity = (Integer) spinnerCapacity.getValue();
                String rawSeq = txtSequence.getText();
                // Validate
                InputParser.parseSequence(rawSeq);
                clearValidation();
                if (onRunSimulation != null) {
                    onRunSimulation.accept(capacity, rawSeq);
                }
            } catch (IllegalArgumentException ex) {
                showValidation(ex.getMessage());
            }
        });

        actionPanel.add(btnRandom);
        actionPanel.add(btnReset);
        actionPanel.add(btnRun);
        formPanel.add(actionPanel, gbc);

        // Row 3: Validation Error Banner
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 4;
        lblValidationMsg = new JLabel(" ");
        lblValidationMsg.setFont(UITheme.FONT_BODY_BOLD);
        lblValidationMsg.setForeground(UITheme.COLOR_REPLACE);
        formPanel.add(lblValidationMsg, gbc);

        add(formPanel, BorderLayout.CENTER);
    }

    private void showValidation(String message) {
        lblValidationMsg.setText("⚠ " + message);
    }

    private void clearValidation() {
        lblValidationMsg.setText(" ");
    }

    public int getSelectedCapacity() {
        return (Integer) spinnerCapacity.getValue();
    }

    public String getSequenceInput() {
        return txtSequence.getText();
    }
}
