package fuzzd.generator.selection

import fuzzd.generator.ast.Type
import fuzzd.generator.ast.Type.BoolType
import fuzzd.generator.ast.Type.ClassType
import fuzzd.generator.ast.Type.ConstructorType.ArrayType
import fuzzd.generator.ast.Type.IntType
import fuzzd.generator.ast.Type.LiteralType
import fuzzd.generator.ast.Type.MapType
import fuzzd.generator.ast.Type.RealType
import fuzzd.generator.ast.Type.SetType
import fuzzd.generator.ast.Type.TraitType
import fuzzd.generator.ast.error.InvalidInputException
import fuzzd.generator.ast.operators.BinaryOperator
import fuzzd.generator.ast.operators.BinaryOperator.BooleanBinaryOperator
import fuzzd.generator.ast.operators.BinaryOperator.Companion.isBinaryType
import fuzzd.generator.ast.operators.BinaryOperator.ComparisonBinaryOperator
import fuzzd.generator.ast.operators.BinaryOperator.DataStructureBinaryOperator
import fuzzd.generator.ast.operators.BinaryOperator.DataStructureMembershipOperator
import fuzzd.generator.ast.operators.BinaryOperator.UnionOperator
import fuzzd.generator.ast.operators.UnaryOperator
import fuzzd.generator.ast.operators.UnaryOperator.Companion.isUnaryType
import fuzzd.generator.ast.operators.UnaryOperator.NegationOperator
import fuzzd.generator.ast.operators.UnaryOperator.NotOperator
import fuzzd.generator.context.GenerationContext
import fuzzd.generator.selection.ExpressionType.BINARY
import fuzzd.generator.selection.ExpressionType.CONSTRUCTOR
import fuzzd.generator.selection.ExpressionType.FUNCTION_METHOD_CALL
import fuzzd.generator.selection.ExpressionType.IDENTIFIER
import fuzzd.generator.selection.ExpressionType.LITERAL
import fuzzd.generator.selection.ExpressionType.TERNARY
import fuzzd.generator.selection.ExpressionType.UNARY
import fuzzd.generator.selection.StatementType.ASSIGN
import fuzzd.generator.selection.StatementType.CLASS_INSTANTIATION
import fuzzd.generator.selection.StatementType.DECLARATION
import fuzzd.generator.selection.StatementType.IF
import fuzzd.generator.selection.StatementType.METHOD_CALL
import fuzzd.generator.selection.StatementType.WHILE
import fuzzd.utils.unionAll
import kotlin.random.Random

