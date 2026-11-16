package com.cacheinspector.algorithm;

import com.cacheinspector.model.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the Least Recently Used (LRU) page replacement algorithm.
 * Tracks access timestamps of pages in buffer frames and replaces the frame
 * whose page has the oldest (least recent) access timestamp when the pool is full.
 */
public class LruAlgorithm implements PageReplacementAlgorithm {

    @Override
    public AlgorithmType getAlgorithmType() {
        return AlgorithmType.LRU;
    }

    @Override
    public SimulationResult simulate(int capacity, List<Integer> pageSequence) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Buffer pool capacity must be at least 1.");
        }
        if (pageSequence == null || pageSequence.isEmpty()) {
            return new SimulationResult(AlgorithmType.LRU, capacity, new ArrayList<>(), new ArrayList<>());
        }

        List<BufferFrame> frames = new ArrayList<>(capacity);
        for (int i = 0; i < capacity; i++) {
            frames.add(new BufferFrame(i));
        }

        List<SimulationStep> steps = new ArrayList<>();
        int cumulativeHits = 0;
        int cumulativeMisses = 0;
        int cumulativeReplacements = 0;

        for (int stepIdx = 0; stepIdx < pageSequence.size(); stepIdx++) {
            int stepNumber = stepIdx + 1;
            int pageId = pageSequence.get(stepIdx);

            // Clear transient visualization flags from previous step
            for (BufferFrame f : frames) {
                f.clearStepFlags();
            }

            // 1. Check for Hit
            int hitIndex = -1;
            for (int i = 0; i < capacity; i++) {
                if (!frames.get(i).isEmpty() && frames.get(i).getPageId() == pageId) {
                    hitIndex = i;
                    break;
                }
            }

            if (hitIndex != -1) {
                // PAGE HIT
                cumulativeHits++;
                BufferFrame hitFrame = frames.get(hitIndex);
                long previousTime = hitFrame.getLastAccessTimestamp();
                hitFrame.setLastAccessTimestamp(stepNumber);
                hitFrame.setHitInCurrentStep(true);
                hitFrame.setModifiedInCurrentStep(true);

                String explanation = String.format(
                        "PAGE HIT: Page %d is already cached in Frame #%d. Access timestamp updated from t=%d to t=%d.",
                        pageId, hitIndex, previousTime, stepNumber);

                steps.add(new SimulationStep(
                        stepNumber,
                        pageId,
                        PageStatus.HIT,
                        null,
                        hitIndex,
                        frames,
                        cumulativeHits,
                        cumulativeMisses,
                        cumulativeReplacements,
                        -1,
                        -1,
                        null,
                        explanation));
            } else {
                // PAGE MISS
                cumulativeMisses++;

                // Check for free frame
                int freeIndex = -1;
                for (int i = 0; i < capacity; i++) {
                    if (frames.get(i).isEmpty()) {
                        freeIndex = i;
                        break;
                    }
                }

                if (freeIndex != -1) {
                    // Buffer has empty slot -> No replacement
                    BufferFrame frame = frames.get(freeIndex);
                    frame.setPageId(pageId);
                    frame.setLastAccessTimestamp(stepNumber);
                    frame.setModifiedInCurrentStep(true);

                    String explanation = String.format(
                            "PAGE MISS (Cache Warmup): Page %d not in buffer pool. Placed into empty Frame #%d at t=%d.",
                            pageId, freeIndex, stepNumber);

                    steps.add(new SimulationStep(
                            stepNumber,
                            pageId,
                            PageStatus.MISS_EMPTY,
                            null,
                            freeIndex,
                            frames,
                            cumulativeHits,
                            cumulativeMisses,
                            cumulativeReplacements,
                            -1,
                            -1,
                            null,
                            explanation));
                } else {
                    // Buffer pool full -> Evict LRU page
                    cumulativeReplacements++;
                    int lruIndex = 0;
                    long oldestTimestamp = frames.get(0).getLastAccessTimestamp();

                    for (int i = 1; i < capacity; i++) {
                        if (frames.get(i).getLastAccessTimestamp() < oldestTimestamp) {
                            oldestTimestamp = frames.get(i).getLastAccessTimestamp();
                            lruIndex = i;
                        }
                    }

                    BufferFrame victimFrame = frames.get(lruIndex);
                    int victimPage = victimFrame.getPageId();

                    victimFrame.setPageId(pageId);
                    victimFrame.setLastAccessTimestamp(stepNumber);
                    victimFrame.setVictimInCurrentStep(true);
                    victimFrame.setModifiedInCurrentStep(true);

                    String explanation = String.format(
                            "PAGE MISS & EVICTION: Buffer full. Evicted LRU Page %d from Frame #%d (last accessed at t=%d). Loaded Page %d into Frame #%d.",
                            victimPage, lruIndex, oldestTimestamp, pageId, lruIndex);

                    steps.add(new SimulationStep(
                            stepNumber,
                            pageId,
                            PageStatus.MISS_REPLACE,
                            victimPage,
                            lruIndex,
                            frames,
                            cumulativeHits,
                            cumulativeMisses,
                            cumulativeReplacements,
                            -1,
                            -1,
                            null,
                            explanation));
                }
            }
        }

        return new SimulationResult(AlgorithmType.LRU, capacity, pageSequence, steps);
    }
}
