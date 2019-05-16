package de.hpi.msd.salsa;

import de.hpi.msd.salsa.processor.EdgeProcessor;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;

import java.io.IOException;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class App {
    public Topology buildTopology() {
        return new Topology()
                .addSource("Edge-Source", "edges")
                .addProcessor("EdgeProcessor", EdgeProcessor::new, "Edge-Source");
    }

    public Properties getProperties(String filePath) {
        Properties props = new Properties();
        try {
            props.load(Objects.requireNonNull(App.class.getClassLoader().getResourceAsStream(filePath)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde.class.getName());
        return props;
    }

    public static void main(String[] args) throws Exception {
        App app = new App();
        final KafkaStreams streams = new KafkaStreams(app.buildTopology(), app.getProperties("app.properties"));
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

}
