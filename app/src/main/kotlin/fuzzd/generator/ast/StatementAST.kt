package fuzzd.generator.ast

import fuzzd.generator.ast.ExpressionAST.IdentifierAST
import fuzzd.generator.ast.error.InvalidInputException

sealed class StatementAST : ASTElement {
    class IfStatementAST(
        val condition: ExpressionAST,
        val ifBranch: SequenceAST,
        val elseBranch: SequenceAST? = null,
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

    open class WhileLoopAST(val condition: ExpressionAST, open val body: SequenceAST) : StatementAST() {
        init {
            if (condition.type() != Type.BoolType) {
                throw InvalidInputException("If statement condition not bool type")
            }
        }

        open fun body(): SequenceAST = body

        override fun toString(): String = "while ($condition) {\n${body()}\n}"
    }

    class CounterLimitedWhileLoopAST(
        val counterInitialisation: DeclarationAST,
        val terminationCheck: IfStatementAST,
        val counterUpdate: AssignmentAST,
        condition: ExpressionAST,
        override val body: SequenceAST,
    ) : WhileLoopAST(condition, SequenceAST(listOf(terminationCheck, counterUpdate) + body.statements)) {
        override fun body(): SequenceAST = SequenceAST(listOf(terminationCheck, counterUpdate) + body.statements)

        override fun toString(): String = "$counterInitialisation\n${super.toString()}"
    }

    class DataStructureMemberDeclarationAST(val identifier: IdentifierAST, val dataStructure: IdentifierAST) : StatementAST() {
        override fun toString(): String = "var $identifier :| $identifier in $dataStructure;"
    }

    open class MultiDeclarationAST(
        val identifiers: List<IdentifierAST>,
        val exprs: List<ExpressionAST>,
    ) : StatementAST() {
        override fun toString(): String = "var ${identifiers.joinToString(", ")} := ${exprs.joinToString(", ")};"
    }

    class DeclarationAST(identifier: IdentifierAST, expr: ExpressionAST) :
        MultiDeclarationAST(listOf(identifier), listOf(expr))

    open class MultiTypedDeclarationAST(val identifiers: List<IdentifierAST>, val exprs: List<ExpressionAST>) :
        StatementAST() {
        override fun toString(): String {
            val sb = StringBuilder()
            sb.append("var ${identifiers.joinToString(", ") { "${it.name}: ${it.type()}" }}")
            if (exprs.isNotEmpty()) {
                sb.append(" := ${exprs.joinToString(", ")}")
            }
            sb.append(";")
            return sb.toString()
        }
    }

    class TypedDeclarationAST(val identifier: IdentifierAST, val expr: ExpressionAST? = null) :
        MultiTypedDeclarationAST(listOf(identifier), if (expr != null) listOf(expr) else emptyList())

    open class MultiAssignmentAST(
        val identifiers: List<IdentifierAST>,
        val exprs: List<ExpressionAST>,
    ) : StatementAST() {
        override fun toString(): String = "${identifiers.joinToString(", ")} := ${exprs.joinToString(", ")};"
    }

    class AssignmentAST(identifier: IdentifierAST, expr: ExpressionAST) :
        MultiAssignmentAST(listOf(identifier), listOf(expr))

    class PrintAST(val expr: List<ExpressionAST>) : StatementAST() {
        constructor(expr: ExpressionAST) : this(listOf(expr))

        override fun toString(): String = "print ${expr.joinToString(", ")}, \"\\n\";"
    }

    open class VoidMethodCallAST(val method: MethodSignatureAST, val params: List<ExpressionAST>) : StatementAST() {
        init {
            val methodName = method.name

            if (method.returns.isNotEmpty()) {
                throw InvalidInputException("Generating invalid method call to non-void method $methodName")
            }

            val methodParams = method.params

            if (params.size != methodParams.size) {
                throw InvalidInputException("Generating method call to $methodName with incorrect no. of parameters. Got ${params.size}, expected ${methodParams.size}")
            }

            (params.indices).forEach { i ->
                val paramType = params[i].type()
                val expectedType = methodParams[i].type()
                if (paramType != expectedType) {
                    throw InvalidInputException("Method call parameter type mismatch for parameter $i. Expected $expectedType, got $paramType")
                }
            }
        }

        override fun toString(): String = "${method.name}(${params.joinToString(", ")});"
    }

    object BreakAST : StatementAST() {
        override fun toString(): String = "break;"
    }
}
