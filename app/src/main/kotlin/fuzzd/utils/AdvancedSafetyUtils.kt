package fuzzd.utils

import fuzzd.generator.ast.ExpressionAST.BinaryExpressionAST
import fuzzd.generator.ast.ExpressionAST.BooleanLiteralAST
import fuzzd.generator.ast.ExpressionAST.IdentifierAST
import fuzzd.generator.ast.ExpressionAST.IntegerLiteralAST
import fuzzd.generator.ast.ExpressionAST.MapIndexAST
import fuzzd.generator.ast.ExpressionAST.MapIndexAssignAST
import fuzzd.generator.ast.ExpressionAST.StringLiteralAST
import fuzzd.generator.ast.ExpressionAST.TernaryExpressionAST
import fuzzd.generator.ast.ExpressionAST.UnaryExpressionAST
import fuzzd.generator.ast.MethodAST
import fuzzd.generator.ast.SequenceAST
import fuzzd.generator.ast.StatementAST.AssignmentAST
import fuzzd.generator.ast.StatementAST.DeclarationAST
import fuzzd.generator.ast.StatementAST.IfStatementAST
import fuzzd.generator.ast.StatementAST.MultiDeclarationAST
import fuzzd.generator.ast.StatementAST.PrintAST
import fuzzd.generator.ast.Type.BoolType
import fuzzd.generator.ast.Type.IntType
import fuzzd.generator.ast.Type.MapType
import fuzzd.generator.ast.Type.StringType
import fuzzd.generator.ast.operators.BinaryOperator
import fuzzd.generator.ast.operators.BinaryOperator.BooleanBinaryOperator
import fuzzd.generator.ast.operators.BinaryOperator.DisjunctionOperator
import fuzzd.generator.ast.operators.BinaryOperator.DivisionOperator
import fuzzd.generator.ast.operators.BinaryOperator.EqualsOperator
import fuzzd.generator.ast.operators.BinaryOperator.GreaterThanEqualOperator
import fuzzd.generator.ast.operators.BinaryOperator.LessThanEqualOperator
import fuzzd.generator.ast.operators.BinaryOperator.LessThanOperator
import fuzzd.generator.ast.operators.BinaryOperator.MapMembershipOperator
import fuzzd.generator.ast.operators.BinaryOperator.ModuloOperator
import fuzzd.generator.ast.operators.BinaryOperator.MultiplicationOperator
import fuzzd.generator.ast.operators.UnaryOperator
import fuzzd.generator.ast.operators.UnaryOperator.NotOperator

private val INT_PARAM_1 = IdentifierAST("p1", IntType)
private val INT_PARAM_2 = IdentifierAST("p2", IntType)
private val STATE = IdentifierAST("state", MapType(StringType, BoolType))
private val ID = IdentifierAST("id", StringType)
private val INT_RETURNS = IdentifierAST("r1", IntType)
private val NEW_STATE = IdentifierAST("newState", MapType(StringType, BoolType))

private val PRINT_UPDATE_STATE = IfStatementAST(
    BinaryExpressionAST(
        UnaryExpressionAST(
            BinaryExpressionAST(ID, MapMembershipOperator, NEW_STATE),
            NotOperator
        ),
        DisjunctionOperator,
        UnaryExpressionAST(MapIndexAST(NEW_STATE, ID), NotOperator)
    ), SequenceAST(
        listOf(
            PrintAST(listOf(ID, StringLiteralAST("\\n"))),
            AssignmentAST(
                NEW_STATE,
                MapIndexAssignAST(NEW_STATE, ID, BooleanLiteralAST(true))
            )
        )
    )
)

