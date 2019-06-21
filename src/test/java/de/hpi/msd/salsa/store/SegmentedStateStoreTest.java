package de.hpi.msd.salsa.store;

import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.StateStore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

class SegmentedStateStoreTest {
    @Mock
    ProcessorContext processorContext;
    @Mock
    StateStore stateStore;

    SegmentedStateStore segmentedStateStore;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        segmentedStateStore = new SegmentedStateStore(false, null, "leftIndex", 25, 20, 131072);
        segmentedStateStore.init(processorContext, stateStore);
    }

    @Test
    void shouldInsertAndRetrieveAllNodesIfStoreHasEnoughSpace() {
        final List<Long> expectedNodes = new ArrayList<>(100);
        final int numberOfNodes = 20000;
        final int numberOfTargetNodesPerNode = 1000;

        for (long targetNodeId = 0; targetNodeId < numberOfTargetNodesPerNode; targetNodeId++) {
            expectedNodes.add(targetNodeId);
        }

        for (long sourceNodeId = 0; sourceNodeId < numberOfNodes; sourceNodeId++) {
            for (long targetNodeId = 0; targetNodeId < numberOfTargetNodesPerNode; targetNodeId++) {
                segmentedStateStore.write(sourceNodeId, targetNodeId, 1);
            }
        }

        for (long sourceNodeId = 0; sourceNodeId < numberOfNodes; sourceNodeId++) {
            final List<Long> targetNodes = segmentedStateStore.read(sourceNodeId).getNeighbors();
            // Results are not guaranteed to be sorted like the list of expected nodes.
            targetNodes.sort(Comparator.naturalOrder());
            Assertions.assertEquals(expectedNodes, targetNodes);
        }
    }
}