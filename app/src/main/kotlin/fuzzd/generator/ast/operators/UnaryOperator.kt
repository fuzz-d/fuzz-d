package fuzzd.generator.ast.operators

import fuzzd.generator.ast.ASTElement
import fuzzd.generator.ast.Type
import fuzzd.generator.ast.Type.BoolType
import fuzzd.generator.ast.Type.IntType

sealed class UnaryOperator(val precedence: Int, private val symbol: String) : ASTElement {
    abstract fun supportsInput(t1: Type): Boolean

    override fun toString(): String = symbol

    object NotOperator : UnaryOperator(10, "!") {
        override fun supportsInput(t1: Type): Boolean = t1 == BoolType
    }

    object NegationOperator : UnaryOperator(5, "-") {
        override fun supportsInput(t1: Type): Boolean = t1 in listOf(IntType)
    }

    companion object {
        fun isUnaryType(type: Type): Boolean {
            return UnaryOperator::class.sealedSubclasses
                .any { op ->
                    op.objectInstance?.supportsInput(type) ?: false
                }
        }
    }
}
