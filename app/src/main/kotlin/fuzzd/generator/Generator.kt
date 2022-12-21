package fuzzd.generator

import fuzzd.generator.ast.ASTElement
import fuzzd.generator.ast.ExpressionAST
import fuzzd.generator.ast.ExpressionAST.BinaryExpressionAST
import fuzzd.generator.ast.ExpressionAST.BooleanLiteralAST
import fuzzd.generator.ast.ExpressionAST.IdentifierAST
import fuzzd.generator.ast.ExpressionAST.IntegerLiteralAST
import fuzzd.generator.ast.ExpressionAST.LiteralAST
import fuzzd.generator.ast.ExpressionAST.RealLiteralAST
import fuzzd.generator.ast.ExpressionAST.UnaryExpressionAST
import fuzzd.generator.ast.MainFunctionAST
import fuzzd.generator.ast.SequenceAST
import fuzzd.generator.ast.StatementAST
import fuzzd.generator.ast.StatementAST.DeclarationAST
import fuzzd.generator.ast.StatementAST.PrintAST
import fuzzd.generator.ast.Type
import fuzzd.generator.selection.ExpressionType
import fuzzd.generator.selection.SelectionManager
import fuzzd.generator.selection.StatementType
import kotlin.math.abs
import kotlin.random.Random

class Generator(
    private val identifierNameGenerator: IdentifierNameGenerator,
    private val selectionManager: SelectionManager
) : ASTGenerator {
    private val random = Random.Default
    override fun generate(): ASTElement = MainFunctionAST(generateSequence(GenerationContext()))

    override fun generateSequence(context: GenerationContext): SequenceAST {
        val n = random.nextInt(1, 30)

        val statements = (1..n).map { generateStatement(context) }.toList()

        val allStatements = context.getDependentStatements() + statements
        context.clearDependentStatements()

        return SequenceAST(allStatements)
    }

    override fun generateStatement(context: GenerationContext): StatementAST =
        when (selectionManager.selectStatementType(context)) {
            StatementType.PRINT -> generatePrintStatement(context)
            StatementType.DECLARATION -> generateDeclarationStatement(context)
        }

    override fun generatePrintStatement(context: GenerationContext): PrintAST =
        PrintAST(generateExpression(context, selectionManager.selectType()))

    override fun generateDeclarationStatement(context: GenerationContext): DeclarationAST {
        val targetType = selectionManager.selectType()
        val identifier = IdentifierAST(identifierNameGenerator.newIdentifierName(), targetType)
        val expr = generateExpression(context, targetType)

        context.symbolTable.add(identifier)

        return DeclarationAST(identifier, expr)
    }

    override fun generateExpression(context: GenerationContext, targetType: Type): ExpressionAST =
        when (selectionManager.selectExpressionType(targetType, context)) {
            ExpressionType.BINARY -> generateBinaryExpression(context, targetType)
            ExpressionType.LITERAL -> generateLiteralForType(context, targetType)
            ExpressionType.IDENTIFIER -> generateIdentifier(context, targetType)
            ExpressionType.UNARY -> generateUnaryExpression(context, targetType)
        }

    override fun generateIdentifier(context: GenerationContext, targetType: Type): IdentifierAST {
        if (!context.symbolTable.hasType(targetType::class)) {
            val ident = IdentifierAST(identifierNameGenerator.newIdentifierName(), targetType)
            val expr = generateLiteralForType(context, targetType)
            context.addDependentStatement(DeclarationAST(ident, expr))
            context.symbolTable.add(ident)
        }

        return selectionManager.randomSelection(context.symbolTable.withType(targetType::class))
    }

    override fun generateUnaryExpression(context: GenerationContext, targetType: Type): UnaryExpressionAST {
        val expr = generateExpression(context, targetType)
        val operator = selectionManager.selectUnaryOperator(targetType)

        return UnaryExpressionAST(expr, operator)
    }

    override fun generateBinaryExpression(context: GenerationContext, targetType: Type): BinaryExpressionAST {
        val nextDepthContext = context.increaseExpressionDepth()
        val (operator, inputType) = selectionManager.selectBinaryOperator(targetType)
        val expr1 = generateExpression(nextDepthContext, inputType)
        val expr2 = generateExpression(nextDepthContext, inputType)

        return BinaryExpressionAST(expr1, operator, expr2)
    }

    override fun generateLiteralForType(context: GenerationContext, targetType: Type): LiteralAST =
        when (targetType) {
            Type.BoolType -> generateBooleanLiteral(context)
            Type.CharType -> generateCharLiteral(context)
            Type.IntType -> generateIntegerLiteral(context)
            Type.RealType -> generateRealLiteral(context)
        }

    override fun generateIntegerLiteral(context: GenerationContext): IntegerLiteralAST {
        val value = if (selectionManager.randomWeightedSelection(listOf(true to 0.7, false to 0.3))) {
            generateDecimalLiteralValue(negative = true)
        } else {
            generateHexLiteralValue(negative = true)
        }
        return IntegerLiteralAST(value)
    }

    override fun generateBooleanLiteral(context: GenerationContext): BooleanLiteralAST =
        BooleanLiteralAST(random.nextBoolean())

    override fun generateRealLiteral(context: GenerationContext): RealLiteralAST {
        val beforePoint = generateDecimalLiteralValue(negative = true)
        val afterPoint = generateDecimalLiteralValue(negative = false)

        return RealLiteralAST("$beforePoint.$afterPoint")
    }

    override fun generateCharLiteral(context: GenerationContext): ExpressionAST.CharacterLiteralAST {
        return ExpressionAST.CharacterLiteralAST("'c'")
    }

    private fun generateDecimalLiteralValue(negative: Boolean = true): String {
        var value = random.nextInt(1000)

        val sb = StringBuilder()
        sb.append(if (negative && value < 0) "-" else "")

        value = abs(value)
        do {
            sb.append(value % 10)
            value /= 10
//            if (value != 0 && random.nextBoolean()) sb.append("_")
        } while (value > 0)

        return sb.toString()
    }

    private fun generateHexLiteralValue(negative: Boolean = true): String {
        val hexString = Integer.toHexString(random.nextInt(1000))

        val sb = StringBuilder()
        if (negative && random.nextBoolean()) sb.append("-")
        sb.append("0x")

        for (i in hexString.indices) {
            val c = hexString[i]
            sb.append(c)
//            if (i < hexString.length - 1 && random.nextBoolean()) sb.append('_')
        }

        return sb.toString()
    }
}
