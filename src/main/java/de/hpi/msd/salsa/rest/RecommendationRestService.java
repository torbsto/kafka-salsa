package de.hpi.msd.salsa.rest;

import de.hpi.msd.salsa.EdgeToAdjacencyApp;
import de.hpi.msd.salsa.algorithm.Salsa;
import de.hpi.msd.salsa.index.KeyValueGraph;
import de.hpi.msd.salsa.serde.avro.AdjacencyList;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.state.HostInfo;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Random;

@Path("/recommendation")
public class RecommendationRestService {
    private final KeyValueGraph internalGraph;

    public RecommendationRestService(KafkaStreams streams) {
        internalGraph = new KeyValueGraph(streams.store(EdgeToAdjacencyApp.LEFT_INDEX_NAME, QueryableStoreTypes.keyValueStore()),
                streams.store(EdgeToAdjacencyApp.RIGHT_INDEX_NAME, QueryableStoreTypes.keyValueStore()));
    }

    @GET
    @Path("/salsa/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Long> getRecommendationsForUser(@PathParam("userId") final long userId, @QueryParam("limit") final int limit) {
        return new Salsa(internalGraph, new Random()).compute(userId, 1000, 100, 0.1, limit);
    }


}
