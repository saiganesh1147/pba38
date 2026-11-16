package com.cacheinspector.ui.tabs;

import com.cacheinspector.model.*;
import com.cacheinspector.ui.components.*;
import com.cacheinspector.ui.theme.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Tab panel for visualizing a single page replacement algorithm (LRU or Clock).
 * Integrates summary cards, interactive memory frames, radial clock dial,
 * step narrative, and replacement history JTable.
 */
public class AlgorithmTabPanel extends JPanel {

    private final AlgorithmType algorithmType;
    private SimulationResult simulationResult;
    private int currentStepIndex = -1; // -1 = before start or reset

    // Stat Cards
    private StatCard cardHits;
    private StatCard cardMisses;
    private StatCard cardReplacements;
    private StatCard cardHitRatio;
    private StatCard cardMissRatio;

    // Visualizers
    private BufferFramesView bufferFramesView;
    private ClockRadialView clockRadialView; // For Clock algorithm
    private SequenceTapeView sequenceTapeView;

    // Narrative & Status
    private JLabel lblStepCounter;
    private JLabel lblNarrativeText;
    private JLabel lblStatusBadge;

    // Table
    private JTable historyTable;
    private DefaultTableModel tableModel;

    // Playback Timer
    private Timer playTimer;
    private boolean isPlaying = false;
    private ModernButton btnPlay;
    private ModernButton btnPrev;
    private ModernButton btnNext;
    private ModernButton btnFirst;
    private ModernButton btnLast;
    private JSlider speedSlider;

    public AlgorithmTabPanel(AlgorithmType algorithmType) {
        this.algorithmType = algorithmType;
        setLayout(new BorderLayout(0, 10));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        initTimer();
        initComponents();
    }

    private void initTimer() {
        playTimer = new Timer(750, e -> {
            if (simulationResult != null && currentStepIndex < simulationResult.getSteps().size() - 1) {
                stepForward();
            } else {
                pausePlayback();
            }
        });
    }

    private void initComponents() {
        // TOP SECTION: Stat Cards + Sequence Tape
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);

        // Stats Row
        JPanel statsRow = new JPanel(new GridLayout(1, 5, 10, 0));
        statsRow.setOpaque(false);

        cardHits = new StatCard("Page Hits", "0", "0.0% Hit Ratio", UITheme.COLOR_HIT);
        cardMisses = new StatCard("Page Misses", "0", "0.0% Miss Ratio", UITheme.COLOR_MISS);
        cardReplacements = new StatCard("Evictions", "0", "Buffer replacements", UITheme.COLOR_REPLACE);
        cardHitRatio = new StatCard("Hit Ratio", "0.0%", "Hits / Total", UITheme.ACCENT_PRIMARY);
        cardMissRatio = new StatCard("Miss Ratio", "0.0%", "Misses / Total", UITheme.ACCENT_PURPLE);

        statsRow.add(cardHits);
        statsRow.add(cardMisses);
        statsRow.add(cardReplacements);
        statsRow.add(cardHitRatio);
        statsRow.add(cardMissRatio);

        topContainer.add(statsRow);
        topContainer.add(Box.createVerticalStrut(10));

        // Sequence Tape View
        sequenceTapeView = new SequenceTapeView();
        topContainer.add(sequenceTapeView);
        topContainer.add(Box.createVerticalStrut(10));

        // Playback Control Bar
        topContainer.add(createPlaybackBar());

        add(topContainer, BorderLayout.NORTH);

        // CENTER SECTION: Visual Frame Display + Clock Radial + Narrative
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setOpaque(false);

        // Frame Visualizer Container
        JPanel vizContainer = new JPanel(new BorderLayout(10, 0));
        vizContainer.setOpaque(false);
        vizContainer.setBorder(UITheme.createCardBorder());

        bufferFramesView = new BufferFramesView(algorithmType);

        if (algorithmType == AlgorithmType.CLOCK) {
            clockRadialView = new ClockRadialView();

            JPanel clockSplit = new JPanel(new BorderLayout(10, 0));
            clockSplit.setOpaque(false);

            JPanel dialWrapper = new JPanel(new BorderLayout());
            dialWrapper.setOpaque(false);
            dialWrapper.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            dialWrapper.add(clockRadialView, BorderLayout.CENTER);

            clockSplit.add(dialWrapper, BorderLayout.WEST);
            clockSplit.add(bufferFramesView, BorderLayout.CENTER);

            vizContainer.add(clockSplit, BorderLayout.CENTER);
        } else {
            vizContainer.add(bufferFramesView, BorderLayout.CENTER);
        }

