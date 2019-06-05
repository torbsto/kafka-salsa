package de.hpi.msd.salsa;

import de.hpi.msd.salsa.processor.EdgeProcessor;
import de.hpi.msd.salsa.processor.SamplingEdgeProcessor;
import de.hpi.msd.salsa.rest.AdjacencyStateRestService;
import de.hpi.msd.salsa.rest.RecommendationRestService;
import de.hpi.msd.salsa.rest.StreamsRestService;
import de.hpi.msd.salsa.serde.avro.AdjacencyList;
import de.hpi.msd.salsa.serde.avro.SampledAdjacencyList;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.errors.InvalidStateStoreException;
import org.apache.kafka.streams.processor.ProcessorSupplier;
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


    public final static String LEFT_INDEX_NAME = "leftIndex";
    public final static String RIGHT_INDEX_NAME = "rightIndex";
    private final Logger log = LoggerFactory.getLogger(EdgeToAdjacencyApp.class);


    public Properties getProperties() {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, this.getClass().getSimpleName());
        props.put(StreamsConfig.APPLICATION_SERVER_CONFIG, String.format("%s:%s", host, port));
        props.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde.class);

        return props;
    }

    public Topology buildSamplingTopology(String schemaRegistryUrl, int bufferSize) {
        SpecificAvroSerde<SampledAdjacencyList> adjacencyListSerde = new SpecificAvroSerde<>();
        return buildTopology(schemaRegistryUrl, () -> new SamplingEdgeProcessor(bufferSize), adjacencyListSerde);
    }

    public Topology buildTopology(String schemaRegistryUrl) {
        SpecificAvroSerde<AdjacencyList> adjacencyListSerde = new SpecificAvroSerde<>();
        return buildTopology(schemaRegistryUrl, EdgeProcessor::new, adjacencyListSerde);
    }

    private Topology buildTopology(String schemaRegistryUrl, ProcessorSupplier supplier, SpecificAvroSerde<? extends SpecificRecordBase> adjacencyListSerde) {
        final Map<String, String> serdeConfig = Collections.singletonMap(
                AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        adjacencyListSerde.configure(serdeConfig, true);
        return new Topology()
                .addSource("Edge-Source", topicName)
                .addProcessor("EdgeProcessor", supplier, "Edge-Source")
                .addStateStore(Stores.keyValueStoreBuilder(
                        Stores.inMemoryKeyValueStore(LEFT_INDEX_NAME),
                        Serdes.Long(),
                        adjacencyListSerde), "EdgeProcessor")
                .addStateStore(Stores.keyValueStoreBuilder(
                        Stores.inMemoryKeyValueStore(RIGHT_INDEX_NAME),
                        Serdes.Long(),
                        adjacencyListSerde), "EdgeProcessor");
    }


    @Override
    public Void call() throws Exception {
        Properties properties = this.getProperties();
        final KafkaStreams streams = new KafkaStreams(this.buildTopology(properties.getProperty(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG)), properties);

        streams.cleanUp();
        streams.start();
        waitForKafkaStreams(streams);
        final RecommendationRestService recommendationRestService = new RecommendationRestService(streams);
        final AdjacencyStateRestService adjacencyStateRestService = new AdjacencyStateRestService(streams);
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

    public static void main(String[] args) throws Exception {
        CommandLine commandLine = new CommandLine(new EdgeToAdjacencyApp());
        commandLine.execute(args);
    }

    private void waitForKafkaStreams(KafkaStreams streams) throws Exception {
        while (true) {
            try {
                streams.store(LEFT_INDEX_NAME, QueryableStoreTypes.keyValueStore());
                streams.store(RIGHT_INDEX_NAME, QueryableStoreTypes.keyValueStore());
                return;
            } catch (InvalidStateStoreException ignored) {
                // store not yet ready for querying
                Thread.sleep(1000);
            }
        }
    }
}
