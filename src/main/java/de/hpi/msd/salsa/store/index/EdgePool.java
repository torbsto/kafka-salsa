package de.hpi.msd.salsa.store.index;

import java.util.Arrays;

public class EdgePool {
    private final int sliceSize;
    private final int numberOfSlices;
    private final long[] slices;
    private int currentSlice;

    public EdgePool(int numberOfSlices, int sliceSize) {
        this.numberOfSlices = numberOfSlices;
        this.sliceSize = sliceSize;
        this.slices = new long[numberOfSlices * sliceSize];
        this.currentSlice = -1;
    }

    public void addToSlice(long slice, int position, long encodedEdge) {
        if (slice >= numberOfSlices) {
            throw new IndexOutOfBoundsException("No slice at position " + slice);
        }

        slices[(int) (slice * sliceSize) + position] = encodedEdge;
    }

    public long[] getSlice(int position) {
        final int sliceStart = position * sliceSize;
        final int sliceEnd = sliceStart + sliceSize;
        return Arrays.copyOfRange(slices, sliceStart, sliceEnd);
    }

    public int nextFreeSliceIndex() throws SegmentFullException {
        currentSlice++;

        if (currentSlice >= numberOfSlices) {
            throw new SegmentFullException(String.format("Edge pool, slices %d slice size: %d  Reached max number of slices ", numberOfSlices, sliceSize));
        }

        return currentSlice;
    }
}
