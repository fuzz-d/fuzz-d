package fuzzd.generator

import fuzzd.generator.ast.ASTElement
import fuzzd.generator.ast.ExpressionAST
import fuzzd.generator.ast.ExpressionAST.BooleanLiteralAST
import fuzzd.generator.ast.ExpressionAST.IntegerLiteralAST
import fuzzd.generator.ast.MainFunctionAST
import fuzzd.generator.ast.SequenceAST
import fuzzd.generator.ast.StatementAST
import fuzzd.generator.ast.StatementAST.PrintAST
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random

class Generator : ASTGenerator {
    private val random = Random.Default
    override suspend fun generate(): ASTElement = withContext(Dispatchers.Default) {
        MainFunctionAST(generateSequence())
    }

    override suspend fun generateSequence(): SequenceAST {
        val n = random.nextInt(1, 30)

        val statements = (1..n).map {
            withContext(Dispatchers.Default) {
                generateStatement()
            }
        }.toList()

        return SequenceAST(statements)
    }

    override fun generateStatement(): StatementAST =
        PrintAST(generateExpression())

    override fun generateExpression(): ExpressionAST =
        if (random.nextBoolean()) {
            BooleanLiteralAST(random.nextBoolean())
        } else {
            IntegerLiteralAST(random.nextInt())
        }
}
