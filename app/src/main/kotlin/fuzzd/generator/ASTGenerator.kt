package fuzzd.generator

import fuzzd.generator.ast.*
import fuzzd.generator.ast.ExpressionAST.ArrayIndexAST
import fuzzd.generator.ast.ExpressionAST.ArrayInitAST
import fuzzd.generator.ast.ExpressionAST.BinaryExpressionAST
import fuzzd.generator.ast.ExpressionAST.BooleanLiteralAST
import fuzzd.generator.ast.ExpressionAST.CharacterLiteralAST
import fuzzd.generator.ast.ExpressionAST.FunctionMethodCallAST
import fuzzd.generator.ast.ExpressionAST.IdentifierAST
import fuzzd.generator.ast.ExpressionAST.IntegerLiteralAST
import fuzzd.generator.ast.ExpressionAST.LiteralAST
import fuzzd.generator.ast.ExpressionAST.RealLiteralAST
import fuzzd.generator.ast.ExpressionAST.UnaryExpressionAST
import fuzzd.generator.ast.StatementAST.AssignmentAST
import fuzzd.generator.ast.StatementAST.DeclarationAST
import fuzzd.generator.ast.StatementAST.IfStatementAST
import fuzzd.generator.ast.StatementAST.PrintAST
import fuzzd.generator.ast.StatementAST.WhileLoopAST
import fuzzd.generator.ast.Type.ArrayType
import fuzzd.generator.ast.Type.LiteralType
import fuzzd.generator.context.GenerationContext

interface ASTGenerator {
    fun generate(): TopLevelAST

    fun generateMainFunction(context: GenerationContext): MainFunctionAST

    fun generateFunctionMethodAST(context: GenerationContext, targetType: Type? = null): FunctionMethodAST

    fun generateSequence(context: GenerationContext): SequenceAST

    fun generateStatement(context: GenerationContext): StatementAST

    fun generateIfStatement(context: GenerationContext): IfStatementAST

    fun generateWhileStatement(context: GenerationContext): WhileLoopAST

    fun generatePrintStatement(context: GenerationContext): PrintAST

    fun generateDeclarationStatement(context: GenerationContext): DeclarationAST

    fun generateAssignmentStatement(context: GenerationContext): AssignmentAST

    fun generateExpression(context: GenerationContext, targetType: Type): ExpressionAST

    fun generateFunctionMethodCall(context: GenerationContext, targetType: Type): FunctionMethodCallAST

    fun generateIdentifier(context: GenerationContext, targetType: Type): IdentifierAST

    fun generateUnaryExpression(context: GenerationContext, targetType: Type): UnaryExpressionAST

    fun generateBinaryExpression(context: GenerationContext, targetType: Type): BinaryExpressionAST

    fun generateArrayInitialisation(context: GenerationContext, targetType: ArrayType): ArrayInitAST

    fun generateArrayIndex(context: GenerationContext, targetType: Type): ArrayIndexAST

    fun generateLiteralForType(context: GenerationContext, targetType: LiteralType): LiteralAST

    fun generateIntegerLiteral(context: GenerationContext): IntegerLiteralAST

    fun generateBooleanLiteral(context: GenerationContext): BooleanLiteralAST

    fun generateRealLiteral(context: GenerationContext): RealLiteralAST

    fun generateCharLiteral(context: GenerationContext): CharacterLiteralAST
}
