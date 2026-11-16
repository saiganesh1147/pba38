package com.cacheinspector.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * Encapsulates the complete execution results of a page replacement simulation.
 */
public class SimulationResult implements Serializable {
    private final AlgorithmType algorithmType;
    private final int bufferCapacity;
    private final List<Integer> pageSequence;
    private final List<SimulationStep> steps;
    private final int totalHits;
    private final int totalMisses;
    private final int totalReplacements;
    private final double hitPercentage;
    private final double missPercentage;

    public SimulationResult(
            AlgorithmType algorithmType,
            int bufferCapacity,
            List<Integer> pageSequence,
            List<SimulationStep> steps) {
        this.algorithmType = algorithmType;
        this.bufferCapacity = bufferCapacity;
        this.pageSequence = Collections.unmodifiableList(pageSequence);
        this.steps = Collections.unmodifiableList(steps);

        int hits = 0;
        int misses = 0;
        int replacements = 0;

        for (SimulationStep step : steps) {
            if (step.isHit()) {
                hits++;
            } else {
                misses++;
                if (step.isReplacement()) {
                    replacements++;
                }
            }
        }

        this.totalHits = hits;
        this.totalMisses = misses;
        this.totalReplacements = replacements;

        int total = hits + misses;
        this.hitPercentage = total > 0 ? ((double) hits / total) * 100.0 : 0.0;
        this.missPercentage = total > 0 ? ((double) misses / total) * 100.0 : 0.0;
    }

    public AlgorithmType getAlgorithmType() {
        return algorithmType;
    }

    public int getBufferCapacity() {
        return bufferCapacity;
    }

    public List<Integer> getPageSequence() {
        return pageSequence;
    }

    public List<SimulationStep> getSteps() {
        return steps;
    }

    public int getTotalReferences() {
        return pageSequence.size();
    }

    public int getTotalHits() {
        return totalHits;
    }

    public int getTotalMisses() {
        return totalMisses;
    }

    public int getTotalReplacements() {
        return totalReplacements;
    }

    public double getHitPercentage() {
        return hitPercentage;
    }

    public double getMissPercentage() {
        return missPercentage;
    }

    public List<BufferFrame> getFinalFrames() {
        if (steps.isEmpty()) {
            return Collections.emptyList();
        }
        return steps.get(steps.size() - 1).getFramesSnapshot();
    }

    public SimulationStep getStep(int index) {
        if (index < 0 || index >= steps.size()) {
            return null;
        }
        return steps.get(index);
    }
}
