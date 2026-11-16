package com.cacheinspector.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Utility for parsing, validating, and generating page reference strings.
 */
public final class InputParser {

    private InputParser() {}

    /**
     * Parses a page reference string (comma, space, or hyphen-delimited) into a list of integers.
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
                if (pageId > 9999) {
                    throw new IllegalArgumentException("Page ID " + pageId + " is too large. Please use values <= 9999.");
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
