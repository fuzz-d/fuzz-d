package fuzzd.utils

import fuzzd.generator.ast.ExpressionAST.BinaryExpressionAST
import fuzzd.generator.ast.ExpressionAST.FunctionMethodCallAST
import fuzzd.generator.ast.ExpressionAST.IdentifierAST
import fuzzd.generator.ast.ExpressionAST.IntegerLiteralAST
import fuzzd.generator.ast.ExpressionAST.RealLiteralAST
import fuzzd.generator.ast.ExpressionAST.TernaryExpressionAST
import fuzzd.generator.ast.FunctionMethodAST
import fuzzd.generator.ast.Type
import fuzzd.generator.ast.Type.CharType
import fuzzd.generator.ast.Type.IntType
import fuzzd.generator.ast.Type.RealType
import fuzzd.generator.ast.operators.BinaryOperator
import fuzzd.generator.ast.operators.BinaryOperator.AdditionOperator
import fuzzd.generator.ast.operators.BinaryOperator.ConjunctionOperator
import fuzzd.generator.ast.operators.BinaryOperator.DisjunctionOperator
import fuzzd.generator.ast.operators.BinaryOperator.DivisionOperator
import fuzzd.generator.ast.operators.BinaryOperator.EqualsOperator
import fuzzd.generator.ast.operators.BinaryOperator.GreaterThanOperator
import fuzzd.generator.ast.operators.BinaryOperator.LessThanOperator
import fuzzd.generator.ast.operators.BinaryOperator.ModuloOperator
import fuzzd.generator.ast.operators.BinaryOperator.MultiplicationOperator
import fuzzd.generator.ast.operators.BinaryOperator.SubtractionOperator

private const val DAFNY_MIN_INT = -100_000
private const val DAFNY_MAX_INT = 100_000
private const val DAFNY_MIN_REAL = -100_000f
private const val DAFNY_MAX_REAL = 100_000f

private val INT_IDENTIFIER = IdentifierAST("x", IntType)
private val INT_IDENTIFIER_1 = IdentifierAST("x1", IntType)
private val INT_IDENTIFIER_2 = IdentifierAST("x2", IntType)

private val REAL_IDENTIFIER_1 = IdentifierAST("x1", RealType)
private val REAL_IDENTIFIER_2 = IdentifierAST("x2", RealType)

private val CHAR_IDENTIFIER_1 = IdentifierAST("c1", CharType)
private val CHAR_IDENTIFIER_2 = IdentifierAST("c2", CharType)

// -------------- ARRAY INDEX SAFETY ---------------

val ABSOLUTE = FunctionMethodAST(
    "abs",
    IntType,
    listOf(INT_IDENTIFIER),
    TernaryExpressionAST(
        BinaryExpressionAST(
            INT_IDENTIFIER,
            LessThanOperator,
            IntegerLiteralAST("0")
        ),
        BinaryExpressionAST(
            IntegerLiteralAST("-1"),
            MultiplicationOperator,
            INT_IDENTIFIER
        ),
        INT_IDENTIFIER
    )
)

// ----------- DIVISION & MODULO SAFETY ---------------

// if (y == 0) then x else x / y
val SAFE_DIVISION_INT = FunctionMethodAST(
    "safeDivisionInt",
    IntType,
    listOf(INT_IDENTIFIER_1, INT_IDENTIFIER_2),
    TernaryExpressionAST(
        BinaryExpressionAST(INT_IDENTIFIER_2, EqualsOperator, IntegerLiteralAST("0")),
        INT_IDENTIFIER_1,
        BinaryExpressionAST(INT_IDENTIFIER_1, DivisionOperator, INT_IDENTIFIER_2)
    )
)

// if (y == 0) then x else x % y
val SAFE_MODULO_INT = FunctionMethodAST(
    "safeModuloInt",
    IntType,
    listOf(INT_IDENTIFIER_1, INT_IDENTIFIER_2),
    TernaryExpressionAST(
        BinaryExpressionAST(INT_IDENTIFIER_2, EqualsOperator, IntegerLiteralAST("0")),
        INT_IDENTIFIER_1,
        BinaryExpressionAST(FunctionMethodCallAST(ABSOLUTE, listOf(INT_IDENTIFIER_1)), ModuloOperator, INT_IDENTIFIER_2)
    )
)

