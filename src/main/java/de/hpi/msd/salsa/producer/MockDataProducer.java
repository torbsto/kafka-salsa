package de.hpi.msd.salsa.producer;

import de.hpi.msd.salsa.serde.avro.Edge;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serdes;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

public class MockDataProducer {


    public static void main(final String[] args) throws Exception {
        final String bootstrapServers = "localhost:29092";
        final String schemaRegistryUrl = "http://localhost:8081";
        System.out.println("Connecting to Kafka cluster via bootstrap servers " + bootstrapServers);
        System.out.println("Connecting to Confluent schema registry at " + schemaRegistryUrl);

        // Read comma-delimited file of songs into Array

        final Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        final Map<String, String> serdeConfig = Collections.singletonMap(
                AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        final SpecificAvroSerializer<Edge> edgeSpecificAvroSerializer = new SpecificAvroSerializer<>();
        edgeSpecificAvroSerializer.configure(serdeConfig, false);

        final KafkaProducer<String, Edge> edgeKafkaProducer = new KafkaProducer<>(props,
                Serdes.String().serializer(),
                edgeSpecificAvroSerializer);

        final Random random = new Random();

        // send a play event every 100 milliseconds
        while (true) {
            final Edge edge = new Edge((long) random.nextInt(200), (long) (200 + random.nextInt(200)), 5);
            System.out.println(edge.toString());
            edgeKafkaProducer.send(
                    new ProducerRecord<>("edges",
                            "", edge));
            Thread.sleep(100L);
        }
    }

}

