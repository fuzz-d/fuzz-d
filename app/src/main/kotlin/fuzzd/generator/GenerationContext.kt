package fuzzd.generator

data class GenerationContext(
    val statementDepth: Int = 1,
    val expressionDepth: Int = 1,
    val symbolTable: SymbolTable = SymbolTable()
) {
    fun increaseExpressionDepth(): GenerationContext =
        GenerationContext(statementDepth, expressionDepth + 1, symbolTable)

    fun increaseStatementDepth(): GenerationContext =
        GenerationContext(statementDepth + 1, expressionDepth, symbolTable)
}
