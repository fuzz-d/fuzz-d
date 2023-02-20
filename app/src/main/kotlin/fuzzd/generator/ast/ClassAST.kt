package fuzzd.generator.ast

import fuzzd.generator.ast.ExpressionAST.IdentifierAST
import fuzzd.generator.ast.StatementAST.AssignmentAST
import fuzzd.generator.ast.error.InvalidInputException
import fuzzd.utils.indent
import fuzzd.utils.unionAll

class ClassAST(
    val name: String,
    private val extends: Set<TraitAST>,
    val functionMethods: Set<FunctionMethodAST>,
    val methods: Set<MethodAST>,
    val fields: Set<IdentifierAST>,
) : ASTElement {
    private val constructor: ConstructorAST

    init {
        // need to check that we implement all trait members
        val requiredFields = extends.map { it.fields() }.unionAll()
        val missingFields = requiredFields subtract fields
        if (missingFields.isNotEmpty()) {
            throw InvalidInputException("Missing fields for class declaration $name: ${missingFields.joinToString(", ")}")
        }

        val requiredFunctionMethods = extends.map { it.functionMethods() }.unionAll()
        val missingFunctionMethods = requiredFunctionMethods subtract functionMethods.map { it.signature }.toSet()
        if (missingFunctionMethods.isNotEmpty()) {
            throw InvalidInputException(
                "Missing function methods for class declaration $name: ${missingFunctionMethods.joinToString(", ") { it.name }}",
            )
        }

        val requiredMethods = extends.map { it.methods() }.unionAll()
        val missingMethods = requiredMethods subtract methods.map { it.signature }.toSet()
        if (missingMethods.isNotEmpty()) {
            throw InvalidInputException("Missing methods for class declaration $name: ${missingMethods.joinToString(", ") { it.name }}")
        }

        this.constructor = ConstructorAST(fields)
    }

    override fun toString(): String {
        val sb = StringBuilder()

        sb.append("class $name")
        if (extends.isNotEmpty()) {
            sb.append("extends ${extends.joinToString(", ") { it.name }}")
        }
        sb.appendLine(" {")

        fields.forEach { field -> sb.appendLine(indent("var ${field.name} : ${field.type()};")) }

        sb.appendLine(indent(constructor))

        functionMethods.forEach { fm -> sb.appendLine(indent(fm)) }
        methods.forEach { m -> sb.appendLine(indent(m)) }

        sb.appendLine("}")
        return sb.toString()
    }

    companion object {
        fun builder() = ClassASTBuilder()

        class ClassASTBuilder {
            private lateinit var name: String

            private val extends = mutableSetOf<TraitAST>()
            private val fields = mutableSetOf<IdentifierAST>()
            private val functionMethods = mutableSetOf<FunctionMethodAST>()
            private val methods = mutableSetOf<MethodAST>()

            fun build() = ClassAST(name, extends, functionMethods, methods, fields)

            fun withExtends(extends: Set<TraitAST>): ClassASTBuilder {
                this.extends.addAll(extends)
                return this
            }

            fun withFields(fields: Set<IdentifierAST>): ClassASTBuilder {
                this.fields.addAll(fields)
                return this
            }

            fun withFunctionMethods(functionMethods: Set<FunctionMethodAST>): ClassASTBuilder {
                this.functionMethods.addAll(functionMethods)
                return this
            }

            fun withMethods(methods: Set<MethodAST>): ClassASTBuilder {
                this.methods.addAll(methods)
                return this
            }

            fun withName(name: String): ClassASTBuilder {
                this.name = name
                return this
            }
        }
    }
}

class ConstructorAST(private val fields: Set<IdentifierAST>) : ASTElement {
    private val assignments = mutableListOf<AssignmentAST>()

    init {
        fields.forEach { field ->
            assignments.add(AssignmentAST(IdentifierAST("this.${field.name}", field.type()), field))
        }
    }

    override fun toString(): String {
        val sb = StringBuilder()

        sb.append("constructor (")
        sb.append(fields.joinToString(", ") { field -> "${field.name} : ${field.type()}" })
        sb.appendLine(") {")

        assignments.forEach { assignment -> sb.appendLine(indent(assignment)) }
        sb.appendLine("}")

        return sb.toString()
    }
}
