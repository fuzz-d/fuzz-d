package fuzzd.generator.selection

import fuzzd.generator.GenerationContext
import fuzzd.generator.ast.Type
import fuzzd.generator.ast.Type.BoolType
import fuzzd.generator.ast.Type.CharType
import fuzzd.generator.ast.Type.IntType
import fuzzd.generator.ast.Type.RealType
import fuzzd.generator.ast.error.InvalidInputException
import fuzzd.generator.ast.operators.BinaryOperator
import fuzzd.generator.ast.operators.UnaryOperator
import fuzzd.generator.ast.operators.UnaryOperator.NegationOperator
import fuzzd.generator.ast.operators.UnaryOperator.NotOperator
import fuzzd.generator.selection.ExpressionType.BINARY
import fuzzd.generator.selection.ExpressionType.IDENTIFIER
import fuzzd.generator.selection.ExpressionType.LITERAL
import fuzzd.generator.selection.ExpressionType.UNARY
import fuzzd.generator.selection.StatementType.DECLARATION
import fuzzd.generator.selection.StatementType.IF
import fuzzd.generator.selection.StatementType.PRINT
import kotlin.random.Random

class SelectionManager(
    private val random: Random
) {
    fun selectType(): Type {
        val selection = listOf(RealType to 0.0, IntType to 0.54, BoolType to 0.44, CharType to 0.02)
        return randomWeightedSelection(selection)
    }

    // selects operator, returning the operator and a selected input type for inner expressions
    fun selectBinaryOperator(targetType: Type): Pair<BinaryOperator, Type> =
        when (targetType) {
            BoolType ->
                if (random.nextBoolean()) {
                    val subclasses = BinaryOperator.BooleanBinaryOperator::class.sealedSubclasses
                    val selectedIndex = random.nextInt(subclasses.size)
                    Pair(subclasses[selectedIndex].objectInstance!!, BoolType)
                } else {
                    val subclasses = BinaryOperator.ComparisonBinaryOperator::class.sealedSubclasses
                    val subclass = subclasses[random.nextInt(subclasses.size)].objectInstance!!
                    val supportedInputTypes = subclass.supportedInputTypes()
                    val inputType = supportedInputTypes[random.nextInt(supportedInputTypes.size)]

                    Pair(subclass, inputType)
                }

            IntType, RealType, CharType -> {
                val subclassInstances = BinaryOperator.MathematicalBinaryOperator::class.sealedSubclasses
                    .mapNotNull { it.objectInstance }
                    .filter { targetType in it.supportedInputTypes() }
                val selectedIndex = random.nextInt(subclassInstances.size)
                Pair(subclassInstances[selectedIndex], targetType)
            }
        }

    fun selectUnaryOperator(targetType: Type): UnaryOperator =
        when (targetType) {
            BoolType -> NotOperator
            IntType, RealType -> NegationOperator
            else -> throw InvalidInputException("Target type $targetType not supported for unary operations")
        }

    fun selectStatementType(context: GenerationContext): StatementType {
        val ifStatementProbability =
            if (context.statementDepth < MAX_STATEMENT_DEPTH) 0.1 / context.statementDepth else 0.0
        val remainingProbability = 1 - ifStatementProbability
        val selection = listOf(
            IF to ifStatementProbability,
            PRINT to remainingProbability / 2,
            DECLARATION to remainingProbability / 2
        )
        return randomWeightedSelection(selection)
    }

    fun selectExpressionType(targetType: Type, context: GenerationContext): ExpressionType {
        val binaryProbability = if (targetType != CharType) 0.4 / context.expressionDepth else 0.0
        val unaryProbability = if (targetType != CharType) 0.2 / context.expressionDepth else 0.0
        val remainingProbability = (1 - binaryProbability - unaryProbability) / 2

        val selection = listOf(
            LITERAL to remainingProbability,
            IDENTIFIER to remainingProbability,
            UNARY to unaryProbability,
            BINARY to binaryProbability
        )

        return randomWeightedSelection(selection)
    }

    fun <T> randomSelection(items: List<T>): T {
        val randomIndex = random.nextInt(items.size)
        return items[randomIndex]
    }

    fun <T> randomWeightedSelection(items: List<Pair<T, Double>>): T {
        val probability = random.nextFloat()
        var wsum = 0.0

        for ((item, w) in items) {
            wsum += w

            if (probability < wsum) return item
        }

        return items[0].first // default
    }

    companion object {
        private const val MAX_STATEMENT_DEPTH = 5
    }
}
