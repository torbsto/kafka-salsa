package de.hpi.msd.salsa.store.index;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdjacencyStore {
    private static final int[] NODE_THRESHOLDS;

    static {
        NODE_THRESHOLDS = new int[32];
        NODE_THRESHOLDS[0] = 0;

        for (int i = 1; i < NODE_THRESHOLDS.length; i++) {
            NODE_THRESHOLDS[i] += Math.pow(2, i);
        }
    }

    private final int expectedNodes;
    private final IdEncoder encoder;
    private final List<EdgePool> edgePools = new ArrayList<>();
    private final Map<Long, List<Integer>> adjacencyList;
    private final Map<Long, Integer> cardinalities;


    public AdjacencyStore(int expectedNodes) {
        this.expectedNodes = expectedNodes;
        this.encoder = new IdEncoder();
        adjacencyList = new HashMap<>();
        cardinalities = new HashMap<>();
    }

    public void addEdge(long sourceNode, long target, long edgeType) {
        long encodedEdge = encoder.encode(target, edgeType);

        List<Integer> poolPositions = adjacencyList.getOrDefault(sourceNode, new ArrayList<>(4));
        int currentPool = poolPositions.size() - 1;

        // Load cardinality for node to calculate position in pool
        int cardinality = cardinalities.getOrDefault(sourceNode, 0);
        int poolIndex = getPoolForCardinality(cardinality) - 1;

        if (poolIndex > (edgePools.size() - 1)) {
            // We need a new pool
            addEdgePool(expectedNodes);
        }

        int slicePosition;

        if (poolIndex == currentPool && poolPositions.size() > 0) {
            // There is a current slice and there is still space in it
            slicePosition = poolPositions.get(currentPool);
        } else {
            // We need to create a new slice
            slicePosition = edgePools.get(poolIndex).getNewSlicePosition();
            poolPositions.add(slicePosition);
        }

        EdgePool pool = edgePools.get(poolIndex);

        // FIXME
        int positionInSlice = cardinality - NODE_THRESHOLDS[poolIndex];

        pool.addToSlice(slicePosition, positionInSlice, encodedEdge);
        adjacencyList.put(sourceNode, poolPositions);
        cardinalities.put(sourceNode, cardinality + 1);
    }

    public long[] getRightNodes(long sourceNode) {
        List<Integer> slices = adjacencyList.get(sourceNode);
        int cardinality = cardinalities.get(sourceNode);
        long[] nodes = new long[cardinality];
        int nodeCount = 0;

        for (int i = 0; i < slices.size(); i++) {
            EdgePool pool = edgePools.get(i);
            long[] slice = pool.getSlice(slices.get(i));

            // Convert bitpacked edges to vertex only
            int nodesLeft = cardinality - nodeCount;

            for (int s = 0; s < slice.length && s < nodesLeft; s++) {
                long decoded = encoder.decodeNode(slice[s]);
                nodes[(nodeCount + s)] = encoder.decodeNode(decoded);
            }

            nodeCount += slice.length;
        }
        return nodes;
    }

    void addEdgePool(int expectedNodes) {
        int currentPools = edgePools.size();
        // Slice sizes scale 2^(pools + 1): 2, 4, 8, 16, 32...
        int sliceSize = (int) Math.pow(2, 1 + currentPools);
        // Number of slices per pool scale n / 2^(pools - 1): n, n/2, n/4, n/8...
        int slices = (int) (expectedNodes / Math.pow(2, currentPools - 1));
        // Create a edge pool
        edgePools.add(new EdgePool(slices, sliceSize));
    }

    int getPoolForCardinality(int cardinality) {
        // Todo: Make faster with lookup table: http://graphics.stanford.edu/~seander/bithacks.html#IntegerLogLookup
        return (int) Math.floor(Math.log(cardinality + 2) / Math.log(2));
    }
}