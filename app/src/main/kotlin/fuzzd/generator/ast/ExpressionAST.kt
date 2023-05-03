package fuzzd.generator.ast

import fuzzd.generator.ast.ExpressionAST.IdentifierAST
import fuzzd.generator.ast.Type.BoolType
import fuzzd.generator.ast.Type.CharType
import fuzzd.generator.ast.Type.ClassType
import fuzzd.generator.ast.Type.ConstructorType.ArrayType
import fuzzd.generator.ast.Type.DatatypeType
import fuzzd.generator.ast.Type.IntType
import fuzzd.generator.ast.Type.MapType
import fuzzd.generator.ast.Type.MethodReturnType
import fuzzd.generator.ast.Type.MultisetType
import fuzzd.generator.ast.Type.PlaceholderType
import fuzzd.generator.ast.Type.SequenceType
import fuzzd.generator.ast.Type.SetType
import fuzzd.generator.ast.Type.StringType
import fuzzd.generator.ast.Type.TopLevelDatatypeType
import fuzzd.generator.ast.Type.TraitType
import fuzzd.generator.ast.error.InvalidFormatException
import fuzzd.generator.ast.error.InvalidInputException
import fuzzd.generator.ast.operators.BinaryOperator
import fuzzd.generator.ast.operators.UnaryOperator
import fuzzd.utils.escape
import fuzzd.utils.indent
import jdk.incubator.vector.VectorOperators.Binary

fun checkParams(expected: List<IdentifierAST>, actual: List<ExpressionAST>, context: String) {
    if (expected.size != actual.size) {
        throw InvalidInputException("Number of parameters for context {$context} doesn't match. Expected ${expected.size}, got ${actual.size}")
    }

    (expected.indices).forEach { i ->
        val expectedType = expected[i].type()
        val actualType = actual[i].type()

        if (actualType != expectedType) {
            throw InvalidInputException("Parameter type mismatch for parameter $i in context {$context}. Expected $expectedType, got $actualType")
        }
    }
}

sealed class ExpressionAST : ASTElement {

    abstract fun type(): Type

    class ClassInstantiationAST(val clazz: ClassAST, val params: List<ExpressionAST>) :
        ExpressionAST() {
        init {
            val expectedParams = clazz.constructorFields.toList()
            checkParams(expectedParams, params, "constructor call for ${clazz.name}")
        }

        override fun type(): Type = ClassType(clazz)

        override fun toString(): String = "new ${clazz.name}(${params.joinToString(", ")})"
    }

    class DatatypeInstantiationAST(
        val datatype: DatatypeAST,
        val constructor: DatatypeConstructorAST,
        val params: List<ExpressionAST>,
    ) : ExpressionAST() {
        override fun type(): Type = DatatypeType(datatype, constructor)

        override fun toString(): String = "${constructor.name}(${params.joinToString(", ")})"
    }

    class DatatypeUpdateAST(
        val datatypeInstance: ExpressionAST,
        val updates: List<Pair<IdentifierAST, ExpressionAST>>,
    ) : ExpressionAST() {
        init {
            if (updates.isEmpty()) {
                throw InvalidInputException("Datatype update requires at least 1 update")
            }

            updates.forEach { (identifier, expr) ->
                if (identifier.type() != expr.type()) {
                    throw InvalidInputException("Type mismatch for $identifier in update of $datatypeInstance. Got ${expr.type()}, expected ${identifier.type()}")
                }
            }
        }

        override fun type(): Type = datatypeInstance.type()

        override fun toString(): String {
            val instanceStr = if (datatypeInstance is TernaryExpressionAST || datatypeInstance is BinaryExpressionAST)
                "($datatypeInstance)" else "$datatypeInstance"
            return "$instanceStr.(${updates.joinToString(", ") { (ident, expr) -> "$ident := $expr" }})"
        }
    }

    class MatchExpressionAST(
        val match: ExpressionAST,
        val type: Type,
        val cases: List<Pair<ExpressionAST, ExpressionAST>>,
    ) : ExpressionAST() {
        override fun type(): Type = type

        override fun toString(): String {
            val sb = StringBuilder()
            sb.appendLine("match $match {")
            cases.forEach { (case, expr) ->
                sb.appendLine(indent("case $case => $expr"))
            }
            sb.append("}")
            return sb.toString()
        }
    }

