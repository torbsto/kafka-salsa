package de.hpi.msd.salsa.index;

import de.hpi.msd.salsa.serde.avro.Edge;

import java.util.List;

public interface RightIndexedBipartiteGraph {
    int getRightNodeDegree(long nodeId);

    List<Long> getRightNodeNeighbors(long nodeId);

}
