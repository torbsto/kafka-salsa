package de.hpi.msd.salsa.rest;

import de.hpi.msd.salsa.EdgeToAdjacencyApp;
import de.hpi.msd.salsa.graph.BipartiteGraph;
import de.hpi.msd.salsa.graph.KeyValueGraph;
import de.hpi.msd.salsa.graph.LocalKeyValueGraph;
import de.hpi.msd.salsa.serde.avro.AdjacencyList;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/state")
public class AdjacencyStateRestService implements BipartiteGraph {
    private final KeyValueGraph internalGraph;

    public AdjacencyStateRestService(KafkaStreams streams) {
        final ReadOnlyKeyValueStore<Long, AdjacencyList> leftIndex = streams.store(EdgeToAdjacencyApp.LEFT_INDEX_NAME, QueryableStoreTypes.keyValueStore());
        final ReadOnlyKeyValueStore<Long, AdjacencyList> rightIndex = streams.store(EdgeToAdjacencyApp.RIGHT_INDEX_NAME, QueryableStoreTypes.keyValueStore());
        internalGraph = new LocalKeyValueGraph(leftIndex, rightIndex);
    }

    @Override
    @GET
    @Path("/leftNode/{id}/degree")
    @Produces(MediaType.APPLICATION_JSON)
    public int getLeftNodeDegree(@PathParam("id") long nodeId) {
        return internalGraph.getLeftNodeDegree(nodeId);
    }

    @Override
    @GET
    @Path("/leftNode/{id}/neighborhood")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Long> getLeftNodeNeighbors(@PathParam("id") long nodeId) {
        return internalGraph.getLeftNodeNeighbors(nodeId);
    }

    @Override
    @GET
    @Path("/rightNode/{id}/degree")
    @Produces(MediaType.APPLICATION_JSON)
    public int getRightNodeDegree(@PathParam("id") long nodeId) {
        return internalGraph.getRightNodeDegree(nodeId);
    }

    @Override
    @GET
    @Path("/rightNode/{id}/neighborhood")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Long> getRightNodeNeighbors(@PathParam("id") long nodeId) {
        return internalGraph.getRightNodeNeighbors(nodeId);
    }

    @Override
    @GET
    @Path("/leftNode/{id}/neighborhood/sample")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Long> getLeftNodeNeighborSample(@PathParam("id") long nodeId, @QueryParam("size") int size) {
        return internalGraph.getLeftNodeNeighborSample(nodeId, size);
    }

    @Override
    @GET
    @Path("/rightNode/{id}/neighborhood/sample")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Long> getRightNodeNeighborSample(@PathParam("id") long nodeId, @QueryParam("size") int size) {
        return internalGraph.getRightNodeNeighborSample(nodeId, size);
    }
}
