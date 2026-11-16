package com.cacheinspector.algorithm;

import com.cacheinspector.model.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the Clock (Second-Chance) page replacement algorithm.
 * Maintains a circular buffer of frames and a clock hand pointer. Each frame has
 * a reference bit (0 or 1). When looking for a replacement, the clock hand sweeps
 * frames: if a frame has reference bit 1, it is given a second chance (bit set to 0);
 * if it has reference bit 0 (or is empty), it is selected as the victim.
 */
public class ClockAlgorithm implements PageReplacementAlgorithm {

    @Override
    public AlgorithmType getAlgorithmType() {
        return AlgorithmType.CLOCK;
    }

    @Override
    public SimulationResult simulate(int capacity, List<Integer> pageSequence) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Buffer pool capacity must be at least 1.");
        }
        if (pageSequence == null || pageSequence.isEmpty()) {
            return new SimulationResult(AlgorithmType.CLOCK, capacity, new ArrayList<>(), new ArrayList<>());
        }

        List<BufferFrame> frames = new ArrayList<>(capacity);
        for (int i = 0; i < capacity; i++) {
            frames.add(new BufferFrame(i));
        }

        List<SimulationStep> steps = new ArrayList<>();
        int cumulativeHits = 0;
        int cumulativeMisses = 0;
        int cumulativeReplacements = 0;
        int clockPointer = 0; // Starts at frame index 0

        for (int stepIdx = 0; stepIdx < pageSequence.size(); stepIdx++) {
            int stepNumber = stepIdx + 1;
            int pageId = pageSequence.get(stepIdx);
            int pointerBefore = clockPointer;

            // Clear transient visualization flags from previous step
            for (BufferFrame f : frames) {
                f.clearStepFlags();
            }

            // 1. Check for Hit in the buffer pool
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
                int oldRefBit = hitFrame.getReferenceBit();
                hitFrame.setReferenceBit(1); // Set second-chance bit
                hitFrame.setLastAccessTimestamp(stepNumber);
                hitFrame.setHitInCurrentStep(true);
                hitFrame.setModifiedInCurrentStep(true);

                // Clock pointer stays at its current position on hit
                frames.get(clockPointer).setClockHandHere(true);

                String explanation = String.format(
                        "PAGE HIT: Page %d is already in Frame #%d. Reference bit set to 1 (was %d). Clock hand remains at Frame #%d.",
                        pageId, hitIndex, oldRefBit, clockPointer);

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
                        pointerBefore,
                        clockPointer,
                        List.of(hitIndex),
                        explanation));
            } else {
                // PAGE MISS
                cumulativeMisses++;
                List<Integer> inspectedFrames = new ArrayList<>();
                StringBuilder sweepLog = new StringBuilder();

                int victimIndex = -1;
                Integer victimPage = null;
                boolean isReplacement = false;

                // Circular search starting at clockPointer
                while (true) {
                    BufferFrame candidate = frames.get(clockPointer);
                    inspectedFrames.add(clockPointer);

                    if (candidate.isEmpty()) {
                        // Empty frame slot found
                        victimIndex = clockPointer;
                        sweepLog.append(String.format("Frame #%d is empty -> Selected for loading. ", clockPointer));
                        clockPointer = (clockPointer + 1) % capacity;
                        break;
                    } else if (candidate.getReferenceBit() == 0) {
                        // Candidate has bit 0 -> Victim found!
                        victimIndex = clockPointer;
                        victimPage = candidate.getPageId();
                        isReplacement = true;
                        sweepLog.append(String.format("Frame #%d (Page %d) has ref bit 0 -> EVICTED! ", clockPointer, victimPage));
                        clockPointer = (clockPointer + 1) % capacity;
                        break;
                    } else {
                        // Candidate has bit 1 -> Second chance given! Reset bit to 0 and advance hand
                        candidate.setReferenceBit(0);
                        sweepLog.append(String.format("Frame #%d (Page %d) had ref bit 1 -> reset to 0 (second chance granted); ",
                                clockPointer, candidate.getPageId()));
                        clockPointer = (clockPointer + 1) % capacity;
                    }
                }

                BufferFrame targetFrame = frames.get(victimIndex);
                targetFrame.setPageId(pageId);
                targetFrame.setReferenceBit(1); // Newly loaded page gets reference bit 1
                targetFrame.setLastAccessTimestamp(stepNumber);
                targetFrame.setModifiedInCurrentStep(true);

                if (isReplacement) {
                    cumulativeReplacements++;
                    targetFrame.setVictimInCurrentStep(true);
                }

                // Update clock pointer position flag for next step
                frames.get(clockPointer).setClockHandHere(true);

                String explanation;
                PageStatus status;
                if (isReplacement) {
                    status = PageStatus.MISS_REPLACE;
                    explanation = String.format(
                            "PAGE MISS & EVICTION: %sLoaded Page %d into Frame #%d with ref bit=1. Clock hand advanced to Frame #%d.",
                            sweepLog.toString(), pageId, victimIndex, clockPointer);
                } else {
                    status = PageStatus.MISS_EMPTY;
                    explanation = String.format(
                            "PAGE MISS (Warmup): %sLoaded Page %d into empty Frame #%d with ref bit=1. Clock hand advanced to Frame #%d.",
                            sweepLog.toString(), pageId, victimIndex, clockPointer);
                }

                steps.add(new SimulationStep(
                        stepNumber,
                        pageId,
                        status,
                        victimPage,
                        victimIndex,
                        frames,
                        cumulativeHits,
                        cumulativeMisses,
                        cumulativeReplacements,
                        pointerBefore,
                        clockPointer,
                        inspectedFrames,
                        explanation));
            }
        }

        return new SimulationResult(AlgorithmType.CLOCK, capacity, pageSequence, steps);
    }
}
