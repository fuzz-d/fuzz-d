package fuzzd.generator

import fuzzd.generator.ast.ExpressionAST.IdentifierAST
import fuzzd.generator.ast.Type
import fuzzd.generator.ast.error.InvalidInputException
import kotlin.reflect.KClass

class SymbolTable(private val parent: SymbolTable? = null) {
    private val symbolTable = mutableMapOf<IdentifierAST, KClass<Type>>()
    private val typeTable = mutableMapOf<KClass<Type>, MutableList<IdentifierAST>>()

    fun add(identifier: IdentifierAST, type: KClass<Type>) {
        symbolTable[identifier] = type

        if (type !in typeTable) {
            typeTable[type] = mutableListOf()
        }

        typeTable[type]!!.add(identifier)
    }

    fun withType(type: KClass<Type>): List<IdentifierAST> {
        val results = mutableListOf<IdentifierAST>()

        results.addAll(parent?.withType(type) ?: listOf())
        results.addAll(typeTable[type] ?: listOf())

        return results
    }

    fun typeOf(identifier: IdentifierAST): KClass<Type> = symbolTable[identifier] ?: parent?.typeOf(identifier)
        ?: throw InvalidInputException("Identifier not found in symbol table")
}
