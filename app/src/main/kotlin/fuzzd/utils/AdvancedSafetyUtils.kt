package fuzzd.utils

import fuzzd.generator.ast.ClassAST
import fuzzd.generator.ast.ExpressionAST.BinaryExpressionAST
import fuzzd.generator.ast.ExpressionAST.BooleanLiteralAST
import fuzzd.generator.ast.ExpressionAST.ClassInstanceFieldAST
import fuzzd.generator.ast.ExpressionAST.IdentifierAST
import fuzzd.generator.ast.ExpressionAST.IndexAssignAST
import fuzzd.generator.ast.ExpressionAST.IntegerLiteralAST
import fuzzd.generator.ast.ExpressionAST.MapIndexAST
import fuzzd.generator.ast.ExpressionAST.TernaryExpressionAST
import fuzzd.generator.ast.ExpressionAST.UnaryExpressionAST
import fuzzd.generator.ast.MethodAST
import fuzzd.generator.ast.SequenceAST
import fuzzd.generator.ast.StatementAST.AssignmentAST
import fuzzd.generator.ast.StatementAST.IfStatementAST
import fuzzd.generator.ast.StatementAST.MultiDeclarationAST
import fuzzd.generator.ast.StatementAST.PrintAST
import fuzzd.generator.ast.Type.BoolType
import fuzzd.generator.ast.Type.ClassType
import fuzzd.generator.ast.Type.IntType
import fuzzd.generator.ast.Type.MapType
import fuzzd.generator.ast.Type.StringType
import fuzzd.generator.ast.operators.BinaryOperator.DisjunctionOperator
import fuzzd.generator.ast.operators.BinaryOperator.DivisionOperator
import fuzzd.generator.ast.operators.BinaryOperator.EqualsOperator
import fuzzd.generator.ast.operators.BinaryOperator.GreaterThanEqualOperator
import fuzzd.generator.ast.operators.BinaryOperator.LessThanOperator
import fuzzd.generator.ast.operators.BinaryOperator.MembershipOperator
import fuzzd.generator.ast.operators.BinaryOperator.ModuloOperator
import fuzzd.generator.ast.operators.BinaryOperator.MultiplicationOperator
import fuzzd.generator.ast.operators.UnaryOperator.NotOperator

private val INT_PARAM_1 = IdentifierAST("p1", IntType)
private val INT_PARAM_2 = IdentifierAST("p2", IntType)
private val STATE = IdentifierAST("state", MapType(StringType, BoolType))
private val ID = IdentifierAST("id", StringType)
private val INT_RETURNS = IdentifierAST("r1", IntType)

val ADVANCED_RECONDITION_CLASS = ClassAST(
    "AdvancedReconditionState",
    emptySet(),
    emptySet(),
    emptySet(),
    setOf(STATE),
    emptySet(),
)
val ADVANCED_STATE = IdentifierAST("advancedState", ClassType(ADVANCED_RECONDITION_CLASS))
val ADVANCED_STATE_FIELD = ClassInstanceFieldAST(ADVANCED_STATE, STATE)

private val PRINT_UPDATE_STATE = IfStatementAST(
    BinaryExpressionAST(
        UnaryExpressionAST(
            BinaryExpressionAST(ID, MembershipOperator, ADVANCED_STATE_FIELD),
            NotOperator,
        ),
        DisjunctionOperator,
        UnaryExpressionAST(MapIndexAST(ADVANCED_STATE_FIELD, ID), NotOperator),
    ),
    SequenceAST(
        listOf(
            PrintAST(ID),
            AssignmentAST(
                ADVANCED_STATE_FIELD,
                IndexAssignAST(ADVANCED_STATE_FIELD, ID, BooleanLiteralAST(true)),
            ),
        ),
    ),
)

