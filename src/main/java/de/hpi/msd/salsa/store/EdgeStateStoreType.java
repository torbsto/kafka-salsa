package de.hpi.msd.salsa.store;

import org.apache.kafka.streams.processor.StateStore;
import org.apache.kafka.streams.state.QueryableStoreType;
import org.apache.kafka.streams.state.internals.StateStoreProvider;

public class EdgeStateStoreType implements QueryableStoreType<EdgeReadableStateStore> {
    @Override
    public boolean accepts(StateStore stateStore) {
        return stateStore instanceof EdgeStateStore;
    }

    @Override
    public EdgeReadableStateStore create(StateStoreProvider stateStoreProvider, String storeName) {
        return new EdgeStateStoreTypeWrapper(this, storeName, stateStoreProvider);
    }
}
