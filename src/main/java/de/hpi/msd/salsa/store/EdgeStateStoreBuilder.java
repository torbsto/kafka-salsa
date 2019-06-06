package de.hpi.msd.salsa.store;

import org.apache.kafka.streams.state.StoreBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class EdgeStateStoreBuilder implements StoreBuilder<EdgeStateStore> {
    private final Logger logger = LoggerFactory.getLogger(EdgeStateStoreBuilder.class);
    private boolean changelogEnabled;
    private Map<String, String> logConfig;
    private int indexSize;
    private String name;

    public EdgeStateStoreBuilder(int size, String name) {
        indexSize = size;
        this.name = name;
    }

    @Override
    public StoreBuilder<EdgeStateStore> withCachingEnabled() {
        logger.warn("Caching not supported");
        return this;
    }

    @Override
    public StoreBuilder<EdgeStateStore> withCachingDisabled() {
        return this;
    }

    @Override
    public StoreBuilder<EdgeStateStore> withLoggingEnabled(Map<String, String> map) {
        changelogEnabled = true;
        logConfig = map;
        return this;
    }

    @Override
    public StoreBuilder<EdgeStateStore> withLoggingDisabled() {
        changelogEnabled = false;
        return this;
    }


    @Override
    public EdgeStateStore build() {
        return new EdgeStateStore(changelogEnabled, logConfig, indexSize, name);
    }

    @Override
    public Map<String, String> logConfig() {
        return logConfig;
    }

    @Override
    public boolean loggingEnabled() {
        return changelogEnabled;
    }

    @Override
    public String name() {
        return name;
    }
}