class SelectionManager(
    private val random: Random,
) {
    fun selectType(context: GenerationContext, literalOnly: Boolean = false): Type {
        val selection = listOf<Pair<(GenerationContext) -> Type, Double>>(
//            this::selectClassType to 0.0,
//            this::selectTraitType to 0.0,
            this::selectArrayType to if (literalOnly) 0.0 else 0.1,
            this::selectDataStructureType to 0.2,
            this::selectLiteralType to if (literalOnly) 0.8 else 0.7,
        )

        return randomWeightedSelection(selection).invoke(context)
    }

    private fun selectClassType(context: GenerationContext): ClassType =
        ClassType(randomSelection(context.functionSymbolTable.classes().toList()))

    private fun selectTraitType(context: GenerationContext): TraitType =
        TraitType(randomSelection(context.functionSymbolTable.traits().toList()))

    private fun selectDataStructureType(context: GenerationContext): Type =
        randomWeightedSelection(
            listOf<Pair<(GenerationContext) -> Type, Double>>(
                this::selectSetType to 0.5,
                this::selectMapType to 0.5,
            ),
        ).invoke(context)

    private fun selectSetType(context: GenerationContext): SetType = SetType(selectType(context))

    private fun selectMapType(context: GenerationContext): MapType = MapType(selectType(context), selectType(context))

    private fun selectArrayType(context: GenerationContext): ArrayType = selectArrayTypeWithDepth(context, 1)

    private fun selectArrayTypeWithDepth(context: GenerationContext, depth: Int): ArrayType {
        // TODO: Multi dimensional arrays
        val innerType =
            if (withProbability(0.0 / depth)) {
                selectArrayTypeWithDepth(context, depth + 1)
            } else {
                selectLiteralType(context)
            }
        return ArrayType(innerType)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun selectLiteralType(context: GenerationContext): LiteralType =
        randomSelection(LITERAL_TYPES)

    fun selectMethodReturnType(context: GenerationContext): List<Type> {
        val numberOfReturns = random.nextInt(MAX_RETURNS)
        return (1..numberOfReturns).map { selectType(context, literalOnly = true) }
    }

    // selects operator, returning the operator and a selected input type for inner expressions
    fun selectBinaryOperator(context: GenerationContext, targetType: Type): Pair<BinaryOperator, Pair<Type, Type>> =
        when (targetType) {
            BoolType -> {
                val subclasses = BinaryOperator::class.sealedSubclasses
                val subclassInstances = subclasses.map { it.sealedSubclasses }.unionAll()
                    .mapNotNull { it.objectInstance }
                    .filter { it.outputType(targetType, targetType) == BoolType }
                val selectedIndex = random.nextInt(subclassInstances.size)

                when (val selectedSubclass = subclassInstances[selectedIndex]) {
                    is BooleanBinaryOperator -> Pair(selectedSubclass, Pair(BoolType, BoolType))
                    is ComparisonBinaryOperator -> Pair(selectedSubclass, Pair(IntType, IntType))
                    is DataStructureMembershipOperator -> {
                        val dataStructureType = selectDataStructureType(context)
                        Pair(
                            selectedSubclass,
                            if (dataStructureType is SetType) {
                                Pair(
                                    dataStructureType.innerType,
                                    dataStructureType,
                                )
                            } else {
                                Pair((dataStructureType as MapType).keyType, dataStructureType)
                            },
                        )
                    }

                    is DataStructureBinaryOperator -> {
                        val dataStructureType = selectDataStructureType(context)
                        Pair(selectedSubclass, Pair(dataStructureType, dataStructureType))
                    }

                    else -> throw UnsupportedOperationException()
                }
            }

            is SetType -> {
                val subclassInstances = BinaryOperator.DataStructureMathematicalOperator::class.sealedSubclasses
                    .mapNotNull { it.objectInstance }
                val selectedIndex = random.nextInt(subclassInstances.size)
                Pair(subclassInstances[selectedIndex], Pair(targetType, targetType))
            }

            is MapType -> Pair(UnionOperator, Pair(targetType, targetType))

            IntType -> {
                val subclassInstances = BinaryOperator.MathematicalBinaryOperator::class.sealedSubclasses
                    .mapNotNull { it.objectInstance }
                    .filter { it.supportsInput(targetType, targetType) }
                val selectedIndex = random.nextInt(subclassInstances.size)
                Pair(subclassInstances[selectedIndex], Pair(targetType, targetType))
            }

            else -> throw UnsupportedOperationException("Target type $targetType not supported for binary operations")
        }

    fun selectUnaryOperator(targetType: Type): UnaryOperator =
        when (targetType) {
            BoolType -> NotOperator
            IntType, RealType -> NegationOperator
            else -> throw InvalidInputException("Target type $targetType not supported for unary operations")
        }

    fun selectStatementType(context: GenerationContext, methodCalls: Boolean = true): StatementType {
        val ifStatementProbability =
            if (context.statementDepth < MAX_STATEMENT_DEPTH) 0.1 / context.statementDepth else 0.0
        val whileStatementProbability = ifStatementProbability

        val methodCallProbability = if (methodCalls) {
            if (context.methodContext == null) 0.2 else 0.05
        } else {
            0.0 // TODO() is there a better heuristic?
        }

        val remainingProbability = 1 - methodCallProbability - whileStatementProbability - ifStatementProbability

        val selection = listOf(
            IF to ifStatementProbability,
            WHILE to whileStatementProbability,
            METHOD_CALL to methodCallProbability,
            DECLARATION to 3 * remainingProbability / 6,
            ASSIGN to 2 * remainingProbability / 6,
            CLASS_INSTANTIATION to remainingProbability / 6,
        )

        return randomWeightedSelection(selection)
    }

    fun generateNewMethod(context: GenerationContext): Boolean {
        val trueWeighting =
            0.2 / maxOf(context.statementDepth, context.expressionDepth, context.functionSymbolTable.methods().size)

        return context.methodContext == null &&
            randomWeightedSelection(listOf(true to trueWeighting, false to 1 - trueWeighting))
    }

    fun generateNewClass(context: GenerationContext): Boolean =
        random.nextFloat() < (0.1 / context.functionSymbolTable.classes().size)

    fun selectExpressionType(targetType: Type, context: GenerationContext, identifier: Boolean = true): ExpressionType {
        val binaryProbability = if (isBinaryType(targetType)) 0.4 / context.expressionDepth else 0.0
        val unaryProbability = if (isUnaryType(targetType)) 0.15 / context.expressionDepth else 0.0
        val functionMethodCallProbability = if (targetType !is ArrayType) 0.15 / context.expressionDepth else 0.0
        val ternaryProbability = 0.10 / context.expressionDepth

        val remainingProbability =
            (1 - binaryProbability - unaryProbability - functionMethodCallProbability - ternaryProbability)
        val identifierProbability = if (identifier) 2 * remainingProbability / 3 else 0.0
        val literalProbability =
            if (isLiteralType(targetType)) {
                if (identifier) remainingProbability / 3 else remainingProbability
            } else {
                0.0
            }
        val constructorProbability = if (!isLiteralType(targetType) && context.expressionDepth == 1) {
            if (identifier) remainingProbability / 3 else remainingProbability
        } else {
            0.0
        }

        val selection = listOf(
            CONSTRUCTOR to constructorProbability,
            LITERAL to literalProbability,
            TERNARY to ternaryProbability,
            IDENTIFIER to identifierProbability,
            FUNCTION_METHOD_CALL to functionMethodCallProbability,
            UNARY to unaryProbability,
            BINARY to binaryProbability,
        )

        return randomWeightedSelection(normaliseWeights(selection))
    }

    fun <T> normaliseWeights(items: List<Pair<T, Double>>): List<Pair<T, Double>> {
        var weightSum = 0.0

        items.forEach { item -> weightSum += item.second }
        return items.map { item -> Pair(item.first, item.second / weightSum) }
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

    fun withProbability(probability: Double): Boolean = random.nextFloat() < probability

    fun selectNumberOfParameters(): Int = random.nextInt(0, MAX_PARAMETERS)

    fun selectArrayLength(): Int = random.nextInt(MIN_ARRAY_LENGTH, MAX_ARRAY_LENGTH)

    fun selectSequenceLength(maxLength: Int) = random.nextInt(1, maxLength)

    fun selectNumberOfFields() = random.nextInt(0, MAX_FIELDS)

    fun selectNumberOfGlobalFields() = random.nextInt(0, MAX_GLOBAL_FIELDS)

    fun selectNumberOfFunctionMethods() = random.nextInt(0, MAX_FUNCTION_METHODS)

    fun selectNumberOfMethods() = random.nextInt(0, MAX_METHODS)

    fun selectNumberOfTraits() = random.nextInt(0, MAX_TRAITS)

    fun selectNumberOfTraitInherits() = random.nextInt(0, MAX_TRAIT_INHERITS)

    fun selectDecimalLiteral(): Int = random.nextInt(0, MAX_INT_VALUE)

    fun selectCharacter(): Char = random.nextInt(20, MAX_CHAR_VALUE).toChar()

    fun selectBoolean(): Boolean = random.nextBoolean()

    companion object {
        private const val MAX_STATEMENT_DEPTH = 5
        private const val MIN_ARRAY_LENGTH = 1
        private const val MAX_ARRAY_LENGTH = 30
        private const val MAX_INT_VALUE = 1000
        private const val MAX_CHAR_VALUE = 127
        private const val MAX_PARAMETERS = 15
        private const val MAX_RETURNS = 15
        private const val MAX_FIELDS = 5
        private const val MAX_GLOBAL_FIELDS = 15
        private const val MAX_FUNCTION_METHODS = 10
        private const val MAX_METHODS = 3
        private const val MAX_TRAITS = 3
        private const val MAX_TRAIT_INHERITS = 2

        private val LITERAL_TYPES = listOf(IntType, BoolType)
        private fun isLiteralType(type: Type) = type in LITERAL_TYPES
    }
}
