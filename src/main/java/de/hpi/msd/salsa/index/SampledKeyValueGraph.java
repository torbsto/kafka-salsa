package de.hpi.msd.salsa.index;

import de.hpi.msd.salsa.serde.avro.AdjacencyList;
import de.hpi.msd.salsa.serde.avro.SampledAdjacencyList;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

import java.util.List;

public class SampledKeyValueGraph implements BipartiteGraph {
    private ReadOnlyKeyValueStore<Long, SampledAdjacencyList> leftIndex;
    private ReadOnlyKeyValueStore<Long, SampledAdjacencyList> rightIndex;

    public SampledKeyValueGraph(ReadOnlyKeyValueStore<Long, SampledAdjacencyList> leftIndex,
                                ReadOnlyKeyValueStore<Long, SampledAdjacencyList> rightIndex) {
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
