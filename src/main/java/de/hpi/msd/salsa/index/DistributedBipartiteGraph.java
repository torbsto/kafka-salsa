package de.hpi.msd.salsa.index;

import de.hpi.msd.salsa.EdgeToAdjacencyApp;
import de.hpi.msd.salsa.serde.avro.AdjacencyList;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.state.HostInfo;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.glassfish.jersey.jackson.JacksonFeature;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import java.util.List;

public class DistributedBipartiteGraph implements BipartiteGraph {
    private final KafkaStreams streams;
    private final String leftIndexName;
    private final String rightIndexName;
    private final HostInfo hostInfo;
    private final KeyValueGraph internalGraph;
    protected final Client client = ClientBuilder.newBuilder().register(JacksonFeature.class).build();

    public DistributedBipartiteGraph(KafkaStreams streams, String leftIndexName, String rightIndexName, HostInfo hostInfo) {
        this.streams = streams;
        this.leftIndexName = leftIndexName;
        this.rightIndexName = rightIndexName;
        this.hostInfo = hostInfo;

        final ReadOnlyKeyValueStore<Long, AdjacencyList> leftIndex = streams.store(EdgeToAdjacencyApp.LEFT_INDEX_NAME, QueryableStoreTypes.keyValueStore());
        final ReadOnlyKeyValueStore<Long, AdjacencyList> rightIndex = streams.store(EdgeToAdjacencyApp.RIGHT_INDEX_NAME, QueryableStoreTypes.keyValueStore());
        internalGraph = new KeyValueGraph(leftIndex, rightIndex);
    }

    @Override
    public int getLeftNodeDegree(long nodeId) {
        return getLeftNodeNeighbors(nodeId).size();
    }

    @Override
    public List<Long> getLeftNodeNeighbors(long nodeId) {
        HostInfo info = streams.metadataForKey(leftIndexName, nodeId, Serdes.Long().serializer()).hostInfo();
        if (!this.hostInfo.equals(info)) {
            return client
                    .target(hostInfo.toString()).request("/leftNode/" + nodeId + "/neighborhood", MediaType.APPLICATION_JSON)
                    .get()
                    .readEntity(new GenericType<List<Long>>() {
                    });
        } else {
            return internalGraph.getLeftNodeNeighbors(nodeId);
        }
    }

    @Override
    public int getRightNodeDegree(long nodeId) {
        return getRightNodeNeighbors(nodeId).size();
    }

    @Override
    public List<Long> getRightNodeNeighbors(long nodeId) {
        HostInfo info = streams.metadataForKey(rightIndexName, nodeId, Serdes.Long().serializer()).hostInfo();
        if (!this.hostInfo.equals(info)) {
            return client
                    .target(hostInfo.toString()).request("/rightNode/" + nodeId + "/neighborhood", MediaType.APPLICATION_JSON)
                    .get()
                    .readEntity(new GenericType<List<Long>>() {
                    });
        } else {
            return internalGraph.getRightNodeNeighbors(nodeId);
        }
    }


}
