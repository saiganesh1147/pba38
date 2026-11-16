package com.cacheinspector.ui.components;

import com.cacheinspector.model.BufferFrame;
import com.cacheinspector.model.SimulationStep;
import com.cacheinspector.ui.theme.UITheme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * High-definition circular dial visualizer for the Clock Page Replacement Algorithm.
 * Paints physical frames arranged in a circle, reference bit status, and a rotating
 * clock hand indicating current pointer position and sweep trajectory.
 */
public class ClockRadialView extends JPanel {

    private final List<BufferFrame> frames = new ArrayList<>();
    private int clockPointer = 0;
    private SimulationStep currentStep = null;
    private int capacity = 3;

    public ClockRadialView() {
        setPreferredSize(new Dimension(340, 320));
        setMinimumSize(new Dimension(280, 260));
        setOpaque(false);
    }

    public void updateState(int capacity, List<BufferFrame> frames, SimulationStep step) {
        this.capacity = capacity;
        this.frames.clear();
        this.currentStep = step;

        if (frames != null) {
            for (BufferFrame f : frames) {
                this.frames.add(f.clone());
            }
        }

        if (step != null) {
            this.clockPointer = step.getClockPointerAfter();
        } else {
            this.clockPointer = 0;
        }

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
        int centerX = width / 2;
        int centerY = height / 2;

        int diameter = Math.min(width, height) - 70;
        int radius = Math.max(diameter / 2, 70);

        // 1. Draw outer subtle ring
        g2.setColor(new Color(51, 65, 85, 90));
        g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, new float[]{6.0f, 6.0f}, 0.0f));
        g2.draw(new Ellipse2D.Double(centerX - radius, centerY - radius, radius * 2, radius * 2));

        // 2. Draw sweep arcs or trails if this step inspected multiple frames
        if (currentStep != null && currentStep.getClockInspectedFrames().size() > 1) {
            g2.setColor(new Color(99, 102, 241, 100));
            g2.setStroke(new BasicStroke(4.0f));
            g2.draw(new Ellipse2D.Double(centerX - radius, centerY - radius, radius * 2, radius * 2));
        }

        if (capacity <= 0 || frames.isEmpty()) {
            g2.dispose();
            return;
        }

        // 3. Draw Clock Hand Pointer pointing toward current clockPointer frame
        double angleForPointer = -Math.PI / 2.0 + (clockPointer * (2.0 * Math.PI / capacity));
        drawClockHand(g2, centerX, centerY, radius - 30, angleForPointer);

        // 4. Draw Radial Frame Nodes
        for (int i = 0; i < capacity; i++) {
            double angle = -Math.PI / 2.0 + (i * (2.0 * Math.PI / capacity));
            int nodeX = (int) (centerX + radius * Math.cos(angle));
            int nodeY = (int) (centerY + radius * Math.sin(angle));

            BufferFrame frame = (i < frames.size()) ? frames.get(i) : null;
            boolean isPointerTarget = (i == clockPointer);
            boolean wasInspected = currentStep != null && currentStep.getClockInspectedFrames().contains(i);

            drawFrameNode(g2, nodeX, nodeY, frame, i, isPointerTarget, wasInspected);
        }

        // 5. Center Hub
        g2.setColor(UITheme.BG_CARD_ALT);
        g2.fill(new Ellipse2D.Double(centerX - 16, centerY - 16, 32, 32));
        g2.setColor(UITheme.COLOR_CLOCK_HAND);
        g2.setStroke(new BasicStroke(2.0f));
        g2.draw(new Ellipse2D.Double(centerX - 16, centerY - 16, 32, 32));
        g2.fill(new Ellipse2D.Double(centerX - 5, centerY - 5, 10, 10));

        // 6. Draw pointer position badge at bottom
        String pointerText = String.format("Clock Hand: Frame #%d", clockPointer);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        FontMetrics fm = g2.getFontMetrics();
        int textWidth = fm.stringWidth(pointerText);
        int badgeX = centerX - (textWidth + 24) / 2;
        int badgeY = height - 26;

        g2.setColor(new Color(30, 41, 59, 220));
        g2.fill(new RoundRectangle2D.Double(badgeX, badgeY, textWidth + 24, 22, 10, 10));
        g2.setColor(UITheme.COLOR_CLOCK_HAND);
        g2.setStroke(new BasicStroke(1.0f));
        g2.draw(new RoundRectangle2D.Double(badgeX, badgeY, textWidth + 24, 22, 10, 10));

        g2.setColor(UITheme.TEXT_PRIMARY);
        g2.drawString(pointerText, badgeX + 12, badgeY + 15);

        g2.dispose();
    }

    private void drawClockHand(Graphics2D g2, int cx, int cy, int length, double angle) {
        int tipX = (int) (cx + length * Math.cos(angle));
        int tipY = (int) (cy + length * Math.sin(angle));

        // Hand line
        g2.setColor(UITheme.COLOR_CLOCK_HAND);
        g2.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(new Line2D.Double(cx, cy, tipX, tipY));

        // Arrow head
        double arrowAngle = Math.PI / 7;
        int arrowLength = 14;
        double x1 = tipX - arrowLength * Math.cos(angle - arrowAngle);
        double y1 = tipY - arrowLength * Math.sin(angle - arrowAngle);
        double x2 = tipX - arrowLength * Math.cos(angle + arrowAngle);
        double y2 = tipY - arrowLength * Math.sin(angle + arrowAngle);

        Path2D arrow = new Path2D.Double();
        arrow.moveTo(tipX, tipY);
        arrow.lineTo(x1, y1);
        arrow.lineTo(x2, y2);
        arrow.closePath();

        g2.fill(arrow);
    }

    private void drawFrameNode(Graphics2D g2, int x, int y, BufferFrame frame, int frameIndex, boolean isPointerTarget, boolean wasInspected) {
        int nodeRadius = 30;

        // Aura / Highlight glow
        if (frame != null && frame.isHitInCurrentStep()) {
            g2.setColor(new Color(16, 185, 129, 90));
            g2.fill(new Ellipse2D.Double(x - nodeRadius - 6, y - nodeRadius - 6, (nodeRadius + 6) * 2, (nodeRadius + 6) * 2));
        } else if (frame != null && frame.isVictimInCurrentStep()) {
            g2.setColor(new Color(239, 68, 68, 90));
            g2.fill(new Ellipse2D.Double(x - nodeRadius - 6, y - nodeRadius - 6, (nodeRadius + 6) * 2, (nodeRadius + 6) * 2));
        } else if (isPointerTarget) {
            g2.setColor(new Color(244, 63, 94, 60));
            g2.fill(new Ellipse2D.Double(x - nodeRadius - 5, y - nodeRadius - 5, (nodeRadius + 5) * 2, (nodeRadius + 5) * 2));
        }

        // Node circle background
        g2.setColor(UITheme.BG_CARD);
        g2.fill(new Ellipse2D.Double(x - nodeRadius, y - nodeRadius, nodeRadius * 2, nodeRadius * 2));

        // Node border
        Color borderColor = UITheme.BORDER_DEFAULT;
        float stroke = 1.5f;

        if (frame != null && frame.isHitInCurrentStep()) {
            borderColor = UITheme.COLOR_HIT;
            stroke = 2.5f;
        } else if (frame != null && frame.isVictimInCurrentStep()) {
            borderColor = UITheme.COLOR_REPLACE;
            stroke = 2.5f;
        } else if (isPointerTarget) {
            borderColor = UITheme.COLOR_CLOCK_HAND;
            stroke = 2.0f;
        } else if (wasInspected) {
            borderColor = UITheme.ACCENT_INDIGO;
            stroke = 2.0f;
        }

        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(stroke));
        g2.draw(new Ellipse2D.Double(x - nodeRadius, y - nodeRadius, nodeRadius * 2, nodeRadius * 2));

        // Frame text
        String frameLabel = "F" + frameIndex;
        g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
        g2.setColor(UITheme.TEXT_SECONDARY);
        FontMetrics fm1 = g2.getFontMetrics();
        g2.drawString(frameLabel, x - fm1.stringWidth(frameLabel) / 2, y - 8);

        // Page text
        String pageLabel = (frame == null || frame.isEmpty()) ? "--" : "P" + frame.getPageId();
        g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
        g2.setColor((frame != null && !frame.isEmpty()) ? UITheme.TEXT_PRIMARY : UITheme.TEXT_MUTED);
        FontMetrics fm2 = g2.getFontMetrics();
        g2.drawString(pageLabel, x - fm2.stringWidth(pageLabel) / 2, y + 8);

        // Reference bit badge (small circle attached to top-right)
        int badgeX = x + 14;
        int badgeY = y - 26;
        int bit = (frame != null) ? frame.getReferenceBit() : 0;
        Color bitColor = (bit == 1) ? UITheme.COLOR_REF_BIT_1 : UITheme.COLOR_REF_BIT_0;

        g2.setColor(UITheme.BG_CARD_ALT);
        g2.fill(new RoundRectangle2D.Double(badgeX, badgeY, 18, 16, 6, 6));
        g2.setColor(bitColor);
        g2.setStroke(new BasicStroke(1.2f));
        g2.draw(new RoundRectangle2D.Double(badgeX, badgeY, 18, 16, 6, 6));

        g2.setFont(new Font("Consolas", Font.BOLD, 10));
        g2.drawString(String.valueOf(bit), badgeX + 6, badgeY + 12);
    }
}
