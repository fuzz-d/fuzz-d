package fuzzd.generator

import fuzzd.generator.ast.ClassAST
import fuzzd.generator.ast.DafnyAST
import fuzzd.generator.ast.ExpressionAST
import fuzzd.generator.ast.ExpressionAST.ArrayIndexAST
import fuzzd.generator.ast.ExpressionAST.ArrayInitAST
import fuzzd.generator.ast.ExpressionAST.BinaryExpressionAST
import fuzzd.generator.ast.ExpressionAST.BooleanLiteralAST
import fuzzd.generator.ast.ExpressionAST.CharacterLiteralAST
import fuzzd.generator.ast.ExpressionAST.FunctionMethodCallAST
import fuzzd.generator.ast.ExpressionAST.IdentifierAST
import fuzzd.generator.ast.ExpressionAST.IntegerLiteralAST
import fuzzd.generator.ast.ExpressionAST.RealLiteralAST
import fuzzd.generator.ast.ExpressionAST.TernaryExpressionAST
import fuzzd.generator.ast.ExpressionAST.UnaryExpressionAST
import fuzzd.generator.ast.FunctionMethodAST
import fuzzd.generator.ast.FunctionMethodSignatureAST
import fuzzd.generator.ast.MainFunctionAST
import fuzzd.generator.ast.MethodAST
import fuzzd.generator.ast.MethodSignatureAST
import fuzzd.generator.ast.SequenceAST
import fuzzd.generator.ast.StatementAST
import fuzzd.generator.ast.StatementAST.AssignmentAST
import fuzzd.generator.ast.StatementAST.DeclarationAST
import fuzzd.generator.ast.StatementAST.IfStatementAST
import fuzzd.generator.ast.StatementAST.PrintAST
import fuzzd.generator.ast.StatementAST.WhileLoopAST
import fuzzd.generator.ast.TraitAST
import fuzzd.generator.ast.Type
import fuzzd.generator.ast.Type.ConstructorType.ArrayType
import fuzzd.generator.context.GenerationContext

interface ASTGenerator {
    /* ========================================== TOP LEVEL ========================================== */
    fun generate(): DafnyAST

    fun generateMainFunction(context: GenerationContext): MainFunctionAST

    fun generateTrait(context: GenerationContext): TraitAST

    fun generateClass(context: GenerationContext): ClassAST

    fun generateField(context: GenerationContext): IdentifierAST

    fun generateFunctionMethod(
        context: GenerationContext,
        targetType: Type? = null,
        literalParams: Boolean = false,
    ): FunctionMethodAST

    fun generateFunctionMethodSignature(
        context: GenerationContext,
        targetType: Type? = null,
        literalParams: Boolean = false,
    ): FunctionMethodSignatureAST

    fun generateMethod(context: GenerationContext): MethodAST

    fun generateMethodSignature(context: GenerationContext): MethodSignatureAST

    fun generateSequence(context: GenerationContext, maxStatements: Int = 15): SequenceAST

    /* ========================================== STATEMENTS ========================================== */

    fun generateStatement(context: GenerationContext): StatementAST

    fun generateIfStatement(context: GenerationContext): IfStatementAST

    fun generateWhileStatement(context: GenerationContext): WhileLoopAST

    fun generatePrintStatement(context: GenerationContext): PrintAST

    fun generateDeclarationStatement(context: GenerationContext): DeclarationAST

    fun generateAssignmentStatement(context: GenerationContext): AssignmentAST

    fun generateClassInstantiation(context: GenerationContext): DeclarationAST

    fun generateMethodCall(context: GenerationContext): StatementAST

    fun generateChecksum(context: GenerationContext): List<PrintAST>

    /* ========================================== EXPRESSIONS ========================================== */

    fun generateExpression(context: GenerationContext, targetType: Type): ExpressionAST

    fun generateFunctionMethodCall(context: GenerationContext, targetType: Type): FunctionMethodCallAST

    fun generateIdentifier(
        context: GenerationContext,
        targetType: Type,
        mutableConstraint: Boolean = false,
        initialisedConstraint: Boolean = true,
    ): IdentifierAST

    fun generateUnaryExpression(context: GenerationContext, targetType: Type): UnaryExpressionAST

    fun generateBinaryExpression(context: GenerationContext, targetType: Type): BinaryExpressionAST

    fun generateTernaryExpression(context: GenerationContext, targetType: Type): TernaryExpressionAST

    fun generateArrayInitialisation(context: GenerationContext, targetType: ArrayType): ArrayInitAST

    fun generateArrayIndex(context: GenerationContext, targetType: Type): ArrayIndexAST

    fun generateBaseExpressionForType(context: GenerationContext, targetType: Type): ExpressionAST

    fun generateIntegerLiteral(context: GenerationContext): IntegerLiteralAST

    fun generateBooleanLiteral(context: GenerationContext): BooleanLiteralAST

    fun generateRealLiteral(context: GenerationContext): RealLiteralAST

    fun generateCharLiteral(context: GenerationContext): CharacterLiteralAST

    /* ========================================== TYPE ========================================== */

    fun generateType(context: GenerationContext, literalOnly: Boolean = true): Type
}
