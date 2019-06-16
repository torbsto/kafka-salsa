package de.hpi.msd.salsa.store;

import de.hpi.msd.salsa.serde.avro.Edge;

public interface EdgeWritableStateStore extends EdgeReadableStateStore {
    void write(Edge edge);
}
