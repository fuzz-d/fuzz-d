package fuzzd

import fuzzd.generator.Generator
import fuzzd.generator.selection.SelectionManager
import fuzzd.logging.Logger
import fuzzd.logging.OutputWriter
import fuzzd.validator.OutputValidator
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

        // generate program
        try {
            val ast = generator.generate()

            // output program
            val writer = OutputWriter(path, directory, "$DAFNY_MAIN.$DAFNY_TYPE")
            writer.write { ast }
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
