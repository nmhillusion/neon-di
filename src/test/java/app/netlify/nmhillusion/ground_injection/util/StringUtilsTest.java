package app.netlify.nmhillusion.ground_injection.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StringUtilsTest {

    @Test
    void testBlank() {
        assertTrue(StringUtils.isBlank(null), "check null");
        assertTrue(StringUtils.isBlank(""), "check empty");
        assertTrue(StringUtils.isBlank("  "), "check blank");
        assertFalse(StringUtils.isBlank(1), "check number");
        assertFalse(StringUtils.isBlank(new Object()), "check Object");
        assertFalse(StringUtils.isBlank("abc"), "check String");
        assertFalse(StringUtils.isBlank("abc  "), "check String with blank");
        assertFalse(StringUtils.isBlank(" e c  "), "check String with blank");
    }
}