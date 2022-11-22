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
import fuzzd.generator.selection.StatementType.DECLARATION
import fuzzd.generator.selection.StatementType.PRINT
import kotlin.random.Random

class SelectionManager(
    private val random: Random
) {
    fun selectType(): Type {
        val selection = listOf(RealType to 0.33, IntType to 0.33, BoolType to 0.33, CharType to 0.01)
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

            IntType, RealType -> {
                val subclasses = BinaryOperator.MathematicalBinaryOperator::class.sealedSubclasses
                val selectedIndex = random.nextInt(subclasses.size)
                Pair(subclasses[selectedIndex].objectInstance!!, targetType)
            }

            CharType -> {
                val selection =
                    listOf(BinaryOperator.AdditionOperator to 0.5, BinaryOperator.SubtractionOperator to 0.5)
                Pair(randomWeightedSelection(selection), targetType)
            }
        }

    fun selectUnaryOperator(targetType: Type): UnaryOperator =
        when (targetType) {
            BoolType -> NotOperator
            IntType, RealType -> NegationOperator
            else -> throw InvalidInputException("Target type $targetType not supported for unary operations")
        }

    fun selectStatementType(context: GenerationContext): StatementType {
        val selection = listOf(PRINT to 0.5, DECLARATION to 0.5)
        return randomWeightedSelection(selection)
    }

    private fun <T> randomWeightedSelection(items: List<Pair<T, Double>>): T {
        val probability = random.nextFloat()
        var wsum = 0.0

        for ((item, w) in items) {
            wsum += w

            if (probability < wsum) return item
        }

        return items[0].first // default
    }
}
