package de.hpi.msd.salsa.algorithm;

import de.hpi.msd.salsa.index.BipartiteGraph;

import java.util.*;
import java.util.stream.Collectors;

public class Salsa {
    private Map<Long, Integer> currentLeftNodeVisits;
    private Map<Long, Integer> currentRightNodeVisits;
    private Map<Long, Integer> totalRightNodeVisits;
    private BipartiteGraph graph;
    private Random random;
    private int totalVisits = 0;

    public Salsa(BipartiteGraph graph, Random random) {
        this.graph = graph;
        this.random = random;
        this.currentLeftNodeVisits = new HashMap<>();
        this.currentRightNodeVisits = new HashMap<>();
        this.totalRightNodeVisits = new HashMap<>();
    }

    public List<Long> compute(long rootNode, int walks, int length, double resetProbability, int limit) {
        boolean isLeftToRight = true;

        // Initialize seed set on left side
        currentLeftNodeVisits.put(rootNode, walks);

        // Perform forward and backward iterations between users and tweets
        for (int i = 0; i < length; i++) {
            if (isLeftToRight) {
                leftIteration(rootNode, resetProbability);
            } else {
                rightIteration();
            }
            isLeftToRight = !isLeftToRight;
        }

        // Print out results (unordered)
        for (Map.Entry<Long, Integer> rightNodeVisit : totalRightNodeVisits.entrySet()) {
            float visitPercentage = (float) rightNodeVisit.getValue() / totalVisits;
            System.out.printf("Visited %s %d times, %s%%%n", rightNodeVisit.getKey(), rightNodeVisit.getValue(), visitPercentage);
        }

        Set<Long> knownNodes = new HashSet<>(graph.getLeftNodeNeighbors(rootNode));

        return totalRightNodeVisits
                .entrySet()
                .stream()
                .filter(entry -> !knownNodes.contains(entry.getKey()))
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private void leftIteration(long rootNode, double resetProbability) {
        int totalResets = 0;

        // For each left node
        for (Long node : currentLeftNodeVisits.keySet()) {
            // Get previous visits
            Integer visits = currentLeftNodeVisits.get(node);
            int walks = 0;
            int resets = 0;

            // Calculate how many new walks should be performed and how many resets will happen
            for (int i = 0; i < visits; i++) {

                if (random.nextDouble() > resetProbability) {
                    walks++;
                } else {
                    resets++;
                }
            }

            // Sample out edges and pick random walks to the right side
            List<Long> edges = graph.getLeftNodeNeighbors(node);

            // Perform walks to the right side
            for (int i = 0; i < walks; i++) {
                // Ignore nodes without out links
                if (edges.size() > 0) {
                    int randomPosition = random.nextInt(edges.size());
                    Long edge = edges.get(randomPosition);
                    currentRightNodeVisits.put(edge, currentRightNodeVisits.getOrDefault(edge, 0) + 1);
                    totalRightNodeVisits.put(edge, totalRightNodeVisits.getOrDefault(edge, 0) + 1);
                }
            }

            totalVisits += walks;
            // Add resets to currentLeftNodeVisits
            totalResets += resets;
        }

        currentLeftNodeVisits.clear();
        currentLeftNodeVisits.put(rootNode, totalResets);
    }

    private void rightIteration() {
        for (long node : currentRightNodeVisits.keySet()) {
            Integer visits = currentRightNodeVisits.get(node);

            // Sample left edges for all walks
            List<Long> edges = graph.getRightNodeNeighbors(node);

            // Perform walks back to the left side
            for (int i = 0; i < visits; i++) {
                // Ignore nodes without out links
                if (edges.size() > 0) {
                    int randomPosition = random.nextInt(edges.size());
                    long edge = edges.get(randomPosition);
                    currentLeftNodeVisits.put(edge, currentLeftNodeVisits.getOrDefault(edge, 0) + 1);
                }
            }
        }
        currentRightNodeVisits.clear();
    }
}
