package de.hpi.msd.salsa.rest;

import de.hpi.msd.salsa.EdgeToAdjacencyApp;
import de.hpi.msd.salsa.index.BipartiteGraph;
import de.hpi.msd.salsa.index.KeyValueGraph;
import de.hpi.msd.salsa.serde.avro.AdjacencyList;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/state")
public class AdjacencyStateRestService implements BipartiteGraph {
    private final KeyValueGraph internalGraph;

    public AdjacencyStateRestService(KafkaStreams streams) {
        final ReadOnlyKeyValueStore<Long, AdjacencyList> leftIndex = streams.store(EdgeToAdjacencyApp.LEFT_INDEX_NAME, QueryableStoreTypes.keyValueStore());
        final ReadOnlyKeyValueStore<Long, AdjacencyList> rightIndex = streams.store(EdgeToAdjacencyApp.RIGHT_INDEX_NAME, QueryableStoreTypes.keyValueStore());
        internalGraph = new KeyValueGraph(leftIndex, rightIndex);
    }

    @Override
    @Path("/leftNode/{id}/degree")
    @Produces(MediaType.APPLICATION_JSON)
    public int getLeftNodeDegree(@PathParam("id") long nodeId) {
        return internalGraph.getLeftNodeDegree(nodeId);
    }

    @Override
    @Path("/leftNode/{id}/neighborhood")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Long> getLeftNodeNeighbors(@PathParam("id") long nodeId) {
        return internalGraph.getLeftNodeNeighbors(nodeId);
    }

    @Override
    @Path("/rightNode/{id}/degree")
    @Produces(MediaType.APPLICATION_JSON)
    public int getRightNodeDegree(@PathParam("id") long nodeId) {
        return internalGraph.getRightNodeDegree(nodeId);
    }

    @Override
    @Path("/rightNode/{id}/neighborhood")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Long> getRightNodeNeighbors(@PathParam("id") long nodeId) {
        return internalGraph.getRightNodeNeighbors(nodeId);
    }

}
