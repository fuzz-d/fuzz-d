package fuzzd.generator.ast

import fuzzd.generator.ast.ExpressionAST.IdentifierAST

class MethodAST(
    val name: String,
    val params: List<IdentifierAST>,
    val returns: List<IdentifierAST>,
    private val body: SequenceAST
) : ASTElement {
    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("method $name(${params.joinToString(", ")})} returns (${returns.joinToString(", ") }})")
        sb.appendLine("{")
        sb.appendLine(body)
        sb.appendLine("}")

        return sb.toString()
    }
}
