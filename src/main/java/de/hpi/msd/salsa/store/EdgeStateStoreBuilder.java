package de.hpi.msd.salsa.store;

import org.apache.kafka.streams.state.StoreBuilder;

import java.util.Map;

public class EdgeStateStoreBuilder implements StoreBuilder<EdgeStateStore> {
    @Override
    public StoreBuilder<EdgeStateStore> withCachingEnabled() {
        return null;
    }

    @Override
    public StoreBuilder<EdgeStateStore> withCachingDisabled() {
        return null;
    }

    @Override
    public StoreBuilder<EdgeStateStore> withLoggingEnabled(Map<String, String> map) {
        return null;
    }

    @Override
    public StoreBuilder<EdgeStateStore> withLoggingDisabled() {
        return null;
    }

    @Override
    public EdgeStateStore build() {
        return null;
    }

    @Override
    public Map<String, String> logConfig() {
        return null;
    }

    @Override
    public boolean loggingEnabled() {
        return false;
    }

    @Override
    public String name() {
        return null;
    }
}
