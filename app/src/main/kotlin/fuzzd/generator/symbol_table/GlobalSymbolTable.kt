package fuzzd.generator.symbol_table

import fuzzd.generator.ast.ClassAST
import fuzzd.generator.ast.ExpressionAST.IdentifierAST
import fuzzd.generator.ast.FunctionMethodAST
import fuzzd.generator.ast.MethodAST
import fuzzd.generator.ast.TraitAST
import fuzzd.generator.ast.Type

class GlobalSymbolTable {
    private val fields = mutableListOf<IdentifierAST>()
    private val functionMethods = mutableMapOf<FunctionMethodAST, Type>()
    private val methods = mutableListOf<MethodAST>()

    private val traits = mutableListOf<TraitAST>()
    private val classes = mutableListOf<ClassAST>()

    fun addField(field: IdentifierAST) {
        fields.add(field)
    }

    fun hasFieldType(type: Type) = fields.any { it.type() == type }

    fun addAllFunctionMethods(allFunctionMethods: List<FunctionMethodAST>) =
        allFunctionMethods.forEach(this::addFunctionMethod)

    fun addFunctionMethod(functionMethod: FunctionMethodAST) {
        functionMethods[functionMethod] = functionMethod.returnType()
    }

    fun hasFunctionMethodType(type: Type): Boolean = functionMethods.any { it.value == type }

    fun withFunctionMethodType(type: Type): List<FunctionMethodAST> =
        functionMethods.filter { it.value == type }.keys.toList()

    fun functionMethods(): List<FunctionMethodAST> = functionMethods.keys.toList()

    fun addMethod(method: MethodAST) {
        methods.add(method)
    }

    fun methods(): List<MethodAST> = methods

    fun addTrait(trait: TraitAST) {
        traits.add(trait)
    }

    fun traits(): List<TraitAST> = traits

    fun addClass(clazz: ClassAST) {
        classes.add(clazz)
    }

    fun classes(): List<ClassAST> = classes
}
