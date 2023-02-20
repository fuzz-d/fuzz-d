package fuzzd.generator.ast

import fuzzd.generator.ast.ExpressionAST.IdentifierAST
import fuzzd.utils.indent

open class FunctionMethodAST(
    val signature: FunctionMethodSignatureAST,
    private val body: ExpressionAST,
) : ASTElement {
    constructor(name: String, returnType: Type, params: List<IdentifierAST>, body: ExpressionAST) :
        this(FunctionMethodSignatureAST(name, returnType, params), body)

    fun params(): List<IdentifierAST> = signature.params

    fun name(): String = signature.name

    fun returnType(): Type = signature.returnType

    override fun toString(): String = "$signature {\n${indent(body)}\n}"
}

class FunctionMethodSignatureAST(
    val name: String,
    val returnType: Type,
    val params: List<IdentifierAST>,
) : ASTElement {
    override fun toString(): String =
        "function method $name(${params.joinToString(", ") { param -> "${param.name}: ${param.type()}" }}): $returnType"
}
