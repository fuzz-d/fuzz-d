package fuzzd.generator.ast

import fuzzd.generator.ast.error.InvalidFormatException

sealed class ExpressionAST : ASTElement {

    class BooleanLiteralAST(private val value: Boolean) : ExpressionAST() {
        override fun toString(): String = value.toString()
    }

    /**
     * grammar from documentation:
     * digits = digit {['_'] digit}
     * hexdigits = "0x" hexdigit {['_'] hexdigit}
     */
    class IntegerLiteralAST(private val value: String) : ExpressionAST() {
        init {
            if (!value.matches(Regex("(-)?(0x[0-9A-Fa-f]+(_[0-9A-Fa-f]+)*|[0-9]+(_[0-9]+)*)"))) {
                throw InvalidFormatException("Value passed (= $value) did not match supported integer format")
            }
        }

        override fun toString(): String = value
    }

    /**
     * grammar from documentation:
     * decimaldigits = digit {['_'] digit} '.' digit {['_'] digit}
     */
    class RealLiteralAST(private val value: String) : ExpressionAST() {
        init {
            if (!value.matches(Regex("(-)?[0-9]+(_[0-9]+)*.[0-9]+(_[0-9]+)*"))) {
                throw InvalidFormatException("Value passed (= $value) did not match supported float format")
            }
        }

        override fun toString() = value
    }
}
