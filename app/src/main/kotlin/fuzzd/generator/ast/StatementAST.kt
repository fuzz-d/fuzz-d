package fuzzd.generator.ast

sealed class StatementAST : ASTElement {

    class PrintAST(private val expr: ExpressionAST) : StatementAST() {
        override fun toString(): String {
            return "print $expr;"
        }
    }
}
