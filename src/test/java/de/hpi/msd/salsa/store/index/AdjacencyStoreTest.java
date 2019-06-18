package de.hpi.msd.salsa.store.index;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

        long[] targetNodes = adjacencyStore.getTargetNodes(nodeId);
        Assertions.assertArrayEquals(targetNodes, new long[]{4, 5, 6});
    }

    @Test
    void shouldInsertManyNodes() {
        long nodeId = 3;
        int nodes = 300;
        long[] expectedNodes = new long[nodes];

        for (int targetNodeId = 0; targetNodeId < nodes; targetNodeId++) {
            adjacencyStore.addEdge(nodeId, targetNodeId, 1);
            expectedNodes[targetNodeId] = targetNodeId;
        }

        long[] targetNodes = adjacencyStore.getTargetNodes(nodeId);
        Assertions.assertArrayEquals(expectedNodes, targetNodes);
    }
}
