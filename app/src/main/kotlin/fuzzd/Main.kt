package fuzzd

import dafnyLexer
import dafnyParser
import fuzzd.generator.Generator
import fuzzd.generator.selection.SelectionManager
import fuzzd.logging.Logger
import fuzzd.logging.OutputWriter
import fuzzd.recondition.visitor.DafnyVisitor
import fuzzd.recondition.Reconditioner
import fuzzd.utils.WRAPPER_FUNCTIONS
import fuzzd.validator.OutputValidator
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import java.io.File
import java.util.UUID
import kotlin.random.Random

class Main(private val path: String) {
    private val validator = OutputValidator()

    fun fuzz(seed: Long = Random.Default.nextLong()) {
        // init
        val generator = Generator(SelectionManager(Random(seed)))
        val directory = UUID.randomUUID().toString()
        val logger = Logger(path, directory)

        logger.log { "Fuzzing with seed: $seed" }
        println("Fuzzing with seed: $seed")
        println("Output being written to directory: $directory")

        // generate program
        try {
            val ast = if (false /* TODO() */) {
                val input = File("../test.dfy").inputStream()
                val charStream = CharStreams.fromStream(input)
                val tokens = CommonTokenStream(dafnyLexer(charStream))

                DafnyVisitor().visitProgram(dafnyParser(tokens).program())
            } else {
                generator.generate()
            }

            val reconditioner = Reconditioner()
            val reconditionedAST = reconditioner.recondition(ast)

            // output program
            val writer = OutputWriter(path, directory, "$DAFNY_MAIN.$DAFNY_TYPE")
            WRAPPER_FUNCTIONS.forEach { wrapper -> writer.write { "$wrapper\n" } }
            writer.write { reconditionedAST }
            writer.close()

            // differential testing; log results
            val validationResult = validator.validateFile(writer.dirPath, DAFNY_MAIN)
            logger.log { validationResult }
        } catch (e: Exception) {
            // do nothing
            logger.log { "Generation threw error" }
            logger.log { "======================" }
            logger.log { e.stackTraceToString() }
        } finally {
            logger.close()
        }
    }

    companion object {
        private const val DAFNY_MAIN = "main"
        private const val DAFNY_TYPE = "dfy"
    }
}

fun main(args: Array<String>) {
    val seed = if (args.isNotEmpty()) {
        args[0].toLong()
    } else {
        Random.Default.nextLong()
    }

    Main("../output").fuzz(seed)
//    val generator = Generator(SelectionManager(Random(seed)))
//    val ast = generator.generate()
//    print(ast)
}
