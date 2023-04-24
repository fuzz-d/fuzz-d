package fuzzd.generator.ast.operators

import fuzzd.generator.ast.ASTElement
import fuzzd.generator.ast.Type
import fuzzd.generator.ast.Type.BoolType
import fuzzd.generator.ast.Type.IntType
import fuzzd.generator.ast.Type.LiteralType
import fuzzd.generator.ast.Type.MapType
import fuzzd.generator.ast.Type.MultisetType
import fuzzd.generator.ast.Type.SequenceType
import fuzzd.generator.ast.Type.SetType

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

    override fun toString(): String = symbol

    /* -------------------------------------- Logical Binary Operators -------------------------------------- */
    sealed class BooleanBinaryOperator(precedence: Int, symbol: String) : BinaryOperator(precedence, symbol) {
        override fun supportsInput(t1: Type, t2: Type): Boolean =
            t1 == BoolType && t2 == BoolType
    }

    object IffOperator : BooleanBinaryOperator(1, "<==>")
    object ImplicationOperator : BooleanBinaryOperator(2, "==>")
    object ReverseImplicationOperator : BooleanBinaryOperator(2, "<==")
    object ConjunctionOperator : BooleanBinaryOperator(3, "&&")
    object DisjunctionOperator : BooleanBinaryOperator(3, "||")

    sealed class ComparisonBinaryOperator(symbol: String) : BinaryOperator(4, symbol) {
        override fun supportsInput(t1: Type, t2: Type): Boolean = t1 == t2 && t1 is LiteralType
        override fun outputType(t1: Type, t2: Type): Type = BoolType
    }

    object LessThanOperator : ComparisonBinaryOperator("<")
    object LessThanEqualOperator : ComparisonBinaryOperator("<=")
    object GreaterThanOperator : ComparisonBinaryOperator(">")
    object GreaterThanEqualOperator : ComparisonBinaryOperator(">=")
    object EqualsOperator : ComparisonBinaryOperator("==")
    object NotEqualsOperator : ComparisonBinaryOperator("!=")

    /* -------------------------------- NUMERICAL (& LIST) OPERATORS --------------------------------- */

    sealed class MathematicalBinaryOperator(
        precedence: Int,
        symbol: String,
    ) : BinaryOperator(precedence, symbol) {
        override fun outputType(t1: Type, t2: Type): Type = IntType
        override fun supportsInput(t1: Type, t2: Type): Boolean = t1 == t2 && t1 == IntType
    }

    object AdditionOperator : MathematicalBinaryOperator(1, "+")
    object SubtractionOperator : MathematicalBinaryOperator(1, "-")
    object MultiplicationOperator : MathematicalBinaryOperator(2, "*")
    object DivisionOperator : MathematicalBinaryOperator(2, "/")
    object ModuloOperator : MathematicalBinaryOperator(2, "%")

    /* ------------------------------ DATA STRUCTURE OPERATORS -------------------------------- */

    sealed class DataStructureBinaryOperator(precedence: Int, symbol: String) : BinaryOperator(precedence, symbol) {
        override fun outputType(t1: Type, t2: Type): Type = BoolType
        override fun supportsInput(t1: Type, t2: Type): Boolean =
            (t1 is LiteralType && t1 == t2) || t1 is SetType || t1 is MultisetType || t1 is MapType || t1 is SequenceType
    }

    object DataStructureEqualityOperator : DataStructureBinaryOperator(4, "==")
    object DataStructureInequalityOperator : DataStructureBinaryOperator(4, "!=")

    sealed class DataStructureMembershipOperator(symbol: String) : BinaryOperator(4, symbol) {
        override fun outputType(t1: Type, t2: Type): Type = BoolType
        override fun supportsInput(t1: Type, t2: Type) =
            t2 is SetType && t1 == t2.innerType ||
                t2 is MultisetType && t1 == t2.innerType ||
                t2 is MapType && t1 == t2.keyType ||
                t2 is SequenceType && t1 == t2.innerType
    }

    object MembershipOperator : DataStructureMembershipOperator("in")
    object AntiMembershipOperator : DataStructureMembershipOperator("!in")

    sealed class DataStructureComparisonOperator(symbol: String) : DataStructureBinaryOperator(4, symbol) {
        override fun outputType(t1: Type, t2: Type): Type = BoolType
        override fun supportsInput(t1: Type, t2: Type): Boolean =
            t1 == t2 && (t1 is SetType || t1 is MultisetType || t1 is SequenceType)
    }

    object ProperSubsetOperator : DataStructureComparisonOperator("<")
    object SubsetOperator : DataStructureComparisonOperator("<=")
    object SupersetOperator : DataStructureComparisonOperator(">=")
    object ProperSupersetOperator : DataStructureComparisonOperator(">")

    object DisjointOperator : DataStructureComparisonOperator("!!") {
        override fun supportsInput(t1: Type, t2: Type): Boolean = t1 == t2 && (t1 is SetType || t1 is MultisetType)
    }

    sealed class DataStructureMathematicalOperator(precedence: Int, symbol: String) :
        DataStructureBinaryOperator(precedence, symbol) {
        override fun outputType(t1: Type, t2: Type): Type = t1
        override fun supportsInput(t1: Type, t2: Type): Boolean = t1 == t2 && (t1 is SetType || t1 is MultisetType)
    }

    object UnionOperator : DataStructureMathematicalOperator(6, "+") {
        override fun supportsInput(t1: Type, t2: Type): Boolean =
            t1 == t2 || (t1 is SetType || t1 is MultisetType || t1 is MapType || t1 is SequenceType)
    }

    object DifferenceOperator : DataStructureMathematicalOperator(6, "-") {
        override fun supportsInput(t1: Type, t2: Type): Boolean =
            (t1 == t2 && (t1 is SetType || t1 is MultisetType || t1 is MapType)) ||
                ((t1 is MapType || t1 is MultisetType) && t2 is SetType)
    }

    object IntersectionOperator : DataStructureMathematicalOperator(7, "*")

    companion object {
        fun isBinaryType(type: Type): Boolean {
            return BinaryOperator::class.sealedSubclasses
                .any { opType ->
                    opType.sealedSubclasses.any { op ->
                        op.objectInstance?.supportsInput(type, type) ?: false
                    }
                }
        }
    }
}
