package de.hpi.msd.salsa.commands;

import de.hpi.msd.salsa.graph.BipartiteGraph;
import de.hpi.msd.salsa.graph.segmented.SegmentedGraph;
import de.hpi.msd.salsa.processor.SegmentedEdgeProcessor;
import de.hpi.msd.salsa.serde.avro.SampledAdjacencyList;
import de.hpi.msd.salsa.store.EdgeReadableStateStoreType;
import de.hpi.msd.salsa.store.SegmentedStateStoreBuilder;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.Topology;
import picocli.CommandLine;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;

@CommandLine.Command(name = "simple", mixinStandardHelpOptions = true,
        description = "Simple Edge Processor")
public class SegmentedApp extends BaseKafkaSalsaApp {
    @CommandLine.Option(names = "--segments", defaultValue = "10", description = "Segments inside graphjet index")
    private int segments = 10;

    @CommandLine.Option(names = "--pools", defaultValue = "16", description = "Pools inside graphjet segment")
    private int poolsPerSegment = 16;

    @CommandLine.Option(names = "--nodesPerPool", defaultValue = "131072", description = "Nodes per graphjet pool")
    private int nodesPerPool = 131072;

    public SegmentedApp() {
    }

    public SegmentedApp(int segments, int poolsPerSegment, int nodesPerPool) {
        this.segments = segments;
        this.poolsPerSegment = poolsPerSegment;
        this.nodesPerPool = nodesPerPool;
    }

    @Override
    public BipartiteGraph getGraph(KafkaStreams streams) {
        return new SegmentedGraph(
                streams.store(LEFT_INDEX_NAME, new EdgeReadableStateStoreType()),
                streams.store(RIGHT_INDEX_NAME, new EdgeReadableStateStoreType()));
    }

    @Override
    public Topology getTopology(Properties properties) {
        final Map<String, String> serdeConfig = Collections.singletonMap(
                AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, properties.getProperty(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG));
        final SpecificAvroSerde<SampledAdjacencyList> adjacencyListSerde = new SpecificAvroSerde<>();
        adjacencyListSerde.configure(serdeConfig, true);

        return new Topology()
                .addSource("Edge-Source", topicName)
                .addProcessor("EdgeProcessor", SegmentedEdgeProcessor::new, "Edge-Source")
                .addStateStore(new SegmentedStateStoreBuilder(LEFT_INDEX_NAME, segments, poolsPerSegment, nodesPerPool),
                        "EdgeProcessor")
                .addStateStore(new SegmentedStateStoreBuilder(RIGHT_INDEX_NAME, segments, poolsPerSegment, nodesPerPool),
                        "EdgeProcessor");
    }

    public static void main(String[] args) {
        CommandLine commandLine = new CommandLine(new SegmentedApp());
        commandLine.execute(args);
    }
}
