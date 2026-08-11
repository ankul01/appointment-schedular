package scheduling.domain;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AppointmentStatusTest {

    @Test
    void onlyConfirmedAndCancelled() {
        assertArrayEquals(
                new AppointmentStatus[] {AppointmentStatus.CONFIRMED, AppointmentStatus.CANCELLED},
                AppointmentStatus.values()
        );
        assertEquals(2, AppointmentStatus.values().length);
    }
}
