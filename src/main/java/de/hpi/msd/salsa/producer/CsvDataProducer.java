package de.hpi.msd.salsa.producer;

import de.hpi.msd.salsa.serde.avro.Edge;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serdes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

public class CsvDataProducer {
    private final KafkaProducer<String, Edge> producer;
    private final File file;
    private final int limit;

    public CsvDataProducer(KafkaProducer<String, Edge> producer,
                           File file,
                           int limit) {
        this.producer = producer;
        this.file = file;
        this.limit = limit;
    }

    public void start() throws IOException {
        final BufferedReader reader = new BufferedReader(new FileReader(file));
        reader.lines()
                .map(line -> {
                    String[] lineSplit = line.split(", ");
                    final long userId = Long.valueOf(lineSplit[0]);
                    final long tweetId = Long.valueOf(lineSplit[1]);
                    final int edgeType = Integer.valueOf(lineSplit[2]);
                    return new Edge(userId, tweetId, edgeType);
                })
                .limit(limit)
                .forEach(edge -> {
                    System.out.println("Sending edge " + edge);
                    producer.send(new ProducerRecord<>("edges", "", edge));
                });
    }

    public static void main(final String[] args) throws Exception {
        assert args.length == 1 : "Please specify the csv to read as argument";

        final String csvFile = args[1];
        final String bootstrapServers = "localhost:29092";
        final String schemaRegistryUrl = "http://localhost:8081";
        System.out.println("Connecting to Kafka cluster via bootstrap servers " + bootstrapServers);
        System.out.println("Connecting to Confluent schema registry at " + schemaRegistryUrl);

        final Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        final Map<String, String> serdeConfig = Collections.singletonMap(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        final SpecificAvroSerializer<Edge> edgeSpecificAvroSerializer = new SpecificAvroSerializer<>();
        edgeSpecificAvroSerializer.configure(serdeConfig, false);

        final KafkaProducer<String, Edge> edgeKafkaProducer = new KafkaProducer<>(props,
                Serdes.String().serializer(),
                edgeSpecificAvroSerializer);

        final File file = new File(csvFile);
        final CsvDataProducer producer = new CsvDataProducer(edgeKafkaProducer, file, 10000);
        producer.start();
    }
}
