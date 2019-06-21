package de.hpi.msd.salsa.store.index;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IdEncoderTest {
    private IdEncoder idEncoder;

    @BeforeEach
    void setUp() {
        idEncoder = new IdEncoder();
    }

    @Test
    public void shouldEncodeNodeId() {
        long nodeId = 8;
        long edgeType = 3;

        long encoded = idEncoder.encode(nodeId, edgeType);

        Assertions.assertEquals(nodeId, idEncoder.decodeNode(encoded));
    }

    @Test
    public void shouldEncodeEdgeType() {
        long nodeId = 8;
        long edgeType = 3;

        long encoded = idEncoder.encode(nodeId, edgeType);

        Assertions.assertEquals(edgeType, idEncoder.decodeEdgeType(encoded));
    }

    @Test
    public void shouldStoreNodeIdsUpToSevenBytes() {
        long nodeId = (long) Math.pow(2, 56) - 1;
        long edgeType = 3;

        long encoded = idEncoder.encode(nodeId, edgeType);

        Assertions.assertEquals(nodeId, idEncoder.decodeNode(encoded));
        Assertions.assertEquals(edgeType, idEncoder.decodeEdgeType(encoded));
    }

    @Test
    public void shouldFailForNodeIdsOverSevenBytes() {
        long nodeId = (long) Math.pow(2, 56);
        long edgeType = 3;

        Assertions.assertThrows(IllegalArgumentException.class, () -> idEncoder.encode(nodeId, edgeType));
    }

    @Test
    public void shouldStoreEdgeTypesUpToOneBytes() {
        long nodeId = 8;
        long edgeType = 255;

        long encoded = idEncoder.encode(nodeId, edgeType);

        Assertions.assertEquals(nodeId, idEncoder.decodeNode(encoded));
        Assertions.assertEquals(edgeType, idEncoder.decodeEdgeType(encoded));
    }

    @Test
    public void shouldFailForNodeIdsOverOneByte() {
        long nodeId = 8;
        long edgeType = 256;

        Assertions.assertThrows(IllegalArgumentException.class, () -> idEncoder.encode(nodeId, edgeType));
    }

    @Test
    public void shouldEncodeZeroNodeAndEdgeType() {
        long nodeId = 0;
        long edgeType = 0;

        long encoded = idEncoder.encode(nodeId, edgeType);

        Assertions.assertEquals(nodeId, idEncoder.decodeNode(encoded));
        Assertions.assertEquals(edgeType, idEncoder.decodeEdgeType(encoded));
    }
}