package com.cacheinspector;

import com.cacheinspector.algorithm.ClockAlgorithm;
import com.cacheinspector.algorithm.LruAlgorithm;
import com.cacheinspector.model.AlgorithmType;
import com.cacheinspector.model.SimulationResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AlgorithmTest {

    private final List<Integer> sampleSequence = Arrays.asList(1, 2, 3, 1, 4, 5, 2, 1, 2, 3, 4, 5);
    private final int bufferCapacity = 3;

    @Test
    @DisplayName("Test LRU algorithm on standard sample sequence")
    public void testLruSample() {
        LruAlgorithm lru = new LruAlgorithm();
        SimulationResult result = lru.simulate(bufferCapacity, sampleSequence);

        assertEquals(AlgorithmType.LRU, result.getAlgorithmType());
        assertEquals(12, result.getTotalReferences());
        assertEquals(2, result.getTotalHits(), "LRU should produce 2 hits for sample sequence");
        assertEquals(10, result.getTotalMisses(), "LRU should produce 10 misses for sample sequence");
        assertEquals(7, result.getTotalReplacements(), "LRU should produce 7 replacements");

        double expectedHitPercentage = (2.0 / 12.0) * 100.0;
        assertEquals(expectedHitPercentage, result.getHitPercentage(), 0.01);

        // Check final buffer content
        // Final pages in buffer should be [5, 4, 3] or permutations depending on frame indices
        List<Integer> finalPages = result.getFinalFrames().stream()
                .map(f -> f.getPageId())
                .sorted()
                .toList();
        assertEquals(List.of(3, 4, 5), finalPages, "Final buffer should hold pages 3, 4, and 5");
    }

    @Test
    @DisplayName("Test Clock algorithm on standard sample sequence")
    public void testClockSample() {
        ClockAlgorithm clock = new ClockAlgorithm();
        SimulationResult result = clock.simulate(bufferCapacity, sampleSequence);

        assertEquals(AlgorithmType.CLOCK, result.getAlgorithmType());
        assertEquals(12, result.getTotalReferences());
        assertEquals(2, result.getTotalHits(), "Clock should produce 2 hits for sample sequence");
        assertEquals(10, result.getTotalMisses(), "Clock should produce 10 misses for sample sequence");
        assertEquals(7, result.getTotalReplacements(), "Clock should produce 7 replacements");

        double expectedHitPercentage = (2.0 / 12.0) * 100.0;
        assertEquals(expectedHitPercentage, result.getHitPercentage(), 0.01);

        List<Integer> finalPages = result.getFinalFrames().stream()
                .map(f -> f.getPageId())
                .sorted()
                .toList();
        assertEquals(List.of(3, 4, 5), finalPages, "Final buffer should hold pages 3, 4, and 5");
    }

    @Test
    @DisplayName("Test High Locality sequence")
    public void testHighLocality() {
        List<Integer> highLocality = Arrays.asList(1, 2, 1, 3, 1, 2, 1);
        LruAlgorithm lru = new LruAlgorithm();
        SimulationResult result = lru.simulate(3, highLocality);

        // 1 (miss), 2 (miss), 1 (hit), 3 (miss), 1 (hit), 2 (hit), 1 (hit) -> 4 hits, 3 misses
        assertEquals(4, result.getTotalHits());
        assertEquals(3, result.getTotalMisses());
        assertEquals(0, result.getTotalReplacements());
    }
}
