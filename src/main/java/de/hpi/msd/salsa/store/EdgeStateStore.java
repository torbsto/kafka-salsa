package de.hpi.msd.salsa.store;

import de.hpi.msd.salsa.serde.avro.AdjacencyList;
import de.hpi.msd.salsa.store.index.AdjacencyStore;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.StateStore;

import java.util.Map;

public abstract class EdgeStateStore implements StateStore, EdgeWritableStateStore {
    private final boolean changelogEnabled;
    private final Map<String, String> logConfig;
    private final String name;
    private final int indexSize;
    protected AdjacencyStore adjacencyStore;

    public EdgeStateStore(boolean changelogEnabled,
                          Map<String, String> logConfig,
                          int indexSize,
                          String name) {
        this.changelogEnabled = changelogEnabled;
        this.logConfig = logConfig;
        this.name = name;
        this.indexSize = indexSize;
        this.adjacencyStore = new AdjacencyStore(indexSize);
    }


    @Override
    public AdjacencyList read(long key) {
        return new AdjacencyList(adjacencyStore.getTargetNodes(key));
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public void init(ProcessorContext processorContext, StateStore stateStore) {
        processorContext.register(stateStore, (bytes, bytes1) -> {});
    }

    @Override
    public void flush() {

    }

    @Override
    public void close() {

    }

    @Override
    public boolean persistent() {
        return false;
    }

    @Override
    public boolean isOpen() {
        return false;
    }
}