        // Narrative Explanation Card
        JPanel narrativeCard = new JPanel(new BorderLayout(10, 0));
        narrativeCard.setOpaque(false);
        narrativeCard.setBackground(UITheme.BG_CARD);
        narrativeCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_DEFAULT, 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        lblStatusBadge = new JLabel("READY");
        lblStatusBadge.setFont(UITheme.FONT_BADGE);
        lblStatusBadge.setForeground(UITheme.ACCENT_PRIMARY);
        lblStatusBadge.setPreferredSize(new Dimension(100, 24));

        lblNarrativeText = new JLabel("Click 'Run Simulation' on the dashboard or step through using the controls.");
        lblNarrativeText.setFont(UITheme.FONT_BODY);
        lblNarrativeText.setForeground(UITheme.TEXT_PRIMARY);

        narrativeCard.add(lblStatusBadge, BorderLayout.WEST);
        narrativeCard.add(lblNarrativeText, BorderLayout.CENTER);

        JPanel middleSection = new JPanel(new BorderLayout(0, 8));
        middleSection.setOpaque(false);
        middleSection.add(vizContainer, BorderLayout.CENTER);
        middleSection.add(narrativeCard, BorderLayout.SOUTH);

        centerPanel.add(middleSection, BorderLayout.NORTH);

