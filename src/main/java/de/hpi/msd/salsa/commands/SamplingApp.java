package de.hpi.msd.salsa.commands;

import de.hpi.msd.salsa.graph.BipartiteGraph;
import de.hpi.msd.salsa.graph.rangeKey.SampleKeyValueGraph;
import de.hpi.msd.salsa.processor.SamplingEdgeProcessor;
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

@CommandLine.Command(name = "sampling", mixinStandardHelpOptions = true,
        description = "Sampling Edge Processor")
public class SamplingApp extends BaseKafkaSalsaApp {

    @CommandLine.Option(names = "--buffer", defaultValue = "5000", description = "Buffer for reservoir sampling")
    private int bufferSize = 5000;

    public SamplingApp() {
    }

    public SamplingApp(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    @Override
    public BipartiteGraph getGraph(KafkaStreams streams) {
        return new SampleKeyValueGraph(
                streams.store(LEFT_INDEX_NAME, QueryableStoreTypes.keyValueStore()),
                streams.store(RIGHT_INDEX_NAME, QueryableStoreTypes.keyValueStore()),
                bufferSize);
    }

    @Override
    public Topology getTopology(Properties properties) {
        final Map<String, String> serdeConfig = Collections.singletonMap(
                AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, properties.getProperty(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG));
        final SpecificAvroSerde<RangeKey> rangeKeySerde = new SpecificAvroSerde<>();
        rangeKeySerde.configure(serdeConfig, true);

        return new Topology()
                .addSource("Edge-Source", topicName)
                .addProcessor("EdgeProcessor", () -> new SamplingEdgeProcessor(bufferSize), "Edge-Source")
                .addStateStore(Stores.keyValueStoreBuilder(
                        Stores.inMemoryKeyValueStore(LEFT_INDEX_NAME),
                        rangeKeySerde, Serdes.Long()), "EdgeProcessor")
                .addStateStore(Stores.keyValueStoreBuilder(
                        Stores.inMemoryKeyValueStore(RIGHT_INDEX_NAME),
                        rangeKeySerde, Serdes.Long()), "EdgeProcessor")
                .addStateStore(Stores.keyValueStoreBuilder(
                        Stores.inMemoryKeyValueStore("leftCount"),
                        Serdes.Long(), Serdes.Long()), "EdgeProcessor")
                .addStateStore(Stores.keyValueStoreBuilder(
                        Stores.inMemoryKeyValueStore("rightCount"),
                        Serdes.Long(), Serdes.Long()), "EdgeProcessor");
    }

    public static void main(String[] args) {
        CommandLine commandLine = new CommandLine(new SamplingApp());
        commandLine.execute(args);
    }
}
