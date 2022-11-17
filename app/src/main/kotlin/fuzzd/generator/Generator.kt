package fuzzd.generator

import fuzzd.generator.ast.ASTElement
import fuzzd.generator.ast.ExpressionAST
import fuzzd.generator.ast.ExpressionAST.BooleanLiteralAST
import fuzzd.generator.ast.ExpressionAST.IntegerLiteralAST
import fuzzd.generator.ast.ExpressionAST.RealLiteralAST
import fuzzd.generator.ast.MainFunctionAST
import fuzzd.generator.ast.SequenceAST
import fuzzd.generator.ast.StatementAST
import fuzzd.generator.ast.StatementAST.PrintAST
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.random.Random

class Generator : ASTGenerator {
    private val random = Random.Default
    override suspend fun generate(): ASTElement = withContext(Default) {
        MainFunctionAST(generateSequence())
    }

    override suspend fun generateSequence(): SequenceAST {
        val n = random.nextInt(1, 30)

        val statements = (1..n).map {
            withContext(Default) {
                generateStatement()
            }
        }.toList()

        return SequenceAST(statements)
    }

    override suspend fun generateStatement(): StatementAST = PrintAST(generateExpression())

    override suspend fun generateExpression(): ExpressionAST =
        if (random.nextBoolean()) {
            generateBooleanLiteral()
        } else if (random.nextBoolean()) {
            generateIntegerLiteral()
        } else {
            generateRealLiteral()
        }

    override suspend fun generateIntegerLiteral(): ExpressionAST {
        val value = if (random.nextBoolean()) {
            generateDecimalLiteralValue(negative = true)
        } else {
            generateHexLiteralValue(negative = true)
        }
        return IntegerLiteralAST(value)
    }

    override suspend fun generateBooleanLiteral(): ExpressionAST = BooleanLiteralAST(random.nextBoolean())

    override suspend fun generateRealLiteral(): ExpressionAST {
        val beforePoint = withContext(Default) { generateDecimalLiteralValue(negative = true) }
        val afterPoint = withContext(Default) { generateDecimalLiteralValue(negative = false) }

        return RealLiteralAST("$beforePoint.$afterPoint")
    }

    private fun generateDecimalLiteralValue(negative: Boolean = true): String {
        var value = random.nextInt()

        val sb = StringBuilder()
        sb.append(if (negative && value < 0) "-" else "")

        value = abs(value)
        do {
            sb.append(value % 10)
            value /= 10
            if (value != 0 && random.nextBoolean()) sb.append("_")
        } while (value > 0)

        return sb.toString()
    }

    fun generateHexLiteralValue(negative: Boolean = true): String {
        val hexString = Integer.toHexString(random.nextInt())

        val sb = StringBuilder()
        if (negative && random.nextBoolean()) sb.append("-")
        sb.append("0x")

        for (i in hexString.indices) {
            val c = hexString[i]
            sb.append(c)
            if (i < hexString.length - 1 && random.nextBoolean()) sb.append('_')
        }

        return sb.toString()
    }
}
