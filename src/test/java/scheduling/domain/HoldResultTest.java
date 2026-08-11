package scheduling.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class HoldResultTest {

    @Test
    void successCarriesTokenAndVersion() {
        HoldResult result = HoldResult.success("hold-token-1", 7L);

        assertTrue(result.success());
        assertEquals("hold-token-1", result.holdToken());
        assertEquals(7L, result.newVersion());
    }

    @Test
    void failureHasNoToken() {
        HoldResult result = HoldResult.failure();

        assertFalse(result.success());
        assertNull(result.holdToken());
        assertEquals(0L, result.newVersion());
    }

    @Test
    void successRejectsNullToken() {
        assertThrows(NullPointerException.class, () -> HoldResult.success(null, 1L));
    }
}
