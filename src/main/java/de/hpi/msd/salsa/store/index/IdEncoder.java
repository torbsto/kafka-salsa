package de.hpi.msd.salsa.store.index;

public class IdEncoder {
    private static final long EDGE_TYPE_BITS = Byte.BYTES * 8;
    private static final long ALLOWED_NODE_BITS = (Long.BYTES * 8) - EDGE_TYPE_BITS;

    public IdEncoder() {
    }

    public long encode(long node, long edgeType) {
        if (node >>> ALLOWED_NODE_BITS > 0) {
            throw new IllegalArgumentException(
                    String.format("The node id needs to use less than %d bits", ALLOWED_NODE_BITS));
        }

        if (edgeType >>> EDGE_TYPE_BITS > 0) {
            throw new IllegalArgumentException(
                    String.format("The edge type needs to use less than %d bits", EDGE_TYPE_BITS));
        }

        return (edgeType << ALLOWED_NODE_BITS) | node;
    }

    public long decodeEdgeType(long encodedEdge) {
        return (encodedEdge >>> ALLOWED_NODE_BITS);
    }

    public long decodeNode(long encodedEdge) {
        return ((encodedEdge << EDGE_TYPE_BITS) >>> EDGE_TYPE_BITS);
    }
}
