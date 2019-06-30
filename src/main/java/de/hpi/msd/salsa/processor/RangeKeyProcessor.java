package de.hpi.msd.salsa.processor;

import de.hpi.msd.salsa.EdgeToAdjacencyApp;
import de.hpi.msd.salsa.serde.avro.Edge;
import de.hpi.msd.salsa.serde.avro.RangeKey;
import org.apache.kafka.streams.processor.AbstractProcessor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;

public class RangeKeyProcessor extends AbstractProcessor<byte[], Edge> {
    private KeyValueStore<RangeKey, Long> leftIndex;
    private KeyValueStore<RangeKey, Long> rightIndex;
    private KeyValueStore<Long, Long> leftPositionStore;
    private KeyValueStore<Long, Long> rightPositionStore;

    @Override
    public void init(ProcessorContext processorContext) {
        super.init(processorContext);
        leftIndex = (KeyValueStore<RangeKey, Long>) processorContext.getStateStore(EdgeToAdjacencyApp.LEFT_INDEX_NAME);
        rightIndex = (KeyValueStore<RangeKey, Long>) processorContext.getStateStore(EdgeToAdjacencyApp.RIGHT_INDEX_NAME);
        leftPositionStore = (KeyValueStore<Long, Long>) processorContext.getStateStore("leftPosition");
        rightPositionStore = (KeyValueStore<Long, Long>) processorContext.getStateStore("rightPosition");
    }

    @Override
    public void process(byte[] bytes, Edge edge) {
        Long leftPosition = leftPositionStore.get(edge.getUserId());
        Long rightPosition = rightPositionStore.get(edge.getTweedId());

        leftPosition = leftPosition == null ? 0 : leftPosition + 1;
        rightPosition = rightPosition == null ? 0 : rightPosition + 1;

        leftPositionStore.put(edge.getUserId(), leftPosition);
        rightPositionStore.put(edge.getTweedId(), rightPosition);

        RangeKey leftKey = new RangeKey(edge.getUserId(), leftPosition);
        RangeKey rightKey = new RangeKey(edge.getTweedId(), rightPosition);

        leftIndex.put(leftKey, edge.getTweedId());
        rightIndex.put(rightKey, edge.getUserId());

    }
}
