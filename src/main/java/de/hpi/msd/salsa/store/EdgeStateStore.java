package de.hpi.msd.salsa.store;

import de.hpi.msd.salsa.serde.avro.AdjacencyList;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.StateStore;

import java.util.Map;

public class EdgeStateStore implements StateStore, EdgeWritableStateStore {
    private final boolean changelogEnabled;
    private final Map<String, String> logConfig;
    private final int indexSize;
    private final String name;

    public EdgeStateStore(boolean changelogEnabled, Map<String, String> logConfig, int indexSize, String name) {
        this.changelogEnabled = changelogEnabled;
        this.logConfig = logConfig;
        this.indexSize = indexSize;
        this.name = name;
    }


    @Override
    public void write(long key, AdjacencyList list) {

    }

    @Override
    public AdjacencyList read(long key) {
        return null;
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