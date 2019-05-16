package de.hpi.msd.salsa;

import de.hpi.msd.salsa.processor.EdgeProcessor;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class App {
    public final static String TOPIC = "interaction_topic";

    public static void main(String[] args) throws Exception {
        Topology topology = new Topology();
        topology
                .addSource("Edge-Source", "edges")
                .addProcessor("EdgeProcessor", EdgeProcessor::new, "Edge-Source");

        final KafkaStreams streams = new KafkaStreams(topology, getProperties());
        final CountDownLatch latch = new CountDownLatch(1);


        Runtime.getRuntime().addShutdownHook(new Thread("streams-shutdown-hook") {
            @Override
            public void run() {
                streams.close();
                latch.countDown();
            }
        });

        try {
            streams.start();
            latch.await();
        } catch (Throwable e) {
            System.exit(1);
        }
        System.exit(0);
    }

    private static Properties getProperties() {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "key_value_test");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
        props.put(KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG, "localhost:8081");
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde.class.getName());
        return props;
    }
}
