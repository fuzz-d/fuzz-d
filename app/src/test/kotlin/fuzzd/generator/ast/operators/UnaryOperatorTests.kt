package fuzzd.generator.ast.operators

import fuzzd.generator.ast.ClassAST
import fuzzd.generator.ast.Type.BoolType
import fuzzd.generator.ast.Type.ClassType
import fuzzd.generator.ast.Type.IntType
import fuzzd.generator.ast.operators.UnaryOperator.Companion.isUnaryType
import fuzzd.generator.ast.operators.UnaryOperator.NegationOperator
import fuzzd.generator.ast.operators.UnaryOperator.NotOperator
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UnaryOperatorTests {
    @Test
    fun givenNotOperator_whenSupportsInputBool_expectTrue() {
        // when
        val support = NotOperator.supportsInput(BoolType)

        // expect
        assertTrue { support }
    }

    @Test
    fun givenNotOperator_whenSupportsInputInt_expectFalse() {
        // when
        val support = NotOperator.supportsInput(IntType)

        // expect
        assertFalse { support }
    }

    @Test
    fun givenNegationOperator_whenSupportsInputInt_expectTrue() {
        // when
        val support = NegationOperator.supportsInput(IntType)

        // expect
        assertTrue { support }
    }

    @Test
    fun givenNegationOperator_whenSupportsInputBool_expectFalse() {
        // when
        val support = NegationOperator.supportsInput(BoolType)

        // expect
        assertFalse { support }
    }

    @Test
    fun givenUnaryType_whenIsUnaryType_expectTrue() {
        // when
        val isUnaryTypeInt = isUnaryType(IntType)
        val isUnaryTypeBool = isUnaryType(BoolType)

        // expect
        assertTrue { isUnaryTypeInt }
        assertTrue { isUnaryTypeBool }
    }

    @Test
    fun givenNonUnaryType_whenIsUnaryType_expectFalse() {
        // given
        val type = ClassType(ClassAST.builder().withName("C1").build())

        // when
        val isUnary = isUnaryType(type)

        // expect
        assertFalse { isUnary }
    }
}
