package de.hpi.msd.salsa.commands;

import de.hpi.msd.salsa.graph.BipartiteGraph;
import de.hpi.msd.salsa.graph.adjacencyList.LocalKeyValueGraph;
import de.hpi.msd.salsa.processor.EdgeProcessor;
import de.hpi.msd.salsa.serde.avro.AdjacencyList;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.Stores;
import picocli.CommandLine;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;

@CommandLine.Command(name = "simple", mixinStandardHelpOptions = true,
        description = "Simple Edge Processor")
public class SimpleApp extends BaseKafkaSalsaApp {
    public final static String LEFT_INDEX_NAME = "leftIndex";
    public final static String RIGHT_INDEX_NAME = "rightIndex";

    @Override
    BipartiteGraph getGraph(KafkaStreams streams) {
        return new LocalKeyValueGraph(
                streams.store(LEFT_INDEX_NAME, QueryableStoreTypes.keyValueStore()),
                streams.store(RIGHT_INDEX_NAME, QueryableStoreTypes.keyValueStore()));
    }

    @Override
    Topology getTopology(Properties properties) {
        final Map<String, String> serdeConfig = Collections.singletonMap(
                AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        final SpecificAvroSerde<AdjacencyList> adjacencyListSerde = new SpecificAvroSerde<>();
        adjacencyListSerde.configure(serdeConfig, true);

        return new Topology()
                .addSource("Edge-Source", topicName)
                .addProcessor("EdgeProcessor", EdgeProcessor::new, "Edge-Source")
                .addStateStore(Stores.keyValueStoreBuilder(
                        Stores.inMemoryKeyValueStore(LEFT_INDEX_NAME),
                        Serdes.Long(), adjacencyListSerde), "EdgeProcessor")
                .addStateStore(Stores.keyValueStoreBuilder(
                        Stores.inMemoryKeyValueStore(RIGHT_INDEX_NAME),
                        Serdes.Long(), adjacencyListSerde), "EdgeProcessor");
    }

    public static void main(String[] args) {
        CommandLine commandLine = new CommandLine(new SimpleApp());
        commandLine.execute(args);
    }
}
