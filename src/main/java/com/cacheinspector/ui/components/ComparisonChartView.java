package com.cacheinspector.ui.components;

import com.cacheinspector.model.SimulationResult;
import com.cacheinspector.model.SimulationStep;
import com.cacheinspector.ui.theme.UITheme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Custom high-resolution Graphics2D component rendering graphical comparisons
 * between LRU and Clock page replacement algorithms (bar charts and timeline curves).
 */
public class ComparisonChartView extends JPanel {

    private SimulationResult lruResult;
    private SimulationResult clockResult;

    public ComparisonChartView() {
        setPreferredSize(new Dimension(800, 360));
        setMinimumSize(new Dimension(600, 320));
        setOpaque(false);
    }

    public void setResults(SimulationResult lruResult, SimulationResult clockResult) {
        this.lruResult = lruResult;
        this.clockResult = clockResult;
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

        if (lruResult == null || clockResult == null || lruResult.getSteps().isEmpty()) {
            drawEmptyState(g2, width, height);
            g2.dispose();
            return;
        }

        // Split panel into Left (Grouped Bar Chart) and Right (Hit Ratio Timeline)
        int padding = 16;
        int halfWidth = (width - padding * 3) / 2;
        int chartHeight = height - padding * 2;

        drawBarComparisonChart(g2, padding, padding, halfWidth, chartHeight);
        drawTimelineCurveChart(g2, padding * 2 + halfWidth, padding, halfWidth, chartHeight);

        g2.dispose();
    }

    private void drawEmptyState(Graphics2D g2, int width, int height) {
        g2.setFont(UITheme.FONT_BODY);
        g2.setColor(UITheme.TEXT_MUTED);
        String msg = "Run a simulation to generate LRU vs Clock comparative analytics.";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(msg, (width - fm.stringWidth(msg)) / 2, height / 2);
    }

    private void drawBarComparisonChart(Graphics2D g2, int x, int y, int w, int h) {
        // Card Background
        g2.setColor(UITheme.BG_CARD);
        g2.fill(new RoundRectangle2D.Double(x, y, w, h, 12, 12));
        g2.setColor(UITheme.BORDER_DEFAULT);
        g2.setStroke(new BasicStroke(1.0f));
        g2.draw(new RoundRectangle2D.Double(x, y, w, h, 12, 12));

        // Title
        g2.setFont(UITheme.FONT_SECTION);
        g2.setColor(UITheme.TEXT_PRIMARY);
        g2.drawString("Performance Metrics Comparison", x + 16, y + 28);

        // Legend
        int legendX = x + w - 160;
        int legendY = y + 16;

        g2.setColor(UITheme.ACCENT_PRIMARY);
        g2.fillRoundRect(legendX, legendY, 12, 12, 4, 4);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
        g2.setColor(UITheme.TEXT_PRIMARY);
        g2.drawString("LRU", legendX + 16, legendY + 10);

        g2.setColor(UITheme.ACCENT_PURPLE);
        g2.fillRoundRect(legendX + 55, legendY, 12, 12, 4, 4);
        g2.setColor(UITheme.TEXT_PRIMARY);
        g2.drawString("Clock", legendX + 71, legendY + 10);

        // Metrics to compare
        String[] categories = {"Page Hits", "Page Misses", "Evictions", "Hit %"};
        double[] lruVals = {
                lruResult.getTotalHits(),
                lruResult.getTotalMisses(),
                lruResult.getTotalReplacements(),
                lruResult.getHitPercentage()
        };
        double[] clockVals = {
                clockResult.getTotalHits(),
                clockResult.getTotalMisses(),
                clockResult.getTotalReplacements(),
                clockResult.getHitPercentage()
        };

        int maxRawVal = Math.max(lruResult.getTotalReferences(), 1);
        int startY = y + 55;
        int rowHeight = (h - 70) / categories.length;
        int barMaxLen = w - 180;

        for (int i = 0; i < categories.length; i++) {
            int rowY = startY + i * rowHeight;
            boolean isPercentage = (i == 3);

            // Label
            g2.setFont(UITheme.FONT_BODY_BOLD);
            g2.setColor(UITheme.TEXT_SECONDARY);
            g2.drawString(categories[i], x + 16, rowY + 16);

            // Calculate widths
            int lruLen = isPercentage
                    ? (int) ((lruVals[i] / 100.0) * barMaxLen)
                    : (int) ((lruVals[i] / maxRawVal) * barMaxLen);
            int clockLen = isPercentage
                    ? (int) ((clockVals[i] / 100.0) * barMaxLen)
                    : (int) ((clockVals[i] / maxRawVal) * barMaxLen);

            int barStartX = x + 110;
            int barHeight = 11;

            // LRU Bar
            g2.setColor(UITheme.ACCENT_PRIMARY);
            g2.fill(new RoundRectangle2D.Double(barStartX, rowY + 2, Math.max(lruLen, 4), barHeight, 6, 6));

            // LRU Value text
            String lruText = isPercentage ? String.format("%.1f%%", lruVals[i]) : String.valueOf((int) lruVals[i]);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g2.setColor(UITheme.TEXT_PRIMARY);
            g2.drawString(lruText, barStartX + Math.max(lruLen, 4) + 6, rowY + 11);

            // Clock Bar
            g2.setColor(UITheme.ACCENT_PURPLE);
            g2.fill(new RoundRectangle2D.Double(barStartX, rowY + 18, Math.max(clockLen, 4), barHeight, 6, 6));

            // Clock Value text
            String clockText = isPercentage ? String.format("%.1f%%", clockVals[i]) : String.valueOf((int) clockVals[i]);
            g2.drawString(clockText, barStartX + Math.max(clockLen, 4) + 6, rowY + 27);
        }
    }

