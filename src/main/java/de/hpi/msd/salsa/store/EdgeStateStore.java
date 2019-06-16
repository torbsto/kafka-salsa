package de.hpi.msd.salsa.store;

import de.hpi.msd.salsa.serde.avro.AdjacencyList;
import de.hpi.msd.salsa.store.index.AdjacencyStore;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.StateStore;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public abstract class EdgeStateStore implements StateStore, EdgeWritableStateStore {
    private final boolean changelogEnabled;
    private final Map<String, String> logConfig;
    private final String name;

    protected final AdjacencyStore adjacencyStore;

    public EdgeStateStore(boolean changelogEnabled, Map<String, String> logConfig, int indexSize, String name) {
        this.changelogEnabled = changelogEnabled;
        this.logConfig = logConfig;
        this.name = name;
        this.adjacencyStore = new AdjacencyStore(indexSize);
    }


    @Override
    public AdjacencyList read(long key) {
        return new AdjacencyList(LongStream.of(adjacencyStore.getRightNodes(key)).boxed().collect(Collectors.toList()));
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public void init(ProcessorContext processorContext, StateStore stateStore) {
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
