package com.cacheinspector.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Immutable snapshot of a single step in the buffer pool simulation.
 */
public class SimulationStep implements Serializable {
    private final int stepNumber;
    private final int pageReferenced;
    private final PageStatus status;
    private final Integer victimPage;
    private final int targetFrameIndex;
    private final List<BufferFrame> framesSnapshot;

    // Cumulative metrics up to this step
    private final int cumulativeHits;
    private final int cumulativeMisses;
    private final int cumulativeReplacements;
    private final double runningHitRatio;
    private final double runningMissRatio;

    // Clock-specific inspection data
    private final int clockPointerBefore;
    private final int clockPointerAfter;
    private final List<Integer> clockInspectedFrames;

    // Human-readable narrative explanation
    private final String explanation;

    public SimulationStep(
            int stepNumber,
            int pageReferenced,
            PageStatus status,
            Integer victimPage,
            int targetFrameIndex,
            List<BufferFrame> framesSnapshot,
            int cumulativeHits,
            int cumulativeMisses,
            int cumulativeReplacements,
            int clockPointerBefore,
            int clockPointerAfter,
            List<Integer> clockInspectedFrames,
            String explanation) {
        this.stepNumber = stepNumber;
        this.pageReferenced = pageReferenced;
        this.status = status;
        this.victimPage = victimPage;
        this.targetFrameIndex = targetFrameIndex;

        // Deep copy frames
        List<BufferFrame> copy = new ArrayList<>(framesSnapshot.size());
        for (BufferFrame f : framesSnapshot) {
            copy.add(f.clone());
        }
        this.framesSnapshot = Collections.unmodifiableList(copy);

        this.cumulativeHits = cumulativeHits;
        this.cumulativeMisses = cumulativeMisses;
        this.cumulativeReplacements = cumulativeReplacements;

        int total = cumulativeHits + cumulativeMisses;
        this.runningHitRatio = total > 0 ? ((double) cumulativeHits / total) * 100.0 : 0.0;
        this.runningMissRatio = total > 0 ? ((double) cumulativeMisses / total) * 100.0 : 0.0;

        this.clockPointerBefore = clockPointerBefore;
        this.clockPointerAfter = clockPointerAfter;
        this.clockInspectedFrames = clockInspectedFrames != null 
                ? Collections.unmodifiableList(new ArrayList<>(clockInspectedFrames)) 
                : Collections.emptyList();
        this.explanation = explanation;
    }

    public int getStepNumber() {
        return stepNumber;
    }

    public int getPageReferenced() {
        return pageReferenced;
    }

    public PageStatus getStatus() {
        return status;
    }

    public boolean isHit() {
        return status.isHit();
    }

    public boolean isReplacement() {
        return status.isReplacement();
    }

    public Integer getVictimPage() {
        return victimPage;
    }

    public int getTargetFrameIndex() {
        return targetFrameIndex;
    }

    public List<BufferFrame> getFramesSnapshot() {
        return framesSnapshot;
    }

    public int getCumulativeHits() {
        return cumulativeHits;
    }

    public int getCumulativeMisses() {
        return cumulativeMisses;
    }

    public int getCumulativeReplacements() {
        return cumulativeReplacements;
    }

    public double getRunningHitRatio() {
        return runningHitRatio;
    }

    public double getRunningMissRatio() {
        return runningMissRatio;
    }

    public int getClockPointerBefore() {
        return clockPointerBefore;
    }

    public int getClockPointerAfter() {
        return clockPointerAfter;
    }

    public List<Integer> getClockInspectedFrames() {
        return clockInspectedFrames;
    }

    public String getExplanation() {
        return explanation;
    }

    public String getBufferStateString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < framesSnapshot.size(); i++) {
            BufferFrame f = framesSnapshot.get(i);
            sb.append(f.isEmpty() ? "-" : f.getPageId());
            if (i < framesSnapshot.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
