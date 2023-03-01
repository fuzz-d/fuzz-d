package fuzzd.generator.ast

class DafnyAST(val topLevelElements: List<TopLevelAST>) : ASTElement {
    override fun toString(): String = topLevelElements.joinToString("\n")
}
