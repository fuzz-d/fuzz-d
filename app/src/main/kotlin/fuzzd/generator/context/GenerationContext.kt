package fuzzd.generator.context

import fuzzd.generator.ast.MethodAST
import fuzzd.generator.ast.StatementAST
import fuzzd.generator.symbol_table.GlobalSymbolTable
import fuzzd.generator.symbol_table.SymbolTable

data class GenerationContext(
    val globalSymbolTable: GlobalSymbolTable,
    val statementDepth: Int = 1,
    val expressionDepth: Int = 1,
    val symbolTable: SymbolTable = SymbolTable(),
    val methodContext: MethodAST? = null, // track if we are inside a method
    val onDemandIdentifiers: Boolean = true,
    private val dependentStatements: MutableList<StatementAST.DeclarationAST> = mutableListOf(),
) {
    fun addDependentStatement(statement: StatementAST.DeclarationAST) {
        dependentStatements.add(statement)
    }

    fun getDependentStatements(): List<StatementAST> {
        return dependentStatements
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
            methodContext,
            onDemandIdentifiers,
        )

    fun disableOnDemand(): GenerationContext =
        GenerationContext(
            globalSymbolTable,
            statementDepth,
            expressionDepth,
            symbolTable,
            methodContext,
            false,
            dependentStatements,
        )
}