    private void drawTimelineCurveChart(Graphics2D g2, int x, int y, int w, int h) {
        // Card Background
        g2.setColor(UITheme.BG_CARD);
        g2.fill(new RoundRectangle2D.Double(x, y, w, h, 12, 12));
        g2.setColor(UITheme.BORDER_DEFAULT);
        g2.setStroke(new BasicStroke(1.0f));
        g2.draw(new RoundRectangle2D.Double(x, y, w, h, 12, 12));

        // Title
        g2.setFont(UITheme.FONT_SECTION);
        g2.setColor(UITheme.TEXT_PRIMARY);
        g2.drawString("Cumulative Hit Ratio Progression", x + 16, y + 28);

        int plotLeft = x + 45;
        int plotRight = x + w - 25;
        int plotTop = y + 55;
        int plotBottom = y + h - 35;
        int plotW = plotRight - plotLeft;
        int plotH = plotBottom - plotTop;

        // Draw horizontal grid lines (0%, 25%, 50%, 75%, 100%)
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        g2.setColor(UITheme.TEXT_MUTED);

        for (int p = 0; p <= 100; p += 25) {
            int gy = plotBottom - (int) ((p / 100.0) * plotH);
            g2.setColor(new Color(51, 65, 85, 90));
            g2.setStroke(new BasicStroke(1.0f));
            g2.drawLine(plotLeft, gy, plotRight, gy);

            g2.setColor(UITheme.TEXT_MUTED);
            g2.drawString(p + "%", x + 14, gy + 3);
        }

        int stepCount = lruResult.getSteps().size();
        if (stepCount < 2) {
            g2.drawString("Requires 2+ steps to plot progression.", plotLeft + 20, plotTop + 40);
            return;
        }

        // Draw LRU Curve
        Path2D lruPath = new Path2D.Double();
        for (int i = 0; i < stepCount; i++) {
            SimulationStep step = lruResult.getStep(i);
            double ratio = step.getRunningHitRatio();
            double px = plotLeft + ((double) i / (stepCount - 1)) * plotW;
            double py = plotBottom - (ratio / 100.0) * plotH;

            if (i == 0) lruPath.moveTo(px, py);
            else lruPath.lineTo(px, py);
        }

        g2.setColor(UITheme.ACCENT_PRIMARY);
        g2.setStroke(new BasicStroke(2.2f));
        g2.draw(lruPath);

        // Draw Clock Curve
        Path2D clockPath = new Path2D.Double();
        for (int i = 0; i < stepCount; i++) {
            SimulationStep step = clockResult.getStep(i);
            double ratio = step.getRunningHitRatio();
            double px = plotLeft + ((double) i / (stepCount - 1)) * plotW;
            double py = plotBottom - (ratio / 100.0) * plotH;

            if (i == 0) clockPath.moveTo(px, py);
            else clockPath.lineTo(px, py);
        }

        g2.setColor(UITheme.ACCENT_PURPLE);
        g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, new float[]{4.0f, 4.0f}, 0.0f));
        g2.draw(clockPath);

        // Draw X-axis label
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        g2.setColor(UITheme.TEXT_MUTED);
        g2.drawString("Simulation Steps (1 → " + stepCount + ")", plotLeft + (plotW / 2) - 50, plotBottom + 20);
    }
}
