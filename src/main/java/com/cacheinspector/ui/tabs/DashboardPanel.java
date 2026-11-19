package com.cacheinspector.ui.tabs;

import com.cacheinspector.ui.components.ModernButton;
import com.cacheinspector.ui.theme.UITheme;
import com.cacheinspector.util.InputParser;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;
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
    private final ModernButton btnLoadFile;
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

        btnLoadFile = new ModernButton("📂 Load File...", ModernButton.ButtonStyle.SECONDARY);
        btnLoadFile.setToolTipText("Load a real database trace file (.csv, .txt, .log) or map a real physical file to 4KB pages");
        btnLoadFile.addActionListener(e -> handleLoadFile());

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

        actionPanel.add(btnLoadFile);
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

    private void handleLoadFile() {
        JFileChooser fileChooser = new JFileChooser(new File("samples"));
        if (!fileChooser.getCurrentDirectory().exists()) {
            fileChooser.setCurrentDirectory(new File("."));
        }
        fileChooser.setDialogTitle("Select Real-Time Workload Trace or Physical Database File");

        int res = fileChooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                String name = selectedFile.getName().toLowerCase();
                List<Integer> seq;

                if (name.endsWith(".csv") || name.endsWith(".txt") || name.endsWith(".log")) {
                    // Ask user how they want to interpret the file
                    Object[] options = {"Load Workload Trace (Page IDs)", "Simulate 4KB File Blocks (Physical I/O)", "Cancel"};
                    int choice = JOptionPane.showOptionDialog(
                            this,
                            "How would you like to process '" + selectedFile.getName() + "'?\n\n" +
                                    "• Workload Trace: Read real page reference log entries.\n" +
                                    "• Physical File I/O: Divide the file into 4 KB database pages and simulate queries.",
                            "Real-Time File Ingestion Mode",
                            JOptionPane.YES_NO_CANCEL_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            options,
                            options[0]);

                    if (choice == 0) {
                        seq = InputParser.parseTraceFile(selectedFile);
                    } else if (choice == 1) {
                        Object[] patterns = {"Index Tree Traversal", "Sequential Table Scan", "Mixed Pareto (80/20)"};
                        String pat = (String) JOptionPane.showInputDialog(
                                this,
                                "Select simulated query workload pattern:",
                                "Workload Pattern",
                                JOptionPane.QUESTION_MESSAGE,
                                null,
                                patterns,
                                patterns[0]);
                        if (pat == null) return;
                        String patKey = pat.contains("Scan") ? "SCAN" : (pat.contains("Index") ? "INDEX" : "MIXED");
                        seq = InputParser.generateFileBlockWorkload(selectedFile, InputParser.DEFAULT_PAGE_SIZE_BYTES, patKey);
                    } else {
                        return;
                    }
                } else {
                    // Binary or arbitrary physical disk file
                    long kb = Math.max(1, selectedFile.length() / 1024);
                    int pages = (int) Math.ceil(selectedFile.length() / 4096.0);
                    seq = InputParser.generateFileBlockWorkload(selectedFile, InputParser.DEFAULT_PAGE_SIZE_BYTES, "MIXED");
                    JOptionPane.showMessageDialog(this,
                            String.format("Mapped physical file '%s' (%d KB) into %d database pages (4 KB blocks).",
                                    selectedFile.getName(), kb, pages),
                            "Physical File Mapped",
                            JOptionPane.INFORMATION_MESSAGE);
                }

                if (seq != null && !seq.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < seq.size(); i++) {
                        sb.append(seq.get(i));
                        if (i < seq.size() - 1) sb.append(", ");
                    }
                    txtSequence.setText(sb.toString());
                    clearValidation();
                    lblValidationMsg.setForeground(UITheme.COLOR_HIT);
                    lblValidationMsg.setText(String.format("✔ Loaded %d page references from '%s'", seq.size(), selectedFile.getName()));
                }
            } catch (Exception ex) {
                showValidation("File Read Error: " + ex.getMessage());
            }
        }
    }

    private void showValidation(String message) {
        lblValidationMsg.setForeground(UITheme.COLOR_REPLACE);
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
