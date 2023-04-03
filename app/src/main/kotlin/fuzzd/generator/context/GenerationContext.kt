package fuzzd.generator.context

import fuzzd.generator.ast.ClassAST
import fuzzd.generator.ast.MethodSignatureAST
import fuzzd.generator.ast.StatementAST
import fuzzd.generator.ast.identifier_generator.NameGenerator.IdentifierNameGenerator
import fuzzd.generator.ast.identifier_generator.NameGenerator.LoopCounterGenerator
import fuzzd.generator.symbol_table.FunctionSymbolTable
import fuzzd.generator.symbol_table.SymbolTable

data class GenerationContext(
    val functionSymbolTable: FunctionSymbolTable,
    val statementDepth: Int = 1,
    val expressionDepth: Int = 1,
    val symbolTable: SymbolTable = SymbolTable(),
    val identifierNameGenerator: IdentifierNameGenerator = IdentifierNameGenerator(),
    val loopCounterGenerator: LoopCounterGenerator = LoopCounterGenerator(),
    val methodContext: MethodSignatureAST? = null, // track if we are inside a method
    val onDemandIdentifiers: Boolean = true,
    private val dependentStatements: MutableList<StatementAST.DeclarationAST> = mutableListOf(),
) {
    private lateinit var globalState: ClassAST

    fun globalState() = globalState

    fun setGlobalState(globalState: ClassAST): GenerationContext {
        this.globalState = globalState
        return this
    }

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
            functionSymbolTable,
            statementDepth,
            expressionDepth + 1,
            symbolTable,
            identifierNameGenerator,
            loopCounterGenerator,
            methodContext,
            onDemandIdentifiers,
            dependentStatements,
        ).setGlobalState(globalState)

    fun increaseStatementDepth(): GenerationContext =
        GenerationContext(
            functionSymbolTable,
            statementDepth + 1,
            expressionDepth,
            SymbolTable(symbolTable),
            identifierNameGenerator,
            loopCounterGenerator,
            methodContext,
            onDemandIdentifiers,
        ).setGlobalState(globalState)

    fun disableOnDemand(): GenerationContext =
        GenerationContext(
            functionSymbolTable,
            statementDepth,
            expressionDepth,
            symbolTable,
            identifierNameGenerator,
            loopCounterGenerator,
            methodContext,
            false,
            dependentStatements,
        ).setGlobalState(globalState)
}
