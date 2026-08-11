package scheduling.domain;

import java.time.Instant;
import java.util.Objects;

/** Availability/hold view; client must echo {@link #version()} on book/hold. */
public final class SlotView {
    private final String slotId;
    private final String resourceId;
    private final ResourceType resourceType;
    private final Instant start;
    private final Instant end;
    private final long version;

    public SlotView(
            String slotId,
            String resourceId,
            ResourceType resourceType,
            Instant start,
            Instant end,
            long version
    ) {
        this.slotId = Objects.requireNonNull(slotId, "slotId");
        this.resourceId = Objects.requireNonNull(resourceId, "resourceId");
        this.resourceType = Objects.requireNonNull(resourceType, "resourceType");
        this.start = Objects.requireNonNull(start, "start");
        this.end = Objects.requireNonNull(end, "end");
        this.version = version;
    }

    public String slotId() {
        return slotId;
    }

    public String resourceId() {
        return resourceId;
    }

    public ResourceType resourceType() {
        return resourceType;
    }

    public Instant start() {
        return start;
    }

    public Instant end() {
        return end;
    }

    public long version() {
        return version;
    }
}
