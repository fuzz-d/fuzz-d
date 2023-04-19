package fuzzd.interpreter.value

class ValueTable<T, U>(private val parent: ValueTable<T, U>? = null) {
    val values = mutableMapOf<T, U?>()

    constructor(parent: ValueTable<T, U>?, values: Map<T, U?>) : this(parent) {
        values.forEach { (k, v) -> this.values[k] = v }
    }

    fun topLevel(): ValueTable<T, U> = parent?.topLevel() ?: this

    fun withParent(parent: ValueTable<T, U>) = ValueTable(parent, values)

    fun has(item: T): Boolean = item in values || parent?.has(item) == true

    fun create(item: T) {
        values[item] = null
    }

    fun get(item: T): U = if (item in values)
        values[item]!!
    else
        parent!!.get(item)

    fun set(item: T, value: U) {
        if (item !in values && parent != null && parent.has(item)) {
            parent.set(item, value)
        } else {
            values[item] = value
        }
    }
}
