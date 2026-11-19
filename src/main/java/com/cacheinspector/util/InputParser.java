package com.cacheinspector.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Utility for parsing, validating, and generating page reference strings,
 * including reading from real trace files (.csv, .txt, .log) and mapping
 * physical files into database page blocks.
 */
public final class InputParser {

    public static final int DEFAULT_PAGE_SIZE_BYTES = 4096; // Standard 4 KB database page

    private InputParser() {}

    /**
     * Parses a page reference string (comma, semicolon, or space-delimited) into a list of integers.
     *
     * @param input raw input string from user
     * @return validated list of page IDs
     * @throws IllegalArgumentException if the input is empty or contains non-numeric tokens
     */
    public static List<Integer> parseSequence(String input) {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException("Page reference sequence cannot be empty.");
        }

        // Normalize delimiters (commas, semicolons, and whitespace) to single space
        String normalized = input.replaceAll("[,;\\s]+", " ").trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("No valid page numbers found in the reference string.");
        }

        String[] tokens = normalized.split(" ");
        List<Integer> sequence = new ArrayList<>(tokens.length);

        for (String token : tokens) {
            token = token.trim();
            if (token.isEmpty()) continue;
            try {
                int pageId = Integer.parseInt(token);
                if (pageId < 0) {
                    throw new IllegalArgumentException("Page IDs must be non-negative integers. Found: " + pageId);
                }
                if (pageId > 99999) {
                    throw new IllegalArgumentException("Page ID " + pageId + " is too large. Please use values <= 99999.");
                }
                sequence.add(pageId);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid token '" + token + "'. Only numeric page IDs are allowed.");
            }
        }

        if (sequence.isEmpty()) {
            throw new IllegalArgumentException("Page reference sequence must contain at least one page ID.");
        }

        return sequence;
    }

    /**
     * Reads a real-world workload trace file (.txt, .csv, .log) and extracts page IDs.
     * Handles both single-line and multi-line files, CSV headers, and column extraction.
     *
     * @param file the trace file on disk
     * @return extracted list of page IDs
     * @throws IOException if the file cannot be read
     * @throws IllegalArgumentException if no valid page references are found
     */
    public static List<Integer> parseTraceFile(File file) throws IOException {
        if (file == null || !file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("Selected file does not exist or is not a valid file.");
        }

        StringBuilder contentBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int pageColumnIndex = -1;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#") || line.startsWith("//")) {
                    continue; // Skip comments and empty lines
                }

                // Check for CSV header on the first non-comment line
                if (firstLine && line.contains(",")) {
                    String[] headers = line.split(",");
                    for (int i = 0; i < headers.length; i++) {
                        String h = headers[i].trim().toLowerCase();
                        if (h.contains("page") || h.contains("block") || h.equals("id") || h.equals("page_id")) {
                            pageColumnIndex = i;
                            break;
                        }
                    }
                    if (pageColumnIndex != -1) {
                        firstLine = false;
                        continue; // Header consumed
                    }
                }
                firstLine = false;

                // If a CSV page column was detected, extract that specific column
                if (pageColumnIndex != -1 && line.contains(",")) {
                    String[] parts = line.split(",");
                    if (pageColumnIndex < parts.length) {
                        contentBuilder.append(parts[pageColumnIndex].trim()).append(" ");
                    }
                } else {
                    contentBuilder.append(line).append(" ");
                }
            }
        }

        return parseSequence(contentBuilder.toString());
    }

    /**
     * Converts a real physical file on disk (e.g. database file, PDF, binary, text)
     * into a database page block workload by dividing its size into 4KB page chunks
     * and generating realistic query accesses (Sequential Scan, Index Lookup, Point Queries).
     *
     * @param file physical file on disk
     * @param pageSizeBytes block size (typically 4096 bytes)
     * @param queryPattern "SCAN" (sequential scan), "INDEX" (B-tree traversal simulation), or "MIXED"
     * @return generated page reference sequence reflecting actual file blocks
     */
    public static List<Integer> generateFileBlockWorkload(File file, int pageSizeBytes, String queryPattern) {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("Target file does not exist.");
        }

        long fileSizeBytes = file.length();
        int totalPages = (int) Math.ceil((double) fileSizeBytes / pageSizeBytes);
        if (totalPages <= 0) {
            totalPages = 1;
        }
        // Limit page range for clean simulation visualization
        int effectivePages = Math.min(totalPages, 50);

        List<Integer> sequence = new ArrayList<>();
        Random rng = new Random();

        if ("SCAN".equalsIgnoreCase(queryPattern)) {
            // Sequential table scan over physical file blocks
            for (int p = 0; p < effectivePages; p++) {
                sequence.add(p);
            }
        } else if ("INDEX".equalsIgnoreCase(queryPattern)) {
            // Simulates B+Tree index lookups: Root (Page 0) -> Branch (Page 1..3) -> Leaf (Page 4..N)
            for (int q = 0; q < 12; q++) {
                sequence.add(0); // Root page accessed on every query
                int branch = 1 + rng.nextInt(Math.min(3, Math.max(1, effectivePages - 1)));
                sequence.add(branch);
                int leaf = 4 + rng.nextInt(Math.max(1, effectivePages - 4));
                sequence.add(leaf % effectivePages);
            }
        } else {
            // Mixed realistic database workload (80/20 Pareto principle)
            int hotPages = Math.max(1, (int) (effectivePages * 0.20));
            for (int i = 0; i < 16; i++) {
                if (rng.nextDouble() < 0.80) {
                    // 80% of queries hit the hot 20% pages
                    sequence.add(rng.nextInt(hotPages));
                } else {
                    sequence.add(rng.nextInt(effectivePages));
                }
            }
        }

        return sequence;
    }

    /**
     * Generates a random page reference string with realistic spatial and temporal locality.
     *
     * @param length number of references to generate (e.g. 12 to 20)
     * @param maxUniquePages distinct page range (e.g. 1 to 7)
     * @return comma-separated sequence string
     */
    public static String generateRandomSequence(int length, int maxUniquePages) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        int lastPage = 1;

        for (int i = 0; i < length; i++) {
            int page;
            // 40% chance of repeating a recent page (locality)
            if (i > 0 && random.nextDouble() < 0.40) {
                page = Math.max(1, lastPage + (random.nextInt(3) - 1));
                if (page > maxUniquePages) page = maxUniquePages;
            } else {
                page = random.nextInt(maxUniquePages) + 1;
            }
            lastPage = page;

            sb.append(page);
            if (i < length - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }
}
