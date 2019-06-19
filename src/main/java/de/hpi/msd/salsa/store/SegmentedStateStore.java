package de.hpi.msd.salsa.store;

import com.google.common.collect.EvictingQueue;
import com.google.common.collect.Streams;
import de.hpi.msd.salsa.serde.avro.AdjacencyList;
import de.hpi.msd.salsa.store.index.ImmutableSegment;
import de.hpi.msd.salsa.store.index.MutableSegment;
import de.hpi.msd.salsa.store.index.SegmentFullException;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.StateStore;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SegmentedStateStore implements StateStore, EdgeWritableStateStore {
    private final boolean changelogEnabled;
    private final Map<String, String> logConfig;
    private final String name;
    private final int maxSegments;
    private final int maxEdgesPerSegment;
    private EvictingQueue<ImmutableSegment> immutableSegments;
    private MutableSegment mutableSegment;

    public SegmentedStateStore(boolean changelogEnabled,
                               Map<String, String> logConfig,
                               String name,
                               int maxSegments,
                               int maxEdgesPerSegment) {
        this.changelogEnabled = changelogEnabled;
        this.logConfig = logConfig;
        this.name = name;
        this.maxSegments = maxSegments;
        this.maxEdgesPerSegment = maxEdgesPerSegment;
    }

    @Override
    public void init(ProcessorContext processorContext, StateStore stateStore) {
        this.immutableSegments = EvictingQueue.create(maxSegments - 1);
        this.mutableSegment = new MutableSegment(maxEdgesPerSegment);
        processorContext.register(stateStore, (bytes, bytes1) -> {});
    }

    @Override
    public void write(long sourceId, long targetId, long edgeType) {
        try {
            mutableSegment.addEdge(sourceId, targetId, edgeType);
        } catch (SegmentFullException e) {
            ImmutableSegment immutableSegment = ImmutableSegment.fromSegment(mutableSegment);
            immutableSegments.add(immutableSegment);
            mutableSegment = new MutableSegment(maxEdgesPerSegment);
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
        return false;
    }
}
