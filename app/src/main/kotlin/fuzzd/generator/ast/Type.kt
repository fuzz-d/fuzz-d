package fuzzd.generator.ast

sealed class Type {
    class ArrayType(val internalType: Type) : Type() {
        override fun equals(other: Any?): Boolean {
            return other != null && other is ArrayType &&
                other.internalType == internalType
        }

        override fun hashCode(): Int {
            return internalType.hashCode()
        }

        override fun toString(): String = "array<$internalType>"
    }

    abstract class LiteralType : Type()

    object BoolType : LiteralType() {
        override fun toString(): String = "bool"
    }

    object IntType : LiteralType() {
        override fun toString(): String = "int"
    }

    object RealType : LiteralType() {
        override fun toString(): String = "real"
    }

    object CharType : LiteralType() {
        override fun toString(): String = "char"
    }
}
