package fuzzd.generator.ast

import fuzzd.generator.ast.Type.BoolType
import fuzzd.generator.ast.Type.CharType
import fuzzd.generator.ast.Type.IntType
import fuzzd.generator.ast.Type.RealType
import fuzzd.generator.ast.error.InvalidFormatException
import fuzzd.generator.ast.error.InvalidInputException
import fuzzd.generator.ast.operators.BinaryOperator
import fuzzd.generator.ast.operators.UnaryOperator

sealed class ExpressionAST : ASTElement {

    abstract fun type(): Type

    class UnaryExpressionAST(private val expr: ExpressionAST, private val operator: UnaryOperator) : ExpressionAST() {
        override fun type(): Type = expr.type()

        override fun toString(): String {
            val sb = StringBuilder()
            sb.append(operator)
            val wrapExpr = expr is BinaryExpressionAST
            sb.append(if (wrapExpr) "($expr)" else "$expr")
            return sb.toString()
        }
    }

    class BinaryExpressionAST(
        private val expr1: ExpressionAST,
        private val operator: BinaryOperator,
        private val expr2: ExpressionAST
    ) : ExpressionAST() {
        private val type1: Type = expr1.type()
        private val type2: Type = expr2.type()

        init {
            if (!operator.supportsInput(type1, type2)) {
                throw InvalidInputException("Operator $operator does not support input types ($type1, $type2)")
            }
        }

        override fun type(): Type = operator.outputType(type1, type2)

        override fun toString(): String {
            // we need to add parentheses around expressions when
            // they're boolean binary expressions and have the same precedence
            // otherwise we can choose to / not to wrap
            val wrapExpr1 = expr1 is BinaryExpressionAST && type() == BoolType
            val wrapExpr2 = expr2 is BinaryExpressionAST && type() == BoolType

            val sb = StringBuilder()
            sb.append(if (wrapExpr1) "($expr1)" else "$expr1")
            sb.append(operator)
            sb.append(if (wrapExpr2) "($expr2)" else "$expr2")

            return sb.toString()
        }
    }

    class IdentifierAST(private val name: String, private val type: Type) : ExpressionAST() {
        override fun type(): Type = type

        override fun toString(): String = name
    }

    abstract class LiteralAST(private val value: String, private val type: Type) : ExpressionAST() {
        override fun toString(): String = value

        override fun type(): Type = type
    }

    class BooleanLiteralAST(value: Boolean) : LiteralAST(value.toString(), BoolType)

    /**
     * grammar from documentation:
     * digits = digit {['_'] digit}
     * hexdigits = "0x" hexdigit {['_'] hexdigit}
     */
    class IntegerLiteralAST(value: String) : LiteralAST(value, IntType) {
        init {
            if (!value.matches(Regex("(-)?(0x[0-9A-Fa-f]+(_[0-9A-Fa-f]+)*|[0-9]+(_[0-9]+)*)"))) {
                throw InvalidFormatException("Value passed (= $value) did not match supported integer format")
            }
        }
    }

    /**
     * grammar from documentation:
     * decimaldigits = digit {['_'] digit} '.' digit {['_'] digit}
     */
    class RealLiteralAST(value: String) : LiteralAST(value, RealType) {
        init {
            if (!value.matches(Regex("(-)?[0-9]+(_[0-9]+)*.[0-9]+(_[0-9]+)*"))) {
                throw InvalidFormatException("Value passed (= $value) did not match supported float format")
            }
        }
    }

    class CharacterLiteralAST(value: String) : LiteralAST(value, CharType)
}
