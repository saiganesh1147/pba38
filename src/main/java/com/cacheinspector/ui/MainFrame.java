package com.cacheinspector.ui;

import com.cacheinspector.algorithm.ClockAlgorithm;
import com.cacheinspector.algorithm.LruAlgorithm;
import com.cacheinspector.model.AlgorithmType;
import com.cacheinspector.model.SimulationResult;
import com.cacheinspector.ui.tabs.AlgorithmTabPanel;
import com.cacheinspector.ui.tabs.ComparisonTabPanel;
import com.cacheinspector.ui.tabs.DashboardPanel;
import com.cacheinspector.ui.tabs.GuideTabPanel;
import com.cacheinspector.ui.theme.UITheme;
import com.cacheinspector.util.InputParser;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Main window for the Buffer Pool Management System - Cache Inspector.
 * Coordinates the dashboard controls, simulation execution, and tab navigation.
 */
public class MainFrame extends JFrame {

    private DashboardPanel dashboardPanel;
    private AlgorithmTabPanel lruTabPanel;
    private AlgorithmTabPanel clockTabPanel;
    private ComparisonTabPanel comparisonTabPanel;
    private GuideTabPanel guideTabPanel;

    private JTabbedPane tabbedPane;
    private JLabel lblStatusLeft;
    private JLabel lblStatusRight;

    private final LruAlgorithm lruAlgorithm = new LruAlgorithm();
    private final ClockAlgorithm clockAlgorithm = new ClockAlgorithm();

    public MainFrame() {
        setTitle("Buffer Pool Management System – Cache Inspector");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1024, 768));
        setPreferredSize(new Dimension(1280, 880));

        initUI();
        pack();
        setLocationRelativeTo(null);

        // Run default benchmark automatically on launch so the user immediately sees a live simulation
        SwingUtilities.invokeLater(this::runDefaultSimulation);
    }

    private void initUI() {
        JPanel rootPanel = new JPanel(new BorderLayout(0, 8));
        rootPanel.setBackground(UITheme.BG_CANVAS);
        rootPanel.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        // 1. Top Section: App Header + Dashboard Control Panel
        JPanel topContainer = new JPanel(new BorderLayout(0, 8));
        topContainer.setOpaque(false);

        topContainer.add(createAppHeader(), BorderLayout.NORTH);

        dashboardPanel = new DashboardPanel(this::handleRunSimulation, this::handleReset);
        topContainer.add(dashboardPanel, BorderLayout.CENTER);

        rootPanel.add(topContainer, BorderLayout.NORTH);

        // 2. Center Section: Tabbed Pane
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UITheme.FONT_BODY_BOLD);

        lruTabPanel = new AlgorithmTabPanel(AlgorithmType.LRU);
        clockTabPanel = new AlgorithmTabPanel(AlgorithmType.CLOCK);
        comparisonTabPanel = new ComparisonTabPanel();
        guideTabPanel = new GuideTabPanel();

        tabbedPane.addTab("LRU Algorithm", lruTabPanel);
        tabbedPane.addTab("Clock Algorithm (Second Chance)", clockTabPanel);
        tabbedPane.addTab("Comparison & Analytics", comparisonTabPanel);
        tabbedPane.addTab("Architecture & Guide", guideTabPanel);

        rootPanel.add(tabbedPane, BorderLayout.CENTER);

        // 3. Bottom Section: Status Bar
        rootPanel.add(createStatusBar(), BorderLayout.SOUTH);

        setContentPane(rootPanel);
    }

    private JPanel createAppHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(2, 4, 6, 4));

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);

        JLabel lblAppTitle = new JLabel("Buffer Pool Management System – Cache Inspector");
        lblAppTitle.setFont(UITheme.FONT_TITLE);
        lblAppTitle.setForeground(UITheme.TEXT_PRIMARY);

        JLabel lblAppSub = new JLabel("Database Memory Management & Page Eviction Policy Visualizer (LRU & Clock)");
        lblAppSub.setFont(UITheme.FONT_SUBTITLE);
        lblAppSub.setForeground(UITheme.ACCENT_PRIMARY);

        titleBox.add(lblAppTitle);
        titleBox.add(Box.createVerticalStrut(2));
        titleBox.add(lblAppSub);

        header.add(titleBox, BorderLayout.WEST);

        // Version badge / indicator
        JPanel badgePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 4));
        badgePanel.setOpaque(false);

        JLabel lblTag = new JLabel(" v1.0.0 • Java Swing ");
        lblTag.setFont(UITheme.FONT_BADGE);
        lblTag.setForeground(UITheme.TEXT_SECONDARY);
        lblTag.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_DEFAULT, 1, true),
                BorderFactory.createEmptyBorder(3, 8, 3, 8)
        ));
        badgePanel.add(lblTag);

        header.add(badgePanel, BorderLayout.EAST);

        return header;
    }

    private JPanel createStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_DEFAULT),
                BorderFactory.createEmptyBorder(6, 4, 2, 4)
        ));

        lblStatusLeft = new JLabel("Ready. Configure buffer frames and page reference string, then click 'Run Simulation'.");
        lblStatusLeft.setFont(UITheme.FONT_SUBTITLE);
        lblStatusLeft.setForeground(UITheme.TEXT_SECONDARY);

        lblStatusRight = new JLabel("Algorithms: LRU (Least Recently Used) & Clock (Second Chance)");
        lblStatusRight.setFont(UITheme.FONT_SUBTITLE);
        lblStatusRight.setForeground(UITheme.TEXT_MUTED);

        bar.add(lblStatusLeft, BorderLayout.WEST);
        bar.add(lblStatusRight, BorderLayout.EAST);

        return bar;
    }

    private void runDefaultSimulation() {
        handleRunSimulation(3, "1, 2, 3, 1, 4, 5, 2, 1, 2, 3, 4, 5");
    }

    private void handleRunSimulation(int capacity, String rawSequence) {
        try {
            List<Integer> sequence = InputParser.parseSequence(rawSequence);

            // Execute simulations for both algorithms independently
            SimulationResult lruResult = lruAlgorithm.simulate(capacity, sequence);
            SimulationResult clockResult = clockAlgorithm.simulate(capacity, sequence);

            // Load results into individual tabs
            lruTabPanel.loadSimulation(lruResult);
            clockTabPanel.loadSimulation(clockResult);
            comparisonTabPanel.updateComparison(lruResult, clockResult);

            lblStatusLeft.setText(String.format(
                    "Simulation completed successfully! Evaluated %d page references across %d buffer pool frames. LRU Hits: %d (%.1f%%) | Clock Hits: %d (%.1f%%)",
                    sequence.size(), capacity,
                    lruResult.getTotalHits(), lruResult.getHitPercentage(),
                    clockResult.getTotalHits(), clockResult.getHitPercentage()
            ));
            lblStatusLeft.setForeground(UITheme.COLOR_HIT);

        } catch (Exception ex) {
            lblStatusLeft.setText("Error executing simulation: " + ex.getMessage());
            lblStatusLeft.setForeground(UITheme.COLOR_REPLACE);
            JOptionPane.showMessageDialog(this,
                    "Simulation Error: " + ex.getMessage(),
                    "Input / Execution Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleReset() {
        lruTabPanel.resetView();
        clockTabPanel.resetView();
        comparisonTabPanel.updateComparison(null, null);

        lblStatusLeft.setText("Buffer pool state reset. Ready for next workload input.");
        lblStatusLeft.setForeground(UITheme.TEXT_SECONDARY);
    }
}
