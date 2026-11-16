package com.cacheinspector.ui.components;

import com.cacheinspector.model.AlgorithmType;
import com.cacheinspector.model.BufferFrame;
import com.cacheinspector.model.SimulationStep;
import com.cacheinspector.ui.theme.UITheme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Visualizer for physical memory frames residing within the database buffer pool.
 * Highlights page hits, cache misses, page evictions, LRU recency, and Clock reference bits.
 */
public class BufferFramesView extends JPanel {

    private final AlgorithmType algorithmType;
    private final List<BufferFrame> currentFrames = new ArrayList<>();
    private SimulationStep currentStep = null;
    private int capacity = 3;

    public BufferFramesView(AlgorithmType algorithmType) {
        this.algorithmType = algorithmType;
        setLayout(new FlowLayout(FlowLayout.CENTER, 14, 14));
        setOpaque(false);
        setFrames(3, null, null);
    }

    public void setFrames(int capacity, List<BufferFrame> frames, SimulationStep step) {
        this.capacity = capacity;
        this.currentFrames.clear();
        this.currentStep = step;

        if (frames != null) {
            for (BufferFrame f : frames) {
                this.currentFrames.add(f.clone());
            }
        } else {
            for (int i = 0; i < capacity; i++) {
                this.currentFrames.add(new BufferFrame(i));
            }
        }

        rebuildComponents();
        revalidate();
        repaint();
    }

    private void rebuildComponents() {
        removeAll();

        // Calculate min and max access timestamp among resident pages for LRU ranking
        long minAccess = Long.MAX_VALUE;
        long maxAccess = Long.MIN_VALUE;
        int occupiedCount = 0;

        for (BufferFrame f : currentFrames) {
            if (!f.isEmpty()) {
                occupiedCount++;
                if (f.getLastAccessTimestamp() < minAccess) minAccess = f.getLastAccessTimestamp();
                if (f.getLastAccessTimestamp() > maxAccess) maxAccess = f.getLastAccessTimestamp();
            }
        }

        for (int i = 0; i < currentFrames.size(); i++) {
            BufferFrame frame = currentFrames.get(i);
            boolean isLruCandidate = (occupiedCount == capacity) && !frame.isEmpty() && (frame.getLastAccessTimestamp() == minAccess);
            boolean isMruCandidate = !frame.isEmpty() && (frame.getLastAccessTimestamp() == maxAccess) && occupiedCount > 1;

            SingleFrameCard card = new SingleFrameCard(frame, algorithmType, isLruCandidate, isMruCandidate);
            add(card);
        }
    }

    /**
     * Card component for a single buffer frame.
     */
    private static class SingleFrameCard extends JPanel {
        private final BufferFrame frame;
        private final AlgorithmType algorithmType;
        private final boolean isLruCandidate;
        private final boolean isMruCandidate;

