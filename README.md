# Buffer Pool Management System – Cache Inspector
### Complete User Manual & How-to-Use Guide

A Java Swing desktop application designed to visually simulate, demonstrate, and compare database buffer pool memory management and page replacement policies (**Least Recently Used (LRU)** vs. **Clock / Second-Chance** algorithms).

---

## 🚀 Quick Start: How to Run the Application

You have three easy ways to launch the application:

### Method 1: One-Click Desktop Shortcut (Fastest)
1. Double-click the **Buffer Pool Cache Inspector** shortcut on your Windows Desktop.
2. The application will start immediately.

### Method 2: One-Click Batch Script
1. Open the project folder `D:\pba38`.
2. Double-click [run_cache_inspector.bat](file:///d:/pba38/run_cache_inspector.bat).

### Method 3: Running inside Apache NetBeans
1. Open **Apache NetBeans** (use the Desktop shortcut or [launch_netbeans.bat](file:///d:/pba38/launch_netbeans.bat)).
2. In NetBeans, go to **File** $\rightarrow$ **Open Project...** (or press `Ctrl + Shift + O`).
3. Select `D:\pba38` (marked with a Maven icon) and click **Open Project**.
4. Press **`F6`** (or right-click the project in the left panel and click **Run**).

### Method 4: Command Line (Maven / Java JAR)
```powershell
cd D:\pba38
# Run with Maven:
mvn compile exec:java

# Or run the packaged standalone JAR:
java -jar target/buffer-pool-cache-inspector-1.0.0.jar
```

---

## 🖥️ Screen Layout & Dashboard Overview

When you launch the application, you will see a dark-themed dashboard divided into four main sections:

```
┌────────────────────────────────────────────────────────────────────────┐
│  BUFFER POOL MANAGEMENT SYSTEM – CACHE INSPECTOR          v1.0.0      │
│  Database Memory Management & Page Eviction Policy Visualizer          │
├────────────────────────────────────────────────────────────────────────┤
│  [ Workload Preset: Dropdown ▼ ]                                      │
│  Buffer Capacity: [ 3 ]   Page Sequence: [ 1, 2, 3, 1, 4, 5, 2... ]   │
│                                    [🎲 Randomize] [↺ Reset] [▶ Run]   │
├────────────────────────────────────────────────────────────────────────┤
│  [ LRU Algorithm ] [ Clock Algorithm ] [ Comparison ] [ Guide ]        │
│ ┌────────────────────────────────────────────────────────────────────┐ │
│ │  📊 Stat Cards: Hits | Misses | Evictions | Hit % | Miss %         │ │
│ │  🎞️ Page Reference Tape: [1] [2] [3] [1] [4]...                   │ │
│ │  🎮 Playback Bar: [⏮] [◀ Prev] [▶ Play] [Next ▶] [⏭] Speed: ──●── │ │
│ │  🗄️ Visual Memory Frames / Clock Dial Visualizer                  │ │
│ │  💬 Live Step Narrative Box (Human-readable explanation)          │ │
│ │  📋 Step-by-Step History Table (Trace log)                         │ │
│ └────────────────────────────────────────────────────────────────────┘ │
├────────────────────────────────────────────────────────────────────────┤
│  Status: Ready • Evaluated 12 page references across 3 frames         │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 📖 Step-by-Step Usage Guide

### 1. Setting Up a Simulation
1. **Choose a Preset or Enter Custom Data**:
   - Use the **Workload Preset** dropdown to select a ready-to-test scenario:
     - **Sample Benchmark**: `1, 2, 3, 1, 4, 5, 2, 1, 2, 3, 4, 5` (3 frames)
     - **High Locality**: `1, 2, 1, 3, 1, 2, 4, 1, 2, 1, 3, 4` (4 frames)
     - **Sequential Scan**: `1, 2, 3, 4, 5, 6, 7, 8, 9, 10` (3 frames)
     - **Cyclic Working Set**: `1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3, 4` (3 frames)
     - **Belady's Anomaly**: `1, 2, 3, 4, 1, 2, 5, 1, 2, 3, 4, 5` (3 frames)
   - Or manually type any space, comma, or semicolon-separated sequence into the **Page Reference String** box (e.g. `1, 5, 2, 1, 3, 4`).
2. **Set Buffer Pool Capacity**:
   - Adjust the **Buffer Pool Capacity** spinner (from 1 to 10 frames).
3. **Execute**:
   - Click the blue **`▶ Run Simulation`** button.
   - The simulator runs both LRU and Clock simultaneously and updates all tabs.

---

### 2. Using Step-by-Step Playback Controls

Both the **LRU Algorithm** and **Clock Algorithm** tabs include interactive playback controls:

| Button / Control | Action | Description |
| :--- | :---: | :--- |
| **`⏮ Start`** | Jump to Start | Rewinds the buffer pool to step 1 (initial state). |
| **`◀ Prev Step`** | Step Back | Reverts to the previous reference step. |
| **`▶ Play / ⏸ Pause`** | Auto-play Toggle | Automatically steps through the sequence with live animations. |
| **`Next Step ▶`** | Step Forward | Advances to the next page reference. |
| **`End ⏭`** | Jump to End | Jumps directly to the final completed simulation state. |
| **Speed Slider** | Speed Control | Slide left for faster playback, slide right for slower step inspection. |
| **History Table Click** | Direct Seek | Click on any row in the table to jump directly to that exact step. |

---

### 3. Understanding the Visual Indicators & Colors

The simulator uses high-contrast semantic colors across all frames, tables, and tape ribbons:

| Color | Status | What it Means in the Database Buffer Pool |
| :---: | :---: | :--- |
| 🟢 **Emerald Green** | **PAGE HIT** | The requested page is **already in RAM**. Accessed instantly with 0 disk I/O. |
| 🟡 **Amber Orange** | **CACHE MISS (Warmup)** | The page was not in memory, but a **free frame was available**. Loaded without evicting any page. |
| 🔴 **Rose Crimson** | **MISS & EVICTION** | Buffer pool is **full**. A resident page was **evicted** to disk to make room for the new page. |
| 🔵 **Sky Blue** | **CLOCK / MRU POINTER** | Highlights the current active pointer, clock hand, or most recently accessed frame. |

---

### 4. Working with the Tabs

#### Tab 1: LRU Algorithm
- **Buffer Frame Cards**:
  - Displays each memory frame (`Frame #0`, `Frame #1`, ...), the page currently residing in it (`Page 4`), and access time (`t=11`).
  - **MRU Badge**: Highlights the Most Recently Used page.
  - **LRU Victim Badge**: Highlights the Least Recently Used page (the next page scheduled for eviction if a miss occurs).
- **Sequence Tape Ribbon**: Shows past, current (`▼`), and upcoming pages.
- **Narrative Box**: Explains the exact database event that occurred (e.g., *"Page 2 resident in Frame #1. Access timestamp updated to t=4"*).
- **Trace Table**: Full audit log showing buffer states `[F0, F1, F2]` and running hit rates.

#### Tab 2: Clock (Second-Chance) Algorithm
- **Circular Clock Dial Visualizer (`ClockRadialView`)**:
  - Memory frames arranged in a circular ring like numbers on a clock face.
  - **Clock Hand Arrow**: Points to the candidate frame currently under inspection.
  - **Reference Bit Badges**:
    - `[1]` (Green): Page was recently accessed; granted a **second chance** if swept.
    - `[0]` (Gray): Page has not been accessed again; selected as the **victim** when swept.
- **Sweep Log**: Detailed trace of the clock hand sweeps:
  > *"Frame #0 had ref bit 1 $\rightarrow$ cleared to 0 (second chance); Frame #1 had ref bit 0 $\rightarrow$ Evicted Page 2, loaded Page 4 (bit=1). Clock hand advanced to Frame #2."*

#### Tab 3: Comparison & Analytics
- **Side-by-Side Metric Cards**: Compare Total Hits, Misses, Replacements, and Hit Ratios.
- **Grouped Bar Charts**: Visual comparison of performance metrics for both algorithms.
- **Progression Curve Chart**: Line chart plotting the cumulative hit percentage over time.
- **Database Engine Takeaways**: Explains why real-world database engines (like PostgreSQL) use the Clock algorithm instead of true LRU to avoid lock contention on high-concurrency systems.

#### Tab 4: Architecture & Guide
- Built-in educational documentation on Buffer Pool architecture, frame tables, hash indexes, pin counts, dirty bits, and disk page flushing.

---

## 📊 Benchmark Verification Example

**Input**:
- **Buffer Capacity**: 3 Frames
- **Page Sequence**: `1, 2, 3, 1, 4, 5, 2, 1, 2, 3, 4, 5`

**Expected & Verified Results**:

| Step | Page | LRU Outcome | LRU Buffer | Clock Outcome | Clock Buffer & Hand | Hand / Eviction Note |
| :---: | :---: | :---: | :---: | :---: | :---: | :--- |
| **1** | **1** | MISS | `[1, -, -]` | MISS | `[1*(1), -, -]` | Loaded into Frame 0, Hand $\rightarrow$ 1 |
| **2** | **2** | MISS | `[1, 2, -]` | MISS | `[1*(1), 2*(1), -]` | Loaded into Frame 1, Hand $\rightarrow$ 2 |
| **3** | **3** | MISS | `[1, 2, 3]` | MISS | `[1*(1), 2*(1), 3*(1)]` | Loaded into Frame 2, Hand $\rightarrow$ 0 |
| **4** | **1** | 🟢 **HIT** | `[1, 2, 3]` | 🟢 **HIT** | `[1*(1), 2*(1), 3*(1)]` | Page 1 in F0; Hand stays at 0 |
| **5** | **4** | 🔴 **EVICT** | `[1, 4, 3]` | 🔴 **EVICT** | `[4*(1), 2(0), 3(0)]` | Clock clears bits 0,1,2 $\rightarrow$ evicts F0 |
| **6** | **5** | 🔴 **EVICT** | `[1, 4, 5]` | 🔴 **EVICT** | `[4*(1), 5*(1), 3(0)]` | Evicts F1 (bit was 0) $\rightarrow$ Hand $\rightarrow$ 2 |
| **7** | **2** | 🔴 **EVICT** | `[2, 4, 5]` | 🔴 **EVICT** | `[4*(1), 5*(1), 2*(1)]` | Evicts F2 (bit was 0) $\rightarrow$ Hand $\rightarrow$ 0 |
| **8** | **1** | 🔴 **EVICT** | `[2, 1, 5]` | 🔴 **EVICT** | `[1*(1), 5(0), 2(0)]` | Clock clears bits $\rightarrow$ evicts F0 |
| **9** | **2** | 🟢 **HIT** | `[2, 1, 5]` | 🟢 **HIT** | `[1*(1), 5(0), 2*(1)]` | Page 2 in F2; bit set to 1 |
| **10** | **3** | 🔴 **EVICT** | `[2, 1, 3]` | 🔴 **EVICT** | `[1*(1), 3*(1), 2*(1)]` | Evicts F1 (bit was 0) $\rightarrow$ Hand $\rightarrow$ 2 |
| **11** | **4** | 🔴 **EVICT** | `[2, 4, 3]` | 🔴 **EVICT** | `[1(0), 3(0), 4*(1)]` | Clock sweeps $\rightarrow$ evicts F2 |
| **12** | **5** | 🔴 **EVICT** | `[5, 4, 3]` | 🔴 **EVICT** | `[5*(1), 3(0), 4*(1)]` | Evicts F0 (bit was 0) $\rightarrow$ Hand $\rightarrow$ 1 |

**Final Totals**:
- **Total Hits**: **2** (Step 4 & Step 9)
- **Total Misses**: **10**
- **Total Replacements (Evictions)**: **7**
- **Hit Ratio**: **16.67%**
- **Miss Ratio**: **83.33%**

---

## 🛠️ Project Structure & File Reference

```
d:\pba38\
├── launch_netbeans.bat                        # One-click launcher for Apache NetBeans (Java 21 LTS)
├── run_cache_inspector.bat                    # One-click application runner
├── pom.xml                                    # Maven build file with shaded JAR configuration
├── README.md                                  # Complete User Manual and Documentation
└── src\
    ├── main\java\com\cacheinspector\
    │   ├── Main.java                          # Swing Event Dispatch Thread application entry point
    │   ├── model\
    │   │   ├── AlgorithmType.java             # Enum for LRU and CLOCK algorithms
    │   │   ├── PageStatus.java                # HIT, MISS_EMPTY, MISS_REPLACE outcomes
    │   │   ├── BufferFrame.java               # Physical buffer frame memory model
    │   │   ├── SimulationStep.java            # Immutable step snapshot & narrative data
    │   │   └── SimulationResult.java          # Performance metrics & aggregated results
    │   ├── algorithm\
    │   │   ├── PageReplacementAlgorithm.java  # Common strategy contract
    │   │   ├── LruAlgorithm.java              # Deterministic Least Recently Used implementation
    │   │   └── ClockAlgorithm.java            # Second-Chance Clock implementation
    │   ├── util\
    │   │   └── InputParser.java               # Sequence validator & random generator
    │   └── ui\
    │       ├── MainFrame.java                 # Main dashboard window & tab coordinator
    │       ├── theme\
    │       │   └── UITheme.java               # Dark theme color tokens and FlatLaf styling
    │       ├── components\
    │       │   ├── ModernButton.java          # Flat rounded button with hover states
    │       │   ├── StatCard.java              # Metrics card with semantic colored bar
    │       │   ├── BufferFramesView.java      # Visual memory slots (MRU/LRU badges)
    │       │   ├── ClockRadialView.java       # Circular clock dial with rotating pointer
    │       │   ├── SequenceTapeView.java      # Horizontal sequence ribbon
    │       │   └── ComparisonChartView.java   # Custom Java2D bar charts & progression lines
    │       └── tabs\
    │           ├── DashboardPanel.java        # Top configuration inputs & preset selector
    │           ├── AlgorithmTabPanel.java     # Single-algorithm playback & trace table
    │           ├── ComparisonTabPanel.java    # Side-by-side comparison & insights
    │           └── GuideTabPanel.java         # Built-in Buffer Pool architecture guide
    └── test\java\com\cacheinspector\
        ├── AlgorithmTest.java                 # Verified unit tests for LRU and Clock
        ├── InputParserTest.java               # Validation & delimiter test suite
        └── UiSmokeTest.java                   # GUI initialization smoke test
```

---

## ❓ Frequently Asked Questions & Troubleshooting

### Q: Which Java version is used?
- The application compiles and runs on **Java 17+** and **Java 21 LTS** (`D:\jdk-21`).
- Apache NetBeans is configured to run using OpenJDK 21 LTS via [launch_netbeans.bat](file:///d:/pba38/launch_netbeans.bat).

### Q: Can I run without NetBeans open?
- **Yes!** You do not need NetBeans to use the application. Simply double-click the **Buffer Pool Cache Inspector** shortcut on your Desktop or run [run_cache_inspector.bat](file:///d:/pba38/run_cache_inspector.bat).

### Q: How do I test Belady's Anomaly?
- Select **Belady's Anomaly Test** from the preset dropdown (`1, 2, 3, 4, 1, 2, 5, 1, 2, 3, 4, 5`).
- Test with **3 frames** (notice 9 misses), then test with **4 frames** (notice 10 misses with FIFO, demonstrating that more frames don't always mean fewer misses for non-stack algorithms).
