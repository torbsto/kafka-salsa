package de.hpi.msd.salsa.store.index;

import java.util.*;

public final class ImmutableSegment implements ReadableSegment {
    public static ImmutableSegment fromSegment(ReadableSegment segment) {
        final List<Long> sourceNodes = segment.getAllSourceNodes();
        final Map<Long, Entry> entryMap = new HashMap<>(sourceNodes.size());
        final List<Long> nodes = new ArrayList<>(sourceNodes.size());

        for (Long source : sourceNodes) {
            final List<Long> targetNodes = segment.getTargetNodes(source);
            final int start = nodes.size();
            final int end = start + targetNodes.size();
            nodes.addAll(targetNodes);
            entryMap.put(source, new Entry(start, end));
        }

        return new ImmutableSegment(entryMap, nodes);
    }

    private final Map<Long, Entry> entryMap;
    private final List<Long> segment;

    private ImmutableSegment(Map<Long, Entry> entryMap, List<Long> segment) {
        this.entryMap = entryMap;
        this.segment = segment;
    }

    @Override
    public List<Long> getTargetNodes(long source) {
        if (entryMap.containsKey(source)) {
            Entry entry = entryMap.get(source);
            return new ArrayList<>(segment.subList(entry.start, entry.end));
        }
        return Collections.emptyList();
    }

    @Override
    public List<Long> getAllSourceNodes() {
        return new ArrayList<>(entryMap.keySet());
    }

    @Override
    public int getCardinality(long source) {
        final Entry entry = entryMap.getOrDefault(source, new Entry(0, 0));
        return entry.end - entry.start;
    }

    private static class Entry {
        final int start;
        final int end;

        public Entry(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }
}
