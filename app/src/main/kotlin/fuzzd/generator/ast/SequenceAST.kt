package fuzzd.generator.ast

import fuzzd.utils.indent

class SequenceAST(private val statements: List<StatementAST>) : ASTElement {
    override fun toString(): String = statements.joinToString("\n") { s -> indent(s.toString()) }
}
