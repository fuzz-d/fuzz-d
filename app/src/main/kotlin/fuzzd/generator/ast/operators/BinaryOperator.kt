package fuzzd.generator.ast.operators

import fuzzd.generator.ast.ASTElement
import fuzzd.generator.ast.Type
import fuzzd.generator.ast.Type.BoolType
import fuzzd.generator.ast.Type.CharType
import fuzzd.generator.ast.Type.IntType
import fuzzd.generator.ast.Type.RealType

/**
 * Supported operators with associated precedences taken from documentation
 * More advanced enum class with extra data available + callable methods
 */
sealed class BinaryOperator(val precedence: Int, private val symbol: String) : ASTElement {
    /**
     * accounts for the majority of binary operator cases
     * where input types and output type are the same.
     */
    open fun outputType(t1: Type, t2: Type): Type {
        if (t1 != t2) throw UnsupportedOperationException()
        return t1
    }

    abstract fun supportsInput(t1: Type, t2: Type): Boolean

    /* -------------------------------------- Logical Binary Operators -------------------------------------- */
    abstract class BooleanBinaryOperator(precedence: Int, symbol: String) : BinaryOperator(precedence, symbol) {
        override fun supportsInput(t1: Type, t2: Type): Boolean =
            t1 == BoolType && t2 == BoolType
    }

    override fun toString(): String = symbol

    object IffOperator : BooleanBinaryOperator(1, "<==>")
    object ImplicationOperator : BooleanBinaryOperator(2, "==>")
    object ReverseImplicationOperator : BooleanBinaryOperator(2, "<==")
    object ConjunctionOperator : BooleanBinaryOperator(3, "&&")
    object DisjunctionOperator : BooleanBinaryOperator(3, "||")

    abstract class ComparisonBinaryOperator(symbol: String) : BinaryOperator(4, symbol) {
        private val supportedInputTypes = listOf(IntType, CharType, RealType)
        override fun supportsInput(t1: Type, t2: Type): Boolean =
            t1 == t2 && t1 in supportedInputTypes

        override fun outputType(t1: Type, t2: Type): Type = BoolType
    }

    object LessThanOperator : ComparisonBinaryOperator("<")
    object LessThanEqualOperator : ComparisonBinaryOperator("<=")
    object GreaterThanOperator : ComparisonBinaryOperator(">")
    object GreaterThanEqualOperator : ComparisonBinaryOperator(">=")
    object EqualsOperator : ComparisonBinaryOperator("==")
    object NotEqualsOperator : ComparisonBinaryOperator("!=")

    /* -------------------------------- NUMERICAL (& LIST) OPERATORS --------------------------------- */

    abstract class MathematicalBinaryOperator(
        precedence: Int,
        symbol: String,
        private val supportedInputTypes: List<Type> = listOf(IntType, RealType)
    ) : BinaryOperator(precedence, symbol) {
        override fun supportsInput(t1: Type, t2: Type): Boolean =
            t1 == t2 && t1 in supportedInputTypes
    }

    object AdditionOperator : MathematicalBinaryOperator(1, "+", listOf(CharType, IntType, RealType))
    object SubtractionOperator : MathematicalBinaryOperator(1, "-", listOf(CharType, IntType, RealType))
    object MultiplicationOperator : MathematicalBinaryOperator(2, "*")
    object DivisionOperator : MathematicalBinaryOperator(2, "/")
    object ModuloOperator : MathematicalBinaryOperator(2, "%")
}
