# Buffer Pool Management System – Cache Inspector

A Java Swing desktop application designed to visually simulate, demonstrate, and compare database buffer pool memory management and page replacement policies, specifically **Least Recently Used (LRU)** and the **Clock (Second-Chance)** page replacement algorithms.

---

## 1. Project Objective & Overview

Database Management Systems (DBMS) like PostgreSQL, MySQL, and Oracle manage massive data files stored on persistent disk blocks (pages). Because physical disk I/O is orders of magnitude slower than CPU registers and RAM, the database engine maintains an in-memory cache known as the **Buffer Pool**.

The buffer pool is divided into an array of fixed-size slots called **Frames**. As user queries run:
1. **Page Hit**: If the requested page is already loaded into a buffer frame, it is accessed directly in memory without disk access.
2. **Page Miss (Warmup)**: If the page is not in memory but free buffer frames are available, the page is read from disk into an empty frame.
3. **Page Miss & Eviction**: If the page is not in memory and all buffer pool frames are full, a replacement algorithm must select an existing resident page (a "victim") to evict, flush any dirty modifications to disk, and load the new page in its place.

**Buffer Pool Management System – Cache Inspector** provides a high-fidelity visual simulation of this process. It enables database engineering students, instructors, and developers to observe how memory frames fill up, track recency, inspect second-chance reference bits, witness clock hand sweep trajectories, and analyze empirical performance metrics.

---

## 2. Key Features

### A. Dashboard & Workload Configuration
- **Customizable Buffer Capacity**: Set the number of physical frames ($1 \le N \le 10$).
- **Flexible Reference String Input**: Accepts comma, semicolon, or space-separated sequences of page IDs (e.g., `1, 2, 3, 1, 4, 5, 2, 1, 2, 3, 4, 5`).
- **One-Click Presets**:
  - *Sample Benchmark*: 3 frames, `1, 2, 3, 1, 4, 5, 2, 1, 2, 3, 4, 5`
  - *High Locality*: 4 frames, `1, 2, 1, 3, 1, 2, 4, 1, 2, 1, 3, 4`
  - *Sequential Scan*: 3 frames, `1, 2, 3, 4, 5, 6, 7, 8, 9, 10`
  - *Cyclic Working Set*: 3 frames, `1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3, 4`
  - *Belady's Anomaly Test*: 3 frames, `1, 2, 3, 4, 1, 2, 5, 1, 2, 3, 4, 5`
- **Random Sequence Generator**: Generates realistic access streams with temporal locality.
- **Input Validation & Error Handling**: Non-numeric tokens, negative page numbers, out-of-range inputs, and empty sequences are flagged with clear contextual alerts.

### B. LRU (Least Recently Used) Algorithm Tab
- **Interactive Memory Frame Cards**:
  - Displays Frame ID (`Frame #0`, `Frame #1`, ...), current resident page (`Page 4`), and last access timestamp (`t=11`).
  - Highlights the **Most Recently Used (MRU)** page and the **Least Recently Used (LRU - Victim Candidate)** page in real time.
- **Vibrant Status Color Coding**:
  - 🟢 **Page Hit**: Vibrant Emerald Green border with soft green glow aura.
  - 🟡 **Page Miss (Warmup)**: Amber Orange border with warm glow.
  - 🔴 **Page Miss & Eviction**: Rose Crimson border with eviction indicator banner.
- **Visual Reference Stream Tape**: Horizontal tape showing processed, current (active marker `▼`), and upcoming pages.
- **Step-by-Step Replacement History Table (`JTable`)**:
  - Columns: `Step #`, `Referenced Page`, `Outcome (HIT / MISS / EVICTION)`, `Victim Page`, `Target Frame`, `Buffer Pool State`, `Recency`, `Running Hit %`.
  - Color-coded cells and click-to-jump row selection.

### C. Clock (Second-Chance) Algorithm Tab
- **Dual Visualizer**:
  1. **Circular Clock Dial Visualizer (`ClockRadialView`)**:
     - Custom high-resolution Java2D vector dial.
     - Buffer frames arranged as radial nodes along a circular dial.
     - Rotating **Clock Hand Pointer** (bright rose arrow) pointing to the active candidate frame.
     - Sweep indicators showing frames evaluated and second chances granted.
     - Reference bit badges (`[1]` green for second chance granted, `[0]` gray for victim candidate).
  2. **Linear Memory Frame Cards**: Synchronized with the radial dial for easy inspection.
- **Detailed Sweep Log Narrative**:
  - Live narrative explaining the exact pointer sweep:
    > *"Frame #0 (Page 1) had ref bit 1 -> reset to 0 (second chance granted); Frame #1 (Page 2) had ref bit 0 -> EVICTED! Loaded Page 4 with ref bit=1. Clock hand advanced to Frame #2."*
- **Step-by-Step Replacement History Table (`JTable`)**: Tracks clock hand positions, swept frames, and hit ratios at every point in time.

