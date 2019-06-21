package de.hpi.msd.salsa.graph;

import de.hpi.msd.salsa.store.EdgeReadableStateStore;

import java.util.Collections;
import java.util.List;

public class SegmentedGraph implements BipartiteGraph {
    private final EdgeReadableStateStore leftIndex;
    private final EdgeReadableStateStore rightIndex;

    public SegmentedGraph(EdgeReadableStateStore leftIndex, EdgeReadableStateStore rightIndex) {
        this.leftIndex = leftIndex;
        this.rightIndex = rightIndex;
    }

    @Override
    public int getLeftNodeDegree(long nodeId) {
        return leftIndex.getNodeDegree(nodeId);
    }

    @Override
    public List<Long> getLeftNodeNeighbors(long nodeId) {
        return leftIndex.read(nodeId).getNeighbors();
    }

    @Override
    public List<Long> getLeftNodeNeighborSample(long nodeId, int size) {
        final List<Long> neighbors = getLeftNodeNeighbors(nodeId);
        Collections.shuffle(neighbors);
        return neighbors.subList(0, size);
    }

    @Override
    public int getRightNodeDegree(long nodeId) {
        return rightIndex.getNodeDegree(nodeId);
    }

    @Override
    public List<Long> getRightNodeNeighbors(long nodeId) {
        return rightIndex.read(nodeId).getNeighbors();
    }

    @Override
    public List<Long> getRightNodeNeighborSample(long nodeId, int size) {
        final List<Long> neighbors = getRightNodeNeighbors(nodeId);
        Collections.shuffle(neighbors);
        return neighbors.subList(0, size);
    }
}
