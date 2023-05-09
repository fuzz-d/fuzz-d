package fuzzd.interpreter.value

import fuzzd.generator.ast.DatatypeAST
import fuzzd.generator.ast.DatatypeConstructorAST
import fuzzd.generator.ast.ExpressionAST
import fuzzd.generator.ast.ExpressionAST.BooleanLiteralAST
import fuzzd.generator.ast.ExpressionAST.CharacterLiteralAST
import fuzzd.generator.ast.ExpressionAST.DatatypeInstantiationAST
import fuzzd.generator.ast.ExpressionAST.IdentifierAST
import fuzzd.generator.ast.ExpressionAST.IntegerLiteralAST
import fuzzd.generator.ast.ExpressionAST.MapConstructorAST
import fuzzd.generator.ast.ExpressionAST.SequenceDisplayAST
import fuzzd.generator.ast.ExpressionAST.SetDisplayAST
import fuzzd.generator.ast.ExpressionAST.StringLiteralAST
import fuzzd.generator.ast.Type.DataStructureType.MapType
import fuzzd.interpreter.InterpreterContext
import fuzzd.utils.reduceLists
import java.lang.Integer.min
import java.math.BigInteger
import java.math.BigInteger.ONE
import java.math.BigInteger.ZERO
import java.math.BigInteger.valueOf

fun <T> multisetDifference(m1: Map<T, Int>, m2: Map<T, Int>): Map<T, Int> {
    val diff = mutableMapOf<T, Int>()
    m1.entries.forEach { (k, v) ->
        if (k !in m2) {
            diff[k] = v
        } else if (k in m2 && m2[k]!! < v) {
            diff[k] = v - m2[k]!!
        }
    }
    return diff
}

fun <T> multisetIntersect(m1: Map<T, Int>, m2: Map<T, Int>): Map<T, Int> {
    val intersect = mutableMapOf<T, Int>()
    m1.entries.forEach { (k, v) ->
        if (k in m2 && m2[k] != 0) {
            intersect[k] = min(v, m2[k]!!)
        }
    }
    return intersect
}

fun divideEuclidean(a: BigInteger, b: BigInteger): BigInteger =
    when {
        b == ZERO -> throw UnsupportedOperationException()
        a > ZERO -> a / b
        a < ZERO && b > ZERO -> (a / b) - if (a % b == ZERO) ZERO else ONE
        else -> if (a % b == ZERO) a / b else (a + b) / b
    }

sealed class Value {
    open fun toExpressionAST(): ExpressionAST = throw UnsupportedOperationException()

    open class TopLevelDatatypeValue(
        val datatype: DatatypeAST,
        val values: ValueTable<IdentifierAST, Value?>,
    ) : Value() {
        override fun toExpressionAST(): ExpressionAST {
            val fields = fields()
            val constructor = datatype.constructors.first { it.fields == fields }
            return DatatypeInstantiationAST(
                datatype,
                constructor,
                constructor.fields.map { values.get(it)!!.toExpressionAST() },
            )
        }

        open fun assign(assigns: List<Pair<IdentifierAST, Value>>): TopLevelDatatypeValue {
            val valueTable = values.clone()
            assigns.forEach { (identifier, value) -> valueTable.assign(identifier, value) }
            return TopLevelDatatypeValue(datatype, valueTable)
        }

        fun fields(): List<IdentifierAST> = values.values.filter { (_, v) -> v != null }.keys.toList()

        override fun equals(other: Any?): Boolean =
            other is TopLevelDatatypeValue && other.datatype == datatype && other.values == values

        override fun hashCode(): Int {
            var result = datatype.hashCode()
            result = 31 * result + values.hashCode()
            return result
        }
    }

    class DatatypeValue(datatype: DatatypeAST, val constructor: DatatypeConstructorAST, values: ValueTable<IdentifierAST, Value?>) :
        TopLevelDatatypeValue(datatype, values) {
        override fun toExpressionAST(): ExpressionAST =
            DatatypeInstantiationAST(datatype, constructor, constructor.fields.map { values.get(it)!!.toExpressionAST() })

        override fun assign(assigns: List<Pair<IdentifierAST, Value>>): DatatypeValue {
            val valueTable = values.clone()
            assigns.forEach { (identifier, value) -> valueTable.assign(identifier, value) }
            return DatatypeValue(datatype, constructor, valueTable)
        }
    }

    data class MultiValue(val values: List<Value>) : Value()

    // classContext stores class fields, methods and functions
    data class ClassValue(val classContext: InterpreterContext) : Value()

    class ArrayValue(length: Int) : Value() {
        val arr = Array<Value?>(length) { null }

        fun setIndex(index: Int, value: Value) {
            arr[index] = value
        }

        fun getIndex(index: Int): Value =
            arr[index] ?: throw UnsupportedOperationException("Array index $index was null")

        fun length(): IntValue = IntValue(valueOf(arr.size.toLong()))
    }

    sealed class DataStructureValue : Value() {
        abstract fun contains(item: Value): BoolValue
        abstract fun notContains(item: Value): BoolValue
        abstract fun modulus(): IntValue

