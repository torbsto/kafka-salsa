package de.hpi.msd.salsa;

import com.bakdata.fluent_kafka_streams_tests.junit5.TestTopologyExtension;
import de.hpi.msd.salsa.serde.avro.AdjacencyList;
import de.hpi.msd.salsa.serde.avro.Edge;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import org.apache.kafka.streams.state.KeyValueStore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

class EdgeToAdjacencyAppTest {
    private final EdgeToAdjacencyApp edgeToAdjacencyApp = new EdgeToAdjacencyApp();

    private final Properties properties = this.edgeToAdjacencyApp.getProperties("test.properties");

    @RegisterExtension
    final TestTopologyExtension<String, Edge> testTopology = new TestTopologyExtension<>(
            prop -> this.edgeToAdjacencyApp.buildTopology(prop.getProperty(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG)),
            properties);


    @Test
    void shouldAddTweetToUserAdjacencyList() {
        testTopology.input().add(new Edge(2L, 200L, 5));

        KeyValueStore<Long, AdjacencyList> leftIndex = testTopology.getTestDriver().getKeyValueStore("leftIndex");
        Assertions.assertEquals(Collections.singletonList(200L), leftIndex.get(2L).getNeighbors());
    }

    @Test
    void shouldAddUserToTweeAdjacencyList() {
        testTopology.input().add(new Edge(2L, 200L, 5));

        KeyValueStore<Long, AdjacencyList> index = testTopology.getTestDriver().getKeyValueStore("rightIndex");
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

        KeyValueStore<Long, AdjacencyList> index = testTopology.getTestDriver().getKeyValueStore("leftIndex");
        Assertions.assertEquals(Arrays.asList(200L, 100L), index.get(2L).getNeighbors());
    }


    @Test
    void shouldGetSchemaRegistryClient() {
        Assertions.assertNotNull(this.testTopology.getSchemaRegistry());
    }


}