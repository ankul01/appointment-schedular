package scheduling.domain;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.Test;

class SlotStatusTest {

    @Test
    void valuesMatchLld() {
        assertArrayEquals(
                new SlotStatus[] {SlotStatus.AVAILABLE, SlotStatus.HELD, SlotStatus.BOOKED},
                SlotStatus.values()
        );
    }
}
