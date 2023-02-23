package fuzzd.generator.symbol_table

import fuzzd.generator.ast.ClassAST
import fuzzd.generator.ast.FunctionMethodAST
import fuzzd.generator.ast.MethodAST
import fuzzd.generator.ast.TraitAST
import fuzzd.generator.ast.Type

class FunctionSymbolTable(private val parent: FunctionSymbolTable? = null) {
    private val functionMethods = mutableSetOf<FunctionMethodAST>()
    private val methods = mutableSetOf<MethodAST>()

    private val traits = mutableSetOf<TraitAST>()
    private val classes = mutableSetOf<ClassAST>()

    fun topLevel(): FunctionSymbolTable = parent?.topLevel() ?: this

    fun addFunctionMethods(allFunctionMethods: Collection<FunctionMethodAST>) =
        allFunctionMethods.forEach(this::addFunctionMethod)

    fun addFunctionMethod(functionMethod: FunctionMethodAST) {
        functionMethods.add(functionMethod)
    }

    fun withFunctionMethodType(type: Type): List<FunctionMethodAST> =
        functionMethods.filter { it.returnType() == type }

    fun functionMethods(): Set<FunctionMethodAST> = functionMethods union (parent?.functionMethods() ?: listOf())

    fun addMethods(allMethods: Collection<MethodAST>) {
        allMethods.forEach(this::addMethod)
    }

    fun addMethod(method: MethodAST) {
        methods.add(method)
    }

    fun methods(): Set<MethodAST> = methods union (parent?.methods() ?: listOf())

    fun addTraits(traits: Collection<TraitAST>) {
        traits.forEach(this::addTrait)
    }

    fun addTrait(trait: TraitAST) {
        parent?.addTrait(trait) ?: traits.add(trait)
    }

    fun traits(): Set<TraitAST> = traits union (parent?.traits() ?: listOf())

    fun addClasses(classes: Collection<ClassAST>) {
        classes.forEach(this::addClass)
    }

    fun addClass(clazz: ClassAST) {
        parent?.addClass(clazz) ?: classes.add(clazz)
    }

    fun hasClasses(): Boolean = classes.isNotEmpty() || parent?.hasClasses() ?: false

    fun classes(): Set<ClassAST> = classes union (parent?.classes() ?: listOf())
}
