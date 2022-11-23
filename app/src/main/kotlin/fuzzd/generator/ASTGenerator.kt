package fuzzd.generator

import fuzzd.generator.ast.ASTElement
import fuzzd.generator.ast.ExpressionAST
import fuzzd.generator.ast.ExpressionAST.BinaryExpressionAST
import fuzzd.generator.ast.ExpressionAST.BooleanLiteralAST
import fuzzd.generator.ast.ExpressionAST.CharacterLiteralAST
import fuzzd.generator.ast.ExpressionAST.IdentifierAST
import fuzzd.generator.ast.ExpressionAST.IntegerLiteralAST
import fuzzd.generator.ast.ExpressionAST.LiteralAST
import fuzzd.generator.ast.ExpressionAST.RealLiteralAST
import fuzzd.generator.ast.ExpressionAST.UnaryExpressionAST
import fuzzd.generator.ast.SequenceAST
import fuzzd.generator.ast.StatementAST
import fuzzd.generator.ast.StatementAST.DeclarationAST
import fuzzd.generator.ast.StatementAST.PrintAST
import fuzzd.generator.ast.Type

interface ASTGenerator {
    suspend fun generate(): ASTElement

    suspend fun generateSequence(context: GenerationContext): SequenceAST

    suspend fun generateStatement(context: GenerationContext): StatementAST

    suspend fun generatePrintStatement(context: GenerationContext): PrintAST

    suspend fun generateDeclarationStatement(context: GenerationContext): DeclarationAST

    suspend fun generateExpression(context: GenerationContext, targetType: Type): ExpressionAST

    suspend fun generateIdentifier(context: GenerationContext, targetType: Type): IdentifierAST

    suspend fun generateUnaryExpression(context: GenerationContext, targetType: Type): UnaryExpressionAST

    suspend fun generateBinaryExpression(context: GenerationContext, targetType: Type): BinaryExpressionAST

    suspend fun generateLiteralForType(context: GenerationContext, targetType: Type): LiteralAST

    suspend fun generateIntegerLiteral(context: GenerationContext): IntegerLiteralAST

    suspend fun generateBooleanLiteral(context: GenerationContext): BooleanLiteralAST

    suspend fun generateRealLiteral(context: GenerationContext): RealLiteralAST

    suspend fun generateCharLiteral(context: GenerationContext): CharacterLiteralAST
}
