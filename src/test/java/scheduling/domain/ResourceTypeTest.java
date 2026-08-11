package scheduling.domain;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ResourceTypeTest {

    @Test
    void valuesMatchLld() {
        assertArrayEquals(
                new ResourceType[] {ResourceType.BAY, ResourceType.TECHNICIAN},
                ResourceType.values()
        );
        assertEquals(2, ResourceType.values().length);
    }
}
