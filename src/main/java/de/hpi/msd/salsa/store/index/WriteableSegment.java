package de.hpi.msd.salsa.store.index;

public class WriteableSegment {
    private final AdjacencyStore adjacencyList;

    public WriteableSegment(int expectedNodes) {
        this.adjacencyList = new AdjacencyStore(expectedNodes);
    }

    public void insert(int sourceNode, int targetEdge, byte edgeType) {
        adjacencyList.addEdge(sourceNode, targetEdge, edgeType);
    }

    public int[] getRightEdges(int sourceNode) {
        return adjacencyList.getRightNodes(sourceNode);
    }
}