### D. Step-by-Step Simulation & Animation Controls
- **Controls**: `⏮ Start`, `◀ Prev Step`, `▶ Play / ⏸ Pause`, `Next Step ▶`, `End ⏭`.
- **Adjustable Speed Slider**: Dynamically adjusts timer tick interval from 150ms to 1500ms.
- **Interactive Timeline**: Click on any table row to jump instantly to that step.

### E. Graphical Comparison & Analytics Tab
- **Side-by-Side Performance Cards**:
  - LRU Hits vs Clock Hits
  - LRU Hit Ratio % vs Clock Hit Ratio %
  - Performance Delta & Advantage summary (e.g. `EQUAL (0.0%)`, `LRU +8.3%`, `CLOCK +5.0%`)
- **Grouped Bar Charts**: Compares Total Hits, Total Misses, Total Evictions, and Hit % visually.
- **Cumulative Hit Ratio Progression Curves**: Vector chart plotting the running hit percentage of both algorithms across the entire sequence.
- **Synchronized Step Table**: Direct side-by-side comparison of LRU vs Clock frame states at each step.
- **Database Engineering Insights**: Theoretical and architectural comparison explaining why production database engines (like PostgreSQL and the Linux kernel) favor Clock over true LRU to minimize lock contention.

### F. Architecture & Cache Inspector Guide Tab
- Built-in documentation explaining Buffer Pool architecture, frame tables, hash indexes, pin counts, dirty flags, and real-world database engine design.

---

## 3. Verified Sample Sequence Execution Trace

Given the assignment requirements:
- **Buffer Pool Capacity**: `3 Frames`
- **Page Reference String**: `1, 2, 3, 1, 4, 5, 2, 1, 2, 3, 4, 5`

### Deterministic Results Summary:

| Metric | LRU Algorithm | Clock Algorithm | Match |
| :--- | :---: | :---: | :---: |
| **Total Page References** | 12 | 12 | Identical |
| **Total Page Hits** | **2** (Steps 4 & 9) | **2** (Steps 4 & 9) | Identical |
| **Total Page Misses** | **10** | **10** | Identical |
| **Total Evictions (Replacements)** | **7** | **7** | Identical |
| **Final Hit Percentage** | **16.67%** (2/12) | **16.67%** (2/12) | Identical |
| **Final Miss Percentage** | **83.33%** (10/12) | **83.33%** (10/12) | Identical |
| **Final Resident Buffer Pages** | `[5, 4, 3]` | `[5, 3, 4]` | Pages {3, 4, 5} |

### Step-by-Step Trace:
- **Step 1 (Ref 1)**: Miss. Empty Frame 0 allocated $\rightarrow$ `[1, -, -]`
- **Step 2 (Ref 2)**: Miss. Empty Frame 1 allocated $\rightarrow$ `[1, 2, -]`
- **Step 3 (Ref 3)**: Miss. Empty Frame 2 allocated $\rightarrow$ `[1, 2, 3]`
- **Step 4 (Ref 1)**: **HIT!** Page 1 already resident $\rightarrow$ `[1, 2, 3]`
- **Step 5 (Ref 4)**: Miss & Replace $\rightarrow$ Evicts Page 2 in LRU, Evicts Page 1 in Clock
- **Step 6 (Ref 5)**: Miss & Replace
- **Step 7 (Ref 2)**: Miss & Replace
- **Step 8 (Ref 1)**: Miss & Replace
- **Step 9 (Ref 2)**: **HIT!** Page 2 already resident in buffer
- **Step 10 (Ref 3)**: Miss & Replace
- **Step 11 (Ref 4)**: Miss & Replace
- **Step 12 (Ref 5)**: Miss & Replace

---

## 4. Technology Stack & Architecture

- **Language**: Java 17+ (Eclipse Adoptium OpenJDK 17)
- **GUI Framework**: Java Swing (`JFrame`, `JPanel`, `JTabbedPane`, `JTable`, `JScrollPane`, `JSpinner`, `JTextField`, `JLabel`, `JSlider`, `Timer`)
- **Theme & Styling**: FlatLaf Dark theme with custom high-DPI Java2D antialiased rendering
- **Build System**: Apache Maven 3.x
- **Testing Framework**: JUnit 5 (Jupiter 5.10.2)

