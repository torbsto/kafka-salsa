package de.hpi.msd.salsa.rest;

import de.hpi.msd.salsa.graph.BipartiteGraph;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/state")
public class AdjacencyStateRestService implements BipartiteGraph {
    private final BipartiteGraph graph;

    public AdjacencyStateRestService(BipartiteGraph graph) {
        this.graph = graph;
    }

    @Override
    @GET
    @Path("/leftNode/{id}/degree")
    @Produces(MediaType.APPLICATION_JSON)
    public int getLeftNodeDegree(@PathParam("id") long nodeId) {
        return graph.getLeftNodeDegree(nodeId);
    }

    @Override
    @GET
    @Path("/leftNode/{id}/neighborhood")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Long> getLeftNodeNeighbors(@PathParam("id") long nodeId) {
        return graph.getLeftNodeNeighbors(nodeId);
    }

    @Override
    @GET
    @Path("/rightNode/{id}/degree")
    @Produces(MediaType.APPLICATION_JSON)
    public int getRightNodeDegree(@PathParam("id") long nodeId) {
        return graph.getRightNodeDegree(nodeId);
    }

    @Override
    @GET
    @Path("/rightNode/{id}/neighborhood")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Long> getRightNodeNeighbors(@PathParam("id") long nodeId) {
        return graph.getRightNodeNeighbors(nodeId);
    }

    @Override
    @GET
    @Path("/leftNode/{id}/neighborhood/sample")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Long> getLeftNodeNeighborSample(@PathParam("id") long nodeId, @QueryParam("size") int size) {
        return graph.getLeftNodeNeighborSample(nodeId, size);
    }

    @Override
    @GET
    @Path("/rightNode/{id}/neighborhood/sample")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Long> getRightNodeNeighborSample(@PathParam("id") long nodeId, @QueryParam("size") int size) {
        return graph.getRightNodeNeighborSample(nodeId, size);
    }
}
