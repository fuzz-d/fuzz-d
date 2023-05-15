package fuzzd.generator.selection.probability_manager

class VerifierProbabilityManager(val probabilityManager: ProbabilityManager) : ProbabilityManager {
    override fun classType(): Double = 0.0
    override fun traitType(): Double = 0.0
    override fun datatype(): Double = probabilityManager.datatype()
    override fun arrayType(): Double = 0.0
    override fun datatstructureType(): Double = probabilityManager.datatstructureType()
    override fun literalType(): Double = probabilityManager.literalType()

    // Datastructure types
    override fun setType(): Double = probabilityManager.setType()
    override fun multisetType(): Double = probabilityManager.multisetType()
    override fun mapType(): Double = probabilityManager.multisetType()
    override fun sequenceType(): Double = probabilityManager.sequenceType()
    override fun stringType(): Double = probabilityManager.stringType()

    // literal types
    override fun intType(): Double = probabilityManager.intType()
    override fun boolType(): Double = probabilityManager.boolType()
    override fun charType(): Double = probabilityManager.charType()

    // Statements
    override fun assertStatement(): Double = 0.4
    override fun ifStatement(): Double = probabilityManager.ifStatement()
    override fun matchStatement(): Double = probabilityManager.matchStatement()
    override fun forallStatement(): Double = 0.0
    override fun forLoopStatement(): Double = 0.0
    override fun whileStatement(): Double = probabilityManager.whileStatement() / 3
    override fun methodCall(): Double = probabilityManager.methodCall()
    override fun mapAssign(): Double = probabilityManager.mapAssign()
    override fun assignStatement(): Double = probabilityManager.assignStatement()
    override fun multiAssignStatement(): Double = probabilityManager.multiAssignStatement()
    override fun classInstantiation(): Double = 0.0

    // Decl info
    override fun constField(): Double = probabilityManager.constField()

    // Assign types
    override fun assignIdentifier(): Double = probabilityManager.assignIdentifier()
    override fun assignArrayIndex(): Double = 0.0

    // Expressions
    override fun binaryExpression(): Double = probabilityManager.binaryExpression()
    override fun unaryExpression(): Double = probabilityManager.unaryExpression()
    override fun modulusExpression(): Double = probabilityManager.modulusExpression()
    override fun multisetConversion(): Double = probabilityManager.multisetConversion()
    override fun functionCall(): Double = probabilityManager.functionCall()
    override fun ternary(): Double = probabilityManager.ternary()
    override fun matchExpression(): Double = probabilityManager.matchExpression()
    override fun assignExpression(): Double = probabilityManager.assignExpression()
    override fun indexExpression(): Double = probabilityManager.indexExpression()
    override fun identifier(): Double = probabilityManager.identifier()
    override fun literal(): Double = probabilityManager.literal()
    override fun constructor(): Double = probabilityManager.constructor()
    override fun comprehension(): Double = 0.0
    override fun comprehensionConditionIntRange(): Double = probabilityManager.comprehensionConditionIntRange()

    // Index types
    override fun arrayIndexType(): Double = 0.0
    override fun mapIndexType(): Double = probabilityManager.mapIndexType()
    override fun multisetIndexType(): Double = probabilityManager.multisetIndexType()
    override fun sequenceIndexType(): Double = probabilityManager.sequenceIndexType()
    override fun stringIndexType(): Double = probabilityManager.stringIndexType()
    override fun datatypeIndexType(): Double = 0.0

    // Array Init types
    override fun arrayInitDefault(): Double = 0.0
    override fun arrayInitComprehension(): Double = 0.0
    override fun arrayInitValues(): Double = 0.0

    // Other info
    override fun methodStatements(): Int = 5
    override fun ifBranchStatements(): Int = 5
    override fun forLoopBodyStatements(): Int = 4
    override fun whileBodyStatements(): Int = 4
    override fun mainFunctionStatements(): Int = 5
    override fun matchStatements(): Int = 3
    override fun comprehensionIdentifiers(): Int = probabilityManager.comprehensionIdentifiers()
    override fun numberOfTraits(): Int = 0

    override fun maxNumberOfAssigns(): Int = probabilityManager.maxNumberOfAssigns()

    // Verification mutation
    override fun mutateVerificationCondition(): Double = 0.1
    override fun mutateAssertFalse(): Double = 0.3
}
