package fuzzd.interpreter.value

class ValueTable<T, U>(private val parent: ValueTable<T, U>? = null) {
    val values = mutableMapOf<T, U?>()

    constructor(parent: ValueTable<T, U>?, values: Map<T, U?>) : this(parent) {
        values.forEach { (k, v) -> this.values[k] = v }
    }

    fun topLevel(): ValueTable<T, U> = parent?.topLevel() ?: this

    fun keys(): Set<T> = values.keys

    fun withParent(parent: ValueTable<T, U>) = ValueTable(parent, values)

    fun has(item: T): Boolean = item in values || parent?.has(item) == true

    fun create(item: T) {
        values[item] = null
    }

    fun get(item: T): U = if (item in values) values[item]!! else parent!!.get(item)

    fun assign(item: T, value: U) {
        if (item !in values && parent != null && parent.has(item)) {
            parent.assign(item, value)
        } else {
            values[item] = value
        }
    }

    fun declare(item: T, value: U) {
        values[item] = value
    }

    fun clone(): ValueTable<T, U> {
        val valueTable = ValueTable(parent)
        values.forEach { (k, v) -> if (v != null) valueTable.declare(k, v) else valueTable.create(k) }
        return valueTable
    }

    override fun equals(other: Any?): Boolean =
        other is ValueTable<*, *> && other.parent == parent && other.values == values

    override fun hashCode(): Int {
        var result = parent?.hashCode() ?: 0
        result = 31 * result + values.hashCode()
        return result
    }

    override fun toString() = values.toString()
}
