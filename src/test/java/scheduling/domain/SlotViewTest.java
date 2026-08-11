package scheduling.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;

import org.junit.jupiter.api.Test;

class SlotViewTest {

    @Test
    void storesFields() {
        Instant start = Instant.parse("2026-08-11T11:00:00Z");
        Instant end = Instant.parse("2026-08-11T12:00:00Z");

        SlotView view = new SlotView("slot-1", "bay-1", ResourceType.BAY, start, end, 3L);

        assertEquals("slot-1", view.slotId());
        assertEquals("bay-1", view.resourceId());
        assertEquals(ResourceType.BAY, view.resourceType());
        assertEquals(start, view.start());
        assertEquals(end, view.end());
        assertEquals(3L, view.version());
    }

    @Test
    void rejectsNullRequiredFields() {
        Instant start = Instant.parse("2026-08-11T11:00:00Z");
        Instant end = Instant.parse("2026-08-11T12:00:00Z");

        assertThrows(
                NullPointerException.class,
                () -> new SlotView(null, "bay-1", ResourceType.BAY, start, end, 0L)
        );
        assertThrows(
                NullPointerException.class,
                () -> new SlotView("slot-1", null, ResourceType.BAY, start, end, 0L)
        );
        assertThrows(
                NullPointerException.class,
                () -> new SlotView("slot-1", "bay-1", null, start, end, 0L)
        );
        assertThrows(
                NullPointerException.class,
                () -> new SlotView("slot-1", "bay-1", ResourceType.BAY, null, end, 0L)
        );
        assertThrows(
                NullPointerException.class,
                () -> new SlotView("slot-1", "bay-1", ResourceType.BAY, start, null, 0L)
        );
    }
}
