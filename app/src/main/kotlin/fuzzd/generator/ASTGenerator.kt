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
    fun generate(): ASTElement

    fun generateSequence(context: GenerationContext): SequenceAST

    fun generateStatement(context: GenerationContext): StatementAST

    fun generatePrintStatement(context: GenerationContext): PrintAST

    fun generateDeclarationStatement(context: GenerationContext): DeclarationAST

    fun generateExpression(context: GenerationContext, targetType: Type): ExpressionAST

    fun generateIdentifier(context: GenerationContext, targetType: Type): IdentifierAST

    fun generateUnaryExpression(context: GenerationContext, targetType: Type): UnaryExpressionAST

    fun generateBinaryExpression(context: GenerationContext, targetType: Type): BinaryExpressionAST

    fun generateLiteralForType(context: GenerationContext, targetType: Type): LiteralAST

    fun generateIntegerLiteral(context: GenerationContext): IntegerLiteralAST

    fun generateBooleanLiteral(context: GenerationContext): BooleanLiteralAST

    fun generateRealLiteral(context: GenerationContext): RealLiteralAST

    fun generateCharLiteral(context: GenerationContext): CharacterLiteralAST
}
