package fuzzd.generator

import fuzzd.generator.ast.ASTElement
import fuzzd.generator.ast.ExpressionAST
import fuzzd.generator.ast.SequenceAST
import fuzzd.generator.ast.StatementAST

interface ASTGenerator {
    suspend fun generate(): ASTElement

    suspend fun generateSequence(): SequenceAST

    suspend fun generateStatement(): StatementAST

    suspend fun generateExpression(): ExpressionAST

    suspend fun generateIntegerLiteral(): ExpressionAST

    suspend fun generateBooleanLiteral(): ExpressionAST

    suspend fun generateRealLiteral(): ExpressionAST
}
