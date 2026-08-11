package scheduling.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class BookableWindowTest {

    @Test
    void storesPairedSlotsAndVersions() {
        Instant start = Instant.parse("2026-08-11T11:00:00Z");
        Instant end = Instant.parse("2026-08-11T12:00:00Z");
        SlotView bay = new SlotView("bay-slot", "bay-1", ResourceType.BAY, start, end, 1L);
        SlotView tech = new SlotView("tech-slot", "tech-1", ResourceType.TECHNICIAN, start, end, 2L);

        BookableWindow window = new BookableWindow(
                "window-1",
                start,
                end,
                List.of(bay),
                List.of(tech),
                Map.of("bay-slot", 1L, "tech-slot", 2L)
        );

        assertEquals("window-1", window.windowId());
        assertEquals(start, window.start());
        assertEquals(end, window.end());
        assertEquals(List.of(bay), window.baySlots());
        assertEquals(List.of(tech), window.techSlots());
        assertEquals(Map.of("bay-slot", 1L, "tech-slot", 2L), window.expectedVersions());
        assertEquals(List.of("bay-slot", "tech-slot"), window.allSlotIds());
    }

    @Test
    void defensiveCopyPreventsExternalMutation() {
        Instant start = Instant.parse("2026-08-11T11:00:00Z");
        Instant end = Instant.parse("2026-08-11T12:00:00Z");
        SlotView bay = new SlotView("bay-slot", "bay-1", ResourceType.BAY, start, end, 1L);

        Map<String, Long> versions = new HashMap<>();
        versions.put("bay-slot", 1L);

        BookableWindow window = new BookableWindow(
                "window-1",
                start,
                end,
                List.of(bay),
                List.of(),
                versions
        );

        versions.put("bay-slot", 99L);
        assertEquals(1L, window.expectedVersions().get("bay-slot"));
        assertThrows(UnsupportedOperationException.class, () -> window.baySlots().add(bay));
        assertThrows(
                UnsupportedOperationException.class,
                () -> window.expectedVersions().put("x", 1L)
        );
    }

    @Test
    void allSlotIdsSupportsSingleResourceModes() {
        Instant start = Instant.parse("2026-08-11T11:00:00Z");
        Instant end = Instant.parse("2026-08-11T12:00:00Z");
        SlotView bay = new SlotView("bay-slot", "bay-1", ResourceType.BAY, start, end, 1L);

        BookableWindow bayOnly = new BookableWindow(
                "w-bay",
                start,
                end,
                List.of(bay),
                List.of(),
                Map.of("bay-slot", 1L)
        );
        assertEquals(List.of("bay-slot"), bayOnly.allSlotIds());
        assertTrue(bayOnly.techSlots().isEmpty());
    }

    @Test
    void rejectsNullRequiredFields() {
        Instant start = Instant.parse("2026-08-11T11:00:00Z");
        Instant end = Instant.parse("2026-08-11T12:00:00Z");

        assertThrows(
                NullPointerException.class,
                () -> new BookableWindow(null, start, end, List.of(), List.of(), Map.of())
        );
        assertThrows(
                NullPointerException.class,
                () -> new BookableWindow("w", null, end, List.of(), List.of(), Map.of())
        );
        assertThrows(
                NullPointerException.class,
                () -> new BookableWindow("w", start, null, List.of(), List.of(), Map.of())
        );
        assertThrows(
                NullPointerException.class,
                () -> new BookableWindow("w", start, end, null, List.of(), Map.of())
        );
        assertThrows(
                NullPointerException.class,
                () -> new BookableWindow("w", start, end, List.of(), null, Map.of())
        );
        assertThrows(
                NullPointerException.class,
                () -> new BookableWindow("w", start, end, List.of(), List.of(), null)
        );
    }
}
