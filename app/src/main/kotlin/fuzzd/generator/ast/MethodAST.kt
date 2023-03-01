package fuzzd.generator.ast

import fuzzd.generator.ast.ExpressionAST.IdentifierAST

class MethodAST(
    val signature: MethodSignatureAST,
) : TopLevelAST() {
    constructor(name: String, params: List<IdentifierAST>, returns: List<IdentifierAST>) :
        this(MethodSignatureAST(name, params, returns))

    private lateinit var body: SequenceAST

    fun name() = signature.name

    fun params() = signature.params

    fun returns() = signature.returns

    fun body() = body

    fun setBody(body: SequenceAST) {
        this.body = body
    }

    override fun toString(): String = "$signature {\n$body\n}"
}

class MethodSignatureAST(
    val name: String,
    val params: List<IdentifierAST>,
    val returns: List<IdentifierAST>,
) {
    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("method $name(${params.joinToString(", ") { p -> "$p: ${p.type()}" }}) ")

        if (returns.isNotEmpty()) {
            sb.append("returns (${returns.joinToString(", ") { p -> "$p: ${p.type()}" }})")
        }
        return sb.toString()
    }
}
