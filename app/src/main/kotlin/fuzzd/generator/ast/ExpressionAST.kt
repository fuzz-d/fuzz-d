package fuzzd.generator.ast

import fuzzd.generator.ast.Type.BoolType
import fuzzd.generator.ast.error.InvalidFormatException
import fuzzd.generator.ast.error.InvalidInputException
import fuzzd.generator.ast.operators.BinaryOperator
import kotlin.random.Random

sealed class ExpressionAST : ASTElement {

    abstract fun type(): Type

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
            val wrapExpr1 = (
                expr1 is BinaryExpressionAST && type() == BoolType && type1 == BoolType &&
                    operator != expr1.operator && operator.precedence == expr1.operator.precedence
                ) || Random.Default.nextBoolean()

            val wrapExpr2 = (
                expr2 is BinaryExpressionAST && type() == BoolType && type1 == BoolType &&
                    operator != expr2.operator && operator.precedence == expr2.operator.precedence
                ) || Random.Default.nextBoolean()

//            val wrapThis = Random.Default.nextBoolean()

            val sb = StringBuilder()
//            if (wrapThis) sb.append("(")
            sb.append(if (wrapExpr1) "($expr1)" else "$expr1")
            sb.append(operator)
            sb.append(if (wrapExpr2) "($expr2)" else "$expr1")
//            if (wrapThis) sb.append(")")

            return sb.toString()
        }
    }

    class BooleanLiteralAST(private val value: Boolean) : ExpressionAST() {
        override fun type(): Type = BoolType

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

        override fun type(): Type = Type.IntType

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

        override fun type(): Type = Type.RealType

        override fun toString() = value
    }
}
