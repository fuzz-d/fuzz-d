package fuzzd.generator.ast

import fuzzd.generator.ast.ExpressionAST.BinaryExpressionAST
import fuzzd.generator.ast.ExpressionAST.IdentifierAST
import fuzzd.generator.ast.ExpressionAST.IntegerLiteralAST
import fuzzd.generator.ast.ExpressionAST.RealLiteralAST
import fuzzd.generator.ast.ExpressionAST.TernaryExpressionAST
import fuzzd.generator.ast.Type.IntType
import fuzzd.generator.ast.Type.RealType
import fuzzd.generator.ast.operators.BinaryOperator.EqualsOperator
import fuzzd.generator.ast.operators.BinaryOperator.LessThanOperator
import fuzzd.generator.ast.operators.BinaryOperator.MultiplicationOperator
import fuzzd.utils.indent

class FunctionMethodAST(
    val name: String,
    val returnType: Type,
    val params: List<IdentifierAST>,
    private val body: ExpressionAST
) : ASTElement {
    override fun toString(): String {
        val sb = StringBuilder()

        sb.append("function method $name(")
        sb.append(params.joinToString(",") { param -> "${param.name}: ${param.type()}" })
        sb.append("): $returnType {\n")
        sb.append(indent(body.toString()))
        sb.append("\n}")

        return sb.toString()
    }

    companion object {
        val MAKE_NOT_ZERO_INT = FunctionMethodAST(
            "makeNotZeroInt",
            IntType,
            listOf(IdentifierAST("x", IntType)),
            TernaryExpressionAST(
                BinaryExpressionAST(IdentifierAST("x", IntType), EqualsOperator, IntegerLiteralAST("0")),
                IntegerLiteralAST("1"),
                IdentifierAST("x", IntType)
            )
        )

        val MAKE_NOT_ZERO_REAL = FunctionMethodAST(
            "makeNotZeroReal",
            RealType,
            listOf(IdentifierAST("x", RealType)),
            TernaryExpressionAST(
                BinaryExpressionAST(IdentifierAST("x", RealType), EqualsOperator, RealLiteralAST("0.0")),
                RealLiteralAST("1.0"),
                IdentifierAST("x", RealType)
            )
        )

        val ABSOLUTE = FunctionMethodAST(
            "abs",
            IntType,
            listOf(IdentifierAST("x", IntType)),
            TernaryExpressionAST(
                BinaryExpressionAST(IdentifierAST("x", IntType), LessThanOperator, IntegerLiteralAST("0")),
                BinaryExpressionAST(IntegerLiteralAST("-1"), MultiplicationOperator, IdentifierAST("x", IntType)),
                IdentifierAST("x", IntType)
            )
        )
    }
}
