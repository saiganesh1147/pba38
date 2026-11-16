package com.cacheinspector.ui.components;

import com.cacheinspector.ui.theme.UITheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Modern flat rounded button with hover effects and customizable styling.
 */
public class ModernButton extends JButton {

    public enum ButtonStyle {
        PRIMARY(UITheme.ACCENT_PRIMARY, Color.BLACK, new Color(125, 211, 252)),
        SUCCESS(UITheme.COLOR_HIT, Color.BLACK, new Color(52, 211, 153)),
        DANGER(new Color(225, 29, 72), Color.WHITE, new Color(244, 63, 94)),
        SECONDARY(UITheme.BG_HOVER, UITheme.TEXT_PRIMARY, new Color(71, 85, 105)),
        OUTLINE(UITheme.BG_CARD, UITheme.TEXT_PRIMARY, UITheme.BG_HOVER);

        final Color baseColor;
        final Color textColor;
        final Color hoverColor;

        ButtonStyle(Color baseColor, Color textColor, Color hoverColor) {
            this.baseColor = baseColor;
            this.textColor = textColor;
            this.hoverColor = hoverColor;
        }
    }

    private final ButtonStyle style;
    private boolean isHovered = false;
    private boolean isPressed = false;
    private int cornerRadius = 8;

    public ModernButton(String text, ButtonStyle style) {
        super(text);
        this.style = style;
        init();
    }

    public ModernButton(String text) {
        this(text, ButtonStyle.PRIMARY);
    }

    private void init() {
        setFont(UITheme.FONT_BODY_BOLD);
        setForeground(style.textColor);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setMargin(new Insets(8, 14, 8, 14));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (isEnabled()) {
                    isHovered = true;
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (isEnabled()) {
                    isPressed = true;
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isPressed = false;
                repaint();
            }
        });
    }

    public void setCornerRadius(int radius) {
        this.cornerRadius = radius;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        Color bg;
        if (!isEnabled()) {
            bg = new Color(51, 65, 85, 100);
            g2.setColor(UITheme.TEXT_MUTED);
        } else if (isPressed) {
            bg = style.baseColor.darker();
            g2.setColor(style.textColor);
        } else if (isHovered) {
            bg = style.hoverColor;
            g2.setColor(style.textColor);
        } else {
            bg = style.baseColor;
            g2.setColor(style.textColor);
        }

        // Fill rounded background
        g2.setColor(bg);
        g2.fill(new RoundRectangle2D.Double(0, 0, width, height, cornerRadius, cornerRadius));

        // Draw subtle border for OUTLINE style
        if (style == ButtonStyle.OUTLINE) {
            g2.setColor(UITheme.BORDER_DEFAULT);
            g2.setStroke(new BasicStroke(1.2f));
            g2.draw(new RoundRectangle2D.Double(0.5, 0.5, width - 1, height - 1, cornerRadius, cornerRadius));
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
