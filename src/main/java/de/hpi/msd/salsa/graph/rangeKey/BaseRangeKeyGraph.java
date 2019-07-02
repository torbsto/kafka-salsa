package de.hpi.msd.salsa.graph.rangeKey;

import de.hpi.msd.salsa.graph.KeyValueGraph;
import de.hpi.msd.salsa.serde.avro.RangeKey;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

public abstract class BaseRangeKeyGraph extends KeyValueGraph {
    private ReadOnlyKeyValueStore<RangeKey, Long> leftIndex;
    private ReadOnlyKeyValueStore<RangeKey, Long> rightIndex;

    public BaseRangeKeyGraph(ReadOnlyKeyValueStore<RangeKey, Long> leftIndex,
                               ReadOnlyKeyValueStore<RangeKey, Long> rightIndex) {
        this.leftIndex = leftIndex;
        this.rightIndex = rightIndex;
    }

    public ReadOnlyKeyValueStore<RangeKey, Long> getLeftIndex() {
        return leftIndex;
    }

    public ReadOnlyKeyValueStore<RangeKey, Long> getRightIndex() {
        return rightIndex;
    }
}
