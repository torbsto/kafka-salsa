package de.hpi.msd.salsa.processor;


import de.hpi.msd.salsa.EdgeToAdjacencyApp;
import de.hpi.msd.salsa.serde.avro.Edge;
import de.hpi.msd.salsa.store.TweetAdjacencyStore;
import de.hpi.msd.salsa.store.TweetStateStoreBuilder;
import de.hpi.msd.salsa.store.UserAdjacencyStore;
import org.apache.kafka.streams.processor.AbstractProcessor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EdgeProcessor extends AbstractProcessor<byte[], Edge> {
    private final Logger logger = LoggerFactory.getLogger(TweetStateStoreBuilder.class);
    private UserAdjacencyStore userStore;
    private TweetAdjacencyStore tweetStore;

    @Override
    public void init(ProcessorContext processorContext) {
        super.init(processorContext);
        userStore = (UserAdjacencyStore) processorContext.getStateStore(EdgeToAdjacencyApp.LEFT_INDEX_NAME);
        tweetStore = (TweetAdjacencyStore) processorContext.getStateStore(EdgeToAdjacencyApp.RIGHT_INDEX_NAME);
    }

    @Override
    public void process(byte[] bytes, Edge edge) {
        logger.debug("Processing: %s", edge);
        userStore.write(edge);
        tweetStore.write(edge);
    }
}
