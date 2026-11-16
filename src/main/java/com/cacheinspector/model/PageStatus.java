package com.cacheinspector.model;

/**
 * Outcome status of a page reference operation in the buffer pool.
 */
public enum PageStatus {
    HIT("HIT", "Page was already resident in buffer pool memory.", true, false),
    MISS_EMPTY("MISS (Loaded)", "Page was not resident; loaded into an empty buffer frame.", false, false),
    MISS_REPLACE("MISS & REPLACE", "Buffer pool was full; a resident page was evicted to make room.", false, true);

    private final String label;
    private final String description;
    private final boolean hit;
    private final boolean replacement;

    PageStatus(String label, String description, boolean hit, boolean replacement) {
        this.label = label;
        this.description = description;
        this.hit = hit;
        this.replacement = replacement;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public boolean isHit() {
        return hit;
    }

    public boolean isReplacement() {
        return replacement;
    }

    @Override
    public String toString() {
        return label;
    }
}
