package fuzzd.generator.ast

class MainFunctionAST(private val sequenceAST: SequenceAST) : ASTElement {

    override fun toString(): String {
        return "method Main() {\n$sequenceAST\n}"
    }
}
