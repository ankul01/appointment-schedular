package scheduling.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Paired bay + technician window for {@link RequiredResourceMode#BAY_AND_TECHNICIAN}.
 * Also usable for single-resource modes with one side empty.
 */
public final class BookableWindow {
    private final String windowId;
    private final Instant start;
    private final Instant end;
    private final List<SlotView> baySlots;
    private final List<SlotView> techSlots;
    private final Map<String, Long> expectedVersions;

    public BookableWindow(
            String windowId,
            Instant start,
            Instant end,
            List<SlotView> baySlots,
            List<SlotView> techSlots,
            Map<String, Long> expectedVersions
    ) {
        this.windowId = Objects.requireNonNull(windowId, "windowId");
        this.start = Objects.requireNonNull(start, "start");
        this.end = Objects.requireNonNull(end, "end");
        this.baySlots = List.copyOf(Objects.requireNonNull(baySlots, "baySlots"));
        this.techSlots = List.copyOf(Objects.requireNonNull(techSlots, "techSlots"));
        this.expectedVersions = Collections.unmodifiableMap(
                new LinkedHashMap<>(Objects.requireNonNull(expectedVersions, "expectedVersions"))
        );
    }

    public String windowId() {
        return windowId;
    }

    public Instant start() {
        return start;
    }

    public Instant end() {
        return end;
    }

    /** N consecutive bay slots ordered by start; may be empty for technician-only modes. */
    public List<SlotView> baySlots() {
        return baySlots;
    }

    /** N consecutive tech slots for the same [start, end); may be empty for bay-only modes. */
    public List<SlotView> techSlots() {
        return techSlots;
    }

    /** slotId → version for the union of bay + tech slots. */
    public Map<String, Long> expectedVersions() {
        return expectedVersions;
    }

    /** All slot IDs in this window (bay then tech), for book requests. */
    public List<String> allSlotIds() {
        List<String> ids = new ArrayList<>(baySlots.size() + techSlots.size());
        for (SlotView slot : baySlots) {
            ids.add(slot.slotId());
        }
        for (SlotView slot : techSlots) {
            ids.add(slot.slotId());
        }
        return List.copyOf(ids);
    }
}
