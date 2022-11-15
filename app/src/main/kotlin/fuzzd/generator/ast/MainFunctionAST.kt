package fuzzd.generator.ast

class MainFunctionAST(val sequenceAST: SequenceAST) : ASTElement {

    override fun toString(): String {
        return "method Main() {\n$sequenceAST\n}"
    }
}
