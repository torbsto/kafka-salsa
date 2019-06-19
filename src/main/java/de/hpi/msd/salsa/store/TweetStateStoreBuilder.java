package de.hpi.msd.salsa.store;

import org.apache.kafka.streams.state.StoreBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class TweetStateStoreBuilder implements StoreBuilder<TweetAdjacencyStore> {
    private final Logger logger = LoggerFactory.getLogger(TweetStateStoreBuilder.class);
    private boolean loggingEnabled;
    private Map<String, String> logConfig;
    private int indexSize;
    private String name;

    public TweetStateStoreBuilder(int size, String name) {
        this.indexSize = size;
        this.name = name;
        this.logConfig = new HashMap<>();
    }

    @Override
    public StoreBuilder<TweetAdjacencyStore> withCachingEnabled() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public StoreBuilder<TweetAdjacencyStore> withCachingDisabled() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public StoreBuilder<TweetAdjacencyStore> withLoggingEnabled(Map<String, String> map) {
        loggingEnabled = true;
        logConfig.clear();
        logConfig.putAll(map);
        return this;
    }

    @Override
    public StoreBuilder<TweetAdjacencyStore> withLoggingDisabled() {
        loggingEnabled = false;
        logConfig.clear();
        return this;
    }


    @Override
    public TweetAdjacencyStore build() {
        return new TweetAdjacencyStore(loggingEnabled, logConfig, indexSize, name);
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
