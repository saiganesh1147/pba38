package com.cacheinspector.model;

/**
 * Enumeration representing supported page replacement algorithms.
 */
public enum AlgorithmType {
    LRU("Least Recently Used (LRU)", "Tracks the order of page references and evicts the least recently accessed page."),
    CLOCK("Clock (Second Chance)", "Uses a circular buffer and reference bits to give pages a second chance before eviction.");

    private final String displayName;
    private final String description;

    AlgorithmType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
