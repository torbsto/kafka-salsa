package de.hpi.msd.salsa.processor;


import de.hpi.msd.salsa.serde.avro.AdjacencyList;
import de.hpi.msd.salsa.serde.avro.Edge;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.processor.AbstractProcessor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.Stores;

public class EdgeProcessor extends AbstractProcessor<byte[], Edge> {
    private SpecificAvroSerde<AdjacencyList> adjacencyListSerde;
    private KeyValueStore<Long, AdjacencyList> leftIndex;
    private KeyValueStore<Long, AdjacencyList> rightIndex;


    @Override
    public void init(ProcessorContext processorContext) {
        super.init(processorContext);
        adjacencyListSerde = new SpecificAvroSerde<>();
        leftIndex = createAdjacencyStore("leftToRightIndex");
        rightIndex = createAdjacencyStore("rightToLeftIndex");
    }

    @Override
    public void process(byte[] bytes, Edge edge) {
        AdjacencyList tweets = leftIndex.get(edge.getUserId());
        tweets.getNeighbors().add(edge.getTweedId());

        rightIndex.put(edge.getUserId(), tweets);

        for (Long tweetId : tweets.getNeighbors()) {
            AdjacencyList currentNeighbors = rightIndex.get(tweetId);
            currentNeighbors.getNeighbors().add(edge.getUserId());
            rightIndex.put(tweetId, currentNeighbors);
        }
    }

    @Override
    public void close() {
        leftIndex.close();
        rightIndex.close();
    }


    private KeyValueStore<Long, AdjacencyList> createAdjacencyStore(String name) {
        return Stores.keyValueStoreBuilder(
                Stores.persistentKeyValueStore(name),
                Serdes.Long(),
                adjacencyListSerde
        ).build();
    }

}
