package fuzzd.interpreter

import fuzzd.interpreter.value.divideEuclidean
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.math.BigInteger.valueOf
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class ValueTests {

    @Nested
    class EuclideanDivisionTests {

        @Test
        fun givenZeroAsDenominator_whenDivideEuclidean_expectUnsupportedOperationException() {
            runCatching {
                divideEuclidean(valueOf(5L), valueOf(0L))
            }.onSuccess {
                fail()
            }.onFailure {
                assertTrue { it is UnsupportedOperationException }
            }
        }

        @Test
        fun givenTwoPositiveNumbers_whenDivideEuclidean_expectCorrectValue() {
            assertEquals(valueOf(2L), divideEuclidean(valueOf(6L), valueOf(3L)))
            assertEquals(valueOf(2L), divideEuclidean(valueOf(7L), valueOf(3L)))
            assertEquals(valueOf(7L), divideEuclidean(valueOf(7L), valueOf(1L)))
        }

        @Test
        fun givenPositiveNumeratorAndNegativeDenominator_whenDivideEuclidean_expectCorrectValue() {
            assertEquals(valueOf(-2L), divideEuclidean(valueOf(6L), valueOf(-3L)))
            assertEquals(valueOf(-2L), divideEuclidean(valueOf(7L), valueOf(-3L)))
        }

        @Test
        fun givenNegativeNumeratorAndPositiveDenominator_whenDivideEuclidean_expectCorrectValue() {
            assertEquals(valueOf(-2L), divideEuclidean(valueOf(-6L), valueOf(3L)))
            assertEquals(valueOf(-3L), divideEuclidean(valueOf(-7L), valueOf(3L)))
        }

        @Test
        fun givenTwoNegativeNumbers_whenDivideEuclidean_expectCorrectValue() {
            assertEquals(valueOf(2), divideEuclidean(valueOf(-4L), valueOf(-2L)))
            assertEquals(valueOf(3), divideEuclidean(valueOf(-5L), valueOf(-2L)))
        }
    }
}
