package fuzzd.generator.ast

import fuzzd.generator.ast.ExpressionAST.IdentifierAST
import fuzzd.generator.ast.StatementAST.ReturnAST

class MethodAST(
    private val name: String,
    private val params: List<IdentifierAST>,
    private val returnType: Type?, // null => Unit/void
    private val body: SequenceAST,
    private val returnStatement: ReturnAST?
) : ASTElement {
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
