package de.hpi.msd.salsa.graph;

import de.hpi.msd.salsa.serde.avro.SampledAdjacencyList;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

import java.util.List;

public class SampledKeyValueGraph extends KeyValueGraph {
    private ReadOnlyKeyValueStore<Long, SampledAdjacencyList> leftIndex;
    private ReadOnlyKeyValueStore<Long, SampledAdjacencyList> rightIndex;

    public SampledKeyValueGraph(ReadOnlyKeyValueStore<Long, SampledAdjacencyList> leftIndex,
                                ReadOnlyKeyValueStore<Long, SampledAdjacencyList> rightIndex) {
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
