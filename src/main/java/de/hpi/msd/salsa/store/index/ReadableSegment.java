package de.hpi.msd.salsa.store.index;

import java.util.List;

public interface ReadableSegment {
    List<Long> getTargetNodes(long source);

    List<Long> getAllSourceNodes();

    int getCardinality(long source);
}
