package fuzzd.generator.ast

import fuzzd.generator.ast.ExpressionAST.BinaryExpressionAST
import fuzzd.generator.ast.ExpressionAST.BooleanLiteralAST
import fuzzd.generator.ast.ExpressionAST.IntegerLiteralAST
import fuzzd.generator.ast.ExpressionAST.RealLiteralAST
import fuzzd.generator.ast.ExpressionAST.UnaryExpressionAST
import fuzzd.generator.ast.operators.BinaryOperator.AdditionOperator
import fuzzd.generator.ast.operators.BinaryOperator.ConjunctionOperator
import fuzzd.generator.ast.operators.BinaryOperator.DisjunctionOperator
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
