package de.hpi.msd.salsa;

import de.hpi.msd.salsa.graph.BipartiteGraph;
import de.hpi.msd.salsa.graph.LocalKeyValueGraph;
import de.hpi.msd.salsa.graph.SampledKeyValueGraph;
import de.hpi.msd.salsa.graph.SegmentedGraph;
import de.hpi.msd.salsa.processor.EdgeProcessor;
import de.hpi.msd.salsa.processor.SamplingEdgeProcessor;
import de.hpi.msd.salsa.processor.SegmentedEdgeProcessor;
import de.hpi.msd.salsa.rest.AdjacencyStateRestService;
import de.hpi.msd.salsa.rest.RecommendationRestService;
import de.hpi.msd.salsa.rest.StreamsRestService;
import de.hpi.msd.salsa.serde.avro.AdjacencyList;
import de.hpi.msd.salsa.serde.avro.SampledAdjacencyList;
import de.hpi.msd.salsa.store.EdgeReadableStateStoreType;
import de.hpi.msd.salsa.store.SegmentedStateStoreBuilder;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.errors.InvalidStateStoreException;
import org.apache.kafka.streams.state.HostInfo;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.Stores;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;

public class EdgeToAdjacencyApp implements Callable<Void> {
    public final static String LEFT_INDEX_NAME = "leftIndex";
    public final static String RIGHT_INDEX_NAME = "rightIndex";
    private final Logger log = LoggerFactory.getLogger(EdgeToAdjacencyApp.class);

    @CommandLine.Option(names = "--host", required = true, description = "address of host machine")
    private String host = "localhost";

    @CommandLine.Option(names = "--port", defaultValue = "8070", description = "port of REST service")
    private int port = 8070;

    @CommandLine.Option(names = "--brokers", required = true, description = "address of kafka broker")
    private String brokers = "localhost:29092";

    @CommandLine.Option(names = "--schema-registry-url", required = true, description = "address of schema registry")
    private String schemaRegistryUrl = "localhost:8081";

    @CommandLine.Option(names = "--topic", defaultValue = "edges", description = "name of topic with incoming edges")
    private String topicName = "edges";

    @CommandLine.Option(names = "--processor", defaultValue = "simple", description = "type of edge processor. Valid values: ${COMPLETION-CANDIDATES}")
    private EdgeProcessorType edgeProcessorType = EdgeProcessorType.simple;

    @CommandLine.Option(names = "--buffer", defaultValue = "5000", description = "Buffer for reservoir sampling")
    private int bufferSize = 5000;

    @CommandLine.Option(names = "--segmets", defaultValue = "10", description = "Segments inside graphjet index")
    private int segments = 10;

    @CommandLine.Option(names = "--pools", defaultValue = "16", description = "Pools inside graphjet segment")
    private int poolsPerSegment = 16;

    @CommandLine.Option(names = "--nodesPerPool", defaultValue = "131072", description = "Nodes per graphjet pool")
    private int nodesPerPool = 5000;

