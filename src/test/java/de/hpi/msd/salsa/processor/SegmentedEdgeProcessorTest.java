package de.hpi.msd.salsa.processor;

import com.bakdata.fluent_kafka_streams_tests.TestInput;
import com.bakdata.fluent_kafka_streams_tests.junit5.TestTopologyExtension;
import de.hpi.msd.salsa.commands.BaseKafkaSalsaApp;
import de.hpi.msd.salsa.commands.SegmentedApp;
import de.hpi.msd.salsa.serde.avro.Edge;
import de.hpi.msd.salsa.store.SegmentedStateStore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.stream.Stream;

class SegmentedEdgeProcessorTest {
    private final SegmentedApp app = new SegmentedApp(25, 20, 131072);

    @RegisterExtension
    final TestTopologyExtension<String, Edge> testTopology = new TestTopologyExtension<>(app::getTopology, app.getProperties());


    @Test
    void shouldAddTweetToUserAdjacencyList() {
        testTopology.input().add(new Edge(2L, 200L, 5));

        SegmentedStateStore leftIndex = (SegmentedStateStore) testTopology.getTestDriver().getStateStore(BaseKafkaSalsaApp.LEFT_INDEX_NAME);
        Assertions.assertEquals(Collections.singletonList(200L), leftIndex.read(2L).getNeighbors());
    }

    @Test
    void shouldAddUserToTweeAdjacencyList() {
        testTopology.input().add(new Edge(2L, 200L, 5));

        SegmentedStateStore index = (SegmentedStateStore) testTopology.getTestDriver().getStateStore(BaseKafkaSalsaApp.RIGHT_INDEX_NAME);
        Assertions.assertEquals(Collections.singletonList(2L), index.read(200L).getNeighbors());
    }

    @Test
    void shouldHandleMultipleInsertsLeftIndex() {
        testTopology.input()
                .add(new Edge(2L, 200L, 5))
                .add(new Edge(3L, 200L, 5))
                .add(new Edge(4L, 200L, 5))
                .add(new Edge(5L, 200L, 5))
                .add(new Edge(2L, 100L, 5));

        SegmentedStateStore index = (SegmentedStateStore) testTopology.getTestDriver().getStateStore(BaseKafkaSalsaApp.LEFT_INDEX_NAME);
        Assertions.assertEquals(Arrays.asList(200L, 100L), index.read(2L).getNeighbors());
    }

    @Test
    void shouldHandleMultipleEntriesForUser() {
        TestInput<String, Edge> input = testTopology.input();
        Random random = new Random();
        int count = 40;
        Stream.generate(() -> new Edge(30L, random.nextLong(), random.nextInt(6))).limit(count).forEach(input::add);
        SegmentedStateStore index = (SegmentedStateStore) testTopology.getTestDriver().getStateStore(BaseKafkaSalsaApp.LEFT_INDEX_NAME);
        Assertions.assertEquals(count, index.read(30L).getNeighbors().size());
    }

}