package fuzzd

import fuzzd.generator.Generator
import fuzzd.generator.selection.SelectionManager
import fuzzd.logging.Logger
import fuzzd.logging.OutputWriter
import fuzzd.utils.DAFNY_BODY
import fuzzd.utils.DAFNY_GENERATED
import fuzzd.utils.DAFNY_MAIN
import fuzzd.utils.DAFNY_TYPE
import fuzzd.utils.DAFNY_WRAPPERS
import fuzzd.validator.OutputValidator
import kotlin.random.Random

class FuzzRunner(private val outputPath: String, private val outputDir: String, private val logger: Logger) {
    private val validator = OutputValidator()
    private val reconditionRunner = ReconditionRunner(outputPath, outputDir, logger)

    fun run(seed: Long, advanced: Boolean, instrument: Boolean, run: Boolean) {
        val generator = Generator<Any?>(SelectionManager(Random(seed)), instrument)

        logger.log { "Fuzzing with seed: $seed" }
        println("Fuzzing with seed: $seed")
        println("Output being written to directory: $outputDir")

        // generate program
        try {
            val ast = generator.generate()

            logger.log { "Generated ast" }

            val originalWriter = OutputWriter(outputPath, outputDir, "$DAFNY_GENERATED.$DAFNY_TYPE")
            originalWriter.write { ast }
            originalWriter.close()

            if (run) {
                reconditionRunner.run(ast, advanced)

                // differential testing; log results
                val validationResult = validator.validateFile(
                    originalWriter.dirPath,
                    DAFNY_WRAPPERS,
                    DAFNY_BODY,
                    DAFNY_MAIN,
                )

                logger.log { validationResult }
            }
        } catch (e: Exception) {
            // do nothing
            logger.log { "Generation threw error" }
            logger.log { "======================" }
            logger.log { e.stackTraceToString() }
            println(e.stackTraceToString())
        }
    }
}
