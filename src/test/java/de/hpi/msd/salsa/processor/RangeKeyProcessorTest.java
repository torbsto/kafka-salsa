package de.hpi.msd.salsa.processor;

import com.bakdata.fluent_kafka_streams_tests.TestInput;
import com.bakdata.fluent_kafka_streams_tests.junit5.TestTopologyExtension;
import de.hpi.msd.salsa.commands.RangeKeyApp;
import de.hpi.msd.salsa.graph.rangeKey.RangeKeyGraph;
import de.hpi.msd.salsa.serde.avro.Edge;
import de.hpi.msd.salsa.serde.avro.RangeKey;
import org.apache.kafka.streams.state.KeyValueStore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.stream.Stream;

class RangeKeyProcessorTest {
    private final RangeKeyApp app = new RangeKeyApp();

    @RegisterExtension
    final TestTopologyExtension<String, Edge> testTopology = new TestTopologyExtension<>(app::getTopology, app.getProperties());

    @Test
    void shouldAddTweetToUserAdjacencyList() {
        testTopology.input().add(new Edge(2L, 200L, 5));

        RangeKeyGraph graph = getGraph();
        Assertions.assertEquals(Collections.singletonList(200L), graph.getLeftNodeNeighbors(2));
    }

    @Test
    void testRightIndex() {
        testTopology.input()
                .add(new Edge(2L, 200L, 5))
                .add(new Edge(5L, 200L, 5));

        RangeKeyGraph graph = getGraph();
        Assertions.assertEquals(Arrays.asList(2L, 5L), graph.getRightNodeNeighbors(200L));
    }

    @Test
    void shouldAddUserToTweeAdjacencyList() {
        testTopology.input().add(new Edge(2L, 200L, 5));

        RangeKeyGraph graph = getGraph();
        Assertions.assertEquals(Collections.singletonList(2L), graph.getRightNodeNeighbors(200L));
    }

    @Test
    void shouldHandleMultipleInsertsLeftIndex() {
        testTopology.input()
                .add(new Edge(2L, 200L, 5))
                .add(new Edge(3L, 200L, 5))
                .add(new Edge(4L, 200L, 5))
                .add(new Edge(5L, 200L, 5))
                .add(new Edge(2L, 100L, 5));

        RangeKeyGraph graph = getGraph();
        Assertions.assertEquals(Arrays.asList(200L, 100L), graph.getLeftNodeNeighbors(2L));
    }

    @Test
    void shouldHandleMultipleEntriesForUser() {
        TestInput<String, Edge> input = testTopology.input();
        Random random = new Random();
        int count = 40;
        Stream.generate(() -> new Edge(30L, random.nextLong() & Long.MAX_VALUE, random.nextInt(6))).limit(count).forEach(input::add);
        RangeKeyGraph graph = getGraph();
        Assertions.assertEquals(count, graph.getLeftNodeNeighbors(30L).size());
    }

    @Test
    void shouldHandleMultipleEntriesForTweet() {
        TestInput<String, Edge> input = testTopology.input();
        Random random = new Random();
        int count = 40;
        Stream.generate(() -> new Edge(random.nextLong() & Long.MAX_VALUE, 764196671L, random.nextInt(6))).limit(count).forEach(input::add);
        RangeKeyGraph graph = getGraph();
        Assertions.assertEquals(count, graph.getRightNodeNeighbors(764196671L).size());
    }


    private RangeKeyGraph getGraph() {
        KeyValueStore<RangeKey, Long> leftIndex = testTopology.getTestDriver().getKeyValueStore(RangeKeyApp.LEFT_INDEX_NAME);
        KeyValueStore<RangeKey, Long> rightIndex = testTopology.getTestDriver().getKeyValueStore(RangeKeyApp.RIGHT_INDEX_NAME);
        KeyValueStore<Long, Long> leftPositionStore = testTopology.getTestDriver().getKeyValueStore(RangeKeyApp.LEFT_POSITION_STORE_NAME);
        KeyValueStore<Long, Long> rightPositionStore = testTopology.getTestDriver().getKeyValueStore(RangeKeyApp.RIGHT_POSITION_STORE_NAME);
        return new RangeKeyGraph(leftIndex, rightIndex, leftPositionStore, rightPositionStore);
    }
}