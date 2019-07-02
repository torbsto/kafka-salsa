package de.hpi.msd.salsa.commands;

import de.hpi.msd.salsa.EdgeToAdjacencyApp;
import de.hpi.msd.salsa.graph.BipartiteGraph;
import de.hpi.msd.salsa.graph.rangeKey.RangeKeyGraph;
import de.hpi.msd.salsa.processor.RangeKeyProcessor;
import de.hpi.msd.salsa.serde.avro.RangeKey;
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

public class RangeKeyApp extends BaseKafkaSalsaApp {


    @Override
    BipartiteGraph getGraph(KafkaStreams streams) {
        return new RangeKeyGraph(
                streams.store(LEFT_INDEX_NAME, QueryableStoreTypes.keyValueStore()),
                streams.store(RIGHT_INDEX_NAME, QueryableStoreTypes.keyValueStore()),
                streams.store("leftPosition", QueryableStoreTypes.keyValueStore()),
                streams.store("rightPosition", QueryableStoreTypes.keyValueStore()));
    }

    @Override
    Topology getTopology(Properties properties) {
        final Map<String, String> serdeConfig = Collections.singletonMap(
                AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        final SpecificAvroSerde<RangeKey> rangeKeySerde = new SpecificAvroSerde<>();
        rangeKeySerde.configure(serdeConfig, true);

        return new Topology()
                .addSource("Edge-Source", topicName)
                .addProcessor("EdgeProcessor", RangeKeyProcessor::new, "Edge-Source")
                .addStateStore(Stores.keyValueStoreBuilder(
                        Stores.inMemoryKeyValueStore(LEFT_INDEX_NAME),
                        rangeKeySerde, Serdes.Long()), "EdgeProcessor")
                .addStateStore(Stores.keyValueStoreBuilder(
                        Stores.inMemoryKeyValueStore(RIGHT_INDEX_NAME),
                        rangeKeySerde, Serdes.Long()), "EdgeProcessor")
                .addStateStore(Stores.keyValueStoreBuilder(
                        Stores.inMemoryKeyValueStore("leftPosition"),
                        Serdes.Long(), Serdes.Long()), "EdgeProcessor")
                .addStateStore(Stores.keyValueStoreBuilder(
                        Stores.inMemoryKeyValueStore("rightPosition"),
                        Serdes.Long(), Serdes.Long()), "EdgeProcessor");
    }

    public static void main(String[] args) {
        CommandLine commandLine = new CommandLine(new RangeKeyApp());
        commandLine.execute(args);
    }
}
