package fuzzd.generator.ast

sealed class Type : ASTElement {

    open fun hasHeapType(): Boolean = false

    class TopLevelDatatypeType(val datatype: DatatypeAST) : Type() {
        override fun toString(): String = datatype.name
    }

    class DatatypeType(val datatype: DatatypeAST, val constructor: DatatypeConstructorAST) : Type() {
        override fun hasHeapType(): Boolean = constructor.fields.any { it.type().hasHeapType() }

        override fun equals(other: Any?): Boolean = other is DatatypeType && other.constructor == constructor

        override fun hashCode(): Int = constructor.hashCode()

        override fun toString(): String = datatype.name
    }

    class ClassType(val clazz: ClassAST) : Type() {
        override fun hasHeapType(): Boolean = true

        override fun equals(other: Any?): Boolean = other is ClassType && other.clazz == this.clazz

        override fun hashCode(): Int = clazz.hashCode()

        override fun toString(): String = clazz.name
    }

    class TraitType(val trait: TraitAST) : Type() {
        override fun hasHeapType(): Boolean = true

        override fun equals(other: Any?): Boolean = other is TraitType && other.trait == this.trait

        override fun hashCode(): Int = trait.hashCode()

        override fun toString(): String = trait.name
    }

    sealed class ConstructorType : Type() {
        class ArrayType(val internalType: Type) : ConstructorType() {
            override fun hasHeapType() = true

            override fun equals(other: Any?): Boolean {
                return other != null && other is ArrayType &&
                    other.internalType == internalType
            }

            override fun hashCode(): Int {
                return internalType.hashCode()
            }

            override fun toString(): String = "array<$internalType>"
        }
    }

    class MapType(val keyType: Type, val valueType: Type) : Type() {
        override fun hasHeapType(): Boolean = keyType.hasHeapType() || valueType.hasHeapType()

        override fun toString() = "map<$keyType, $valueType>"

        override fun equals(other: Any?): Boolean =
            other is MapType && other.keyType == keyType && other.valueType == valueType

        override fun hashCode(): Int {
            var result = keyType.hashCode()
            result = 31 * result + valueType.hashCode()
            return result
        }
    }

    class SetType(val innerType: Type) : Type() {
        override fun hasHeapType(): Boolean = innerType.hasHeapType()

        override fun toString(): String = "set<$innerType>"

        override fun equals(other: Any?): Boolean = other is SetType && other.innerType == innerType

        override fun hashCode(): Int = innerType.hashCode()
    }

    class MultisetType(val innerType: Type) : Type() {
        override fun hasHeapType(): Boolean = innerType.hasHeapType()

        override fun toString(): String = "multiset<$innerType>"

        override fun equals(other: Any?): Boolean = other is MultisetType && other.innerType == innerType

        override fun hashCode(): Int = innerType.hashCode()
    }

    open class SequenceType(val innerType: Type) : Type() {
        override fun hasHeapType(): Boolean = innerType.hasHeapType()

        override fun toString(): String = "seq<$innerType>"

        override fun equals(other: Any?): Boolean = other is SequenceType && other.innerType == innerType

        override fun hashCode(): Int = innerType.hashCode()
    }

    object StringType : SequenceType(CharType) {
        override fun toString(): String = "string"
    }

    class MethodReturnType(val types: List<Type>) : Type() {
        override fun toString(): String = "(${types.joinToString(", ")})"
    }

    abstract class LiteralType : Type()

    object BoolType : LiteralType() {
        override fun toString(): String = "bool"
    }

    object IntType : LiteralType() {
        override fun toString(): String = "int"
    }

    object CharType : LiteralType() {
        override fun toString(): String = "char"
    }

    object PlaceholderType : Type() {
        override fun toString(): String = "placeholder"
    }
}
