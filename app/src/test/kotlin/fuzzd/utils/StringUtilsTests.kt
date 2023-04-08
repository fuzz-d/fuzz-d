package fuzzd.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class StringUtilsTests {
    @Test
    fun givenIntString_whenToHexInt_expectCorrectValue() {
        assertEquals(25, "25".toHexInt())
        assertEquals(472, "472".toHexInt())
    }

    @Test
    fun givenNegativeIntString_whenToHexInt_expectCorrectValue() {
        assertEquals(-25, "-25".toHexInt())
        assertEquals(-472, "-472".toHexInt())
    }

    @Test
    fun givenHexString_whenToHexInt_expectCorrectValue() {
        assertEquals(25, "0x19".toHexInt())
        assertEquals(472, "0x1d8".toHexInt())
    }

    @Test
    fun givenNegativeHexString_whenToHexInt_expectCorrectValue() {
        assertEquals(-25, "-0x19".toHexInt())
        assertEquals(-472, "-0x1d8".toHexInt())
    }
}
