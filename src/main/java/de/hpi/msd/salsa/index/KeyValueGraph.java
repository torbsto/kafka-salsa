package de.hpi.msd.salsa.index;

import de.hpi.msd.salsa.serde.avro.AdjacencyList;
import org.apache.kafka.streams.state.KeyValueStore;

import java.util.List;

public class KeyValueGraph implements BipartiteGraph {
    private KeyValueStore<Long, AdjacencyList> leftIndex;
    private KeyValueStore<Long, AdjacencyList> rightIndex;

    public KeyValueGraph(KeyValueStore<Long, AdjacencyList> leftIndex, KeyValueStore<Long, AdjacencyList> rightIndex) {
        this.leftIndex = leftIndex;
        this.rightIndex = rightIndex;
    }

    @Override
    public int getLeftNodeDegree(long nodeId) {
        return leftIndex.get(nodeId).getNeighbors().size();
    }

    @Override
    public List<Long> getLeftNodeNeighbors(long nodeId) {
        return leftIndex.get(nodeId).getNeighbors();
    }

    @Override
    public int getRightNodeDegree(long nodeId) {
        return rightIndex.get(nodeId).getNeighbors().size();
    }

    @Override
    public List<Long> getRightNodeNeighbors(long nodeId) {
        return rightIndex.get(nodeId).getNeighbors();
    }
}
