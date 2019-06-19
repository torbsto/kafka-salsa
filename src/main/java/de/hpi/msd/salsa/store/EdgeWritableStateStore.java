package de.hpi.msd.salsa.store;

public interface EdgeWritableStateStore extends EdgeReadableStateStore {
    void write(long sourceId, long targetId, long edgeType);
}
