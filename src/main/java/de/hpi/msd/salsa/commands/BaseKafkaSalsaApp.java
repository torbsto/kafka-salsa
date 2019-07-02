package de.hpi.msd.salsa.commands;


import de.hpi.msd.salsa.graph.BipartiteGraph;
import de.hpi.msd.salsa.rest.AdjacencyStateRestService;
import de.hpi.msd.salsa.rest.RecommendationRestService;
import de.hpi.msd.salsa.rest.StreamsRestService;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.errors.InvalidStateStoreException;
import org.apache.kafka.streams.state.HostInfo;
import picocli.CommandLine;

import java.util.Properties;
import java.util.concurrent.Callable;

import static org.apache.kafka.common.requests.DeleteAclsResponse.log;

public abstract class BaseKafkaSalsaApp implements Callable<Void> {
    @CommandLine.Option(names = "--application-id", required = true, description = "name of streams application")
    String applicationId = "simple";

    @CommandLine.Option(names = "--host", required = true, description = "address of host machine")
    String host = "localhost";

    @CommandLine.Option(names = "--port", defaultValue = "8070", description = "port of REST service")
    int port = 8070;

    @CommandLine.Option(names = "--brokers", required = true, description = "address of kafka broker")
    String brokers = "localhost:29092";

    @CommandLine.Option(names = "--schema-registry-url", required = true, description = "address of schema registry")
    String schemaRegistryUrl = "localhost:8081";

    @CommandLine.Option(names = "--topic", defaultValue = "edges", description = "name of topic with incoming edges")
    String topicName = "edges";


    @Override
    public Void call() throws Exception {
        final Properties properties = this.getProperties();
        final Topology topology = getTopology(properties);
        final KafkaStreams streams = new KafkaStreams(topology, properties);

        streams.cleanUp();
        streams.start();
        waitForKafkaStreams(streams);

        final BipartiteGraph graph = getGraph(streams);
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

    abstract BipartiteGraph getGraph(KafkaStreams streams);

    abstract Topology getTopology(Properties properties);

    private void waitForKafkaStreams(KafkaStreams streams) throws Exception {
        while (true) {
            try {
                getGraph(streams);
                return;
            } catch (InvalidStateStoreException ignored) {
                // store not yet ready for querying
                Thread.sleep(1000);
            }
        }
    }
}

