package de.hpi.msd.salsa.store;

import org.apache.kafka.streams.state.StoreBuilder;

import java.util.HashMap;
import java.util.Map;

public class SegmentedStateStoreBuilder implements StoreBuilder<SegmentedStateStore> {
    private final int maxSegments;
    private final int maxEdgesPerSegment;
    private final String name;
    private final Map<String, String> logConfig;
    private boolean loggingEnabled;

    public SegmentedStateStoreBuilder(int maxSegments, int maxEdgesPerSegment, String name) {
        this.maxSegments = maxSegments;
        this.maxEdgesPerSegment = maxEdgesPerSegment;
        this.name = name;
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
        return new SegmentedStateStore(loggingEnabled, logConfig, name, maxSegments, maxEdgesPerSegment);
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
