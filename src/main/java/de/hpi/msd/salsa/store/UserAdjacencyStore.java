package de.hpi.msd.salsa.store;

import de.hpi.msd.salsa.serde.avro.Edge;

import java.util.Map;

public class UserAdjacencyStore extends EdgeStateStore {

    public UserAdjacencyStore(boolean changelogEnabled, Map<String, String> logConfig, int indexSize, String name) {
        super(changelogEnabled, logConfig, indexSize, name);
    }

    @Override
    public void write(Edge edge) {
        adjacencyStore.addEdge(edge.getUserId(), edge.getTweedId(), edge.getEdgeType().byteValue());
    }
}
