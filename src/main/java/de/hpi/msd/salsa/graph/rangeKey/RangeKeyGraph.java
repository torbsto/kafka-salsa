package de.hpi.msd.salsa.graph.rangeKey;

import de.hpi.msd.salsa.serde.avro.RangeKey;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

import java.util.ArrayList;
import java.util.List;

public class RangeKeyGraph extends BaseRangeKeyGraph {
    private ReadOnlyKeyValueStore<Long, Long> leftPositionStore;
    private ReadOnlyKeyValueStore<Long, Long> rightPositionStore;

    public RangeKeyGraph(ReadOnlyKeyValueStore<RangeKey, Long> leftIndex,
                         ReadOnlyKeyValueStore<RangeKey, Long> rightIndex,
                         ReadOnlyKeyValueStore<Long, Long> leftPositionStore,
                         ReadOnlyKeyValueStore<Long, Long> rightPositionStore) {
        super(leftIndex, rightIndex);
        this.leftPositionStore = leftPositionStore;
        this.rightPositionStore = rightPositionStore;
    }

    @Override
    public List<Long> getLeftNodeNeighbors(long nodeId) {
        return getNeighbors(nodeId, getLeftIndex(), leftPositionStore);
    }

    @Override
    public List<Long> getRightNodeNeighbors(long nodeId) {
        return getNeighbors(nodeId, getRightIndex(), rightPositionStore);
    }

    private List<Long> getNeighbors(long nodeId, ReadOnlyKeyValueStore<RangeKey, Long> index, ReadOnlyKeyValueStore<Long, Long> positionStore) {
        KeyValueIterator<RangeKey, Long> iterator = index.range(new RangeKey(nodeId, 0L), new RangeKey(nodeId, positionStore.get(nodeId)));
        List<Long> list = new ArrayList<>();
        while (iterator.hasNext() && iterator.peekNextKey().getNodeId() == nodeId) {
            list.add(iterator.next().value);
        }
        return list;
    }
}
