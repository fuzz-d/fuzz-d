package fuzzd.generator.symbol_table

import fuzzd.generator.ast.ExpressionAST.IdentifierAST
import fuzzd.generator.ast.Type
import fuzzd.generator.ast.Type.ArrayType

class SymbolTable(private val parent: SymbolTable? = null) {
    private val symbolTable = mutableMapOf<IdentifierAST, Type>()
    private val typeTable = mutableMapOf<Type, MutableList<IdentifierAST>>()

    fun has(identifier: IdentifierAST): Boolean = symbolTable[identifier] != null || parent?.has(identifier) ?: false

    fun add(identifier: IdentifierAST) {
        if (has(identifier)) return

        val type = identifier.type()
        symbolTable[identifier] = type

        if (type !in typeTable) {
            typeTable[type] = mutableListOf()
        }

        typeTable[type]!!.add(identifier)
    }

    /**
     * Updates an identifier in the symbol table to track a new updated value after assignment.
     * @param singleLevel refers to whether or not the update should only affect *this scope* -- which
     * would be the case for the branch of an if statement which isn't taken
     */
    fun update(oldIdentifier: IdentifierAST, newIdentifier: IdentifierAST, singleLevel: Boolean = false) {
        val updateInParent = !singleLevel && parent != null && symbolTable[oldIdentifier] == null

        if (updateInParent) {
            // updateInParent checks for parent not null
            parent!!.update(oldIdentifier, newIdentifier)
        } else {
            remove(oldIdentifier)
            add(newIdentifier)
        }
    }

    fun remove(identifier: IdentifierAST) {
        val type = identifier.type()

        symbolTable.remove(identifier)
        typeTable[type]?.remove(identifier)
    }

    fun withType(type: Type): List<IdentifierAST> = (parent?.withType(type) ?: listOf()) + (typeTable[type] ?: listOf())

    fun types(): List<Type> = typeTable.keys.toList()

    fun withInternalType(type: Type): List<IdentifierAST> = withType(ArrayType(type))

    fun hasType(type: Type): Boolean =
        typeTable[type]?.isNotEmpty() == true || parent?.hasType(type) == true

    fun hasInternalType(type: Type): Boolean =
        typeTable[ArrayType(type)]?.isNotEmpty() == true || parent?.hasInternalType(type) == true
}
