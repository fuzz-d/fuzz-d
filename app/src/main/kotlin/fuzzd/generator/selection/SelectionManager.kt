package fuzzd.generator.selection

import fuzzd.generator.ast.Type
import fuzzd.generator.ast.Type.BoolType
import fuzzd.generator.ast.Type.CharType
import fuzzd.generator.ast.Type.ClassType
import fuzzd.generator.ast.Type.ConstructorType.ArrayType
import fuzzd.generator.ast.Type.DatatypeType
import fuzzd.generator.ast.Type.IntType
import fuzzd.generator.ast.Type.LiteralType
import fuzzd.generator.ast.Type.MapType
import fuzzd.generator.ast.Type.MultisetType
import fuzzd.generator.ast.Type.SequenceType
import fuzzd.generator.ast.Type.SetType
import fuzzd.generator.ast.Type.StringType
import fuzzd.generator.ast.Type.TopLevelDatatypeType
import fuzzd.generator.ast.Type.TraitType
import fuzzd.generator.ast.error.InvalidInputException
import fuzzd.generator.ast.operators.BinaryOperator
import fuzzd.generator.ast.operators.BinaryOperator.BooleanBinaryOperator
import fuzzd.generator.ast.operators.BinaryOperator.ComparisonBinaryOperator
import fuzzd.generator.ast.operators.BinaryOperator.DataStructureBinaryOperator
import fuzzd.generator.ast.operators.BinaryOperator.DataStructureMembershipOperator
import fuzzd.generator.ast.operators.BinaryOperator.UnionOperator
import fuzzd.generator.ast.operators.UnaryOperator
import fuzzd.generator.ast.operators.UnaryOperator.Companion.isUnaryType
import fuzzd.generator.ast.operators.UnaryOperator.NegationOperator
import fuzzd.generator.ast.operators.UnaryOperator.NotOperator
import fuzzd.generator.context.GenerationContext
import fuzzd.generator.selection.AssignType.ARRAY_INDEX
import fuzzd.generator.selection.ExpressionType.BINARY
import fuzzd.generator.selection.ExpressionType.CONSTRUCTOR
import fuzzd.generator.selection.ExpressionType.FUNCTION_METHOD_CALL
import fuzzd.generator.selection.ExpressionType.IDENTIFIER
import fuzzd.generator.selection.ExpressionType.INDEX
import fuzzd.generator.selection.ExpressionType.INDEX_ASSIGN
import fuzzd.generator.selection.ExpressionType.LITERAL
import fuzzd.generator.selection.ExpressionType.MATCH
import fuzzd.generator.selection.ExpressionType.MODULUS
import fuzzd.generator.selection.ExpressionType.MULTISET_CONVERSION
import fuzzd.generator.selection.ExpressionType.TERNARY
import fuzzd.generator.selection.ExpressionType.UNARY
import fuzzd.generator.selection.IndexType.ARRAY
import fuzzd.generator.selection.IndexType.DATATYPE
import fuzzd.generator.selection.IndexType.MAP
import fuzzd.generator.selection.IndexType.MULTISET
import fuzzd.generator.selection.IndexType.SEQUENCE
import fuzzd.generator.selection.IndexType.STRING
import fuzzd.generator.selection.StatementType.ASSIGN
import fuzzd.generator.selection.StatementType.CLASS_INSTANTIATION
import fuzzd.generator.selection.StatementType.IF
import fuzzd.generator.selection.StatementType.MAP_ASSIGN
import fuzzd.generator.selection.StatementType.METHOD_CALL
import fuzzd.generator.selection.StatementType.WHILE
import fuzzd.generator.selection.probability_manager.BaseProbabilityManager
import fuzzd.generator.selection.probability_manager.ProbabilityManager
import fuzzd.utils.unionAll
import kotlin.math.min
import kotlin.random.Random