    class NonVoidMethodCallAST(val method: MethodSignatureAST, val params: List<ExpressionAST>) :
        ExpressionAST() {
        init {
            val methodParams = method.params
            checkParams(methodParams, params, "method call to ${method.name}")
        }

        override fun type(): Type = MethodReturnType(method.returns.map { it.type() })

        override fun toString(): String = "${method.name}(${params.joinToString(", ")})"
    }

    class FunctionMethodCallAST(
        val function: FunctionMethodSignatureAST,
        val params: List<ExpressionAST>,
    ) :
        ExpressionAST() {
        init {
            val functionParams = function.params
            checkParams(functionParams, params, "function method call to ${function.name}")
        }

        override fun type(): Type = function.returnType

        override fun toString(): String = "${function.name}(${params.joinToString(", ")})"
    }

    class TernaryExpressionAST(
        val condition: ExpressionAST,
        val ifBranch: ExpressionAST,
        val elseBranch: ExpressionAST,
    ) : ExpressionAST() {
        init {
            if (condition.type() != BoolType) {
                throw InvalidInputException("Invalid input type for ternary expression condition. Got ${condition.type()}")
            }
        }

        override fun type(): Type = ifBranch.type()

        override fun toString(): String = "if ($condition) then $ifBranch else $elseBranch"
    }

    class UnaryExpressionAST(val expr: ExpressionAST, val operator: UnaryOperator) : ExpressionAST() {
        init {
            if (!operator.supportsInput(expr.type())) {
                throw InvalidInputException("Operator $operator does not support input type ${expr.type()}")
            }
        }

        override fun type(): Type = expr.type()

        override fun toString(): String {
            val sb = StringBuilder()
            sb.append(operator)
            val wrapExpr = expr is BinaryExpressionAST || expr is TernaryExpressionAST
            sb.append(if (wrapExpr) "($expr)" else "$expr")
            return sb.toString()
        }
    }

    class ModulusExpressionAST(val expr: ExpressionAST) : ExpressionAST() {
        init {
            val type = expr.type()
            if (type !is PlaceholderType && type !is MapType && type !is SetType && type !is MultisetType && type !is SequenceType) {
                throw InvalidInputException("Invalid expression type for modulus. Got $type, expected map, set or seq")
            }
        }

        override fun type(): Type = IntType

        override fun toString(): String = "|$expr|"
    }

    class MultisetConversionAST(val expr: ExpressionAST) : ExpressionAST() {
        init {
            val type = expr.type()
            if (type !is SequenceType) {
                throw InvalidInputException("Conversion to multiset requires sequence type. Got $type")
            }
        }

        override fun type(): Type = MultisetType((expr.type() as SequenceType).innerType)

        override fun toString(): String = "multiset($expr)"
    }

    class BinaryExpressionAST(
        val expr1: ExpressionAST,
        val operator: BinaryOperator,
        val expr2: ExpressionAST,
    ) : ExpressionAST() {
        private val type1: Type = expr1.type()
        private val type2: Type = expr2.type()

        init {
            if (type1 != PlaceholderType && type2 != PlaceholderType && !operator.supportsInput(type1, type2)) {
                throw InvalidInputException("Operator $operator does not support input types ($type1, $type2)")
            }
        }

        override fun type(): Type = operator.outputType(type1, type2)

        override fun toString(): String {
            val sb = StringBuilder()
            sb.append(if (shouldWrap(expr1)) "($expr1)" else "$expr1")
            sb.append(" $operator ")
            sb.append(if (shouldWrap(expr2)) "($expr2)" else "$expr2")

            return sb.toString()
        }

        private fun shouldWrap(expr: ExpressionAST) = expr is TernaryExpressionAST || expr is BinaryExpressionAST
    }

