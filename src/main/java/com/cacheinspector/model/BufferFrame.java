package com.cacheinspector.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a physical memory frame slot within the database buffer pool.
 */
public class BufferFrame implements Serializable, Cloneable {
    private final int frameIndex;
    private Integer pageId;
    private long lastAccessTimestamp;
    private int referenceBit; // 0 or 1 for Clock algorithm
    private boolean dirty;
    private int pinCount;

    // Transient visualization state flags for step rendering
    private boolean hitInCurrentStep;
    private boolean victimInCurrentStep;
    private boolean modifiedInCurrentStep;
    private boolean clockHandHere;

    public BufferFrame(int frameIndex) {
        this.frameIndex = frameIndex;
        this.pageId = null;
        this.lastAccessTimestamp = 0;
        this.referenceBit = 0;
        this.dirty = false;
        this.pinCount = 0;
    }

    public BufferFrame(BufferFrame other) {
        this.frameIndex = other.frameIndex;
        this.pageId = other.pageId;
        this.lastAccessTimestamp = other.lastAccessTimestamp;
        this.referenceBit = other.referenceBit;
        this.dirty = other.dirty;
        this.pinCount = other.pinCount;
        this.hitInCurrentStep = other.hitInCurrentStep;
        this.victimInCurrentStep = other.victimInCurrentStep;
        this.modifiedInCurrentStep = other.modifiedInCurrentStep;
        this.clockHandHere = other.clockHandHere;
    }

    public boolean isEmpty() {
        return pageId == null;
    }

    public int getFrameIndex() {
        return frameIndex;
    }

    public Integer getPageId() {
        return pageId;
    }

    public void setPageId(Integer pageId) {
        this.pageId = pageId;
    }

    public long getLastAccessTimestamp() {
        return lastAccessTimestamp;
    }

    public void setLastAccessTimestamp(long lastAccessTimestamp) {
        this.lastAccessTimestamp = lastAccessTimestamp;
    }

    public int getReferenceBit() {
        return referenceBit;
    }

    public void setReferenceBit(int referenceBit) {
        this.referenceBit = referenceBit;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public int getPinCount() {
        return pinCount;
    }

    public void setPinCount(int pinCount) {
        this.pinCount = pinCount;
    }

    public boolean isHitInCurrentStep() {
        return hitInCurrentStep;
    }

    public void setHitInCurrentStep(boolean hitInCurrentStep) {
        this.hitInCurrentStep = hitInCurrentStep;
    }

    public boolean isVictimInCurrentStep() {
        return victimInCurrentStep;
    }

    public void setVictimInCurrentStep(boolean victimInCurrentStep) {
        this.victimInCurrentStep = victimInCurrentStep;
    }

    public boolean isModifiedInCurrentStep() {
        return modifiedInCurrentStep;
    }

    public void setModifiedInCurrentStep(boolean modifiedInCurrentStep) {
        this.modifiedInCurrentStep = modifiedInCurrentStep;
    }

    public boolean isClockHandHere() {
        return clockHandHere;
    }

    public void setClockHandHere(boolean clockHandHere) {
        this.clockHandHere = clockHandHere;
    }

    public void clearStepFlags() {
        this.hitInCurrentStep = false;
        this.victimInCurrentStep = false;
        this.modifiedInCurrentStep = false;
        this.clockHandHere = false;
    }

    @Override
    public BufferFrame clone() {
        return new BufferFrame(this);
    }

    @Override
    public String toString() {
        return String.format("Frame#%d: %s (ref=%d, access=%d)",
                frameIndex,
                isEmpty() ? "EMPTY" : "P" + pageId,
                referenceBit,
                lastAccessTimestamp);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BufferFrame that = (BufferFrame) o;
        return frameIndex == that.frameIndex &&
                lastAccessTimestamp == that.lastAccessTimestamp &&
                referenceBit == that.referenceBit &&
                Objects.equals(pageId, that.pageId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(frameIndex, pageId, lastAccessTimestamp, referenceBit);
    }
}
