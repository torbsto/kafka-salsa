package de.hpi.msd.salsa.index;

import java.util.List;

public interface LeftIndexedBipartiteGraph {
    int getLeftNodeDegree(long nodeId);

    List<Long> getLeftNodeNeighbors(long nodeId);
}
