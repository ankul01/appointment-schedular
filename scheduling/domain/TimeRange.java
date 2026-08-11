package scheduling.domain;

import java.time.Instant;
import java.util.Objects;

public final class TimeRange {
    private final Instant from;
    private final Instant to;

    public TimeRange(Instant from, Instant to) {
        this.from = Objects.requireNonNull(from, "from");
        this.to = Objects.requireNonNull(to, "to");
        if (!to.isAfter(from)) {
            throw new IllegalArgumentException("to must be after from");
        }
    }

    public Instant from() {
        return from;
    }

    public Instant to() {
        return to;
    }
}
