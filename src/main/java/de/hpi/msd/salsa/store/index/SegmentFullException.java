package de.hpi.msd.salsa.store.index;

public final class SegmentFullException extends IndexOutOfBoundsException {
    public SegmentFullException() {
    }

    public SegmentFullException(String s) {
        super(s);
    }
}
