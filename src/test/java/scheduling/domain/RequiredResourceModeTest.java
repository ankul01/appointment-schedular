package scheduling.domain;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.Test;

class RequiredResourceModeTest {

    @Test
    void valuesMatchLld() {
        assertArrayEquals(
                new RequiredResourceMode[] {
                    RequiredResourceMode.BAY_ONLY,
                    RequiredResourceMode.TECHNICIAN_ONLY,
                    RequiredResourceMode.BAY_AND_TECHNICIAN
                },
                RequiredResourceMode.values()
        );
    }
}
