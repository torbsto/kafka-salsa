package de.hpi.msd.salsa.store.index;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class AdjacencyStoreTest {
    private AdjacencyStore adjacencyStore;

    @BeforeEach
    void setUp() {
        adjacencyStore = new AdjacencyStore(100);
    }

    @Test
    void shouldInsertNodes() {
        long nodeId = 2;

        adjacencyStore.addEdge(nodeId, 4, 2);
        adjacencyStore.addEdge(nodeId, 5, 2);
        adjacencyStore.addEdge(nodeId, 6, 2);

        List<Long> targetNodes = adjacencyStore.getTargetNodes(nodeId);
        Assertions.assertEquals(targetNodes, Arrays.asList(4L, 5L, 6L));
    }

    @Test
    void shouldInsertManyNodes() {
        long nodeId = 3;
        int nodes = 300;
        List<Long> expectedNodes = new ArrayList<>(nodes);

        for (long targetNodeId = 0; targetNodeId < nodes; targetNodeId++) {
            adjacencyStore.addEdge(nodeId, targetNodeId, 1);
            expectedNodes.add(targetNodeId);
        }

        List<Long> targetNodes = adjacencyStore.getTargetNodes(nodeId);
        Assertions.assertEquals(expectedNodes, targetNodes);
    }
}
