package com.cacheinspector.algorithm;

import com.cacheinspector.model.AlgorithmType;
import com.cacheinspector.model.SimulationResult;

import java.util.List;

/**
 * Common contract for buffer pool page replacement algorithms.
 */
public interface PageReplacementAlgorithm {
    /**
     * Executes the simulation on the given page sequence with the specified buffer capacity.
     *
     * @param capacity the number of buffer frames in the pool (e.g., 3, 4, 5)
     * @param pageSequence the list of requested page IDs
     * @return complete SimulationResult containing all step snapshots and performance metrics
     */
    SimulationResult simulate(int capacity, List<Integer> pageSequence);

    /**
     * Returns the algorithm type.
     */
    AlgorithmType getAlgorithmType();
}
