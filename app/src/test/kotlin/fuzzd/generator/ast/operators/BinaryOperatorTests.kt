package fuzzd.generator.ast.operators

import fuzzd.generator.ast.ClassAST
import fuzzd.generator.ast.Type.BoolType
import fuzzd.generator.ast.Type.ClassType
import fuzzd.generator.ast.Type.IntType
import fuzzd.generator.ast.operators.BinaryOperator.AdditionOperator
import fuzzd.generator.ast.operators.BinaryOperator.BooleanBinaryOperator
import fuzzd.generator.ast.operators.BinaryOperator.Companion.isBinaryType
import fuzzd.generator.ast.operators.BinaryOperator.ComparisonBinaryOperator
import fuzzd.generator.ast.operators.BinaryOperator.ConjunctionOperator
import fuzzd.generator.ast.operators.BinaryOperator.DisjunctionOperator
import fuzzd.generator.ast.operators.BinaryOperator.DivisionOperator
import fuzzd.generator.ast.operators.BinaryOperator.EqualsOperator
import fuzzd.generator.ast.operators.BinaryOperator.GreaterThanEqualOperator
import fuzzd.generator.ast.operators.BinaryOperator.GreaterThanOperator
import fuzzd.generator.ast.operators.BinaryOperator.IffOperator
import fuzzd.generator.ast.operators.BinaryOperator.ImplicationOperator
import fuzzd.generator.ast.operators.BinaryOperator.LessThanEqualOperator
import fuzzd.generator.ast.operators.BinaryOperator.LessThanOperator
import fuzzd.generator.ast.operators.BinaryOperator.MathematicalBinaryOperator
import fuzzd.generator.ast.operators.BinaryOperator.ModuloOperator
import fuzzd.generator.ast.operators.BinaryOperator.MultiplicationOperator
import fuzzd.generator.ast.operators.BinaryOperator.NotEqualsOperator
import fuzzd.generator.ast.operators.BinaryOperator.ReverseImplicationOperator
import fuzzd.generator.ast.operators.BinaryOperator.SubtractionOperator
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import java.util.stream.Stream
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BinaryOperatorTests {

    @ParameterizedTest
    @ArgumentsSource(BooleanBinaryOperatorProvider::class)
    fun givenBooleanBinaryOperator_whenSupportsInputBoolBool_expectTrue(booleanBinaryOperator: BooleanBinaryOperator) {
        // when
        val support = booleanBinaryOperator.supportsInput(BoolType, BoolType)

        // expect
        assertTrue { support }
    }

    @ParameterizedTest
    @ArgumentsSource(BooleanBinaryOperatorProvider::class)
    fun givenBooleanBinaryOperator_whenSupportsInputIntBool_expectFalse(booleanBinaryOperator: BooleanBinaryOperator) {
        // when
        val support = booleanBinaryOperator.supportsInput(IntType, BoolType)

        // expect
        assertFalse { support }
    }

    @ParameterizedTest
    @ArgumentsSource(BooleanBinaryOperatorProvider::class)
    fun givenBooleanBinaryOperator_whenSupportsInputBoolInt_expectFalse(booleanBinaryOperator: BooleanBinaryOperator) {
        // when
        val support = booleanBinaryOperator.supportsInput(BoolType, IntType)

        // expect
        assertFalse { support }
    }

    @ParameterizedTest
    @ArgumentsSource(ComparisonBinaryOperatorProvider::class)
    fun givenComparisonBinaryOperator_whenSupportsInputIntInt_expectTrue(comparisonBinaryOperator: ComparisonBinaryOperator) {
        // when
        val support = comparisonBinaryOperator.supportsInput(IntType, IntType)

        // expect
        assertTrue { support }
    }

    @ParameterizedTest
    @ArgumentsSource(ComparisonBinaryOperatorProvider::class)
    fun givenComparisonBinaryOperator_whenSupportsInputBoolInt_expectFalse(comparisonBinaryOperator: ComparisonBinaryOperator) {
        // when
        val support = comparisonBinaryOperator.supportsInput(BoolType, IntType)

        // expect
        assertFalse { support }
    }

    @ParameterizedTest
    @ArgumentsSource(ComparisonBinaryOperatorProvider::class)
    fun givenComparisonBinaryOperator_whenSupportsInputIntBool_expectFalse(comparisonBinaryOperator: ComparisonBinaryOperator) {
        // when
        val support = comparisonBinaryOperator.supportsInput(IntType, BoolType)

        // expect
        assertFalse { support }
    }

    @ParameterizedTest
    @ArgumentsSource(MathematicalBinaryOperatorProvider::class)
    fun givenMathematicalBinaryOperator_whenSupportsInputIntInt_expectTrue(mathematicalBinaryOperator: MathematicalBinaryOperator) {
        // when
        val support = mathematicalBinaryOperator.supportsInput(IntType, IntType)

        // expect
        assertTrue { support }
    }

    @ParameterizedTest
    @ArgumentsSource(MathematicalBinaryOperatorProvider::class)
    fun givenMathematicalBinaryOperator_whenSupportsInputBoolInt_expectFalse(mathematicalBinaryOperator: MathematicalBinaryOperator) {
        // when
        val support = mathematicalBinaryOperator.supportsInput(BoolType, IntType)

        // expect
        assertFalse { support }
    }

    @ParameterizedTest
    @ArgumentsSource(MathematicalBinaryOperatorProvider::class)
    fun givenMathematicalBinaryOperator_whenSupportsInputIntBool_expectFalse(mathematicalBinaryOperator: MathematicalBinaryOperator) {
        // when
        val support = mathematicalBinaryOperator.supportsInput(IntType, BoolType)

        // expect
        assertFalse { support }
    }

    @Test
    fun givenTypeSupportingBinaryOperations_whenIsBinaryType_expectTrue() {
        // given
        val type = IntType

        // when
        val isBinary = isBinaryType(type)

        // expect
        assertTrue { isBinary }
    }

    @Test
    fun givenTypeNotSupportingBinaryOperators_whenIsBinaryType_expectFalse() {
        // given
        val type = ClassType(ClassAST.builder().withName("C1").build())

        // when
        val isBinary = isBinaryType(type)

        // expect
        assertFalse { isBinary }
    }

    class BooleanBinaryOperatorProvider : ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> = Stream.of(
            Arguments.of(IffOperator),
            Arguments.of(ImplicationOperator),
            Arguments.of(ReverseImplicationOperator),
            Arguments.of(ConjunctionOperator),
            Arguments.of(DisjunctionOperator),
        )
    }

    class ComparisonBinaryOperatorProvider : ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> = Stream.of(
            Arguments.of(LessThanOperator),
            Arguments.of(LessThanEqualOperator),
            Arguments.of(GreaterThanOperator),
            Arguments.of(GreaterThanEqualOperator),
            Arguments.of(EqualsOperator),
            Arguments.of(NotEqualsOperator),
        )
    }

    class MathematicalBinaryOperatorProvider : ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> = Stream.of(
            Arguments.of(AdditionOperator),
            Arguments.of(SubtractionOperator),
            Arguments.of(MultiplicationOperator),
            Arguments.of(DivisionOperator),
            Arguments.of(ModuloOperator),
        )
    }
}
