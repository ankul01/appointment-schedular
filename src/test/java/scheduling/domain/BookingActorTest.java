package scheduling.domain;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.Test;

class BookingActorTest {

    @Test
    void valuesMatchLld() {
        assertArrayEquals(
                new BookingActor[] {BookingActor.CUSTOMER, BookingActor.ADVISOR},
                BookingActor.values()
        );
    }
}