// if ((y < 0.001) && (y > -0.001)) || (x / y > 100_000) then x else x / y
val SAFE_DIVISION_REAL = FunctionMethodAST(
    "safeDivisionReal",
    RealType,
    listOf(REAL_IDENTIFIER_1, REAL_IDENTIFIER_2),
    TernaryExpressionAST(
        BinaryExpressionAST(
            BinaryExpressionAST(
                BinaryExpressionAST(REAL_IDENTIFIER_2, LessThanOperator, RealLiteralAST(0.001f)),
                ConjunctionOperator,
                BinaryExpressionAST(REAL_IDENTIFIER_2, GreaterThanOperator, RealLiteralAST(-0.001f))
            ),
            DisjunctionOperator,
            BinaryExpressionAST(
                BinaryExpressionAST(REAL_IDENTIFIER_1, DivisionOperator, REAL_IDENTIFIER_2),
                GreaterThanOperator,
                RealLiteralAST(DAFNY_MAX_REAL)
            )
        ),
        REAL_IDENTIFIER_1,
        BinaryExpressionAST(REAL_IDENTIFIER_1, DivisionOperator, REAL_IDENTIFIER_2)
    )
)

// -------------- SUBTRACTION SAFETY ------------------
val SAFE_SUBTRACT_CHAR = FunctionMethodAST(
    "safeSubtractChar",
    CharType,
    listOf(CHAR_IDENTIFIER_1, CHAR_IDENTIFIER_2),
    TernaryExpressionAST(
        BinaryExpressionAST(CHAR_IDENTIFIER_1, LessThanOperator, CHAR_IDENTIFIER_2),
        BinaryExpressionAST(CHAR_IDENTIFIER_2, SubtractionOperator, CHAR_IDENTIFIER_1),
        BinaryExpressionAST(CHAR_IDENTIFIER_1, SubtractionOperator, CHAR_IDENTIFIER_2)
    )
)

val SAFE_SUBTRACT_INT = FunctionMethodAST(
    "safeSubtractInt",
    IntType,
    listOf(INT_IDENTIFIER_1, INT_IDENTIFIER_2),
    TernaryExpressionAST(
        BinaryExpressionAST(
            BinaryExpressionAST(
                BinaryExpressionAST(
                    INT_IDENTIFIER_1,
                    SubtractionOperator,
                    INT_IDENTIFIER_2
                ),
                LessThanOperator,
                IntegerLiteralAST(DAFNY_MIN_INT)
            ),
            DisjunctionOperator,
            BinaryExpressionAST(
                BinaryExpressionAST(
                    INT_IDENTIFIER_1,
                    SubtractionOperator,
                    INT_IDENTIFIER_2
                ),
                GreaterThanOperator,
                IntegerLiteralAST(DAFNY_MAX_INT)
            )
        ),
        BinaryExpressionAST(INT_IDENTIFIER_1, AdditionOperator, INT_IDENTIFIER_2),
        BinaryExpressionAST(INT_IDENTIFIER_1, SubtractionOperator, INT_IDENTIFIER_2)
    )
)

val SAFE_SUBTRACT_REAL = FunctionMethodAST(
    "safeSubtractReal",
    RealType,
    listOf(REAL_IDENTIFIER_1, REAL_IDENTIFIER_2),
    TernaryExpressionAST(
        BinaryExpressionAST(
            BinaryExpressionAST(
                BinaryExpressionAST(REAL_IDENTIFIER_1, SubtractionOperator, REAL_IDENTIFIER_2),
                LessThanOperator,
                RealLiteralAST(DAFNY_MIN_REAL)
            ),
            DisjunctionOperator,
            BinaryExpressionAST(
                BinaryExpressionAST(REAL_IDENTIFIER_1, SubtractionOperator, REAL_IDENTIFIER_2),
                GreaterThanOperator,
                RealLiteralAST(DAFNY_MAX_REAL)
            )
        ),
        BinaryExpressionAST(INT_IDENTIFIER_1, AdditionOperator, INT_IDENTIFIER_2),
        BinaryExpressionAST(INT_IDENTIFIER_1, SubtractionOperator, INT_IDENTIFIER_2)
    )
)

// ----------------------- ADDITION SAFETY --------------------------
val SAFE_ADDITION_INT = FunctionMethodAST(
    "safeAdditionInt",
    IntType,
    listOf(INT_IDENTIFIER_1, INT_IDENTIFIER_2),
    TernaryExpressionAST(
        BinaryExpressionAST(
            BinaryExpressionAST(
                BinaryExpressionAST(INT_IDENTIFIER_1, AdditionOperator, INT_IDENTIFIER_2),
                LessThanOperator,
                IntegerLiteralAST(DAFNY_MIN_INT)
            ),
            DisjunctionOperator,
            BinaryExpressionAST(
                BinaryExpressionAST(INT_IDENTIFIER_1, AdditionOperator, INT_IDENTIFIER_2),
                GreaterThanOperator,
                IntegerLiteralAST(DAFNY_MAX_INT)
            )
        ),
        BinaryExpressionAST(INT_IDENTIFIER_1, SubtractionOperator, INT_IDENTIFIER_2),
        BinaryExpressionAST(INT_IDENTIFIER_1, AdditionOperator, INT_IDENTIFIER_2)
    )
)