        abstract fun elements(): List<Value>
    }

    data class SequenceValue(val seq: List<Value>) : DataStructureValue() {
        override fun contains(item: Value): BoolValue = BoolValue(item in seq)
        override fun notContains(item: Value): BoolValue = BoolValue(item !in seq)
        override fun modulus(): IntValue = IntValue(valueOf(seq.size.toLong()))
        override fun elements(): List<Value> = seq

        fun properSubsetOf(other: SequenceValue): BoolValue =
            BoolValue(other.seq.containsAll(seq) && (other.seq - seq.toSet()).isNotEmpty())

        fun subsetOf(other: SequenceValue): BoolValue = BoolValue(other.seq.containsAll(seq))
        fun supersetOf(other: SequenceValue): BoolValue = BoolValue(seq.containsAll(other.seq))
        fun properSupersetOf(other: SequenceValue): BoolValue =
            BoolValue(seq.containsAll(other.seq) && (seq - other.seq.toSet()).isNotEmpty())

        fun union(other: SequenceValue): SequenceValue = SequenceValue(seq + other.seq)

        fun getIndex(index: Int): Value = seq[index]

        fun assign(key: Int, value: Value): SequenceValue =
            SequenceValue(seq.subList(0, key) + value + seq.subList(key + 1, seq.size))

        fun asStringValue(): StringValue {
            val chars = seq.map { (it as CharValue).value }.toCharArray()
            return StringValue(chars.concatToString())
        }

        override fun equals(other: Any?): Boolean = other is SequenceValue && seq == other.seq
        override fun hashCode(): Int = seq.hashCode()

        override fun toExpressionAST(): ExpressionAST = SequenceDisplayAST(seq.map { it.toExpressionAST() })
    }

    data class MapValue(val type: MapType, val map: Map<Value, Value>) : DataStructureValue() {
        override fun contains(item: Value): BoolValue = BoolValue(map.containsKey(item))
        override fun notContains(item: Value): BoolValue = BoolValue(!map.containsKey(item))
        override fun modulus(): IntValue = IntValue(valueOf(map.size.toLong()))
        override fun elements(): List<Value> = map.keys.toList()
        fun union(other: MapValue): MapValue = MapValue(type, map + other.map)
        fun difference(other: SetValue): MapValue = MapValue(type, map - other.set)

        fun get(key: Value): Value = map[key] ?: throw UnsupportedOperationException("Map didn't contain key $key")

        fun assign(key: Value, value: Value): MapValue = MapValue(type, map + mapOf(key to value))

        override fun equals(other: Any?): Boolean = other is MapValue && map == other.map
        override fun hashCode(): Int = map.hashCode()

        override fun toExpressionAST(): ExpressionAST {
            val assignments = map.map { (k, v) -> Pair(k.toExpressionAST(), v.toExpressionAST()) }
            return MapConstructorAST(type.keyType, type.valueType, assignments)
        }
    }

    data class MultisetValue(val map: Map<Value, Int>) : DataStructureValue() {
        override fun contains(item: Value): BoolValue = BoolValue(map.containsKey(item) && map[item] != 0)
        override fun notContains(item: Value): BoolValue = BoolValue(!map.containsKey(item) || map[item] == 0)
        override fun modulus(): IntValue = IntValue(valueOf(map.values.sum().toLong()))
        override fun elements(): List<Value> = map.keys.toList()

        fun properSubsetOf(other: MultisetValue): BoolValue =
            BoolValue(multisetDifference(map, other.map).isEmpty() && multisetDifference(map, other.map).isNotEmpty())

        fun subsetOf(other: MultisetValue): BoolValue = BoolValue(multisetDifference(map, other.map).isEmpty())
        fun supersetOf(other: MultisetValue): BoolValue = BoolValue(multisetDifference(other.map, map).isEmpty())
        fun properSupersetOf(other: MultisetValue): BoolValue =
            BoolValue(multisetDifference(map, other.map).isNotEmpty() && multisetDifference(other.map, map).isEmpty())

        fun disjoint(other: MultisetValue): BoolValue = BoolValue(map.keys.none { it in other.map })
        fun union(other: MultisetValue): MultisetValue = MultisetValue(
            other.map.keys.fold(map.toMutableMap()) { m, k ->
                if (m.containsKey(k)) m[k] = m[k]!! + other.map[k]!! else m[k] = other.map[k]!!
                m
            },
        )

        fun difference(other: MultisetValue): MultisetValue = MultisetValue(multisetDifference(map, other.map))
        fun intersect(other: MultisetValue): MultisetValue = MultisetValue(multisetIntersect(map, other.map))

        fun get(key: Value): IntValue = if (key in map) {
            IntValue(valueOf(map[key]!!.toLong()))
        } else {
            throw UnsupportedOperationException("Multiset didn't contain key $key")
        }

        fun assign(key: Value, value: Int): MultisetValue = if (value == 0) {
            MultisetValue(map - setOf(key))
        } else {
            MultisetValue(map + mapOf(key to value))
        }

