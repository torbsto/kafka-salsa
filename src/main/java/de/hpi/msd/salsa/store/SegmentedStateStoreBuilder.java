package de.hpi.msd.salsa.store;

import org.apache.kafka.streams.state.StoreBuilder;

import java.util.HashMap;
import java.util.Map;

public class SegmentedStateStoreBuilder implements StoreBuilder<SegmentedStateStore> {
    private final int maxSegments;
    private final int maxPoolsPerSegment;
    private final int maxNodesPerPool;
    private final String name;
    private final Map<String, String> logConfig;
    private boolean loggingEnabled;

    public SegmentedStateStoreBuilder(String name,
                                      int maxSegments,
                                      int maxPoolsPerSegment,
                                      int maxNodesPerPool) {
        this.name = name;
        this.maxSegments = maxSegments;
        this.maxPoolsPerSegment = maxPoolsPerSegment;
        this.maxNodesPerPool = maxNodesPerPool;
        this.logConfig = new HashMap<>();
    }

    @Override
    public StoreBuilder<SegmentedStateStore> withCachingEnabled() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public StoreBuilder<SegmentedStateStore> withCachingDisabled() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public StoreBuilder<SegmentedStateStore> withLoggingEnabled(Map<String, String> map) {
        loggingEnabled = true;
        logConfig.clear();
        logConfig.putAll(map);
        return this;
    }

    @Override
    public StoreBuilder<SegmentedStateStore> withLoggingDisabled() {
        loggingEnabled = false;
        logConfig.clear();
        return this;
    }


    @Override
    public SegmentedStateStore build() {
        return new SegmentedStateStore(loggingEnabled, logConfig, name, maxSegments, maxPoolsPerSegment, maxNodesPerPool);
    }

    @Override
    public Map<String, String> logConfig() {
        return logConfig;
    }

    @Override
    public boolean loggingEnabled() {
        return loggingEnabled;
    }

    @Override
    public String name() {
        return name;
    }
}
