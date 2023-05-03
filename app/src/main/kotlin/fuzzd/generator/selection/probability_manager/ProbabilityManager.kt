package fuzzd.generator.selection.probability_manager

interface ProbabilityManager {
    fun classType(): Double
    fun traitType(): Double
    fun datatype(): Double
    fun arrayType(): Double
    fun datatstructureType(): Double
    fun literalType(): Double

    // Datastructure types
    fun setType(): Double
    fun multisetType(): Double
    fun mapType(): Double
    fun sequenceType(): Double
    fun stringType(): Double

    // literal types
    fun intType(): Double
    fun boolType(): Double
    fun charType(): Double

    // statements
    fun ifStatement(): Double
    fun matchStatement(): Double
    fun whileStatement(): Double
    fun methodCall(): Double
    fun mapAssign(): Double
    fun assignStatement(): Double
    fun classInstantiation(): Double

    // assign type
    fun assignIdentifier(): Double
    fun assignArrayIndex(): Double

    // expressions
    fun binaryExpression(): Double
    fun unaryExpression(): Double
    fun modulusExpression(): Double
    fun multisetConversion(): Double
    fun functionCall(): Double
    fun ternary(): Double
    fun matchExpression(): Double
    fun assignExpression(): Double
    fun indexExpression(): Double
    fun identifier(): Double
    fun literal(): Double
    fun constructor(): Double

    // index type
    fun arrayIndexType(): Double
    fun mapIndexType(): Double
    fun multisetIndexType(): Double
    fun sequenceIndexType(): Double
    fun stringIndexType(): Double
    fun datatypeIndexType(): Double

    // additional data
    fun methodStatements(): Int
    fun ifBranchStatements(): Int
    fun whileBodyStatements(): Int
    fun mainFunctionStatements(): Int
    fun matchStatements(): Int
}
