package de.hpi.msd.salsa.store;

import com.google.common.collect.EvictingQueue;
import com.google.common.collect.Streams;
import de.hpi.msd.salsa.serde.avro.AdjacencyList;
import de.hpi.msd.salsa.store.index.ImmutableSegment;
import de.hpi.msd.salsa.store.index.MutableSegment;
import de.hpi.msd.salsa.store.index.SegmentFullException;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.StateStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SegmentedStateStore implements StateStore, EdgeWritableStateStore {
    private final Logger logger = LoggerFactory.getLogger(SegmentedStateStore.class);
    private final boolean changelogEnabled;
    private final Map<String, String> logConfig;
    private final String name;
    private final int maxSegments;
    private final int maxImmutableSegments;
    private final int maxPoolsPerSegment;
    private final int maxNodesPerPool;
    private EvictingQueue<ImmutableSegment> immutableSegments;
    private MutableSegment mutableSegment;

    public SegmentedStateStore(boolean changelogEnabled,
                               Map<String, String> logConfig,
                               String name,
                               int maxSegments,
                               int maxPoolsPerSegment,
                               int maxNodesPerPool) {
        this.changelogEnabled = changelogEnabled;
        this.logConfig = logConfig;
        this.name = name;
        this.maxSegments = maxSegments;
        this.maxImmutableSegments = maxSegments - 1;
        this.maxPoolsPerSegment = maxPoolsPerSegment;
        this.maxNodesPerPool = maxNodesPerPool;
    }

    @Override
    public void init(ProcessorContext processorContext, StateStore stateStore) {
        this.immutableSegments = EvictingQueue.create(maxImmutableSegments);
        this.mutableSegment = new MutableSegment(maxPoolsPerSegment, maxNodesPerPool);
        processorContext.register(stateStore, (bytes, bytes1) -> {
        });
    }

    @Override
    public void write(long sourceId, long targetId, long edgeType) {
        try {
            mutableSegment.addEdge(sourceId, targetId, edgeType);
        } catch (SegmentFullException e) {
            // TODO: Process on background thread
            logger.info(String.format("Segment: %d is full: %s", immutableSegments.size() + 1, e.getMessage()));
            ImmutableSegment immutableSegment = ImmutableSegment.fromSegment(mutableSegment);
            immutableSegments.add(immutableSegment);
            mutableSegment = new MutableSegment(maxPoolsPerSegment, maxNodesPerPool);
            mutableSegment.addEdge(sourceId, targetId, edgeType);
        }
    }

    @Override
    public AdjacencyList read(long source) {
        final List<Long> targetNodes = Streams.concat(Stream.of(mutableSegment), immutableSegments.stream())
                .flatMap(segment -> segment.getTargetNodes(source).stream())
                .collect(Collectors.toList());

        return new AdjacencyList(targetNodes);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public void flush() {

    }

    @Override
    public void close() {

    }

    @Override
    public boolean persistent() {
        return false;
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    public int getNodeDegree(long source) {
        return Streams.concat(Stream.of(mutableSegment), immutableSegments.stream())
                .mapToInt(segment -> segment.getCardinality(source))
                .sum();
    }
}
