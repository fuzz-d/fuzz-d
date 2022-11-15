package fuzzd.generator.ast.operators

import fuzzd.generator.ast.Type

sealed class UnaryOperator(val precedence: Int, private val symbol: String) {
    abstract fun supportsInput(t1: Type): Boolean

    override fun toString(): String = symbol

    object NotOperator : UnaryOperator(10, "!") {
        override fun supportsInput(t1: Type): Boolean = t1 == Type.BoolType
    }

    object NegationOperator : UnaryOperator(5, "-") {
        override fun supportsInput(t1: Type): Boolean = t1 in listOf(Type.RealType, Type.IntType)
    }
}
