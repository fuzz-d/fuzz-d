package fuzzd.generator.ast

import fuzzd.generator.ast.ExpressionAST.IdentifierAST
import fuzzd.generator.ast.error.InvalidInputException
import fuzzd.utils.unionAll

class ClassAST(
    val name: String,
    private val extends: Set<TraitAST>,
    private val functionMethods: Set<FunctionMethodAST>,
    private val methods: Set<MethodAST>,
    private val fields: Set<IdentifierAST>
) : ASTElement {
    init {
        // need to check that we implement all trait members
        val requiredFields = extends.map { it.fields() }.unionAll()
        val missingFields = fields subtract requiredFields
        if (missingFields.isNotEmpty()) {
            throw InvalidInputException("Missing fields for class declaration $name: ${missingFields.joinToString(", ")}")
        }

        val requiredFunctionMethods = extends.map { it.functionMethods() }.unionAll()
        val missingFunctionMethods = functionMethods.map { it.signature } - requiredFunctionMethods
        if (missingFunctionMethods.isNotEmpty()) {
            throw InvalidInputException(
                "Missing function methods for class declaration $name: ${missingFunctionMethods.joinToString(", ") { it.name }}"
            )
        }

        val requiredMethods = extends.map { it.methods() }.reduce { x, y -> x union y }
        val missingMethods = methods.map { it.signature } - requiredMethods
        if (missingMethods.isNotEmpty()) {
            throw InvalidInputException("Missing methods for class declaration $name: ${missingMethods.joinToString(", ") { it.name }}")
        }
    }

    override fun toString(): String {
        val sb = StringBuilder()

        return sb.toString()
    }
}
