package fuzzd.generator.ast

sealed class Type {
    object BoolType : Type() {
        override fun toString(): String = "bool"
    }

    object IntType : Type() {
        override fun toString(): String = "int"
    }

    object RealType : Type() {
        override fun toString(): String = "real"
    }

    object CharType : Type() {
        override fun toString(): String = "char"
    }
}
