package com.cacheinspector.ui.tabs;

import com.cacheinspector.model.SimulationResult;
import com.cacheinspector.model.SimulationStep;
import com.cacheinspector.ui.components.ComparisonChartView;
import com.cacheinspector.ui.components.StatCard;
import com.cacheinspector.ui.theme.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Tab panel providing side-by-side graphical comparison, statistical benchmarking,
 * and technical architectural analysis between LRU and Clock page replacement algorithms.
 */
public class ComparisonTabPanel extends JPanel {

    private SimulationResult lruResult;
    private SimulationResult clockResult;

    // Metric Cards
    private StatCard cardLruHits;
    private StatCard cardClockHits;
    private StatCard cardLruHitRatio;
    private StatCard cardClockHitRatio;
    private StatCard cardAdvantage;

    // Charts & Tables
    private ComparisonChartView chartView;
    private JTable comparisonTable;
    private DefaultTableModel tableModel;
    private JTextArea txtAnalysis;

    public ComparisonTabPanel() {
        setLayout(new BorderLayout(0, 10));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        initComponents();
    }

    private void initComponents() {
        // TOP: Stat Cards
        JPanel topContainer = new JPanel(new GridLayout(1, 5, 10, 0));
        topContainer.setOpaque(false);

        cardLruHits = new StatCard("LRU Hits", "0", "0.0% Hit Ratio", UITheme.ACCENT_PRIMARY);
        cardClockHits = new StatCard("Clock Hits", "0", "0.0% Hit Ratio", UITheme.ACCENT_PURPLE);
        cardLruHitRatio = new StatCard("LRU Hit %", "0.0%", "Hits / References", UITheme.COLOR_HIT);
        cardClockHitRatio = new StatCard("Clock Hit %", "0.0%", "Hits / References", UITheme.COLOR_HIT);
        cardAdvantage = new StatCard("Efficiency Delta", "TIE", "Comparative performance", UITheme.ACCENT_PRIMARY);

        topContainer.add(cardLruHits);
        topContainer.add(cardClockHits);
        topContainer.add(cardLruHitRatio);
        topContainer.add(cardClockHitRatio);
        topContainer.add(cardAdvantage);

        add(topContainer, BorderLayout.NORTH);

        // CENTER: Charts & Analysis
        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setOpaque(false);

        chartView = new ComparisonChartView();
        centerPanel.add(chartView, BorderLayout.NORTH);

        // Split bottom into Side-by-Side Table (left) and Architectural Analysis (right)
        JPanel bottomSplit = new JPanel(new GridLayout(1, 2, 10, 0));
        bottomSplit.setOpaque(false);

        // Side-by-Side JTable
        JPanel tableContainer = new JPanel(new BorderLayout(0, 6));
        tableContainer.setOpaque(false);
        JLabel lblTableTitle = new JLabel("Synchronized Step-by-Step Outcomes");
        lblTableTitle.setFont(UITheme.FONT_BODY_BOLD);
        lblTableTitle.setForeground(UITheme.TEXT_SECONDARY);
        tableContainer.add(lblTableTitle, BorderLayout.NORTH);

        String[] cols = {"Step", "Page", "LRU Result", "LRU Buffer", "Clock Result", "Clock Buffer", "Outcome Match"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        comparisonTable = new JTable(tableModel);
        comparisonTable.setRowHeight(24);
        comparisonTable.setFont(UITheme.FONT_BODY);
        comparisonTable.getTableHeader().setFont(UITheme.FONT_BODY_BOLD);
        comparisonTable.setShowGrid(true);
        comparisonTable.setGridColor(UITheme.BORDER_DEFAULT);

        comparisonTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                setHorizontalAlignment(col == 0 || col == 1 || col == 6 ? CENTER : LEFT);

                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? UITheme.BG_CARD : UITheme.BG_CARD_ALT);
                    c.setForeground(UITheme.TEXT_PRIMARY);

                    if ((col == 2 || col == 4) && value != null) {
                        String s = value.toString();
                        if (s.contains("HIT")) c.setForeground(UITheme.COLOR_HIT);
                        else if (s.contains("REPLACE")) c.setForeground(UITheme.COLOR_REPLACE);
                        else c.setForeground(UITheme.COLOR_MISS);
                    }

                    if (col == 6 && value != null) {
                        if ("Identical".equals(value.toString())) {
                            c.setForeground(UITheme.COLOR_HIT);
                        } else {
                            c.setForeground(UITheme.COLOR_MISS);
                        }
                    }
                } else {
                    c.setBackground(new Color(51, 65, 85));
                    c.setForeground(Color.WHITE);
                }
                return c;
            }
        });

        JScrollPane scrollTable = new JScrollPane(comparisonTable);
        scrollTable.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_DEFAULT, 1));
        tableContainer.add(scrollTable, BorderLayout.CENTER);
        bottomSplit.add(tableContainer);

        // Technical Analysis Card
        JPanel analysisCard = new JPanel(new BorderLayout(0, 6));
        analysisCard.setOpaque(false);
        JLabel lblAnalysisTitle = new JLabel("Database Buffer Pool Engineering Insights");
        lblAnalysisTitle.setFont(UITheme.FONT_BODY_BOLD);
        lblAnalysisTitle.setForeground(UITheme.TEXT_SECONDARY);
        analysisCard.add(lblAnalysisTitle, BorderLayout.NORTH);

        txtAnalysis = new JTextArea();
        txtAnalysis.setFont(UITheme.FONT_BODY);
        txtAnalysis.setForeground(UITheme.TEXT_PRIMARY);
        txtAnalysis.setBackground(UITheme.BG_CARD);
        txtAnalysis.setCaretColor(Color.WHITE);
        txtAnalysis.setEditable(false);
        txtAnalysis.setLineWrap(true);
        txtAnalysis.setWrapStyleWord(true);
        txtAnalysis.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        txtAnalysis.setText("Run a simulation on the dashboard to generate comprehensive comparative engineering insights.");

        JScrollPane scrollAnalysis = new JScrollPane(txtAnalysis);
        scrollAnalysis.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_DEFAULT, 1));
        analysisCard.add(scrollAnalysis, BorderLayout.CENTER);
        bottomSplit.add(analysisCard);

        centerPanel.add(bottomSplit, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);
    }

    public void updateComparison(SimulationResult lru, SimulationResult clock) {
        this.lruResult = lru;
        this.clockResult = clock;

        chartView.setResults(lru, clock);
        tableModel.setRowCount(0);

        if (lru == null || clock == null || lru.getSteps().isEmpty()) {
            cardLruHits.update("0", "0.0% Hit Ratio");
            cardClockHits.update("0", "0.0% Hit Ratio");
            cardLruHitRatio.update("0.0%", "Hits / References");
            cardClockHitRatio.update("0.0%", "Hits / References");
            cardAdvantage.update("TIE", "Comparative performance");
            txtAnalysis.setText("No simulation results available. Run a simulation from the dashboard.");
            return;
        }

        // Update Stat Cards
        cardLruHits.update(String.valueOf(lru.getTotalHits()), String.format("%d Misses, %d Evictions", lru.getTotalMisses(), lru.getTotalReplacements()));
        cardClockHits.update(String.valueOf(clock.getTotalHits()), String.format("%d Misses, %d Evictions", clock.getTotalMisses(), clock.getTotalReplacements()));
        cardLruHitRatio.update(String.format("%.1f%%", lru.getHitPercentage()), lru.getTotalHits() + " of " + lru.getTotalReferences());
        cardClockHitRatio.update(String.format("%.1f%%", clock.getHitPercentage()), clock.getTotalHits() + " of " + clock.getTotalReferences());

        double hitDiff = lru.getHitPercentage() - clock.getHitPercentage();
        if (Math.abs(hitDiff) < 0.001) {
            cardAdvantage.update("EQUAL (0.0%)", "Identical hit counts");
        } else if (hitDiff > 0) {
            cardAdvantage.update(String.format("LRU +%.1f%%", hitDiff), "LRU had " + (lru.getTotalHits() - clock.getTotalHits()) + " more hits");
        } else {
            cardAdvantage.update(String.format("CLOCK +%.1f%%", -hitDiff), "Clock had " + (clock.getTotalHits() - lru.getTotalHits()) + " more hits");
        }

        // Populate Synchronized Table
        int steps = Math.min(lru.getSteps().size(), clock.getSteps().size());
        for (int i = 0; i < steps; i++) {
            SimulationStep sLru = lru.getStep(i);
            SimulationStep sClock = clock.getStep(i);

            boolean match = sLru.getStatus() == sClock.getStatus();

            tableModel.addRow(new Object[]{
                    sLru.getStepNumber(),
                    "Page " + sLru.getPageReferenced(),
                    sLru.getStatus().getLabel(),
                    sLru.getBufferStateString(),
                    sClock.getStatus().getLabel(),
                    sClock.getBufferStateString(),
                    match ? "Identical" : "Diverged"
            });
        }

        // Generate Analysis Narrative
        StringBuilder sb = new StringBuilder();
        sb.append("📊 SIMULATION BENCHMARK SUMMARY\n");
        sb.append("-------------------------------------------------------------------------\n");
        sb.append(String.format("• Buffer Pool Frames: %d\n", lru.getBufferCapacity()));
        sb.append(String.format("• Total Page References: %d\n\n", lru.getTotalReferences()));

        sb.append("🎯 RESULTS COMPARISON:\n");
        sb.append(String.format("• LRU Algorithm:   %d Hits (%.1f%%), %d Misses (%.1f%%), %d Evictions\n",
                lru.getTotalHits(), lru.getHitPercentage(), lru.getTotalMisses(), lru.getMissPercentage(), lru.getTotalReplacements()));
        sb.append(String.format("• Clock Algorithm: %d Hits (%.1f%%), %d Misses (%.1f%%), %d Evictions\n\n",
                clock.getTotalHits(), clock.getHitPercentage(), clock.getTotalMisses(), clock.getMissPercentage(), clock.getTotalReplacements()));

        sb.append("💡 ARCHITECTURAL & SYSTEM TAKEAWAYS:\n");
        if (Math.abs(hitDiff) < 0.001) {
            sb.append("1. Performance Parity: Both algorithms yielded identical hit ratios on this workload. Clock successfully approximated LRU without incurring LRU's timestamp tracking overhead.\n\n");
        } else if (hitDiff > 0) {
            sb.append(String.format("1. Recency Sensitivity: LRU captured true recency better on this sequence, outperforming Clock by +%.1f%% hit ratio.\n\n", hitDiff));
        } else {
            sb.append(String.format("1. Second-Chance Advantage: Clock retained working-set pages more effectively via its reference bits, outperforming LRU by +%.1f%% hit ratio.\n\n", -hitDiff));
        }

        sb.append("2. Database Engine Design: In high-concurrency systems (like PostgreSQL and Linux Page Cache), true LRU requires locking the entire buffer list on EVERY page hit to re-link pointers. Clock only sets a single bit in the frame header, avoiding global write locks and dramatically boosting scalability.\n\n");
        sb.append("3. Memory Overhead: LRU requires storing 64-bit timestamps or 2 pointers per frame. Clock requires only 1 bit per frame plus a single shared circular pointer.");

        txtAnalysis.setText(sb.toString());
        txtAnalysis.setCaretPosition(0);
    }
}
