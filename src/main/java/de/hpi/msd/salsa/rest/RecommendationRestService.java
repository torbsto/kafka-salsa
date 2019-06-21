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
    public List<Long> getRecommendationsForUser(@PathParam("userId") final long userId, @QueryParam("limit") final int limit) {
        return new Salsa(graph, new Random()).compute(userId, 1000, 100, 0.1, limit);
    }
}
