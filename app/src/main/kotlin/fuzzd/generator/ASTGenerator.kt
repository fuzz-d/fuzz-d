package fuzzd.generator

import fuzzd.generator.ast.ClassAST
import fuzzd.generator.ast.DafnyAST
import fuzzd.generator.ast.DatatypeAST
import fuzzd.generator.ast.ExpressionAST
import fuzzd.generator.ast.ExpressionAST.ArrayIndexAST
import fuzzd.generator.ast.ExpressionAST.ArrayInitAST
import fuzzd.generator.ast.ExpressionAST.BinaryExpressionAST
import fuzzd.generator.ast.ExpressionAST.BooleanLiteralAST
import fuzzd.generator.ast.ExpressionAST.CharacterLiteralAST
import fuzzd.generator.ast.ExpressionAST.DatatypeInstantiationAST
import fuzzd.generator.ast.ExpressionAST.FunctionMethodCallAST
import fuzzd.generator.ast.ExpressionAST.IdentifierAST
import fuzzd.generator.ast.ExpressionAST.IntegerLiteralAST
import fuzzd.generator.ast.ExpressionAST.MapConstructorAST
import fuzzd.generator.ast.ExpressionAST.MatchExpressionAST
import fuzzd.generator.ast.ExpressionAST.ModulusExpressionAST
import fuzzd.generator.ast.ExpressionAST.MultisetConversionAST
import fuzzd.generator.ast.ExpressionAST.SequenceDisplayAST
import fuzzd.generator.ast.ExpressionAST.SetDisplayAST
import fuzzd.generator.ast.ExpressionAST.StringLiteralAST
import fuzzd.generator.ast.ExpressionAST.TernaryExpressionAST
import fuzzd.generator.ast.ExpressionAST.UnaryExpressionAST
import fuzzd.generator.ast.FunctionMethodAST
import fuzzd.generator.ast.FunctionMethodSignatureAST
import fuzzd.generator.ast.MainFunctionAST
import fuzzd.generator.ast.MethodAST
import fuzzd.generator.ast.MethodSignatureAST
import fuzzd.generator.ast.SequenceAST
import fuzzd.generator.ast.StatementAST
import fuzzd.generator.ast.TraitAST
import fuzzd.generator.ast.Type
import fuzzd.generator.ast.Type.ConstructorType.ArrayType
import fuzzd.generator.ast.Type.DatatypeType
import fuzzd.generator.context.GenerationContext

interface ASTGenerator {
    /* ========================================== TOP LEVEL ========================================== */
    fun generate(): DafnyAST

    fun generateMainFunction(context: GenerationContext): MainFunctionAST

    fun generateTrait(context: GenerationContext): TraitAST

    fun generateDatatype(context: GenerationContext): DatatypeAST

    fun generateClass(context: GenerationContext, mustExtend: List<TraitAST> = emptyList()): ClassAST

    fun generateField(context: GenerationContext): IdentifierAST

    fun generateFunctionMethod(context: GenerationContext, targetType: Type? = null): FunctionMethodAST

    fun generateFunctionMethodSignature(
        context: GenerationContext,
        targetType: Type? = null,
    ): FunctionMethodSignatureAST

    fun generateMethod(context: GenerationContext): MethodAST

    fun generateMethodSignature(context: GenerationContext): MethodSignatureAST

    fun generateSequence(context: GenerationContext, maxStatements: Int = 15): SequenceAST

    /* ========================================== STATEMENTS ========================================== */

    fun generateStatement(context: GenerationContext): List<StatementAST>

    fun generateMatchStatement(context: GenerationContext): List<StatementAST>

    fun generateIfStatement(context: GenerationContext): List<StatementAST>

    fun generateWhileStatement(context: GenerationContext): List<StatementAST>

    fun generatePrintStatement(context: GenerationContext): List<StatementAST>

    fun generateDeclarationStatement(context: GenerationContext): List<StatementAST>

    fun generateAssignmentStatement(context: GenerationContext): List<StatementAST>

    fun generateClassInstantiation(context: GenerationContext): List<StatementAST>

    fun generateMethodCall(context: GenerationContext): List<StatementAST>

    fun generateMapAssign(context: GenerationContext): List<StatementAST>

    /* ========================================== EXPRESSIONS ========================================== */

    fun generateExpression(context: GenerationContext, targetType: Type): Pair<ExpressionAST, List<StatementAST>>

    fun generateDatatypeInstantiation(
        context: GenerationContext,
        targetType: DatatypeType,
    ): Pair<DatatypeInstantiationAST, List<StatementAST>>

    fun generateMatchExpression(
        context: GenerationContext,
        targetType: Type,
    ): Pair<MatchExpressionAST, List<StatementAST>>

    fun generateFunctionMethodCall(
        context: GenerationContext,
        targetType: Type,
    ): Pair<FunctionMethodCallAST, List<StatementAST>>

    fun generateIdentifier(
        context: GenerationContext,
        targetType: Type,
        mutableConstraint: Boolean = false,
        initialisedConstraint: Boolean = true,
    ): Pair<IdentifierAST, List<StatementAST>>

    fun generateUnaryExpression(
        context: GenerationContext,
        targetType: Type,
    ): Pair<UnaryExpressionAST, List<StatementAST>>

    fun generateModulus(context: GenerationContext, targetType: Type): Pair<ModulusExpressionAST, List<StatementAST>>

    fun generateMultisetConversion(
        context: GenerationContext,
        targetType: Type,
    ): Pair<MultisetConversionAST, List<StatementAST>>

    fun generateBinaryExpression(
        context: GenerationContext,
        targetType: Type,
    ): Pair<BinaryExpressionAST, List<StatementAST>>

    fun generateTernaryExpression(
        context: GenerationContext,
        targetType: Type,
    ): Pair<TernaryExpressionAST, List<StatementAST>>

    fun generateArrayInitialisation(
        context: GenerationContext,
        targetType: ArrayType,
    ): Pair<ArrayInitAST, List<StatementAST>>

    fun generateArrayIndex(context: GenerationContext, targetType: Type): Pair<ArrayIndexAST, List<StatementAST>>

    fun generateSetDisplay(context: GenerationContext, targetType: Type): Pair<SetDisplayAST, List<StatementAST>>

    fun generateSequenceDisplay(
        context: GenerationContext,
        targetType: Type,
    ): Pair<SequenceDisplayAST, List<StatementAST>>

    fun generateMapConstructor(
        context: GenerationContext,
        targetType: Type,
    ): Pair<MapConstructorAST, List<StatementAST>>

    fun generateIndexAssign(
        context: GenerationContext,
        targetType: Type,
    ): Pair<ExpressionAST, List<StatementAST>>

    fun generateIndex(context: GenerationContext, targetType: Type): Pair<ExpressionAST, List<StatementAST>>

    fun generateBaseExpressionForType(
        context: GenerationContext,
        targetType: Type,
    ): Pair<ExpressionAST, List<StatementAST>>

    fun generateIntegerLiteral(context: GenerationContext): Pair<IntegerLiteralAST, List<StatementAST>>

    fun generateBooleanLiteral(context: GenerationContext): Pair<BooleanLiteralAST, List<StatementAST>>

    fun generateCharLiteral(context: GenerationContext): Pair<CharacterLiteralAST, List<StatementAST>>

    fun generateStringLiteral(context: GenerationContext): Pair<StringLiteralAST, List<StatementAST>>

    /* ========================================== TYPE ========================================== */

    fun generateType(context: GenerationContext): Type
}
