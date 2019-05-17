package de.hpi.msd.salsa;

import com.bakdata.fluent_kafka_streams_tests.junit5.TestTopologyExtension;
import de.hpi.msd.salsa.serde.avro.AdjacencyList;
import de.hpi.msd.salsa.serde.avro.Edge;
import org.apache.kafka.streams.state.KeyValueStore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Collections;

class EdgeToAdjacencyAppTest {
    private final EdgeToAdjacencyApp edgeToAdjacencyApp = new EdgeToAdjacencyApp();

    @RegisterExtension
    final TestTopologyExtension<String, Edge> testTopology = new TestTopologyExtension<>(this.edgeToAdjacencyApp::buildTopology, this.edgeToAdjacencyApp.getProperties("test.properties"));


    @Test
    void shouldAddTweetToUserAdjacencyList() {
        testTopology.input().add(new Edge(2L, 200L, 5));

        KeyValueStore<Long, AdjacencyList> leftIndex = testTopology.getTestDriver().getKeyValueStore("leftIndex");
        Assertions.assertEquals(Collections.singletonList(200L), leftIndex.get(2L).getNeighbors());
    }


    @Test
    void shouldGetSchemaRegistryClient() {
        Assertions.assertNotNull(this.testTopology.getSchemaRegistry());
    }


}