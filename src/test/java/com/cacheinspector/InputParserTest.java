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

    @Test
    @DisplayName("Test real trace file parsing (.csv, .txt, .log)")
    public void testTraceFileParsing() throws Exception {
        java.io.File csvFile = new java.io.File("samples/ecommerce_oltp_workload.csv");
        assertTrue(csvFile.exists(), "Sample CSV trace file should exist");
        List<Integer> pages = InputParser.parseTraceFile(csvFile);
        assertFalse(pages.isEmpty(), "Parsed page list should not be empty");
        assertEquals(18, pages.size(), "Should have parsed 18 trace rows");
        assertEquals(1, pages.get(0), "First page should be 1");
    }

    @Test
    @DisplayName("Test physical disk file block workload mapping")
    public void testPhysicalFileBlockMapping() {
        java.io.File dbFile = new java.io.File("samples/customer_data.db");
        assertTrue(dbFile.exists(), "Sample DB physical file should exist");
        List<Integer> scanWorkload = InputParser.generateFileBlockWorkload(dbFile, 4096, "SCAN");
        assertEquals(16, scanWorkload.size(), "64KB file with 4KB pages should have 16 pages in sequential scan");
        for (int i = 0; i < 16; i++) {
            assertEquals(i, scanWorkload.get(i));
        }

        List<Integer> indexWorkload = InputParser.generateFileBlockWorkload(dbFile, 4096, "INDEX");
        assertFalse(indexWorkload.isEmpty(), "Index workload should produce page references");
    }
}
