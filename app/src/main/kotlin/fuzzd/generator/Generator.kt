package fuzzd.generator

import fuzzd.generator.ast.ASTElement
import fuzzd.generator.ast.MainFunctionAST

class Generator {
    fun generateAST(): ASTElement = MainFunctionAST()
}
