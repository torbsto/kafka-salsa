package de.hpi.msd.salsa.store;

import de.hpi.msd.salsa.serde.avro.AdjacencyList;

public interface EdgeWritableStateStore extends EdgeReadableStateStore {
    void write(long key, AdjacencyList list);
}
