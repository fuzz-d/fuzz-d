package fuzzd.generator.ast

import fuzzd.generator.ast.Type.ArrayType
import fuzzd.generator.ast.Type.BoolType
import fuzzd.generator.ast.Type.CharType
import fuzzd.generator.ast.Type.IntType
import fuzzd.generator.ast.Type.MethodReturnType
import fuzzd.generator.ast.Type.RealType
import fuzzd.generator.ast.error.InvalidFormatException
import fuzzd.generator.ast.error.InvalidInputException
import fuzzd.generator.ast.operators.BinaryOperator
import fuzzd.generator.ast.operators.BinaryOperator.ModuloOperator
import fuzzd.generator.ast.operators.UnaryOperator
import fuzzd.utils.ABSOLUTE
import fuzzd.utils.escape
import fuzzd.utils.safetyMap

sealed class ExpressionAST : ASTElement {

    abstract fun type(): Type

    open fun makeSafe(): ExpressionAST = this

    class NonVoidMethodCallAST(private val method: MethodAST, private val params: List<ExpressionAST>) :
        ExpressionAST() {
        init {
            if (params.size != method.params.size) {
                throw InvalidInputException("Number of parameters for call to ${method.name} doesn't match. Expected ${method.params.size}, Got ${params.size}")
            }

            (params.indices).forEach() { i ->
                val paramType = params[i].type()
                val expectedType = method.params[i].type()
                if (paramType != expectedType) {
                    throw InvalidInputException("Method call parameter type mismatch for parameter $i. Expected $expectedType, got $paramType")
                }
            }
        }

        override fun type(): Type = MethodReturnType(method.returns.map { it.type() })

        override fun toString(): String = "${method.name}(${params.joinToString(", ")})"
    }

    class FunctionMethodCallAST(private val function: FunctionMethodAST, private val params: List<ExpressionAST>) :
        ExpressionAST() {
        init {
            if (params.size != function.params.size) {
                throw InvalidInputException("Number of parameters doesn't match. Expected ${function.params.size}, Got ${params.size}")
            }

            (params.indices).forEach { i ->
                val paramType = params[i].type()
                val expectedType = function.params[i].type()
                if (paramType != expectedType) {
                    throw InvalidInputException("Function call parameter type mismatch for parameter $i. Expected $expectedType, got $paramType")
                }
            }
        }

        override fun type(): Type = function.returnType

        override fun toString(): String = "${function.name}(${params.joinToString(", ")})"
    }

    class TernaryExpressionAST(
        private val condition: ExpressionAST,
        private val ifBranch: ExpressionAST,
        private val elseBranch: ExpressionAST
    ) : ExpressionAST() {
        init {
            if (condition.type() != BoolType) {
                throw InvalidInputException("Invalid input type for ternary expression condition. Got ${condition.type()}")
            }

            if (ifBranch.type() != elseBranch.type()) {
                throw InvalidInputException("Ternary expression branches have different types. If branch: ${ifBranch.type()}. Else branch: ${elseBranch.type()}")
            }
        }

        override fun type(): Type = ifBranch.type()

        override fun makeSafe(): ExpressionAST =
            TernaryExpressionAST(condition.makeSafe(), ifBranch.makeSafe(), elseBranch.makeSafe())

        override fun toString(): String = "if ($condition) then $ifBranch else $elseBranch"
    }

    class UnaryExpressionAST(private val expr: ExpressionAST, private val operator: UnaryOperator) : ExpressionAST() {
        init {
            if (!operator.supportsInput(expr.type())) {
                throw InvalidInputException("Operator $operator does not support input type ${expr.type()}")
            }
        }

        override fun type(): Type = expr.type()

        override fun makeSafe(): UnaryExpressionAST = UnaryExpressionAST(expr.makeSafe(), operator)

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

        override fun makeSafe(): ExpressionAST {
            val safeExpr1 = expr1.makeSafe()
            val safeExpr2 = expr2.makeSafe()

            val key = Pair(operator, type())
            return if (safetyMap.containsKey(key)) {
                FunctionMethodCallAST(safetyMap[key]!!, listOf(safeExpr1, safeExpr2))
            } else {
                BinaryExpressionAST(safeExpr1, operator, safeExpr2)
            }
        }

        override fun toString(): String {
            // we need to add parentheses around expressions when
            // they're boolean binary expressions and have the same precedence
            // otherwise we can choose to / not to wrap
            val wrapExpr1 = expr1 is BinaryExpressionAST && type() == BoolType
            val wrapExpr2 = expr2 is BinaryExpressionAST && type() == BoolType

            val sb = StringBuilder()
            sb.append(if (wrapExpr1) "($expr1)" else "$expr1")
            sb.append(" $operator ")
            sb.append(if (wrapExpr2) "($expr2)" else "$expr2")

            return sb.toString()
        }
    }

    open class IdentifierAST(
        val name: String,
        private val type: Type,
        val mutable: Boolean = true
    ) : ExpressionAST() {
        override fun type(): Type = type

        override fun toString(): String = name

        override fun makeSafe(): IdentifierAST = this
    }

    class ArrayIdentifierAST(
        name: String,
        private val type: ArrayType,
        val length: Int
    ) : IdentifierAST(name, type) {
        override fun type(): ArrayType = type
    }

    class ArrayIndexAST(
        val array: ArrayIdentifierAST,
        val index: ExpressionAST
    ) : IdentifierAST(
        array.name,
        array.type().internalType
    ) {
        init {
            if (index.type() != IntType) {
                throw InvalidInputException("Creating array index with index of type ${index.type()}")
            }
        }

        override fun makeSafe(): ArrayIndexAST {
            val safeIndex = index.makeSafe()

            return ArrayIndexAST(
                array,
                BinaryExpressionAST(
                    FunctionMethodCallAST(ABSOLUTE, listOf(safeIndex)),
                    ModuloOperator,
                    IntegerLiteralAST(array.length.toString())
                )
            )
        }

        override fun toString(): String {
            return "$array[$index]"
        }
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

    class BooleanLiteralAST(private val value: Boolean) : LiteralAST(value.toString(), BoolType) {
        override fun equals(other: Any?): Boolean = other is BooleanLiteralAST && other.value == value
        override fun hashCode(): Int = value.hashCode()
    }

    /**
     * grammar from documentation:
     * digits = digit {['_'] digit}
     * hexdigits = "0x" hexdigit {['_'] hexdigit}
     */
    class IntegerLiteralAST(private val value: String, private val hexFormat: Boolean = false) :
        LiteralAST(value, IntType) {
        constructor(value: Int) : this(value.toString())

        init {
            if (!value.matches(Regex("(-)?[0-9]+([0-9]+)*"))) {
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

    /**
     * grammar from documentation:
     * decimaldigits = digit {['_'] digit} '.' digit {['_'] digit}
     */
    class RealLiteralAST(value: String) : LiteralAST(value, RealType) {
        constructor(value: Float) : this(value.toString())

        init {
            if (!value.matches(Regex("(-)?[0-9]+.[0-9]+"))) {
                throw InvalidFormatException("Value passed (= $value) did not match supported float format")
            }
        }
    }

    class CharacterLiteralAST(char: Char) : LiteralAST("'${char.escape()}'", CharType)
}
