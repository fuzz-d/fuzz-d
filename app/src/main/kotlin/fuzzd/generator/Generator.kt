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
import fuzzd.generator.selection.SelectionManager
import fuzzd.generator.selection.StatementType
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.random.Random

class Generator(
    private val identifierNameGenerator: IdentifierNameGenerator,
    private val selectionManager: SelectionManager
) : ASTGenerator {
    private val random = Random.Default
    override suspend fun generate(): ASTElement = MainFunctionAST(generateSequence(GenerationContext()))

    override suspend fun generateSequence(context: GenerationContext): SequenceAST {
        val n = random.nextInt(1, 30)

        val statements = (1..n).map {
            withContext(Default) {
                generateStatement(context)
            }
        }.toList()

        return SequenceAST(statements)
    }

    override suspend fun generateStatement(context: GenerationContext): StatementAST =
        when (selectionManager.selectStatementType(context)) {
            StatementType.PRINT -> generatePrintStatement(context)
            StatementType.DECLARATION -> generateDeclarationStatement(context)
        }

    override suspend fun generatePrintStatement(context: GenerationContext): PrintAST =
        PrintAST(generateExpression(context, selectionManager.selectType()))

    override suspend fun generateDeclarationStatement(context: GenerationContext): DeclarationAST {
        val targetType = selectionManager.selectType()
        val identifier = IdentifierAST(identifierNameGenerator.newIdentifierName(), targetType)
        val expr = generateExpression(context, targetType)

        context.symbolTable.add(identifier)

        return DeclarationAST(identifier, expr)
    }

    override suspend fun generateExpression(context: GenerationContext, targetType: Type): ExpressionAST =
        if (random.nextFloat() < 1 / context.expressionDepth.toDouble()) {
            generateBinaryExpression(context, targetType)
        } else {
            generateLiteralForType(context, targetType)
        }

    override suspend fun generateIdentifier(context: GenerationContext): IdentifierAST {
        TODO()
    }

    override suspend fun generateUnaryExpression(context: GenerationContext, targetType: Type): UnaryExpressionAST {
        val expr = generateExpression(context, targetType)
        val operator = selectionManager.selectUnaryOperator(targetType)

        return UnaryExpressionAST(expr, operator)
    }

    override suspend fun generateBinaryExpression(context: GenerationContext, targetType: Type): BinaryExpressionAST {
        val nextDepthContext = context.increaseExpressionDepth()
        val (operator, inputType) = selectionManager.selectBinaryOperator(targetType)
        val expr1 = withContext(Default) { generateExpression(nextDepthContext, inputType) }
        val expr2 = withContext(Default) { generateExpression(nextDepthContext, inputType) }

        return BinaryExpressionAST(expr1, operator, expr2)
    }

    override suspend fun generateLiteralForType(context: GenerationContext, targetType: Type): LiteralAST =
        when (targetType) {
            Type.BoolType -> generateBooleanLiteral(context)
            Type.CharType -> generateCharLiteral(context)
            Type.IntType -> generateIntegerLiteral(context)
            Type.RealType -> generateRealLiteral(context)
        }

    override suspend fun generateIntegerLiteral(context: GenerationContext): IntegerLiteralAST {
        val value = if (random.nextBoolean()) {
            generateDecimalLiteralValue(negative = true)
        } else {
            generateHexLiteralValue(negative = true)
        }
        return IntegerLiteralAST(value)
    }

    override suspend fun generateBooleanLiteral(context: GenerationContext): BooleanLiteralAST =
        BooleanLiteralAST(random.nextBoolean())

    override suspend fun generateRealLiteral(context: GenerationContext): RealLiteralAST {
        val beforePoint = withContext(Default) { generateDecimalLiteralValue(negative = true) }
        val afterPoint = withContext(Default) { generateDecimalLiteralValue(negative = false) }

        return RealLiteralAST("$beforePoint.$afterPoint")
    }

    override suspend fun generateCharLiteral(context: GenerationContext): ExpressionAST.CharacterLiteralAST {
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
