package fuzzd.generator

import fuzzd.generator.ast.ASTElement
import fuzzd.generator.ast.ExpressionAST
import fuzzd.generator.ast.SequenceAST
import fuzzd.generator.ast.StatementAST

interface ASTGenerator {
    suspend fun generate(): ASTElement

    suspend fun generateSequence(): SequenceAST

    fun generateStatement(): StatementAST

    fun generateExpression(): ExpressionAST
}
