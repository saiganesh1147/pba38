package com.cacheinspector.ui.tabs;

import com.cacheinspector.ui.theme.UITheme;

import javax.swing.*;
import java.awt.*;

/**
 * Educational guide explaining the Database Buffer Pool Architecture,
 * page replacement algorithms, and real-world database engine design (e.g. PostgreSQL, MySQL).
 */
public class GuideTabPanel extends JPanel {

    public GuideTabPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        JEditorPane editorPane = new JEditorPane();
        editorPane.setContentType("text/html");
        editorPane.setEditable(false);
        editorPane.setOpaque(false);
        editorPane.setCaretPosition(0);

        String html = """
                <html>
                <head>
                <style>
                    body {
                        font-family: 'Segoe UI', sans-serif;
                        color: #F8FAFC;
                        background-color: #0F172A;
                        margin: 10px;
                        line-height: 1.5;
                    }
                    h1 { color: #38BDF8; font-size: 20px; border-bottom: 2px solid #334155; padding-bottom: 6px; }
                    h2 { color: #818CF8; font-size: 16px; margin-top: 18px; }
                    h3 { color: #34D399; font-size: 14px; }
                    p, li { color: #CBD5E1; font-size: 13px; }
                    code { background-color: #1E293B; color: #38BDF8; padding: 2px 5px; border-radius: 4px; font-family: Consolas, monospace; }
                    .card {
                        background-color: #1E293B;
                        border: 1px solid #334155;
                        border-radius: 8px;
                        padding: 12px 16px;
                        margin-bottom: 14px;
                    }
                    .badge-hit { color: #10B981; font-weight: bold; }
                    .badge-miss { color: #F59E0B; font-weight: bold; }
                    .badge-replace { color: #EF4444; font-weight: bold; }
                </style>
                </head>
                <body>
                    <h1>Buffer Pool Management System & Cache Inspector</h1>
                    
                    <div class='card'>
                        <h2>1. What is a Database Buffer Pool?</h2>
                        <p>A database management system (DBMS) manages data larger than physical RAM by storing tables and indexes on disk in fixed-size blocks called <b>Pages</b> (typically 4KB to 16KB). Because disk I/O is orders of magnitude slower than RAM, the DBMS reserves a region of main memory known as the <b>Buffer Pool</b>.</p>
                        <p>The Buffer Pool is divided into an array of fixed-size slots called <b>Frames</b>. When a query needs to read or write a database page:</p>
                        <ul>
                            <li><b><span class='badge-hit'>Page Hit</span></b>: The page is already resident in a buffer frame. The DBMS accesses it directly at memory speed.</li>
                            <li><b><span class='badge-miss'>Page Miss (Warmup)</span></b>: The page is not in memory, but there is an empty frame available. The page is loaded from disk into the free frame.</li>
                            <li><b><span class='badge-replace'>Page Miss & Eviction</span></b>: The page is not in memory and <i>all frames are occupied</i>. A replacement policy must evict a victim page to disk to make space for the incoming page.</li>
                        </ul>
                    </div>

                    <div class='card'>
                        <h2>2. Least Recently Used (LRU) Algorithm</h2>
                        <p>The LRU policy assumes that pages accessed recently are likely to be accessed again in the near future (temporal locality).</p>
                        <ul>
                            <li><b>Tracking</b>: Each frame records the timestamp or access order of its most recent reference.</li>
                            <li><b>Victim Selection</b>: When the buffer pool is full, the frame with the oldest (smallest) access timestamp is evicted.</li>
                            <li><b>Pros</b>: High hit ratios under steady temporal locality workloads.</li>
                            <li><b>Tradeoff in Databases</b>: In highly concurrent multi-threaded database servers, updating a shared LRU list or timestamp on <i>every single page hit</i> causes massive lock contention (cacheline ping-pong).</li>
                        </ul>
                    </div>

                    <div class='card'>
                        <h2>3. Clock (Second-Chance) Page Replacement Algorithm</h2>
                        <p>The Clock algorithm provides an efficient approximation of LRU without the heavy synchronization overhead of maintaining a strictly sorted list.</p>
                        <ul>
                            <li><b>Circular Buffer</b>: Frames are treated as a logical circle with a rotating <b>Clock Hand</b> pointer.</li>
                            <li><b>Reference Bits</b>: Each frame maintains a 1-bit flag (<code>referenceBit</code>).</li>
                            <li><b>On Page Hit</b>: The page's reference bit is simply set to <code>1</code>. The clock hand does not move. Setting a single bit can be done with an atomic instruction (no global lock required!).</li>
                            <li><b>On Eviction Search</b>: The clock hand sweeps frames in circular order:
                                <ul>
                                    <li>If <code>referenceBit == 1</code>: The page is granted a <b>Second Chance</b>. Its bit is cleared to <code>0</code>, and the hand advances to the next frame.</li>
                                    <li>If <code>referenceBit == 0</code> (or empty): This frame is chosen as the <b>Victim</b>! The existing page is evicted, the new page is loaded with <code>referenceBit = 1</code>, and the clock hand advances by 1.</li>
                                </ul>
                            </li>
                            <li><b>Industry Usage</b>: PostgreSQL's buffer pool manager and Linux's page cache use Clock / Second-Chance variants because of their minimal lock contention and $O(1)$ amortized eviction overhead.</li>
                        </ul>
                    </div>

                    <div class='card'>
                        <h2>4. Quick How-To Use Cache Inspector</h2>
                        <ol>
                            <li><b>Configure</b>: Set the number of Buffer Frames (e.g., 3, 4, 5) and enter a Page Reference String (e.g. <code>1, 2, 3, 1, 4, 5, 2, 1, 2, 3, 4, 5</code>) or pick a preset.</li>
                            <li><b>Run Simulation</b>: Click <code>Run Simulation</code> to compute both algorithms concurrently.</li>
                            <li><b>Step & Animate</b>: Use <code>▶ Play</code> or <code>Next Step ▶</code> to watch pages enter frames, reference bits change, and the clock hand sweep.</li>
                            <li><b>Compare</b>: Switch to the <code>Comparison & Analytics</code> tab to view side-by-side performance charts, hit ratios, and architectural tradeoffs.</li>
                        </ol>
                    </div>
                </body>
                </html>
                """;

        editorPane.setText(html);
        JScrollPane scrollPane = new JScrollPane(editorPane);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_DEFAULT, 1));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }
}
