package de.hpi.msd.salsa.processor;

import de.hpi.msd.salsa.EdgeToAdjacencyApp;
import de.hpi.msd.salsa.serde.avro.Edge;
import de.hpi.msd.salsa.serde.avro.SampledAdjacencyList;
import org.apache.kafka.streams.processor.AbstractProcessor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;

import java.util.Collections;
import java.util.Random;

public class SamplingEdgeProcessor extends AbstractProcessor<byte[], Edge> {
    private KeyValueStore<Long, SampledAdjacencyList> leftIndex;
    private KeyValueStore<Long, SampledAdjacencyList> rightIndex;

    private int bufferSize;

    public SamplingEdgeProcessor(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    @Override
    public void init(ProcessorContext context) {
        super.init(context);
        leftIndex = (KeyValueStore<Long, SampledAdjacencyList>) context.getStateStore(EdgeToAdjacencyApp.LEFT_INDEX_NAME);
        rightIndex = (KeyValueStore<Long, SampledAdjacencyList>) context.getStateStore(EdgeToAdjacencyApp.RIGHT_INDEX_NAME);
    }


    @Override
    public void process(byte[] bytes, Edge edge) {
        SampledAdjacencyList tweets = getAdjacencyList(edge.getUserId(), edge.getTweedId(), leftIndex);
        leftIndex.put(edge.getUserId(), tweets);
        for (Long tweetId : tweets.getNeighbors()) {
            rightIndex.put(tweetId, getAdjacencyList(tweetId, edge.getUserId(), rightIndex));
        }

        context().forward(edge.getUserId(), tweets);
    }

    private SampledAdjacencyList resorvoirSampling(SampledAdjacencyList list, Long newElement) {
        if (list.getNeighbors().size() >= this.bufferSize) {
            int replaceIndex = new Random().nextInt(list.getCount());
            if (replaceIndex < this.bufferSize) {
                list.getNeighbors().set(replaceIndex, newElement);
            }

        } else {
            list.getNeighbors().add(newElement);
            list.setCount(list.getCount() + 1);
        }

        return list;
    }

    private SampledAdjacencyList getAdjacencyList(Long tweetId, Long userId, KeyValueStore<Long, SampledAdjacencyList> index) {
        SampledAdjacencyList currentNeighbors = index.get(tweetId);
        if (currentNeighbors == null) {
            currentNeighbors = new SampledAdjacencyList(Collections.singletonList(userId), 1);
        } else {
            currentNeighbors.getNeighbors().add(userId);
        }
        return currentNeighbors;
    }
}