val ADVANCED_SAFE_DIV_INT = MethodAST(
    "advancedSafeDivInt",
    params = listOf(INT_PARAM_1, INT_PARAM_2, ADVANCED_STATE, ID),
    returns = listOf(INT_RETURNS),
    body = SequenceAST(
        listOf(
            IfStatementAST(
                BinaryExpressionAST(INT_PARAM_2, EqualsOperator, IntegerLiteralAST(0)),
                SequenceAST(
                    listOf(
                        PRINT_UPDATE_STATE,
                        AssignmentAST(INT_RETURNS, INT_PARAM_1),
                    ),
                ),
                SequenceAST(
                    listOf(
                        AssignmentAST(
                            INT_RETURNS,
                            BinaryExpressionAST(INT_PARAM_1, DivisionOperator, INT_PARAM_2),
                        ),
                    ),
                ),
            ),
        ),
    ),
)

val ADVANCED_SAFE_MODULO_INT = MethodAST(
    "advancedSafeModInt",
    params = listOf(INT_PARAM_1, INT_PARAM_2, ADVANCED_STATE, ID),
    returns = listOf(INT_RETURNS),
    body = SequenceAST(
        listOf(
            IfStatementAST(
                BinaryExpressionAST(INT_PARAM_2, EqualsOperator, IntegerLiteralAST(0)),
                SequenceAST(
                    listOf(
                        PRINT_UPDATE_STATE,
                        AssignmentAST(INT_RETURNS, INT_PARAM_1),
                    ),
                ),
                SequenceAST(
                    listOf(
                        AssignmentAST(
                            INT_RETURNS,
                            BinaryExpressionAST(INT_PARAM_1, ModuloOperator, INT_PARAM_2),
                        ),
                    ),
                ),
            ),
        ),
    ),
)

val ADVANCED_ABSOLUTE = MethodAST(
    "advancedAbsolute",
    params = listOf(INT_PARAM_1, ADVANCED_STATE, ID),
    returns = listOf(INT_RETURNS),
    body = SequenceAST(
        listOf(
            IfStatementAST(
                BinaryExpressionAST(INT_PARAM_1, LessThanOperator, IntegerLiteralAST(0)),
                SequenceAST(
                    listOf(
                        PRINT_UPDATE_STATE,
                        AssignmentAST(
                            INT_RETURNS,
                            BinaryExpressionAST(IntegerLiteralAST(-1), MultiplicationOperator, INT_PARAM_1),
                        ),
                    ),
                ),
                SequenceAST(
                    listOf(
                        AssignmentAST(INT_RETURNS, INT_PARAM_1),
                    ),
                ),
            ),
        ),
    ),
)

val ADVANCED_SAFE_ARRAY_INDEX = MethodAST(
    "advancedSafeArrayIndex",
    params = listOf(INT_PARAM_1, INT_PARAM_2, ADVANCED_STATE, ID),
    returns = listOf(INT_RETURNS),
    body = SequenceAST(
        listOf(
            MultiDeclarationAST(
                listOf(IdentifierAST("b1", BoolType), IdentifierAST("b2", BoolType)),
                listOf(
                    BinaryExpressionAST(
                        INT_PARAM_1,
                        LessThanOperator,
                        IntegerLiteralAST(0),
                    ),
                    BinaryExpressionAST(INT_PARAM_1, GreaterThanEqualOperator, INT_PARAM_2),
                ),
            ),
            IfStatementAST(
                BinaryExpressionAST(IdentifierAST("b1", BoolType), DisjunctionOperator, IdentifierAST("b2", BoolType)),
                SequenceAST(
                    listOf(
                        PRINT_UPDATE_STATE,
                        AssignmentAST(
                            INT_RETURNS,
                            TernaryExpressionAST(
                                IdentifierAST("b1", BoolType),
                                IntegerLiteralAST(0),
                                BinaryExpressionAST(
                                    INT_PARAM_1,
                                    ModuloOperator,
                                    INT_PARAM_2,
                                ),
                            ),
                        ),
                    ),
                ),
                SequenceAST(
                    listOf(AssignmentAST(INT_RETURNS, INT_PARAM_1)),
                ),
            ),
        ),
    ),
)

fun main() {
    println(ADVANCED_SAFE_ARRAY_INDEX)
}
