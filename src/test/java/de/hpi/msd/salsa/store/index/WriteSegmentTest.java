package de.hpi.msd.salsa.store.index;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class WriteSegmentTest {
    private WriteSegment writeSegment;

    @BeforeEach
    void setUp() {
        writeSegment = new WriteSegment(1000);
    }

    @Test
    void shouldInsertNodes() {
        long nodeId = 2;

        writeSegment.addEdge(nodeId, 4, 2);
        writeSegment.addEdge(nodeId, 5, 2);
        writeSegment.addEdge(nodeId, 6, 2);

        List<Long> targetNodes = writeSegment.getTargetNodes(nodeId);
        Assertions.assertEquals(targetNodes, Arrays.asList(4L, 5L, 6L));
    }

    @Test
    void shouldInsertManyNodes() {
        long nodeId = 3;
        int nodes = 300;
        List<Long> expectedNodes = new ArrayList<>(nodes);

        for (long targetNodeId = 0; targetNodeId < nodes; targetNodeId++) {
            writeSegment.addEdge(nodeId, targetNodeId, 1);
            expectedNodes.add(targetNodeId);
        }

        List<Long> targetNodes = writeSegment.getTargetNodes(nodeId);
        Assertions.assertEquals(expectedNodes, targetNodes);
    }

    @Test
    void shouldInsertTargeNodesForDifferentSourceNodes() {
        final List<Long> expectedNodes = new ArrayList<>(100);

        for (long targetNodeId = 0; targetNodeId < 100; targetNodeId++) {
            expectedNodes.add(targetNodeId);
        }

        for (long sourceNodeId = 0; sourceNodeId < 10; sourceNodeId++) {
            for (long targetNodeId = 0; targetNodeId < 100; targetNodeId++) {
                writeSegment.addEdge(sourceNodeId, targetNodeId, 1);
            }
        }

        for (long sourceNodeId = 0; sourceNodeId < 10; sourceNodeId++) {
            final List<Long> targetNodes = writeSegment.getTargetNodes(sourceNodeId);
            Assertions.assertEquals(expectedNodes, targetNodes);
        }
    }
}
