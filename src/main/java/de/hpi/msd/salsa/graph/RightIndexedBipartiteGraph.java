package de.hpi.msd.salsa.graph;

import java.util.List;

public interface RightIndexedBipartiteGraph {
    int getRightNodeDegree(long nodeId);

    List<Long> getRightNodeNeighbors(long nodeId);

    List<Long> getRightNodeNeighborSample(long nodeId, int size);

}
