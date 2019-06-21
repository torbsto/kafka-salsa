package de.hpi.msd.salsa.store;

import de.hpi.msd.salsa.serde.avro.AdjacencyList;

public interface EdgeReadableStateStore {
    AdjacencyList read(long sourceId);

    int getNodeDegree(long sourceId);
}
