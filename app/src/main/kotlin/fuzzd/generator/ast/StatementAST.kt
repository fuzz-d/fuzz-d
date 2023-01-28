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

    open class MultiDeclarationAST(
        private val identifiers: List<IdentifierAST>,
        private val exprs: List<ExpressionAST>
    ) :
        StatementAST() {
        override fun toString(): String = "var ${identifiers.joinToString(", ")} := ${exprs.joinToString(", ")};"
    }

    class DeclarationAST(private val identifier: IdentifierAST, private val expr: ExpressionAST) :
        MultiDeclarationAST(listOf(identifier), listOf(expr))

    open class MultiAssignmentAST(
        private val identifiers: List<IdentifierAST>,
        private val exprs: List<ExpressionAST>
    ) :
        StatementAST() {
        override fun toString(): String = "${identifiers.joinToString(", ")} := ${exprs.joinToString(", ")};"
    }

    class AssignmentAST(private val identifier: IdentifierAST, private val expr: ExpressionAST) :
        MultiAssignmentAST(listOf(identifier), listOf(expr))

    class PrintAST(private val expr: ExpressionAST) : StatementAST() {
        override fun toString(): String = "print $expr;"
    }

    class VoidMethodCallAST(private val method: MethodAST, private val params: List<ExpressionAST>) : StatementAST() {
        init {
            if (method.returns.isNotEmpty()) {
                throw InvalidInputException("Generating invalid method call to non-void method ${method.name}")
            }

            if (params.size != method.params.size) {
                throw InvalidInputException("Generating method call to ${method.name} with incorrect no. of parameters. Got ${params.size}, expected ${method.params.size}")
            }

            (params.indices).forEach { i ->
                val paramType = params[i].type()
                val expectedType = method.params[i].type()
                if (paramType != expectedType) {
                    throw InvalidInputException("Method call parameter type mismatch for parameter $i. Expected $expectedType, got $paramType")
                }
            }
        }

        override fun toString(): String {
            val sb = StringBuilder()
            sb.append("${method.name}(${params.joinToString(", ")})")
            return sb.toString()
        }
    }

    class ReturnAST(private val expr: ExpressionAST) : StatementAST() {
        override fun toString(): String = "return $expr"

        fun type() = expr.type()
    }

    object BreakAST : StatementAST() {
        override fun toString(): String = "break;"
    }
}
