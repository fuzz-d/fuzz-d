package fuzzd.generator.symbol_table

import fuzzd.generator.ast.ClassAST
import fuzzd.generator.ast.FunctionMethodAST
import fuzzd.generator.ast.MethodAST
import fuzzd.generator.ast.TraitAST
import fuzzd.generator.ast.Type

class GlobalSymbolTable {
    private val functionMethods = mutableListOf<FunctionMethodAST>()
    private val methods = mutableListOf<MethodAST>()

    private val traits = mutableListOf<TraitAST>()
    private val classes = mutableListOf<ClassAST>()

    fun addAllFunctionMethods(allFunctionMethods: Iterable<FunctionMethodAST>) =
        allFunctionMethods.forEach(this::addFunctionMethod)

    fun addFunctionMethod(functionMethod: FunctionMethodAST) {
        functionMethods.add(functionMethod)
    }

    fun hasFunctionMethodType(type: Type): Boolean = functionMethods.any { it.returnType() == type }

    fun withFunctionMethodType(type: Type): List<FunctionMethodAST> =
        functionMethods.filter { it.returnType() == type }

    fun functionMethods(): List<FunctionMethodAST> = functionMethods

    fun addAllMethods(methods: Iterable<MethodAST>) {
        methods.forEach(this::addMethod)
    }

    fun addMethod(method: MethodAST) {
        methods.add(method)
    }

    fun methods(): List<MethodAST> = methods

    fun addAllTraits(traits: Iterable<TraitAST>) {
        traits.forEach(this::addTrait)
    }

    fun addTrait(trait: TraitAST) {
        traits.add(trait)
    }

    fun traits(): List<TraitAST> = traits

    fun addAllClasses(classes: Iterable<ClassAST>) {
        classes.forEach(this::addClass)
    }

    fun addClass(clazz: ClassAST) {
        classes.add(clazz)
    }

    fun hasClasses(): Boolean = classes.isNotEmpty()

    fun classes(): List<ClassAST> = classes

    fun clone(): GlobalSymbolTable {
        val clone = GlobalSymbolTable()

        clone.addAllFunctionMethods(functionMethods)
        clone.addAllMethods(methods)
        clone.addAllTraits(traits)
        clone.addAllClasses(classes)

        return clone
    }
}