        SingleFrameCard(BufferFrame frame, AlgorithmType algorithmType, boolean isLruCandidate, boolean isMruCandidate) {
            this.frame = frame;
            this.algorithmType = algorithmType;
            this.isLruCandidate = isLruCandidate;
            this.isMruCandidate = isMruCandidate;

            setPreferredSize(new Dimension(175, 205));
            setOpaque(false);
            setLayout(new BorderLayout(4, 6));
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            // Top bar: Frame ID + Clock Pointer if applicable
            JPanel topPanel = new JPanel(new BorderLayout());
            topPanel.setOpaque(false);

            JLabel lblFrameId = new JLabel("FRAME #" + frame.getFrameIndex());
            lblFrameId.setFont(UITheme.FONT_BADGE);
            lblFrameId.setForeground(UITheme.TEXT_SECONDARY);
            topPanel.add(lblFrameId, BorderLayout.WEST);

            if (algorithmType == AlgorithmType.CLOCK && frame.isClockHandHere()) {
                JLabel lblHand = new JLabel("👉 HAND");
                lblHand.setFont(new Font("Segoe UI", Font.BOLD, 10));
                lblHand.setForeground(UITheme.COLOR_CLOCK_HAND);
                topPanel.add(lblHand, BorderLayout.EAST);
            }
            add(topPanel, BorderLayout.NORTH);

            // Center box: Page ID
            JPanel centerPanel = new JPanel(new GridBagLayout());
            centerPanel.setOpaque(false);

            if (frame.isEmpty()) {
                JLabel lblEmpty = new JLabel("[ EMPTY ]");
                lblEmpty.setFont(new Font("Segoe UI", Font.BOLD, 16));
                lblEmpty.setForeground(UITheme.TEXT_MUTED);
                centerPanel.add(lblEmpty);
            } else {
                JPanel pageBox = new JPanel(new BorderLayout());
                pageBox.setOpaque(false);

                JLabel lblPage = new JLabel("PAGE " + frame.getPageId(), SwingConstants.CENTER);
                lblPage.setFont(new Font("Segoe UI", Font.BOLD, 22));
                lblPage.setForeground(UITheme.TEXT_PRIMARY);

                pageBox.add(lblPage, BorderLayout.CENTER);
                centerPanel.add(pageBox);
            }
            add(centerPanel, BorderLayout.CENTER);

            // Bottom metadata badges
            JPanel bottomPanel = new JPanel();
            bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
            bottomPanel.setOpaque(false);

            if (frame.isEmpty()) {
                JLabel lblUnused = new JLabel("Unallocated Frame", SwingConstants.CENTER);
                lblUnused.setFont(new Font("Segoe UI", Font.ITALIC, 11));
                lblUnused.setForeground(UITheme.TEXT_MUTED);
                lblUnused.setAlignmentX(Component.CENTER_ALIGNMENT);
                bottomPanel.add(lblUnused);
            } else {
                if (algorithmType == AlgorithmType.LRU) {
                    JLabel lblAccess = new JLabel("Last Access: t=" + frame.getLastAccessTimestamp(), SwingConstants.CENTER);
                    lblAccess.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                    lblAccess.setForeground(UITheme.TEXT_SECONDARY);
                    lblAccess.setAlignmentX(Component.CENTER_ALIGNMENT);
                    bottomPanel.add(lblAccess);

                    bottomPanel.add(Box.createVerticalStrut(4));

                    if (isLruCandidate) {
                        JLabel lblVictim = new JLabel("⚠ LEAST RECENT (LRU)", SwingConstants.CENTER);
                        lblVictim.setFont(new Font("Segoe UI", Font.BOLD, 10));
                        lblVictim.setForeground(UITheme.COLOR_REPLACE);
                        lblVictim.setAlignmentX(Component.CENTER_ALIGNMENT);
                        bottomPanel.add(lblVictim);
                    } else if (isMruCandidate) {
                        JLabel lblMru = new JLabel("★ MOST RECENT (MRU)", SwingConstants.CENTER);
                        lblMru.setFont(new Font("Segoe UI", Font.BOLD, 10));
                        lblMru.setForeground(UITheme.ACCENT_PRIMARY);
                        lblMru.setAlignmentX(Component.CENTER_ALIGNMENT);
                        bottomPanel.add(lblMru);
                    } else {
                        JLabel lblWarm = new JLabel("Resident Page", SwingConstants.CENTER);
                        lblWarm.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                        lblWarm.setForeground(UITheme.TEXT_MUTED);
                        lblWarm.setAlignmentX(Component.CENTER_ALIGNMENT);
                        bottomPanel.add(lblWarm);
                    }
                } else { // CLOCK
                    JPanel bitPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
                    bitPanel.setOpaque(false);

                    JLabel lblBitLabel = new JLabel("Ref Bit:");
                    lblBitLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                    lblBitLabel.setForeground(UITheme.TEXT_SECONDARY);

                    JLabel lblBitVal = new JLabel(" [" + frame.getReferenceBit() + "] ");
                    lblBitVal.setFont(UITheme.FONT_MONO);
                    lblBitVal.setForeground(frame.getReferenceBit() == 1 ? UITheme.COLOR_REF_BIT_1 : UITheme.COLOR_REF_BIT_0);

                    bitPanel.add(lblBitLabel);
                    bitPanel.add(lblBitVal);
                    bottomPanel.add(bitPanel);

                    bottomPanel.add(Box.createVerticalStrut(4));

                    String bitStatus = frame.getReferenceBit() == 1 ? "Second Chance: YES" : "Second Chance: NO";
                    JLabel lblChance = new JLabel(bitStatus, SwingConstants.CENTER);
                    lblChance.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                    lblChance.setForeground(frame.getReferenceBit() == 1 ? UITheme.COLOR_REF_BIT_1 : UITheme.TEXT_MUTED);
                    lblChance.setAlignmentX(Component.CENTER_ALIGNMENT);
                    bottomPanel.add(lblChance);
                }
            }

            add(bottomPanel, BorderLayout.SOUTH);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int radius = 12;

            // Determine card background and border colors based on step state
            Color bg = UITheme.BG_CARD;
            Color borderColor = UITheme.BORDER_DEFAULT;
            float strokeWidth = 1.2f;

            if (frame.isHitInCurrentStep()) {
                bg = UITheme.COLOR_HIT_BG;
                borderColor = UITheme.COLOR_HIT;
                strokeWidth = 2.4f;
            } else if (frame.isVictimInCurrentStep()) {
                bg = UITheme.COLOR_REPLACE_BG;
                borderColor = UITheme.COLOR_REPLACE;
                strokeWidth = 2.4f;
            } else if (frame.isModifiedInCurrentStep()) {
                bg = UITheme.COLOR_MISS_BG;
                borderColor = UITheme.COLOR_MISS;
                strokeWidth = 2.0f;
            } else if (algorithmType == AlgorithmType.CLOCK && frame.isClockHandHere()) {
                borderColor = UITheme.COLOR_CLOCK_HAND;
                strokeWidth = 2.0f;
            } else if (isLruCandidate) {
                borderColor = new Color(244, 63, 94, 180);
                strokeWidth = 1.5f;
            }

            // Fill card body
            g2.setColor(bg);
            g2.fill(new RoundRectangle2D.Double(0, 0, width, height, radius, radius));

            // Draw border
            g2.setColor(borderColor);
            if (frame.isEmpty()) {
                // Dashed stroke for empty frames
                float[] dash = {5.0f, 4.0f};
                g2.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
            } else {
                g2.setStroke(new BasicStroke(strokeWidth));
            }
            g2.draw(new RoundRectangle2D.Double(strokeWidth / 2.0, strokeWidth / 2.0,
                    width - strokeWidth, height - strokeWidth, radius, radius));

            g2.dispose();
            super.paintComponent(g);
        }
    }
}
