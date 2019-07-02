package de.hpi.msd.salsa.graph;

import de.hpi.msd.salsa.serde.avro.RangeKey;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

import java.util.ArrayList;
import java.util.List;

public class SampleKeyValueGraph extends KeyValueGraph {
    private ReadOnlyKeyValueStore<RangeKey, Long> leftIndex;
    private ReadOnlyKeyValueStore<RangeKey, Long> rightIndex;
    private int bufferSize;

    public SampleKeyValueGraph(ReadOnlyKeyValueStore<RangeKey, Long> leftIndex,
                               ReadOnlyKeyValueStore<RangeKey, Long> rightIndex,
                               int bufferSize) {
        this.leftIndex = leftIndex;
        this.rightIndex = rightIndex;
        this.bufferSize = bufferSize;
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
        KeyValueIterator<RangeKey, Long> iterator = index.range(new RangeKey(nodeId, 0L), new RangeKey(nodeId, (long) bufferSize));
        List<Long> list = new ArrayList<>();
        iterator.forEachRemaining(k -> list.add(k.value));
        return list;
    }

}
