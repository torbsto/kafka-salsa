package de.hpi.msd.salsa;

import de.hpi.msd.salsa.processor.EdgeProcessor;
import de.hpi.msd.salsa.rest.AdjacencyStateRestService;
import de.hpi.msd.salsa.rest.RecommendationRestService;
import de.hpi.msd.salsa.rest.StreamsRestService;
import de.hpi.msd.salsa.serde.avro.AdjacencyList;
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

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

public class EdgeToAdjacencyApp {
    public final static String LEFT_INDEX_NAME = "leftIndex";
    public final static String RIGHT_INDEX_NAME = "rightIndex";
    private final Logger log = LoggerFactory.getLogger(EdgeToAdjacencyApp.class);


    public Topology buildTopology(String schemaRegistryUrl) {
        SpecificAvroSerde<AdjacencyList> adjacencyListSerde = new SpecificAvroSerde<>();
        final Map<String, String> serdeConfig = Collections.singletonMap(
                AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        adjacencyListSerde.configure(serdeConfig, false);

        return new Topology()
                .addSource("Edge-Source", "edges")
                .addProcessor("EdgeProcessor", () -> new EdgeProcessor(LEFT_INDEX_NAME, RIGHT_INDEX_NAME), "Edge-Source")
                .addStateStore(Stores.keyValueStoreBuilder(
                        Stores.inMemoryKeyValueStore(LEFT_INDEX_NAME),
                        Serdes.Long(),
                        adjacencyListSerde

                ), "EdgeProcessor")
                .addStateStore(Stores.keyValueStoreBuilder(
                        Stores.inMemoryKeyValueStore(RIGHT_INDEX_NAME),
                        Serdes.Long(),
                        adjacencyListSerde
                ), "EdgeProcessor");
    }

    public Properties getProperties(String filePath) {
        Properties props = new Properties();
        try {
            props.load(Objects.requireNonNull(EdgeToAdjacencyApp.class.getClassLoader().getResourceAsStream(filePath)));
        } catch (IOException e) {
            log.error("Could not load properties", e);
            e.printStackTrace();
        }
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde.class);
        return props;
    }

    public static void main(String[] args) throws Exception {
        EdgeToAdjacencyApp edgeToAdjacencyApp = new EdgeToAdjacencyApp();
        Properties properties = edgeToAdjacencyApp.getProperties("app.properties");
        final KafkaStreams streams = new KafkaStreams(edgeToAdjacencyApp.buildTopology(properties.getProperty(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG)), properties);

        streams.cleanUp();
        streams.start();

        while (true) {
            try {
                streams.store(LEFT_INDEX_NAME, QueryableStoreTypes.keyValueStore());
                break;
            } catch (InvalidStateStoreException ignored) {
                // store not yet ready for querying
                Thread.sleep(1000);
            }
        }

        final RecommendationRestService recommendationRestService = new RecommendationRestService(streams);
        final AdjacencyStateRestService adjacencyStateRestService = new AdjacencyStateRestService(streams);
        final StreamsRestService restService = new StreamsRestService(new HostInfo("localhost", 8070), recommendationRestService, adjacencyStateRestService);
        restService.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                streams.close();
                restService.stop();
            } catch (Exception e) {
                edgeToAdjacencyApp.log.warn("Error in rest shutdown", e);
            }
        }));


    }
}
