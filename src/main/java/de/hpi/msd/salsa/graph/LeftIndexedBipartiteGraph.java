package de.hpi.msd.salsa.graph;

import java.util.List;

public interface LeftIndexedBipartiteGraph {
    int getLeftNodeDegree(long nodeId);

    List<Long> getLeftNodeNeighbors(long nodeId);
}
