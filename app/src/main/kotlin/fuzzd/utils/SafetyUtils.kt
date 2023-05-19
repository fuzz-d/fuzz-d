package fuzzd.utils

import fuzzd.generator.ast.ExpressionAST.BinaryExpressionAST
import fuzzd.generator.ast.ExpressionAST.FunctionMethodCallAST
import fuzzd.generator.ast.ExpressionAST.IdentifierAST
import fuzzd.generator.ast.ExpressionAST.IntegerLiteralAST
import fuzzd.generator.ast.ExpressionAST.TernaryExpressionAST
import fuzzd.generator.ast.FunctionMethodAST
import fuzzd.generator.ast.Type
import fuzzd.generator.ast.Type.IntType
import fuzzd.generator.ast.VerifierAnnotationAST.RequiresAnnotation
import fuzzd.generator.ast.operators.BinaryOperator
import fuzzd.generator.ast.operators.BinaryOperator.DivisionOperator
import fuzzd.generator.ast.operators.BinaryOperator.EqualsOperator
import fuzzd.generator.ast.operators.BinaryOperator.GreaterThanEqualOperator
import fuzzd.generator.ast.operators.BinaryOperator.GreaterThanOperator
import fuzzd.generator.ast.operators.BinaryOperator.LessThanOperator
import fuzzd.generator.ast.operators.BinaryOperator.ModuloOperator
import fuzzd.generator.ast.operators.BinaryOperator.MultiplicationOperator

private val INT_IDENTIFIER = IdentifierAST("x", IntType)
private val INT_IDENTIFIER_1 = IdentifierAST("x1", IntType)
private val INT_IDENTIFIER_2 = IdentifierAST("x2", IntType)

private val LENGTH = IdentifierAST("length", IntType)

// -------------- ARRAY INDEX SAFETY ---------------

val ABSOLUTE = FunctionMethodAST(
    "abs",
    IntType,
    listOf(INT_IDENTIFIER),
    emptyList(),
    TernaryExpressionAST(
        BinaryExpressionAST(
            INT_IDENTIFIER,
            LessThanOperator,
            IntegerLiteralAST("0"),
        ),
        BinaryExpressionAST(
            IntegerLiteralAST("-1"),
            MultiplicationOperator,
            INT_IDENTIFIER,
        ),
        INT_IDENTIFIER,
    ),
)

val SAFE_INDEX = FunctionMethodAST(
    "safeIndex",
    IntType,
    listOf(INT_IDENTIFIER, LENGTH),
    listOf(RequiresAnnotation(BinaryExpressionAST(LENGTH, GreaterThanOperator, IntegerLiteralAST(0)))),
    TernaryExpressionAST(
        BinaryExpressionAST(INT_IDENTIFIER, LessThanOperator, IntegerLiteralAST(0)),
        IntegerLiteralAST(0),
        TernaryExpressionAST(
            BinaryExpressionAST(INT_IDENTIFIER, GreaterThanEqualOperator, LENGTH),
            BinaryExpressionAST(INT_IDENTIFIER, ModuloOperator, LENGTH),
            INT_IDENTIFIER,
        ),
    ),
)

// ----------- DIVISION & MODULO SAFETY ---------------

// if (y == 0) then x else x / y
val SAFE_DIVISION_INT = FunctionMethodAST(
    "safeDivisionInt",
    IntType,
    listOf(INT_IDENTIFIER_1, INT_IDENTIFIER_2),
    emptyList(),
    TernaryExpressionAST(
        BinaryExpressionAST(INT_IDENTIFIER_2, EqualsOperator, IntegerLiteralAST("0")),
        INT_IDENTIFIER_1,
        BinaryExpressionAST(INT_IDENTIFIER_1, DivisionOperator, INT_IDENTIFIER_2),
    ),
)

// if (y == 0) then x else x % y
val SAFE_MODULO_INT = FunctionMethodAST(
    "safeModuloInt",
    IntType,
    listOf(INT_IDENTIFIER_1, INT_IDENTIFIER_2),
    emptyList(),
    TernaryExpressionAST(
        BinaryExpressionAST(INT_IDENTIFIER_2, EqualsOperator, IntegerLiteralAST("0")),
        INT_IDENTIFIER_1,
        BinaryExpressionAST(INT_IDENTIFIER_1, ModuloOperator, INT_IDENTIFIER_2),
    ),
)

val safetyMap = mapOf<Pair<BinaryOperator, Type>, FunctionMethodAST>(
    Pair(DivisionOperator, IntType) to SAFE_DIVISION_INT,
    Pair(ModuloOperator, IntType) to SAFE_MODULO_INT,
)

val WRAPPER_FUNCTIONS = listOf(
    ABSOLUTE,
    SAFE_INDEX,
    SAFE_DIVISION_INT,
    SAFE_MODULO_INT,
)
