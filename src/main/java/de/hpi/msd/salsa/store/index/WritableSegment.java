package de.hpi.msd.salsa.store.index;

public interface WritableSegment {
    void addEdge(long source, long target, long edgeType) throws SegmentFullException;
}
