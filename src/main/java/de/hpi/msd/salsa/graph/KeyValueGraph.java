package de.hpi.msd.salsa.graph;

import java.util.Collections;
import java.util.List;

public abstract class KeyValueGraph implements BipartiteGraph {

    @Override
    public int getLeftNodeDegree(long nodeId) {
        return getLeftNodeNeighbors(nodeId).size();
    }

    @Override
    public int getRightNodeDegree(long nodeId) {
        return getRightNodeNeighbors(nodeId).size();
    }

    @Override
    public List<Long> getLeftNodeNeighborSample(long nodeId, int size) {
        List<Long> neighbors = getLeftNodeNeighbors(nodeId);
        Collections.shuffle(neighbors);
        return neighbors.subList(0, size);
    }

    @Override
    public List<Long> getRightNodeNeighborSample(long nodeId, int size) {
        List<Long> neighbors = getRightNodeNeighbors(nodeId);
        Collections.shuffle(neighbors);
        return neighbors.subList(0, size);
    }
}