    open class IdentifierAST(
        val name: String,
        private val type: Type,
        val mutable: Boolean = true,
        private var initialised: Boolean = false,
    ) : ExpressionAST() {
        override fun type(): Type = type

        override fun toString(): String = name

        open fun initialise(): IdentifierAST = if (initialised) this else IdentifierAST(name, type, mutable, true)

        fun initialised(): Boolean = initialised

        override fun equals(other: Any?): Boolean =
            other is IdentifierAST && other.name == this.name && other.type == this.type

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + type.hashCode()
            result = 31 * result + mutable.hashCode()
            return result
        }
    }

    abstract class ObjectOrientedInstanceAST(name: String, type: Type, mutable: Boolean, initialised: Boolean) : IdentifierAST(name, type, mutable, initialised) {
        abstract fun fields(): List<IdentifierAST>
        abstract fun functionMethods(): List<ClassInstanceFunctionMethodSignatureAST>

        abstract fun methods(): List<ClassInstanceMethodSignatureAST>
    }

    class TraitInstanceAST(
        val trait: TraitAST,
        name: String,
        mutable: Boolean = true,
        initialised: Boolean = false,
    ) : ObjectOrientedInstanceAST(name, TraitType(trait), mutable, initialised) {
        private val fields = trait.fields().map { ClassInstanceFieldAST(this, it) }
        private val functionMethods = trait.functionMethods().map { ClassInstanceFunctionMethodSignatureAST(this, it) }
        private val methods = trait.methods().map { ClassInstanceMethodSignatureAST(this, it) }

        override fun fields(): List<IdentifierAST> = fields
        override fun functionMethods(): List<ClassInstanceFunctionMethodSignatureAST> = functionMethods
        override fun methods(): List<ClassInstanceMethodSignatureAST> = methods

        override fun initialise(): IdentifierAST =
            if (initialised()) this else TraitInstanceAST(trait, name, mutable, true)

        override fun equals(other: Any?): Boolean = other is TraitInstanceAST && trait == other.trait && name == other.name

        override fun hashCode(): Int {
            var result = super.hashCode()
            result = 31 * result + trait.hashCode()
            result = 31 * result + fields.hashCode()
            result = 31 * result + functionMethods.hashCode()
            result = 31 * result + methods.hashCode()
            return result
        }
    }

    class ClassInstanceAST(
        val clazz: ClassAST,
        name: String,
        mutable: Boolean = true,
        initialised: Boolean = false,
    ) : ObjectOrientedInstanceAST(name, ClassType(clazz), mutable, initialised) {
        private val fields = clazz.fields.map { ClassInstanceFieldAST(this, it) }
        private val functionMethods = clazz.functionMethods.map { ClassInstanceFunctionMethodSignatureAST(this, it.signature) }
        private val methods = clazz.methods.map { ClassInstanceMethodSignatureAST(this, it.signature) }

        override fun fields(): List<IdentifierAST> = fields

        override fun functionMethods(): List<ClassInstanceFunctionMethodSignatureAST> = functionMethods

        override fun methods(): List<ClassInstanceMethodSignatureAST> = methods

        override fun initialise(): IdentifierAST =
            if (initialised()) this else ClassInstanceAST(clazz, name, mutable, true)

        override fun equals(other: Any?): Boolean =
            other is ClassInstanceAST &&
                    clazz == other.clazz &&
                    name == other.name

        override fun hashCode(): Int {
            var result = super.hashCode()
            result = 31 * result + clazz.hashCode()
            result = 31 * result + fields.hashCode()
            result = 31 * result + functionMethods.hashCode()
            result = 31 * result + methods.hashCode()
            return result
        }
    }

    open class TopLevelDatatypeInstanceAST(
        name: String,
        open val datatype: TopLevelDatatypeType,
        mutable: Boolean = true,
        initialised: Boolean = false,
    ) : IdentifierAST(name, datatype, mutable, initialised) {
        override fun equals(other: Any?): Boolean = other is TopLevelDatatypeInstanceAST && other.name == name && other.datatype == datatype

        override fun hashCode(): Int {
            var result = super.hashCode()
            result = 31 * result + name.hashCode()
            result = 31 * result + datatype.hashCode()
            return result
        }
    }

    class DatatypeInstanceAST(
        name: String,
        override val datatype: DatatypeType,
        mutable: Boolean = true,
        initialised: Boolean = false,
    ) : TopLevelDatatypeInstanceAST(name, datatype, mutable, initialised)

    class ClassInstanceFieldAST(
        val classInstance: IdentifierAST,
        val classField: IdentifierAST,
    ) : IdentifierAST("$classInstance.$classField", classField.type(), mutable = true, initialised = true)

    class DatatypeDestructorAST(
        val datatypeInstance: ExpressionAST,
        val field: IdentifierAST,
    ) : IdentifierAST("$datatypeInstance.$field", field.type(), true, true) {
        override fun type(): Type = field.type()

        override fun toString(): String {
            val shouldWrap = datatypeInstance !is IdentifierAST && datatypeInstance !is DatatypeInstantiationAST
            return if (shouldWrap) {
                "($datatypeInstance).$field"
            } else {
                "$datatypeInstance.$field"
            }
        }

        override fun equals(other: Any?): Boolean = other is DatatypeDestructorAST && other.datatypeInstance == datatypeInstance && other.field == field

        override fun hashCode(): Int {
            var result = super.hashCode()
            result = 31 * result + datatypeInstance.hashCode()
            result = 31 * result + field.hashCode()
            return result
        }
    }

    class ArrayIndexAST(
        val array: IdentifierAST,
        val index: ExpressionAST,
        private val initialised: Boolean = false,
    ) : IdentifierAST(
        array.name,
        (array.type() as ArrayType).internalType,
        initialised,
    ) {
        init {
            if (array.type() !is ArrayType) {
                throw InvalidInputException("Creating array index with identifier of type ${array.type()}")
            }

            if (index.type() != IntType) {
                throw InvalidInputException("Creating array index with index of type ${index.type()}")
            }
        }

        override fun initialise(): ArrayIndexAST = if (initialised()) this else ArrayIndexAST(array, index, initialised = true)

        override fun toString(): String = "$array[$index]"
    }

    abstract class IndexAST(val dataStructure: ExpressionAST, val key: ExpressionAST) : ExpressionAST() {
        override fun toString(): String =
            if (dataStructure !is IdentifierAST) {
                "($dataStructure)[$key]"
            } else {
                "$dataStructure[$key]"
            }
    }

    class SequenceIndexAST(val sequence: ExpressionAST, key: ExpressionAST) : IndexAST(sequence, key) {
        init {
            val sequenceType = sequence.type()
            if (sequenceType !is SequenceType) {
                throw InvalidInputException("Expected sequence type for sequence index. Got $sequenceType")
            }

            if (key.type() != IntType) {
                throw InvalidInputException("Got invalid type for sequence index. Got ${key.type()}, expected int")
            }
        }

        override fun type(): Type = (sequence.type() as SequenceType).innerType
    }

    class MapIndexAST(val map: ExpressionAST, key: ExpressionAST) : IndexAST(map, key) {
        init {
            val mapType = map.type()
            if (mapType !is MapType) throw InvalidInputException("Expected map type for map index. Got $mapType")

            val expectedKeyType = mapType.keyType

            if (key.type() != expectedKeyType) {
                throw InvalidInputException("Invalid key type for map IndexAST. Expected $expectedKeyType, got ${key.type()}")
            }
        }

        override fun type(): Type = (map.type() as MapType).valueType
    }

    class MultisetIndexAST(val multiset: ExpressionAST, key: ExpressionAST) : IndexAST(multiset, key) {
        init {
            val multisetType = multiset.type()
            if (multisetType !is MultisetType) {
                throw InvalidInputException("Expected multiset type for multiset index. Got $multisetType")
            }
            val expectedKeyType = multisetType.innerType
            if (key.type() != expectedKeyType) {
                throw InvalidInputException("Invalid key type for multiset IndexAST. Expected $expectedKeyType, got ${key.type()}")
            }
        }

        override fun type(): Type = IntType
    }

    class IndexAssignAST(
        val expression: ExpressionAST,
        val key: ExpressionAST,
        val value: ExpressionAST,
    ) : ExpressionAST() {
        init {
            when (val type = expression.type()) {
                is MapType -> {
                    if (key.type() != type.keyType) {
                        throw InvalidInputException("Invalid key type for multiset IndexAssignAST. Expected ${type.keyType}, got ${key.type()}")
                    }

                    if (value.type() != type.valueType) {
                        throw InvalidInputException("Invalid value type for multiset IndexAssignAST. Expected ${type.valueType}, got ${value.type()}")
                    }
                }

                is MultisetType -> {
                    if (key.type() != type.innerType) {
                        throw InvalidInputException("Invalid key type for multiset IndexAssignAST. Expected ${type.innerType}, got ${value.type()}")
                    }

                    if (value.type() != IntType) {
                        throw InvalidInputException("Invalid value type for multiset IndexAssignAST. Expected int, got ${value.type()}")
                    }
                }

                is SequenceType -> {
                    if (key.type() != IntType) {
                        throw InvalidInputException("Invalid key type for sequence IndexAssignAST. Expected int, got ${key.type()}")
                    }

                    if (value.type() != type.innerType) {
                        throw InvalidInputException("Invalid value type for sequence IndexAssignAST. Expected ${type.innerType}, got ${value.type()}")
                    }
                }

                else -> throw InvalidInputException("Invalid identifier type for IndexAssignAST. Expected map or multiset, got $type")
            }
        }

        override fun type(): Type = expression.type()

        override fun toString(): String = if (expression !is IdentifierAST) {
            "($expression)[$key := $value]"
        } else {
            "$expression[$key := $value]"
        }
    }

    class MapConstructorAST(
        val keyType: Type,
        val valueType: Type,
        val assignments: List<Pair<ExpressionAST, ExpressionAST>> = emptyList(),
    ) : ExpressionAST() {
        init {
            assignments.indices.forEach { i ->
                val pair = assignments[i]

                if (pair.first.type() != keyType) {
                    throw InvalidInputException("Invalid key type for index $i of map constructor. Expected $keyType, got ${pair.first.type()}")
                }

                if (pair.second.type() != valueType) {
                    throw InvalidInputException("Invalid value type for index $i of map constructor. Expected $valueType, got ${pair.second.type()}")
                }
            }
        }

        override fun type(): Type = MapType(keyType, valueType)

        override fun toString() = "map[${assignments.joinToString(", ") { "${it.first} := ${it.second}" }}]"
    }

    class SetDisplayAST(val exprs: List<ExpressionAST>, val isMultiset: Boolean) : ExpressionAST() {
        private var innerType = if (exprs.isEmpty()) PlaceholderType else exprs[0].type()

        constructor(exprs: List<ExpressionAST>, isMultiset: Boolean, innerType: Type) : this(exprs, isMultiset) {
            this.innerType = innerType
        }

        override fun type(): Type = if (isMultiset) MultisetType(innerType) else SetType(innerType)

        override fun toString(): String = "${if (isMultiset) "multiset" else ""}{${exprs.joinToString(", ")}}"
    }

    class SequenceDisplayAST(val exprs: List<ExpressionAST>) : ExpressionAST() {
        private var innerType = if (exprs.isEmpty()) PlaceholderType else exprs[0].type()

        constructor(exprs: List<ExpressionAST>, innerType: Type) : this(exprs) {
            this.innerType = innerType
        }

        override fun type(): Type = SequenceType(innerType)

        override fun toString(): String = "[${exprs.joinToString(", ")}]"
    }

    class ArrayLengthAST(val array: IdentifierAST) : ExpressionAST() {
        init {
            if (array.type() !is ArrayType) {
                throw InvalidInputException("Creating array index with identifier of type ${array.type()}")
            }
        }

        override fun type(): Type = IntType

        override fun toString(): String = "${array.name}.Length"
    }

    class ArrayInitAST(val length: Int, private val type: ArrayType) : ExpressionAST() {
        override fun type(): Type = type

        override fun toString(): String = "new ${type.internalType}[$length]"
    }

    abstract class LiteralAST(private val value: String, private val type: Type) : ExpressionAST() {
        override fun toString(): String = value

        override fun type(): Type = type

        override fun equals(other: Any?): Boolean = other is LiteralAST && other.value == value
    }

    class BooleanLiteralAST(val value: Boolean) : LiteralAST(value.toString(), BoolType) {
        override fun equals(other: Any?): Boolean = other is BooleanLiteralAST && other.value == value
        override fun hashCode(): Int = value.hashCode()
    }

    /**
     * grammar from documentation:
     * digits = digit {['_'] digit}
     * hexdigits = "0x" hexdigit {['_'] hexdigit}
     */
    class IntegerLiteralAST(val value: String, private val hexFormat: Boolean = false) :
        LiteralAST(value, IntType) {
        constructor(value: Int) : this(value.toString())

        init {
            if (!value.matches(Regex("(-)?[0-9]+"))) {
                throw InvalidFormatException("Value passed (= $value) did not match supported integer format")
            }
        }

        override fun toString(): String = if (hexFormat) {
            val sb = StringBuilder()
            val negative = value[0] == '-'
            if (negative) sb.append("-")
            sb.append("0x")

            if (negative) {
                sb.append(Integer.toHexString(value.substring(1).toInt()))
            } else {
                sb.append(Integer.toHexString(value.toInt()))
            }

            sb.toString()
        } else {
            value
        }
    }

    class CharacterLiteralAST(val value: Char) : LiteralAST("'${value.escape()}'", CharType)

    class StringLiteralAST(val value: String) : LiteralAST("\"$value\"", StringType)
}
