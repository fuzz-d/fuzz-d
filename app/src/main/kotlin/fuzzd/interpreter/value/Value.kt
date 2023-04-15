package fuzzd.interpreter.value

sealed class Value {
    data class SetValue<T : Value>(val set: Set<T>) : Value() {
        fun contains(item: T): BoolValue = BoolValue(item in set)
        fun notContains(item: T): BoolValue = BoolValue(item !in set)
        fun properSubsetOf(other: SetValue<T>): BoolValue =
            BoolValue(other.set.containsAll(set) && (other.set subtract set).isNotEmpty())

        fun subsetOf(other: SetValue<T>): BoolValue = BoolValue(other.set.containsAll(set))
        fun supersetOf(other: SetValue<T>): BoolValue = BoolValue(set.containsAll(other.set))
        fun properSupersetOf(other: SetValue<T>): BoolValue =
            BoolValue(set.containsAll(other.set) && (set subtract other.set).isNotEmpty())

        fun disjoint(other: SetValue<T>): BoolValue = BoolValue(set.none { other.set.contains(it) })
        fun union(other: SetValue<T>): SetValue<T> = SetValue(set union other.set)
        fun difference(other: SetValue<T>): SetValue<T> = SetValue(set subtract other.set)
        fun intersect(other: SetValue<T>): SetValue<T> = SetValue(set intersect other.set)
        override fun equals(other: Any?): Boolean = (other is SetValue<*>) && set == other.set
        override fun hashCode(): Int = set.hashCode()
    }

    data class StringValue(val value: String) : Value() {
        override fun equals(other: Any?): Boolean = other is StringValue && value == other.value
        override fun hashCode(): Int = value.hashCode()
    }

    data class BoolValue(val value: Boolean) : Value() {
        fun iff(other: BoolValue): BoolValue = BoolValue(value && other.value || !value && !other.value)
        fun impl(other: BoolValue): BoolValue = BoolValue(!value || other.value)
        fun rimpl(other: BoolValue): BoolValue = BoolValue(!other.value || value)
        fun and(other: BoolValue): BoolValue = BoolValue(value && other.value)
        fun or(other: BoolValue): BoolValue = BoolValue(value || other.value)
        override fun equals(other: Any?): Boolean = other is BoolValue && value == other.value
        override fun hashCode(): Int = value.hashCode()
    }

    data class IntValue(val value: Long) : Value() {
        fun plus(other: IntValue): IntValue = IntValue(value + other.value)
        fun subtract(other: IntValue): IntValue = IntValue(value - other.value)
        fun multiply(other: IntValue): IntValue = IntValue(value * other.value)
        fun divide(other: IntValue): IntValue = TODO("Euclidean division")
        fun modulo(other: IntValue): IntValue = IntValue(value % other.value)
        fun lessThan(other: IntValue): BoolValue = BoolValue(value < other.value)
        fun lessThanEquals(other: IntValue): BoolValue = BoolValue(value <= other.value)
        fun greaterThanEquals(other: IntValue): BoolValue = BoolValue(value >= other.value)
        fun greaterThan(other: IntValue): BoolValue = BoolValue(value > other.value)

        override fun equals(other: Any?): Boolean = other is IntValue && value == other.value
        override fun hashCode(): Int = value.hashCode()
    }
}
