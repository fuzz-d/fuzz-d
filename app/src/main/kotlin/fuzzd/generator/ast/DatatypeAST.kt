package fuzzd.generator.ast

import fuzzd.generator.ast.ExpressionAST.IdentifierAST
import fuzzd.generator.ast.Type.DatatypeType
import fuzzd.generator.ast.error.InvalidInputException

class DatatypeAST(val name: String, val constructors: List<DatatypeConstructorAST>) : TopLevelAST() {
    init {
        if (constructors.isEmpty()) {
            throw InvalidInputException("Datatypes must have at least 1 constructor")
        }
    }

    fun datatypes(): List<DatatypeType> = constructors.map { DatatypeType(this, it) }

    override fun toString(): String = "datatype $name = ${constructors.joinToString(" | ")}"

    override fun equals(other: Any?): Boolean =
        other is DatatypeAST && other.name == name && other.constructors == constructors

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + constructors.hashCode()
        return result
    }
}

class DatatypeConstructorAST(val name: String, val fields: List<IdentifierAST>) : ASTElement {
    override fun toString(): String =
        "$name${if (fields.isNotEmpty()) "(${fields.joinToString(", ") { "${it.name}: ${it.type()}" }})" else ""}"

    override fun equals(other: Any?): Boolean =
        other is DatatypeConstructorAST && other.name == name && other.fields == fields

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + fields.hashCode()
        return result
    }
}