### Directory Structure:
```
d:\pba38\
├── pom.xml                                   # Maven project descriptor & shaded JAR build config
├── README.md                                 # Comprehensive documentation & setup manual
└── src\
    ├── main\java\com\cacheinspector\
    │   ├── Main.java                         # Application entrypoint (Swing EDT launcher)
    │   ├── model\
    │   │   ├── AlgorithmType.java            # Enum: LRU, CLOCK
    │   │   ├── PageStatus.java               # Enum: HIT, MISS_EMPTY, MISS_REPLACE
    │   │   ├── BufferFrame.java              # Buffer frame slot model (pageId, timestamps, refBits)
    │   │   ├── SimulationStep.java           # Immutable step snapshot & explanation
    │   │   └── SimulationResult.java         # Aggregated metrics & step history
    │   ├── algorithm\
    │   │   ├── PageReplacementAlgorithm.java # Common algorithm strategy interface
    │   │   ├── LruAlgorithm.java             # Least Recently Used implementation
    │   │   └── ClockAlgorithm.java           # Second-Chance Clock implementation
    │   ├── util\
    │   │   └── InputParser.java              # Input parser, validator, and random sequence generator
    │   └── ui\
    │       ├── MainFrame.java                # Main dashboard JFrame & tab container
    │       ├── theme\
    │       │   └── UITheme.java              # Color tokens, fonts, FlatLaf configuration
    │       ├── components\
    │       │   ├── ModernButton.java         # Custom rounded button with hover/pressed states
    │       │   ├── StatCard.java             # Metric card with color indicator bar
    │       │   ├── BufferFramesView.java     # Interactive buffer memory slots visualizer
    │       │   ├── ClockRadialView.java      # Circular clock dial with rotating pointer
    │       │   ├── SequenceTapeView.java     # Horizontal page stream tape
    │       │   └── ComparisonChartView.java  # Custom Java2D bar charts and progression curves
    │       └── tabs\
    │           ├── DashboardPanel.java       # Top configuration panel with inputs & presets
    │           ├── AlgorithmTabPanel.java    # Single-algorithm visualization panel
    │           ├── ComparisonTabPanel.java   # Side-by-side comparative analytics panel
    │           └── GuideTabPanel.java        # Architecture guide & documentation
    └── test\java\com\cacheinspector\
        ├── AlgorithmTest.java                # Verified unit tests for LRU and Clock
        ├── InputParserTest.java              # Robust input validation tests
        └── UiSmokeTest.java                  # Headless & GUI initialization smoke tests
```

---

## 5. Compilation and Execution Instructions

### Option 1: Command Line (CLI via Maven)

1. Open PowerShell, Command Prompt, or Terminal in the project root:
   ```bash
   cd d:\pba38
   ```

2. Compile and run all automated tests:
   ```bash
   mvn test
   ```

3. Launch the application directly:
   ```bash
   mvn compile exec:java
   ```

4. Or build and run the standalone, self-contained executable JAR (with all dependencies bundled):
   ```bash
   mvn package
   java -jar target/buffer-pool-cache-inspector-1.0.0.jar
   ```

---

### Option 2: IntelliJ IDEA

1. Open IntelliJ IDEA.
2. Select **File $\rightarrow$ Open...** and choose the `d:\pba38` directory (select `pom.xml` as a project).
3. IntelliJ will automatically detect and import the Maven project.
4. Verify that the Project SDK is set to **Java 17** or higher (**File $\rightarrow$ Project Structure $\rightarrow$ Project $\rightarrow$ SDK**).
5. Locate `src/main/java/com/cacheinspector/Main.java`.
6. Right-click `Main.java` and select **Run 'Main.main()'** (or press `Shift + F10`).

---

### Option 3: Apache NetBeans

1. Open Apache NetBeans (v17 or higher recommended).
2. Select **File $\rightarrow$ Open Project...**.
3. Browse to `d:\pba38` (NetBeans will recognize the Maven icon on the project folder).
4. Click **Open Project**.
5. Right-click the project in the Projects panel and select **Clean and Build**.
6. Right-click the project and select **Run** (or press `F6`). NetBeans will execute `com.cacheinspector.Main`.

---

## 6. How to Test and Verify

1. **Launch the application**:
   - The default configuration (3 frames, `1, 2, 3, 1, 4, 5, 2, 1, 2, 3, 4, 5`) automatically simulates upon launch.
2. **Examine the LRU Tab**:
   - Inspect the stat cards: `Total Hits: 2`, `Total Misses: 10`, `Evictions: 7`, `Hit Ratio: 16.7%`.
   - Click `▶ Play` to watch the step-by-step playback or click `Next Step ▶` to advance one step at a time.
   - Note how Frame slots highlight with Emerald Green on Step 4 and Step 9 (Hits).
3. **Examine the Clock Algorithm Tab**:
   - Observe the circular dial: the bright rose clock hand points to the active candidate frame.
   - Watch the reference bit badges toggle between `[1]` (green) and `[0]` (gray) during eviction sweeps.
   - Read the live step narrative to see how the clock hand grants second chances before choosing a victim.
4. **Examine the Comparison Tab**:
   - Inspect the grouped bar charts comparing Hits, Misses, and Evictions.
   - Observe the cumulative hit progression curve and read the database architectural analysis.
5. **Try Custom Inputs**:
   - Change buffer capacity to 4 or 5 frames and click `▶ Run Simulation`.
   - Select the `High Locality` or `Belady's Anomaly` preset from the dropdown.
   - Click `🎲 Randomize` to evaluate unpredictable random workloads.
