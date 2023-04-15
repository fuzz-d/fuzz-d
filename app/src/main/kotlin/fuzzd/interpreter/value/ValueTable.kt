package fuzzd.interpreter.value

import fuzzd.generator.ast.ExpressionAST.IdentifierAST

class ValueTable(val parent: ValueTable? = null) {
    val values = mutableMapOf<IdentifierAST, Value>()

    fun has(identifier: IdentifierAST): Boolean = identifier in values || parent?.has(identifier) == true

    fun get(identifier: IdentifierAST): Value =
        if (identifier in values) values[identifier]!! else parent!!.get(identifier)

    fun set(identifier: IdentifierAST, value: Value) {
        if (identifier !in values && parent != null && parent.has(identifier)) {
            parent.set(identifier, value)
        } else {
            values[identifier] = value
        }
    }

    fun remove(identifier: IdentifierAST) {
        values.remove(identifier)
    }
}
