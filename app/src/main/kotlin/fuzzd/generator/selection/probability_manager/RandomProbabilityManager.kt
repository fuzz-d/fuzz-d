package fuzzd.generator.selection.probability_manager

import kotlin.math.min
import kotlin.random.Random
import kotlin.reflect.KFunction
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.typeOf

class RandomProbabilityManager(seed: Long, excludedFeatures: Set<KFunction<*>> = setOf()) : ProbabilityManager {
    private val random = Random(seed)
    private val probabilities = mutableMapOf<KFunction<*>, Double>()
    private val statementCounts = mutableMapOf<KFunction<*>, Int>()

    init {
        ProbabilityManager::class.declaredFunctions
            .filter { it.returnType == typeOf<Double>() }
            .forEach {
                probabilities[it] = random.nextDouble()
            }

        excludedFeatures.forEach { probabilities[it] = 0.0 }

        ProbabilityManager::class.declaredFunctions
            .filter { it.returnType == typeOf<Int>() }
            .forEach {
                statementCounts[it] = random.nextInt(1, 6)
            }
    }

    private fun getProbability(function: KFunction<*>): Double = probabilities[function] ?: 0.0

    private fun getStatementCount(function: KFunction<*>): Int = statementCounts[function] ?: 0

    override fun classType(): Double = getProbability(ProbabilityManager::classType)
    override fun traitType(): Double = getProbability(ProbabilityManager::traitType)
    override fun datatype(): Double = getProbability(ProbabilityManager::datatype)
    override fun arrayType(): Double = getProbability(ProbabilityManager::arrayType)
    override fun datatstructureType(): Double = getProbability(ProbabilityManager::datatstructureType)
    override fun literalType(): Double = getProbability(ProbabilityManager::literalType)
    override fun setType(): Double = getProbability(ProbabilityManager::setType)
    override fun multisetType(): Double = getProbability(ProbabilityManager::multisetType)
    override fun mapType(): Double = getProbability(ProbabilityManager::mapType)
    override fun sequenceType(): Double = getProbability(ProbabilityManager::sequenceType)
    override fun stringType(): Double = getProbability(ProbabilityManager::stringType)
    override fun intType(): Double = getProbability(ProbabilityManager::intType)
    override fun boolType(): Double = getProbability(ProbabilityManager::boolType)
    override fun charType(): Double = getProbability(ProbabilityManager::charType)
    override fun ifStatement(): Double = getProbability(ProbabilityManager::ifStatement)
    override fun matchStatement(): Double = getProbability(ProbabilityManager::matchStatement)
    override fun forallStatement(): Double = getProbability(ProbabilityManager::forallStatement)
    override fun forLoopStatement(): Double = getProbability(ProbabilityManager::forLoopStatement)
    override fun whileStatement(): Double = getProbability(ProbabilityManager::whileStatement)
    override fun methodCall(): Double = getProbability(ProbabilityManager::methodCall)
    override fun mapAssign(): Double = getProbability(ProbabilityManager::mapAssign)
    override fun assignStatement(): Double = getProbability(ProbabilityManager::assignStatement)
    override fun classInstantiation(): Double = getProbability(ProbabilityManager::classInstantiation)
    override fun assignIdentifier(): Double = getProbability(ProbabilityManager::assignIdentifier)
    override fun assignArrayIndex(): Double = getProbability(ProbabilityManager::assignArrayIndex)
    override fun binaryExpression(): Double = getProbability(ProbabilityManager::binaryExpression)
    override fun unaryExpression(): Double = getProbability(ProbabilityManager::unaryExpression)
    override fun modulusExpression(): Double = getProbability(ProbabilityManager::modulusExpression)
    override fun multisetConversion(): Double = getProbability(ProbabilityManager::multisetConversion)
    override fun functionCall(): Double = getProbability(ProbabilityManager::functionCall)
    override fun ternary(): Double = getProbability(ProbabilityManager::ternary)
    override fun matchExpression(): Double = getProbability(ProbabilityManager::matchExpression)
    override fun assignExpression(): Double = getProbability(ProbabilityManager::assignExpression)
    override fun indexExpression(): Double = getProbability(ProbabilityManager::indexExpression)
    override fun identifier(): Double = getProbability(ProbabilityManager::identifier)
    override fun literal(): Double = getProbability(ProbabilityManager::literal)
    override fun constructor(): Double = getProbability(ProbabilityManager::constructor)
    override fun comprehension(): Double = getProbability(ProbabilityManager::comprehension)

    override fun comprehensionConditionIntRange(): Double = getProbability(ProbabilityManager::comprehensionConditionIntRange)

    override fun arrayIndexType(): Double = getProbability(ProbabilityManager::arrayIndexType)
    override fun mapIndexType(): Double = getProbability(ProbabilityManager::mapIndexType)
    override fun multisetIndexType(): Double = getProbability(ProbabilityManager::multisetIndexType)
    override fun sequenceIndexType(): Double = getProbability(ProbabilityManager::sequenceIndexType)
    override fun stringIndexType(): Double = getProbability(ProbabilityManager::stringIndexType)
    override fun datatypeIndexType(): Double = getProbability(ProbabilityManager::datatypeIndexType)

    override fun arrayInitValues(): Double = getProbability(ProbabilityManager::arrayInitValues)
    override fun arrayInitComprehension(): Double = getProbability(ProbabilityManager::arrayInitComprehension)
    override fun arrayInitDefault(): Double = getProbability(ProbabilityManager::arrayInitDefault)

    override fun methodStatements(): Int = getStatementCount(ProbabilityManager::methodStatements)
    override fun ifBranchStatements(): Int = getStatementCount(ProbabilityManager::ifBranchStatements)
    override fun forLoopBodyStatements(): Int = getStatementCount(ProbabilityManager::forLoopBodyStatements)
    override fun whileBodyStatements(): Int = getStatementCount(ProbabilityManager::whileBodyStatements)
    override fun mainFunctionStatements(): Int = getStatementCount(ProbabilityManager::mainFunctionStatements) + 5 // adjust for smaller random #
    override fun matchStatements(): Int = getStatementCount(ProbabilityManager::matchStatements)
    override fun comprehensionIdentifiers(): Int = min(getStatementCount(ProbabilityManager::comprehensionIdentifiers), 4)

    override fun numberOfTraits(): Int = getStatementCount(ProbabilityManager::numberOfTraits)
}
