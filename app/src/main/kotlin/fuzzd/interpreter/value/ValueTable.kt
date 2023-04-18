package fuzzd.interpreter.value

class ValueTable<T, U>(val parent: ValueTable<T, U>? = null) {
    val values = mutableMapOf<T, U>()

    constructor(parent: ValueTable<T, U>?, values: Map<T, U>) : this(parent) {
        values.forEach { (k, v) -> this.values[k] = v }
    }

    fun topLevel(): ValueTable<T, U> = parent?.topLevel() ?: this

    fun has(item: T): Boolean = item in values || parent?.has(item) == true

    fun get(item: T): U = if (item in values) values[item]!! else parent!!.get(item)

    fun set(item: T, value: U) {
        if (item !in values && parent != null && parent.has(item)) {
            parent.set(item, value)
        } else {
            values[item] = value
        }
    }

    fun remove(item: T) {
        values.remove(item)
    }
}
