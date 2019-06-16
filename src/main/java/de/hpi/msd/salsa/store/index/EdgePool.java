package de.hpi.msd.salsa.store.index;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;

public class EdgePool {
    private final int sliceSize;
    private final int numberOfSlices;
    private final long[] slices;
    private int currentSlice;

    public EdgePool(int sliceCount, int sliceSize) {
        this.numberOfSlices = sliceCount;
        this.sliceSize = sliceSize;
        this.slices = new long[sliceCount * sliceSize];
    }

    public void addToSlice(long slice, int position, long encodedEdge) {
        if (slice > numberOfSlices) {
            throw new IndexOutOfBoundsException("No segment at position " + slice);
        }

        // We iterated into next slice.
        if (position - (slice * sliceSize) >= sliceSize) {
            throw new IndexOutOfBoundsException("Segment at position " + slice + "is full in this pool.");
        }

        slices[position] = encodedEdge;
    }

    public long[] getSlice(int position) {
        int i = position * sliceSize;
        return Arrays.copyOfRange(slices, i, i + sliceSize);
    }

    public int getNewSlicePosition() {
        currentSlice++;
        return currentSlice;
    }

    public int getSliceSize() {
        return sliceSize;
    }
}