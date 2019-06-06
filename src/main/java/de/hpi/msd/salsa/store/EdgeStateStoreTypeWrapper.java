package de.hpi.msd.salsa.store;

import de.hpi.msd.salsa.serde.avro.AdjacencyList;
import org.apache.kafka.streams.state.QueryableStoreType;
import org.apache.kafka.streams.state.internals.StateStoreProvider;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class EdgeStateStoreTypeWrapper implements EdgeReadableStateStore {
    private final QueryableStoreType<EdgeReadableStateStore> storeType;
    private final String storeName;
    private final StateStoreProvider provider;

    public EdgeStateStoreTypeWrapper(QueryableStoreType<EdgeReadableStateStore> storeType, String storeName, StateStoreProvider provider) {
        this.storeType = storeType;
        this.storeName = storeName;
        this.provider = provider;
    }


    @Override
    public AdjacencyList read(long key) {
        final List<EdgeReadableStateStore> stores = provider.stores(storeName, storeType);
        final Optional<AdjacencyList> value = stores.stream().map(store -> store.read(key)).filter(Objects::nonNull).findFirst();
        return value.orElse(null);
    }
}
