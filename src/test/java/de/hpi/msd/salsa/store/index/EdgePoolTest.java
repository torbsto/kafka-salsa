package de.hpi.msd.salsa.store.index;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EdgePoolTest {
    private static final int SLICES = 10;
    private static final int SLICE_SIZE = 4;
    private EdgePool edgePool;

    @BeforeEach
    void setUp() {
        this.edgePool = new EdgePool(SLICES, SLICE_SIZE);
    }

    @Test
    void shouldInsertEdgesAtSlicePositions() {
        edgePool.addToSlice(9, 0, 9);
        edgePool.addToSlice(9, 1, 8);
        edgePool.addToSlice(9, 2, 7);

        long[] slice = edgePool.getSlice(0);
        long[] expectedSlice = new long[]{9, 8, 7, 0};

        Assertions.assertArrayEquals(expectedSlice, slice);
    }

    @Test
    void throwIndexOutOfBoundsExceptionWhenExceedingSlices() {
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> edgePool.addToSlice(10, 0, 0));
    }
}