        override fun equals(other: Any?): Boolean = other is MultisetValue && map == other.map
        override fun hashCode(): Int = map.hashCode()

        override fun toExpressionAST(): ExpressionAST =
            SetDisplayAST(map.map { (k, v) -> List(v) { k.toExpressionAST() } }.reduceLists(), true)
    }

    data class SetValue(val set: Set<Value>) : DataStructureValue() {
        override fun contains(item: Value): BoolValue = BoolValue(item in set)
        override fun notContains(item: Value): BoolValue = BoolValue(item !in set)
        override fun modulus(): IntValue = IntValue(valueOf(set.size.toLong()))
        override fun elements(): List<Value> = set.toList()
        override fun toExpressionAST(): ExpressionAST = SetDisplayAST(set.map { it.toExpressionAST() }, false)

        fun properSubsetOf(other: SetValue): BoolValue =
            BoolValue(other.set.containsAll(set) && (other.set subtract set).isNotEmpty())

        fun subsetOf(other: SetValue): BoolValue = BoolValue(other.set.containsAll(set))
        fun supersetOf(other: SetValue): BoolValue = BoolValue(set.containsAll(other.set))
        fun properSupersetOf(other: SetValue): BoolValue =
            BoolValue(set.containsAll(other.set) && (set subtract other.set).isNotEmpty())

        fun disjoint(other: SetValue): BoolValue = BoolValue(set.none { other.set.contains(it) })
        fun union(other: SetValue): SetValue = SetValue(set union other.set)
        fun difference(other: SetValue): SetValue = SetValue(set subtract other.set)
        fun intersect(other: SetValue): SetValue = SetValue(set intersect other.set)

        override fun equals(other: Any?): Boolean = (other is SetValue) && set == other.set
        override fun hashCode(): Int = set.hashCode()
    }

    data class StringValue(val value: String) : DataStructureValue() {
        override fun contains(item: Value): BoolValue = BoolValue((item as CharValue).value in value)
        override fun notContains(item: Value): BoolValue = BoolValue((item as CharValue).value !in value)
        override fun modulus(): IntValue = IntValue(valueOf(value.length.toLong()))
        override fun elements(): List<Value> = value.toCharArray().map { c -> CharValue(c) }

        fun concat(other: StringValue): StringValue = StringValue(value + other.value)
        fun getIndex(index: Int): CharValue = CharValue(value[index])
        fun assign(index: Int, char: CharValue) =
            StringValue(value.substring(0, index) + char.value + value.substring(index + 1))

        override fun toExpressionAST(): ExpressionAST = StringLiteralAST(value)
        override fun equals(other: Any?): Boolean = other is StringValue && value == other.value
        override fun hashCode(): Int = value.hashCode()
    }

    data class CharValue(val value: Char) : Value() {
        override fun toExpressionAST(): ExpressionAST = CharacterLiteralAST(value)
        override fun equals(other: Any?): Boolean = other is CharValue && value == other.value
        override fun hashCode(): Int = value.hashCode()
    }

    data class BoolValue(val value: Boolean) : Value() {
        fun not(): BoolValue = BoolValue(!value)
        fun iff(other: BoolValue): BoolValue = BoolValue(value && other.value || !value && !other.value)
        fun impl(other: BoolValue): BoolValue = BoolValue(!value || other.value)
        fun rimpl(other: BoolValue): BoolValue = BoolValue(!other.value || value)
        fun and(other: BoolValue): BoolValue = BoolValue(value && other.value)
        fun or(other: BoolValue): BoolValue = BoolValue(value || other.value)
        fun shortAnd(block: () -> BoolValue) = BoolValue(value && block().value)
        fun shortOr(block: () -> BoolValue) = BoolValue(value || block().value)

        override fun toExpressionAST(): ExpressionAST = BooleanLiteralAST(value)

        override fun equals(other: Any?): Boolean = other is BoolValue && value == other.value
        override fun hashCode(): Int = value.hashCode()
    }

    data class IntValue(val value: BigInteger) : Value() {
        fun negate(): IntValue = IntValue(valueOf(-1) * value)
        fun plus(other: IntValue): IntValue = IntValue(value + other.value)
        fun subtract(other: IntValue): IntValue = IntValue(value - other.value)
        fun multiply(other: IntValue): IntValue = IntValue(value * other.value)
        fun divide(other: IntValue): IntValue = IntValue(divideEuclidean(value, other.value))
        fun modulo(other: IntValue): IntValue = IntValue(value - divideEuclidean(value, other.value) * other.value)
        fun lessThan(other: IntValue): BoolValue = BoolValue(value < other.value)
        fun lessThanEquals(other: IntValue): BoolValue = BoolValue(value <= other.value)
        fun greaterThanEquals(other: IntValue): BoolValue = BoolValue(value >= other.value)
        fun greaterThan(other: IntValue): BoolValue = BoolValue(value > other.value)

        override fun equals(other: Any?): Boolean = other is IntValue && value == other.value
        override fun hashCode(): Int = value.hashCode()

        override fun toExpressionAST(): ExpressionAST = IntegerLiteralAST(value.toInt())
    }
}
