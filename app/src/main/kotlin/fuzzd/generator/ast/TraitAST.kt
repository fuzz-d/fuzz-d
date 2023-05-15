package fuzzd.generator.ast

import fuzzd.generator.ast.ExpressionAST.IdentifierAST
import fuzzd.utils.indent
import fuzzd.utils.unionAll

class TraitAST(
    val name: String,
    private val extends: Set<TraitAST>,
    private val functionMethods: Set<FunctionMethodSignatureAST>,
    private val methods: Set<MethodSignatureAST>,
    val fields: Set<IdentifierAST>,
) : TopLevelAST() {
    fun extends(): Set<TraitAST> =
        extends union (extends.map { it.extends() }).unionAll()

    fun functionMethods(): Set<FunctionMethodSignatureAST> =
        functionMethods union extends().map { it.functionMethods }.unionAll()

    fun methods(): Set<MethodSignatureAST> = methods union extends().map { it.methods }.unionAll()

    fun fields(): Set<IdentifierAST> = fields union extends().map { it.fields }.unionAll()

    override fun toString(): String {
        val sb = StringBuilder()

        sb.append("trait $name")

        if (extends.isNotEmpty()) {
            sb.append(" extends ${extends.joinToString(", ") { it.name }}")
        }

        sb.appendLine(" {")

        fields.forEach { field ->
            sb.appendLine(indent("${if(field.mutable) "var" else "const"} ${field.name} : ${field.type()}"))
        }

        functionMethods.forEach { fm -> sb.appendLine(indent(fm)) }

        methods.forEach { m -> sb.appendLine(indent(m)) }

        sb.appendLine("}")

        return sb.toString()
    }

    companion object {
        fun builder() = TraitASTBuilder()

        class TraitASTBuilder {
            private lateinit var name: String
            private val extends = mutableSetOf<TraitAST>()
            private val functionMethods = mutableSetOf<FunctionMethodSignatureAST>()
            private val methods = mutableSetOf<MethodSignatureAST>()
            private val fields = mutableSetOf<IdentifierAST>()

            fun build(): TraitAST = TraitAST(name, extends, functionMethods, methods, fields)

            fun withName(name: String): TraitASTBuilder {
                this.name = name
                return this
            }

            fun withExtends(traits: Set<TraitAST>): TraitASTBuilder {
                this.extends.addAll(traits)
                return this
            }

            fun withFunctionMethods(fms: Set<FunctionMethodSignatureAST>): TraitASTBuilder {
                this.functionMethods.addAll(fms)
                return this
            }

            fun withMethods(ms: Set<MethodSignatureAST>): TraitASTBuilder {
                this.methods.addAll(ms)
                return this
            }

            fun withFields(fields: Set<IdentifierAST>): TraitASTBuilder {
                this.fields.addAll(fields)
                return this
            }
        }
    }
}
