package fuzzd.generator.ast

class TopLevelAST(private val ast: List<ASTElement>) : ASTElement {
    override fun toString(): String = ast.joinToString("\n")
}
