package fuzzd.generator.ast

class SequenceAST(private val statements: List<StatementAST>) : ASTElement {
    override fun toString(): String = statements.joinToString("\n") { s -> "\t$s" }
}
