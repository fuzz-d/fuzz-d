package fuzzd.generator

import fuzzd.generator.ast.ExpressionAST.IdentifierAST
import fuzzd.generator.ast.Type
import fuzzd.generator.ast.error.InvalidInputException
import kotlin.reflect.KClass

class SymbolTable(private val parent: SymbolTable? = null) {
    private val symbolTable = mutableMapOf<IdentifierAST, KClass<out Type>>()
    private val typeTable = mutableMapOf<KClass<out Type>, MutableList<IdentifierAST>>()

    fun add(identifier: IdentifierAST) {
        val type = identifier.type()::class
        symbolTable[identifier] = type

        if (type !in typeTable) {
            typeTable[type] = mutableListOf()
        }

        typeTable[type]!!.add(identifier)
    }

    fun withType(type: KClass<out Type>): List<IdentifierAST> {
        val results = mutableListOf<IdentifierAST>()

        results.addAll(parent?.withType(type) ?: listOf())
        results.addAll(typeTable[type] ?: listOf())

        return results
    }

    fun hasType(type: KClass<out Type>): Boolean =
        typeTable[type]?.isNotEmpty() == true || parent?.hasType(type) == true

    fun typeOf(identifier: IdentifierAST): KClass<out Type> = symbolTable[identifier] ?: parent?.typeOf(identifier)
        ?: throw InvalidInputException("Identifier not found in symbol table")
}
