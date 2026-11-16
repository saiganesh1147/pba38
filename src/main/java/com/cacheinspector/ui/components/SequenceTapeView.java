package com.cacheinspector.ui.components;

import com.cacheinspector.model.SimulationResult;
import com.cacheinspector.model.SimulationStep;
import com.cacheinspector.ui.theme.UITheme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Visual stream tape displaying the complete page reference string,
 * highlighting processed pages, the active reference, and upcoming requests.
 */
public class SequenceTapeView extends JPanel {

    private final List<Integer> sequence = new ArrayList<>();
    private SimulationResult simulationResult = null;
    private int currentStepIndex = -1; // 0-based index into sequence

    public SequenceTapeView() {
        setPreferredSize(new Dimension(800, 68));
        setMinimumSize(new Dimension(400, 68));
        setOpaque(false);
    }

    public void updateState(SimulationResult result, int stepIndex) {
        this.simulationResult = result;
        this.currentStepIndex = stepIndex;
        this.sequence.clear();
        if (result != null && result.getPageSequence() != null) {
            this.sequence.addAll(result.getPageSequence());
        }
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        int width = getWidth();
        int height = getHeight();

        // Background strip
        g2.setColor(UITheme.BG_CARD);
        g2.fill(new RoundRectangle2D.Double(0, 0, width, height, 10, 10));
        g2.setColor(UITheme.BORDER_DEFAULT);
        g2.setStroke(new BasicStroke(1.0f));
        g2.draw(new RoundRectangle2D.Double(0.5, 0.5, width - 1, height - 1, 10, 10));

        if (sequence.isEmpty()) {
            g2.setFont(UITheme.FONT_BODY);
            g2.setColor(UITheme.TEXT_MUTED);
            g2.drawString("No page reference string loaded.", 20, 38);
            g2.dispose();
            return;
        }

        // Draw items
        int cellWidth = 44;
        int cellHeight = 36;
        int gap = 8;
        int totalWidth = sequence.size() * cellWidth + (sequence.size() - 1) * gap;

        int startX = Math.max(16, (width - totalWidth) / 2);
        int startY = 22;

        for (int i = 0; i < sequence.size(); i++) {
            int x = startX + i * (cellWidth + gap);
            int y = startY;

            int pageId = sequence.get(i);
            boolean isCurrent = (i == currentStepIndex);
            boolean isPast = (i < currentStepIndex);

            // Background & Border
            Color cellBg = UITheme.BG_CARD_ALT;
            Color cellBorder = UITheme.BORDER_DEFAULT;
            Color textColor = UITheme.TEXT_MUTED;

            if (isCurrent) {
                cellBg = new Color(56, 189, 248, 60);
                cellBorder = UITheme.ACCENT_PRIMARY;
                textColor = UITheme.TEXT_PRIMARY;
            } else if (isPast && simulationResult != null && i < simulationResult.getSteps().size()) {
                SimulationStep step = simulationResult.getStep(i);
                if (step.isHit()) {
                    cellBg = new Color(16, 185, 129, 40);
                    cellBorder = UITheme.COLOR_HIT;
                    textColor = UITheme.COLOR_HIT;
                } else if (step.isReplacement()) {
                    cellBg = new Color(239, 68, 68, 40);
                    cellBorder = UITheme.COLOR_REPLACE;
                    textColor = UITheme.COLOR_REPLACE;
                } else {
                    cellBg = new Color(245, 158, 11, 40);
                    cellBorder = UITheme.COLOR_MISS;
                    textColor = UITheme.COLOR_MISS;
                }
            }

            // Draw cell
            g2.setColor(cellBg);
            g2.fill(new RoundRectangle2D.Double(x, y, cellWidth, cellHeight, 8, 8));
            g2.setColor(cellBorder);
            g2.setStroke(new BasicStroke(isCurrent ? 2.0f : 1.0f));
            g2.draw(new RoundRectangle2D.Double(x, y, cellWidth, cellHeight, 8, 8));

            // Draw Step Index above cell if current
            if (isCurrent) {
                g2.setColor(UITheme.ACCENT_PRIMARY);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                String pointerIndicator = "▼";
                FontMetrics fmPtr = g2.getFontMetrics();
                g2.drawString(pointerIndicator, x + (cellWidth - fmPtr.stringWidth(pointerIndicator)) / 2, y - 4);
            }

            // Draw Page Number
            g2.setColor(textColor);
            g2.setFont(isCurrent ? UITheme.FONT_BODY_BOLD : UITheme.FONT_BODY);
            FontMetrics fm = g2.getFontMetrics();
            String pageStr = String.valueOf(pageId);
            int textX = x + (cellWidth - fm.stringWidth(pageStr)) / 2;
            int textY = y + (cellHeight + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(pageStr, textX, textY);
        }

        g2.dispose();
    }
}
