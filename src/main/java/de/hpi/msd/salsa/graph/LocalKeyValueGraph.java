package de.hpi.msd.salsa.graph;

import de.hpi.msd.salsa.serde.avro.AdjacencyList;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

import java.util.List;

public class LocalKeyValueGraph extends KeyValueGraph {
    private ReadOnlyKeyValueStore<Long, AdjacencyList> leftIndex;
    private ReadOnlyKeyValueStore<Long, AdjacencyList> rightIndex;

    public LocalKeyValueGraph(ReadOnlyKeyValueStore<Long, AdjacencyList> leftIndex,
                              ReadOnlyKeyValueStore<Long, AdjacencyList> rightIndex) {
        this.leftIndex = leftIndex;
        this.rightIndex = rightIndex;
    }

    @Override
    public List<Long> getLeftNodeNeighbors(long nodeId) {
        return leftIndex.get(nodeId).getNeighbors();
    }


    @Override
    public List<Long> getRightNodeNeighbors(long nodeId) {
        return rightIndex.get(nodeId).getNeighbors();
    }
}
