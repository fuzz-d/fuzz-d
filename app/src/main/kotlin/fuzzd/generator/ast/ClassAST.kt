package fuzzd.generator.ast

import fuzzd.generator.ast.ExpressionAST.IdentifierAST
import fuzzd.generator.ast.StatementAST.AssignmentAST
import fuzzd.generator.ast.error.InvalidInputException
import fuzzd.utils.indent
import fuzzd.utils.unionAll

class ClassAST(
    val name: String,
    val extends: Set<TraitAST>,
    val functionMethods: Set<FunctionMethodAST>,
    val methods: Set<MethodAST>,
    val fields: Set<IdentifierAST>,
    val inheritedFields: Set<IdentifierAST>,
) : TopLevelAST() {
    val constructorFields: Set<IdentifierAST>
    private val constructor: ConstructorAST

    init {
        // need to check that we implement all trait members
        val requiredFields = extends.map { it.fields() }.unionAll()

        // check we aren't missing any fields required by traits
        val missingFields = requiredFields subtract inheritedFields
        if (missingFields.isNotEmpty()) {
            throw InvalidInputException("Missing fields for class declaration $name: ${missingFields.joinToString(", ")}")
        }

        // check we aren't passing in any class-defined fields as required
        val extraRequiredFields = inheritedFields subtract requiredFields
        if (extraRequiredFields.isNotEmpty()) {
            throw InvalidInputException(
                "Too many trait fields for class declaration $name: ${
                    extraRequiredFields.joinToString(
                        ", ",
                    )
                }",
            )
        }

        // check required fields not passed in with class-defined fields
        val classDefTraitFields = requiredFields intersect fields
        if (classDefTraitFields.isNotEmpty()) {
            throw InvalidInputException(
                "Passed in trait field as class field for class declaration $name: ${
                    classDefTraitFields.joinToString(
                        ", ",
                    )
                }",
            )
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

        this.constructorFields = fields union inheritedFields
        this.constructor = ConstructorAST(constructorFields)
    }

    override fun toString(): String {
        val sb = StringBuilder()

        sb.append("class $name")
        if (extends.isNotEmpty()) {
            sb.append(" extends ${extends.joinToString(", ") { it.name }}")
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
            private val inheritedFields = mutableSetOf<IdentifierAST>()
            private val methods = mutableSetOf<MethodAST>()

            fun build() = ClassAST(name, extends, functionMethods, methods, fields, inheritedFields)

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

            fun withInheritedFields(inheritedFields: Set<IdentifierAST>): ClassASTBuilder {
                this.inheritedFields.addAll(inheritedFields)
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
