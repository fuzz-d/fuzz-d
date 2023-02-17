package fuzzd.generator.ast

sealed class Type {
    class ClassType(val clazz: ClassAST) : Type() {
        override fun equals(other: Any?): Boolean = other is ClassType && other.clazz == this.clazz

        override fun hashCode(): Int = clazz.hashCode()

        override fun toString(): String = clazz.name
    }

    class TraitType(val trait: TraitAST) : Type() {
        override fun equals(other: Any?): Boolean = other is TraitType && other.trait == this.trait

        override fun hashCode(): Int = trait.hashCode()

        override fun toString(): String = trait.name
    }

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

    class MethodReturnType(val types: List<Type>) : Type()

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
