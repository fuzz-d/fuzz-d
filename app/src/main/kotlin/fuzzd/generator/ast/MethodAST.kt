package fuzzd.generator.ast

import fuzzd.generator.ast.ExpressionAST.IdentifierAST

class MethodAST(
    val name: String,
    val params: List<IdentifierAST>,
    val returns: List<IdentifierAST>
) : ASTElement {
    private val calls = mutableSetOf<MethodAST>()
    private lateinit var body: SequenceAST

    fun calls(method: MethodAST) = method in calls

    fun addCall(method: MethodAST) {
        calls.add(method)
    }

    fun setBody(body: SequenceAST) {
        this.body = body
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("method $name(${params.joinToString(", ") { p -> "$p: ${p.type()}" }}) ")

        if (returns.isNotEmpty()) {
            sb.append("returns (${returns.joinToString(", ") { p -> "$p: ${p.type()}" }}) ")
        }

        sb.appendLine("{")
        sb.appendLine(body)
        sb.appendLine("}")

        return sb.toString()
    }
}