class SelectionManager(
    private val random: Random,
    private val probabilityManager: ProbabilityManager = BaseProbabilityManager(),
) {
    fun selectType(context: GenerationContext, literalOnly: Boolean = false): Type {
        val classTypeProb = if (!literalOnly && context.onDemandIdentifiers && context.functionSymbolTable.hasClasses()) probabilityManager.classType() else 0.0
        val traitTypeProb = if (!literalOnly && context.onDemandIdentifiers && context.functionSymbolTable.hasTraits()) probabilityManager.traitType() else 0.0
        val datatypeProb = if (context.functionSymbolTable.hasAvailableDatatypes(context.onDemandIdentifiers)) probabilityManager.datatype() else 0.0

        val selection = listOf<Pair<(GenerationContext, Boolean) -> Type, Double>>(
            this::selectClassType to classTypeProb,
            this::selectTraitType to traitTypeProb,
            this::selectDatatypeType to datatypeProb,
            this::selectArrayType to if (!literalOnly && context.onDemandIdentifiers) probabilityManager.arrayType() / context.expressionDepth else 0.0,
            this::selectDataStructureType to probabilityManager.datatstructureType(),
            this::selectLiteralType to probabilityManager.literalType(),
        )

        return randomWeightedSelection(normaliseWeights(selection)).invoke(context, literalOnly)
    }

    private fun selectClassType(context: GenerationContext, literalOnly: Boolean): ClassType =
        ClassType(randomSelection(context.functionSymbolTable.classes().toList()))

    private fun selectTraitType(context: GenerationContext, literalOnly: Boolean): TraitType =
        TraitType(randomSelection(context.functionSymbolTable.traits().toList()))

    fun selectDatatypeType(context: GenerationContext, literalOnly: Boolean): DatatypeType =
        randomSelection(context.functionSymbolTable.availableDatatypes(context.onDemandIdentifiers))

    fun selectDataStructureType(context: GenerationContext, literalOnly: Boolean): Type =
        randomWeightedSelection(
            normaliseWeights(
                listOf<Pair<(GenerationContext, Boolean) -> Type, Double>>(
                    this::selectSetType to probabilityManager.setType(),
                    this::selectMultisetType to probabilityManager.multisetType(),
                    this::selectMapType to probabilityManager.mapType(),
                    this::selectSequenceType to probabilityManager.sequenceType(),
                    this::selectStringType to probabilityManager.stringType(),
                ),
            ),
        ).invoke(context, literalOnly)

    private fun selectStringType(context: GenerationContext, literalOnly: Boolean): StringType = StringType

    private fun selectSetType(context: GenerationContext, literalOnly: Boolean): SetType =
        SetType(selectType(context, literalOnly))

    private fun selectMultisetType(context: GenerationContext, literalOnly: Boolean): MultisetType =
        MultisetType(selectType(context, literalOnly))

    private fun selectMapType(context: GenerationContext, literalOnly: Boolean): MapType =
        MapType(selectType(context, literalOnly), selectType(context, literalOnly))

    private fun selectSequenceType(context: GenerationContext, literalOnly: Boolean): SequenceType =
        SequenceType(selectType(context, literalOnly))

    private fun selectArrayType(context: GenerationContext, literalOnly: Boolean): ArrayType =
        selectArrayTypeWithDepth(context, 1)

    private fun selectArrayTypeWithDepth(context: GenerationContext, depth: Int): ArrayType {
        // TODO: Multi dimensional arrays
        val innerType =
            if (withProbability(0.0 / depth)) {
                selectArrayTypeWithDepth(context, depth + 1)
            } else {
                selectType(context, false)
            }
        return ArrayType(innerType)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun selectLiteralType(context: GenerationContext, literalOnly: Boolean): LiteralType =
        randomWeightedSelection(
            normaliseWeights(
                listOf(
                    IntType to probabilityManager.intType(),
                    BoolType to probabilityManager.boolType(),
                    CharType to probabilityManager.charType(),
                ),
            ),
        )

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
                        val dataStructureType = selectDataStructureType(context, false)
                        val keyType = when (dataStructureType) {
                            is SetType -> dataStructureType.innerType
                            is MultisetType -> dataStructureType.innerType
                            is SequenceType -> dataStructureType.innerType
                            is MapType -> dataStructureType.keyType
                            else -> throw UnsupportedOperationException()
                        }

                        Pair(selectedSubclass, Pair(keyType, dataStructureType))
                    }

                    is DataStructureBinaryOperator -> {
                        val dataStructureType = selectDataStructureType(context, false)
                        Pair(selectedSubclass, Pair(dataStructureType, dataStructureType))
                    }

                    else -> throw UnsupportedOperationException("$selectedSubclass not a valid boolean-type binary operator")
                }
            }

            is SetType, is MultisetType -> {
                val subclassInstances = BinaryOperator.DataStructureMathematicalOperator::class.sealedSubclasses
                    .mapNotNull { it.objectInstance }
                val selectedIndex = random.nextInt(subclassInstances.size)
                Pair(subclassInstances[selectedIndex], Pair(targetType, targetType))
            }

            is MapType, is SequenceType -> Pair(UnionOperator, Pair(targetType, targetType))

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
            IntType -> NegationOperator
            else -> throw InvalidInputException("Target type $targetType not supported for unary operations")
        }

    fun selectStatementType(context: GenerationContext, methodCalls: Boolean = true): StatementType {
        val ifStatementProbability = if (context.statementDepth < MAX_STATEMENT_DEPTH) min(probabilityManager.ifStatement() / context.statementDepth, 0.3) else 0.0
        val matchProbability = if (context.statementDepth < MAX_STATEMENT_DEPTH) min(probabilityManager.matchStatement() / context.statementDepth, 0.3) else 0.0
        val whileStatementProbability = if (context.statementDepth == 1) probabilityManager.whileStatement() else 0.0

        val methodCallProbability = if (methodCalls) {
            if (context.methodContext == null) probabilityManager.methodCall() else 0.05
        } else {
            0.0 // TODO() is there a better heuristic?
        }

        val selection = listOf(
            IF to ifStatementProbability,
            WHILE to whileStatementProbability,
            METHOD_CALL to methodCallProbability,
            StatementType.MATCH to matchProbability,
            MAP_ASSIGN to probabilityManager.mapAssign(),
            ASSIGN to probabilityManager.assignStatement(),
            CLASS_INSTANTIATION to probabilityManager.classInstantiation(),
        )

        return randomWeightedSelection(normaliseWeights(selection))
    }

    fun selectAssignType(context: GenerationContext): AssignType =
        randomWeightedSelection(
            listOf(
                AssignType.IDENTIFIER to probabilityManager.assignIdentifier(),
                ARRAY_INDEX to probabilityManager.assignArrayIndex(),
            ),
        )

    private fun isBinaryType(targetType: Type): Boolean =
        targetType !is ArrayType && targetType !is ClassType && targetType !is TraitType &&
            targetType !is TopLevelDatatypeType && targetType != CharType

    private fun isAssignType(targetType: Type): Boolean =
        targetType is MapType || targetType is MultisetType || targetType is SequenceType ||
            (targetType is DatatypeType && targetType.constructor.fields.isNotEmpty())

    fun selectExpressionType(targetType: Type, context: GenerationContext, identifier: Boolean = true): ExpressionType {
        val binaryProbability =
            if (isBinaryType(targetType)) probabilityManager.binaryExpression() / context.expressionDepth else 0.0
        val unaryProbability = if (isUnaryType(targetType)) probabilityManager.unaryExpression() / context.expressionDepth else 0.0
        val modulusProbability = if (targetType == IntType) probabilityManager.modulusExpression() else 0.0
        val multisetConversionProbability = if (targetType is MultisetType) probabilityManager.multisetConversion() else 0.0
        val functionCallProbability =
            if (!targetType.hasHeapType() && context.onDemandIdentifiers && context.functionCalls) {
                probabilityManager.functionCall() / context.expressionDepth
            } else {
                0.0
            }
        val ternaryProbability = probabilityManager.ternary() / context.expressionDepth
        val matchProbability = if (context.expressionDepth == 1 && context.functionSymbolTable.hasAvailableDatatypes(context.onDemandIdentifiers)) {
            probabilityManager.matchExpression()
        } else {
            0.0
        }
        val assignProbability = if (isAssignType(targetType) && context.symbolTable.hasType(targetType)) {
            probabilityManager.assignExpression() / context.expressionDepth
        } else {
            0.0
        }
        val indexProbability = if (identifier) probabilityManager.indexExpression() / context.expressionDepth else 0.0

        val identifierProbability = if (identifier) probabilityManager.identifier() else 0.0
        val literalProbability =
            if (isLiteralType(targetType)) {
                if (identifier) probabilityManager.literal() / 3 else probabilityManager.literal()
            } else {
                0.0
            }
        val constructorProbability =
            if (((targetType is ArrayType || targetType is ClassType || targetType is TraitType) && context.expressionDepth == 1) ||
                (targetType !is LiteralType && targetType !is ArrayType && targetType !is ClassType && targetType !is TraitType)
            ) {
                if (identifier) probabilityManager.constructor() / 3 else probabilityManager.constructor()
            } else {
                0.0
            }

        val selection = listOf(
            CONSTRUCTOR to constructorProbability,
            LITERAL to literalProbability,
            TERNARY to ternaryProbability,
            MATCH to matchProbability,
            IDENTIFIER to identifierProbability,
            MULTISET_CONVERSION to multisetConversionProbability,
            FUNCTION_METHOD_CALL to functionCallProbability,
            UNARY to unaryProbability,
            BINARY to binaryProbability,
            MODULUS to modulusProbability,
            INDEX to indexProbability,
            INDEX_ASSIGN to assignProbability,
        )

        return randomWeightedSelection(normaliseWeights(selection))
    }

    fun selectIndexType(context: GenerationContext, targetType: Type): IndexType = randomWeightedSelection(
        normaliseWeights(
            listOf(
                ARRAY to if (context.onDemandIdentifiers) probabilityManager.arrayIndexType() else 0.0,
                MAP to probabilityManager.mapIndexType(),
                MULTISET to if (targetType == IntType) probabilityManager.multisetIndexType() else 0.0,
                SEQUENCE to probabilityManager.sequenceIndexType(),
                STRING to if (targetType == CharType) probabilityManager.stringIndexType() else 0.0,
                DATATYPE to probabilityManager.datatypeIndexType(),
            ),
        ),
    )

    fun <T> normaliseWeights(items: List<Pair<T, Double>>): List<Pair<T, Double>> {
        var weightSum = 0.0

        items.forEach { item -> weightSum += item.second }
        return items.mapNotNull { item -> if (item.second == 0.0) null else Pair(item.first, item.second / weightSum) }
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

    fun selectNumberOfConstructorFields(context: GenerationContext) =
        randomWeightedSelection(
            normaliseWeights(
                listOf(
                    1 to 0.4,
                    2 to 0.2,
                    3 to 0.2 / context.expressionDepth,
                    4 to 0.1 / context.expressionDepth,
                    5 to 0.1 / context.expressionDepth,
                ),
            ),
        )

    fun selectMakeDatatypeInductive() = withProbability(0.4)

    fun selectNumberOfDatatypeConstructors() =
        randomWeightedSelection(normaliseWeights(listOf(1 to 0.25, 2 to 0.4, 3 to 0.25, 4 to 0.1)))

    fun selectNumberOfDatatypeFields() =
        randomWeightedSelection(normaliseWeights(listOf(0 to 0.1, 1 to 0.2, 2 to 0.2, 3 to 0.2, 4 to 0.1, 5 to 0.1)))

    fun selectNumberOfFunctionMethods() = random.nextInt(0, MAX_FUNCTION_METHODS)

    fun selectNumberOfMethods() = random.nextInt(0, MAX_METHODS)

    fun selectNumberOfTraits() = random.nextInt(0, MAX_TRAITS)

    fun selectNumberOfTraitInherits() = random.nextInt(0, MAX_TRAIT_INHERITS)

    fun selectDecimalLiteral(): Int = random.nextInt(0, MAX_INT_VALUE)

    fun selectStringLength(): Int = random.nextInt(1, MAX_STRING_LENGTH)

    fun selectCharacter(): Char = random.nextInt('a'.code, 'z'.code).toChar()

    fun selectBoolean(): Boolean = random.nextBoolean()

    fun selectInt(min: Int, max: Int): Int = if (min == max) min else random.nextInt(min, max)

    companion object {
        private const val MAX_STATEMENT_DEPTH = 5
        private const val MIN_ARRAY_LENGTH = 1
        private const val MAX_ARRAY_LENGTH = 30
        private const val MAX_INT_VALUE = 1000
        private const val MAX_STRING_LENGTH = 10
        private const val MAX_PARAMETERS = 15
        private const val MAX_RETURNS = 15
        private const val MAX_FIELDS = 5
        private const val MAX_GLOBAL_FIELDS = 15
        private const val MAX_FUNCTION_METHODS = 10
        private const val MAX_METHODS = 3
        private const val MAX_TRAITS = 3
        private const val MAX_TRAIT_INHERITS = 2

        private val LITERAL_TYPES = listOf(IntType, BoolType, CharType)
        private fun isLiteralType(type: Type) = type in LITERAL_TYPES
    }
}
