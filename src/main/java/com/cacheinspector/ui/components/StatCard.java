package com.cacheinspector.ui.components;

import com.cacheinspector.ui.theme.UITheme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Metric card component for presenting summary counts, percentages, and ratios.
 */
public class StatCard extends JPanel {

    private final JLabel lblTitle;
    private final JLabel lblValue;
    private final JLabel lblSubtitle;
    private final Color accentColor;

    public StatCard(String title, String initialValue, String subtitle, Color accentColor) {
        this.accentColor = accentColor;
        setLayout(new BorderLayout(6, 6));
        setOpaque(false);
        setPreferredSize(new Dimension(160, 80));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 14));

        lblTitle = new JLabel(title.toUpperCase());
        lblTitle.setFont(UITheme.FONT_STAT_LABEL);
        lblTitle.setForeground(UITheme.TEXT_SECONDARY);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblValue = new JLabel(initialValue);
        lblValue.setFont(UITheme.FONT_STAT_VALUE);
        lblValue.setForeground(UITheme.TEXT_PRIMARY);
        lblValue.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblSubtitle = new JLabel(subtitle);
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSubtitle.setForeground(UITheme.TEXT_MUTED);
        lblSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        contentPanel.add(lblTitle);
        contentPanel.add(Box.createVerticalStrut(2));
        contentPanel.add(lblValue);
        contentPanel.add(Box.createVerticalStrut(2));
        contentPanel.add(lblSubtitle);

        add(contentPanel, BorderLayout.CENTER);
    }

    public void update(String value, String subtitle) {
        lblValue.setText(value);
        lblSubtitle.setText(subtitle);
        repaint();
    }

    public void updateValue(String value) {
        lblValue.setText(value);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        int cornerRadius = 10;

        // Card background
        g2.setColor(UITheme.BG_CARD);
        g2.fill(new RoundRectangle2D.Double(0, 0, width, height, cornerRadius, cornerRadius));

        // Subtle border
        g2.setColor(UITheme.BORDER_DEFAULT);
        g2.setStroke(new BasicStroke(1.0f));
        g2.draw(new RoundRectangle2D.Double(0.5, 0.5, width - 1, height - 1, cornerRadius, cornerRadius));

        // Left accent indicator bar
        g2.setColor(accentColor);
        g2.fillRoundRect(0, 0, 5, height, cornerRadius, cornerRadius);

        g2.dispose();
        super.paintComponent(g);
    }
}
