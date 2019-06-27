package de.hpi.msd.salsa.serde;

public class RangeKey {
    private long nodeId;
    private long position;

    public RangeKey(long nodeId, long position) {
        this.nodeId = nodeId;
        this.position = position;
    }

    public long getNodeId() {
        return nodeId;
    }

    public void setNodeId(long nodeId) {
        this.nodeId = nodeId;
    }

    public long getPosition() {
        return position;
    }

    public void setPosition(long position) {
        this.position = position;
    }

    public static RangeKeySerde serde() {
        return new RangeKeySerde();
    }


}