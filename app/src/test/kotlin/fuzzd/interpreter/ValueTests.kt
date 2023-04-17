package fuzzd.interpreter

import fuzzd.interpreter.value.divideEuclidean
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class ValueTests {

    @Nested
    class EuclideanDivisionTests {

        @Test
        fun givenZeroAsDenominator_whenDivideEuclidean_expectUnsupportedOperationException() {
            runCatching {
                divideEuclidean(5L, 0L)
            }.onSuccess {
                fail()
            }.onFailure {
                assertTrue { it is UnsupportedOperationException }
            }
        }

        @Test
        fun givenTwoPositiveNumbers_whenDivideEuclidean_expectCorrectValue() {
            assertEquals(2L, divideEuclidean(6L, 3L))
            assertEquals(2L, divideEuclidean(7L, 3L))
            assertEquals(7L, divideEuclidean(7L, 1L))
        }

        @Test
        fun givenPositiveNumeratorAndNegativeDenominator_whenDivideEuclidean_expectCorrectValue() {
            assertEquals(-2L, divideEuclidean(6L, -3L))
            assertEquals(-2L, divideEuclidean(7L, -3L))
        }

        @Test
        fun givenNegativeNumeratorAndPositiveDenominator_whenDivideEuclidean_expectCorrectValue() {
            assertEquals(-2L, divideEuclidean(-6L, 3L))
            assertEquals(-3L, divideEuclidean(-7L, 3L))
        }

        @Test
        fun givenTwoNegativeNumbers_whenDivideEuclidean_expectCorrectValue() {
            assertEquals(2, divideEuclidean(-4L, -2L))
            assertEquals(3, divideEuclidean(-5L, -2L))
        }
    }
}
