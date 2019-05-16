package de.hpi.msd.salsa;

import com.bakdata.fluent_kafka_streams_tests.TestTopology;
import de.hpi.msd.salsa.serde.avro.AdjacencyList;
import de.hpi.msd.salsa.serde.avro.Edge;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.state.KeyValueStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AppTest {
    private final App app = new App();
    @RegisterExtension
    final TestTopology<Bytes, Edge> testTopology = new TestTopology<>(this.app::buildTopology, this.app.getProperties("test.properties"));

    @Test
    public void test() {
        this.testTopology
                .input()
                .add(new Edge(1294L, 512L, 1))
                .add(new Edge(1294L, 234L, 5));

        KeyValueStore<Long, AdjacencyList> store = testTopology.getTestDriver().getKeyValueStore("leftToRightIndex");

        assertEquals(store.get(1294L).getNeighbors(), Arrays.asList(512L, 234L));


    }
}