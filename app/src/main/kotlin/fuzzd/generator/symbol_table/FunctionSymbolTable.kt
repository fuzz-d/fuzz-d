package fuzzd.generator.symbol_table

import fuzzd.generator.ast.FunctionMethodAST
import fuzzd.generator.ast.MethodAST
import fuzzd.generator.ast.Type

class FunctionSymbolTable(private val parent: FunctionSymbolTable? = null) {
    private val functionMethods = mutableSetOf<FunctionMethodAST>()
    private val methods = mutableSetOf<MethodAST>()

    fun topLevel(): FunctionSymbolTable = parent?.topLevel() ?: this

    fun addFunctionMethods(allFunctionMethods: Collection<FunctionMethodAST>) =
        allFunctionMethods.forEach(this::addFunctionMethod)

    fun addFunctionMethod(functionMethod: FunctionMethodAST) {
        functionMethods.add(functionMethod)
    }

    fun withFunctionMethodType(type: Type): List<FunctionMethodAST> =
        functionMethods.filter { it.returnType() == type } + (parent?.withFunctionMethodType(type) ?: listOf())

    fun functionMethods(): Set<FunctionMethodAST> = functionMethods union (parent?.functionMethods() ?: setOf())

    fun addMethods(allMethods: Collection<MethodAST>) {
        allMethods.forEach(this::addMethod)
    }

    fun addMethod(method: MethodAST) {
        methods.add(method)
    }

    fun methods(): Set<MethodAST> = methods union (parent?.methods() ?: setOf())
}
