package fuzzd.generator.ast

class DafnyAST(val topLevelElements: List<TopLevelAST>) : ASTElement {
    override fun toString(): String = topLevelElements.joinToString("\n")

    fun addPrintStatements(prints: List<StatementAST>) = DafnyAST(
        topLevelElements.map { topLevel ->
            if (topLevel is MainFunctionAST) {
                MainFunctionAST(topLevel.sequenceAST.addStatements(prints))
            } else {
                topLevel
            }
        }
    )
}