val SAFE_ADDITION_REAL = FunctionMethodAST(
    "safeAdditionReal",
    RealType,
    listOf(REAL_IDENTIFIER_1, REAL_IDENTIFIER_2),
    TernaryExpressionAST(
        BinaryExpressionAST(
            BinaryExpressionAST(
                BinaryExpressionAST(REAL_IDENTIFIER_1, AdditionOperator, REAL_IDENTIFIER_2),
                LessThanOperator,
                RealLiteralAST(DAFNY_MIN_REAL)
            ),
            DisjunctionOperator,
            BinaryExpressionAST(
                BinaryExpressionAST(REAL_IDENTIFIER_1, AdditionOperator, REAL_IDENTIFIER_2),
                GreaterThanOperator,
                RealLiteralAST(DAFNY_MAX_REAL)
            )
        ),
        BinaryExpressionAST(REAL_IDENTIFIER_1, SubtractionOperator, REAL_IDENTIFIER_2),
        BinaryExpressionAST(REAL_IDENTIFIER_1, AdditionOperator, REAL_IDENTIFIER_2)
    )
)

// ----------------------- MULTIPLICATION SAFETY ---------------------------
val SAFE_MULTIPLY_INT = FunctionMethodAST(
    "safeMultiplyInt",
    IntType,
    listOf(INT_IDENTIFIER_1, INT_IDENTIFIER_2),
    TernaryExpressionAST(
        BinaryExpressionAST(
            BinaryExpressionAST(
                BinaryExpressionAST(INT_IDENTIFIER_1, MultiplicationOperator, INT_IDENTIFIER_2),
                LessThanOperator,
                IntegerLiteralAST(DAFNY_MIN_INT)
            ),
            DisjunctionOperator,
            BinaryExpressionAST(
                BinaryExpressionAST(INT_IDENTIFIER_1, MultiplicationOperator, INT_IDENTIFIER_2),
                GreaterThanOperator,
                IntegerLiteralAST(DAFNY_MAX_INT)
            )
        ),
        BinaryExpressionAST(INT_IDENTIFIER_1, DivisionOperator, INT_IDENTIFIER_2),
        BinaryExpressionAST(INT_IDENTIFIER_1, MultiplicationOperator, INT_IDENTIFIER_2)
    )
)

val SAFE_MULTIPLY_REAL = FunctionMethodAST(
    "safeMultiplyReal",
    RealType,
    listOf(REAL_IDENTIFIER_1, REAL_IDENTIFIER_2),
    TernaryExpressionAST(
        BinaryExpressionAST(
            BinaryExpressionAST(
                BinaryExpressionAST(REAL_IDENTIFIER_1, MultiplicationOperator, REAL_IDENTIFIER_2),
                LessThanOperator,
                RealLiteralAST(DAFNY_MIN_REAL)
            ),
            DisjunctionOperator,
            BinaryExpressionAST(
                BinaryExpressionAST(REAL_IDENTIFIER_1, MultiplicationOperator, REAL_IDENTIFIER_2),
                GreaterThanOperator,
                RealLiteralAST(DAFNY_MAX_REAL)
            )
        ),
        BinaryExpressionAST(REAL_IDENTIFIER_1, DivisionOperator, REAL_IDENTIFIER_2),
        BinaryExpressionAST(REAL_IDENTIFIER_1, MultiplicationOperator, REAL_IDENTIFIER_2)
    )
)

val safetyMap = mapOf<Pair<BinaryOperator, Type>, FunctionMethodAST>(
    Pair(AdditionOperator, IntType) to SAFE_ADDITION_INT,
    Pair(AdditionOperator, RealType) to SAFE_ADDITION_REAL,
    Pair(DivisionOperator, IntType) to SAFE_DIVISION_INT,
    Pair(DivisionOperator, RealType) to SAFE_DIVISION_REAL,
    Pair(ModuloOperator, IntType) to SAFE_MODULO_INT,
    Pair(MultiplicationOperator, IntType) to SAFE_MULTIPLY_INT,
    Pair(MultiplicationOperator, RealType) to SAFE_MULTIPLY_REAL,
    Pair(SubtractionOperator, CharType) to SAFE_SUBTRACT_CHAR,
    Pair(SubtractionOperator, IntType) to SAFE_SUBTRACT_INT,
    Pair(SubtractionOperator, RealType) to SAFE_SUBTRACT_REAL
)
