package fuzzd.generator.ast

import fuzzd.generator.ast.ExpressionAST.IdentifierAST
import fuzzd.utils.indent

open class FunctionMethodAST(
    val signature: FunctionMethodSignatureAST,
    val body: ExpressionAST,
) : TopLevelAST() {
    constructor(name: String, returnType: Type, params: List<IdentifierAST>, annotations: List<VerifierAnnotationAST>, body: ExpressionAST) :
        this(FunctionMethodSignatureAST(name, returnType, params, annotations), body)

    fun params(): List<IdentifierAST> = signature.params

    fun name(): String = signature.name

    fun returnType(): Type = signature.returnType

    override fun toString(): String = "$signature{\n${indent(body)}\n}"

    override fun equals(other: Any?): Boolean =
        other is FunctionMethodAST && other.signature == signature && other.body == body

    override fun hashCode(): Int {
        var result = signature.hashCode()
        result = 31 * result + body.hashCode()
        return result
    }
}

open class FunctionMethodSignatureAST(
    val name: String,
    val returnType: Type,
    val params: List<IdentifierAST>,
    val annotations: List<VerifierAnnotationAST>,
) : ASTElement {
    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("function $name(${params.joinToString(", ") { param -> "${param.name}: ${param.type()}" }}): $returnType ")

        if (annotations.isNotEmpty()) {
            sb.append("\n")
            annotations.forEach { sb.appendLine(indent(it)) }
        }

        return sb.toString()
    }

    override fun equals(other: Any?): Boolean =
        other is FunctionMethodSignatureAST && other.name == name && other.returnType == returnType && other.params == params

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + returnType.hashCode()
        result = 31 * result + params.hashCode()
        return result
    }
}

class ClassInstanceFunctionMethodSignatureAST(
    val classInstance: IdentifierAST,
    val signature: FunctionMethodSignatureAST,
) : FunctionMethodSignatureAST("$classInstance.${signature.name}", signature.returnType, signature.params, signature.annotations)
