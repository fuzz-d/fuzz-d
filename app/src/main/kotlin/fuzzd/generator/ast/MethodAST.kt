package fuzzd.generator.ast

import fuzzd.generator.ast.ExpressionAST.IdentifierAST
import fuzzd.generator.ast.StatementAST.ReturnAST
import fuzzd.generator.ast.error.InvalidInputException

class MethodAST(
    private val name: String,
    private val params: List<IdentifierAST>,
    private val returnType: Type?, // null => Unit/void
    private val body: SequenceAST,
    private val returnStatement: ReturnAST?
) : ASTElement {
    init {
        if (returnType != null && returnStatement == null) {
            throw InvalidInputException("Expected return statement of type $returnType for function $name")
        }

        if (returnType != null && returnStatement?.type() != returnType) {
            throw InvalidInputException("Return statement type did not match method return type for $name. Got: ${returnStatement?.type()}, expected: $returnType")
        }
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("method $name(${params.joinToString(", ")})} ")
        if (returnType != null) {
            sb.append(": $returnType ")
        }
        sb.appendLine("{")
        sb.appendLine(body)
        if (returnType != null) {
            sb.appendLine(returnStatement)
        }
        sb.appendLine("}")

        return sb.toString()
    }
}
