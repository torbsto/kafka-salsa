package de.hpi.msd.salsa.serde;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.nio.ByteBuffer;
import java.util.Map;

public class RangeKeySerde implements Serde<RangeKey> {
    @Override
    public void configure(Map<String, ?> map, boolean b) {
    }

    @Override
    public void close() {
    }

    @Override
    public Serializer<RangeKey> serializer() {
        return new RangeKeySerializer();
    }

    @Override
    public Deserializer<RangeKey> deserializer() {
        return new RangeKeyDeserializer();
    }


    public class RangeKeySerializer implements Serializer<RangeKey> {
        @Override
        public void configure(Map<String, ?> map, boolean b) {
        }

        @Override
        public byte[] serialize(String s, RangeKey rangeKey) {
            ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES * 2);
            buffer.putLong(rangeKey.getNodeId());
            buffer.putLong(rangeKey.getPosition());
            return buffer.array();
        }

        @Override
        public void close() {
        }
    }

    public class RangeKeyDeserializer implements Deserializer<RangeKey> {
        @Override
        public void configure(Map<String, ?> map, boolean b) {
        }

        @Override
        public RangeKey deserialize(String s, byte[] bytes) {
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            long nodeId = buffer.getLong();
            long position = buffer.getLong();
            return new RangeKey(nodeId, position);
        }

        @Override
        public void close() {
        }
    }

}


