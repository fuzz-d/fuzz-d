package fuzzd.generator.ast

import fuzzd.generator.ast.ExpressionAST.IdentifierAST
import fuzzd.generator.ast.error.InvalidInputException

sealed class StatementAST : ASTElement {
    class IfStatementAST(
        private val condition: ExpressionAST,
        private val ifBranch: SequenceAST,
        private val elseBranch: SequenceAST
    ) : StatementAST() {
        init {
            if (condition.type() != Type.BoolType) {
                throw InvalidInputException("If statement condition not bool type")
            }
        }

        override fun toString(): String = "if ($condition) {\n$ifBranch\n} else {\n$elseBranch\n}"
    }

    class DeclarationAST(private val identifier: IdentifierAST, private val expr: ExpressionAST) : StatementAST() {
        override fun toString(): String = "var $identifier := $expr;"
    }

    class PrintAST(private val expr: ExpressionAST) : StatementAST() {
        override fun toString(): String = "print $expr;"
    }
}
