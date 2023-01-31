package fuzzd.generator.symbol_table

import fuzzd.generator.ast.MethodAST

class MethodCallTable {
    private val calls = mutableMapOf<MethodAST, Set<MethodAST>>()


}
