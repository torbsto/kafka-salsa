package de.hpi.msd.salsa.processor;


import de.hpi.msd.salsa.EdgeToAdjacencyApp;
import de.hpi.msd.salsa.serde.avro.Edge;
import de.hpi.msd.salsa.store.SegmentedStateStore;
import de.hpi.msd.salsa.store.SegmentedStateStoreBuilder;
import org.apache.kafka.streams.processor.AbstractProcessor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EdgeProcessor extends AbstractProcessor<byte[], Edge> {
    private final Logger logger = LoggerFactory.getLogger(SegmentedStateStoreBuilder.class);
    private SegmentedStateStore leftIndex;
    private SegmentedStateStore rightIndex;

    @Override
    public void init(ProcessorContext processorContext) {
        super.init(processorContext);
        leftIndex = (SegmentedStateStore) processorContext.getStateStore(EdgeToAdjacencyApp.LEFT_INDEX_NAME);
        rightIndex = (SegmentedStateStore) processorContext.getStateStore(EdgeToAdjacencyApp.RIGHT_INDEX_NAME);
    }

    @Override
    public void process(byte[] bytes, Edge edge) {
        logger.debug("Processing: %s", edge);
        leftIndex.write(edge.getUserId(), edge.getTweedId(), edge.getEdgeType());
        rightIndex.write(edge.getTweedId(), edge.getUserId(), edge.getEdgeType());
    }
}
