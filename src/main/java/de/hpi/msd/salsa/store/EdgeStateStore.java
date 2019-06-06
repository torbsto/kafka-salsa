package de.hpi.msd.salsa.store;

import de.hpi.msd.salsa.serde.avro.AdjacencyList;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.StateStore;

public class EdgeStateStore implements StateStore, EdgeWritableStateStore {
    @Override
    public void write(long key, AdjacencyList list) {

    }

    @Override
    public AdjacencyList read(long key) {
        return null;
    }

    @Override
    public String name() {
        return null;
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
