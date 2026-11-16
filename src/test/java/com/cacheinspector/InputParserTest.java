package com.cacheinspector;

import com.cacheinspector.util.InputParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InputParserTest {

    @Test
    @DisplayName("Test parsing valid page sequences in various formats")
    public void testValidSequences() {
        // Comma separated
        List<Integer> seq1 = InputParser.parseSequence("1, 2, 3, 1, 4, 5");
        assertEquals(List.of(1, 2, 3, 1, 4, 5), seq1);

        // Space separated
        List<Integer> seq2 = InputParser.parseSequence("7 0 1 2 0 3 0 4");
        assertEquals(List.of(7, 0, 1, 2, 0, 3, 0, 4), seq2);

        // Mixed delimiters and extra whitespace
        List<Integer> seq3 = InputParser.parseSequence("  1, 2; 3 4 , 5  ");
        assertEquals(List.of(1, 2, 3, 4, 5), seq3);
    }

    @Test
    @DisplayName("Test invalid input error handling")
    public void testInvalidSequences() {
        assertThrows(IllegalArgumentException.class, () -> InputParser.parseSequence(""));
        assertThrows(IllegalArgumentException.class, () -> InputParser.parseSequence("   "));
        assertThrows(IllegalArgumentException.class, () -> InputParser.parseSequence("1, 2, abc, 4"));
        assertThrows(IllegalArgumentException.class, () -> InputParser.parseSequence("1, -5, 3"));
        assertThrows(IllegalArgumentException.class, () -> InputParser.parseSequence("1, 99999999"));
    }

    @Test
    @DisplayName("Test random sequence generation")
    public void testRandomSequence() {
        String randomStr = InputParser.generateRandomSequence(15, 5);
        assertNotNull(randomStr);
        List<Integer> parsed = InputParser.parseSequence(randomStr);
        assertEquals(15, parsed.size());
        for (int p : parsed) {
            assertTrue(p >= 1 && p <= 5);
        }
    }
}
