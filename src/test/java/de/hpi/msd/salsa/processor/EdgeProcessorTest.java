package de.hpi.msd.salsa.processor;

import com.bakdata.fluent_kafka_streams_tests.TestInput;
import com.bakdata.fluent_kafka_streams_tests.junit5.TestTopologyExtension;
import de.hpi.msd.salsa.commands.SimpleApp;
import de.hpi.msd.salsa.serde.avro.AdjacencyList;
import de.hpi.msd.salsa.serde.avro.Edge;
import org.apache.kafka.streams.state.KeyValueStore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.stream.Stream;

class EdgeProcessorTest {
    private final SimpleApp app = new SimpleApp();

    @RegisterExtension
    final TestTopologyExtension<String, Edge> testTopology = new TestTopologyExtension<>(app::getTopology, app.getProperties());

    @Test
    void shouldAddTweetToUserAdjacencyList() {
        testTopology.input().add(new Edge(2L, 200L, 5));

        KeyValueStore<Long, AdjacencyList> leftIndex = testTopology.getTestDriver().getKeyValueStore(SimpleApp.LEFT_INDEX_NAME);
        Assertions.assertEquals(Collections.singletonList(200L), leftIndex.get(2L).getNeighbors());
    }

    @Test
    void testRightIndex() {
        testTopology.input()
                .add(new Edge(2L, 200L, 5))
                .add(new Edge(2L, 300L, 5))
                .add(new Edge(2L, 400L, 5))
                .add(new Edge(2L, 500L, 5))
                .add(new Edge(2L, 600L, 5));

        KeyValueStore<Long, AdjacencyList> index = testTopology.getTestDriver().getKeyValueStore(SimpleApp.RIGHT_INDEX_NAME);
        Assertions.assertEquals(Collections.singletonList(2L), index.get(200L).getNeighbors());
    }

    @Test
    void shouldAddUserToTweeAdjacencyList() {
        testTopology.input().add(new Edge(2L, 200L, 5));

        KeyValueStore<Long, AdjacencyList> index = testTopology.getTestDriver().getKeyValueStore(SimpleApp.RIGHT_INDEX_NAME);
        Assertions.assertEquals(Collections.singletonList(2L), index.get(200L).getNeighbors());
    }

    @Test
    void shouldHandleMultipleInsertsLeftIndex() {
        testTopology.input()
                .add(new Edge(2L, 200L, 5))
                .add(new Edge(3L, 200L, 5))
                .add(new Edge(4L, 200L, 5))
                .add(new Edge(5L, 200L, 5))
                .add(new Edge(2L, 100L, 5));

        KeyValueStore<Long, AdjacencyList> index = testTopology.getTestDriver().getKeyValueStore(SimpleApp.LEFT_INDEX_NAME);
        Assertions.assertEquals(Arrays.asList(200L, 100L), index.get(2L).getNeighbors());
    }

    @Test
    void shouldHandleMultipleEntriesForUser() {
        TestInput<String, Edge> input = testTopology.input();
        Random random = new Random();
        int count = 40;
        Stream.generate(() -> new Edge(30L, random.nextLong(), random.nextInt(6))).limit(count).forEach(input::add);
        KeyValueStore<Long, AdjacencyList> index = testTopology.getTestDriver().getKeyValueStore(SimpleApp.LEFT_INDEX_NAME);
        Assertions.assertEquals(count, index.get(30L).getNeighbors().size());
    }

    @Test
    void shouldGetSchemaRegistryClient() {
        Assertions.assertNotNull(this.testTopology.getSchemaRegistry());
    }
}