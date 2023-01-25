package fuzzd.generator.ast

import fuzzd.generator.ast.ExpressionAST.IdentifierAST
import fuzzd.generator.ast.error.InvalidInputException
import fuzzd.utils.indent

sealed class StatementAST : ASTElement {
    class IfStatementAST(
        private val condition: ExpressionAST,
        private val ifBranch: SequenceAST,
        private val elseBranch: SequenceAST?
    ) : StatementAST() {
        init {
            if (condition.type() != Type.BoolType) {
                throw InvalidInputException("If statement condition not bool type")
            }
        }

        override fun toString(): String {
            val sb = StringBuilder()
            sb.appendLine("if ($condition) {")
            sb.appendLine(ifBranch)
            sb.append("}")

            if (elseBranch != null) {
                sb.append(" else {\n")
                sb.append(elseBranch)
                sb.append("\n}")
            }

            sb.append("\n")
            return sb.toString()
        }
    }

    class WhileLoopAST(
        private val counterInitialisation: DeclarationAST,
        private val terminationCheck: IfStatementAST,
        private val condition: ExpressionAST,
        private val body: SequenceAST,
        private val counterUpdate: AssignmentAST
    ) : StatementAST() {
        override fun toString(): String {
            val sb = StringBuilder()
            sb.appendLine(counterInitialisation)
            sb.appendLine("while ($condition) {")
            sb.appendLine(indent(terminationCheck))
            sb.appendLine(body)
            sb.appendLine(indent(counterUpdate))
            sb.appendLine("}")
            return sb.toString()
        }
    }

    class DeclarationAST(private val identifier: IdentifierAST, private val expr: ExpressionAST) : StatementAST() {
        override fun toString(): String = "var $identifier := $expr;"
    }

    class AssignmentAST(private val identifier: IdentifierAST, private val expr: ExpressionAST) : StatementAST() {
        override fun toString(): String = "$identifier := $expr;"
    }

    class PrintAST(private val expr: ExpressionAST) : StatementAST() {
        override fun toString(): String = "print $expr;"
    }

    class ReturnAST(private val expr: ExpressionAST) : StatementAST() {
        override fun toString(): String = "return $expr"

        fun type() = expr.type()
    }

    object BreakAST : StatementAST() {
        override fun toString(): String = "break;"
    }
}
