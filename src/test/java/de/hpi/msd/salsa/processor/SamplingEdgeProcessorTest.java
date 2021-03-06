package de.hpi.msd.salsa.processor;

import com.bakdata.fluent_kafka_streams_tests.TestInput;
import com.bakdata.fluent_kafka_streams_tests.junit5.TestTopologyExtension;
import de.hpi.msd.salsa.commands.SamplingApp;
import de.hpi.msd.salsa.graph.rangeKey.SampleKeyValueGraph;
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

class SamplingEdgeProcessorTest {
    private final static int BUFFER_SIZE = 20;
    private final SamplingApp app = new SamplingApp(BUFFER_SIZE);

    @RegisterExtension
    final TestTopologyExtension<String, Edge> testTopology = new TestTopologyExtension<>(app::getTopology, app.getProperties());

    @Test
    void shouldAddTweetToUserAdjacencyList() {
        testTopology.input().add(new Edge(2L, 200L, 5));

        SampleKeyValueGraph graph = getGraph();
        Assertions.assertEquals(Collections.singletonList(200L), graph.getLeftNodeNeighbors(2));
    }


    @Test
    void shouldAddUserToTweeAdjacencyList() {
        testTopology.input().add(new Edge(2L, 200L, 5));

        SampleKeyValueGraph graph = getGraph();
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

        SampleKeyValueGraph graph = getGraph();
        Assertions.assertEquals(Arrays.asList(200L, 100L), graph.getLeftNodeNeighbors(2L));
    }

    @Test
    void shouldSampleLeftIndexWhenMaxSizeReached() {
        TestInput<String, Edge> input = testTopology.input();
        Random random = new Random();
        Stream.generate(() -> new Edge(30L, random.nextLong(), random.nextInt(6))).limit(BUFFER_SIZE + 10).forEach(input::add);
        SampleKeyValueGraph graph = getGraph();
        Assertions.assertEquals(20, graph.getLeftNodeNeighbors(30L).size());
    }

    @Test
    void shouldSampleRightIndexWhenMaxSizeReached() {
        TestInput<String, Edge> input = testTopology.input();
        Random random = new Random(256);
        Stream.generate(() -> new Edge(random.nextLong(), 30L, random.nextInt(6))).limit(BUFFER_SIZE + 10).forEach(input::add);
        SampleKeyValueGraph graph = getGraph();
        Assertions.assertEquals(20, graph.getRightNodeNeighbors(30L).size());
    }

    @Test
    void updatesCountCorrectlyOnLeftIndex() {
        TestInput<String, Edge> input = testTopology.input();
        Random random = new Random();
        int count = 40;
        Stream.generate(() -> new Edge(30L, random.nextLong(), random.nextInt(6))).limit(count).forEach(input::add);
        KeyValueStore<Long, Long> positionStore = testTopology.getTestDriver().getKeyValueStore(SamplingApp.LEFT_COUNT_STORE_NAME);
        Assertions.assertEquals(count, positionStore.get(30L));
    }

    @Test
    void updatesCountCorrectlyOnRightIndex() {
        TestInput<String, Edge> input = testTopology.input();
        Random random = new Random();
        int count = 40;
        Stream.generate(() -> new Edge(random.nextLong(), 30L, random.nextInt(6))).limit(count).forEach(input::add);
        KeyValueStore<Long, Long> positionStore = testTopology.getTestDriver().getKeyValueStore(SamplingApp.RIGHT_COUNT_STORE_NAME);
        Assertions.assertEquals(count, positionStore.get(30L));
    }

    @Test
    void testRightIndex() {
        testTopology.input()
                .add(new Edge(2L, 200L, 5))
                .add(new Edge(2L, 300L, 5))
                .add(new Edge(2L, 400L, 5))
                .add(new Edge(2L, 500L, 5))
                .add(new Edge(2L, 600L, 5));

        SampleKeyValueGraph graph = getGraph();
        Assertions.assertEquals(Collections.singletonList(2L), graph.getRightNodeNeighbors(200L));
    }

    @Test
    void shouldGetSchemaRegistryClient() {
        Assertions.assertNotNull(this.testTopology.getSchemaRegistry());
    }


    private SampleKeyValueGraph getGraph() {
        KeyValueStore<RangeKey, Long> leftIndex = testTopology.getTestDriver().getKeyValueStore(SamplingApp.LEFT_INDEX_NAME);
        KeyValueStore<RangeKey, Long> rightIndex = testTopology.getTestDriver().getKeyValueStore(SamplingApp.RIGHT_INDEX_NAME);
        return new SampleKeyValueGraph(leftIndex, rightIndex, BUFFER_SIZE);
    }
}