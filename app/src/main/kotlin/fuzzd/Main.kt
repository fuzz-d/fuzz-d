package fuzzd

import fuzzd.generator.Generator
import fuzzd.generator.IdentifierNameGenerator
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
        val generator = Generator(IdentifierNameGenerator(), SelectionManager(Random(seed)))
        val directory = UUID.randomUUID().toString()
        val logger = Logger(path, directory)

        logger.log { "Fuzzing with seed: $seed" }

        // generate program
        val ast = generator.generate()
        // output program
        val writer = OutputWriter(path, directory, "$DAFNY_MAIN.$DAFNY_TYPE")
        writer.write { ast }
        writer.close()

        // differential testing; log results
        val validationResult = validator.validateFile(writer.dirPath, DAFNY_MAIN)
        logger.log { validationResult }

        logger.close()
    }

    companion object {
        private const val DAFNY_MAIN = "main"
        private const val DAFNY_TYPE = "dfy"
    }
}

fun main() = Main("../output").fuzz()
