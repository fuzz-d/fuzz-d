package fuzzd.generator.context

import fuzzd.generator.ast.MethodAST
import fuzzd.generator.ast.StatementAST
import fuzzd.generator.ast.identifier_generator.NameGenerator.IdentifierNameGenerator
import fuzzd.generator.ast.identifier_generator.NameGenerator.LoopCounterGenerator
import fuzzd.generator.symbol_table.GlobalSymbolTable
import fuzzd.generator.symbol_table.SymbolTable

data class GenerationContext(
    val globalSymbolTable: GlobalSymbolTable,
    val statementDepth: Int = 1,
    val expressionDepth: Int = 1,
    val symbolTable: SymbolTable = SymbolTable(),
    val identifierNameGenerator: IdentifierNameGenerator = IdentifierNameGenerator(),
    val loopCounterGenerator: LoopCounterGenerator = LoopCounterGenerator(),
    val methodContext: MethodAST? = null, // track if we are inside a method
    val onDemandIdentifiers: Boolean = true,
    private val dependentStatements: MutableList<StatementAST.DeclarationAST> = mutableListOf(),
) {
    fun addDependentStatement(statement: StatementAST.DeclarationAST) {
        dependentStatements.add(statement)
    }

    fun clearDependentStatements(): List<StatementAST> {
        val statements = dependentStatements.map { it }.toList()
        dependentStatements.clear()
        return statements
    }

    fun increaseExpressionDepth(): GenerationContext =
        GenerationContext(
            globalSymbolTable,
            statementDepth,
            expressionDepth + 1,
            symbolTable,
            identifierNameGenerator,
            loopCounterGenerator,
            methodContext,
            onDemandIdentifiers,
            dependentStatements,
        )

    fun increaseStatementDepth(): GenerationContext =
        GenerationContext(
            globalSymbolTable,
            statementDepth + 1,
            expressionDepth,
            SymbolTable(symbolTable),
            identifierNameGenerator,
            loopCounterGenerator,
            methodContext,
            onDemandIdentifiers,
        )

    fun disableOnDemand(): GenerationContext =
        GenerationContext(
            globalSymbolTable,
            statementDepth,
            expressionDepth,
            symbolTable,
            identifierNameGenerator,
            loopCounterGenerator,
            methodContext,
            false,
            dependentStatements,
        )
}
