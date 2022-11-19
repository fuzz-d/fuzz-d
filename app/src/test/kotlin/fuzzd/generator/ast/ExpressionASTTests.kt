package fuzzd.generator.ast

import fuzzd.generator.ast.ExpressionAST.BinaryExpressionAST
import fuzzd.generator.ast.ExpressionAST.BooleanLiteralAST
import fuzzd.generator.ast.ExpressionAST.IntegerLiteralAST
import fuzzd.generator.ast.ExpressionAST.RealLiteralAST
import fuzzd.generator.ast.ExpressionAST.UnaryExpressionAST
import fuzzd.generator.ast.error.InvalidFormatException
import fuzzd.generator.ast.operators.BinaryOperator.ConjunctionOperator
import fuzzd.generator.ast.operators.BinaryOperator.DisjunctionOperator
import fuzzd.generator.ast.operators.BinaryOperator.ImplicationOperator
import fuzzd.generator.ast.operators.UnaryOperator.NotOperator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class ExpressionASTTests {

    @Nested
    inner class UnaryExpressionASTTests {
        @Test
        fun givenBinaryExpression_whenToString_expectWrapExpr() {
            // given
            val bexp = BinaryExpressionAST(BooleanLiteralAST(false), ConjunctionOperator, BooleanLiteralAST(true))
            val operator = NotOperator
            val expr = UnaryExpressionAST(bexp, operator)

            // when
            val str = expr.toString()

            // expect
            assertEquals("$operator($bexp)", str)
        }
    }

    @Nested
    inner class BinaryExpressionASTTests {
        @Test
        fun givenDifferentPrecedenceExpr1_whenToString_expectNoWrapExpr1() {
            // given
            val expr1 = BinaryExpressionAST(BooleanLiteralAST(false), ImplicationOperator, BooleanLiteralAST(true))
            val operator = ConjunctionOperator
            val expr2 = BooleanLiteralAST(false)

            val expr = BinaryExpressionAST(expr1, operator, expr2)

            // when
            val str = expr.toString()

            // expect
            assertEquals(str, "$expr1$operator$expr2")
        }

        @Test
        fun givenDifferentPrecedenceExpr2_whenToString_expectNoWrapExpr2() {
            // given
            val expr1 = BooleanLiteralAST(false)
            val operator = ConjunctionOperator
            val expr2 = BinaryExpressionAST(BooleanLiteralAST(false), ImplicationOperator, BooleanLiteralAST(true))

            val expr = BinaryExpressionAST(expr1, operator, expr2)

            // when
            val str = expr.toString()

            // expect
            assertEquals(str, "$expr1$operator$expr2")
        }

        @Test
        fun givenSamePrecedenceExpr1_whenToString_expectWrapExpr1() {
            // given
            val expr1 = BinaryExpressionAST(BooleanLiteralAST(false), DisjunctionOperator, BooleanLiteralAST(true))
            val operator = ConjunctionOperator
            val expr2 = BooleanLiteralAST(false)

            val expr = BinaryExpressionAST(expr1, operator, expr2)

            // when
            val str = expr.toString()

            // expect
            assertEquals(str, "($expr1)$operator$expr2")
        }

        @Test
        fun givenSamePrecedenceExpr2_whenToString_expectWrapExpr2() {
            // given
            val expr1 = BooleanLiteralAST(false)
            val operator = ConjunctionOperator
            val expr2 = BinaryExpressionAST(BooleanLiteralAST(false), DisjunctionOperator, BooleanLiteralAST(true))

            val expr = BinaryExpressionAST(expr1, operator, expr2)

            // when
            val str = expr.toString()

            // expect
            assertEquals(str, "$expr1$operator($expr2)")
        }

        @Test
        fun givenSamePrecedenceExpr1AndExpr2_whenToString_expectWrapExpr1AndExpr2() {
            // given
            val expr1 = BinaryExpressionAST(BooleanLiteralAST(false), DisjunctionOperator, BooleanLiteralAST(true))
            val operator = ConjunctionOperator
            val expr2 = BinaryExpressionAST(BooleanLiteralAST(false), DisjunctionOperator, BooleanLiteralAST(true))

            val expr = BinaryExpressionAST(expr1, operator, expr2)

            // when
            val str = expr.toString()

            // expect
            assertEquals(str, "($expr1)$operator($expr2)")
        }
    }

    @Nested
    inner class IntegerLiteralASTTests {
        @Test
        fun givenValidIntegerValue_whenCreateIntegerLiteralAST_expectSuccessfulInit() {
            // given
            val value = "123456196"

            // when
            IntegerLiteralAST(value)

            // expect nothing
        }

        @Test
        fun givenValidIntegerValueWithUnderscores_whenCreateIntegerLiteralAST_expectSuccessfulInit() {
            // given
            val value = "1_0319_431_3"

            // when
            IntegerLiteralAST(value)

            // expect nothing
        }

        @Test
        fun givenValidNegativeIntegerValue_whenCreateIntegerLiteralAST_expectSuccessfulInit() {
            // given
            val value = "-12538"

            // when
            IntegerLiteralAST(value)

            // expect nothing
        }

        @Test
        fun givenValidNegativeIntegerValueWithUnderscores_whenCreateIntegerLiteralAST_expectSuccessfulInit() {
            // given
            val value = "-1_345_35678_3"

            // when
            IntegerLiteralAST(value)

            // expect nothing
        }

        @Test
        fun givenIntegerValueStartsWithUnderscore_whenCreateIntegerLiteralAST_expectFailedInit() {
            // given
            val value = "_9315_3"

            // expect
            assertFailsWith<InvalidFormatException> {
                IntegerLiteralAST(value)
            }
        }

        @Test
        fun givenIntegerValueEndsWithUnderscore_whenCreateIntegerLiteralAST_expectFailedInit() {
            // given
            val value = "9315_3_"

            // expect
            assertFailsWith<InvalidFormatException> {
                IntegerLiteralAST(value)
            }
        }

        @Test
        fun givenIntegerValueWithDoubleUnderscore_whenCreateIntegerLiteralAST_expectFailedInit() {
            // given
            val value = "9315__3"

            // expect failure
            assertFailsWith<InvalidFormatException> {
                IntegerLiteralAST(value)
            }
        }

        @Test
        fun givenValidHexValue_whenCreateIntegerLiteralAST_expectSuccessfulInit() {
            // given
            val value = "0x934AF51"

            // when
            IntegerLiteralAST(value)

            // expect nothing
        }

        @Test
        fun givenValidHexValueWithUnderscores_whenCreateIntegerLiteralAST_expectSuccessfulInit() {
            // given
            val value = "0x3_5391A_F"

            // when
            IntegerLiteralAST(value)

            // expect nothing
        }

        @Test
        fun givenNegativeHexValue_whenCreateIntegerLiteralAST_expectSuccessfulInit() {
            // given
            val value = "-0x38317"

            // when
            IntegerLiteralAST(value)

            // expect nothing
        }

        @Test
        fun givenNegativeHexValueWithUnderscores_whenCreateIntegerLiteralAST_expectSuccessfulInit() {
            // given
            val value = "-0x93_A93851_FF"

            // when
            IntegerLiteralAST(value)

            // expect nothing
        }

        @Test
        fun givenHexValueStartsWithUnderscore_whenCreateIntegerLiteralAST_expectFailedInit() {
            // given
            val value = "0x_3418753_4"

            // expect
            assertFailsWith<InvalidFormatException> {
                IntegerLiteralAST(value)
            }
        }

        @Test
        fun givenHexValueEndsWithUnderscore_whenCreateIntegerLiteralAST_expectFailedInit() {
            // given
            val value = "0x95317_F_"

            // expect
            assertFailsWith<InvalidFormatException> {
                IntegerLiteralAST(value)
            }
        }

        @Test
        fun givenHexValueWithDoubleUnderscore_whenCreateIntegerLiteralAST_expectFailedInit() {
            // given
            val value = "-0x95813__453216"

            // expect
            assertFailsWith<InvalidFormatException> {
                IntegerLiteralAST(value)
            }
        }
    }

    @Nested
    inner class RealLiteralASTTests {
        @Test
        fun givenValidRealValue_whenCreateRealLiteralAST_expectSuccessfulInit() {
            // given
            val value = "123.53195"

            // when
            RealLiteralAST(value)

            // expect nothing
        }

        @Test
        fun givenValidRealValueWithUnderscores_whenCreateRealLiteralAST_expectSuccessfulInit() {
            // given
            val value = "120_3141.953_4315"

            // when
            RealLiteralAST(value)

            // expect nothing
        }

        @Test
        fun givenValidNegativeRealValue_whenCreateRealLiteralAST_expectSuccessfulInit() {
            // given
            val value = "-1234951.95138"

            // when
            RealLiteralAST(value)

            // expect nothing
        }

        @Test
        fun givenValidNegativeRealValueWithUnderscores_whenCreateRealLiteralAST_expectSuccessfulInit() {
            // given
            val value = "-934198_8531.93518_493156"

            // when
            RealLiteralAST(value)

            // expect nothing
        }

        @Test
        fun givenRealValueStartsWithUnderscores_whenCreateRealLiteralAST_expectFailedInit() {
            // given
            val value = "_0315.1356"

            // expect
            assertFailsWith<InvalidFormatException> {
                RealLiteralAST(value)
            }
        }

        @Test
        fun givenRealValueWithUnderscoreBeforePoint_whenCreateRealLiteralAST_expectFailedInit() {
            // given
            val value = "0315_.1356"

            // expect
            assertFailsWith<InvalidFormatException> {
                RealLiteralAST(value)
            }
        }

        @Test
        fun givenRealValueWithUnderscoreAfterPoint_whenCreateRealLiteralAST_expectFailedInit() {
            // given
            val value = "0315._1356"

            // expect
            assertFailsWith<InvalidFormatException> {
                RealLiteralAST(value)
            }
        }

        @Test
        fun givenRealValueEndsWithUnderscore_whenCreateRealLiteralAST_expectFailedInit() {
            // given
            val value = "0315.1356_"

            // expect
            assertFailsWith<InvalidFormatException> {
                RealLiteralAST(value)
            }
        }
    }
}
