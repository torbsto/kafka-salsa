package de.hpi.msd.salsa.processor;


import de.hpi.msd.salsa.serde.avro.AdjacencyList;
import de.hpi.msd.salsa.serde.avro.Edge;
import org.apache.kafka.streams.processor.AbstractProcessor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;

import java.util.Collections;

public class EdgeProcessor extends AbstractProcessor<byte[], Edge> {
    private String leftIndexName;
    private String rightIndexName;

    private KeyValueStore<Long, AdjacencyList> leftIndex;
    private KeyValueStore<Long, AdjacencyList> rightIndex;

    public EdgeProcessor(String leftIndexName, String rightIndexName) {
        this.leftIndexName = leftIndexName;
        this.rightIndexName = rightIndexName;
    }

    @Override
    public void init(ProcessorContext processorContext) {
        super.init(processorContext);
        leftIndex = (KeyValueStore<Long, AdjacencyList>) processorContext.getStateStore(leftIndexName);
        rightIndex = (KeyValueStore<Long, AdjacencyList>) processorContext.getStateStore(rightIndexName);
    }

    @Override
    public void process(byte[] bytes, Edge edge) {
        AdjacencyList tweets = leftIndex.get(edge.getUserId());

        if (tweets == null) {
            tweets = new AdjacencyList(Collections.singletonList(edge.getTweedId()));
            leftIndex.put(edge.getUserId(), tweets);
        } else {
            tweets.getNeighbors().add(edge.getTweedId());
            leftIndex.put(edge.getUserId(), tweets);
        }

        for (Long tweetId : tweets.getNeighbors()) {
            AdjacencyList currentNeighbors = rightIndex.get(tweetId);
            currentNeighbors.getNeighbors().add(edge.getUserId());
            rightIndex.put(tweetId, currentNeighbors);
        }

        context().forward(edge.getUserId(), tweets);
    }


}
