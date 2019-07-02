package de.hpi.msd.salsa.processor;

import de.hpi.msd.salsa.commands.SamplingApp;
import de.hpi.msd.salsa.serde.avro.Edge;
import de.hpi.msd.salsa.serde.avro.RangeKey;
import org.apache.kafka.streams.processor.AbstractProcessor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;

import java.util.concurrent.ThreadLocalRandom;

public class SamplingEdgeProcessor extends AbstractProcessor<byte[], Edge> {
    private KeyValueStore<RangeKey, Long> leftIndex;
    private KeyValueStore<RangeKey, Long> rightIndex;
    private KeyValueStore<Long, Long> leftCountStore;
    private KeyValueStore<Long, Long> rightCountStore;
    private int bufferSize;

    public SamplingEdgeProcessor(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    @Override
    public void init(ProcessorContext processorContext) {
        super.init(processorContext);
        leftIndex = (KeyValueStore<RangeKey, Long>) processorContext.getStateStore(SamplingApp.LEFT_INDEX_NAME);
        rightIndex = (KeyValueStore<RangeKey, Long>) processorContext.getStateStore(SamplingApp.RIGHT_INDEX_NAME);
        leftCountStore = (KeyValueStore<Long, Long>) processorContext.getStateStore(SamplingApp.LEFT_COUNT_STORE_NAME);
        rightCountStore = (KeyValueStore<Long, Long>) processorContext.getStateStore(SamplingApp.RIGHT_COUNT_STORE_NAME);
    }


    @Override
    public void process(byte[] bytes, Edge edge) {
        Long leftCount = leftCountStore.get(edge.getUserId());
        Long rightCount = rightCountStore.get(edge.getTweedId());

        reservoirSampling(edge.getUserId(), edge.getTweedId(), leftCount == null ? 1 : leftCount + 1, leftIndex, leftCountStore);
        reservoirSampling(edge.getTweedId(), edge.getUserId(), rightCount == null ? 1 : rightCount + 1, rightIndex, rightCountStore);
    }


    private void reservoirSampling(long nodeId, long value, long count, KeyValueStore<RangeKey, Long> index, KeyValueStore<Long, Long> countStore) {
        if (count > this.bufferSize) {
            long replaceIndex = ThreadLocalRandom.current().nextLong(1, count + 1);
            if (replaceIndex <= this.bufferSize) {
                RangeKey newKey = new RangeKey(nodeId, replaceIndex - 1);
                index.put(newKey, value);
            }
        } else {
            RangeKey newKey = new RangeKey(nodeId, count - 1);
            index.put(newKey, value);
        }
        countStore.put(nodeId, count);
    }

}
