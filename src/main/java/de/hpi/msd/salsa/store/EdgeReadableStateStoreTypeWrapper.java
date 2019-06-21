package de.hpi.msd.salsa.store;

import de.hpi.msd.salsa.serde.avro.AdjacencyList;
import org.apache.kafka.streams.state.QueryableStoreType;
import org.apache.kafka.streams.state.internals.StateStoreProvider;

import java.util.List;
import java.util.stream.Collectors;

public class EdgeReadableStateStoreTypeWrapper implements EdgeReadableStateStore {
    private final QueryableStoreType<EdgeReadableStateStore> storeType;
    private final String storeName;
    private final StateStoreProvider provider;

    public EdgeReadableStateStoreTypeWrapper(QueryableStoreType<EdgeReadableStateStore> storeType,
                                             String storeName,
                                             StateStoreProvider provider) {
        this.storeType = storeType;
        this.storeName = storeName;
        this.provider = provider;
    }

    @Override
    public AdjacencyList read(long sourceId) {
        List<Long> neighbours = provider.stores(storeName, storeType).stream()
                .flatMap(stateStore -> stateStore.read(sourceId).getNeighbors().stream())
                .collect(Collectors.toList());
        return new AdjacencyList(neighbours);
    }

    @Override
    public int getNodeDegree(long sourceId) {
        final List<EdgeReadableStateStore> stores = provider.stores(storeName, storeType);
        return stores.stream().mapToInt(store -> store.getNodeDegree(sourceId)).sum();
    }
}