        // BOTTOM SECTION: Replacement History JTable
        JPanel tablePanel = createTablePanel();
        centerPanel.add(tablePanel, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createPlaybackBar() {
        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setOpaque(false);
        bar.setBackground(UITheme.BG_CARD);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_DEFAULT, 1, true),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));

        // Left: Step Info
        lblStepCounter = new JLabel("Step: - / -");
        lblStepCounter.setFont(UITheme.FONT_BODY_BOLD);
        lblStepCounter.setForeground(UITheme.TEXT_PRIMARY);
        bar.add(lblStepCounter, BorderLayout.WEST);

        // Center: Navigation Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        btnPanel.setOpaque(false);

        btnFirst = new ModernButton("⏮ Start", ModernButton.ButtonStyle.SECONDARY);
        btnPrev = new ModernButton("◀ Prev Step", ModernButton.ButtonStyle.SECONDARY);
        btnPlay = new ModernButton("▶ Play", ModernButton.ButtonStyle.PRIMARY);
        btnNext = new ModernButton("Next Step ▶", ModernButton.ButtonStyle.SECONDARY);
        btnLast = new ModernButton("End ⏭", ModernButton.ButtonStyle.SECONDARY);

        btnFirst.addActionListener(e -> jumpToStart());
        btnPrev.addActionListener(e -> stepBackward());
        btnPlay.addActionListener(e -> togglePlay());
        btnNext.addActionListener(e -> stepForward());
        btnLast.addActionListener(e -> jumpToEnd());

        btnPanel.add(btnFirst);
        btnPanel.add(btnPrev);
        btnPanel.add(btnPlay);
        btnPanel.add(btnNext);
        btnPanel.add(btnLast);

        bar.add(btnPanel, BorderLayout.CENTER);

        // Right: Speed Slider
        JPanel speedPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        speedPanel.setOpaque(false);

        JLabel lblSpeed = new JLabel("Speed:");
        lblSpeed.setFont(UITheme.FONT_BODY);
        lblSpeed.setForeground(UITheme.TEXT_SECONDARY);

        speedSlider = new JSlider(150, 1500, 750);
        speedSlider.setPreferredSize(new Dimension(110, 24));
        speedSlider.setOpaque(false);
        speedSlider.setInverted(true); // Left = Fast (low delay), Right = Slow (high delay)
        speedSlider.addChangeListener(e -> playTimer.setDelay(speedSlider.getValue()));

        speedPanel.add(lblSpeed);
        speedPanel.add(speedSlider);

        bar.add(speedPanel, BorderLayout.EAST);

        return bar;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);

        JLabel lblTitle = new JLabel("Step-by-Step Replacement History & Buffer State Trace");
        lblTitle.setFont(UITheme.FONT_BODY_BOLD);
        lblTitle.setForeground(UITheme.TEXT_SECONDARY);
        panel.add(lblTitle, BorderLayout.NORTH);

        String[] columns = {
                "Step #",
                "Referenced Page",
                "Outcome",
                "Victim Page",
                "Target Frame",
                "Buffer Pool State",
                (algorithmType == AlgorithmType.CLOCK ? "Clock Hand / Swept" : "LRU Recency Order"),
                "Cumulative Hit %"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        historyTable = new JTable(tableModel);
        historyTable.setRowHeight(26);
        historyTable.setFont(UITheme.FONT_BODY);
        historyTable.getTableHeader().setFont(UITheme.FONT_BODY_BOLD);
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyTable.setShowGrid(true);
        historyTable.setGridColor(UITheme.BORDER_DEFAULT);

        // Custom Cell Renderer for Outcomes (Hit, Miss, Eviction)
        historyTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(column == 0 || column == 1 || column == 3 || column == 4 ? CENTER : LEFT);

                if (!isSelected) {
                    if (row % 2 == 0) {
                        c.setBackground(UITheme.BG_CARD);
                    } else {
                        c.setBackground(UITheme.BG_CARD_ALT);
                    }
                    c.setForeground(UITheme.TEXT_PRIMARY);

                    // Color code Outcome column
                    if (column == 2 && value != null) {
                        String s = value.toString();
                        if (s.contains("HIT")) {
                            c.setForeground(UITheme.COLOR_HIT);
                        } else if (s.contains("REPLACE") || s.contains("EVICT")) {
                            c.setForeground(UITheme.COLOR_REPLACE);
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

        // Click row to jump to step
        historyTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = historyTable.getSelectedRow();
                if (selectedRow >= 0 && selectedRow != currentStepIndex) {
                    setStep(selectedRow);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_DEFAULT, 1));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    public void loadSimulation(SimulationResult result) {
        pausePlayback();
        this.simulationResult = result;
        this.currentStepIndex = -1;

        // Populate JTable
        tableModel.setRowCount(0);
        if (result != null) {
            for (SimulationStep step : result.getSteps()) {
                String algoDetail;
                if (algorithmType == AlgorithmType.CLOCK) {
                    algoDetail = String.format("Hand at F#%d (swept: %s)",
                            step.getClockPointerAfter(),
                            step.getClockInspectedFrames().toString());
                } else {
                    algoDetail = "LRU tracking";
                }

                tableModel.addRow(new Object[]{
                        step.getStepNumber(),
                        "Page " + step.getPageReferenced(),
                        step.getStatus().getLabel(),
                        step.getVictimPage() == null ? "--" : "Page " + step.getVictimPage(),
                        "Frame #" + step.getTargetFrameIndex(),
                        step.getBufferStateString(),
                        algoDetail,
                        String.format("%.1f%%", step.getRunningHitRatio())
                });
            }

            // Update Final Stat Cards
            cardHits.update(String.valueOf(result.getTotalHits()), String.format("%.1f%% Hit Rate", result.getHitPercentage()));
            cardMisses.update(String.valueOf(result.getTotalMisses()), String.format("%.1f%% Miss Rate", result.getMissPercentage()));
            cardReplacements.update(String.valueOf(result.getTotalReplacements()), "Page evictions");
            cardHitRatio.update(String.format("%.1f%%", result.getHitPercentage()), result.getTotalHits() + " of " + result.getTotalReferences());
            cardMissRatio.update(String.format("%.1f%%", result.getMissPercentage()), result.getTotalMisses() + " of " + result.getTotalReferences());

            // Jump to first step
            if (!result.getSteps().isEmpty()) {
                setStep(0);
            }
        } else {
            resetView();
        }
    }

    public void setStep(int stepIndex) {
        if (simulationResult == null || simulationResult.getSteps().isEmpty()) {
            return;
        }

        if (stepIndex < 0) stepIndex = 0;
        if (stepIndex >= simulationResult.getSteps().size()) {
            stepIndex = simulationResult.getSteps().size() - 1;
        }

        this.currentStepIndex = stepIndex;
        SimulationStep step = simulationResult.getStep(stepIndex);

        // Update UI
        lblStepCounter.setText(String.format("Step: %d / %d  •  Referenced Page [%d]",
                step.getStepNumber(), simulationResult.getTotalReferences(), step.getPageReferenced()));

        lblNarrativeText.setText(step.getExplanation());

        if (step.isHit()) {
            lblStatusBadge.setText("● PAGE HIT");
            lblStatusBadge.setForeground(UITheme.COLOR_HIT);
        } else if (step.isReplacement()) {
            lblStatusBadge.setText("▲ REPLACED");
            lblStatusBadge.setForeground(UITheme.COLOR_REPLACE);
        } else {
            lblStatusBadge.setText("◆ CACHE MISS");
            lblStatusBadge.setForeground(UITheme.COLOR_MISS);
        }

        // Live stat cards at current step
        cardHits.update(String.valueOf(step.getCumulativeHits()), String.format("%.1f%% Hit Rate", step.getRunningHitRatio()));
        cardMisses.update(String.valueOf(step.getCumulativeMisses()), String.format("%.1f%% Miss Rate", step.getRunningMissRatio()));
        cardReplacements.update(String.valueOf(step.getCumulativeReplacements()), "Evictions so far");
        cardHitRatio.update(String.format("%.1f%%", step.getRunningHitRatio()), step.getCumulativeHits() + " / " + (step.getCumulativeHits() + step.getCumulativeMisses()));
        cardMissRatio.update(String.format("%.1f%%", step.getRunningMissRatio()), step.getCumulativeMisses() + " / " + (step.getCumulativeHits() + step.getCumulativeMisses()));

        // Update Memory Frames
        bufferFramesView.setFrames(simulationResult.getBufferCapacity(), step.getFramesSnapshot(), step);

        if (clockRadialView != null) {
            clockRadialView.updateState(simulationResult.getBufferCapacity(), step.getFramesSnapshot(), step);
        }

        // Update Sequence Tape
        sequenceTapeView.updateState(simulationResult, stepIndex);

        // Sync table selection without firing recursive listener
        if (historyTable.getSelectedRow() != stepIndex) {
            historyTable.setRowSelectionInterval(stepIndex, stepIndex);
            historyTable.scrollRectToVisible(historyTable.getCellRect(stepIndex, 0, true));
        }

        updateButtonStates();
    }

    public void stepForward() {
        if (simulationResult != null && currentStepIndex < simulationResult.getSteps().size() - 1) {
            setStep(currentStepIndex + 1);
        }
    }

    public void stepBackward() {
        if (simulationResult != null && currentStepIndex > 0) {
            setStep(currentStepIndex - 1);
        }
    }

    public void jumpToStart() {
        if (simulationResult != null && !simulationResult.getSteps().isEmpty()) {
            setStep(0);
        }
    }

    public void jumpToEnd() {
        if (simulationResult != null && !simulationResult.getSteps().isEmpty()) {
            setStep(simulationResult.getSteps().size() - 1);
        }
    }

    private void togglePlay() {
        if (isPlaying) {
            pausePlayback();
        } else {
            startPlayback();
        }
    }

    public void startPlayback() {
        if (simulationResult == null || simulationResult.getSteps().isEmpty()) return;
        if (currentStepIndex >= simulationResult.getSteps().size() - 1) {
            setStep(0);
        }
        isPlaying = true;
        btnPlay.setText("⏸ Pause");
        playTimer.start();
    }

    public void pausePlayback() {
        isPlaying = false;
        if (playTimer != null) playTimer.stop();
        if (btnPlay != null) btnPlay.setText("▶ Play");
    }

    private void updateButtonStates() {
        boolean hasData = simulationResult != null && !simulationResult.getSteps().isEmpty();
        btnFirst.setEnabled(hasData && currentStepIndex > 0);
        btnPrev.setEnabled(hasData && currentStepIndex > 0);
        btnNext.setEnabled(hasData && currentStepIndex < simulationResult.getSteps().size() - 1);
        btnLast.setEnabled(hasData && currentStepIndex < simulationResult.getSteps().size() - 1);
        btnPlay.setEnabled(hasData);
    }

    public void resetView() {
        pausePlayback();
        simulationResult = null;
        currentStepIndex = -1;
        tableModel.setRowCount(0);
        lblStepCounter.setText("Step: - / -");
        lblNarrativeText.setText("Buffer pool reset. Enter a sequence and click 'Run Simulation'.");
        lblStatusBadge.setText("IDLE");
        lblStatusBadge.setForeground(UITheme.TEXT_MUTED);

        cardHits.update("0", "0.0% Hit Ratio");
        cardMisses.update("0", "0.0% Miss Ratio");
        cardReplacements.update("0", "Buffer replacements");
        cardHitRatio.update("0.0%", "0 of 0");
        cardMissRatio.update("0.0%", "0 of 0");

        bufferFramesView.setFrames(3, null, null);
        if (clockRadialView != null) {
            clockRadialView.updateState(3, null, null);
        }
        sequenceTapeView.updateState(null, -1);
        updateButtonStates();
    }
}
