package fuzzd.generator.ast

import fuzzd.generator.ast.ExpressionAST.ArrayIdentifierAST
import fuzzd.generator.ast.ExpressionAST.ArrayIndexAST
import fuzzd.generator.ast.ExpressionAST.BinaryExpressionAST
import fuzzd.generator.ast.ExpressionAST.BooleanLiteralAST
import fuzzd.generator.ast.ExpressionAST.CharacterLiteralAST
import fuzzd.generator.ast.ExpressionAST.FunctionMethodCallAST
import fuzzd.generator.ast.ExpressionAST.IntegerLiteralAST
import fuzzd.generator.ast.ExpressionAST.RealLiteralAST
import fuzzd.generator.ast.ExpressionAST.UnaryExpressionAST
import fuzzd.generator.ast.FunctionMethodAST.Companion.ABSOLUTE
import fuzzd.generator.ast.FunctionMethodAST.Companion.MAKE_NOT_ZERO_INT
import fuzzd.generator.ast.FunctionMethodAST.Companion.MAKE_NOT_ZERO_REAL
import fuzzd.generator.ast.FunctionMethodAST.Companion.SAFE_SUBTRACT_CHAR
import fuzzd.generator.ast.Type.ArrayType
import fuzzd.generator.ast.Type.IntType
import fuzzd.generator.ast.operators.BinaryOperator.AdditionOperator
import fuzzd.generator.ast.operators.BinaryOperator.ConjunctionOperator
import fuzzd.generator.ast.operators.BinaryOperator.DisjunctionOperator
import fuzzd.generator.ast.operators.BinaryOperator.DivisionOperator
import fuzzd.generator.ast.operators.BinaryOperator.ModuloOperator
import fuzzd.generator.ast.operators.BinaryOperator.SubtractionOperator
import fuzzd.generator.ast.operators.UnaryOperator.NotOperator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

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
        fun givenBinaryExpressionWithBooleanBinaryChild_whenToString_expectParenWrap() {
            // given
            val lhs = BinaryExpressionAST(BooleanLiteralAST(false), ConjunctionOperator, BooleanLiteralAST(true))
            val operator = DisjunctionOperator
            val rhs = BooleanLiteralAST(false)
            val bexp = BinaryExpressionAST(lhs, operator, rhs)

            // when
            val str = bexp.toString()

            // expect
            assertEquals("($lhs)$operator$rhs", str)
        }

        @Test
        fun givenBinaryExpressionWithIntBinaryChild_whenToString_expectNoParenWrap() {
            // given
            val lhs = BinaryExpressionAST(IntegerLiteralAST("123"), AdditionOperator, IntegerLiteralAST("124"))
            val operator = SubtractionOperator
            val rhs = IntegerLiteralAST("1234")

            val bexp = BinaryExpressionAST(lhs, operator, rhs)

            // when
            val str = bexp.toString()

            // expect
            assertEquals("$lhs$operator$rhs", str)
        }

        @Test
        fun givenBinaryExpressionWithIntDivision_whenMakeSafe_expectNonZeroWrapper() {
            // given
            val lhs = IntegerLiteralAST("123")
            val operator = DivisionOperator
            val rhs = IntegerLiteralAST("234")
            val expr = BinaryExpressionAST(lhs, operator, rhs)

            // when
            val safe = expr.makeSafe()

            // expect
            val expected =
                BinaryExpressionAST(lhs, DivisionOperator, FunctionMethodCallAST(MAKE_NOT_ZERO_INT, listOf(rhs)))
            assertEquals(expected.toString(), safe.toString())
        }

        @Test
        fun givenBinaryExpressionWithRealDivision_whenMakeSafe_expectNonZeroWrapper() {
            // given
            val lhs = RealLiteralAST("123.3")
            val operator = DivisionOperator
            val rhs = RealLiteralAST("234.3")
            val expr = BinaryExpressionAST(lhs, operator, rhs)

            // when
            val safe = expr.makeSafe()

            // expect
            val expected =
                BinaryExpressionAST(lhs, DivisionOperator, FunctionMethodCallAST(MAKE_NOT_ZERO_REAL, listOf(rhs)))
            assertEquals(expected.toString(), safe.toString())
        }

        @Test
        fun givenBinaryExpressionWithModulo_whenMakeSafe_expectAbsoluteWrapper() {
            // given
            val lhs = IntegerLiteralAST("1234")
            val operator = ModuloOperator
            val rhs = IntegerLiteralAST("2345")
            val expr = BinaryExpressionAST(lhs, operator, rhs)

            // when
            val safe = expr.makeSafe()

            // expect
            val expected = BinaryExpressionAST(
                FunctionMethodCallAST(ABSOLUTE, listOf(lhs)),
                operator,
                FunctionMethodCallAST(MAKE_NOT_ZERO_INT, listOf(rhs))
            )

            assertEquals(expected.toString(), safe.toString())
        }

        @Test
        fun givenBinaryExpressionWithCharSubtraction_whenMakeSafe_expectSafeCharSubtractionWrapper() {
            // given
            val lhs = CharacterLiteralAST("B")
            val operator = SubtractionOperator
            val rhs = CharacterLiteralAST("a")

            val expr = BinaryExpressionAST(lhs, operator, rhs)

            // when
            val safe = expr.makeSafe()

            // expect
            val expected = FunctionMethodCallAST(SAFE_SUBTRACT_CHAR, listOf(lhs, rhs))
            assertEquals(expected.toString(), safe.toString())
        }
    }

    @Nested
    inner class IdentifierASTTests {
        @Test
        fun givenArrayIndexAST_whenMakeSafe_expectAbsoluteWrapperAndModulo() {
            // given
            val array = ArrayIdentifierAST("a", ArrayType(IntType), 5)
            val index = IntegerLiteralAST("43")

            val expr = ArrayIndexAST(array, index)

            // when
            val safe = expr.makeSafe()

            // expect
            val expected = ArrayIndexAST(
                array,
                BinaryExpressionAST(
                    FunctionMethodCallAST(ABSOLUTE, listOf(index)),
                    ModuloOperator,
                    IntegerLiteralAST("5")
                )
            )

            assertEquals(expected.toString(), safe.toString())
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
        fun givenValidIntegerValue_whenHexToString_expectHexString() {
            // given
            val value = "258"

            // when
            val str = IntegerLiteralAST(value, true).toString()

            // expect
            assertEquals("0x102", str)
        }

        @Test
        fun givenValidNegativeIntegerValue_whenHexToString_expectHexString() {
            // given
            val value = "-258"

            // when
            val str = IntegerLiteralAST(value, true).toString()

            // expect
            assertEquals("-0x102", str)
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
        fun givenValidNegativeRealValue_whenCreateRealLiteralAST_expectSuccessfulInit() {
            // given
            val value = "-1234951.95138"

            // when
            RealLiteralAST(value)

            // expect nothing
        }
    }
}
