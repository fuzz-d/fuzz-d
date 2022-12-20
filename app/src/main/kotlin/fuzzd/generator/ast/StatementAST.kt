package fuzzd.generator.ast

import fuzzd.generator.ast.ExpressionAST.IdentifierAST

sealed class StatementAST : ASTElement {

    class DeclarationAST(val identifier: IdentifierAST, private val expr: ExpressionAST) : StatementAST() {
        override fun toString(): String = "var $identifier := $expr;"
    }

    class PrintAST(private val expr: ExpressionAST) : StatementAST() {
        override fun toString(): String = "print $expr;"
    }
}
