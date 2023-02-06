package fuzzd.generator.ast

import fuzzd.generator.ast.ExpressionAST.IdentifierAST

class TraitAST(
    val name: String,
    private val extends: List<TraitAST>,
    private val functionMethods: List<FunctionMethodSignatureAST>,
    private val methods: List<MethodSignatureAST>,
    private val fields: List<IdentifierAST>
) : ASTElement {
    override fun toString(): String {
        val sb = StringBuilder()

        sb.append("trait $name")

        if (extends.isNotEmpty()) {
            sb.append(" extends ${extends.joinToString(", ")}")
        }

        sb.append("{\n")
        sb.append(fields.joinToString(";\n"))
        sb.append(functionMethods.joinToString(";\n"))
        sb.append(methods.joinToString(";\n"))
        sb.append("\n}")

        return sb.toString()
    }
}
