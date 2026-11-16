package com.cacheinspector.ui.theme;

import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/**
 * Central design tokens, color palette, typography, and styling helpers for
 * the Buffer Pool Management System dark theme dashboard.
 */
public final class UITheme {

    private UITheme() {}

    // Core Canvas Colors (Slate palette)
    public static final Color BG_CANVAS = new Color(15, 23, 42);       // #0F172A
    public static final Color BG_CARD = new Color(30, 41, 59);         // #1E293B
    public static final Color BG_CARD_ALT = new Color(24, 34, 49);     // #182231
    public static final Color BG_INPUT = new Color(15, 23, 42);        // #0F172A
    public static final Color BG_HOVER = new Color(51, 65, 85);        // #334155

    // Borders & Dividers
    public static final Color BORDER_DEFAULT = new Color(51, 65, 85);  // #334155
    public static final Color BORDER_LIGHT = new Color(71, 85, 105);   // #475569
    public static final Color BORDER_FOCUS = new Color(56, 189, 248);  // #38BDF8

    // Text hierarchy
    public static final Color TEXT_PRIMARY = new Color(248, 250, 252); // #F8FAFC
    public static final Color TEXT_SECONDARY = new Color(148, 163, 184);// #94A3B8
    public static final Color TEXT_MUTED = new Color(100, 116, 139);   // #64748B

    // Brand & Status Accents
    public static final Color ACCENT_PRIMARY = new Color(56, 189, 248);// Sky Blue
    public static final Color ACCENT_INDIGO = new Color(99, 102, 241); // Indigo
    public static final Color ACCENT_PURPLE = new Color(168, 85, 247); // Purple

    // Outcome Colors (High contrast & semantic)
    public static final Color COLOR_HIT = new Color(16, 185, 129);       // Emerald Green #10B981
    public static final Color COLOR_HIT_BG = new Color(6, 78, 59, 160);
    public static final Color COLOR_MISS = new Color(245, 158, 11);      // Amber Orange #F59E0B
    public static final Color COLOR_MISS_BG = new Color(120, 53, 15, 160);
    public static final Color COLOR_REPLACE = new Color(239, 68, 68);    // Rose Crimson #EF4444
    public static final Color COLOR_REPLACE_BG = new Color(136, 19, 55, 160);

    // Clock Visualization specifics
    public static final Color COLOR_CLOCK_HAND = new Color(244, 63, 94); // Bright Rose #F43F5E
    public static final Color COLOR_REF_BIT_1 = new Color(34, 197, 94);  // Green #22C55E
    public static final Color COLOR_REF_BIT_0 = new Color(100, 116, 139);// Slate Muted #64748B

    // Typography
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_SECTION = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_MONO = new Font("Consolas", Font.BOLD, 14);
    public static final Font FONT_STAT_VALUE = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font FONT_STAT_LABEL = new Font("Segoe UI", Font.BOLD, 11);
    public static final Font FONT_BADGE = new Font("Segoe UI", Font.BOLD, 11);

    /**
     * Initializes the modern dark Look and Feel for the application.
     */
    public static void setupLookAndFeel() {
        try {
            FlatDarkLaf.setup();

            // Customize global Swing defaults
            UIManager.put("Panel.background", BG_CANVAS);
            UIManager.put("TabbedPane.background", BG_CANVAS);
            UIManager.put("TabbedPane.foreground", TEXT_SECONDARY);
            UIManager.put("TabbedPane.selectedForeground", TEXT_PRIMARY);
            UIManager.put("TabbedPane.underlineColor", ACCENT_PRIMARY);
            UIManager.put("TabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0));
            UIManager.put("Table.background", BG_CARD);
            UIManager.put("Table.foreground", TEXT_PRIMARY);
            UIManager.put("Table.selectionBackground", new Color(51, 65, 85));
            UIManager.put("Table.selectionForeground", TEXT_PRIMARY);
            UIManager.put("TableHeader.background", BG_CARD_ALT);
            UIManager.put("TableHeader.foreground", TEXT_SECONDARY);
            UIManager.put("ScrollPane.background", BG_CANVAS);
            UIManager.put("ScrollBar.track", BG_CANVAS);
            UIManager.put("ScrollBar.thumb", BORDER_DEFAULT);
            UIManager.put("ToolTip.background", BG_CARD);
            UIManager.put("ToolTip.foreground", TEXT_PRIMARY);
            UIManager.put("ToolTip.border", BorderFactory.createLineBorder(BORDER_DEFAULT));
        } catch (Exception e) {
            System.err.println("Notice: FlatDarkLaf could not be loaded; falling back to default dark palette. " + e.getMessage());
        }
    }

    public static Border createCardBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_DEFAULT, 1, true),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        );
    }

    public static Border createSectionBorder(String title) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(BORDER_DEFAULT, 1),
                        title,
                        0,
                        0,
                        FONT_SECTION,
                        ACCENT_PRIMARY
                ),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        );
    }
}
