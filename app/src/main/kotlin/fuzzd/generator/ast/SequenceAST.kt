package fuzzd.generator.ast

import fuzzd.utils.indent

class SequenceAST(val statements: List<StatementAST>) : ASTElement {
    private var isLive: Boolean = false

    override fun toString(): String = statements.joinToString("\n") { s -> indent(s) }

    fun addStatements(newStatements: List<StatementAST>): SequenceAST = SequenceAST(statements + newStatements)

    fun setLive() {
        isLive = true
    }

    fun isLive() = isLive
}
