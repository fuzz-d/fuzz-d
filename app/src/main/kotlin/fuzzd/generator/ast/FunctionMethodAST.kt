package fuzzd.generator.ast

import fuzzd.generator.ast.ExpressionAST.IdentifierAST
import fuzzd.utils.indent

class FunctionMethodAST(
    val name: String,
    val returnType: Type,
    val params: List<IdentifierAST>,
    private val body: ExpressionAST
) : ASTElement {
    override fun toString(): String {
        val sb = StringBuilder()

        sb.append("function method $name(")
        sb.append(params.joinToString(",") { param -> "${param.name}: ${param.type()}" })
        sb.append("): $returnType {\n")
        sb.append(indent(body))
        sb.append("\n}")

        return sb.toString()
    }
}
