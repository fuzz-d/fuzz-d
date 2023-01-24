package fuzzd.generator.context

import fuzzd.generator.ast.ASTElement
import fuzzd.generator.ast.StatementAST
import fuzzd.generator.symbol_table.GlobalSymbolTable
import fuzzd.generator.symbol_table.SymbolTable

data class GenerationContext(
    val globalSymbolTable: GlobalSymbolTable,
    val statementDepth: Int = 1,
    val expressionDepth: Int = 1,
    val symbolTable: SymbolTable = SymbolTable(),
    private val dependentStatements: MutableList<StatementAST.DeclarationAST> = mutableListOf()
) {
    private var topLevelFunctions = mutableListOf<ASTElement>()

    fun addDependentStatement(statement: StatementAST.DeclarationAST) {
        dependentStatements.add(statement)
    }

    fun getDependentStatements(): List<StatementAST> {
        return dependentStatements
    }

    fun clearDependentStatements() {
        dependentStatements.clear()
    }

    fun increaseExpressionDepth(): GenerationContext =
        GenerationContext(globalSymbolTable, statementDepth, expressionDepth + 1, symbolTable, dependentStatements)

    fun increaseStatementDepth(): GenerationContext =
        GenerationContext(globalSymbolTable, statementDepth + 1, expressionDepth, SymbolTable(symbolTable))

}
