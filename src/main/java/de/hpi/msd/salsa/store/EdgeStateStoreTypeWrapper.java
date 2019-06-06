package de.hpi.msd.salsa.store;

import de.hpi.msd.salsa.serde.avro.AdjacencyList;

public class EdgeStateStoreTypeWrapper implements EdgeReadableStateStore {
    @Override
    public AdjacencyList read(long key) {
        return null;
    }
}
