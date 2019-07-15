package de.hpi.msd.salsa.rest;

import de.hpi.msd.salsa.algorithm.Salsa;
import de.hpi.msd.salsa.graph.BipartiteGraph;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Random;

@Path("/recommendation")
public class RecommendationRestService {
    private final BipartiteGraph graph;

    public RecommendationRestService(BipartiteGraph graph) {
        this.graph = graph;
    }

    @GET
    @Path("/salsa/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Long> getRecommendationsForUser(@PathParam("userId") final long userId,
                                                @DefaultValue("10") @QueryParam("limit") final int limit,
                                                @DefaultValue("1000") @QueryParam("walks") final int walks,
                                                @DefaultValue("100") @QueryParam("walkLength") final int walkLength,
                                                @DefaultValue("0.1") @QueryParam("resetProbability") final float resetProbability,
                                                @DefaultValue("42") @QueryParam("seed") final int seed) {
        return new Salsa(graph, new Random(seed)).compute(userId, walks, walkLength, resetProbability, limit);
    }
}
