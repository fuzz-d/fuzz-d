package fuzzd.generator.ast

import fuzzd.utils.indent

class SequenceAST(val statements: List<StatementAST>) : ASTElement {
    override fun toString(): String = statements.joinToString("\n") { s -> indent(s) }

    fun addStatements(newStatements: List<StatementAST>): SequenceAST = SequenceAST(statements + newStatements)
}
