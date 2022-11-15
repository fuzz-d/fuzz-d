package fuzzd.generator.ast

sealed class ExpressionAST : ASTElement {

    class BooleanLiteralAST(private val value: Boolean) : ExpressionAST() {
        override fun toString(): String = value.toString()
    }

    class IntegerLiteralAST(private val value: Int) : ExpressionAST() {
        override fun toString(): String = value.toString()
    }
}
