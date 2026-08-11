package scheduling.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;

import org.junit.jupiter.api.Test;

class TimeRangeTest {

    @Test
    void storesFromAndTo() {
        Instant from = Instant.parse("2026-08-11T09:00:00Z");
        Instant to = Instant.parse("2026-08-11T17:00:00Z");

        TimeRange range = new TimeRange(from, to);

        assertEquals(from, range.from());
        assertEquals(to, range.to());
    }

    @Test
    void rejectsNullFrom() {
        Instant to = Instant.parse("2026-08-11T17:00:00Z");
        assertThrows(NullPointerException.class, () -> new TimeRange(null, to));
    }

    @Test
    void rejectsNullTo() {
        Instant from = Instant.parse("2026-08-11T09:00:00Z");
        assertThrows(NullPointerException.class, () -> new TimeRange(from, null));
    }

    @Test
    void rejectsToNotAfterFrom() {
        Instant instant = Instant.parse("2026-08-11T09:00:00Z");
        assertThrows(IllegalArgumentException.class, () -> new TimeRange(instant, instant));
        assertThrows(
                IllegalArgumentException.class,
                () -> new TimeRange(instant, instant.minusSeconds(1))
        );
    }
}
