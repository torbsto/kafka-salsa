package de.hpi.msd.salsa.store;

import org.apache.kafka.streams.processor.StateStore;
import org.apache.kafka.streams.state.QueryableStoreType;
import org.apache.kafka.streams.state.internals.StateStoreProvider;

public class EdgeReadableStateStoreType implements QueryableStoreType<EdgeReadableStateStore> {
    @Override
    public boolean accepts(StateStore stateStore) {
        return stateStore instanceof EdgeReadableStateStore;
    }

    @Override
    public EdgeReadableStateStore create(StateStoreProvider stateStoreProvider, String storeName) {
        return new EdgeReadableStateStoreTypeWrapper(this, storeName, stateStoreProvider);
    }
}
