package de.hpi.msd.salsa.store;

import org.apache.kafka.streams.state.StoreBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class UserStateStoreBuilder implements StoreBuilder<UserAdjacencyStore> {
    private final Logger logger = LoggerFactory.getLogger(UserStateStoreBuilder.class);
    private boolean loggingEnabled;
    private Map<String, String> logConfig;
    private int indexSize;
    private String name;

    public UserStateStoreBuilder(int size, String name) {
        this.indexSize = size;
        this.name = name;
        this.logConfig = new HashMap<>();
    }

    @Override
    public StoreBuilder<UserAdjacencyStore> withCachingEnabled() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public StoreBuilder<UserAdjacencyStore> withCachingDisabled() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public StoreBuilder<UserAdjacencyStore> withLoggingEnabled(Map<String, String> map) {
        loggingEnabled = true;
        logConfig.clear();
        logConfig.putAll(map);
        return this;
    }

    @Override
    public StoreBuilder<UserAdjacencyStore> withLoggingDisabled() {
        loggingEnabled = false;
        logConfig.clear();
        return this;
    }


    @Override
    public UserAdjacencyStore build() {
        return new UserAdjacencyStore(loggingEnabled, logConfig, indexSize, name);
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
