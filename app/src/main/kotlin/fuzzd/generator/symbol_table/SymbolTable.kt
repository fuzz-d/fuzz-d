package fuzzd.generator.symbol_table

import fuzzd.generator.ast.ExpressionAST.IdentifierAST
import fuzzd.generator.ast.Type
import fuzzd.generator.ast.Type.ArrayType
import fuzzd.generator.ast.error.InvalidInputException

class SymbolTable(private val parent: SymbolTable? = null) {
    private val symbolTable = mutableMapOf<IdentifierAST, Type>()
    private val typeTable = mutableMapOf<Type, MutableList<IdentifierAST>>()

    fun add(identifier: IdentifierAST) {
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

    fun withType(type: Type): List<IdentifierAST> {
        val results = mutableListOf<IdentifierAST>()

        results.addAll(parent?.withType(type) ?: listOf())
        results.addAll(typeTable[type] ?: listOf())

        return results
    }

    fun withInternalType(type: Type): List<IdentifierAST> = withType(ArrayType(type))

    fun hasType(type: Type): Boolean =
        typeTable[type]?.isNotEmpty() == true || parent?.hasType(type) == true

    fun hasInternalType(type: Type): Boolean =
        typeTable[ArrayType(type)]?.isNotEmpty() == true || parent?.hasInternalType(type) == true

    fun typeOf(identifier: IdentifierAST): Type = symbolTable[identifier] ?: parent?.typeOf(identifier)
        ?: throw InvalidInputException("Identifier not found in symbol table")
}
