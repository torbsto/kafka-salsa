package de.hpi.msd.salsa.store;

import de.hpi.msd.salsa.serde.avro.Edge;

import java.util.Map;

public class TweetAdjacencyStore extends EdgeStateStore {
    public TweetAdjacencyStore(boolean changelogEnabled, Map<String, String> logConfig, int indexSize, String name) {
        super(changelogEnabled, logConfig, indexSize, name);
    }

    @Override
    public void write(Edge edge) {
        writeSegment.addEdge(edge.getTweedId(), edge.getUserId(), edge.getEdgeType());
    }
}