    @Override
    public Void call() throws Exception {
        final Properties properties = this.getProperties();
        final Topology topology = getTopology(properties, edgeProcessorType);
        final KafkaStreams streams = new KafkaStreams(topology, properties);

        streams.cleanUp();
        streams.start();
        waitForKafkaStreams(streams);

        final BipartiteGraph graph = getGraph(edgeProcessorType, streams);
        final RecommendationRestService recommendationRestService = new RecommendationRestService(graph);
        final AdjacencyStateRestService adjacencyStateRestService = new AdjacencyStateRestService(graph);
        final StreamsRestService restService = new StreamsRestService(new HostInfo(host, port),
                recommendationRestService, adjacencyStateRestService);
        restService.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                streams.close();
                restService.stop();
            } catch (Exception e) {
                log.warn("Error in shutdown", e);
            }
        }));
        return null;
    }

    public Properties getProperties() {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
        props.put(StreamsConfig.APPLICATION_SERVER_CONFIG, String.format("%s:%s", host, port));
        props.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde.class);

        return props;
    }

    private Topology getTopology(Properties properties, EdgeProcessorType edgeProcessorType) {
        final String schemaRegistryUrl = properties.getProperty(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG);

        switch (edgeProcessorType) {
            case simple:
                return buildSimpleTopology(schemaRegistryUrl);
            case sampling:
                return buildSamplingTopology(schemaRegistryUrl, bufferSize);
            case segmented:
                return buildSegmetedTopology(schemaRegistryUrl, segments, poolsPerSegment, nodesPerPool);
            default:
                throw new IllegalArgumentException("Cannot create topology for unknown option: " + edgeProcessorType);
        }
    }

    public Topology buildSimpleTopology(String schemaRegistryUrl) {
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

    public Topology buildSamplingTopology(String schemaRegistryUrl, int bufferSize) {
        final Map<String, String> serdeConfig = Collections.singletonMap(
                AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        final SpecificAvroSerde<SampledAdjacencyList> sampledAdjacencyListSerde = new SpecificAvroSerde<>();
        sampledAdjacencyListSerde.configure(serdeConfig, true);

        return new Topology()
                .addSource("Edge-Source", topicName)
                .addProcessor("EdgeProcessor", () -> new SamplingEdgeProcessor(bufferSize), "Edge-Source")
                .addStateStore(Stores.keyValueStoreBuilder(
                        Stores.inMemoryKeyValueStore(LEFT_INDEX_NAME),
                        Serdes.Long(), sampledAdjacencyListSerde), "EdgeProcessor")
                .addStateStore(Stores.keyValueStoreBuilder(
                        Stores.inMemoryKeyValueStore(RIGHT_INDEX_NAME),
                        Serdes.Long(), sampledAdjacencyListSerde), "EdgeProcessor");
    }

    public Topology buildSegmetedTopology(String schemaRegistryUrl, int segments, int poolsPerSegment, int nodesPerPool) {
        final Map<String, String> serdeConfig = Collections.singletonMap(
                AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
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

    private BipartiteGraph getGraph(EdgeProcessorType edgeProcessorType, KafkaStreams streams) {
        switch (edgeProcessorType) {
            case simple:
                return new LocalKeyValueGraph(
                        streams.store(EdgeToAdjacencyApp.LEFT_INDEX_NAME, QueryableStoreTypes.keyValueStore()),
                        streams.store(EdgeToAdjacencyApp.RIGHT_INDEX_NAME, QueryableStoreTypes.keyValueStore()));
            case sampling:
                return new SampledKeyValueGraph(
                        streams.store(EdgeToAdjacencyApp.LEFT_INDEX_NAME, QueryableStoreTypes.keyValueStore()),
                        streams.store(EdgeToAdjacencyApp.RIGHT_INDEX_NAME, QueryableStoreTypes.keyValueStore()));
            case segmented:
                return new SegmentedGraph(
                        streams.store(EdgeToAdjacencyApp.LEFT_INDEX_NAME, new EdgeReadableStateStoreType()),
                        streams.store(EdgeToAdjacencyApp.RIGHT_INDEX_NAME, new EdgeReadableStateStoreType()));
            default:
                throw new IllegalArgumentException("Cannot create graph for unknown option: " + edgeProcessorType);
        }
    }

    private void waitForKafkaStreams(KafkaStreams streams) throws Exception {
        while (true) {
            try {
                getGraph(edgeProcessorType, streams);
                return;
            } catch (InvalidStateStoreException ignored) {
                // store not yet ready for querying
                Thread.sleep(1000);
            }
        }
    }

    public static void main(String[] args) {
        CommandLine commandLine = new CommandLine(new EdgeToAdjacencyApp());
        commandLine.execute(args);
    }

    private enum EdgeProcessorType {
        simple,
        sampling,
        segmented
    }
}
