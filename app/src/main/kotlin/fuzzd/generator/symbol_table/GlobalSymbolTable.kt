package fuzzd.generator.symbol_table

import fuzzd.generator.ast.ClassAST
import fuzzd.generator.ast.DatatypeAST
import fuzzd.generator.ast.TraitAST
import fuzzd.generator.ast.Type.DatatypeType
import fuzzd.utils.reduceLists

class GlobalSymbolTable {
    private val traits = mutableSetOf<TraitAST>()
    private val classes = mutableSetOf<ClassAST>()

    private val datatypes = mutableSetOf<DatatypeAST>()

    fun addTraits(traits: Collection<TraitAST>) {
        traits.forEach(this::addTrait)
    }

    fun addTrait(trait: TraitAST) {
        traits.add(trait)
    }

    fun traits(): Set<TraitAST> = traits

    fun hasTraits(): Boolean = traits.isNotEmpty()

    fun addClasses(classes: Collection<ClassAST>) {
        classes.forEach(this::addClass)
    }

    fun addClass(clazz: ClassAST) {
        classes.add(clazz)
    }

    fun hasClasses(): Boolean = classes.isNotEmpty()

    fun classes(): Set<ClassAST> = classes

    fun addDatatype(datatype: DatatypeAST) {
        datatypes.add(datatype)
    }

    fun hasDatatypes(): Boolean = datatypes.isNotEmpty()

    fun datatypes(): Set<DatatypeAST> = datatypes

    fun hasAvailableDatatypes(onDemand: Boolean) = availableDatatypes(onDemand).isNotEmpty()

    fun availableDatatypes(onDemand: Boolean): List<DatatypeType> = datatypes.map { d ->
        d.datatypes().filter { onDemand || !it.hasHeapType() }
    }.reduceLists()
}
