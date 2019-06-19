package de.hpi.msd.salsa.store.index;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdjacencyStore {
    private static final int[] TOTAL_NODE_CAPACITY;

    static {
        // Lookup table that contains the accumulated number of nodes inserted up to each pool
        // E.g. Pool[0] = 0, Pool[1] = 2, Pool[2] = 6, Pool[3] = 14
        // This is used to quickly calculate positions inside a slice using the node capacity and pool index
        TOTAL_NODE_CAPACITY = new int[32];
        TOTAL_NODE_CAPACITY[0] = 0;

        for (int i = 1; i < TOTAL_NODE_CAPACITY.length; i++) {
            TOTAL_NODE_CAPACITY[i] += Math.pow(2, i) + TOTAL_NODE_CAPACITY[i - 1];
        }
    }

    private final int nodesPerPool;
    private final IdEncoder encoder;
    private final List<EdgePool> edgePools = new ArrayList<>();
    private final Map<Long, List<Integer>> adjacencyList;
    private final Map<Long, Integer> cardinalities;


    public AdjacencyStore(int nodesPerPool) {
        this.nodesPerPool = nodesPerPool;
        this.encoder = new IdEncoder();
        this.adjacencyList = new HashMap<>();
        this.cardinalities = new HashMap<>();
    }

    public void addEdge(long source, long target, long edgeType) {
        final List<Integer> slices = adjacencyList.getOrDefault(source, new ArrayList<>());

        // Calculate edge pool from node cardinality
        final int cardinality = cardinalities.getOrDefault(source, 0);
        final int poolIndex = getPoolIndexForCardinality(cardinality);
        final EdgePool pool = getOrCreateEdgePool(poolIndex);

        // Calculate current slice inside edge pool
        final int lastPoolIndex = slices.size() - 1;
        int sliceIndex;

        if (poolIndex <= lastPoolIndex) {
            sliceIndex = slices.get(lastPoolIndex);
        } else {
            // Get new slice inside a new edge pool
            sliceIndex = edgePools.get(poolIndex).nextFreeSliceIndex();
            slices.add(sliceIndex);
        }

        // Calculate position of node inside slice
        final int nodeIndex = cardinality - TOTAL_NODE_CAPACITY[poolIndex];

        // Insert edge into pool
        // TODO: Encode edges and add in-partition mapping
        pool.addToSlice(sliceIndex, nodeIndex, target);
        adjacencyList.put(source, slices);
        cardinalities.put(source, cardinality + 1);
    }

    public List<Long> getTargetNodes(long sourceNode) {
        final List<Integer> slices = adjacencyList.get(sourceNode);
        final int cardinality = cardinalities.get(sourceNode);
        final List<Long> targetNodes = new ArrayList<>(cardinality);
        int nodeCount = 0;

        for (int i = 0; i < slices.size(); i++) {
            final EdgePool pool = edgePools.get(i);
            final long[] slice = pool.getSlice(slices.get(i));
            final int totalMissingNodes = cardinality - nodeCount;
            final int nodesInSlice = totalMissingNodes < slice.length
                    ? totalMissingNodes
                    : slice.length;

            for (int nodeIndex = 0; nodeIndex < nodesInSlice; nodeIndex++) {
                // TODO: Decode edges
                final long nodeId = slice[nodeIndex];
                targetNodes.add(nodeCount + nodeIndex, nodeId);
            }
            nodeCount += slice.length;
        }
        return targetNodes;
    }

    private EdgePool getOrCreateEdgePool(int poolIndex) {
        if (poolIndex >= edgePools.size()) {
            // Create new edge pool
            createEdgePool(edgePools.size(), nodesPerPool);
        }
        return edgePools.get(poolIndex);
    }

    private void createEdgePool(int poolIndex, int expectedNodes) {
        int currentPools = edgePools.size();
        // Slice sizes scale 2^(pools + 1): 2, 4, 8, 16, 32...
        int sliceSize = (int) Math.pow(2, 1 + currentPools);
        // Number of slices per pool scale n / 2^(pools - 1): n, n/2, n/4, n/8...
        int slices = (int) (expectedNodes / Math.pow(2, currentPools - 1));
        // Create a edge pool
        edgePools.add(poolIndex, new EdgePool(slices, sliceSize));
    }

    private int getPoolIndexForCardinality(int cardinality) {
        // Todo: Replace logarithm with lookup table: http://graphics.stanford.edu/~seander/bithacks.html#IntegerLogLookup
        return (int) Math.floor(Math.log(cardinality + 2) / Math.log(2)) - 1;
    }
}