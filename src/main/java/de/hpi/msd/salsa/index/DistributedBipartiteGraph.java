package de.hpi.msd.salsa.index;

import de.hpi.msd.salsa.EdgeToAdjacencyApp;
import de.hpi.msd.salsa.serde.avro.AdjacencyList;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.state.HostInfo;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

import java.util.List;

public class DistributedBipartiteGraph implements BipartiteGraph {
    private final KafkaStreams streams;
    private final String leftIndexName;
    private final String rightIndexName;
    private final HostInfo hostInfo;
    private final KeyValueGraph internalGraph;

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
        return 0;
    }

    @Override
    public List<Long> getLeftNodeNeighbors(long nodeId) {
        HostInfo info = streams.metadataForKey(leftIndexName, nodeId, Serdes.Long().serializer()).hostInfo();

        if (!this.hostInfo.equals(info)) {

        } else {
            internalGraph.getLeftNodeNeighbors(nodeId);
        }


        return null;
    }

    @Override
    public int getRightNodeDegree(long nodeId) {
        return 0;
    }

    @Override
    public List<Long> getRightNodeNeighbors(long nodeId) {
        return null;
    }

}