val ADVANCED_SAFE_DIV_INT = MethodAST(
    "advancedSafeDivInt",
    params = listOf(INT_PARAM_1, INT_PARAM_2, STATE, ID),
    returns = listOf(INT_RETURNS, NEW_STATE),
    body = SequenceAST(
        listOf(
            AssignmentAST(NEW_STATE, STATE),
            IfStatementAST(
                BinaryExpressionAST(INT_PARAM_2, EqualsOperator, IntegerLiteralAST(0)),
                SequenceAST(
                    listOf(
                        PRINT_UPDATE_STATE,
                        AssignmentAST(INT_RETURNS, INT_PARAM_1)
                    )
                ),
                SequenceAST(
                    listOf(
                        AssignmentAST(
                            INT_RETURNS,
                            BinaryExpressionAST(INT_PARAM_1, DivisionOperator, INT_PARAM_2)
                        )
                    )
                )
            )
        )
    )
)

val ADVANCED_SAFE_MODULO_INT = MethodAST(
    "advancedSafeModInt",
    params = listOf(INT_PARAM_1, INT_PARAM_2, STATE, ID),
    returns = listOf(INT_RETURNS, NEW_STATE),
    body = SequenceAST(
        listOf(
            AssignmentAST(NEW_STATE, STATE),
            IfStatementAST(
                BinaryExpressionAST(INT_PARAM_2, EqualsOperator, IntegerLiteralAST(0)),
                SequenceAST(
                    listOf(
                        PRINT_UPDATE_STATE,
                        AssignmentAST(INT_RETURNS, INT_PARAM_1)
                    )
                ),
                SequenceAST(
                    listOf(
                        AssignmentAST(
                            INT_RETURNS,
                            BinaryExpressionAST(INT_PARAM_1, ModuloOperator, INT_PARAM_2)
                        )
                    )
                )
            )
        )
    )
)

val ADVANCED_ABSOLUTE = MethodAST(
    "advancedAbsolute",
    params = listOf(INT_PARAM_1, STATE, ID),
    returns = listOf(INT_RETURNS, NEW_STATE),
    body = SequenceAST(
        listOf(
            AssignmentAST(NEW_STATE, STATE),
            IfStatementAST(
                BinaryExpressionAST(INT_PARAM_1, LessThanOperator, IntegerLiteralAST(0)),
                SequenceAST(
                    listOf(
                        PRINT_UPDATE_STATE,
                        AssignmentAST(
                            INT_RETURNS,
                            BinaryExpressionAST(IntegerLiteralAST(-1), MultiplicationOperator, INT_PARAM_1)
                        )
                    )
                ),
                SequenceAST(
                    listOf(
                        AssignmentAST(INT_RETURNS, INT_PARAM_1)
                    )
                )
            )
        )
    )
)

val ADVANCED_SAFE_ARRAY_INDEX = MethodAST(
    "advancedSafeArrayIndex",
    params = listOf(INT_PARAM_1, INT_PARAM_2, STATE, ID),
    returns = listOf(INT_RETURNS, NEW_STATE),
    body = SequenceAST(
        listOf(
            AssignmentAST(NEW_STATE, STATE),
            MultiDeclarationAST(
                listOf(IdentifierAST("b1", BoolType), IdentifierAST("b2", BoolType)), listOf(
                    BinaryExpressionAST(
                        INT_PARAM_1, LessThanOperator, IntegerLiteralAST(0)
                    ), BinaryExpressionAST(INT_PARAM_1, GreaterThanEqualOperator, INT_PARAM_2)
                )
            ),
            IfStatementAST(
                BinaryExpressionAST(IdentifierAST("b1", BoolType), DisjunctionOperator, IdentifierAST("b2", BoolType)),
                SequenceAST(
                    listOf(
                        PRINT_UPDATE_STATE,
                        AssignmentAST(
                            INT_RETURNS, TernaryExpressionAST(
                                IdentifierAST("b1", BoolType), IntegerLiteralAST(0), BinaryExpressionAST(
                                    INT_PARAM_1, ModuloOperator, INT_PARAM_2
                                )
                            )
                        )
                    )
                ),
                SequenceAST(
                    listOf(AssignmentAST(INT_RETURNS, INT_PARAM_1))
                )
            )
        )
    )
)

fun main() {
    println(ADVANCED_SAFE_ARRAY_INDEX)
}
