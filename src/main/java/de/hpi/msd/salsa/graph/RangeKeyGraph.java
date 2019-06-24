package de.hpi.msd.salsa.graph;

import de.hpi.msd.salsa.serde.avro.RangeKey;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

import java.util.ArrayList;
import java.util.List;

public class RangeKeyGraph extends KeyValueGraph {
    private ReadOnlyKeyValueStore<RangeKey, Long> leftIndex;
    private ReadOnlyKeyValueStore<RangeKey, Long> rightIndex;

    public RangeKeyGraph(ReadOnlyKeyValueStore<RangeKey, Long> leftIndex, ReadOnlyKeyValueStore<RangeKey, Long> rightIndex) {
        this.leftIndex = leftIndex;
        this.rightIndex = rightIndex;
    }

    @Override
    public List<Long> getLeftNodeNeighbors(long nodeId) {
        return getNeighbors(nodeId, leftIndex);
    }

    @Override
    public List<Long> getRightNodeNeighbors(long nodeId) {
        return getNeighbors(nodeId, rightIndex);
    }

    private List<Long> getNeighbors(long nodeId, ReadOnlyKeyValueStore<RangeKey, Long> index) {
        KeyValueIterator<RangeKey, Long> iterator = index.range(new RangeKey(nodeId, 0L), new RangeKey(nodeId + 1, 0L));
        List<Long> list = new ArrayList<>();
        while (iterator.hasNext() && iterator.peekNextKey().getNodeId() == nodeId) {
            list.add(iterator.next().value);
        }
        return list;
    }
}
