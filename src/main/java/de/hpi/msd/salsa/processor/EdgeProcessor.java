package de.hpi.msd.salsa.processor;


import de.hpi.msd.salsa.EdgeToAdjacencyApp;
import de.hpi.msd.salsa.serde.avro.AdjacencyList;
import de.hpi.msd.salsa.serde.avro.Edge;
import org.apache.kafka.streams.processor.AbstractProcessor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;

import java.util.Collections;

public class EdgeProcessor extends AbstractProcessor<byte[], Edge> {

    private KeyValueStore<Long, AdjacencyList> leftIndex;
    private KeyValueStore<Long, AdjacencyList> rightIndex;

    @Override
    public void init(ProcessorContext processorContext) {
        super.init(processorContext);
        leftIndex = (KeyValueStore<Long, AdjacencyList>) processorContext.getStateStore(EdgeToAdjacencyApp.LEFT_INDEX_NAME);
        rightIndex = (KeyValueStore<Long, AdjacencyList>) processorContext.getStateStore(EdgeToAdjacencyApp.RIGHT_INDEX_NAME);
    }

    @Override
    public void process(byte[] bytes, Edge edge) {
        AdjacencyList tweets = getAdjacencyList(edge.getUserId(), edge.getTweedId(), leftIndex);
        leftIndex.put(edge.getUserId(), tweets);

        for (Long tweetId : tweets.getNeighbors()) {
            rightIndex.put(tweetId, getAdjacencyList(tweetId, edge.getUserId(), rightIndex));
        }

        context().forward(edge.getUserId(), tweets);
    }

    private AdjacencyList getAdjacencyList(Long leftId, Long rightId, KeyValueStore<Long, AdjacencyList> index) {
        AdjacencyList currentNeighbors = index.get(leftId);
        if (currentNeighbors == null) {
            currentNeighbors = new AdjacencyList(Collections.singletonList(rightId));
        } else {
            currentNeighbors.getNeighbors().add(rightId);
        }
        return currentNeighbors;
    }


}
