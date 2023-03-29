package fuzzd.generator.ast

import fuzzd.generator.ast.ExpressionAST.IdentifierAST

class MethodAST(
    val signature: MethodSignatureAST,
) : TopLevelAST() {
    constructor(signature: MethodSignatureAST, body: SequenceAST) : this(signature) {
        setBody(body)
    }

    constructor(name: String, params: List<IdentifierAST>, returns: List<IdentifierAST>) :
            this(MethodSignatureAST(name, params, returns))

    constructor(name: String, params: List<IdentifierAST>, returns: List<IdentifierAST>, body: SequenceAST) :
            this(MethodSignatureAST(name, params, returns)) {
        setBody(body)
    }

    private lateinit var body: SequenceAST

    fun name() = signature.name

    fun params() = signature.params

    fun returns() = signature.returns

    fun getBody() = body

    fun setBody(body: SequenceAST) {
        this.body = body
    }

    override fun toString(): String = "$signature {\n$body\n}"

    override fun equals(other: Any?): Boolean = other is MethodAST && other.signature == signature && other.body == body
}

class MethodSignatureAST(
    val name: String,
    val params: List<IdentifierAST>,
    val returns: List<IdentifierAST>,
) : ASTElement {
    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("method $name(${params.joinToString(", ") { p -> "$p: ${p.type()}" }}) ")

        if (returns.isNotEmpty()) {
            sb.append("returns (${returns.joinToString(", ") { p -> "$p: ${p.type()}" }})")
        }
        return sb.toString()
    }

    override fun equals(other: Any?): Boolean =
        other is MethodSignatureAST && other.name == name && other.params == params && other.returns == returns

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + params.hashCode()
        result = 31 * result + returns.hashCode()
        return result
    }
}
