package de.hpi.msd.salsa.processor;

import de.hpi.msd.salsa.EdgeToAdjacencyApp;
import de.hpi.msd.salsa.serde.avro.Edge;
import de.hpi.msd.salsa.serde.RangeKey;
import org.apache.kafka.streams.processor.AbstractProcessor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;

public class RangeKeyProcessor extends AbstractProcessor<byte[], Edge> {
    private KeyValueStore<RangeKey, Long> leftIndex;
    private KeyValueStore<RangeKey, Long> rightIndex;

    @Override
    public void init(ProcessorContext processorContext) {
        super.init(processorContext);
        leftIndex = (KeyValueStore<RangeKey, Long>) processorContext.getStateStore(EdgeToAdjacencyApp.LEFT_INDEX_NAME);
        rightIndex = (KeyValueStore<RangeKey, Long>) processorContext.getStateStore(EdgeToAdjacencyApp.RIGHT_INDEX_NAME);
    }


    @Override
    public void process(byte[] bytes, Edge edge) {
        RangeKey leftKey = new RangeKey(edge.getUserId(), getPosition(edge.getUserId(), leftIndex));
        RangeKey rightKey = new RangeKey(edge.getTweedId(), getPosition(edge.getTweedId(), rightIndex));

        leftIndex.put(leftKey, edge.getTweedId());
        rightIndex.put(rightKey, edge.getUserId());
    }

    private long getPosition(long nodeId, KeyValueStore<RangeKey, Long> index) {
        long position = 0L;
        RangeKey from = new RangeKey(nodeId, 0L);
        RangeKey to = new RangeKey(nodeId + 1, 0L);

        KeyValueIterator<RangeKey, Long> iterator = index.range(from, to);
        while (iterator.hasNext() && iterator.next().key.getNodeId() == nodeId) {
            position++;
        }
        return position;
    }


}
