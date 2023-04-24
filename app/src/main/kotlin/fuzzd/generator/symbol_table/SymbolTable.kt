package fuzzd.generator.symbol_table

import fuzzd.generator.ast.ExpressionAST.ClassInstanceAST
import fuzzd.generator.ast.ExpressionAST.DatatypeInstanceAST
import fuzzd.generator.ast.ExpressionAST.IdentifierAST
import fuzzd.generator.ast.ExpressionAST.TraitInstanceAST
import fuzzd.generator.ast.Type
import fuzzd.utils.unionAll

class SymbolTable(private val parent: SymbolTable? = null) {
    val symbolTable = mutableMapOf<IdentifierAST, Type>()
    private val typeTable = mutableMapOf<Type, MutableList<IdentifierAST>>()

    fun has(identifier: IdentifierAST): Boolean = symbolTable[identifier] != null || parent?.has(identifier) ?: false

    fun addAll(identifiers: Iterable<IdentifierAST>) {
        identifiers.forEach(this::add)
    }

    fun add(identifier: IdentifierAST) {
        if (has(identifier)) return

        val type = identifier.type()
        symbolTable[identifier] = type

        if (type !in typeTable) {
            typeTable[type] = mutableListOf()
        }

        typeTable[type]!!.add(identifier)
    }

    fun remove(identifier: IdentifierAST) {
        val type = identifier.type()

        symbolTable.remove(identifier)
        typeTable[type]?.remove(identifier)
    }

    fun withType(type: Type): List<IdentifierAST> =
        (parent?.withType(type) ?: listOf()) +
            (typeTable[type] ?: listOf()) +
            classInstances().map { it.fields }.unionAll().filter { it.type() == type } +
            traitInstances().map { it.fields }.unionAll().filter { it.type() == type }

    fun classInstances(): List<ClassInstanceAST> =
        (parent?.classInstances() ?: listOf()) + symbolTable.keys.filterIsInstance<ClassInstanceAST>()

    fun traitInstances(): List<TraitInstanceAST> =
        (parent?.traitInstances() ?: listOf()) + symbolTable.keys.filterIsInstance<TraitInstanceAST>()

    fun datatypeInstances(): List<DatatypeInstanceAST> =
        (parent?.datatypeInstances() ?: listOf()) + symbolTable.keys.filterIsInstance<DatatypeInstanceAST>()

    fun types(): List<Type> = typeTable.keys.toList()

    fun hasType(type: Type): Boolean = typeTable[type]?.isNotEmpty() == true || parent?.hasType(type) == true
}
